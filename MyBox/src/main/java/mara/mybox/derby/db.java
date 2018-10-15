package mara.mybox.derby;

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
public class db {

    protected static final Logger logger = LogManager.getLogger();

    private static final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String protocol = "jdbc:derby:";
    private static final String dbName = CommonValues.DerbyDB;
    private static final String parameters = ";user=mara;password=mybox;create=true";
    private static final String Create_Table_User_Conf
            = " CREATE TABLE \"UserConf\" ( "
            + "  \"keyName\"  VARCHAR(50) not null primary key, "
            + "  \"intValue\" INTEGER ,"
            + "  \"stringValue\" VARCHAR(1024), "
            + ");";

    public static void loadDriver() {
        try {
            Class.forName(driver).newInstance();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public ResultSet initDB() {
        ResultSet resultSet;
        try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                Statement statement = conn.createStatement()) {
            resultSet = statement.executeQuery(Create_Table_User_Conf);
            while (resultSet.next()) {
                System.out.println(resultSet.getString(1));
                System.out.println(resultSet.getString(2));
            }
            return resultSet;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }

    }

    public ResultSet executeSQL(String sql) {
        try {
            ResultSet resultSet;
            try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                    Statement statement = conn.createStatement()) {
                resultSet = statement.executeQuery(sql);
                while (resultSet.next()) {
                    System.out.println(resultSet.getString(1));
                    System.out.println(resultSet.getString(2));
                }
            }
            return resultSet;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

}
