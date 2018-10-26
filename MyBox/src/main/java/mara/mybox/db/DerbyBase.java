package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import mara.mybox.objects.CommonValues;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-10-15 9:31:28
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class DerbyBase {

    protected static final Logger logger = LogManager.getLogger();

    protected static final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    protected static final String protocol = "jdbc:derby:";
    protected static final String dbName = CommonValues.DerbyDB;
    protected static final String parameters = ";user=mara;password=mybox;create=true";

    protected String Table_Name, KeyString, Create_Table_Statement;

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
            new TableUserConf().init(statement);
            new TableAlarmClock().init(statement);
            new TableBrowserUrls().init(statement);
            new TableImageHistory().init(statement);
            return true;
        } catch (Exception e) {
//            logger.debug(e.toString());
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
