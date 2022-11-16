package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.StringValue;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.ConfigTools;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2018-10-15 9:31:28
 * @License Apache License Version 2.0
 */
public class TableUserConf extends BaseTable<StringValue> {

    public TableUserConf() {
        tableName = "User_Conf";
        defineColumns();
    }

    public TableUserConf(boolean defineColumns) {
        tableName = "User_Conf";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableUserConf defineColumns() {
        addColumn(new ColumnDefinition("key_name", ColumnDefinition.ColumnType.String, true, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("string_value", ColumnDefinition.ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("int_Value", ColumnDefinition.ColumnType.Integer));
        return this;
    }

    final static String QueryString = "SELECT string_Value FROM User_Conf WHERE key_Name=?  FETCH FIRST ROW ONLY";
    final static String QueryInt = " SELECT int_Value FROM User_Conf WHERE key_Name=?  FETCH FIRST ROW ONLY";
    final static String InsertInt = "INSERT INTO User_Conf (key_Name, int_Value) VALUES(?, ? )";
    final static String InsertString = "INSERT INTO User_Conf(key_Name, string_Value) VALUES(?, ? )";
    final static String UpdateString = "UPDATE User_Conf SET string_Value=? WHERE key_Name=?";
    final static String UpdateInt = "UPDATE User_Conf SET int_Value=? WHERE key_Name=?";
    final static String Delete = "DELETE FROM User_Conf WHERE key_Name=?";
    final static String DeleteLike = "DELETE FROM User_Conf WHERE key_Name like ?";

    public boolean init(Connection conn) {
        try {
            if (conn == null) {
                return false;
            }
            conn.prepareStatement(createTableStatement()).executeUpdate();
            Map<String, String> values = ConfigTools.readValues();
            if (values == null || values.isEmpty()) {
                return false;
            }
            try ( PreparedStatement intStatement = conn.prepareStatement(InsertInt);
                     PreparedStatement stringStatement = conn.prepareStatement(InsertString)) {
                for (String key : values.keySet()) {
                    String value = values.get(key);
                    switch (value.toLowerCase()) {
                        case "true":
                            intStatement.setString(1, key);
                            intStatement.setInt(2, 1);
                            intStatement.executeUpdate();
                            break;
                        case "false":
                            intStatement.setString(1, key);
                            intStatement.setInt(2, 0);
                            intStatement.executeUpdate();
                            break;
                        default: {
                            try {
                                int v = Integer.valueOf(value);
                                intStatement.setString(1, key);
                                intStatement.setInt(2, v);
                                intStatement.executeUpdate();
                            } catch (Exception e) {
                                stringStatement.setString(1, key);
                                stringStatement.setString(2, value);
                                stringStatement.executeUpdate();
                            }
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return false;
        }
    }

    public static String readString(String keyName, String defaultValue) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return readString(conn, keyName, defaultValue);
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return defaultValue;
        }
    }

    public static String readString(Connection conn, String keyName, String defaultValue) {
        if (conn == null || keyName == null) {
            return defaultValue;
        }
        String value = defaultValue;
        try ( PreparedStatement queryStatement = conn.prepareStatement(QueryString)) {
            queryStatement.setString(1, keyName);
            conn.setAutoCommit(true);
            try ( ResultSet resultSet = queryStatement.executeQuery()) {
                if (resultSet.next()) {
                    value = resultSet.getString(1);
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            if (value == null) {
                delete(conn, keyName);
                if (defaultValue != null) {
                    try ( PreparedStatement insert = conn.prepareStatement(InsertString)) {
                        insert.setString(1, keyName);
                        insert.setString(2, defaultValue);
                        insert.executeUpdate();
                    } catch (Exception e) {
                        MyBoxLog.debug(e);
                    }
                    value = defaultValue;
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return value;
    }

    public static String readString(Connection conn, String keyName) {
        return readString(conn, keyName, null);
    }

    public static int readInt(String keyName, int defaultValue) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return readInt(conn, keyName, defaultValue);
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return defaultValue;
        }
    }

    public static int readInt(Connection conn, String keyName) {
        return readInt(conn, keyName, AppValues.InvalidInteger);
    }

    public static int readInt(Connection conn, String keyName, int defaultValue) {
        if (conn == null || keyName == null) {
            return defaultValue;
        }
        int value = defaultValue;
        try ( PreparedStatement queryStatement = conn.prepareStatement(QueryInt)) {
            queryStatement.setString(1, keyName);
            conn.setAutoCommit(true);
            try ( ResultSet resultSet = queryStatement.executeQuery()) {
                if (resultSet != null && resultSet.next()) {
                    value = resultSet.getInt(1);
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        if (value == AppValues.InvalidInteger) {
            delete(conn, keyName);
            if (defaultValue != AppValues.InvalidInteger) {
                try ( PreparedStatement insert = conn.prepareStatement(InsertString)) {
                    insert.setString(1, keyName);
                    insert.setInt(2, defaultValue);
                    insert.executeUpdate();
                } catch (Exception e) {
                    MyBoxLog.debug(e);
                }
                value = defaultValue;
            }
        }
        return value;
    }

    public static boolean readBoolean(Connection conn, String keyName, boolean defaultValue) {
        int v = readInt(conn, keyName, defaultValue ? 1 : 0);
        return v > 0;
    }

    public static boolean readBoolean(String keyName, boolean defaultValue) {
        int v = readInt(keyName, defaultValue ? 1 : 0);
        return v > 0;
    }

    public static int writeString(String keyName, String stringValue) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return writeString(conn, keyName, stringValue);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return 0;
        }
    }

    public static int writeString(Connection conn, String keyName, String stringValue) {
        if (keyName == null) {
            return 0;
        }
        try {
            if (stringValue == null) {
                return delete(conn, keyName) ? 1 : 0;
            }
            String exist = readString(conn, keyName);
            if (exist != null) {
                if (!stringValue.equals(exist)) {
                    try ( PreparedStatement statement = conn.prepareStatement(UpdateString)) {
                        statement.setString(1, DerbyBase.stringValue(stringValue));
                        statement.setString(2, DerbyBase.stringValue(keyName));
                        return statement.executeUpdate();
                    }
                } else {
                    return 1;
                }
            } else {
                try ( PreparedStatement statement = conn.prepareStatement(InsertString)) {
                    statement.setString(1, DerbyBase.stringValue(keyName));
                    statement.setString(2, DerbyBase.stringValue(stringValue));
                    return statement.executeUpdate();
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return -1;
        }
    }

    public static int writeInt(String keyName, int intValue) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return writeInt(conn, keyName, intValue);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public static int writeInt(Connection conn, String keyName, int intValue) {
        try {
            int exist = readInt(conn, keyName);
            if (exist != AppValues.InvalidInteger) {
                if (intValue != exist) {
                    try ( PreparedStatement statement = conn.prepareStatement(UpdateInt)) {
                        statement.setInt(1, intValue);
                        statement.setString(2, keyName);
                        return statement.executeUpdate();
                    }
                } else {
                    return 1;
                }
            } else {
                try ( PreparedStatement statement = conn.prepareStatement(InsertInt)) {
                    statement.setString(1, keyName);
                    statement.setInt(2, intValue);
                    return statement.executeUpdate();
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return -1;
        }
    }

    public static int writeBoolean(String keyName, boolean booleanValue) {
        return writeInt(keyName, booleanValue ? 1 : 0);
    }

    public static int writeBoolean(Connection conn, String keyName, boolean booleanValue) {
        return writeInt(conn, keyName, booleanValue ? 1 : 0);
    }

    public static boolean delete(String keyName) {
        if (keyName == null || keyName.isEmpty()) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return delete(conn, keyName);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public static boolean delete(Connection conn, String keyName) {
        if (keyName == null || keyName.isEmpty()) {
            return false;
        }
        try ( PreparedStatement statement = conn.prepareStatement(Delete)) {
            statement.setString(1, keyName);
            return statement.executeUpdate() >= 0;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public static boolean deletePrefix(String keyName) {
        if (keyName == null || keyName.isEmpty()) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return deletePrefix(conn, keyName);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public static boolean deletePrefix(Connection conn, String keyName) {
        if (keyName == null || keyName.isEmpty()) {
            return false;
        }
        try ( PreparedStatement statement = conn.prepareStatement(DeleteLike)) {
            statement.setString(1, keyName + "%");
            return statement.executeUpdate() >= 0;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

}
