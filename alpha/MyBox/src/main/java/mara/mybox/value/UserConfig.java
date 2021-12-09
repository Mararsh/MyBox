package mara.mybox.value;

import java.io.File;
import java.sql.Connection;
import javafx.scene.paint.Color;
import mara.mybox.db.table.TableUserConf;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.userConfigValues;
import org.apache.pdfbox.io.MemoryUsageSetting;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class UserConfig {

    public static boolean setString(String key, String value) {
        userConfigValues.put(key, value);
        if (TableUserConf.writeString(key, value) >= 0) {
            return true;
        } else {
            MyBoxLog.console(key + " " + value);
            return false;
        }
    }

    public static boolean setString(Connection conn, String key, String value) {
        userConfigValues.put(key, value);
        if (TableUserConf.writeString(conn, key, value) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static String getString(String key, String defaultValue) {
        try {
            String value;
            if (userConfigValues.containsKey(key)) {
                value = userConfigValues.get(key);
            } else {
                value = TableUserConf.readString(key, defaultValue);
                userConfigValues.put(key, value);
            }
            return value;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean setInt(String key, int value) {
        userConfigValues.put(key, value + "");
        if (TableUserConf.writeInt(key, value) > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static int getInt(String key, int defaultValue) {
        if (userConfigValues.containsKey(key)) {
            try {
                int v = Integer.valueOf(userConfigValues.get(key));
                return v;
            } catch (Exception e) {
                //                MyBoxLog.console(e.toString());
            }
        }
        try {
            int v = TableUserConf.readInt(key, defaultValue);
            userConfigValues.put(key, v + "");
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
        if (userConfigValues.containsKey(key)) {
            try {
                boolean v = userConfigValues.get(key).equals("true");
                return v;
            } catch (Exception e) {
            }
        }
        try {
            boolean v = TableUserConf.readBoolean(key, defaultValue);
            userConfigValues.put(key, v ? "true" : "false");
            return v;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean setBoolean(String key, boolean value) {
        userConfigValues.put(key, value ? "true" : "false");
        if (TableUserConf.writeBoolean(key, value) > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean deleteValue(String key) {
        if (TableUserConf.delete(key)) {
            userConfigValues.remove(key);
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
            if (userConfigValues.containsKey(key)) {
                pathString = userConfigValues.get(key);
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
            userConfigValues.put(key, path.getAbsolutePath());
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
        return Color.web(color);
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

    public static MemoryUsageSetting getPdfMem() {
        return setPdfMem(UserConfig.getString("PdfMemDefault", "1GB"));
    }

    public static boolean setRestoreStagesSize(boolean value) {
        if (UserConfig.setBoolean("RestoreStagesSize", value)) {
            AppVariables.restoreStagesSize = value;
            return true;
        } else {
            return false;
        }
    }

    public static boolean setSceneFontSize(int size) {
        AppVariables.sceneFontSize = size;
        if (UserConfig.setInt("SceneFontSize", size)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean setOpenStageInNewWindow(boolean value) {
        if (UserConfig.setBoolean("OpenStageInNewWindow", value)) {
            AppVariables.openStageInNewWindow = value;
            return true;
        } else {
            return false;
        }
    }

    public static MemoryUsageSetting setPdfMem(String value) {
        switch (value) {
            case "1GB":
                UserConfig.setString("PdfMemDefault", "1GB");
                AppVariables.pdfMemUsage = MemoryUsageSetting.setupMixed(1024 * 1024 * 1024L, -1);
                break;
            case "2GB":
                UserConfig.setString("PdfMemDefault", "2GB");
                AppVariables.pdfMemUsage = MemoryUsageSetting.setupMixed(2048 * 1024 * 1024L, -1);
                break;
            case "Unlimit":
                UserConfig.setString("PdfMemDefault", "Unlimit");
                AppVariables.pdfMemUsage = MemoryUsageSetting.setupMixed(-1, -1);
                break;
            case "500MB":
            default:
                UserConfig.setString("PdfMemDefault", "500MB");
                AppVariables.pdfMemUsage = MemoryUsageSetting.setupMixed(500 * 1024 * 1024L, -1);
        }
        return AppVariables.pdfMemUsage;
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
        new TableUserConf().clear();
        AppVariables.initAppVaribles();
    }

}
