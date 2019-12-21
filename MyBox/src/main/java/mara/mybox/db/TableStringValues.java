package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2019-8-21
 * @License Apache License Version 2.0
 */
public class TableStringValues extends DerbyBase {

    public TableStringValues() {
        Table_Name = "String_Values";
        Keys = new ArrayList<>() {
            {
                add("key_name");
                add("string_value");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE String_Values ( "
                + "  key_name  VARCHAR(1024) NOT NULL, "
                + "  string_value VARCHAR(32672)  NOT NULL, "
                + "  create_time TIMESTAMP NOT NULL, "
                + "  PRIMARY KEY (key_name, string_value)"
                + " )";
    }

    public static List<String> read(String name) {
        List<String> records = new ArrayList<>();
        if (name == null || name.trim().isEmpty()) {
            return records;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM String_Values WHERE key_name='" + name + "' ORDER BY create_time DESC";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                records.add(results.getString("string_value"));
            }
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
        }
        return records;
    }

    public static String last(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        String value = null;
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            statement.setMaxRows(1);
            String sql = " SELECT * FROM String_Values WHERE key_name='" + name + "' ORDER BY create_time DESC";
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                value = results.getString("string_value");
            }
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
        }
        return value;
    }

    public static List<String> max(String name, int max) {
        List<String> records = read(name);
        if (name == null || name.trim().isEmpty()
                || max < 0 || max >= records.size()) {
            return records;
        }
        records = records.subList(0, max);
        clear(name);
        return add(name, records);
    }

    public static List<String> add(String name, String value) {
        if (name == null || name.trim().isEmpty()
                || value == null || value.trim().isEmpty()) {
            return read(name);
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "INSERT INTO String_Values(key_name, string_value , create_time) VALUES('"
                    + name + "', '" + value + "', '"
                    + DateTools.datetimeToString(new Date()) + "')";
            statement.executeUpdate(sql);
            return read(name);
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return read(name);
        }
    }

    public static List<String> add(String name, List<String> values) {
        if (values == null || values.isEmpty()) {
            return read(name);
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql;
            conn.setAutoCommit(false);
            for (String value : values) {
                try {
                    sql = "INSERT INTO String_Values(key_name, string_value , create_time) VALUES('"
                            + name + "', '" + value + "', '"
                            + DateTools.datetimeToString(new Date()) + "')";
                    statement.executeUpdate(sql);
                } catch (Exception e) {
                    failed(e);
//                    // logger.debug(e.toString());
                }
            }
            conn.commit();
            return read(name);
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return read(name);
        }
    }

    public static String exist(String name, String value) {
        if (name == null || name.trim().isEmpty()
                || value == null || value.trim().isEmpty()) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM String_Values WHERE key_name='" + name
                    + "' AND string_value='" + value + "'";
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                return name;
            } else {
                return null;
            }
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static boolean delete(String name, String value) {
        if (name == null || name.trim().isEmpty()
                || value == null || value.trim().isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM String_Values WHERE key_name='" + name
                    + "' AND string_value='" + value + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean clear(String name) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM String_Values WHERE key_name='" + name + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            logger.debug(e.toString());
            return false;
        }
    }

    public static List<String> prefixNames(String prefix) {
        List<String> names = new ArrayList<>();
        if (prefix == null || prefix.trim().isEmpty()) {
            return names;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT DISTINCT key_name  FROM String_Values WHERE key_name like '"
                    + prefix + "%' ORDER BY key_name";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                names.add(results.getString("key_name"));
            }
        } catch (Exception e) {
            failed(e);
//            logger.debug(e.toString());
        }
        return names;
    }

    public static boolean clearPrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM String_Values WHERE  key_name like '"
                    + prefix + "%' ";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

}
