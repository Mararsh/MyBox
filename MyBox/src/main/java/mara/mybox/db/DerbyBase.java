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
//            // logger.debug(e.toString());
            return false;
        }
    }

    public boolean init() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
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
            try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
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
            try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
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
    public static String dbName() {
        return AppVariables.MyBoxDerbyPath.getAbsolutePath();
    }

    public static String initDatabase() {
        try {
            String ret = startDerby();
            initTables();
            return ret;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static String startDerby() {
        String m = ConfigTools.readConfigValue("DerbyMode");
        if (m != null) {
            mode = "client".equals(m) ? "client" : "embedded";
        } else {
            mode = "embedded";
        }
        ConfigTools.writeConfigValue("DerbyMode", mode);

        if ("client".equals(mode)) {
            return networkMode();
        } else {
            return embeddedMode();
        }
    }

    public static String embeddedMode() {
        try {
            if (isServerStarted(port)) {
                shutdownDerbyServer();
            }
            driver = embeddedDriver;
            protocol = "jdbc:derby:";
            Class.forName(driver).getDeclaredConstructors()[0].newInstance();
            logger.debug("Driver: " + driver);

            String lang = Locale.getDefault().getLanguage().toLowerCase();
            return message(lang, "DerbyEmbeddedMode");
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    // Derby will run only at localhost.
    // To avoid to let user have to configure security and network for it.
    public static String networkMode() {
        try {
            String ret;
            String lang = Locale.getDefault().getLanguage().toLowerCase();
            if (startDerbyServer()) {
                driver = clientDriver;
                protocol = "jdbc:derby://" + host + ":" + port + "/";
                ret = MessageFormat.format(message(lang, "DerbyServerListening"), port + "");
            } else {
                driver = embeddedDriver;
                protocol = "jdbc:derby:";
                ret = message(lang, "DerbyFailStartServer");
            }
            Class.forName(driver).getDeclaredConstructors()[0].newInstance();
            logger.debug("Driver: " + driver);
            return ret;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
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
            server.start(null);
//            server.setTraceDirectory("d:/tmp");
            server.trace(false);
            if (isServerStarted(server)) {
                port = uPort;
                logger.debug("Derby server is listening in port " + port + ".");
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.debug(e.toString());
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
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean isServerStarted(int uPort) {
        try ( Connection conn = DriverManager.getConnection(
                "jdbc:derby://" + host + ":" + uPort + "/" + dbName() + login)) {
            port = uPort;
            return true;
        } catch (Exception e) {
//            logger.debug(e.toString());
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
        logger.debug("Protocol: " + protocol);
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + create);
                 Statement statement = conn.createStatement()) {
            List<String> tables = new DerbyBase().tables(statement);
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
            if (!tables.contains("Browser_URLs".toUpperCase())) {
                new TableBrowserUrls().init(statement);
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
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean dropTables() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            new TableSystemConf().drop(statement);
            new TableUserConf().drop(statement);
            new TableAlarmClock().drop(statement);
            new TableBrowserUrls().drop(statement);
            new TableImageHistory().drop(statement);
            new TableConvolutionKernel().drop(statement);
            new TableFloatMatrix().drop(statement);
            new TableVisitHistory().drop(statement);
            new TableImageScope().drop(statement);
            new TableStringValues().drop(statement);
            new TableSRGB().drop(statement);
            return true;
        } catch (Exception e) {
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean clearData() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            new TableUserConf().clear(statement);
            new TableAlarmClock().clear(statement);
            new TableBrowserUrls().clear(statement);
            new TableImageHistory().clear(statement);
            new TableConvolutionKernel().clear(statement);
            new TableFloatMatrix().clear(statement);
            new TableVisitHistory().clear(statement);
            new TableImageScope().clear(statement);
            new TableStringValues().clear(statement);
            new TableSRGB().clear(statement);
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
                startDerby();
            }
        } catch (Exception e) {

        }
    }

}
