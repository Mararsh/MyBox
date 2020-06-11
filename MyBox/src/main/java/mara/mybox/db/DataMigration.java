package mara.mybox.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.data.ConvolutionKernel;
import mara.mybox.data.EpidemicReport;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.GeographyCodeLevel;
import static mara.mybox.db.DerbyBase.BatchSize;
import static mara.mybox.db.DerbyBase.columnNames;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.dropTables;
import static mara.mybox.db.DerbyBase.initTables;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-06-09
 * @License Apache License Version 2.0
 */
public class DataMigration {

    public static boolean checkUpdates() {
        AppVariables.setSystemConfigValue("CurrentVersion", CommonValues.AppVersion);
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            List<String> installed = TableStringValues.read(conn, "InstalledVersions");
            if (installed.contains(CommonValues.AppVersion)) {
                return true;
            }
            List<String> columns = columnNames(conn, "Geography_Code");
            if (columns.contains("owner")) {
                TableStringValues.add("InstalledVersions", "6.3.1");
                return true;
            }
            if (installed.contains("6.3")) {
                migrateFrom63(conn);
                return true;
            }
            columns = columnNames(conn, "Epidemic_Report");
            if (columns.contains("country")) {
                migrateFrom621(conn);
                return true;
            }
            migrateBefore621();

        } catch (Exception e) {
            logger.debug(e.toString());
        }

