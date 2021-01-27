package mara.mybox.value;

import java.awt.Toolkit;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import javafx.scene.paint.Color;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import mara.mybox.controller.AlarmClockController;
import mara.mybox.data.UserLanguage;
import mara.mybox.data.UserTableLanguage;
import mara.mybox.db.table.TableSystemConf;
import mara.mybox.db.table.TableUserConf;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControlStyle;
import org.apache.pdfbox.io.MemoryUsageSetting;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:59:49
 * @Description
 * @License Apache License Version 2.0
 */
public class AppVariables {

    public static String[] appArgs;
    public static File MyboxConfigFile, MyBoxLogsPath;
    public static String MyboxDataPath, AlarmClocksFile;
    public static File MyBoxTempPath, MyBoxDerbyPath, MyBoxLanguagesPath, MyBoxDownloadsPath;
    public static List<File> MyBoxReservePaths;
    public static ResourceBundle currentBundle, currentTableBundle;
    public static Map<String, String> userConfigValues;
    public static Map<String, String> systemConfigValues;
    public static ScheduledExecutorService executorService;
    public static Map<Long, ScheduledFuture<?>> scheduledTasks;
    public static AlarmClockController alarmClockController;
    public static MemoryUsageSetting pdfMemUsage;
    public static int sceneFontSize, fileRecentNumber, iconSize, thumbnailWidth;
    public static boolean openStageInNewWindow, restoreStagesSize, controlDisplayText,
            disableHiDPI, devMode, hidpiIcons, ignoreDbUnavailable, popErrorLogs;
    public static ControlStyle.ColorStyle ControlColor;
    public static SSLSocketFactory defaultSSLSocketFactory;
    public static HostnameVerifier defaultHostnameVerifier;

    public AppVariables() {
    }

