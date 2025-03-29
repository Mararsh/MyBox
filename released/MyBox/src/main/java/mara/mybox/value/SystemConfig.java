package mara.mybox.value;

import java.sql.Connection;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.table.TableSystemConf;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class SystemConfig {

    public static boolean setString(String key, String value) {
        try (Connection conn = DerbyBase.getConnection()) {
            return setString(conn, key, value);
        } catch (Exception e) {
//            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean setString(Connection conn, String key, String value) {
        AppVariables.SystemConfigValues.put(key, value);
        if (TableSystemConf.writeString(conn, key, value) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static String getString(String key, String defaultValue) {
        try {
            //            MyBoxLog.debug("getSystemConfigString:" + key);
            String value;
            if (AppVariables.SystemConfigValues.containsKey(key)) {
                value = AppVariables.SystemConfigValues.get(key);
            } else {
                value = TableSystemConf.readString(key, defaultValue);
                AppVariables.SystemConfigValues.put(key, value);
            }
            return value;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static boolean setInt(String key, int value) {
        AppVariables.SystemConfigValues.put(key, value + "");
        if (TableSystemConf.writeInt(key, value) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static int getInt(String key, int defaultValue) {
        try {
            int v;
            if (AppVariables.SystemConfigValues.containsKey(key)) {
                v = Integer.parseInt(AppVariables.SystemConfigValues.get(key));
            } else {
                v = TableSystemConf.readInt(key, defaultValue);
                AppVariables.SystemConfigValues.put(key, v + "");
            }
            return v;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean setBoolean(String key, boolean value) {
        try (Connection conn = DerbyBase.getConnection()) {
            return setBoolean(conn, key, value);
        } catch (Exception e) {
//            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean setBoolean(Connection conn, String key, boolean value) {
        AppVariables.SystemConfigValues.put(key, value ? "true" : "false");
        if (TableSystemConf.writeBoolean(conn, key, value) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        try {
            boolean v;
            if (AppVariables.SystemConfigValues.containsKey(key)) {
                v = AppVariables.SystemConfigValues.get(key).equals("true");
            } else {
                v = TableSystemConf.readBoolean(key, defaultValue);
                AppVariables.SystemConfigValues.put(key, v ? "true" : "false");
            }
            return v;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, true);
    }

    public static boolean deleteValue(String key) {
        if (TableSystemConf.delete(key)) {
            AppVariables.SystemConfigValues.remove(key);
            return true;
        } else {
            return false;
        }
    }

}