        return true;
    }

    private static void migrateFrom63(Connection conn) {
        try ( Statement statement = conn.createStatement()) {
            String sql = "ALTER TABLE Geography_Code add column altitude DOUBLE ";
            statement.executeUpdate(sql);
            sql = "ALTER TABLE Geography_Code  add column precision DOUBLE";
            statement.executeUpdate(sql);
            sql = "ALTER TABLE Geography_Code  add column owner BIGINT REFERENCES Geography_Code (gcid) ON DELETE CASCADE ON UPDATE RESTRICT";
            statement.executeUpdate(sql);
            sql = "ALTER TABLE Geography_Code  add column coordinate_system SMALLINT";
            statement.executeUpdate(sql);
            sql = "UPDATE Geography_Code  SET area=area*1000000";
            statement.executeUpdate(sql);
            sql = "UPDATE Epidemic_Report  SET data_set='COVID-19_Tecent' WHERE data_set='COVID-19_Tencent'";
            statement.executeUpdate(sql);
            sql = "DROP VIEW Epidemic_Report_Statistic_View";
            statement.executeUpdate(sql);
            statement.executeUpdate(TableEpidemicReport.CreateStatisticView);
        } catch (Exception e) {
            logger.debug(e.toString());
            return;
        }
        try ( PreparedStatement update = conn.prepareStatement(TableGeographyCode.Update)) {
            String sql = "SELECT * FROM Geography_Code";
            int count = 0;
            conn.setAutoCommit(false);
            try ( ResultSet results = conn.createStatement().executeQuery(sql)) {
                while (results.next()) {
                    GeographyCode code = TableGeographyCode.readResults(results);
                    TableGeographyCode.setUpdate(conn, update, code);
                    update.addBatch();
                    if (++count % BatchSize == 0) {
                        update.executeBatch();
                        conn.commit();
                    }
                }
            }
            update.executeBatch();
            conn.commit();
            TableStringValues.add("InstalledVersions", "6.3.1");
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private static void migrateFrom621(Connection conn) {
        exportGeographyCode621(conn);
        exportEpidemicReport621(conn);
        try ( Statement statement = conn.createStatement()) {
            try {
                statement.executeUpdate("DROP TABLE Epidemic_Report");
            } catch (Exception e) {
                logger.debug(e.toString());
            }
            try {
                statement.executeUpdate("DROP TABLE Geography_Code");
            } catch (Exception e) {
                logger.debug(e.toString());
            }

            new TableGeographyCode().init(conn);
            statement.executeUpdate(TableGeographyCode.Create_Index_levelIndex);
            statement.executeUpdate(TableGeographyCode.Create_Index_gcidIndex);
            statement.executeUpdate(TableGeographyCode.Create_Index_codeIndex);

            new TableEpidemicReport().init(conn);
            statement.executeUpdate(TableEpidemicReport.Create_Index_DatasetTimeDesc);
            statement.executeUpdate(TableEpidemicReport.Create_Index_DatasetTimeAsc);
            statement.executeUpdate(TableEpidemicReport.Create_Index_TimeAsc);
            statement.executeUpdate(TableEpidemicReport.CreateStatisticView);

            TableStringValues.add("InstalledVersions", "6.3");
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private static void exportGeographyCode621(Connection conn) {
        try {
            String sql = "SELECT * FROM Geography_Code ORDER BY level, country, province, city";
            List<GeographyCode> codes = new ArrayList<>();
            try ( ResultSet results = conn.createStatement().executeQuery(sql)) {
                while (results.next()) {
                    try {
                        String address = results.getString("address");
                        if (address == null) {
                            break;
                        }
                        GeographyCode code = new GeographyCode();
                        String level = results.getString("level");
                        GeographyCodeLevel levelCode = new GeographyCodeLevel(level);
                        code.setLevelCode(levelCode);
                        code.setLongitude(results.getDouble("longitude"));
                        code.setLatitude(results.getDouble("latitude"));
                        if (AppVariables.isChinese()) {
                            code.setChineseName(address);
                        } else {
                            code.setEnglishName(address);
                        }
                        code.setCountryName(results.getString("country"));
                        code.setProvinceName(results.getString("province"));
                        code.setCityName(results.getString("city"));
                        code.setCode2(results.getString("citycode"));
                        code.setCountyName(results.getString("district"));
                        code.setTownName(results.getString("township"));
                        code.setVillageName(results.getString("neighborhood"));
                        code.setBuildingName(results.getString("building"));
                        code.setCode1(results.getString("administrative_code"));
                        code.setComments(results.getString("street"));
                        codes.add(code);
                    } catch (Exception e) {
//                    logger.debug(e.toString());
                        break;
                    }
                }
            }
            if (!codes.isEmpty()) {
                File tmpFile = new File(AppVariables.MyboxDataPath + File.separator + "data"
                        + File.separator + "GeographyCode6.2.1Exported.csv");
                GeographyCode.writeExternalCSV(tmpFile, codes);
                AppVariables.setSystemConfigValue("GeographyCode621Exported", tmpFile.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private static void exportEpidemicReport621(Connection conn) {
        try {
            String sql = "SELECT * FROM Epidemic_Report ORDER BY level, country, province, city";
            List<EpidemicReport> reports = new ArrayList<>();
            try ( ResultSet results = conn.createStatement().executeQuery(sql)) {
                while (results.next()) {
                    try {
                        String levelValue = results.getString("level");
                        if (levelValue == null) {
                            break;
                        }
                        GeographyCodeLevel levelCode = new GeographyCodeLevel(levelValue);
                        GeographyCode code = new GeographyCode();
                        code.setLevelCode(levelCode);
                        code.setCountryName(results.getString("country"));
                        code.setProvinceName(results.getString("province"));
                        code.setCityName(results.getString("city"));
                        code.setCountyName(results.getString("district"));
                        code.setTownName(results.getString("township"));
                        code.setVillageName(results.getString("neighborhood"));
                        code.setLongitude(results.getDouble("longitude"));
                        code.setLatitude(results.getDouble("latitude"));

                        EpidemicReport report = new EpidemicReport();
                        report.setLocation(code);
                        report.setDataSet(results.getString("data_set"));
                        report.setConfirmed(results.getInt("confirmed"));
                        report.setHealed(results.getInt("healed"));
                        report.setDead(results.getInt("dead"));
                        report.setIncreasedConfirmed(results.getInt("increased_confirmed"));
                        report.setIncreasedHealed(results.getInt("increased_healed"));
                        report.setIncreasedDead(results.getInt("increased_dead"));
                        Date d = results.getTimestamp("time");
                        if (d != null) {
                            report.setTime(d.getTime());
                        }
                        report.setSource("Filled".equals(results.getString("comments")) ? 3 : 2);
                        reports.add(report);

                    } catch (Exception e) {
//                    logger.debug(e.toString());
                        break;
                    }
                }
            }
            if (!reports.isEmpty()) {
                File tmpFile = new File(AppVariables.MyboxDataPath + File.separator + "data"
                        + File.separator + "EpidemicReport6.2.1Exported.csv");
                EpidemicReport.writeExternalCSV(tmpFile, reports, null);
                AppVariables.setSystemConfigValue("EpidemicReport621Exported", tmpFile.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public static boolean migrateBefore621() {
        try {
            if (!AppVariables.getSystemConfigBoolean("UpdatedTables4.2", false)) {
                logger.info("Updating tables in 4.2...");
                List<ConvolutionKernel> records = TableConvolutionKernel.read();
                TableConvolutionKernel t = new TableConvolutionKernel();
                t.drop();
                t.init();
                if (TableConvolutionKernel.write(records)) {
                    AppVariables.setSystemConfigValue("UpdatedTables4.2", true);
                }
            }

            if (!AppVariables.getSystemConfigBoolean("UpdatedTables5.4", false)) {
                logger.info("Updating tables in 5.4...");
                DerbyBase t = new DerbyBase();
                String sql = "ALTER TABLE User_Conf  alter  column  key_Name set data type VARCHAR(1024)";
                t.update(sql);
                sql = "ALTER TABLE User_Conf  alter  column  string_Value set data type VARCHAR(32672)";
                t.update(sql);
                sql = "ALTER TABLE User_Conf  alter  column  default_string_Value set data type VARCHAR(32672)";
                t.update(sql);
                sql = "ALTER TABLE System_Conf  alter  column  key_Name set data type VARCHAR(1024)";
                t.update(sql);
                sql = "ALTER TABLE System_Conf  alter  column  string_Value set data type VARCHAR(32672)";
                t.update(sql);
                sql = "ALTER TABLE System_Conf  alter  column  default_string_Value set data type VARCHAR(32672)";
                t.update(sql);
                sql = "ALTER TABLE image_history  add  column  temp VARCHAR(128)";
                t.update(sql);
                sql = "UPDATE image_history SET temp=CHAR(update_type)";
                t.update(sql);
                sql = "ALTER TABLE image_history drop column update_type";
                t.update(sql);
                sql = "RENAME COLUMN image_history.temp TO update_type";
                t.update(sql);
                sql = "ALTER TABLE image_history  add  column  object_type VARCHAR(128)";
                t.update(sql);
                sql = "ALTER TABLE image_history  add  column  op_type VARCHAR(128)";
                t.update(sql);
                sql = "ALTER TABLE image_history  add  column  scope_type  VARCHAR(128)";
                t.update(sql);
                sql = "ALTER TABLE image_history  add  column  scope_name  VARCHAR(1024)";
                t.update(sql);
                sql = "DROP TABLE image_init";
                t.update(sql);
                AppVariables.setSystemConfigValue("UpdatedTables5.4", true);
            }

            if (!AppVariables.getSystemConfigBoolean("UpdatedTables5.8", false)) {
                logger.info("Updating tables in 5.8...");
                DerbyBase t = new DerbyBase();
                String sql = "ALTER TABLE SRGB  add  column  palette_index  INT";
                t.update(sql);

                List<String> saveColors = TableStringValues.read("ColorPalette");
                if (saveColors != null && !saveColors.isEmpty()) {
                    TableColorData.updatePalette(saveColors);
                }
                TableStringValues.clear("ColorPalette");
                AppVariables.setSystemConfigValue("UpdatedTables5.8", true);
            }

            if (!AppVariables.getSystemConfigBoolean("UpdatedTables5.9", false)) {
                logger.info("Updating tables in 5.9...");
                DerbyBase t = new DerbyBase();
                String sql = "DROP TABLE Browser_URLs";
                t.update(sql);
                AppVariables.setSystemConfigValue("UpdatedTables5.9", true);
            }

            if (!AppVariables.getSystemConfigBoolean("UpdatedTables6.1", false)) {
                logger.info("Updating tables in 6.1...");
                if (TableColorData.migrate()) {
                }
                AppVariables.setSystemConfigValue("UpdatedTables6.1", true);
            }

            if (!AppVariables.getSystemConfigBoolean("UpdatedTables6.1.5", false)) {
                logger.info("Updating tables in 6.1.5...");
                migrateGeographyCode615();
                migrateEpidemicReport615();
                AppVariables.setSystemConfigValue("UpdatedTables6.1.5", true);
            }

            if (!AppVariables.getSystemConfigBoolean("UpdatedTables6.2.1a", false)) {
                logger.info("Updating tables in 6.2.1...");
                migrateGeographyCode621();
                migrateEpidemicReport621();
                AppVariables.setSystemConfigValue("UpdatedTables6.2.1", true);
            }

            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean migrateGeographyCode615() {
        int size = TableGeographyCode.size();
        if (size > 0) {
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                     Statement statement = conn.createStatement()) {
                String sql = "UPDATE Geography_Code SET level='" + message("City")
                        + "' WHERE level IS NULL";
                statement.executeUpdate(sql);

            } catch (Exception e) {
                logger.debug(e.toString());
                return false;
            }

            File tmpFile = new File(AppVariables.MyboxDataPath + File.separator + "data"
                    + File.separator + "Geography_Code" + (new Date().getTime()) + ".del");
            tmpFile.mkdirs();
            DerbyBase.exportData("Geography_Code", tmpFile.getAbsolutePath());
            AppVariables.setSystemConfigValue("GeographyCodeBackup6.1.5", tmpFile.getAbsolutePath());

        }
        new TableGeographyCode().drop();
        new TableGeographyCode().init();
        return true;
    }

    public static boolean migrateGeographyCode621() {
        int size = TableGeographyCode.size();
        if (size > 0) {
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                     Statement statement = conn.createStatement()) {
                Map<String, String> provincesMap = new HashMap<>();
                String sql = "SELECT * FROM Geography_Code WHERE "
                        + " level='" + message("zh", "Province") + "' "
                        + " OR level='" + message("en", "Province") + "' ";
                try ( ResultSet results = statement.executeQuery(sql)) {
                    while (results.next()) {
                        GeographyCode province = TableGeographyCode.readResults(results);
                        if (province.getFullName() != null) {
                            provincesMap.put(province.getFullName(), province.getName());
                        }
                    }
                }
                Set<String> provinces = provincesMap.keySet();
                provinces.remove("");
                for (String fullAddress : provinces) {
                    sql = "UPDATE Geography_Code "
                            + " SET province='" + provincesMap.get(fullAddress) + "' "
                            + " WHERE province='" + fullAddress + "'";
                    statement.executeUpdate(sql);
                }

                Map<String, String> citiesMap = new HashMap<>();
                sql = "SELECT * FROM Geography_Code WHERE "
                        + " level='" + message("City") + "' ";
                try ( ResultSet results = statement.executeQuery(sql)) {
                    while (results.next()) {
                        GeographyCode city = TableGeographyCode.readResults(results);
                        if (city.getFullName() != null) {
                            citiesMap.put(city.getFullName(), city.getName());
                        }
                    }
                }
                Set<String> cities = citiesMap.keySet();
                cities.remove("");
                for (String fullAddress : cities) {
                    sql = "UPDATE Geography_Code "
                            + " SET city='" + citiesMap.get(fullAddress) + "' "
                            + " WHERE city='" + fullAddress + "'";
                    statement.executeUpdate(sql);
                }

                sql = "DELETE FROM Geography_Code "
                        + " WHERE country='" + message("Macao")
                        + "' OR country='" + message("Macau") + "'";
                statement.executeUpdate(sql);
            } catch (Exception e) {
                logger.debug(e.toString());
                return false;
            }
        }
        return true;
    }

    public static boolean migrateEpidemicReport615() {
        int size = TableEpidemicReport.size();
        if (size > 0) {
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                     Statement statement = conn.createStatement()) {
                String sql = "ALTER TABLE Epidemic_Report  add  column  level VARCHAR(1024)";
                statement.executeUpdate(sql);
                sql = "ALTER TABLE Epidemic_Report  add  column  district VARCHAR(2048)";
                statement.executeUpdate(sql);
                sql = "ALTER TABLE Epidemic_Report  add  column  township VARCHAR(2048)";
                statement.executeUpdate(sql);
                sql = "ALTER TABLE Epidemic_Report  add  column  neighborhood VARCHAR(2048)";
                statement.executeUpdate(sql);
                sql = "ALTER TABLE Epidemic_Report  add  column  increased_confirmed INTEGER";
                statement.executeUpdate(sql);
                sql = "ALTER TABLE Epidemic_Report  add  column  increased_suspected INTEGER";
                statement.executeUpdate(sql);
                sql = "ALTER TABLE Epidemic_Report  add  column  increased_healed INTEGER";
                statement.executeUpdate(sql);
                sql = "ALTER TABLE Epidemic_Report  add  column  increased_dead INTEGER";
                statement.executeUpdate(sql);

                sql = "ALTER TABLE Epidemic_Report  alter column time NOT NULL";
                statement.executeUpdate(sql);

                sql = "UPDATE Epidemic_Report SET level='" + message("Global")
                        + "' WHERE country IS NULL AND province IS NULL ";
                statement.executeUpdate(sql);

                sql = "UPDATE Epidemic_Report SET level='" + message("Country")
                        + "' WHERE country IS NOT NULL AND province IS NULL";
                statement.executeUpdate(sql);

                sql = "UPDATE Epidemic_Report SET level='" + message("Province")
                        + "' WHERE province IS NOT NULL";
                statement.executeUpdate(sql);

                sql = "UPDATE Epidemic_Report SET level='" + message("City")
                        + "' WHERE level IS NULL";
                statement.executeUpdate(sql);

                sql = "ALTER TABLE Epidemic_Report  alter column level NOT NULL";
                statement.executeUpdate(sql);

            } catch (Exception e) {
                logger.debug(e.toString());
                return false;
            }
            File tmpFile = new File(AppVariables.MyboxDataPath + File.separator + "data"
                    + File.separator + "Epidemic_Report_backup6.1_" + (new Date().getTime()) + ".del");
            tmpFile.mkdirs();
            DerbyBase.exportData("Epidemic_Report", tmpFile.getAbsolutePath());
            AppVariables.setSystemConfigValue("EpidemicReportBackup6.1.5", tmpFile.getAbsolutePath());
        }

        new TableEpidemicReport().drop();
        new TableEpidemicReport().init();
        return true;
    }

    public static boolean migrateEpidemicReport621() {
        int size = TableGeographyCode.size();
        if (size > 0) {
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                     Statement statement = conn.createStatement()) {
                String sql = "UPDATE Epidemic_Report SET level='" + message("Country")
                        + "', country='" + message("Monaco") + "', province=null, city=null, "
                        + " longitude=7.42, latitude=43.74 "
                        + " WHERE province='摩纳哥'";
                statement.executeUpdate(sql);

                sql = "DELETE FROM Epidemic_Report "
                        + " WHERE country='" + message("Macao")
                        + "' OR country='" + message("Macau") + "'";
                statement.executeUpdate(sql);
            } catch (Exception e) {
                logger.debug(e.toString());
                return false;
            }
        }
        return true;
    }

    private static void resetDB() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText(AppVariables.message("SureClear"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonYes = new ButtonType(AppVariables.message("Yes"));
            ButtonType buttonNo = new ButtonType(AppVariables.message("No"));
            alert.getButtonTypes().setAll(buttonYes, buttonNo);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonYes) {
                dropTables();
                initTables();
                TableStringValues.add("InstalledVersions", CommonValues.AppVersion);
            }
        });
    }

}
