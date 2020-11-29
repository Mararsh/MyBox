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
import mara.mybox.data.tools.GeographyCodeTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.NetworkTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
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
    protected static final String create = ";user=" + CommonValues.AppDerbyUser + ";password="
            + CommonValues.AppDerbyPassword + ";create=true";
    public static final String login = ";user=" + CommonValues.AppDerbyUser + ";password="
            + CommonValues.AppDerbyPassword + ";create=false";
    public static DerbyStatus status;
    public static long lastRetry = 0;
    public static long BatchSize = 500;

    public enum DerbyStatus {
        Embedded, Nerwork, Starting, NotConnected, EmbeddedFailed, NerworkFailed
    }

    protected String Table_Name, Create_Table_Statement;
    protected List<String> Keys;

    public boolean init() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement Statement = conn.createStatement()) {
            Statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return false;
        }
    }

    public boolean clear(Connection conn) {
        if (conn == null) {
            return false;
        }
        String sql = "DELETE FROM " + Table_Name;
        try ( Statement Statement = conn.createStatement()) {
            Statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return false;
        }
    }

    public boolean clear() {
        String sql = "DELETE FROM " + Table_Name;
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement Statement = conn.createStatement()) {
            Statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return false;
        }
    }


    /*
        Static methods
     */
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
                return MessageFormat.format(
                        message(lang, "DerbyNotAvalibale"), AppVariables.MyBoxDerbyPath);
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
            return message(lang, "DerbyEmbeddedMode");
        } catch (Exception e) {
            status = DerbyStatus.EmbeddedFailed;
            MyBoxLog.error(e);
            return e.toString();
        }
    }

    // Derby will run only at localhost.
    // To avoid to let user have to configure security and network for it.
    public static String networkMode() {
        try {
            if (status == DerbyStatus.Starting) {
                String lang = Locale.getDefault().getLanguage().toLowerCase();
                return message(lang, "BeingStartingDerby");
            }
            String lang = Locale.getDefault().getLanguage().toLowerCase();
            if (startDerbyServer()) {
                driver = ClientDriver;
                protocol = "jdbc:derby://" + host + ":" + port + "/";
                mode = "client";
                status = DerbyStatus.Nerwork;
                ConfigTools.writeConfigValue("DerbyMode", mode);
                MyBoxLog.console("Driver: " + driver);
                return MessageFormat.format(message(lang, "DerbyServerListening"), port + "");

            } else if (canEmbeded() && status != DerbyStatus.EmbeddedFailed) {
                return embeddedMode();

            } else {
                status = DerbyStatus.NotConnected;
                return MessageFormat.format(
                        message(lang, "DerbyNotAvalibale"), AppVariables.MyBoxDerbyPath);
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
                    uPort, CommonValues.AppDerbyUser, CommonValues.AppDerbyPassword);
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
                    port, CommonValues.AppDerbyUser, CommonValues.AppDerbyPassword);
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
    public static List<String> tables(Connection conn) {
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

    public static String tableDefinition(Connection conn, String tablename) {
        String s = "";
        for (String column : columns(conn, tablename)) {
            s += column + "\n";
        }
        return s;
    }

    public static String tableDefinition(String tablename) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            String s = "";
            for (String column : columns(conn, tablename)) {
                s += column + "\n";
            }
            return s;
        } catch (Exception e) {
            MyBoxLog.console(e, tablename);
            return null;
        }
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

    public static boolean initTables() {
        MyBoxLog.console("Protocol: " + protocol + dbHome());
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + create)) {
            initTables(conn);
            initIndexs(conn);
            initViews(conn);
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return false;
        }
    }

    public static boolean initTables(Connection conn) {
        try {
            List<String> tables = tables(conn);
            MyBoxLog.console("Tables: " + tables.size());

            if (!tables.contains("String_Values".toUpperCase())) {
                new TableStringValues().init(conn);
            }
            if (!tables.contains("image_scope".toUpperCase())) {
                new TableImageScope().init(conn);
            }
            if (!tables.contains("System_Conf".toUpperCase())) {
                new TableSystemConf().init(conn);
            }
            if (!tables.contains("User_Conf".toUpperCase())) {
                new TableUserConf().init(conn);
            }
            if (!tables.contains("Alarm_Clock".toUpperCase())) {
                new TableAlarmClock().init(conn);
            }
            if (!tables.contains("image_history".toUpperCase())) {
                new TableImageHistory().init(conn);
            }
            if (!tables.contains("Convolution_Kernel".toUpperCase())) {
                new TableConvolutionKernel().init(conn);
            }
            if (!tables.contains("Float_Matrix".toUpperCase())) {
                new TableFloatMatrix().init(conn);
            }
            if (!tables.contains("visit_history".toUpperCase())) {
                new TableVisitHistory().init(conn);
            }
            if (!tables.contains("media_list".toUpperCase())) {
                new TableMediaList().init(conn);
            }
            if (!tables.contains("media".toUpperCase())) {
                new TableMedia().init(conn);
            }
            if (!tables.contains("Browser_History".toUpperCase())) {
                new TableBrowserHistory().init(conn);
            }
            if (!tables.contains("Browser_Bypass_SSL".toUpperCase())) {
                new TableBrowserBypassSSL().init(conn);
            }
            if (!tables.contains("Color_Data".toUpperCase())) {
                new TableColorData().init(conn);
            }
            if (!tables.contains("Geography_Code".toUpperCase())) {
                new TableGeographyCode().createTable(conn);
            }
            if (!tables.contains("Dataset".toUpperCase())) {
                new TableDataset().createTable(conn);
            }
            if (!tables.contains("Location_Data".toUpperCase())) {
                new TableLocationData().createTable(conn);
            }
            if (!tables.contains("Epidemic_Report".toUpperCase())) {
                new TableEpidemicReport().createTable(conn);
            }
            if (!tables.contains("Query_Condition".toUpperCase())) {
                new TableQueryCondition().init(conn);
            }
            if (!tables.contains("String_Value".toUpperCase())) {
                new TableStringValue().init(conn);
            }
//            if (!tables.contains("File_History".toUpperCase())) {
//                new TableFileHistory().createTable(conn);
//            }
            if (!tables.contains("MyBox_Log".toUpperCase())) {
                new TableMyBoxLog().createTable(conn);
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
//                    MyBoxLog.debug(e.toString());
                }
            }
            if (!indexes.contains("Geography_Code_code_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableGeographyCode.Create_Index_codeIndex);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
//                    MyBoxLog.debug(e.toString());
                }
            }
            if (!indexes.contains("Geography_Code_gcid_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableGeographyCode.Create_Index_gcidIndex);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
//                    MyBoxLog.debug(e.toString());
                }
            }
            if (!indexes.contains("Epidemic_Report_DatasetTimeDesc_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableEpidemicReport.Create_Index_DatasetTimeDesc);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
//                    MyBoxLog.debug(e.toString());
                }
            }
            if (!indexes.contains("Epidemic_Report_DatasetTimeAsc_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableEpidemicReport.Create_Index_DatasetTimeAsc);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
//                    MyBoxLog.debug(e.toString());
                }
            }
            if (!indexes.contains("Epidemic_Report_timeAsc_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableEpidemicReport.Create_Index_TimeAsc);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
//                    MyBoxLog.debug(e.toString());
                }
            }
            if (!indexes.contains("Dataset_unique_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableDataset.Create_Index_unique);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
//                    MyBoxLog.debug(e.toString());
                }
            }
//            if (!indexes.contains("File_History_unique_index".toUpperCase())) {
//                try ( Statement statement = conn.createStatement()) {
//                    statement.executeUpdate(TableMyBoxLog.Create_Index_unique);
//                } catch (Exception e) {
////                    MyBoxLog.error(e);
////                    MyBoxLog.debug(e.toString());
//                }
//            }
            if (!indexes.contains("MyBox_Log_index".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableMyBoxLog.Create_Index);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
//                    MyBoxLog.debug(e.toString());
                }
            }
