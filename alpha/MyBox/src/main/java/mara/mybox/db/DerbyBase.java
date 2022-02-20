package mara.mybox.db;

import java.io.File;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import mara.mybox.controller.MyBoxLoadingController;
import mara.mybox.db.data.GeographyCodeTools;
import mara.mybox.db.table.TableAlarmClock;
import mara.mybox.db.table.TableBlobValue;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.db.table.TableColorPaletteName;
import mara.mybox.db.table.TableConvolutionKernel;
import mara.mybox.db.table.TableData2DCell;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.db.table.TableDataset;
import mara.mybox.db.table.TableEpidemicReport;
import mara.mybox.db.table.TableFileBackup;
import mara.mybox.db.table.TableFloatMatrix;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.db.table.TableImageClipboard;
import mara.mybox.db.table.TableImageEditHistory;
import mara.mybox.db.table.TableImageScope;
import mara.mybox.db.table.TableLocationData;
import mara.mybox.db.table.TableMedia;
import mara.mybox.db.table.TableMediaList;
import mara.mybox.db.table.TableMyBoxLog;
import mara.mybox.db.table.TableNote;
import mara.mybox.db.table.TableNoteTag;
import mara.mybox.db.table.TableNotebook;
import mara.mybox.db.table.TableQueryCondition;
import mara.mybox.db.table.TableStringValue;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.db.table.TableSystemConf;
import mara.mybox.db.table.TableTag;
import mara.mybox.db.table.TableTextClipboard;
import mara.mybox.db.table.TableTree;
import mara.mybox.db.table.TableUserConf;
import mara.mybox.db.table.TableVisitHistory;
import mara.mybox.db.table.TableWebFavorite;
import mara.mybox.db.table.TableWebHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.NetworkTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import org.apache.derby.drda.NetworkServerControl;

/**
 * @Author Mara
 * @CreateDate 2018-10-15 9:31:28
 * @License Apache License Version 2.0
 */
public class DerbyBase {

    public static String mode = "embedded";
    public static String host = "localhost";
    public static int port = 1527;
    public static String driver, protocol;
    protected static final String ClientDriver = "org.apache.derby.jdbc.ClientDriver";
    protected static final String embeddedDriver = "org.apache.derby.jdbc.EmbeddedDriver";
    protected static final String create = ";user=" + AppValues.AppDerbyUser + ";password="
            + AppValues.AppDerbyPassword + ";create=true";
    public static final String login = ";user=" + AppValues.AppDerbyUser + ";password="
            + AppValues.AppDerbyPassword + ";create=false";
    public static DerbyStatus status;
    public static long lastRetry = 0;
    public static long BatchSize = 500;

    public enum DerbyStatus {
        Embedded, Nerwork, Starting, NotConnected, EmbeddedFailed, NerworkFailed
    }

    protected String Table_Name, Create_Table_Statement;
    protected List<String> Keys;

