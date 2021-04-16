package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Map;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.ConfigTools;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-10-15 9:31:28
 * @License Apache License Version 2.0
 */
public class TableSystemConf extends DerbyBase {

    final static String QueryString = "SELECT string_Value FROM System_Conf WHERE key_Name=?";
    final static String QueryInt = " SELECT int_Value FROM System_Conf WHERE key_Name=?";
    final static String InsertInt = "INSERT INTO System_Conf (key_Name, int_Value) VALUES(?, ? )";
    final static String InsertString = "INSERT INTO System_Conf(key_Name, string_Value) VALUES(?, ? )";
    final static String UpdateString = "UPDATE System_Conf SET string_Value=? WHERE key_Name=?";
    final static String UpdateInt = "UPDATE System_Conf SET int_Value=? WHERE key_Name=?";
    final static String Delete = "DELETE FROM System_Conf WHERE key_Name=?";
    final static String DeleteLike = "DELETE FROM System_Conf WHERE key_Name like ?";

    public TableSystemConf() {
        Table_Name = "System_Conf";
        Keys = new ArrayList<>() {
            {
                add("key_Name");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE System_Conf ( "
                + "  key_Name  VARCHAR(1024) NOT NULL PRIMARY KEY, "
                + "  int_Value INTEGER, "
                + "  default_int_Value INTEGER, "
                + "  string_Value VARCHAR(32672), "
                + "  default_string_Value VARCHAR(32672) "
                + " )";
    }

    @Override
    public boolean init(Connection conn) {
        try {
            if (conn == null) {
                return false;
            }
            conn.createStatement().executeUpdate(Create_Table_Statement);
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
            MyBoxLog.error(e);

            return false;
        }
    }

    public static String readString(String keyName, String defaultValue) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return readString(conn, keyName, defaultValue);
        } catch (Exception e) {
            MyBoxLog.error(e);

            return defaultValue;
        }
    }

    public static String readString(Connection conn, String keyName, String defaultValue) {
        if (conn == null || keyName == null) {
            return null;
        }
        try ( PreparedStatement queryStatement = conn.prepareStatement(QueryString)) {
            queryStatement.setMaxRows(1);
            queryStatement.setString(1, keyName);
            try ( ResultSet resultSet = queryStatement.executeQuery()) {
                if (resultSet.next()) {
                    String value = resultSet.getString(1);
                    if (value == null) {
                        delete(conn, keyName);
                    } else {
                        return value;
                    }
                }
            }
            if (defaultValue != null) {
                try ( PreparedStatement insert = conn.prepareStatement(InsertString)) {
                    insert.setString(1, keyName);
                    insert.setString(2, defaultValue);
                    insert.executeUpdate();
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);

        }
        return defaultValue;
    }

    public static String readString(Connection conn, String keyName) {
        return readString(conn, keyName, null);
    }

    public static int readInt(String keyName, int defaultValue) {
        try ( Connection conn = DerbyBase.getConnection()) {
            int exist = readInt(conn, keyName);
            if (exist != CommonValues.InvalidInteger) {
                return exist;
            } else {
                try ( PreparedStatement insert = conn.prepareStatement(UpdateInt)) {
                    insert.setInt(1, defaultValue);
                    insert.setString(2, keyName);
                    insert.executeUpdate();
                }
            }
        } catch (Exception e) {
//            MyBoxLog.error(e);
        }
        return defaultValue;
    }

    public static int readInt(Connection conn, String keyName) {
        int value = CommonValues.InvalidInteger;
        try ( PreparedStatement queryStatement = conn.prepareStatement(QueryInt)) {
            queryStatement.setMaxRows(1);
            queryStatement.setString(1, keyName);
            try ( ResultSet resultSet = queryStatement.executeQuery()) {
                if (resultSet.next()) {
                    value = resultSet.getInt(1);
                }
            }
        } catch (Exception e) {
//            MyBoxLog.error(e);
        }
        return value;
    }

    public static boolean readBoolean(String keyName, boolean defaultValue) {
        int v = readInt(keyName, defaultValue ? 1 : 0);
        return v > 0;
    }

    public static int writeString(String keyName, String stringValue) {
        if (keyName == null) {
            return 0;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            if (stringValue == null) {
                return delete(conn, keyName) ? 1 : 0;
            }
            String exist = readString(conn, keyName);
            if (exist != null) {
                if (!stringValue.equals(exist)) {
                    try ( PreparedStatement statement = conn.prepareStatement(UpdateString)) {
                        statement.setString(1, stringValue);
                        statement.setString(2, keyName);
                        return statement.executeUpdate();
                    }
                } else {
                    return 0;
                }
            } else {
                try ( PreparedStatement statement = conn.prepareStatement(InsertString)) {
                    statement.setString(1, keyName);
                    statement.setString(2, stringValue);
                    return statement.executeUpdate();
                }
            }
        } catch (Exception e) {
//            MyBoxLog.error(e);
            return -1;
        }
    }

    public static int writeInt(String keyName, int intValue) {
        try ( Connection conn = DerbyBase.getConnection()) {
            int exist = readInt(conn, keyName);
            if (exist != CommonValues.InvalidInteger) {
                if (intValue != exist) {
                    try ( PreparedStatement statement = conn.prepareStatement(UpdateInt)) {
                        statement.setInt(1, intValue);
                        statement.setString(2, keyName);
                        return statement.executeUpdate();
                    }
                } else {
                    return 0;
                }
            } else {
                try ( PreparedStatement statement = conn.prepareStatement(InsertInt)) {
                    statement.setString(1, keyName);
                    statement.setInt(2, intValue);
                    return statement.executeUpdate();
                }
            }
        } catch (Exception e) {
//            MyBoxLog.error(e);
            return -1;
        }
    }

    public static int writeBoolean(String keyName, boolean booleanValue) {
        return writeInt(keyName, booleanValue ? 1 : 0);
    }

    public static boolean delete(String keyName) {
        if (keyName == null || keyName.isEmpty()) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return delete(conn, keyName);
        } catch (Exception e) {
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
            return false;
        }
    }

}