//            if (!indexes.contains("Download_History_url_index".toUpperCase())) {
//                try ( Statement statement = conn.createStatement()) {
//                    statement.executeUpdate(TableDownloadHistory.Create_Index_url);
//                } catch (Exception e) {
//                    MyBoxLog.error(e);
//                    MyBoxLog.debug(e.toString());
//                }
//            }
//            if (!indexes.contains("Download_History_filename_index".toUpperCase())) {
//                try ( Statement statement = conn.createStatement()) {
//                    statement.executeUpdate(TableDownloadHistory.Create_Index_filename);
//                } catch (Exception e) {
//                    MyBoxLog.error(e);
//                    MyBoxLog.debug(e.toString());
//                }
//            }
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
//                    MyBoxLog.debug(e.toString());
                }
            }
            if (!views.contains("Location_Data_View".toUpperCase())) {
                try ( Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableLocationData.CreateView);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
//                    MyBoxLog.debug(e.toString());
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

    public static boolean dropTables() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            new TableSystemConf().drop(conn);
            new TableUserConf().drop(conn);
            new TableAlarmClock().drop(conn);
            new TableImageHistory().drop(conn);
            new TableConvolutionKernel().drop(conn);
            new TableFloatMatrix().drop(conn);
            new TableVisitHistory().drop(conn);
            new TableImageScope().drop(conn);
            new TableStringValues().drop(conn);
            new TableMediaList().drop(conn);
            new TableMedia().drop(conn);
            new TableBrowserHistory().drop(conn);
            new TableBrowserBypassSSL().drop(conn);
            new TableColorData().drop(conn);
            try {
                conn.createStatement().executeUpdate("DROP VIEW Epidemic_Report_Statistic_View");
            } catch (Exception e) {
            }
            new TableEpidemicReport().dropTable(conn);
            new TableGeographyCode().dropTable(conn);
            new TableQueryCondition().drop(conn);
            new TableStringValue().drop(conn);
            new TableDataset().dropTable(conn);
            try {
                conn.createStatement().executeUpdate("DROP VIEW Location_Data_View");
            } catch (Exception e) {
            }
            new TableLocationData().dropTable(conn);
            new TableMyBoxLog().dropTable(conn);
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return false;
        }
    }

    public static boolean clearData() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            new TableUserConf().clear(conn);
            new TableAlarmClock().clear(conn);
            new TableImageHistory().clear(conn);
            new TableConvolutionKernel().clear(conn);
            new TableFloatMatrix().clear(conn);
            new TableVisitHistory().clear(conn);
            new TableImageScope().clear(conn);
            new TableStringValues().clear(conn);
            new TableMediaList().clear(conn);
            new TableMedia().clear(conn);
            new TableBrowserHistory().clear(conn);
            new TableBrowserBypassSSL().clear(conn);
            new TableColorData().clear(conn);
            new TableEpidemicReport().clearData(conn);
            new TableGeographyCode().clearData(conn);
            new TableQueryCondition().clear(conn);
            new TableStringValue().clear(conn);
            new TableDataset().clearData(conn);
            new TableLocationData().clearData(conn);
            new TableMyBoxLog().clearData(conn);
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return false;
        }
    }

    public static int size(String sql) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
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
        FileTools.delete(f);
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
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
