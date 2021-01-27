package mara.mybox.db.table;

import mara.mybox.db.DerbyBase;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import mara.mybox.tools.DateTools;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2019-8-21
 * @License Apache License Version 2.0
 */
public class TableStringValue extends DerbyBase {

    public static final String Query
            = "SELECT * FROM String_Value WHERE key_name=?";

    public static final String Update
            = "UPDATE String_Value SET create_time=?, string_value=? WHERE key_name=?";

    public static final String Insert
            = "INSERT INTO String_Value (key_name, string_value , create_time) VALUES (?,?,?)";

    public static final String Delete
            = "DELETE FROM String_Value WHERE key_name=?";

    public TableStringValue() {
        Table_Name = "String_Value";
        Keys = new ArrayList<>() {
            {
                add("key_name");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE String_Value ( "
                + "  key_name  VARCHAR(1024) NOT NULL, "
                + "  string_value VARCHAR(32672)  NOT NULL, "
                + "  create_time TIMESTAMP NOT NULL, "
                + "  PRIMARY KEY (key_name)"
                + " )";
    }

    public static String read(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            return read(conn, name);
        } catch (Exception e) {
            MyBoxLog.error(e);

        }
        return null;
    }

    public static String read(Connection conn, String name) {
        if (conn == null || name == null || name.trim().isEmpty()) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(Query)) {
            statement.setMaxRows(1);
            statement.setString(1, name);
            try ( ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getString("string_value");
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);

        }
        return null;
    }

    public static boolean write(String name, String value) {
        if (name == null || name.trim().isEmpty()
                || value == null || value.trim().isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return write(conn, name, value);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean write(Connection conn, String name, String value) {
        try {
            boolean existed = false;
            try ( PreparedStatement statement = conn.prepareStatement(Query)) {
                statement.setMaxRows(1);
                statement.setString(1, name);
                try ( ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        existed = true;
                    }
                }
            }
            if (existed) {
                try ( PreparedStatement statement = conn.prepareStatement(Update)) {
                    statement.setString(1, DateTools.datetimeToString(new Date()));
                    statement.setString(2, value);
                    statement.setString(3, name);
                    return statement.executeUpdate() > 0;
                }
            } else {
                try ( PreparedStatement statement = conn.prepareStatement(Insert)) {
                    statement.setString(1, name);
                    statement.setString(2, value);
                    statement.setString(3, DateTools.datetimeToString(new Date()));
                    return statement.executeUpdate() > 0;
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean write(Map<String, String> nameValues) {
        return writeWithPrefix(null, nameValues);
    }

    public static boolean writeWithPrefix(String prefix, Map<String, String> nameValues) {
        if (nameValues == null || nameValues.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setAutoCommit(false);
            for (String name : nameValues.keySet()) {
                String value = nameValues.get(name);
                if (prefix != null) {
                    write(conn, prefix + name, value);
                } else {
                    write(conn, name, value);
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);

            return false;
        }
    }

    public static boolean delete(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 PreparedStatement statement = conn.prepareStatement(Delete)) {
            statement.setString(1, name);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            MyBoxLog.error(e);

            return false;
        }
    }

    public static Map<String, String> readWithPrefix(String prefix) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            return readWithPrefix(conn, prefix);
        } catch (Exception e) {
            MyBoxLog.error(e);
//            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static Map<String, String> readWithPrefix(Connection conn, String prefix) {
        Map<String, String> keyValues = new HashMap<>();
        if (conn == null || prefix == null || prefix.trim().isEmpty()) {
            return keyValues;
        }
        String sql = " SELECT key_name, string_value  FROM String_Value WHERE key_name like '"
                + stringValue(prefix) + "%' ";
        try ( Statement statement = conn.createStatement();
                 ResultSet results = statement.executeQuery(sql)) {
            while (results.next()) {
                keyValues.put(results.getString("key_name"), results.getString("string_value"));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
//            MyBoxLog.debug(e.toString());
        }
        return keyValues;
    }

    public static boolean clearPrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM String_Value WHERE  key_name like '"
                    + stringValue(prefix) + "%' ";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);

            return false;
        }
    }

}
