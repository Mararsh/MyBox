package mara.mybox.db;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import mara.mybox.data.CoordinateSystem;
import static mara.mybox.db.DerbyBase.BatchSize;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.data.ColorPaletteName;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.db.data.Data2DCell;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Dataset;
import mara.mybox.db.data.EpidemicReport;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeLevel;
import mara.mybox.db.data.GeographyCodeTools;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.db.data.ImageEditHistory;
import mara.mybox.db.data.Location;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.data.WebHistory;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.db.table.TableColorPaletteName;
import mara.mybox.db.table.TableConvolutionKernel;
import mara.mybox.db.table.TableData2DCell;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.db.table.TableEpidemicReport;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.db.table.TableImageClipboard;
import mara.mybox.db.table.TableImageEditHistory;
import mara.mybox.db.table.TableLocationData;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.db.table.TableTreeNode;
import mara.mybox.db.table.TableWebHistory;
import mara.mybox.dev.DevTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.SystemConfig;

/**
 * @Author Mara
 * @CreateDate 2020-06-09
 * @License Apache License Version 2.0
 */
public class DataMigration {

    public static boolean checkUpdates() {
        SystemConfig.setString("CurrentVersion", AppValues.AppVersion);
        try ( Connection conn = DerbyBase.getConnection()) {
            int lastVersion = DevTools.lastVersion(conn);
            int currentVersion = DevTools.myboxVersion(AppValues.AppVersion);
            if (lastVersion == currentVersion) {
                return true;
            }
            MyBoxLog.info("Last version: " + lastVersion + " " + "Current version: " + currentVersion);
            if (lastVersion > 0) {
                reloadInternalDoc();
                reloadInternalData();

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
                if (lastVersion < 6004007) {
                    Platform.runLater(() -> {
                        PopTools.alertWarning(null, message("MigrationNotice"));
                    });
                }
                if (lastVersion < 6004008) {
                    updateIn648(conn);
                }
                if (lastVersion < 6005001) {
                    updateIn651(conn);
                }
                if (lastVersion < 6005002) {
                    updateIn652(conn);
                }
                if (lastVersion < 6005003) {
                    updateIn653(conn);
                }
                if (lastVersion < 6005004) {
                    updateIn654(conn);
                }
            }
            TableStringValues.add(conn, "InstalledVersions", AppValues.AppVersion);
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return true;
    }

    private static void updateIn654(Connection conn) {
        try ( Statement statement = conn.createStatement()) {
            MyBoxLog.info("Updating tables in 6.5.4...");

            conn.setAutoCommit(true);
            statement.executeUpdate("ALTER TABLE Tag ADD COLUMN category VARCHAR(" + StringMaxLength + ") NOT NULL DEFAULT 'Root'");
            statement.executeUpdate("ALTER TABLE Tag ADD COLUMN color VARCHAR(" + StringMaxLength + ")");
            statement.executeUpdate("DROP INDEX Tag_unique_index");
            statement.executeUpdate("CREATE UNIQUE INDEX Tag_unique_index on Tag (  category, tag )");
            statement.executeUpdate("UPDATE Tag SET category='" + TreeNode.Notebook + "'");

            TableTreeNode tableTreeNode = new TableTreeNode();
            tableTreeNode.checkBase(conn);
            statement.executeUpdate("ALTER TABLE tree_node ADD COLUMN oldNodeid BIGINT");
            statement.executeUpdate("ALTER TABLE tree_node ADD COLUMN oldParentid BIGINT");
            statement.executeUpdate("INSERT INTO tree_node (category, title, value, oldNodeid, oldParentid) "
                    + "SELECT '" + TreeNode.WebFavorite + "', title, attribute, nodeid, parent FROM tree WHERE nodeid > 1");
            statement.executeUpdate("INSERT INTO tree_node ( category, title, value, more, oldParentid) "
                    + "SELECT '" + TreeNode.WebFavorite + "', title, address, icon, owner FROM Web_Favorite");
            statement.executeUpdate("Update tree_node AS A set parentid="
                    + "(select B.nodeid from tree_node AS B WHERE A.oldParentid=B.oldNodeid AND B.category='" + TreeNode.WebFavorite + "')  "
                    + "WHERE A.category='" + TreeNode.WebFavorite + "'");

            statement.executeUpdate("INSERT INTO tree_node (category, title, value, oldNodeid, oldParentid)  "
                    + "SELECT '" + TreeNode.Notebook + "', name , description, nbid, owner FROM notebook");
            statement.executeUpdate("Update tree_node AS A set parentid="
                    + "(select B.nodeid from tree_node AS B WHERE A.oldParentid=B.oldNodeid AND B.category='" + TreeNode.Notebook + "' AND A.category='" + TreeNode.Notebook + "') "
                    + " WHERE A.category='" + TreeNode.Notebook + "'");
            statement.executeUpdate("INSERT INTO tree_node (category, title, value, update_time, oldNodeid, oldParentid) "
                    + "SELECT 'Note', title, html, update_time, ntid, notebook FROM note");
            statement.executeUpdate("Update tree_node AS A set parentid="
                    + "(select B.nodeid from tree_node AS B WHERE A.oldParentid=B.oldNodeid AND B.category='Notebook' AND A.category='Note')  "
                    + "WHERE A.category='Note'");
            statement.executeUpdate("INSERT INTO tree_node_tag (tnodeid, tagid)  "
                    + "SELECT tree_node.nodeid, note_tag.tagid FROM tree_node, note_tag where tree_node.oldNodeid=note_tag.noteid AND tree_node.category='Note'");
            statement.executeUpdate("Update tree_node set category='" + TreeNode.Notebook + "' WHERE category='Note'");

            statement.executeUpdate("ALTER TABLE tree_node DROP COLUMN oldNodeid");
            statement.executeUpdate("ALTER TABLE tree_node DROP COLUMN oldParentid");
            statement.executeUpdate("DROP TABLE Web_Favorite");
            statement.executeUpdate("DROP TABLE Note_tag");
            statement.executeUpdate("DROP TABLE Note");
            statement.executeUpdate("DROP TABLE Notebook");
            statement.executeUpdate("DROP TABLE tree");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private static void updateIn653(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.5.3...");

            try ( Statement statement = conn.createStatement()) {
                statement.executeUpdate("ALTER TABLE Data2D_Column DROP COLUMN is_id");
                statement.executeUpdate("ALTER TABLE Data2D_Column ADD COLUMN is_auto BOOLEAN");
                conn.commit();
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private static void updateIn652(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.5.2...");

            try ( Statement statement = conn.createStatement()) {
                statement.executeUpdate("ALTER TABLE User_Conf DROP COLUMN default_int_Value");
                statement.executeUpdate("ALTER TABLE User_Conf DROP COLUMN default_string_Value");
                statement.executeUpdate("ALTER TABLE System_Conf DROP COLUMN default_int_Value");
                statement.executeUpdate("ALTER TABLE System_Conf DROP COLUMN default_string_Value");
                statement.executeUpdate("ALTER TABLE Data2D_Column ADD COLUMN color  VARCHAR(16)");
                conn.commit();
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private static void updateIn651(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.5.1...");

            updateIn651ExtendColumns(conn);

            updateIn651ReplaceDataDefinition(conn);

            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private static void updateIn651ExtendColumns(Connection conn) {
        try {
            conn.setAutoCommit(true);

            alterColumnLength(conn, "User_Conf", "key_Name", StringMaxLength);
            alterColumnLength(conn, "System_Conf", "key_Name", StringMaxLength);
            alterColumnLength(conn, "String_Values", "key_name", StringMaxLength);
            alterColumnLength(conn, "String_Value", "key_name", StringMaxLength);
            alterColumnLength(conn, "Query_Condition", "data_name", StringMaxLength);
            alterColumnLength(conn, "visit_history", "resource_value", StringMaxLength);
            alterColumnLength(conn, "visit_history", "data_more", StringMaxLength);
            alterColumnLength(conn, "Web_History", "address", StringMaxLength);
            alterColumnLength(conn, "Web_History", "title", StringMaxLength);
            alterColumnLength(conn, "Web_History", "icon", StringMaxLength);
            alterColumnLength(conn, "Web_Favorite", "title", StringMaxLength);
            alterColumnLength(conn, "Web_Favorite", "address", StringMaxLength);
            alterColumnLength(conn, "Web_Favorite", "icon", StringMaxLength);
            alterColumnLength(conn, "Tree", "title", StringMaxLength);
            alterColumnLength(conn, "Tree", "attribute", StringMaxLength);
            alterColumnLength(conn, "Tag", "tag", StringMaxLength);
            alterColumnLength(conn, "Notebook", "name", StringMaxLength);
            alterColumnLength(conn, "Notebook", "description", StringMaxLength);
            alterColumnLength(conn, "Note", "title", StringMaxLength);
            alterColumnLength(conn, "MyBox_Log", "log", StringMaxLength);
            alterColumnLength(conn, "MyBox_Log", "file_name", StringMaxLength);
            alterColumnLength(conn, "MyBox_Log", "class_name", StringMaxLength);
            alterColumnLength(conn, "MyBox_Log", "method_name", StringMaxLength);
            alterColumnLength(conn, "MyBox_Log", "callers", StringMaxLength);
            alterColumnLength(conn, "MyBox_Log", "comments", StringMaxLength);
            alterColumnLength(conn, "Location_Data", "label", StringMaxLength);
            alterColumnLength(conn, "Location_Data", "address", StringMaxLength);
            alterColumnLength(conn, "image_scope", "outline", StringMaxLength);
            alterColumnLength(conn, "Image_Edit_History", "scope_name", StringMaxLength);
            alterColumnLength(conn, "Geography_Code", "chinese_name", StringMaxLength);
            alterColumnLength(conn, "Geography_Code", "english_name", StringMaxLength);
            alterColumnLength(conn, "Geography_Code", "code1", 1024);
            alterColumnLength(conn, "Geography_Code", "code2", 1024);
            alterColumnLength(conn, "Geography_Code", "code3", 1024);
            alterColumnLength(conn, "Geography_Code", "code4", 1024);
            alterColumnLength(conn, "Geography_Code", "code5", 1024);
            alterColumnLength(conn, "Geography_Code", "alias1", StringMaxLength);
            alterColumnLength(conn, "Geography_Code", "alias2", StringMaxLength);
            alterColumnLength(conn, "Geography_Code", "alias3", StringMaxLength);
            alterColumnLength(conn, "Geography_Code", "alias4", StringMaxLength);
            alterColumnLength(conn, "Geography_Code", "alias5", StringMaxLength);
            alterColumnLength(conn, "Float_Matrix", "name", StringMaxLength);
            alterColumnLength(conn, "Epidemic_Report", "data_set", StringMaxLength);
            alterColumnLength(conn, "Dataset", "data_category", StringMaxLength);
            alterColumnLength(conn, "Dataset", "data_set", StringMaxLength);
            alterColumnLength(conn, "Convolution_Kernel", "name", StringMaxLength);
            alterColumnLength(conn, "Convolution_Kernel", "description", StringMaxLength);
            alterColumnLength(conn, "Color_Palette_Name", "palette_name", StringMaxLength);
            alterColumnLength(conn, "Color_Palette", "name_in_palette", StringMaxLength);
            alterColumnLength(conn, "Color", "color_name", StringMaxLength);
            alterColumnLength(conn, "Alarm_Clock", "description", StringMaxLength);
            alterColumnLength(conn, "Alarm_Clock", "sound", StringMaxLength);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private static void updateIn651ReplaceDataDefinition(Connection conn) {
        try {
            conn.setAutoCommit(false);
            TableData2DDefinition tableData2DDefinition = new TableData2DDefinition();
            TableData2DColumn tableData2DColumn = new TableData2DColumn();
            try ( ResultSet dquery = conn.createStatement().executeQuery("SELECT * FROM Data_Definition");) {
                while (dquery.next()) {
                    long dfid = dquery.getLong("dfid");
                    short type = dquery.getShort("data_type");
                    String name = dquery.getString("data_name");
                    if (name == null || name.isBlank()) {
                        continue;
                    }
                    File file = null;
                    if (type == 2) {               // Matrix
                        type = 4;
                    } else if (type == 1) {       // DataFile
                        file = new File(name);
                        if (file.exists()) {
                            if (name.endsWith(".csv")) {
                                type = 1;
                            } else {
                                type = 0;    // texts
                            }
                            name = file.getName();
                        } else {
                            continue;
                        }
                    } else if (type == 4) {          // DataClipboard
                        file = new File(name);
                        if (!file.exists()) {
                            continue;
                        }
                        name = file.getName();
                        type = 3;
                    } else {
                        continue;
                    }
                    String charset = dquery.getString("charset");
                    String delimiter = dquery.getString("delimiter");
                    boolean has_header = dquery.getBoolean("has_header");
                    Data2DDefinition def = Data2DDefinition.create()
                            .setType(Data2DDefinition.type(type))
                            .setFile(file).setDataName(name)
                            .setHasHeader(has_header).setDelimiter(delimiter)
                            .setCharsetName(charset);
                    def = tableData2DDefinition.insertData(conn, def);
                    conn.commit();

                    long d2did = def.getD2did();
                    ResultSet cquery = conn.createStatement().executeQuery("SELECT * FROM Data_Column WHERE dataid=" + dfid);
                    while (cquery.next()) {
                        Data2DColumn column = Data2DColumn.create().setD2id(d2did);
                        column.setType(ColumnDefinition.columnType(cquery.getShort("column_type")));
                        column.setColumnName(cquery.getString("column_name"));
                        column.setIndex(cquery.getInt("index"));
                        column.setLength(cquery.getInt("length"));
                        column.setWidth(cquery.getInt("width"));
                        tableData2DColumn.insertData(conn, column);
                    }
                    conn.commit();
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }

            try ( ResultSet mquery = conn.createStatement().executeQuery("SELECT * FROM Matrix")) {
                conn.setAutoCommit(false);
                TableData2DCell tableData2DCell = new TableData2DCell();
                while (mquery.next()) {
                    long mxid = mquery.getLong("mxid");
                    Data2DDefinition def = Data2DDefinition.create()
                            .setType(Data2DDefinition.Type.Matrix)
                            .setDataName(mquery.getString("name"))
                            .setScale(mquery.getShort("scale"))
                            .setColsNumber(mquery.getInt("columns_number"))
                            .setRowsNumber(mquery.getInt("rows_number"))
                            .setModifyTime(mquery.getTimestamp("modify_time"))
                            .setComments(mquery.getString("comments"));
                    def = tableData2DDefinition.insertData(conn, def);
                    conn.commit();
                    long d2did = def.getD2did();
                    try ( ResultSet cquery = conn.createStatement()
                            .executeQuery("SELECT * FROM Matrix_Cell WHERE mcxid=" + mxid)) {
                        while (cquery.next()) {
                            Data2DCell cell = Data2DCell.create().setD2did(d2did)
                                    .setCol(cquery.getInt("col"))
                                    .setRow(cquery.getInt("row"))
                                    .setValue(cquery.getDouble("value") + "");
                            tableData2DCell.insertData(conn, cell);
                        }
                    } catch (Exception e) {
                        MyBoxLog.debug(e);
                    }
                    conn.commit();
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            conn.commit();

            try ( Statement statement = conn.createStatement()) {
                statement.executeUpdate("DROP TABLE Matrix_Cell");
                statement.executeUpdate("DROP TABLE Matrix");
                statement.executeUpdate("DROP VIEW Data_Column_View");
                statement.executeUpdate("DROP TABLE Data_Column");
                statement.executeUpdate("DROP TABLE Data_Definition");
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private static void updateIn648(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.4.8...");
            try ( Statement statement = conn.createStatement()) {
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

    private static void updateIn645(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.4.5...");
            String sql = "SELECT * FROM String_Values where key_name='ImageClipboard'";
            try ( Statement statement = conn.createStatement();
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
            try ( Statement statement = conn.createStatement()) {
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

    private static void updateIn644(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.4.4...");
            TableWebHistory tableWebHistory = new TableWebHistory();
            String sql = "SELECT * FROM Browser_History";
            try ( Statement statement = conn.createStatement();
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
            try ( Statement statement = conn.createStatement()) {
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

    private static void updateIn643(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.4.3...");
            String sql = "SELECT * FROM Color_Data";
            try ( Statement statement = conn.createStatement();
                     ResultSet results = statement.executeQuery(sql)) {
                conn.setAutoCommit(false);
                TableColorPaletteName tableColorPaletteName = new TableColorPaletteName();
                ColorPaletteName defaultPalette = tableColorPaletteName.defaultPalette(conn);
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
                        tableColorPalette.findAndCreate(conn, paletteid, color, true);
                    }
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            conn.commit();
            try ( Statement statement = conn.createStatement()) {
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

    private static void updateIn641(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.4.1...");
            String sql = "SELECT * FROM image_history";

            try ( Statement statement = conn.createStatement();
                     ResultSet results = statement.executeQuery(sql)) {
                TableImageEditHistory tableImageEditHistory = new TableImageEditHistory();
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
                        FileTools.rename(file, Languages.interfaceLanguageFile(name));
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

    private static void updateIn633(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.3.3...");
            updateGeographyCodeIn633(conn);
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
                                + " " + code.getGcid() + " " + code.getOwner());
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
                    report.setLocationid(exist.getGcid());
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

    private static boolean migrateBefore621(Connection conn) {
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
                migrateEpidemicReport615();
                SystemConfig.setBoolean("UpdatedTables6.1.5", true);
            }

            if (!SystemConfig.getBoolean("UpdatedTables6.2.1", false)) {
                MyBoxLog.info("Updating tables in 6.2.1...");
                migrateGeographyCode621();
                migrateEpidemicReport621();
                SystemConfig.setBoolean("UpdatedTables6.2.1", true);
            }

            TableStringValues.add(conn, "InstalledVersions", "6.2.1");
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    private static boolean migrateGeographyCode615() {
        MyBoxLog.info("migrate GeographyCode 6.1.5...");
        try ( Connection conn = DerbyBase.getConnection();
                 Statement statement = conn.createStatement()) {
            int size = DerbyBase.size("select count(level) from Geography_Code");
            if (size <= 0) {
                return true;
            }
            String sql = "UPDATE Geography_Code SET level='" + Languages.message("City")
                    + "' WHERE level IS NULL";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    private static boolean migrateGeographyCode621() {
        MyBoxLog.info("migrate GeographyCode 6.2.1...");
        try ( Connection conn = DerbyBase.getConnection();
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM Geography_Code "
                    + " WHERE country='" + Languages.message("Macao")
                    + "' OR country='" + Languages.message("Macau") + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    private static boolean migrateEpidemicReport615() {
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

            sql = "UPDATE Epidemic_Report SET level='" + Languages.message("Global")
                    + "' WHERE country IS NULL AND province IS NULL ";
            statement.executeUpdate(sql);

            sql = "UPDATE Epidemic_Report SET level='" + Languages.message("Country")
                    + "' WHERE country IS NOT NULL AND province IS NULL";
            statement.executeUpdate(sql);

            sql = "UPDATE Epidemic_Report SET level='" + Languages.message("Province")
                    + "' WHERE province IS NOT NULL";
            statement.executeUpdate(sql);

            sql = "UPDATE Epidemic_Report SET level='" + Languages.message("City")
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

    private static boolean migrateEpidemicReport621() {
        MyBoxLog.info("migrate Epidemic_Report 6.2.1...");
        try ( Connection conn = DerbyBase.getConnection();
                 Statement statement = conn.createStatement()) {
            int size = DerbyBase.size("select count(confirmed) from Epidemic_Report");
            if (size <= 0) {
                return true;
            }
            String sql = "UPDATE Epidemic_Report SET level='" + Languages.message("Country")
                    + "', country='" + Languages.message("Monaco") + "', province=null, city=null, "
                    + " longitude=7.42, latitude=43.74 "
                    + " WHERE province='摩纳哥'";
            statement.executeUpdate(sql);

            sql = "DELETE FROM Epidemic_Report "
                    + " WHERE country='" + Languages.message("Macao")
                    + "' OR country='" + Languages.message("Macau") + "'";
            statement.executeUpdate(sql);

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    /*
        common
     */
    private static void reloadInternalDoc() {
        new Thread() {
            @Override
            public void run() {
                try {
                    MyBoxLog.info("Reloading internal doc...");
                    FxFileTools.getInternalFile("/doc/en/README.md", "doc", "README-en.md", true);
                    FxFileTools.getInternalFile("/doc/zh/README.md", "doc", "README-zh.md", true);
                    MyBoxLog.info("Internal doc loaded.");
                } catch (Exception e) {
                }
            }
        }.start();
    }

    private static void reloadInternalData() {
        new Thread() {
            @Override
            public void run() {
                try {
                    MyBoxLog.info("Reloading internal data...");
                    List<String> names = Arrays.asList(
                            "Notes_Examples_en.txt", "Notes_Examples_zh.txt",
                            "Sql_Examples_en.txt", "Sql_Examples_zh.txt",
                            "Tree_Examples_en.txt", "Tree_Examples_zh.txt",
                            "JavaScript_Examples_en.txt", "JavaScript_Examples_zh.txt",
                            "JShell_Examples_en.txt", "JShell_Examples_zh.txt",
                            "WebFavorites_Examples_en.txt", "WebFavorites_Examples_zh.txt"
                    );
                    for (String name : names) {
                        FxFileTools.getInternalFile("/data/db/" + name, "data", name, true);
                    }
                    MyBoxLog.info("Internal data loaded.");
                } catch (Exception e) {
                }
            }
        }.start();
    }

    private static void alterColumnLength(Connection conn, String tableName, String colName, int length) {
        String sql = "ALTER TABLE " + tableName + "  alter  column  " + colName + " set data type VARCHAR(" + length + ")";
        DerbyBase.update(conn, sql);
    }

}
