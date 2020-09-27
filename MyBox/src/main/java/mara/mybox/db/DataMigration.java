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
import mara.mybox.data.ColorData;
import mara.mybox.data.ConvolutionKernel;
import mara.mybox.data.CoordinateSystem;
import mara.mybox.data.Dataset;
import mara.mybox.data.EpidemicReport;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.GeographyCodeLevel;
import mara.mybox.data.Location;
import mara.mybox.data.tools.EpidemicReportTools;
import mara.mybox.data.tools.GeographyCodeTools;
import static mara.mybox.db.DerbyBase.BatchSize;
import static mara.mybox.db.DerbyBase.columnNames;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.dropTables;
import static mara.mybox.db.DerbyBase.failed;
import static mara.mybox.db.DerbyBase.initTables;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import static mara.mybox.db.DerbyBase.tables;
import static mara.mybox.db.TableColorData.read;
import static mara.mybox.db.TableColorData.write;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.AppVariables;
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
            if (installed.isEmpty()) {
                TableStringValues.add("InstalledVersions", CommonValues.AppVersion);
                return true;
            }
            if (installed.contains(CommonValues.AppVersion)) {
                return true;
            }
            List<String> columns = columnNames(conn, "Geography_Code");
            if (columns.contains("predefined")) {
                updateIn633(conn);
            }
            List<String> tables = tables(conn);
            if (tables.contains("Location_Data".toUpperCase())) {
                return true;
            }
            if (tables.contains("Location".toUpperCase())) {
                updateIn632(conn);
            }
            if (columns.contains("owner")) {
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
            conn.setAutoCommit(true);
        } catch (Exception e) {
            logger.debug(e.toString());
        }

        return true;
    }

    private static void updateIn633(Connection conn) {
        try {
            logger.info("Updating tables in 6.3.3...");
            updateGeographyCodeIn633(conn);
            updateColorDataIn633(conn);
            updateConvolutionKernelIn633(conn);
            TableStringValues.add("InstalledVersions", "6.3.3");
            conn.setAutoCommit(true);
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private static boolean updateGeographyCodeIn633(Connection conn) {
        try ( Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);
            String sql = "ALTER TABLE Geography_Code ADD COLUMN gcsource SMALLINT";
            statement.executeUpdate(sql);
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
        try ( Statement statement = conn.createStatement()) {
            String sql = "UPDATE Geography_Code SET gcsource=1 WHERE predefined=true";
            statement.executeUpdate(sql);
        } catch (Exception e) {
        }
        try ( Statement statement = conn.createStatement()) {
            String sql = "UPDATE Geography_Code SET gcsource=1 WHERE predefined=1";
            statement.executeUpdate(sql);
        } catch (Exception e) {
        }
        try ( Statement statement = conn.createStatement()) {
            String sql = "UPDATE Geography_Code SET gcsource=2 WHERE predefined=false";
            statement.executeUpdate(sql);
        } catch (Exception e) {
        }
        try ( Statement statement = conn.createStatement()) {
            String sql = "UPDATE Geography_Code SET gcsource=2 WHERE predefined=0";
            statement.executeUpdate(sql);
        } catch (Exception e) {
        }
        try ( Statement statement = conn.createStatement()) {
            String sql = "ALTER TABLE Geography_Code DROP COLUMN predefined";
            statement.executeUpdate(sql);
        } catch (Exception e) {
        }
        return true;
    }

    private static void updateColorDataIn633(Connection conn) {
        try ( Statement statement = conn.createStatement();
                 PreparedStatement delete = conn.prepareStatement(TableColorData.Delete);) {
            conn.setAutoCommit(false);
            try ( ResultSet results = statement.executeQuery("SELECT * FROM Color_Data")) {
                while (results.next()) {
                    ColorData data = read(results);
                    String rgba = data.getRgba();
                    String rgbaUpper = rgba.toUpperCase();
                    if (rgba.equals(rgbaUpper)) {
                        continue;
                    }
                    data.setRgba(rgbaUpper);
                    TableColorData.insert(conn, data);
                    delete.setString(1, rgba);
                    delete.executeUpdate();
                }
            }
            conn.commit();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private static boolean updateConvolutionKernelIn633(Connection conn) {
        try ( Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);
            String sql = "ALTER TABLE Convolution_Kernel ADD COLUMN is_gray BOOLEAN";
            statement.executeUpdate(sql);
            sql = "ALTER TABLE Convolution_Kernel ADD COLUMN is_invert BOOLEAN";
            statement.executeUpdate(sql);
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
        try ( Statement statement = conn.createStatement()) {
            String sql = "UPDATE Convolution_Kernel SET is_gray=true WHERE gray>0";
            statement.executeUpdate(sql);
        } catch (Exception e) {
        }
        try ( Statement statement = conn.createStatement()) {
            String sql = "UPDATE Convolution_Kernel SET is_gray=false WHERE gray<1";
            statement.executeUpdate(sql);
        } catch (Exception e) {
        }
        try ( Statement statement = conn.createStatement()) {
            String sql = "ALTER TABLE Convolution_Kernel DROP COLUMN gray";
            statement.executeUpdate(sql);
        } catch (Exception e) {
        }
        return true;
    }

    private static void updateIn632(Connection conn) {
        try {
            logger.info("Updating tables in 6.3.2...");
            updateForeignKeysIn632(conn);
            updateGeographyCodeIn632(conn);
            updateLocationIn632(conn);
            TableStringValues.add("InstalledVersions", "6.3.2");
            conn.setAutoCommit(true);
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private static void updateForeignKeysIn632(Connection conn) {
        try ( Statement query = conn.createStatement();
                 Statement update = conn.createStatement()) {
            conn.setAutoCommit(true);
            String sql = "SELECT tablename, constraintName FROM SYS.SYSTABLES t, SYS.SYSCONSTRAINTS c  where t.TABLEID=c.TABLEID AND type='F'";
            try ( ResultSet results = query.executeQuery(sql)) {
                while (results.next()) {
                    String tablename = results.getString("tablename");
                    String constraintName = results.getString("constraintName");
                    sql = "ALTER TABLE " + tablename + " DROP FOREIGN KEY \"" + constraintName + "\"";
//                    logger.debug(sql);
                    update.executeUpdate(sql);
                }
            }
            sql = "ALTER TABLE Geography_Code ADD CONSTRAINT Geography_Code_owner_fk FOREIGN KEY (owner)"
                    + " REFERENCES GEOGRAPHY_CODE (gcid) ON DELETE RESTRICT ON UPDATE RESTRICT";
            update.executeUpdate(sql);
            sql = "ALTER TABLE Epidemic_Report ADD CONSTRAINT Epidemic_Report_locationid_fk FOREIGN KEY (locationid)"
                    + " REFERENCES GEOGRAPHY_CODE (gcid) ON DELETE RESTRICT ON UPDATE RESTRICT";
            update.executeUpdate(sql);
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private static void updateGeographyCodeIn632(Connection conn) {
        try ( Statement statement = conn.createStatement();
                 PreparedStatement update = conn.prepareStatement(TableGeographyCode.Update)) {
            conn.setAutoCommit(false);
            try ( ResultSet results = statement.executeQuery("SELECT * FROM Geography_Code WHERE gcid < 5000")) {
                while (results.next()) {
                    GeographyCode code = TableGeographyCode.readResults(results);
//                    logger.debug(code.getGcid() + " " + code.getName() + " "
//                            + code.getLongitude() + " " + code.getLatitude() + " " + code.getCoordinateSystem().intValue());
                    code = GeographyCodeTools.toCGCS2000(code, true);
//                    logger.debug(code.getLongitude() + " " + code.getLatitude() + " " + code.getCoordinateSystem().intValue());
                    TableGeographyCode.update(conn, update, code);
                }
            }
            conn.commit();
            conn.setAutoCommit(true);
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private static void updateLocationIn632(Connection conn) {
        TableLocationData tableLocationData = new TableLocationData();
        try ( Statement statement = conn.createStatement();
                 PreparedStatement locationIinsert = conn.prepareStatement(tableLocationData.insertStatement())) {
            int insertCount = 0;
            conn.setAutoCommit(false);
            try ( ResultSet results = statement.executeQuery("SELECT * FROM Location")) {
                Map<String, Dataset> datasets = new HashMap<>();
                while (results.next()) {
                    Location data = new Location();
                    String datasetName = results.getString("data_set");
                    Dataset dataset = datasets.get(datasetName);
                    if (dataset == null) {
                        dataset = tableLocationData.queryAndCreateDataset(conn, datasetName);
                        datasets.put(datasetName, dataset);
                    }
                    data.setDataset(dataset);
                    data.setLabel(results.getString("data_label"));
                    data.setAddress(results.getString("address"));
                    data.setLongitude(results.getDouble("longitude"));
                    data.setLatitude(results.getDouble("latitude"));
                    data.setAltitude(results.getDouble("altitude"));
                    data.setPrecision(results.getDouble("precision"));
                    data.setSpeed(results.getDouble("speed"));
                    data.setDirection(results.getShort("direction"));
                    data.setCoordinateSystem(new CoordinateSystem(results.getShort("coordinate_system")));
                    data.setDataValue(results.getDouble("data_value"));
                    data.setDataSize(results.getDouble("data_size"));
                    Date d = results.getTimestamp("data_time");
                    if (d != null) {
                        data.setStartTime(d.getTime() * (results.getShort("data_time_bc") >= 0 ? 1 : -1));
                    }
                    data.setImage(results.getString("image_location"));
                    data.setComments(results.getString("comments"));
                    tableLocationData.setInsertStatement(conn, locationIinsert, data);
                    locationIinsert.addBatch();
                    if (++insertCount % BatchSize == 0) {
                        locationIinsert.executeBatch();
                        conn.commit();
                    }
                }
            }
            locationIinsert.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            try {
                statement.executeUpdate("DROP TABLE Location");
            } catch (Exception e) {
//                logger.debug(e.toString());
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private static void migrateFrom63(Connection conn) {
        try ( Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);
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
            conn.setAutoCommit(true);
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private static void migrateFrom621(Connection conn) {
        exportGeographyCode621(conn);
        exportEpidemicReport621(conn);
        try ( Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);
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

            new TableGeographyCode().createTable(conn);
            statement.executeUpdate(TableGeographyCode.Create_Index_levelIndex);
            statement.executeUpdate(TableGeographyCode.Create_Index_gcidIndex);
            statement.executeUpdate(TableGeographyCode.Create_Index_codeIndex);

            new TableEpidemicReport().createTable(conn);
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
                File path = new File(AppVariables.MyboxDataPath + File.separator + "migration" + File.separator);
                path.mkdirs();
                File tmpFile = new File(path.getAbsoluteFile() + File.separator + "GeographyCode6.2.1Exported.csv");
                GeographyCodeTools.writeExternalCSV(tmpFile, codes);
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
                File path = new File(AppVariables.MyboxDataPath + File.separator + "migration" + File.separator);
                path.mkdirs();
                File tmpFile = new File(path.getAbsoluteFile() + File.separator + "EpidemicReport6.2.1Exported.csv");
                EpidemicReportTools.writeExternalCSV(tmpFile, reports, null);
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
                String sql = "ALTER TABLE User_Conf  alter  column  key_Name set data type VARCHAR(1024)";
                DerbyBase.update(sql);
                sql = "ALTER TABLE User_Conf  alter  column  default_string_Value set data type VARCHAR(32672)";
                DerbyBase.update(sql);
                sql = "ALTER TABLE System_Conf  alter  column  key_Name set data type VARCHAR(1024)";
                DerbyBase.update(sql);
                sql = "ALTER TABLE System_Conf  alter  column  string_Value set data type VARCHAR(32672)";
                DerbyBase.update(sql);
                sql = "ALTER TABLE System_Conf  alter  column  default_string_Value set data type VARCHAR(32672)";
                DerbyBase.update(sql);
                sql = "ALTER TABLE image_history  add  column  temp VARCHAR(128)";
                DerbyBase.update(sql);
                sql = "UPDATE image_history SET temp=CHAR(update_type)";
                DerbyBase.update(sql);
                sql = "ALTER TABLE image_history drop column update_type";
                DerbyBase.update(sql);
                sql = "RENAME COLUMN image_history.temp TO update_type";
                DerbyBase.update(sql);
                sql = "ALTER TABLE image_history  add  column  object_type VARCHAR(128)";
                DerbyBase.update(sql);
                sql = "ALTER TABLE image_history  add  column  op_type VARCHAR(128)";
                DerbyBase.update(sql);
                sql = "ALTER TABLE image_history  add  column  scope_type  VARCHAR(128)";
                DerbyBase.update(sql);
                sql = "ALTER TABLE image_history  add  column  scope_name  VARCHAR(1024)";
                DerbyBase.update(sql);
                sql = "DROP TABLE image_init";
                DerbyBase.update(sql);
                AppVariables.setSystemConfigValue("UpdatedTables5.4", true);
            }

            if (!AppVariables.getSystemConfigBoolean("UpdatedTables5.8", false)) {
                logger.info("Updating tables in 5.8...");
                String sql = "ALTER TABLE SRGB  add  column  palette_index  INT";
                DerbyBase.update(sql);

                List<String> saveColors = TableStringValues.read("ColorPalette");
                if (saveColors != null && !saveColors.isEmpty()) {
                    TableColorData.setPalette(saveColors);
                }
                TableStringValues.clear("ColorPalette");
                AppVariables.setSystemConfigValue("UpdatedTables5.8", true);
            }

            if (!AppVariables.getSystemConfigBoolean("UpdatedTables5.9", false)) {
                logger.info("Updating tables in 5.9...");
                String sql = "DROP TABLE Browser_URLs";
                DerbyBase.update(sql);
                AppVariables.setSystemConfigValue("UpdatedTables5.9", true);
            }

            if (!AppVariables.getSystemConfigBoolean("UpdatedTables6.1", false)) {
                logger.info("Updating tables in 6.1...");
                migrate61();
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            TableGeographyCode t = new TableGeographyCode();
            int size = t.size();
            if (size > 0) {
                try ( Statement statement = conn.createStatement()) {
                    String sql = "UPDATE Geography_Code SET level='" + message("City")
                            + "' WHERE level IS NULL";
                    statement.executeUpdate(sql);
                } catch (Exception e) {
                    logger.debug(e.toString());
                    return false;
                }

                File path = new File(AppVariables.MyboxDataPath + File.separator + "migration" + File.separator);
                path.mkdirs();
                File tmpFile = new File(path.getAbsoluteFile() + File.separator + "Geography_Code" + (new Date().getTime()) + ".del");
                DerbyBase.exportData("Geography_Code", tmpFile.getAbsolutePath());

            }
            t.dropTable(conn);
            t.createTable(conn);
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean migrateGeographyCode621() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            TableGeographyCode t = new TableGeographyCode();
            int size = t.size();
            if (size > 0) {
                try ( Statement statement = conn.createStatement()) {
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
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean migrateEpidemicReport615() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            TableEpidemicReport t = new TableEpidemicReport();
            int size = t.size();
            if (size > 0) {
                try ( Statement statement = conn.createStatement()) {
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
                File path = new File(AppVariables.MyboxDataPath + File.separator + "migration" + File.separator);
                path.mkdirs();
                File tmpFile = new File(path.getAbsoluteFile() + File.separator + "Epidemic_Report_backup6.1_" + (new Date().getTime()) + ".del");
                DerbyBase.exportData("Epidemic_Report", tmpFile.getAbsolutePath());
            }
            t.dropTable(conn);
            t.createTable(conn);
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean migrateEpidemicReport621() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            TableGeographyCode t = new TableGeographyCode();
            int size = t.size();
            if (size > 0) {
                try ( Statement statement = conn.createStatement()) {
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
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean migrate61() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            String sql = " SELECT * FROM SRGB WHERE palette_index >= 0";
            ResultSet olddata = conn.createStatement().executeQuery(sql);
            List<ColorData> oldData = new ArrayList<>();
            while (olddata.next()) {
                ColorData data = new ColorData(olddata.getString("color_value")).calculate();
                String name = olddata.getString("color_name");
                if (name != null && !name.isEmpty()) {
                    data.setColorName(name);
                }
                data.setPaletteIndex(olddata.getInt("palette_index"));
                oldData.add(data);
            }
            for (ColorData data : oldData) {
                write(conn, data, true);
            }
            sql = "DROP TABLE SRGB";
            conn.createStatement().executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            logger.debug(e.toString());
            return false;
        }
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
