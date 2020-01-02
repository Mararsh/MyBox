package mara.mybox.db;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import mara.mybox.data.ConvolutionKernel;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.NetworkTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import org.apache.derby.drda.NetworkServerControl;

/**
 * @Author Mara
 * @CreateDate 2018-10-15 9:31:28
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class DerbyBase {

    public static String mode = "embedded";
    public static String host = "localhost";
    public static int port = 1527;
    public static String driver, protocol;
    protected static final String clientDriver = "org.apache.derby.jdbc.ClientDriver";
    protected static final String embeddedDriver = "org.apache.derby.jdbc.EmbeddedDriver";
    protected static final String create = ";user=" + CommonValues.AppDerbyUser + ";password="
            + CommonValues.AppDerbyPassword + ";create=true";
    public static final String login = ";user=" + CommonValues.AppDerbyUser + ";password="
            + CommonValues.AppDerbyPassword + ";create=false";
    public static DerbyStatus status;

    public enum DerbyStatus {
        Embedded, Nerwork, Starting, NotConnected, EmbeddedFailed, NerworkFailed
    }

    protected String Table_Name, Create_Table_Statement;
    protected List<String> Keys;

    public boolean init(Statement statement) {
        try {
            if (statement == null) {
                return false;
            }
//            logger.debug(Create_Table_Statement);
            statement.executeUpdate(Create_Table_Statement);
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public boolean init() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            statement.executeUpdate(Create_Table_Statement);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public boolean drop(Statement statement) {
        try {
            if (statement == null) {
                return false;
            }
            String sql = "DROP TABLE " + Table_Name;
            statement.executeUpdate(sql);
//            logger.debug(Create_Table_Statement);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public boolean drop() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "DROP TABLE " + Table_Name;
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public boolean clear(Statement statement) {
        try {
            if (statement == null) {
                return false;
            }
            String sql = "DELETE FROM " + Table_Name;
            statement.executeUpdate(sql);
//            logger.debug(Create_Table_Statement);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public boolean clear() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM " + Table_Name;
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public ResultSet query(String sql) {
        try {
            ResultSet resultSet;
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                     Statement statement = conn.createStatement()) {
                resultSet = statement.executeQuery(sql);
            }
            return resultSet;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public int update(String sql) {
        try {
            int ret;
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                     Statement statement = conn.createStatement()) {
                ret = statement.executeUpdate(sql);
            }
            return ret;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return -1;
        }
    }

    public List<String> tables(Statement statement) {
        List<String> tables = new ArrayList<>();
        try {
            String sql = "SELECT TABLENAME FROM SYS.SYSTABLES WHERE TABLETYPE='T'";
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                tables.add(resultSet.getString("TABLENAME"));
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return tables;
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
            Class.forName(clientDriver).getDeclaredConstructors()[0].newInstance();
            if ("client".equals(readMode())) {
                return networkMode();
            } else {
                return embeddedMode();
            }
        } catch (Exception e) {
            logger.debug(e.toString());
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
            logger.debug("Driver: " + driver);
            mode = "embedded";
            ConfigTools.writeConfigValue("DerbyMode", mode);
            status = DerbyStatus.Embedded;
            return message(lang, "DerbyEmbeddedMode");
        } catch (Exception e) {
            logger.debug(e.toString());
            status = DerbyStatus.EmbeddedFailed;
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
                driver = clientDriver;
                protocol = "jdbc:derby://" + host + ":" + port + "/";
                mode = "client";
                status = DerbyStatus.Nerwork;
                ConfigTools.writeConfigValue("DerbyMode", mode);
                logger.debug("Driver: " + driver);
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
            logger.debug(e.toString());
            return e.toString();
        }
    }

    public static boolean startDerbyServer() {
        try {
            boolean portUsed = NetworkTools.isPortUsed(port);
            int uPort = port;
            if (portUsed) {
                if (DerbyBase.isServerStarted(port)) {
                    logger.debug("Derby server is already started in port " + port + ".");
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
                logger.debug("Derby server is listening in port " + port + ".");
                status = DerbyStatus.Nerwork;
                return true;
            } else {
                status = DerbyStatus.NerworkFailed;
                return false;
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            status = DerbyStatus.NerworkFailed;
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
            logger.debug(e.toString());
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
//            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean canEmbeded() {
        try ( Connection conn = DriverManager.getConnection(
                "jdbc:derby:" + dbHome() + create)) {
            return true;
        } catch (Exception e) {
//            logger.debug(e.toString());
            status = DerbyStatus.EmbeddedFailed;
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
//                failed(e);
//                logger.debug(e.toString());
                try {
                    Thread.currentThread().sleep(wait);
                } catch (Exception ex) {
                }
            }
        }
        return started;
    }

    public static boolean initTables() {
        logger.debug("Protocol: " + protocol + dbHome());
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + create);
                 Statement statement = conn.createStatement()) {
            List<String> tables = new DerbyBase().tables(statement);
//            logger.debug(tables);
            if (!tables.contains("SRGB")) {
                new TableSRGB().init(statement);
            }
            if (!tables.contains("String_Values".toUpperCase())) {
                new TableStringValues().init(statement);
            }
            if (!tables.contains("image_scope".toUpperCase())) {
                new TableImageScope().init(statement);
            }
            if (!tables.contains("System_Conf".toUpperCase())) {
                new TableSystemConf().init(statement);
            }
            if (!tables.contains("User_Conf".toUpperCase())) {
                new TableUserConf().init(statement);
            }
            if (!tables.contains("Alarm_Clock".toUpperCase())) {
                new TableAlarmClock().init(statement);
            }
            if (!tables.contains("image_history".toUpperCase())) {
                new TableImageHistory().init(statement);
            }
            if (!tables.contains("Convolution_Kernel".toUpperCase())) {
                new TableConvolutionKernel().init(statement);
            }
            if (!tables.contains("Float_Matrix".toUpperCase())) {
                new TableFloatMatrix().init(statement);
            }
            if (!tables.contains("visit_history".toUpperCase())) {
                new TableVisitHistory().init(statement);
            }
            if (!tables.contains("media_list".toUpperCase())) {
                new TableMediaList().init(statement);
            }
            if (!tables.contains("media".toUpperCase())) {
                new TableMedia().init(statement);
            }
            if (!tables.contains("Browser_History".toUpperCase())) {
                new TableBrowserHistory().init(statement);
            }
            if (!tables.contains("Browser_Bypass_SSL".toUpperCase())) {
                new TableBrowserBypassSSL().init(statement);
            }
            return true;
        } catch (Exception e) {
//            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean dropTables() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            new TableSystemConf().drop(statement);
            new TableUserConf().drop(statement);
            new TableAlarmClock().drop(statement);
            new TableImageHistory().drop(statement);
            new TableConvolutionKernel().drop(statement);
            new TableFloatMatrix().drop(statement);
            new TableVisitHistory().drop(statement);
            new TableImageScope().drop(statement);
            new TableStringValues().drop(statement);
            new TableSRGB().drop(statement);
            new TableMediaList().drop(statement);
            new TableMedia().drop(statement);
            new TableBrowserHistory().drop(statement);
            new TableBrowserBypassSSL().drop(statement);
            return true;
        } catch (Exception e) {
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean clearData() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            new TableUserConf().clear(statement);
            new TableAlarmClock().clear(statement);
            new TableImageHistory().clear(statement);
            new TableConvolutionKernel().clear(statement);
            new TableFloatMatrix().clear(statement);
            new TableVisitHistory().clear(statement);
            new TableImageScope().clear(statement);
            new TableStringValues().clear(statement);
            new TableSRGB().clear(statement);
            new TableMediaList().clear(statement);
            new TableMedia().clear(statement);
            new TableBrowserHistory().clear(statement);
            new TableBrowserBypassSSL().clear(statement);
            return true;
        } catch (Exception e) {
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean checkUpdates() {
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

            if (!AppVariables.getSystemConfigBoolean("UpdatedTables5.9", false)) {
                logger.info("Updating tables in 5.8...");
                DerbyBase t = new DerbyBase();
                String sql = "ALTER TABLE SRGB  add  column  palette_index  INT";
                t.update(sql);

                List<String> saveColors = TableStringValues.read("ColorPalette");
                if (saveColors != null && !saveColors.isEmpty()) {
                    TableSRGB.updatePalette(saveColors);
                }
                TableStringValues.clear("ColorPalette");
                AppVariables.setSystemConfigValue("UpdatedTables5.9", true);
            }

            if (!AppVariables.getSystemConfigBoolean("UpdatedTables5.9a", false)) {
                logger.info("Updating tables in 5.9...");
                DerbyBase t = new DerbyBase();
                String sql = "DROP TABLE Browser_URLs";
                t.update(sql);
                AppVariables.setSystemConfigValue("UpdatedTables5.9a", true);
            }

            TableStringValues.add("InstalledVersions", CommonValues.AppVersion);

            return true;
        } catch (Exception e) {
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static void failed(Exception exception) {
        try {
            if (exception == null) {
                return;
            }
            if (exception.toString().contains("java.sql.SQLNonTransientConnectionException")) {
                status = DerbyStatus.NotConnected;
                startDerby();
            }
        } catch (Exception e) {

        }
    }

}
