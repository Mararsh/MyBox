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
import mara.mybox.data.CoordinateSystem;
import static mara.mybox.db.DerbyBase.BatchSize;
import mara.mybox.db.DerbyBase;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.db.data.Dataset;
import mara.mybox.db.data.EpidemicReport;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeLevel;
import mara.mybox.db.data.GeographyCodeTools;
import mara.mybox.db.data.ImageEditHistory;
import mara.mybox.db.data.Location;
import mara.mybox.db.table.TableColorData;
import static mara.mybox.db.table.TableColorData.read;
import static mara.mybox.db.table.TableColorData.write;
import mara.mybox.db.table.TableConvolutionKernel;
import mara.mybox.db.table.TableEpidemicReport;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.db.table.TableImageEditHistory;
import mara.mybox.db.table.TableLocationData;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.DevTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.FileTools;
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
        try ( Connection conn = DerbyBase.getConnection()) {
            int lastVersion = DevTools.lastVersion(conn);
            int currentVersion = DevTools.myboxVersion(CommonValues.AppVersion);
            if (lastVersion == currentVersion) {
                return true;
            }
            MyBoxLog.info("Last version: " + lastVersion + " " + "Current version: " + currentVersion);
            if (lastVersion > 0) {
                if (lastVersion < 6002001) {
                    migrateBefore621(conn);
                }
                if (lastVersion < 6003000) {
                    migrateFrom621(conn);
                }
                if (lastVersion < 6003002) {
                    migrateFrom63(conn);
                }
                if (lastVersion < 6003003) {
                    updateIn632(conn);
                }
                if (lastVersion < 6003004) {
                    updateIn633(conn);
                }
                if (lastVersion < 6003006) {
                    updateIn636(conn);
                }
                if (lastVersion < 6003008) {
                    updateIn638(conn);
                }
                if (lastVersion < 6004001) {
                    updateIn641(conn);
                }
            }
            TableStringValues.add(conn, "InstalledVersions", CommonValues.AppVersion);
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return true;
    }

    private static void updateIn641(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.4.1...");
            String sql = "SELECT * FROM image_history";
            TableImageEditHistory tableImageEditHistory = new TableImageEditHistory();
            try ( Statement statement = conn.createStatement();
                     ResultSet results = statement.executeQuery(sql)) {
                while (results.next()) {
                    ImageEditHistory his = new ImageEditHistory();
                    his.setImage(results.getString("image_location"));
                    his.setHistoryLocation(results.getString("history_location"));
                    his.setUpdateType(results.getString("update_type"));
                    his.setObjectType(results.getString("object_type"));
                    his.setOpType(results.getString("op_type"));
                    his.setScopeType(results.getString("scope_type"));
                    his.setScopeName(results.getString("scope_name"));
                    his.setOperationTime(results.getTimestamp("operation_time"));
                    tableImageEditHistory.insertData(conn, his);
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            conn.commit();
            try ( Statement statement = conn.createStatement()) {
                statement.executeUpdate("DROP TABLE image_history");
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            TableStringValues.add(conn, "InstalledVersions", "6.4.1");
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private static void updateIn638(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.3.8...");
            if (AppVariables.MyBoxLanguagesPath.exists() && AppVariables.MyBoxLanguagesPath.isDirectory()) {
                File[] files = AppVariables.MyBoxLanguagesPath.listFiles();
                if (files != null && files.length > 0) {
                    MyBoxLog.info("Change language files names...");
                    for (File file : files) {
                        String name = file.getName();
                        if (!file.isFile() || (name.endsWith(".properties") && name.startsWith("Messages_"))) {
                            continue;
                        }
                        FileTools.rename(file, ConfigTools.interfaceLanguageFile(name));
                    }
                }
            }
            TableStringValues.add(conn, "InstalledVersions", "6.3.8");
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private static void updateIn636(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.3.6...");
            File log4j2Path = new File(System.getProperty("user.home") + File.separator + "log4j2");
            if (log4j2Path.exists() && log4j2Path.isDirectory()) {
                File[] files = log4j2Path.listFiles();
                if (files != null && files.length > 0) {
                    MyBoxLog.info("Clearing MyBox logs generated by log4j2...");
                    for (File pathFile : files) {
                        if (pathFile.isFile()) {
                            if (pathFile.getName().startsWith("MyBox")) {
                                FileTools.delete(pathFile);
                            }
                        } else if (pathFile.isDirectory()) {
                            File[] subPaths = pathFile.listFiles();
                            if (subPaths != null && subPaths.length > 0) {
                                for (File subPathsFile : subPaths) {
                                    if (subPathsFile.isFile()) {
                                        if (subPathsFile.getName().startsWith("MyBox")) {
                                            FileTools.delete(subPathsFile);
                                        }
                                    }
                                }
                                subPaths = pathFile.listFiles();
                                if (subPaths != null && subPaths.length == 0) {
                                    FileTools.deleteDir(pathFile);
                                }
                            }
                        }
                    }
                    files = log4j2Path.listFiles();
                    if (files != null && files.length == 0) {
                        FileTools.deleteDir(log4j2Path);
                    }
                }
            }
            TableStringValues.add(conn, "InstalledVersions", "6.3.6");
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private static void updateIn633(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.3.3...");
            updateGeographyCodeIn633(conn);
            updateColorDataIn633(conn);
            updateConvolutionKernelIn633(conn);
            TableStringValues.add(conn, "InstalledVersions", "6.3.3");
            conn.setAutoCommit(true);
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
        }
    }

    private static boolean updateGeographyCodeIn633(Connection conn) {
        try ( Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);
            String sql = "ALTER TABLE Geography_Code ADD COLUMN gcsource SMALLINT";
            statement.executeUpdate(sql);
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
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
//            MyBoxLog.debug(e.toString());
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
//            MyBoxLog.debug(e.toString());
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
            MyBoxLog.info("Updating tables in 6.3.2...");
            updateForeignKeysIn632(conn);
            updateGeographyCodeIn632(conn);
            updateLocationIn632(conn);
            TableStringValues.add(conn, "InstalledVersions", "6.3.2");
            conn.setAutoCommit(true);
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
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
//                    MyBoxLog.debug(sql);
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
//            MyBoxLog.debug(e.toString());
        }
    }

    private static void updateGeographyCodeIn632(Connection conn) {
        try ( Statement statement = conn.createStatement();
                 PreparedStatement update = conn.prepareStatement(TableGeographyCode.Update)) {
            conn.setAutoCommit(false);
            try ( ResultSet results = statement.executeQuery("SELECT * FROM Geography_Code WHERE gcid < 5000")) {
                while (results.next()) {
                    GeographyCode code = TableGeographyCode.readResults(results);
//                    MyBoxLog.debug(code.getGcid() + " " + code.getName() + " "
//                            + code.getLongitude() + " " + code.getLatitude() + " " + code.getCoordinateSystem().intValue());
                    code = GeographyCodeTools.toCGCS2000(code, true);
//                    MyBoxLog.debug(code.getLongitude() + " " + code.getLatitude() + " " + code.getCoordinateSystem().intValue());
                    TableGeographyCode.update(conn, update, code);
                }
            }
            conn.commit();
            conn.setAutoCommit(true);
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
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
                    data.setImageName(results.getString("image_location"));
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
//                MyBoxLog.debug(e.toString());
            }
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
        }
    }

    private static void migrateFrom63(Connection conn) {
        MyBoxLog.info("Migrate from 6.3...");
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
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
            return;
        }
        try ( Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);
            String sql = "UPDATE Geography_Code  SET area=area*1000000";
            statement.executeUpdate(sql);
            sql = "UPDATE Epidemic_Report  SET data_set='COVID-19_Tecent' WHERE data_set='COVID-19_Tencent'";
            statement.executeUpdate(sql);
            sql = "DROP VIEW Epidemic_Report_Statistic_View";
            statement.executeUpdate(sql);
            statement.executeUpdate(TableEpidemicReport.CreateStatisticView);
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
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
            TableStringValues.add(conn, "InstalledVersions", "6.3.1");
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    private static void migrateFrom621(Connection conn) {
        try {
            migrateGeographyCodeIn621(conn);
            migrateEpidemicReportFrom621(conn);
            conn.setAutoCommit(true);
            TableStringValues.add(conn, "InstalledVersions", "6.3");
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    private static void migrateGeographyCodeIn621(Connection conn) {
        MyBoxLog.info("Migrate GeographyCode from 6.2.1...");
        String sql = "SELECT * FROM Geography_Code ORDER BY level, country, province, city";
        List<GeographyCode> codes = new ArrayList<>();
        try ( Statement statement = conn.createStatement();
                 ResultSet results = statement.executeQuery(sql)) {
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
                    MyBoxLog.debug(e.toString());
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        try ( Statement statement = conn.createStatement()) {
            statement.executeUpdate("DROP TABLE Geography_Code");
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        TableGeographyCode tableGeographyCode = new TableGeographyCode();
        tableGeographyCode.createTable(conn);
        try ( Statement statement = conn.createStatement()) {
            statement.executeUpdate(TableGeographyCode.Create_Index_levelIndex);
            statement.executeUpdate(TableGeographyCode.Create_Index_gcidIndex);
            statement.executeUpdate(TableGeographyCode.Create_Index_codeIndex);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        GeographyCodeTools.importPredefined(conn);
        try ( PreparedStatement geoInsert = conn.prepareStatement(TableGeographyCode.Insert)) {
            conn.setAutoCommit(false);
            int count = 0;
            for (GeographyCode code : codes) {
                Map<String, Object> ret = GeographyCodeTools.encode(conn, geoInsert,
                        code.getLevel(), code.getLongitude(), code.getLatitude(), null,
                        code.getCountryName(), code.getProvinceName(), code.getCityName(),
                        code.getCountyName(), code.getTownName(), code.getVillageName(),
                        null, null, true, false);
                if (ret != null && ret.get("code") != null) {
                    count++;
                }
            }
            conn.commit();
            MyBoxLog.debug("Migrated GeographyCode: " + count);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    private static void migrateEpidemicReportFrom621(Connection conn) {
        MyBoxLog.info("Migrate EpidemicReport from 6.2.1...");
        String sql = "SELECT * FROM Epidemic_Report ORDER BY level, country, province, city";
        List<EpidemicReport> reports = new ArrayList<>();
        try ( Statement statement = conn.createStatement();
                 PreparedStatement geoInsert = conn.prepareStatement(TableGeographyCode.Insert);
                 ResultSet results = statement.executeQuery(sql)) {
            while (results.next()) {
                try {
                    GeographyCode code = new GeographyCode();
                    code.setCountryName(results.getString("country"));
                    code.setProvinceName(results.getString("province"));
                    code.setCityName(results.getString("city"));
                    code.setCountyName(results.getString("district"));
                    code.setTownName(results.getString("township"));
                    code.setVillageName(results.getString("neighborhood"));
                    String levelValue = results.getString("level");
                    if (levelValue != null) {
                        GeographyCodeLevel levelCode = new GeographyCodeLevel(levelValue);
                        code.setLevelCode(levelCode);
                    }
                    code.setLongitude(results.getDouble("longitude"));
                    code.setLatitude(results.getDouble("latitude"));
                    GeographyCode exist = GeographyCodeTools.encode(conn, geoInsert, code, false);
                    if (exist == null) {
                        MyBoxLog.debug(code.getLevelName() + " " + code.getCountryName()
                                + " " + code.getProvinceName() + " " + code.getCityName()
                                + " " + code.getId() + " " + code.getOwner());
                        continue;
                    }
                    EpidemicReport report = new EpidemicReport();
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
                    report.setSource("Filled".equals(results.getString("comments")) ? (short) 3 : (short) 2);
                    report.setLocation(exist);
                    report.setLocationid(exist.getId());
                    reports.add(report);
                } catch (Exception e) {
                    MyBoxLog.debug(e.toString());
                    break;
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        try ( Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);
            statement.executeUpdate("DROP TABLE Epidemic_Report");

            TableEpidemicReport tableEpidemicReport = new TableEpidemicReport();
            tableEpidemicReport.createTable(conn);
            statement.executeUpdate(TableEpidemicReport.Create_Index_DatasetTimeDesc);
            statement.executeUpdate(TableEpidemicReport.Create_Index_DatasetTimeAsc);
            statement.executeUpdate(TableEpidemicReport.Create_Index_TimeAsc);
            statement.executeUpdate(TableEpidemicReport.CreateStatisticView);

            long count = TableEpidemicReport.write(conn, reports, true);
            MyBoxLog.debug("Migrated EpidemicReport: " + count);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public static boolean migrateBefore621(Connection conn) {
        MyBoxLog.info("Migrate before 6.2.1...");
        try {
            if (!AppVariables.getSystemConfigBoolean("UpdatedTables4.2", false)) {
                MyBoxLog.info("Updating tables in 4.2...");
                List<ConvolutionKernel> records = TableConvolutionKernel.read();
                TableConvolutionKernel t = new TableConvolutionKernel();
                t.drop();
                t.init();
                if (TableConvolutionKernel.write(records)) {
                    AppVariables.setSystemConfigValue("UpdatedTables4.2", true);
                }
            }

            if (!AppVariables.getSystemConfigBoolean("UpdatedTables5.4", false)) {
                MyBoxLog.info("Updating tables in 5.4...");
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
                MyBoxLog.info("Updating tables in 5.8...");
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
                MyBoxLog.info("Updating tables in 5.9...");
                String sql = "DROP TABLE Browser_URLs";
                DerbyBase.update(sql);
                AppVariables.setSystemConfigValue("UpdatedTables5.9", true);
            }

            if (!AppVariables.getSystemConfigBoolean("UpdatedTables6.1", false)) {
                MyBoxLog.info("Updating tables in 6.1...");
                migrate61();
                AppVariables.setSystemConfigValue("UpdatedTables6.1", true);
            }

            if (!AppVariables.getSystemConfigBoolean("UpdatedTables6.1.5", false)) {
                MyBoxLog.info("Updating tables in 6.1.5...");
                migrateGeographyCode615();
                migrateEpidemicReport615();
                AppVariables.setSystemConfigValue("UpdatedTables6.1.5", true);
            }

            if (!AppVariables.getSystemConfigBoolean("UpdatedTables6.2.1", false)) {
                MyBoxLog.info("Updating tables in 6.2.1...");
                migrateGeographyCode621();
                migrateEpidemicReport621();
                AppVariables.setSystemConfigValue("UpdatedTables6.2.1", true);
            }

            TableStringValues.add(conn, "InstalledVersions", "6.2.1");
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static boolean migrateGeographyCode615() {
        MyBoxLog.info("migrate GeographyCode 6.1.5...");
        try ( Connection conn = DerbyBase.getConnection();
                 Statement statement = conn.createStatement()) {
            int size = DerbyBase.size("select count(level) from Geography_Code");
            if (size <= 0) {
                return true;
            }
            String sql = "UPDATE Geography_Code SET level='" + message("City")
                    + "' WHERE level IS NULL";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static boolean migrateGeographyCode621() {
        MyBoxLog.info("migrate GeographyCode 6.2.1...");
        try ( Connection conn = DerbyBase.getConnection();
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM Geography_Code "
                    + " WHERE country='" + message("Macao")
                    + "' OR country='" + message("Macau") + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static boolean migrateEpidemicReport615() {
        MyBoxLog.info("migrate Epidemic_Report 6.1.5...");
        try ( Connection conn = DerbyBase.getConnection();
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
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static boolean migrateEpidemicReport621() {
        MyBoxLog.info("migrate Epidemic_Report 6.2.1...");
        try ( Connection conn = DerbyBase.getConnection();
                 Statement statement = conn.createStatement()) {
            int size = DerbyBase.size("select count(confirmed) from Epidemic_Report");
            if (size <= 0) {
                return true;
            }
            String sql = "UPDATE Epidemic_Report SET level='" + message("Country")
                    + "', country='" + message("Monaco") + "', province=null, city=null, "
                    + " longitude=7.42, latitude=43.74 "
                    + " WHERE province='摩纳哥'";
            statement.executeUpdate(sql);

            sql = "DELETE FROM Epidemic_Report "
                    + " WHERE country='" + message("Macao")
                    + "' OR country='" + message("Macau") + "'";
            statement.executeUpdate(sql);

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static boolean migrate61() {
        try ( Connection conn = DerbyBase.getConnection();) {
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
//            MyBoxLog.error(e);
            return false;
        }
    }

}
