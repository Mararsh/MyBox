package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ConvolutionKernel;
import static mara.mybox.objects.AppVaribles.logger;


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
    protected static final String dbName = CommonValues.DerbyDB;
    protected static final String parameters = ";user=mara;password=mybox;create=true";

    protected String Table_Name, Create_Table_Statement;
    protected List<String> Keys;

    public static void loadDriver() {
        try {
            Class.forName(driver).newInstance();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public static boolean initTables() {
        loadDriver();
        try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                Statement statement = conn.createStatement()) {
            new TableSystemConf().init(statement);
            new TableUserConf().init(statement);
            new TableAlarmClock().init(statement);
            new TableBrowserUrls().init(statement);
            new TableImageHistory().init(statement);
            new TableConvolutionKernel().init(statement);
            new TableFloatMatrix().init(statement);
            new TableImageInit().init(statement);
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean dropTables() {
        try {
            new TableSystemConf().drop();
            new TableUserConf().drop();
            new TableAlarmClock().drop();
            new TableBrowserUrls().drop();
            new TableImageHistory().drop();
            new TableConvolutionKernel().drop();
            new TableFloatMatrix().drop();
            new TableImageInit().drop();
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean clearData() {
        try {
            new TableUserConf().clear();
            new TableAlarmClock().clear();
            new TableBrowserUrls().clear();
            new TableImageHistory().clear();
            new TableConvolutionKernel().clear();
            new TableFloatMatrix().clear();
            new TableImageInit().clear();
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean checkUpdates() {
        try {
            if (!AppVaribles.getSystemConfigBoolean("UpdatedTables4.2", false)) {
                logger.debug("Updating TableConvolutionKernel 4.2");
                List<ConvolutionKernel> records = TableConvolutionKernel.read();
                TableConvolutionKernel t = new TableConvolutionKernel();
                t.drop();
                t.init();
                if (TableConvolutionKernel.write(records)) {
                    AppVaribles.setSystemConfigValue("UpdatedTables4.2", true);
                }
            }
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public boolean init(Statement statement) {
        try {
            if (statement == null) {
                return false;
            }
            statement.executeUpdate(Create_Table_Statement);
            return true;
        } catch (Exception e) {
//            logger.debug(e.toString());
            return false;
        }
    }

    public boolean init() {
        try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                Statement statement = conn.createStatement()) {
            statement.executeUpdate(Create_Table_Statement);
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public boolean clear() {
        try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM " + Table_Name;
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public boolean drop() {
        try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                Statement statement = conn.createStatement()) {
            String sql = "DROP TABLE " + Table_Name;
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public ResultSet executeSQL(String sql) {
        try {
            ResultSet resultSet;
            try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                    Statement statement = conn.createStatement()) {
                resultSet = statement.executeQuery(sql);
            }
            return resultSet;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

}
