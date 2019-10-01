package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import mara.mybox.data.ConvolutionKernel;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-10-15 9:31:28
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class DerbyBase {

    protected static final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    protected static final String protocol = "jdbc:derby:";
    protected static final String create = ";user=" + CommonValues.AppDerbyUser + ";password="
            + CommonValues.AppDerbyPassword + ";create=true";
    protected static final String login = ";user=" + CommonValues.AppDerbyUser + ";password="
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
//            // logger.debug(e.toString());
            return false;
        }
    }

    public boolean init() {
        try (Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                Statement statement = conn.createStatement()) {
            statement.executeUpdate(Create_Table_Statement);
            return true;
        } catch (Exception e) {
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static String dbName() {
        return AppVariables.MyBoxDerbyPath.getAbsolutePath();
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
//            // logger.debug(e.toString());
            return false;
        }
    }

    public boolean drop() {
        try (Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                Statement statement = conn.createStatement()) {
            String sql = "DROP TABLE " + Table_Name;
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
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
//            // logger.debug(e.toString());
            return false;
        }
    }

    public boolean clear() {
        try (Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM " + Table_Name;
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
//            // logger.debug(e.toString());
            return false;
        }
    }

    public ResultSet query(String sql) {
        try {
            ResultSet resultSet;
            try (Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                    Statement statement = conn.createStatement()) {
                resultSet = statement.executeQuery(sql);
            }
            return resultSet;
        } catch (Exception e) {
//            // logger.debug(e.toString());
            return null;
        }
    }

    public int update(String sql) {
        try {
            int ret;
            try (Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                    Statement statement = conn.createStatement()) {
                ret = statement.executeUpdate(sql);
            }
            return ret;
        } catch (Exception e) {
//            // logger.debug(e.toString());
            return -1;
        }
    }

    /*
        Static methods
     */
    public static void loadDriver() {
        try {
            Class.forName(driver).getDeclaredConstructors()[0].newInstance();
        } catch (Exception e) {
            // logger.debug(e.toString());
        }
    }

    public static boolean checkCoonection() {
        loadDriver();
        try (Connection conn = DriverManager.getConnection(protocol + dbName() + create)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean initTables() {
        loadDriver();
        try (Connection conn = DriverManager.getConnection(protocol + dbName() + create);
                Statement statement = conn.createStatement()) {
            new TableSRGB().init(statement);
            new TableStringValues().init(statement);
            new TableImageScope().init(statement);
            new TableSystemConf().init(statement);
            new TableUserConf().init(statement);
            new TableAlarmClock().init(statement);
            new TableBrowserUrls().init(statement);
            new TableImageHistory().init(statement);
            new TableConvolutionKernel().init(statement);
            new TableFloatMatrix().init(statement);
            new TableVisitHistory().init(statement);
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean dropTables() {
        try (Connection conn = DriverManager.getConnection(protocol + dbName() + create);
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
        try (Connection conn = DriverManager.getConnection(protocol + dbName() + create);
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

            TableStringValues.add("InstalledVersions", CommonValues.AppVersion);

            return true;
        } catch (Exception e) {
//            // logger.debug(e.toString());
            return false;
        }
    }

}