    public boolean init() {
        try ( Connection conn = DerbyBase.getConnection();
                 Statement Statement = conn.createStatement()) {
            Statement.executeUpdate(Create_Table_Statement);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e, Create_Table_Statement);
            return false;
        }
    }

    public boolean init(Connection conn) {
        if (conn == null) {
            return false;
        }
        try ( Statement Statement = conn.createStatement()) {
            Statement.executeUpdate(Create_Table_Statement);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e, Create_Table_Statement);
            return false;
        }
    }

    public boolean drop(Connection conn) {
        if (conn == null) {
            return false;
        }
        String sql = "DROP TABLE " + Table_Name;
        try ( Statement Statement = conn.createStatement()) {
            Statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return false;
        }
    }

    public boolean drop() {
        String sql = "DROP TABLE " + Table_Name;
        try ( Connection conn = DerbyBase.getConnection();
                 Statement Statement = conn.createStatement()) {
            Statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return false;
        }
    }

    public int clear(Connection conn) {
        if (conn == null) {
            return -1;
        }
        String sql = "DELETE FROM " + Table_Name;
        try ( Statement Statement = conn.createStatement()) {
            return Statement.executeUpdate(sql);
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return -1;
        }
    }

    public int clear() {
        String sql = "DELETE FROM " + Table_Name;
        try ( Connection conn = DerbyBase.getConnection();
                 Statement Statement = conn.createStatement()) {
            return Statement.executeUpdate(sql);
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return -1;
        }
    }


    /*
        Static methods
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(protocol + dbHome() + login);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }

    }

    public static String dbHome() {
        return AppVariables.MyBoxDerbyPath.getAbsolutePath();
    }

    public static String readMode() {
        String value = ConfigTools.readValue("DerbyMode");
        String modeValue;
        if (value != null) {
            modeValue = "client".equals(value.toLowerCase()) ? "client" : "embedded";
        } else {
            modeValue = "embedded";
        }
        return modeValue;
    }

    public static String startDerby() {
        try {
            Class.forName(embeddedDriver).getDeclaredConstructors()[0].newInstance();
            Class.forName(ClientDriver).getDeclaredConstructors()[0].newInstance();
            if ("client".equals(readMode())) {
                return networkMode();
            } else {
                return embeddedMode();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return e.toString();
        }
    }

    public static String embeddedMode() {
        try {
            String lang = Locale.getDefault().getLanguage().toLowerCase();
            if (!canEmbeded()) {
                status = DerbyStatus.NotConnected;
                return MessageFormat.format(Languages.message(lang, "DerbyNotAvalibale"), AppVariables.MyBoxDerbyPath);
            }
            if (isServerStarted(port)) {
                shutdownDerbyServer();
            }
            driver = embeddedDriver;
            protocol = "jdbc:derby:";
            MyBoxLog.console("Driver: " + driver);
            mode = "embedded";
            ConfigTools.writeConfigValue("DerbyMode", mode);
            status = DerbyStatus.Embedded;
            return Languages.message(lang, "DerbyEmbeddedMode");
        } catch (Exception e) {
            status = DerbyStatus.EmbeddedFailed;
            MyBoxLog.error(e);
            return e.toString();
        }
    }

    public static void shutdownEmbeddedDerby() {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (Exception e) {
        }
    }

    // Derby will run only at localhost.
    // To avoid to let user have to configure security and network for it.
    public static String networkMode() {
        try {
            if (status == DerbyStatus.Starting) {
                String lang = Locale.getDefault().getLanguage().toLowerCase();
                return Languages.message(lang, "BeingStartingDerby");
            }
            String lang = Locale.getDefault().getLanguage().toLowerCase();
            if (startDerbyServer()) {
                driver = ClientDriver;
                protocol = "jdbc:derby://" + host + ":" + port + "/";
                mode = "client";
                status = DerbyStatus.Nerwork;
                ConfigTools.writeConfigValue("DerbyMode", mode);
                MyBoxLog.console("Driver: " + driver);
                return MessageFormat.format(Languages.message(lang, "DerbyServerListening"), port + "");

            } else if (canEmbeded() && status != DerbyStatus.EmbeddedFailed) {
                return embeddedMode();

            } else {
                status = DerbyStatus.NotConnected;
                return MessageFormat.format(Languages.message(lang, "DerbyNotAvalibale"), AppVariables.MyBoxDerbyPath);
            }
        } catch (Exception e) {
            status = DerbyStatus.NerworkFailed;
            MyBoxLog.error(e);
            return e.toString();
        }
    }

    public static boolean startDerbyServer() {
        try {
            boolean portUsed = NetworkTools.isPortUsed(port);
            int uPort = port;
            if (portUsed) {
                if (DerbyBase.isServerStarted(port)) {
                    MyBoxLog.console("Derby server is already started in port " + port + ".");
                    return true;
                } else {
                    uPort = NetworkTools.findFreePort(port);
                }
            }
            NetworkServerControl server = new NetworkServerControl(InetAddress.getByName(host),
                    uPort, AppValues.AppDerbyUser, AppValues.AppDerbyPassword);
            status = DerbyStatus.Starting;
            server.start(null);
//            server.setTraceDirectory("d:/tmp");
            server.trace(false);
            if (isServerStarted(server)) {
                port = uPort;
                MyBoxLog.console("Derby server is listening in port " + port + ".");
                status = DerbyStatus.Nerwork;
                return true;
            } else {
                status = DerbyStatus.NerworkFailed;
                return false;
            }
        } catch (Exception e) {
            status = DerbyStatus.NerworkFailed;
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean shutdownDerbyServer() {
        try {
            boolean portUsed = NetworkTools.isPortUsed(port);
            if (!portUsed) {
                return true;
            }
            NetworkServerControl server = new NetworkServerControl(InetAddress.getByName(host),
                    port, AppValues.AppDerbyUser, AppValues.AppDerbyPassword);
            server.shutdown();
            status = DerbyStatus.NotConnected;
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean isServerStarted(int uPort) {
        try ( Connection conn = DriverManager.getConnection(
                "jdbc:derby://" + host + ":" + uPort + "/" + dbHome() + login)) {
            port = uPort;
            status = DerbyStatus.Nerwork;
            return true;
        } catch (Exception e) {

            return false;
        }
    }

    public static boolean canEmbeded() {
        try ( Connection conn = DriverManager.getConnection("jdbc:derby:" + dbHome() + create)) {
            return true;
        } catch (Exception e) {
            status = DerbyStatus.EmbeddedFailed;
//            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean isServerStarted(NetworkServerControl server) {
        boolean started = false;
        int count = 10, wait = 300;
        while (!started && (count > 0)) {
            try {
                count--;
                server.ping();
                started = true;
            } catch (Exception e) {
//                MyBoxLog.error(e);
//                MyBoxLog.debug(e.toString());
                try {
                    Thread.currentThread().sleep(wait);
                } catch (Exception ex) {
                }
            }
        }
        return started;
    }

    // Upper case
    public static List<String> allTables(Connection conn) {
        List<String> tables = new ArrayList<>();
        String sql = "SELECT TABLENAME FROM SYS.SYSTABLES WHERE TABLETYPE='T'";
        try ( Statement statement = conn.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                tables.add(resultSet.getString("TABLENAME"));
            }
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
        }
        return tables;
    }

    public static List<String> columns(Connection conn, String tablename) {
        List<String> columns = new ArrayList<>();
        String sql = "SELECT columnname, columndatatype FROM SYS.SYSTABLES t, SYS.SYSCOLUMNS c "
                + " where t.TABLEID=c.REFERENCEID AND tablename='" + tablename.toUpperCase() + "'"
                + " order by columnnumber";
        try ( Statement statement = conn.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                columns.add(resultSet.getString("columnname").toLowerCase()
                        + ", " + resultSet.getString("columndatatype"));
            }
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
        }
        return columns;
    }

    // lower case
    public static List<String> columnNames(Connection conn, String tablename) {
        List<String> columns = new ArrayList<>();
        String sql = "SELECT columnname  FROM SYS.SYSTABLES t, SYS.SYSCOLUMNS c "
                + " where t.TABLEID=c.REFERENCEID AND tablename='" + tablename.toUpperCase() + "'"
                + " order by columnnumber";
        try ( Statement statement = conn.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                columns.add(resultSet.getString("columnname").toLowerCase());
            }
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
        }
        return columns;
    }

    public static List<String> indexes(Connection conn) {
        List<String> indexes = new ArrayList<>();
        String sql = "SELECT CONGLOMERATENAME FROM SYS.SYSCONGLOMERATES";
        try ( Statement statement = conn.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                indexes.add(resultSet.getString("CONGLOMERATENAME"));
            }
        } catch (Exception e) {
            MyBoxLog.console(e, sql);
        }
        return indexes;
    }

    public static List<String> views(Connection conn) {
        List<String> tables = new ArrayList<>();
        String sql = "SELECT TABLENAME FROM SYS.SYSTABLES WHERE TABLETYPE='V'";
        try ( Statement statement = conn.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                tables.add(resultSet.getString("TABLENAME"));
            }
        } catch (Exception e) {
            MyBoxLog.console(e, sql);
        }
        return tables;
    }

    public static boolean initTables(MyBoxLoadingController loadingController) {
        MyBoxLog.console("Protocol: " + protocol + dbHome());
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + create)) {
            initTables(loadingController, conn);
            initIndexs(conn);
            initViews(conn);
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return false;
        }
    }

    public static boolean initTables(MyBoxLoadingController loadingController, Connection conn) {
        try {
            List<String> tables = allTables(conn);
            MyBoxLog.console("Tables: " + tables.size());

            if (!tables.contains("MyBox_Log".toUpperCase())) {
                new TableMyBoxLog().createTable(conn);
                loadingController.info("MyBox_Log");
            }
            if (!tables.contains("System_Conf".toUpperCase())) {
                new TableSystemConf().init(conn);
                loadingController.info("System_Conf");
            }
            if (!tables.contains("User_Conf".toUpperCase())) {
                new TableUserConf().init(conn);
                loadingController.info("User_Conf");
            }
            if (!tables.contains("Data2D_Definition".toUpperCase())) {
                new TableData2DDefinition().createTable(conn);
                loadingController.info("Data2D_Definition");
            }
            if (!tables.contains("Data2D_Column".toUpperCase())) {
                new TableData2DColumn().createTable(conn);
                loadingController.info("Data2D_Column");
            }
            if (!tables.contains("String_Values".toUpperCase())) {
                new TableStringValues().init(conn);
                loadingController.info("String_Values");
            }
            if (!tables.contains("image_scope".toUpperCase())) {
                new TableImageScope().createTable(conn);
                loadingController.info("image_scope");
            }
            if (!tables.contains("Alarm_Clock".toUpperCase())) {
                new TableAlarmClock().init(conn);
                loadingController.info("Alarm_Clock");
            }
            if (!tables.contains("Convolution_Kernel".toUpperCase())) {
                new TableConvolutionKernel().init(conn);
                loadingController.info("Convolution_Kernel");
            }
            if (!tables.contains("Float_Matrix".toUpperCase())) {
                new TableFloatMatrix().init(conn);
                loadingController.info("Float_Matrix");
            }
            if (!tables.contains("visit_history".toUpperCase())) {
                new TableVisitHistory().init(conn);
                loadingController.info("visit_history");
            }
            if (!tables.contains("media_list".toUpperCase())) {
                new TableMediaList().init(conn);
                loadingController.info("media_list");
            }
            if (!tables.contains("media".toUpperCase())) {
                new TableMedia().init(conn);
                loadingController.info("media");
            }
            if (!tables.contains("Web_History".toUpperCase())) {
                new TableWebHistory().createTable(conn);
                loadingController.info("Web_History");
            }
            if (!tables.contains("Geography_Code".toUpperCase())) {
                new TableGeographyCode().createTable(conn);
                loadingController.info("Geography_Code");
            }
            if (!tables.contains("Dataset".toUpperCase())) {
                new TableDataset().createTable(conn);
                loadingController.info("Dataset");
            }
            if (!tables.contains("Location_Data".toUpperCase())) {
                new TableLocationData().createTable(conn);
                loadingController.info("Location_Data");
            }
            if (!tables.contains("Epidemic_Report".toUpperCase())) {
                new TableEpidemicReport().createTable(conn);
                loadingController.info("Epidemic_Report");
            }
            if (!tables.contains("Query_Condition".toUpperCase())) {
                new TableQueryCondition().init(conn);
                loadingController.info("Query_Condition");
            }
            if (!tables.contains("String_Value".toUpperCase())) {
                new TableStringValue().init(conn);
                loadingController.info("String_Value");
            }

            if (!tables.contains("Image_Edit_History".toUpperCase())) {
                new TableImageEditHistory().createTable(conn);
                loadingController.info("Image_Edit_History");
            }
            if (!tables.contains("File_Backup".toUpperCase())) {
                new TableFileBackup().createTable(conn);
                loadingController.info("File_Backup");
            }
            if (!tables.contains("Notebook".toUpperCase())) {
                new TableNotebook().createTable(conn);
                loadingController.info("Notebook");
            }
            if (!tables.contains("Note".toUpperCase())) {
                new TableNote().createTable(conn);
                loadingController.info("Note");
            }
            if (!tables.contains("Tag".toUpperCase())) {
                new TableTag().createTable(conn);
                loadingController.info("Tag");
            }
            if (!tables.contains("Note_Tag".toUpperCase())) {
                new TableNoteTag().createTable(conn);
                loadingController.info("Note_Tag");
            }
            if (!tables.contains("Color".toUpperCase())) {
                new TableColor().createTable(conn);
                loadingController.info("Color");
            }
            if (!tables.contains("Color_Palette_Name".toUpperCase())) {
                new TableColorPaletteName().createTable(conn);
                loadingController.info("Color_Palette_Name");
            }
            if (!tables.contains("Color_Palette".toUpperCase())) {
                new TableColorPalette().createTable(conn);
                loadingController.info("Color_Palette");
            }
            if (!tables.contains("Tree".toUpperCase())) {
                new TableTree().createTable(conn);
                loadingController.info("Tree");
            }
            if (!tables.contains("Web_Favorite".toUpperCase())) {
                new TableWebFavorite().createTable(conn);
                loadingController.info("Web_Favorite");
            }
            if (!tables.contains("Image_Clipboard".toUpperCase())) {
                new TableImageClipboard().createTable(conn);
                loadingController.info("Image_Clipboard");
            }
            if (!tables.contains("Text_Clipboard".toUpperCase())) {
                new TableTextClipboard().createTable(conn);
                loadingController.info("Text_Clipboard");
            }
            if (!tables.contains("Data2D_Cell".toUpperCase())) {
                new TableData2DCell().createTable(conn);
                loadingController.info("Data2D_Cell");
            }
            if (!tables.contains("Blob_Value".toUpperCase())) {
                new TableBlobValue().createTable(conn);
                loadingController.info("Blob_Value");
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return false;
        }
    }

    public static boolean initIndexs(Connection conn) {
        try {
            List<String> indexes = indexes(conn);
            MyBoxLog.debug("Indexes: " + indexes.size());
            if (!indexes.contains("Geography_Code_level_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableGeographyCode.Create_Index_levelIndex);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("Geography_Code_code_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableGeographyCode.Create_Index_codeIndex);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("Geography_Code_gcid_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableGeographyCode.Create_Index_gcidIndex);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("Epidemic_Report_DatasetTimeDesc_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableEpidemicReport.Create_Index_DatasetTimeDesc);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("Epidemic_Report_DatasetTimeAsc_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableEpidemicReport.Create_Index_DatasetTimeAsc);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("Epidemic_Report_timeAsc_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableEpidemicReport.Create_Index_TimeAsc);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("Dataset_unique_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableDataset.Create_Index_unique);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("MyBox_Log_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableMyBoxLog.Create_Index);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("File_Backup_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableFileBackup.Create_Index);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("Note_time_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableNote.Create_Time_Index);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("Notebook_owner_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableNotebook.Create_Owner_Index);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("Tag_unique_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableTag.Create_Unique_Index);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("Note_Tag_unique_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableNoteTag.Create_Unique_Index);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("Color_rgba_unique_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableColor.Create_RGBA_Unique_Index);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("Color_Palette_Name_unique_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableColorPaletteName.Create_Unique_Index);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("Color_Palette_unique_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableColorPalette.Create_Unique_Index);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("Tree_parent_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableTree.Create_Parent_Index);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("Tree_title_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableTree.Create_Title_Index);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("Web_Favorite_owner_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableWebFavorite.Create_Owner_Index);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("Web_History_time_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableWebHistory.Create_Time_Index);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return false;
        }
    }

    public static boolean initViews(Connection conn) {
        try {
            List<String> views = views(conn);
            MyBoxLog.debug("Views: " + views.size());
            if (!views.contains("Epidemic_Report_Statistic_View".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableEpidemicReport.CreateStatisticView);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
                }
            }
            if (!views.contains("Location_Data_View".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableLocationData.CreateView);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
                }
            }
            if (!views.contains("Data2D_Column_View".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableData2DColumn.CreateView);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
                }
            }
            if (!views.contains("Color_Palette_View".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableColorPalette.CreateView);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return false;
        }
    }

    public static boolean initTableValues() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + create)) {
            if (TableGeographyCode.China(conn) == null) {
                GeographyCodeTools.importPredefined(conn);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return false;
        }
    }

    public static int size(String sql) {
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return size(conn, sql);
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return 0;
        }
    }

    public static int size(Connection conn, String sql) {
        int size = 0;
        try ( ResultSet results = conn.createStatement().executeQuery(sql)) {
            if (results.next()) {
                size = results.getInt(1);
            }
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
        }
        return size;

    }

    public static ResultSet query(String sql) {
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return query(conn, sql);
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return null;
        }
    }

    public static ResultSet query(Connection conn, String sql) {
        try ( Statement statement = conn.createStatement()) {
            return statement.executeQuery(sql);
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return null;
        }
    }

    public static int update(String sql) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return update(conn, sql);
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return 0;
        }
    }

    public static int update(Connection conn, String sql) {
        try ( Statement statement = conn.createStatement()) {
            return statement.executeUpdate(sql);
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return 0;
        }
    }

    public static String stringValue(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("'", "''");
    }

//    CALL SYSCS_UTIL.SYSCS_EXPORT_TABLE ('MARA', 'LOCATION', 'D:\MyBox\src\main\resources\fetch\db\Location.del_en', null, null,  'UTF-8');
    public static void exportData(String table, String file) {
        if (file == null) {
            return;
        }
        File f = new File(file);
        FileDeleteTools.delete(f);
        try ( Connection conn = DerbyBase.getConnection();) {
            PreparedStatement ps = conn.prepareStatement("CALL SYSCS_UTIL.SYSCS_EXPORT_TABLE (?,?,?,?,?,?)");
            ps.setString(1, null);
            ps.setString(2, table.toUpperCase());
            ps.setString(3, file);
            ps.setString(4, null);
            ps.setString(5, null);
            ps.setString(6, "UTF-8");
            ps.execute();
        } catch (Exception e) {
            MyBoxLog.error(e, file);

        }
    }

//    CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE ('MARA', 'LOCATION', 'D:\MyBox\src\main\resources\fetch\db\Location.del',null, null,  'UTF-8', 1);
    public static void importData(String table, String file, boolean replace) {
        if (file == null) {
            return;
        }
        File f = new File(file);
        if (!f.exists()) {
            return;
        }
        try ( Connection conn = DerbyBase.getConnection();) {
            PreparedStatement ps = conn.prepareStatement("CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE (?,?,?,?,?,?,?)");
            ps.setString(1, null);
            ps.setString(2, table.toUpperCase());
            ps.setString(3, file);
            ps.setString(4, null);
            ps.setString(5, null);
            ps.setString(6, "UTF-8");
            ps.setInt(7, replace ? 1 : 0);
            ps.execute();
        } catch (Exception e) {
            MyBoxLog.error(e, file);

        }
    }

    // https://db.apache.org/derby/docs/10.15/ref/crefsqlj1003454.html#crefsqlj1003454
    // https://db.apache.org/derby/docs/10.15/ref/rrefkeywords29722.html
    public static String fixedIdentifier(String name) {
        if (name == null) {
            return null;
        }
        if (name.startsWith("\"") && name.endsWith("\"")) {
            return name;
        }
        String s = "";
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if ((c > 64 && c < 91) || (c > 96 && c < 123) || c < 0 || c > 127) {
                s += c;
                continue;
            }
            if (i == 0) {
                s += "a";
            }
            if (c == '_' || (c > 47 && c < 58)) {
                s += c;
            } else {
                s += "_";
            }
        }
        return s;
    }


    /*
        get/set
     */
    public String getTable_Name() {
        return Table_Name;
    }

    public void setTable_Name(String Table_Name) {
        this.Table_Name = Table_Name;
    }

    public String getCreate_Table_Statement() {
        return Create_Table_Statement;
    }

    public void setCreate_Table_Statement(String Create_Table_Statement) {
        this.Create_Table_Statement = Create_Table_Statement;
    }

    public List<String> getKeys() {
        return Keys;
    }

    public void setKeys(List<String> Keys) {
        this.Keys = Keys;
    }

}
