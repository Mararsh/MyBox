package mara.mybox.db.migration;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.data.ColorPaletteName;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeLevel;
import mara.mybox.db.data.GeographyCodeTools;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.db.data.ImageEditHistory;
import mara.mybox.db.data.WebHistory;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.db.table.TableConvolutionKernel;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.db.table.TableImageClipboard;
import mara.mybox.db.table.TableImageEditHistory;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.db.table.TableWebHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.PaletteTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import mara.mybox.value.SystemConfig;

/**
 * @Author Mara
 * @CreateDate 2020-06-09
 * @License Apache License Version 2.0
 */
public class DataMigrationBefore65 {

    public static void handleVersions(int lastVersion, Connection conn) {
        try {
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
            if (lastVersion < 6004003) {
                updateIn643(conn);
            }
            if (lastVersion < 6004004) {
                updateIn644(conn);
            }
            if (lastVersion < 6004005) {
                updateIn645(conn);
            }
            if (lastVersion < 6004008) {
                updateIn648(conn);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn648(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.4.8...");
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("ALTER TABLE Data_Definition  alter  column  delimiter set data type VARCHAR(128)");
                conn.commit();
                statement.executeUpdate("ALTER TABLE  Data_Column DROP CONSTRAINT  Data_Column_dataid_fk");
                conn.commit();
                statement.executeUpdate("ALTER TABLE  Data_Column ADD  CONSTRAINT  Data_Column_dataid_fk "
                        + " FOREIGN KEY ( dataid ) REFERENCES  Data_Definition ( dfid ) ON DELETE Cascade");
                conn.commit();
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn645(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.4.5...");
            String sql = "SELECT * FROM String_Values where key_name='ImageClipboard'";
            try (Statement statement = conn.createStatement();
                    ResultSet results = statement.executeQuery(sql)) {
                conn.setAutoCommit(false);
                TableImageClipboard tableImageClipboard = new TableImageClipboard();
                while (results.next()) {
                    ImageClipboard clip = new ImageClipboard();
                    clip.setImageFile(new File(results.getString("string_value")));
                    clip.setCreateTime(results.getTimestamp("create_time"));
                    clip.setSource(null);
                    tableImageClipboard.insertData(conn, clip);
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            conn.commit();
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("DELETE FROM String_Values where key_name='ImageClipboard'");
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            TableStringValues.add(conn, "InstalledVersions", "6.4.5");
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn644(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.4.4...");
            TableWebHistory tableWebHistory = new TableWebHistory();
            String sql = "SELECT * FROM Browser_History";
            try (Statement statement = conn.createStatement();
                    ResultSet results = statement.executeQuery(sql)) {
                conn.setAutoCommit(false);
                while (results.next()) {
                    WebHistory his = new WebHistory();
                    his.setAddress(results.getString("address"));
                    his.setTitle(results.getString("title"));
                    his.setIcon(results.getString("icon"));
                    his.setVisitTime(results.getTimestamp("visit_time"));
                    tableWebHistory.insertData(conn, his);
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            conn.commit();
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("DROP TABLE Browser_History");
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            TableStringValues.add(conn, "InstalledVersions", "6.4.4");
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn643(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.4.3...");
            String sql = "SELECT * FROM Color_Data";
            try (Statement statement = conn.createStatement();
                    ResultSet results = statement.executeQuery(sql)) {
                conn.setAutoCommit(false);
                ColorPaletteName defaultPalette = PaletteTools.defaultPalette(Languages.getLangName(), conn);
                long paletteid = defaultPalette.getCpnid();

                TableColorPalette tableColorPalette = new TableColorPalette();
                TableColor tableColor = new TableColor();
                while (results.next()) {
                    ColorData color = tableColor.readData(results);
                    color.setColorValue(results.getInt("color_value"));
                    tableColor.writeData(conn, color);

                    double orderNumber = results.getDouble("palette_index");
                    if (orderNumber > 0) {
                        color.setOrderNumner((float) orderNumber);
                        color.setPaletteid(paletteid);
                        tableColorPalette.findAndCreate(conn, color, true, true);
                    }
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            conn.commit();
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("DROP TABLE Color_Data");
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            TableStringValues.add(conn, "InstalledVersions", "6.4.3");
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn641(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.4.1...");
            String sql = "SELECT * FROM image_history";

            try (Statement statement = conn.createStatement();
                    ResultSet results = statement.executeQuery(sql)) {
                TableImageEditHistory tableImageEditHistory = new TableImageEditHistory();
                while (results.next()) {
                    ImageEditHistory his = new ImageEditHistory();
                    String image = results.getString("image_location");
                    if (image == null) {
                        continue;
                    }
                    his.setImageFile(new File(image));
                    String hisfile = results.getString("history_location");
                    if (hisfile == null) {
                        continue;
                    }
                    his.setHistoryFile(new File(hisfile));
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
            try (Statement statement = conn.createStatement()) {
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

    public static void updateIn638(Connection conn) {
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
                        FileTools.override(file, Languages.interfaceLanguageFile(name));
                    }
                }
            }
            TableStringValues.add(conn, "InstalledVersions", "6.3.8");
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void updateIn636(Connection conn) {
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
                                FileDeleteTools.delete(pathFile);
                            }
                        } else if (pathFile.isDirectory()) {
                            File[] subPaths = pathFile.listFiles();
                            if (subPaths != null && subPaths.length > 0) {
                                for (File subPathsFile : subPaths) {
                                    if (subPathsFile.isFile()) {
                                        if (subPathsFile.getName().startsWith("MyBox")) {
                                            FileDeleteTools.delete(subPathsFile);
                                        }
                                    }
                                }
                                subPaths = pathFile.listFiles();
                                if (subPaths != null && subPaths.length == 0) {
                                    FileDeleteTools.deleteDir(pathFile);
                                }
                            }
                        }
                    }
                    files = log4j2Path.listFiles();
                    if (files != null && files.length == 0) {
                        FileDeleteTools.deleteDir(log4j2Path);
                    }
                }
            }
            TableStringValues.add(conn, "InstalledVersions", "6.3.6");
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void updateIn633(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.3.3...");
            updateGeographyCodeIn633(conn);
            updateConvolutionKernelIn633(conn);
            TableStringValues.add(conn, "InstalledVersions", "6.3.3");
            conn.setAutoCommit(true);
        } catch (Exception e) {
//            MyBoxLog.debug(e);
        }
    }

    public static boolean updateGeographyCodeIn633(Connection conn) {
        try (Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);
            String sql = "ALTER TABLE Geography_Code ADD COLUMN gcsource SMALLINT";
            statement.executeUpdate(sql);
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return false;
        }
        try (Statement statement = conn.createStatement()) {
            String sql = "UPDATE Geography_Code SET gcsource=1 WHERE predefined=true";
            statement.executeUpdate(sql);
        } catch (Exception e) {
        }
        try (Statement statement = conn.createStatement()) {
            String sql = "UPDATE Geography_Code SET gcsource=1 WHERE predefined=1";
            statement.executeUpdate(sql);
        } catch (Exception e) {
        }
        try (Statement statement = conn.createStatement()) {
            String sql = "UPDATE Geography_Code SET gcsource=2 WHERE predefined=false";
            statement.executeUpdate(sql);
        } catch (Exception e) {
        }
        try (Statement statement = conn.createStatement()) {
            String sql = "UPDATE Geography_Code SET gcsource=2 WHERE predefined=0";
            statement.executeUpdate(sql);
        } catch (Exception e) {
        }
        try (Statement statement = conn.createStatement()) {
            String sql = "ALTER TABLE Geography_Code DROP COLUMN predefined";
            statement.executeUpdate(sql);
        } catch (Exception e) {
        }
        return true;
    }

    public static boolean updateConvolutionKernelIn633(Connection conn) {
        try (Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);
            String sql = "ALTER TABLE Convolution_Kernel ADD COLUMN is_gray BOOLEAN";
            statement.executeUpdate(sql);
            sql = "ALTER TABLE Convolution_Kernel ADD COLUMN is_invert BOOLEAN";
            statement.executeUpdate(sql);
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return false;
        }
        try (Statement statement = conn.createStatement()) {
            String sql = "UPDATE Convolution_Kernel SET is_gray=true WHERE gray>0";
            statement.executeUpdate(sql);
        } catch (Exception e) {
        }
        try (Statement statement = conn.createStatement()) {
            String sql = "UPDATE Convolution_Kernel SET is_gray=false WHERE gray<1";
            statement.executeUpdate(sql);
        } catch (Exception e) {
        }
        try (Statement statement = conn.createStatement()) {
            String sql = "ALTER TABLE Convolution_Kernel DROP COLUMN gray";
            statement.executeUpdate(sql);
        } catch (Exception e) {
        }
        return true;
    }

    public static void updateIn632(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.3.2...");
            updateForeignKeysIn632(conn);
            updateGeographyCodeIn632(conn);
            TableStringValues.add(conn, "InstalledVersions", "6.3.2");
            conn.setAutoCommit(true);
        } catch (Exception e) {
//            MyBoxLog.debug(e);
        }
    }

    public static void updateForeignKeysIn632(Connection conn) {
        try (Statement query = conn.createStatement();
                Statement update = conn.createStatement()) {
            conn.setAutoCommit(true);
            String sql = "SELECT tablename, constraintName FROM SYS.SYSTABLES t, SYS.SYSCONSTRAINTS c  where t.TABLEID=c.TABLEID AND type='F'";
            try (ResultSet results = query.executeQuery(sql)) {
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
        } catch (Exception e) {
//            MyBoxLog.debug(e);
        }
    }

    public static void updateGeographyCodeIn632(Connection conn) {
        try (Statement statement = conn.createStatement();
                PreparedStatement update = conn.prepareStatement(TableGeographyCode.Update)) {
            conn.setAutoCommit(false);
            try (ResultSet results = statement.executeQuery("SELECT * FROM Geography_Code WHERE gcid < 5000")) {
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
//            MyBoxLog.debug(e);
        }
    }

    public static void migrateFrom63(Connection conn) {
        MyBoxLog.info("Migrate from 6.3...");
        try (Statement statement = conn.createStatement()) {
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
//            MyBoxLog.debug(e);
            return;
        }
        try (Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);
            String sql = "UPDATE Geography_Code  SET area=area*1000000";
            statement.executeUpdate(sql);
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return;
        }
        try (PreparedStatement update = conn.prepareStatement(TableGeographyCode.Update)) {
            String sql = "SELECT * FROM Geography_Code";
            int count = 0;
            conn.setAutoCommit(false);
            try (ResultSet results = conn.createStatement().executeQuery(sql)) {
                while (results.next()) {
                    GeographyCode code = TableGeographyCode.readResults(results);
                    TableGeographyCode.setUpdate(conn, update, code);
                    update.addBatch();
                    if (++count % Database.BatchSize == 0) {
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
            MyBoxLog.debug(e);
        }
    }

    public static void migrateFrom621(Connection conn) {
        try {
            migrateGeographyCodeIn621(conn);
            conn.setAutoCommit(true);
            TableStringValues.add(conn, "InstalledVersions", "6.3");
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public static void migrateGeographyCodeIn621(Connection conn) {
        MyBoxLog.info("Migrate GeographyCode from 6.2.1...");
        String sql = "SELECT * FROM Geography_Code ORDER BY level, country, province, city";
        List<GeographyCode> codes = new ArrayList<>();
        try (Statement statement = conn.createStatement();
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
                    if (Languages.isChinese()) {
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
                    MyBoxLog.debug(e);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate("DROP TABLE Geography_Code");
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        TableGeographyCode tableGeographyCode = new TableGeographyCode();
        tableGeographyCode.createTable(conn);
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(TableGeographyCode.Create_Index_levelIndex);
            statement.executeUpdate(TableGeographyCode.Create_Index_gcidIndex);
            statement.executeUpdate(TableGeographyCode.Create_Index_codeIndex);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        GeographyCodeTools.importPredefined(null, conn);
        try (PreparedStatement geoInsert = conn.prepareStatement(TableGeographyCode.Insert)) {
            conn.setAutoCommit(false);
            int count = 0;
            for (GeographyCode code : codes) {
                Map<String, Object> ret = GeographyCodeTools.encode(null, conn, geoInsert,
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
            MyBoxLog.debug(e);
        }
    }

    public static boolean migrateBefore621(Connection conn) {
        MyBoxLog.info("Migrate before 6.2.1...");
        try {
            if (!SystemConfig.getBoolean("UpdatedTables4.2", false)) {
                MyBoxLog.info("Updating tables in 4.2...");
                List<ConvolutionKernel> records = TableConvolutionKernel.read();
                TableConvolutionKernel t = new TableConvolutionKernel();
                t.dropTable(conn);
                t.createTable(conn);
                if (TableConvolutionKernel.write(records)) {
                    SystemConfig.setBoolean("UpdatedTables4.2", true);
                }
            }

            if (!SystemConfig.getBoolean("UpdatedTables5.4", false)) {
                MyBoxLog.info("Updating tables in 5.4...");
                String sql = "ALTER TABLE User_Conf  alter  column  key_Name set data type VARCHAR(1024)";
                DerbyBase.update(conn, sql);
                sql = "ALTER TABLE User_Conf  alter  column  default_string_Value set data type VARCHAR(32672)";
                DerbyBase.update(conn, sql);
                sql = "ALTER TABLE System_Conf  alter  column  key_Name set data type VARCHAR(1024)";
                DerbyBase.update(conn, sql);
                sql = "ALTER TABLE System_Conf  alter  column  string_Value set data type VARCHAR(32672)";
                DerbyBase.update(conn, sql);
                sql = "ALTER TABLE System_Conf  alter  column  default_string_Value set data type VARCHAR(32672)";
                DerbyBase.update(conn, sql);
                sql = "ALTER TABLE image_history  add  column  temp VARCHAR(128)";
                DerbyBase.update(conn, sql);
                sql = "UPDATE image_history SET temp=CHAR(update_type)";
                DerbyBase.update(conn, sql);
                sql = "ALTER TABLE image_history drop column update_type";
                DerbyBase.update(conn, sql);
                sql = "RENAME COLUMN image_history.temp TO update_type";
                DerbyBase.update(conn, sql);
                sql = "ALTER TABLE image_history  add  column  object_type VARCHAR(128)";
                DerbyBase.update(conn, sql);
                sql = "ALTER TABLE image_history  add  column  op_type VARCHAR(128)";
                DerbyBase.update(conn, sql);
                sql = "ALTER TABLE image_history  add  column  scope_type  VARCHAR(128)";
                DerbyBase.update(conn, sql);
                sql = "ALTER TABLE image_history  add  column  scope_name  VARCHAR(1024)";
                DerbyBase.update(conn, sql);
                sql = "DROP TABLE image_init";
                DerbyBase.update(conn, sql);
                SystemConfig.setBoolean("UpdatedTables5.4", true);
            }

            if (!SystemConfig.getBoolean("UpdatedTables5.8", false)) {
                MyBoxLog.info("Updating tables in 5.8...");
                String sql = "ALTER TABLE SRGB  add  column  palette_index  INT";
                DerbyBase.update(conn, sql);

//                List<String> saveColors = TableStringValues.read("ColorPalette");
//                if (saveColors != null && !saveColors.isEmpty()) {
//                    TableColor.setPalette(saveColors);
//                }
                TableStringValues.clear("ColorPalette");
                SystemConfig.setBoolean("UpdatedTables5.8", true);
            }

            if (!SystemConfig.getBoolean("UpdatedTables5.9", false)) {
                MyBoxLog.info("Updating tables in 5.9...");
                String sql = "DROP TABLE Browser_URLs";
                DerbyBase.update(conn, sql);
                SystemConfig.setBoolean("UpdatedTables5.9", true);
            }

            if (!SystemConfig.getBoolean("UpdatedTables6.1.5", false)) {
                MyBoxLog.info("Updating tables in 6.1.5...");
                migrateGeographyCode615();
                SystemConfig.setBoolean("UpdatedTables6.1.5", true);
            }

            if (!SystemConfig.getBoolean("UpdatedTables6.2.1", false)) {
                MyBoxLog.info("Updating tables in 6.2.1...");
                migrateGeographyCode621();
                SystemConfig.setBoolean("UpdatedTables6.2.1", true);
            }

            TableStringValues.add(conn, "InstalledVersions", "6.2.1");
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public static boolean migrateGeographyCode615() {
        MyBoxLog.info("migrate GeographyCode 6.1.5...");
        try (Connection conn = DerbyBase.getConnection();
                Statement statement = conn.createStatement()) {
            int size = DerbyBase.size("select count(*) from Geography_Code");
            if (size <= 0) {
                return true;
            }
            String sql = "UPDATE Geography_Code SET level='" + Languages.message("City")
                    + "' WHERE level IS NULL";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return false;
        }
    }

    public static boolean migrateGeographyCode621() {
        MyBoxLog.info("migrate GeographyCode 6.2.1...");
        try (Connection conn = DerbyBase.getConnection();
                Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM Geography_Code "
                    + " WHERE country='" + Languages.message("Macao")
                    + "' OR country='" + Languages.message("Macau") + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

}
