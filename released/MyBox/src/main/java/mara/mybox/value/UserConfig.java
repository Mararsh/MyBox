package mara.mybox.value;

import java.io.File;
import java.sql.Connection;
import javafx.scene.paint.Color;
import mara.mybox.db.table.TableUserConf;
import static mara.mybox.value.AppVariables.UserConfigValues;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class UserConfig {

    public static boolean setString(String key, String value) {
        UserConfigValues.put(key, value);
        if (TableUserConf.writeString(key, value) >= 0) {
            return true;
        } else {
//            MyBoxLog.console(key + " " + value);
            return false;
        }
    }

    public static boolean setString(Connection conn, String key, String value) {
        UserConfigValues.put(key, value);
        return TableUserConf.writeString(conn, key, value) >= 0;
    }

    public static String getString(String key, String defaultValue) {
        try {
            String value;
            if (UserConfigValues.containsKey(key)) {
                value = UserConfigValues.get(key);
            } else {
                value = TableUserConf.readString(key, defaultValue);
                if (value == null) {
                    return defaultValue;
                }
                UserConfigValues.put(key, value);
            }
            return value;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return defaultValue;
        }
    }

    public static String getString(Connection conn, String key, String defaultValue) {
        try {
            if (conn == null) {
                return getString(key, defaultValue);
            }
            String value;
            if (UserConfigValues.containsKey(key)) {
                value = UserConfigValues.get(key);
            } else {
                value = TableUserConf.readString(conn, key, defaultValue);
                if (value == null) {
                    return defaultValue;
                }
                UserConfigValues.put(key, value);
            }
            return value;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean setInt(String key, int value) {
        UserConfigValues.put(key, value + "");
        if (TableUserConf.writeInt(key, value) > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean setInt(Connection conn, String key, int value) {
        UserConfigValues.put(key, value + "");
        if (TableUserConf.writeInt(conn, key, value) > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static int getInt(String key, int defaultValue) {
        if (UserConfigValues.containsKey(key)) {
            try {
                int v = Integer.parseInt(UserConfigValues.get(key));
                return v;
            } catch (Exception e) {
                //                MyBoxLog.console(e.toString());
            }
        }
        try {
            int v = TableUserConf.readInt(key, defaultValue);
            if (v == AppValues.InvalidInteger) {
                return defaultValue;
            }
            UserConfigValues.put(key, v + "");
            return v;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return defaultValue;
        }
    }

    public static int getInt(Connection conn, String key, int defaultValue) {
        if (conn == null) {
            return getInt(key, defaultValue);
        }
        if (UserConfigValues.containsKey(key)) {
            try {
                int v = Integer.parseInt(UserConfigValues.get(key));
                return v;
            } catch (Exception e) {
                //                MyBoxLog.console(e.toString());
            }
        }
        try {
            int v = TableUserConf.readInt(conn, key, defaultValue);
            if (v == AppValues.InvalidInteger) {
                return defaultValue;
            }
            UserConfigValues.put(key, v + "");
            return v;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, true);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        if (UserConfigValues.containsKey(key)) {
            try {
                boolean v = UserConfigValues.get(key).equals("true");
                return v;
            } catch (Exception e) {
            }
        }
        try {
            boolean v = TableUserConf.readBoolean(key, defaultValue);
            UserConfigValues.put(key, v ? "true" : "false");
            return v;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean getBoolean(Connection conn, String key, boolean defaultValue) {
        if (conn == null) {
            return getBoolean(key, defaultValue);
        }
        if (UserConfigValues.containsKey(key)) {
            try {
                boolean v = UserConfigValues.get(key).equals("true");
                return v;
            } catch (Exception e) {
            }
        }
        try {
            boolean v = TableUserConf.readBoolean(conn, key, defaultValue);
            UserConfigValues.put(key, v ? "true" : "false");
            return v;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean setBoolean(String key, boolean value) {
        UserConfigValues.put(key, value ? "true" : "false");
        if (TableUserConf.writeBoolean(key, value) > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean setBoolean(Connection conn, String key, boolean value) {
        UserConfigValues.put(key, value ? "true" : "false");
        if (TableUserConf.writeBoolean(conn, key, value) > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean setDouble(String key, double value) {
        UserConfigValues.put(key, value + "");
        if (TableUserConf.writeString(key, value + "") > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean setDouble(Connection conn, String key, double value) {
        UserConfigValues.put(key, value + "");
        if (TableUserConf.writeString(conn, key, value + "") > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static double getDouble(String key, double defaultValue) {
        if (UserConfigValues.containsKey(key)) {
            try {
                double v = Double.parseDouble(UserConfigValues.get(key));
                return v;
            } catch (Exception e) {
                //                MyBoxLog.console(e.toString());
            }
        }
        try {
            String v = TableUserConf.readString(key, defaultValue + "");
            if (v == null) {
                return defaultValue;
            }
            double d = Double.parseDouble(v);
            UserConfigValues.put(key, v);
            return d;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return defaultValue;
        }
    }

    public static double getDouble(Connection conn, String key, double defaultValue) {
        if (conn == null) {
            return getDouble(key, defaultValue);
        }
        if (UserConfigValues.containsKey(key)) {
            try {
                double v = Double.parseDouble(UserConfigValues.get(key));
                return v;
            } catch (Exception e) {
                //                MyBoxLog.console(e.toString());
            }
        }
        try {
            String v = TableUserConf.readString(conn, key, defaultValue + "");
            if (v == null) {
                return defaultValue;
            }
            double d = Double.parseDouble(v);
            UserConfigValues.put(key, v);
            return d;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean setLong(String key, long value) {
        UserConfigValues.put(key, value + "");
        if (TableUserConf.writeString(key, value + "") > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean setLong(Connection conn, String key, long value) {
        UserConfigValues.put(key, value + "");
        if (TableUserConf.writeString(conn, key, value + "") > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static long getLong(String key, long defaultValue) {
        if (UserConfigValues.containsKey(key)) {
            try {
                long v = Long.parseLong(UserConfigValues.get(key));
                return v;
            } catch (Exception e) {
                //                MyBoxLog.console(e.toString());
            }
        }
        try {
            String v = TableUserConf.readString(key, defaultValue + "");
            if (v == null) {
                return defaultValue;
            }
            long d = Long.parseLong(v);
            UserConfigValues.put(key, v);
            return d;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return defaultValue;
        }
    }

    public static long getLong(Connection conn, String key, long defaultValue) {
        if (conn == null) {
            return getLong(key, defaultValue);
        }
        if (UserConfigValues.containsKey(key)) {
            try {
                long v = Long.parseLong(UserConfigValues.get(key));
                return v;
            } catch (Exception e) {
                //                MyBoxLog.console(e.toString());
            }
        }
        try {
            String v = TableUserConf.readString(conn, key, defaultValue + "");
            if (v == null) {
                return defaultValue;
            }
            long d = Long.parseLong(v);
            UserConfigValues.put(key, v);
            return d;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean setFloat(String key, float value) {
        UserConfigValues.put(key, value + "");
        if (TableUserConf.writeString(key, value + "") > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean setFloat(Connection conn, String key, float value) {
        UserConfigValues.put(key, value + "");
        if (TableUserConf.writeString(conn, key, value + "") > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static float getFloat(String key, float defaultValue) {
        if (UserConfigValues.containsKey(key)) {
            try {
                float v = Float.parseFloat(UserConfigValues.get(key));
                return v;
            } catch (Exception e) {
                //                MyBoxLog.console(e.toString());
            }
        }
        try {
            String v = TableUserConf.readString(key, defaultValue + "");
            if (v == null) {
                return defaultValue;
            }
            float d = Float.parseFloat(v);
            UserConfigValues.put(key, v);
            return d;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return defaultValue;
        }
    }

    public static float getFloat(Connection conn, String key, float defaultValue) {
        if (conn == null) {
            return getFloat(key, defaultValue);
        }
        if (UserConfigValues.containsKey(key)) {
            try {
                float v = Float.parseFloat(UserConfigValues.get(key));
                return v;
            } catch (Exception e) {
                //                MyBoxLog.console(e.toString());
            }
        }
        try {
            String v = TableUserConf.readString(conn, key, defaultValue + "");
            if (v == null) {
                return defaultValue;
            }
            float d = Float.parseFloat(v);
            UserConfigValues.put(key, v);
            return d;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean deleteValue(String key) {
        if (TableUserConf.delete(key)) {
            UserConfigValues.remove(key);
            return true;
        } else {
            return false;
        }
    }

    public static File getPath(String key) {
        return getPath(key, AppPaths.getGeneratedPath());
    }

    public static File getPath(String key, String defaultValue) {
        try {
            String pathString;
            if (UserConfigValues.containsKey(key)) {
                pathString = UserConfigValues.get(key);
            } else {
                pathString = TableUserConf.readString(key, defaultValue);
            }
            if (pathString == null) {
                pathString = defaultValue;
            }
            File path = new File(pathString);
            if (!path.exists() || !path.isDirectory()) {
                deleteValue(key);
                path = new File(AppPaths.getGeneratedPath());
                if (!path.exists()) {
                    path.mkdirs();
                }
            }
            UserConfigValues.put(key, path.getAbsolutePath());
            return path;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static String infoColor() {
        String v = getString("PopInfoColor", "white");
        return v != null && v.startsWith("#") ? v : "white";
    }

    public static String textSize() {
        return getString("PopTextSize", "1.5") + "em";
    }

    public static String textBgColor() {
        String v = getString("PopTextBgColor", "black");
        return v != null && v.startsWith("#") ? v : "black";
    }

    public static Color alphaColor() {
        String color = getString("AlphaAsColor", Color.WHITE.toString());
        Color c = Color.web(color);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), 1d);
    }

    public static String errorColor() {
        String v = getString("PopErrorColor", "aqua");
        return v != null && v.startsWith("#") ? v : "aqua";
    }

    public static String warnColor() {
        String v = getString("PopWarnColor", "orange");
        return v != null && v.startsWith("#") ? v : "orange";
    }

    public static int textDuration() {
        return getInt("PopTextDuration", 3000);
    }

    public static String getStyle() {
        return UserConfig.getString("InterfaceStyle", AppValues.MyBoxStyle);
    }

    public static String badStyle() {
        String c = errorColor();
        return "-fx-text-box-border: " + c + ";   -fx-text-fill: " + c + ";";
    }

    public static String warnStyle() {
        String c = warnColor();
        return "-fx-text-box-border: " + c + ";   -fx-text-fill: " + c + ";";
    }

    public static int imageScale() {
        return UserConfig.getInt("ImageDecimal", 3);
    }

    public static int selectorScrollSize() {
        int size = getInt("SelectorScrollSize", 100);
        if (size <= 0) {
            size = 100;
        }
        return size;
    }

    public static boolean setSceneFontSize(int size) {
        AppVariables.sceneFontSize = size;
        if (UserConfig.setInt("SceneFontSize", size)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean setIconSize(int size) {
        AppVariables.iconSize = size;
        if (UserConfig.setInt("IconSize", size)) {
            return true;
        } else {
            return false;
        }
    }

    public static void clear() {
        new TableUserConf().clearData();
        AppVariables.initAppVaribles();
    }

}
