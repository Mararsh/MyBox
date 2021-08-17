package mara.mybox.value;

import mara.mybox.db.table.TableSystemConf;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class SystemConfig {

    public static boolean setString(String key, String value) {
        AppVariables.systemConfigValues.put(key, value);
        if (TableSystemConf.writeString(key, value) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static String getString(String key, String defaultValue) {
        try {
            //            MyBoxLog.debug("getSystemConfigString:" + key);
            String value;
            if (AppVariables.systemConfigValues.containsKey(key)) {
                value = AppVariables.systemConfigValues.get(key);
            } else {
                value = TableSystemConf.readString(key, defaultValue);
                AppVariables.systemConfigValues.put(key, value);
            }
            return value;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static boolean setInt(String key, int value) {
        AppVariables.systemConfigValues.put(key, value + "");
        if (TableSystemConf.writeInt(key, value) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static int getInt(String key, int defaultValue) {
        try {
            int v;
            if (AppVariables.systemConfigValues.containsKey(key)) {
                v = Integer.valueOf(AppVariables.systemConfigValues.get(key));
            } else {
                v = TableSystemConf.readInt(key, defaultValue);
                AppVariables.systemConfigValues.put(key, v + "");
            }
            return v;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean setBoolean(String key, boolean value) {
        AppVariables.systemConfigValues.put(key, value ? "true" : "false");
        if (TableSystemConf.writeBoolean(key, value) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        try {
            boolean v;
            if (AppVariables.systemConfigValues.containsKey(key)) {
                v = AppVariables.systemConfigValues.get(key).equals("true");
            } else {
                v = TableSystemConf.readBoolean(key, defaultValue);
                AppVariables.systemConfigValues.put(key, v ? "true" : "false");
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
            AppVariables.systemConfigValues.remove(key);
            return true;
        } else {
            return false;
        }
    }

}