    public static void initAppVaribles() {
        try {
            userConfigValues = new HashMap<>();
            systemConfigValues = new HashMap<>();
            getBundle();
            getTableBundle();
            getPdfMem();
            devMode = getUserConfigBoolean("DevMode", false);
            openStageInNewWindow = getUserConfigBoolean("OpenStageInNewWindow", false);
            restoreStagesSize = getUserConfigBoolean("RestoreStagesSize", true);
            sceneFontSize = getUserConfigInt("SceneFontSize", 15);
            fileRecentNumber = getUserConfigInt("FileRecentNumber", 16);
            iconSize = getUserConfigInt("IconSize", 20);
            thumbnailWidth = getUserConfigInt("ThumbnailWidth", 100);
            ControlColor = ControlStyle.getConfigColorStyle();
            controlDisplayText = getUserConfigBoolean("ControlDisplayText", false);
            hidpiIcons = getUserConfigBoolean("HidpiIcons", Toolkit.getDefaultToolkit().getScreenResolution() > 120);
            devMode = getUserConfigBoolean("DevMode", false);
            disableHiDPI = ignoreDbUnavailable = false;
            popErrorLogs = true;
            if (defaultSSLSocketFactory == null) {
                defaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
                defaultHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public static void clear() {
        new TableUserConf().clear();
        initAppVaribles();
    }

    public static String getLanguage() {
        String lang = getUserConfigValue("language", Locale.getDefault().getLanguage());
        return lang != null ? lang.toLowerCase() : Locale.getDefault().getLanguage().toLowerCase();
    }

    public static boolean isChinese() {
        return getLanguage().startsWith("zh");
    }

    public static void setLanguage(String lang) {
        if (lang == null) {
            lang = Locale.getDefault().getLanguage().toLowerCase();
        }
        setUserConfigValue("language", lang);
        currentBundle = getBundle(lang);
        currentTableBundle = getTableBundle(lang);
    }

    public static ResourceBundle getBundle() {
        if (currentBundle == null) {
            currentBundle = getBundle(getLanguage());
        }
        return currentBundle;
    }

    public static ResourceBundle getBundle(String language) {
        String lang = language;
        if (lang == null) {
            lang = Locale.getDefault().getLanguage().toLowerCase();
        }
        ResourceBundle bundle = null;
        if (lang.startsWith("zh")) {
            bundle = CommonValues.BundleZhCN;
        } else if (lang.startsWith("en")) {
            bundle = CommonValues.BundleEn;
        } else {
            try {
                bundle = new UserLanguage(lang);
            } catch (Exception e) {
            }
            if (bundle == null) {
                bundle = CommonValues.BundleEn;
            }
        }
        return bundle;
    }

    public static String message(String language, String msg) {
        try {
            if (msg.isBlank()) {
                return msg;
            }
            ResourceBundle bundle = getBundle(language);
            return bundle.getString(msg);
        } catch (Exception e) {
            return msg;
        }
    }

    public static String message(String msg) {
        try {
            return getBundle().getString(msg);
        } catch (Exception e) {
            return msg;
        }
    }

    public static ResourceBundle getTableBundle() {
        if (currentTableBundle == null) {
            currentTableBundle = getTableBundle(getLanguage());
        }
        return currentTableBundle;
    }

    public static ResourceBundle getTableBundle(String language) {
        String lang = language;
        if (lang == null) {
            lang = Locale.getDefault().getLanguage().toLowerCase();
        }
        ResourceBundle bundle = null;
        if (lang.startsWith("zh")) {
            bundle = CommonValues.TableBundleZhCN;
        } else if (lang.startsWith("en")) {
            bundle = CommonValues.TableBundleEn;
        } else {
            try {
                bundle = new UserTableLanguage(lang);
            } catch (Exception e) {
            }
            if (bundle == null) {
                bundle = CommonValues.TableBundleEn;
            }
        }
        return bundle;
    }

    public static String tableMessage(String language, String msg) {
        try {
            String s = msg.toLowerCase();
            ResourceBundle bundle = getTableBundle(language);
            return bundle.getString(s);
        } catch (Exception e) {
            return msg;
        }
    }

    public static String tableMessage(String msg) {
        try {
            return getTableBundle().getString(msg);
        } catch (Exception e) {
            return msg;
        }
    }

    public static TimeZone getTimeZone() {
        return TimeZone.getDefault();
//        String lang = getLanguage();
//        switch (lang.toLowerCase()) {
//            case "zh":
//                return CommonValues.zoneZhCN;
//            case "en":
//            default:
//                return CommonValues.zoneUTC;
//        }
    }

    public static boolean setOpenStageInNewWindow(boolean value) {
        if (setUserConfigValue("OpenStageInNewWindow", value)) {
            openStageInNewWindow = value;
            return true;
        } else {
            return false;
        }
    }

    public static boolean setRestoreStagesSize(boolean value) {
        if (setUserConfigValue("RestoreStagesSize", value)) {
            restoreStagesSize = value;
            return true;
        } else {
            return false;
        }
    }

    public static MemoryUsageSetting setPdfMem(String value) {
        switch (value) {
            case "1GB":
                setUserConfigValue("PdfMemDefault", "1GB");
                pdfMemUsage = MemoryUsageSetting.setupMixed(1024 * 1024 * 1024L, -1);
                break;
            case "2GB":
                setUserConfigValue("PdfMemDefault", "2GB");
                pdfMemUsage = MemoryUsageSetting.setupMixed(2048 * 1024 * 1024L, -1);
                break;
            case "Unlimit":
                setUserConfigValue("PdfMemDefault", "Unlimit");
                pdfMemUsage = MemoryUsageSetting.setupMixed(-1, -1);
                break;
            case "500MB":
            default:
                setUserConfigValue("PdfMemDefault", "500MB");
                pdfMemUsage = MemoryUsageSetting.setupMixed(500 * 1024 * 1024L, -1);
        }
        return pdfMemUsage;
    }

    public static MemoryUsageSetting getPdfMem() {
        return setPdfMem(getUserConfigValue("PdfMemDefault", "1GB"));
    }

    public static boolean setSceneFontSize(int size) {
        sceneFontSize = size;
        if (setUserConfigInt("SceneFontSize", size)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean setIconSize(int size) {
        iconSize = size;
        if (setUserConfigInt("IconSize", size)) {
            return true;
        } else {
            return false;
        }
    }

    public static String getStyle() {
        return getUserConfigValue("InterfaceStyle", CommonValues.MyBoxStyle);
    }

    public static String getImageHisPath() {
        String imageHistoriesPath = MyboxDataPath + File.separator + "imageHistories";
        File path = new File(imageHistoriesPath);
        if (!path.exists()) {
            path.mkdirs();
        }
        return imageHistoriesPath;
    }

    public static String getImageScopePath() {
        String imageScopesPath = MyboxDataPath + File.separator + "imageScopes";
        File path = new File(imageScopesPath);
        if (!path.exists()) {
            path.mkdirs();
        }
        return imageScopesPath;
    }

    public static String getImageClipboardPath() {
        String imageClipboardPath = MyboxDataPath + File.separator + "imageClipboard";
        File path = new File(imageClipboardPath);
        if (!path.exists()) {
            path.mkdirs();
        }
        return imageClipboardPath;
    }

    public static int getCommentsDelay() {
        return getUserConfigInt("CommentsDelay", 3000);
    }

    public static Color getAlphaColor() {
        String color = getUserConfigValue("AlphaAsColor", Color.WHITE.toString());
        return Color.web(color);
    }

    public static String getUserConfigValue(String key, String defaultValue) {
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

    public static int getUserConfigInt(String key, int defaultValue) {
        if (userConfigValues.containsKey(key)) {
            try {
                int v = Integer.valueOf(userConfigValues.get(key));
                return v;
            } catch (Exception e) {
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

    public static long getUserConfigLong(String key, long defaultValue) {
        if (userConfigValues.containsKey(key)) {
            try {
                long v = Long.valueOf(userConfigValues.get(key));
                return v;
            } catch (Exception e) {
            }
        }
        try {
            String s = TableUserConf.readString(key, defaultValue + "");
            long v = Long.valueOf(s);
            userConfigValues.put(key, v + "");
            return v;
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean getUserConfigBoolean(String key, boolean defaultValue) {
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

    public static boolean getUserConfigBoolean(String key) {
        return getUserConfigBoolean(key, true);
    }

    public static File getUserConfigPath(String key) {
        return getUserConfigPath(key, MyboxDataPath);
    }

    public static File getUserConfigPath(String key, String defaultValue) {
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
                deleteUserConfigValue(key);
                path = new File(MyboxDataPath);
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

    public static boolean deleteUserConfigValue(String key) {
        if (TableUserConf.delete(key)) {
            userConfigValues.remove(key);
            return true;
        } else {
            return false;
        }
    }

    public static boolean resetWindows() {
        if (!TableUserConf.deletePrefix("Interface_")) {
            return false;
        }
        List<String> keys = new ArrayList<>();
        keys.addAll(userConfigValues.keySet());
        for (String key : keys) {
            if (key.startsWith("Interface_")) {
                userConfigValues.remove(key);
            }
        }
        return true;
    }

    public static boolean setUserConfigValue(String key, String value) {
        userConfigValues.put(key, value);
        if (TableUserConf.writeString(key, value) >= 0) {
            return true;
        } else {
            MyBoxLog.console(key + " " + value);
            return false;
        }
    }

    public static boolean setUserConfigValue(Connection conn, String key, String value) {
        userConfigValues.put(key, value);
        if (TableUserConf.writeString(conn, key, value) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean setUserConfigInt(String key, int value) {
        userConfigValues.put(key, value + "");
        if (TableUserConf.writeInt(key, value) > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean setUserConfigValue(String key, boolean value) {
        userConfigValues.put(key, value ? "true" : "false");
        if (TableUserConf.writeBoolean(key, value) > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static String getSystemConfigValue(String key, String defaultValue) {
        try {
//            MyBoxLog.debug("getSystemConfigValue:" + key);
            String value;
            if (systemConfigValues.containsKey(key)) {
                value = systemConfigValues.get(key);
            } else {
                value = TableSystemConf.readString(key, defaultValue);
                systemConfigValues.put(key, value);
            }
            return value;
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static int getSystemConfigInt(String key, int defaultValue) {
        try {
            int v;
            if (systemConfigValues.containsKey(key)) {
                v = Integer.valueOf(systemConfigValues.get(key));
            } else {
                v = TableSystemConf.readInt(key, defaultValue);
                systemConfigValues.put(key, v + "");
            }
            return v;
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean getSystemConfigBoolean(String key,
            boolean defaultValue) {
        try {
            boolean v;
            if (systemConfigValues.containsKey(key)) {
                v = systemConfigValues.get(key).equals("true");
            } else {
                v = TableSystemConf.readBoolean(key, defaultValue);
                systemConfigValues.put(key, v ? "true" : "false");
            }
            return v;
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean getSystemConfigBoolean(String key) {
        return getSystemConfigBoolean(key, true);
    }

    public static boolean setSystemConfigValue(String key, String value) {
        systemConfigValues.put(key, value);
        if (TableSystemConf.writeString(key, value) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean setSystemConfigInt(String key, int value) {
        systemConfigValues.put(key, value + "");
        if (TableSystemConf.writeInt(key, value) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean setSystemConfigValue(String key, boolean value) {
        systemConfigValues.put(key, value ? "true" : "false");
        if (TableSystemConf.writeBoolean(key, value) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean deleteSystemConfigValue(String key) {
        if (TableSystemConf.delete(key)) {
            systemConfigValues.remove(key);
            return true;
        } else {
            return false;
        }
    }

}
