package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import mara.mybox.db.DerbyBase;
import static mara.mybox.db.DerbyBase.stringValue;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.StringValues;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2019-8-21
 * @License Apache License Version 2.0
 */
public class TableStringValues extends BaseTable<StringValues> {

    public TableStringValues() {
        tableName = "String_Values";
        defineColumns();
    }

    public TableStringValues(boolean defineColumns) {
        tableName = "String_Values";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableStringValues defineColumns() {
        addColumn(new ColumnDefinition("key_name", ColumnDefinition.ColumnType.String, true, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("string_value", ColumnDefinition.ColumnType.String, true, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("create_time", ColumnDefinition.ColumnType.Datetime, true));
        orderColumns = "create_time DESC";
        return this;
    }

    public static List<String> read(String name) {
        List<String> records = new ArrayList<>();
        if (name == null || name.trim().isEmpty()) {
            return records;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return read(conn, name);
        } catch (Exception e) {
            MyBoxLog.error(e);

        }
        return records;
    }

    public static List<String> read(Connection conn, String name) {
        List<String> records = new ArrayList<>();
        if (conn == null || name == null || name.trim().isEmpty()) {
            return records;
        }
        String sql = " SELECT * FROM String_Values WHERE key_name='"
                + stringValue(name) + "' ORDER BY create_time DESC";
        try (Statement statement = conn.createStatement();
                ResultSet results = statement.executeQuery(sql)) {
            while (results.next()) {
                records.add(results.getString("string_value"));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);

        }
        return records;
    }

    public static List<StringValues> values(String name) {
        List<StringValues> records = new ArrayList<>();
        if (name == null || name.trim().isEmpty()) {
            return records;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return values(conn, name);
        } catch (Exception e) {
            MyBoxLog.error(e);

        }
        return records;
    }

    public static List<StringValues> values(Connection conn, String name) {
        List<StringValues> records = new ArrayList<>();
        if (conn == null || name == null || name.trim().isEmpty()) {
            return records;
        }
        String sql = " SELECT * FROM String_Values WHERE key_name='"
                + stringValue(name) + "' ORDER BY create_time DESC";
        try (Statement statement = conn.createStatement();
                ResultSet results = statement.executeQuery(sql)) {
            while (results.next()) {
                StringValues record = new StringValues(name,
                        results.getString("string_value"),
                        results.getTimestamp("create_time"));
                records.add(record);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);

        }
        return records;
    }

    public static String last(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        String value = null;
        try (Connection conn = DerbyBase.getConnection();
                Statement statement = conn.createStatement()) {
            conn.setReadOnly(true);
            statement.setMaxRows(1);
            String sql = " SELECT * FROM String_Values WHERE key_name='"
                    + stringValue(name) + "' ORDER BY create_time DESC";
            try (ResultSet results = statement.executeQuery(sql)) {
                if (results.next()) {
                    value = results.getString("string_value");
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);

        }
        return value;
    }

    public static List<String> max(String name, int max) {
        List<String> records = new ArrayList<>();
        if (name == null || name.trim().isEmpty() || max < 0) {
            return records;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return max(conn, name, max);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return records;
    }

    public static List<String> max(Connection conn, String name, int max) {
        List<String> records = new ArrayList<>();
        if (conn == null || name == null || name.trim().isEmpty() || max < 0) {
            return records;
        }
        try (Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM String_Values WHERE key_name='"
                    + stringValue(name) + "' ORDER BY create_time DESC";
            List<String> invalid = new ArrayList<>();
            try (ResultSet results = statement.executeQuery(sql)) {
                while (results.next()) {
                    String value = results.getString("string_value");
                    if (records.size() >= max) {
                        invalid.add(value);
                    } else {
                        records.add(value);
                    }
                }
            }
            for (String v : invalid) {
                sql = "DELETE FROM String_Values WHERE key_name='" + stringValue(name)
                        + "' AND string_value='" + stringValue(v) + "'";
                statement.executeUpdate(sql);
            }
            conn.commit();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return records;
    }

    public static boolean add(String name, String value) {
        if (name == null || name.isBlank()
                || value == null || value.isBlank()) {
            return false;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return add(conn, name, value);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean add(Connection conn, String name, String value) {
        if (name == null || name.isBlank()
                || value == null || value.isBlank()) {
            return false;
        }
        try (Statement statement = conn.createStatement()) {
            boolean existed = false;
            String sql = " SELECT * FROM String_Values WHERE "
                    + "key_name='" + stringValue(name) + "' AND string_value='" + stringValue(value) + "'";
            try (ResultSet results = statement.executeQuery(sql)) {
                if (results.next()) {
                    existed = true;
                }
            }
            if (existed) {
                sql = "UPDATE String_Values SET  create_time='"
                        + DateTools.datetimeToString(new Date()) + "' WHERE "
                        + "key_name='" + stringValue(name) + "' AND string_value='" + stringValue(value) + "'";
            } else {
                sql = "INSERT INTO String_Values(key_name, string_value , create_time) VALUES('"
                        + stringValue(name) + "', '" + stringValue(value) + "', '"
                        + DateTools.datetimeToString(new Date()) + "')";
            }
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean add(String name, List<String> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return add(conn, name, values);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean add(Connection conn, String name, List<String> values) {
        if (conn == null || values == null || values.isEmpty()) {
            return false;
        }
        try {
            conn.setAutoCommit(false);
            for (int i = 0; i < values.size(); i++) {
                add(conn, name, values.get(i));
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean add(Map<String, String> nameValues) {
        if (nameValues == null || nameValues.isEmpty()) {
            return false;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            conn.setAutoCommit(false);
            for (String name : nameValues.keySet()) {
                String value = nameValues.get(name);
                add(conn, name, value);
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);

            return false;
        }
    }

    public static boolean delete(String name, String value) {
        if (name == null || name.trim().isEmpty()
                || value == null || value.trim().isEmpty()) {
            return false;
        }
        try (Connection conn = DerbyBase.getConnection();
                Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM String_Values WHERE key_name='" + stringValue(name)
                    + "' AND string_value='" + stringValue(value) + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);

            return false;
        }
    }

    public static boolean clear(String name) {
        try (Connection conn = DerbyBase.getConnection();
                Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM String_Values WHERE key_name='" + stringValue(name) + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
//            MyBoxLog.debug(e);
            return false;
        }
    }

    public static int size(String name) {
        if (name == null || name.trim().isEmpty()) {
            return -1;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return size(conn, name);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return -1;
    }

    public static int size(Connection conn, String name) {
        if (conn == null || name == null || name.trim().isEmpty()) {
            return -1;
        }
        String sql = " SELECT count(*) FROM String_Values WHERE key_name='" + stringValue(name) + "'";
        return DerbyBase.size(conn, sql);
    }

}
