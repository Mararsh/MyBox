package mara.mybox.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import mara.mybox.value.CommonValues;
import mara.mybox.tools.ConfigTools;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-10-15 9:31:28
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableSystemConf extends DerbyBase {

    public TableSystemConf() {
        Table_Name = "System_Conf";
        Keys = new ArrayList() {
            {
                add("key_Name");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE System_Conf ( "
                + "  key_Name  VARCHAR(100) NOT NULL PRIMARY KEY, "
                + "  int_Value INTEGER, "
                + "  default_int_Value INTEGER, "
                + "  string_Value VARCHAR(1024), "
                + "  default_string_Value VARCHAR(1024) "
                + " )";
    }

    @Override
    public boolean init(Statement statement) {
        try {
            if (statement == null) {
                return false;
            }
            statement.executeUpdate(Create_Table_Statement);
            Map<String, String> values = ConfigTools.readConfigValuesFromFile();
            if (values != null && !values.isEmpty()) {
                for (String key : values.keySet()) {
                    String value = values.get(key);
                    switch (value.toLowerCase()) {
                        case "true":
                            statement.executeUpdate("INSERT INTO System_Conf(key_Name, int_Value) VALUES('" + key + "', 1 )");
                            break;
                        case "false":
                            statement.executeUpdate("INSERT INTO System_Conf(key_Name, int_Value) VALUES('" + key + "', 0 )");
                            break;
                        default:
                            try {
                                int intv = Integer.valueOf(value);
                                statement.executeUpdate("INSERT INTO System_Conf(key_Name, int_Value) VALUES('" + key + "', " + intv + " )");
                            } catch (Exception e) {
                                statement.executeUpdate("INSERT INTO System_Conf(key_Name, string_Value) VALUES('" + key + "', '" + value + "' )");
                            }
                            break;
                    }
                }
                try {
                    new File(CommonValues.UserConfigFile).delete();
                } catch (Exception e) {
                }
            }
            return true;
        } catch (Exception e) {
//            logger.debug(e.toString());
            return false;
        }
    }

    public static int write(String keyName, String stringValue) {
        try (Connection conn = DriverManager.getConnection(protocol + dbName + login);
                Statement statement = conn.createStatement()) {
            String sql = " SELECT string_Value FROM System_Conf WHERE key_Name='" + keyName + "'";
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                if (stringValue == null) {
                    sql = "UPDATE System_Conf SET string_Value=NULL WHERE key_Name='" + keyName + "'";
//                    logger.debug(sql);
                    return statement.executeUpdate(sql);
                } else if (!stringValue.equals(results.getString("string_Value"))) {
                    sql = "UPDATE System_Conf SET string_Value='" + stringValue + "' WHERE key_Name='" + keyName + "'";
//                    logger.debug(sql);
                    return statement.executeUpdate(sql);
                } else {
                    return 0;
                }
            } else {
                sql = "INSERT INTO System_Conf(key_Name, string_Value) VALUES('" + keyName + "', '" + stringValue + "' )";
//                logger.debug(sql);
                return statement.executeUpdate(sql);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            return -1;
        }
    }

    public static int write(String keyName, int intValue) {
        try (Connection conn = DriverManager.getConnection(protocol + dbName + login);
                Statement statement = conn.createStatement()) {
            String sql = " SELECT int_Value FROM System_Conf WHERE key_Name='" + keyName + "'";
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                if (intValue != results.getInt("int_Value")) {
                    sql = "UPDATE System_Conf SET int_Value=" + intValue + " WHERE key_Name='" + keyName + "'";
//                    logger.debug(sql);
                    return statement.executeUpdate(sql);
                } else {
                    return 0;
                }
            } else {
                sql = "INSERT INTO System_Conf(key_Name, int_Value) VALUES('" + keyName + "', " + intValue + " )";
//                logger.debug(sql);
                return statement.executeUpdate(sql);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            return -1;
        }
    }

    public static int write(String keyName, boolean booleanValue) {
        return write(keyName, booleanValue ? 1 : 0);
    }

    public static String read(String keyName, String defaultValue) {
        try (Connection conn = DriverManager.getConnection(protocol + dbName + login);
                Statement statement = conn.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT string_Value FROM System_Conf WHERE key_Name='" + keyName + "'");
            if (resultSet.next()) {
                return resultSet.getString(1);
            } else {
                if (defaultValue != null) {
                    String sql = "INSERT INTO System_Conf(key_Name, string_Value) VALUES('" + keyName + "', '" + defaultValue + "' )";
//                    logger.debug(sql);
                    statement.executeUpdate(sql);
                }
                return defaultValue;
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            return defaultValue;
        }
    }

    public static int readInt(String keyName, int defaultValue) {
        try (Connection conn = DriverManager.getConnection(protocol + dbName + login);
                Statement statement = conn.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT int_Value FROM System_Conf WHERE key_Name='" + keyName + "'");
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                String sql = "INSERT INTO System_Conf(key_Name, int_Value) VALUES('" + keyName + "', " + defaultValue + " )";
//                logger.debug(sql);
                statement.executeUpdate(sql);
                return defaultValue;
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            return defaultValue;
        }
    }

    public static boolean readBoolean(String keyName, boolean defaultValue) {
        int v = readInt(keyName, defaultValue ? 1 : 0);
        return v > 0;
    }

    public static boolean delete(String keyName) {
        if (keyName == null || keyName.isEmpty()) {
            return false;
        }
        try (Connection conn = DriverManager.getConnection(protocol + dbName + login);
                Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM System_Conf WHERE key_Name='" + keyName + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

}
