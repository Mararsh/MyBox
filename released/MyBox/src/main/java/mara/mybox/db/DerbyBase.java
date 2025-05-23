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
import java.util.Map;
import mara.mybox.controller.MyBoxLoadingController;
import mara.mybox.db.table.BaseTable;
import static mara.mybox.db.table.BaseTableTools.internalTables;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.db.table.TableColorPaletteName;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableFileBackup;
import mara.mybox.db.table.TableMyBoxLog;
import mara.mybox.db.table.TableWebHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.NetworkTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import static mara.mybox.value.Languages.sysDefaultLanguage;
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

    public enum DerbyStatus {
        Embedded, Nerwork, Starting, NotConnected, EmbeddedFailed, NerworkFailed
    }

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
            if (isServerStarted(port)) {
                shutdownDerbyServer();
            } else {
//                shutdownEmbeddedDerby();
            }
            Class.forName(embeddedDriver).getDeclaredConstructors()[0].newInstance();
            String lang = sysDefaultLanguage();
            if (!startEmbededDriver()) {
                status = DerbyStatus.NotConnected;
                return MessageFormat.format(message(lang, "DerbyNotAvalibale"), AppVariables.MyBoxDerbyPath);
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

    public static boolean startEmbededDriver() {
        try (Connection conn = DriverManager.getConnection("jdbc:derby:" + dbHome() + create)) {
            return true;
        } catch (Exception e) {
            status = DerbyStatus.EmbeddedFailed;
            MyBoxLog.console(e);
            return false;
        }
    }

    // https://db.apache.org/derby/docs/10.17/devguide/rdevcsecure26537.html
    public static void shutdownEmbeddedDerby() {
        try (Connection conn = DriverManager.getConnection("jdbc:derby:;shutdown=true")) {
        } catch (Exception e) {
            status = DerbyStatus.NotConnected;
//            MyBoxLog.console(e);
        }
    }

    // Derby will run only at localhost.
    // To avoid to let user have to configure security and network for it.
    public static String networkMode() {
        try {
            if (status == DerbyStatus.Starting) {
                String lang = sysDefaultLanguage();
                return message(lang, "BeingStartingDerby");
            }
            Class.forName(ClientDriver).getDeclaredConstructors()[0].newInstance();
            String lang = sysDefaultLanguage();
            if (startDerbyServer()) {
                driver = ClientDriver;
                protocol = "jdbc:derby://" + host + ":" + port + "/";
                mode = "client";
                status = DerbyStatus.Nerwork;
                ConfigTools.writeConfigValue("DerbyMode", mode);
                MyBoxLog.console("Driver: " + driver);
                return MessageFormat.format(message(lang, "DerbyServerListening"), port + "");

            } else if (startEmbededDriver() && status != DerbyStatus.EmbeddedFailed) {
                return embeddedMode();

            } else {
                status = DerbyStatus.NotConnected;
                return MessageFormat.format(message(lang, "DerbyNotAvalibale"), AppVariables.MyBoxDerbyPath);
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
        try (Connection conn = DriverManager.getConnection(
                "jdbc:derby://" + host + ":" + uPort + "/" + dbHome() + login)) {
            port = uPort;
            status = DerbyStatus.Nerwork;
            return true;
        } catch (Exception e) {
//            MyBoxLog.console(e);
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
//                MyBoxLog.debug(e);
                try {
                    Thread.currentThread().sleep(wait);
                } catch (Exception ex) {
                }
            }
        }
        return started;
    }

    public static boolean isStarted() {
        return DerbyBase.status == DerbyStatus.Embedded
                || DerbyBase.status == DerbyStatus.Nerwork;
    }

    // Upper case
    public static List<String> allTables(Connection conn) {
        try {
            List<String> tables = new ArrayList<>();
            String sql = "SELECT TABLENAME FROM SYS.SYSTABLES WHERE TABLETYPE='T' ORDER BY TABLENAME";
            conn.setAutoCommit(true);
            try (Statement statement = conn.createStatement();
                    ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    String savedName = resultSet.getString("TABLENAME");
                    String referredName = fixedIdentifier(savedName);
                    tables.add(referredName);
                }
            } catch (Exception e) {
                MyBoxLog.error(e, sql);
            }
            return tables;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<String> columns(Connection conn, String tablename) {
        try {
            List<String> columns = new ArrayList<>();
            String sql = "SELECT columnname, columndatatype FROM SYS.SYSTABLES t, SYS.SYSCOLUMNS c "
                    + " where t.TABLEID=c.REFERENCEID AND tablename='" + tablename.toUpperCase() + "'"
                    + " order by columnnumber";
            conn.setAutoCommit(true);
            try (Statement statement = conn.createStatement();
                    ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    String savedName = resultSet.getString("columnname");
                    String referredName = fixedIdentifier(savedName);
                    columns.add(referredName + ", " + resultSet.getString("columndatatype"));
                }
            } catch (Exception e) {
                MyBoxLog.error(e, sql);
            }
            return columns;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    // lower case
    public static List<String> columnNames(Connection conn, String tablename) {
        try {
            List<String> columns = new ArrayList<>();
            String sql = "SELECT columnname  FROM SYS.SYSTABLES t, SYS.SYSCOLUMNS c "
                    + " where t.TABLEID=c.REFERENCEID AND tablename='" + tablename.toUpperCase() + "'"
                    + " order by columnnumber";
            conn.setAutoCommit(true);
            try (Statement statement = conn.createStatement();
                    ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    String savedName = resultSet.getString("columnname");
                    String referredName = fixedIdentifier(savedName);
                    columns.add(referredName);
                }
            } catch (Exception e) {
                MyBoxLog.error(e, sql);
            }
            return columns;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<String> indexes(Connection conn) {
        try {
            List<String> indexes = new ArrayList<>();
            String sql = "SELECT CONGLOMERATENAME FROM SYS.SYSCONGLOMERATES";
            conn.setAutoCommit(true);
            try (Statement statement = conn.createStatement();
                    ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    String savedName = resultSet.getString("CONGLOMERATENAME");
                    String referredName = fixedIdentifier(savedName);
                    indexes.add(referredName);
                }
            } catch (Exception e) {
                MyBoxLog.console(e, sql);
            }
            return indexes;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<String> views(Connection conn) {
        try {
            List<String> tables = new ArrayList<>();
            String sql = "SELECT TABLENAME FROM SYS.SYSTABLES WHERE TABLETYPE='V'";
            conn.setAutoCommit(true);
            try (Statement statement = conn.createStatement();
                    ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    String savedName = resultSet.getString("TABLENAME");
                    String referredName = fixedIdentifier(savedName);
                    tables.add(referredName);
                }
            } catch (Exception e) {
                MyBoxLog.console(e, sql);
            }
            return tables;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static boolean initTables(MyBoxLoadingController loadingController) {
        MyBoxLog.console("Protocol: " + protocol + dbHome());
        try (Connection conn = DriverManager.getConnection(protocol + dbHome() + create)) {
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

            Map<String, BaseTable> internalTables = internalTables();
            for (String name : internalTables.keySet()) {
                if (tables.contains(name.toLowerCase())
                        || name.startsWith("NODE") && name.endsWith("_TAG")) {
                    continue;
                }
                internalTables.get(name).createTable(conn);
                loadingController.info("Created table: " + name);
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
            if (!indexes.contains("MyBox_Log_index".toLowerCase())) {
                try (Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableMyBoxLog.Create_Index);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("File_Backup_index".toLowerCase())) {
                try (Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableFileBackup.Create_Index);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("Color_rgba_unique_index".toLowerCase())) {
                try (Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableColor.Create_RGBA_Unique_Index);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("Color_Palette_Name_unique_index".toLowerCase())) {
                try (Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableColorPaletteName.Create_Unique_Index);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }
            if (!indexes.contains("Web_History_time_index".toLowerCase())) {
                try (Statement statement = conn.createStatement()) {
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
            if (!views.contains("Data2D_Column_View".toLowerCase())) {
                try (Statement statement = conn.createStatement()) {
                    statement.executeUpdate(TableData2DColumn.CreateView);
                } catch (Exception e) {
//                    MyBoxLog.error(e);
                }
            }
            if (!views.contains("Color_Palette_View".toLowerCase())) {
                try (Statement statement = conn.createStatement()) {
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

    public static int size(String sql) {
        try (Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return size(conn, sql);
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return 0;
        }
    }

    public static int size(Connection conn, String sql) {
        int size = 0;
        try {
            boolean ac = conn.getAutoCommit();
            conn.setAutoCommit(true);
            try (ResultSet results = conn.createStatement().executeQuery(sql)) {
                if (results != null && results.next()) {
                    size = results.getInt(1);
                }
            } catch (Exception e) {
                MyBoxLog.error(e, sql);
            }
            conn.setAutoCommit(ac);
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return 0;
        }
        return size;

    }

    public static boolean isTableEmpty(String tableName) {
        try (Connection conn = DerbyBase.getConnection()) {
            String sql = "SELECT * FROM " + tableName + " FETCH FIRST ROW ONLY";
            conn.setReadOnly(true);
            return isEmpty(conn, sql);
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
            return false;
        }
    }

    public static boolean isEmpty(Connection conn, String sql) {
        try {
            boolean isEmpty = true;
            conn.setAutoCommit(true);
            try (ResultSet results = conn.createStatement().executeQuery(sql)) {
                isEmpty = results == null || !results.next();
            } catch (Exception e) {
                MyBoxLog.error(e, sql);
            }
            return isEmpty;
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return false;
        }
    }

    public static ResultSet query(String sql) {
        try (Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return query(conn, sql);
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return null;
        }
    }

    public static ResultSet query(Connection conn, String sql) {
        try (Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);
            return statement.executeQuery(sql);
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return null;
        }
    }

    public static int update(String sql) {
        try (Connection conn = DerbyBase.getConnection()) {
            return update(conn, sql);
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return 0;
        }
    }

    public static int update(Connection conn, String sql) {
        try (Statement statement = conn.createStatement()) {
            return statement.executeUpdate(sql);
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
            return 0;
        }
    }

    public static int exist(Connection conn, String referredName) {
        if (conn == null || referredName == null) {
            return -1;
        }
        try (ResultSet resultSet = conn.getMetaData().getColumns(null, "MARA",
                DerbyBase.savedName(referredName), "%")) {
            if (resultSet.next()) {
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e, referredName);
            return -2;
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
        try (Connection conn = DerbyBase.getConnection();) {
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
        try (Connection conn = DerbyBase.getConnection();) {
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

    // https://db.apache.org/derby/docs/10.17/ref/crefsqlj1003454.html#crefsqlj1003454
    // https://db.apache.org/derby/docs/10.17/ref/rrefkeywords29722.html
    public static String fixedIdentifier(String name) {
        if (name == null) {
            return null;
        }
        if (name.startsWith("\"") && name.endsWith("\"")) {
            return name;
        }
        String sname = name.toLowerCase().replaceAll("（", "(").replaceAll("）", ")");
        boolean needQuote = false;
        for (int i = 0; i < sname.length(); i++) {
            char c = sname.charAt(i);
            if (c != '_' && !Character.isLetterOrDigit(c)) {
                needQuote = true;
                break;
            }
            if (i == 0 && Character.isDigit(c)) {
                needQuote = true;
                break;
            }
        }
        return needQuote ? "\"" + sname + "\"" : sname;
    }

    public static String appendIdentifier(String name, String suffix) {
        if (name == null || suffix == null || suffix.isBlank()) {
            return null;
        }
        return fixedIdentifier((name + suffix).replaceAll("\"", ""));
    }

    public static String checkIdentifier(List<String> names, String name, boolean add) {
        if (name == null) {
            return null;
        }
        if (names == null) {
            names = new ArrayList<>();
        }
        String tname = name;
        int index = 1;
        while (names.contains(tname) || names.contains(tname.toUpperCase())) {
            tname = DerbyBase.appendIdentifier(name, ++index + "");
        }
        if (add) {
            names.add(tname);
        }
        return tname;
    }

    public static String savedName(String referedName) {
        if (referedName == null) {
            return null;
        }
        if (referedName.startsWith("\"") && referedName.endsWith("\"")) {
            return referedName.substring(1, referedName.length() - 1);
        } else {
            return referedName.toUpperCase();
        }
    }

}
