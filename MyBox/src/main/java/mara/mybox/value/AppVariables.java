package mara.mybox.value;

import java.io.File;
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
import mara.mybox.data.CustomizedLanguage;
import mara.mybox.db.TableSystemConf;
import mara.mybox.db.TableUserConf;
import mara.mybox.fxml.ControlStyle;
import static mara.mybox.value.CommonValues.BundleEnUS;
import static mara.mybox.value.CommonValues.BundleZhCN;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.io.MemoryUsageSetting;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:59:49
 * @Description
 * @License Apache License Version 2.0
 */
public class AppVariables {

    public static String[] appArgs;
    public static File MyboxConfigFile;
    public static String MyboxDataPath, AlarmClocksFile;
    public static File MyBoxTempPath, MyBoxDerbyPath, MyBoxLanguagesPath, MyBoxDownloadsPath;
    public static List<File> MyBoxReservePaths;

    public static Logger logger = LogManager.getLogger(AppVariables.class);
    public static ResourceBundle currentBundle;
    public static Map<String, String> userConfigValues;
    public static Map<String, String> systemConfigValues;
    public static ScheduledExecutorService executorService;
    public static Map<Long, ScheduledFuture<?>> scheduledTasks;
    public static AlarmClockController alarmClockController;
    public static MemoryUsageSetting pdfMemUsage;
    public static int sceneFontSize, fileRecentNumber, iconSize;
    public static boolean openStageInNewWindow, restoreStagesSize, controlDisplayText,
            ImagePopCooridnate, disableHiDPI, DerbyFailAsked;
    public static ControlStyle.ColorStyle ControlColor;
    public static String lastError;
    public static SSLSocketFactory defaultSSLSocketFactory;
    public static HostnameVerifier defaultHostnameVerifier;

    public AppVariables() {
    }

    public static void initAppVaribles() {
        try {
            userConfigValues = new HashMap<>();
            systemConfigValues = new HashMap<>();
            getBundle();
            getPdfMem();
            openStageInNewWindow = AppVariables.getUserConfigBoolean("OpenStageInNewWindow", false);
            restoreStagesSize = AppVariables.getUserConfigBoolean("RestoreStagesSize", true);
            sceneFontSize = AppVariables.getUserConfigInt("SceneFontSize", 15);
            fileRecentNumber = AppVariables.getUserConfigInt("FileRecentNumber", 15);
            iconSize = AppVariables.getUserConfigInt("IconSize", 20);
            ControlColor = ControlStyle.getConfigColorStyle();
            controlDisplayText = AppVariables.getUserConfigBoolean("ControlDisplayText", false);
            ImagePopCooridnate = AppVariables.getUserConfigBoolean("ImagePopCooridnate", false);
            disableHiDPI = DerbyFailAsked = false;
            lastError = null;
            if (defaultSSLSocketFactory == null) {
                defaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
                defaultHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public static void clear() {
        new TableUserConf().clear();
        initAppVaribles();
    }

    public static String getLanguage() {
        String lang = getUserConfigValue("language", Locale.getDefault().getLanguage().toLowerCase());
        return lang != null ? lang : Locale.getDefault().getLanguage().toLowerCase();
    }

    public static ResourceBundle setLanguage(String lang) {
        if (lang == null) {
            lang = Locale.getDefault().getLanguage().toLowerCase();
        }
        AppVariables.setUserConfigValue("language", lang);
        return getBundle();
    }

    public static ResourceBundle getBundle() {
        String lang = getLanguage();
        if (lang == null) {
            lang = Locale.getDefault().getLanguage().toLowerCase();
        }
        switch (lang.toLowerCase()) {
            case "zh":
                AppVariables.currentBundle = CommonValues.BundleZhCN;
                break;
            case "en":
                AppVariables.currentBundle = CommonValues.BundleEnUS;
                break;
            default: {
                try {
                    AppVariables.currentBundle = new CustomizedLanguage(lang);
                } catch (Exception e) {
                }
                if (AppVariables.currentBundle == null) {
                    AppVariables.currentBundle = CommonValues.BundleEnUS;
                }
            }
        }
        return AppVariables.currentBundle;
    }

    public static String message(String thestr) {
        try {
            if (currentBundle == null) {
                currentBundle = CommonValues.BundleEnUS;
            }
            return currentBundle.getString(thestr);
        } catch (Exception e) {
            return thestr;
        }
    }

    public static String message(String language, String thestr) {
        try {
            if (thestr.trim().isEmpty()) {
                return thestr;
            }
            if (language == null) {
                language = Locale.getDefault().getLanguage().toLowerCase();
            }
            String value = thestr;
            switch (language.toLowerCase()) {
                case "zh":
                case "zh_cn":
                    value = BundleZhCN.getString(thestr);
                    break;
                case "en":
                case "en_us":
                    value = BundleEnUS.getString(thestr);
                    break;
            }
//            logger.debug(language + " " + thestr + " " + value);
            return value;
        } catch (Exception e) {
//            logger.debug(e.toString());
            return thestr;
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
        if (AppVariables.setUserConfigValue("OpenStageInNewWindow", value)) {
            AppVariables.openStageInNewWindow = value;
            return true;
        } else {
            return false;
        }
    }

    public static boolean setRestoreStagesSize(boolean value) {
        if (AppVariables.setUserConfigValue("RestoreStagesSize", value)) {
            AppVariables.restoreStagesSize = value;
            return true;
        } else {
            return false;
        }
    }

    public static MemoryUsageSetting setPdfMem(String value) {
        switch (value) {
            case "1GB":
                AppVariables.setUserConfigValue("PdfMemDefault", "1GB");
                AppVariables.pdfMemUsage = MemoryUsageSetting.setupMixed(1024 * 1024 * 1024, -1);
                break;
            case "2GB":
                AppVariables.setUserConfigValue("PdfMemDefault", "2GB");
                AppVariables.pdfMemUsage = MemoryUsageSetting.setupMixed(2048 * 1024 * 1024, -1);
                break;
            case "Unlimit":
                AppVariables.setUserConfigValue("PdfMemDefault", "Unlimit");
                AppVariables.pdfMemUsage = MemoryUsageSetting.setupMixed(-1, -1);
                break;
            case "500MB":
            default:
                AppVariables.setUserConfigValue("PdfMemDefault", "500MB");
                AppVariables.pdfMemUsage = MemoryUsageSetting.setupMixed(500 * 1024 * 1024, -1);
        }
        return AppVariables.pdfMemUsage;
    }

    public static MemoryUsageSetting getPdfMem() {
        return setPdfMem(getUserConfigValue("PdfMemDefault", "1GB"));
    }

    public static boolean setSceneFontSize(int size) {
        if (AppVariables.setUserConfigInt("SceneFontSize", size)) {
            AppVariables.sceneFontSize = size;
            return true;
        } else {
            return false;
        }
    }

    public static boolean setIconSize(int size) {
        if (AppVariables.setUserConfigInt("IconSize", size)) {
            AppVariables.iconSize = size;
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
        String color = AppVariables.getUserConfigValue("AlphaAsColor", Color.WHITE.toString());
        return Color.web(color);
    }

    public static String getUserConfigValue(String key, String defaultValue) {
        try {
//            logger.debug("getUserConfigValue:" + key);
            String value;
            if (userConfigValues.containsKey(key)) {
                value = userConfigValues.get(key);
            } else {
                value = TableUserConf.read(key, defaultValue);
                userConfigValues.put(key, value);
            }
            return value;
        } catch (Exception e) {
//            logger.error(e.toString());
            return defaultValue;
        }
    }

    public static int getUserConfigInt(String key, int defaultValue) {
        try {
            int v;
            if (userConfigValues.containsKey(key)) {
                v = Integer.valueOf(userConfigValues.get(key));
            } else {
                v = TableUserConf.readInt(key, defaultValue);
                userConfigValues.put(key, v + "");
            }
            return v;
        } catch (Exception e) {
//            logger.error(e.toString());
            return defaultValue;
        }
    }

    public static long getUserConfigLong(String key, long defaultValue) {
        try {
            long v;
            if (userConfigValues.containsKey(key)) {
                v = Long.valueOf(userConfigValues.get(key));
            } else {
                String s = TableUserConf.read(key, defaultValue + "");
                v = Long.valueOf(s);
                userConfigValues.put(key, v + "");
            }
            return v;
        } catch (Exception e) {
//            logger.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean getUserConfigBoolean(String key, boolean defaultValue) {
        try {
            boolean v;
            if (userConfigValues.containsKey(key)) {
                v = userConfigValues.get(key).equals("true");
            } else {
                v = TableUserConf.readBoolean(key, defaultValue);
                userConfigValues.put(key, v ? "true" : "false");
            }
            return v;
        } catch (Exception e) {
//            logger.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean getUserConfigBoolean(String key) {
        return AppVariables.getUserConfigBoolean(key, true);
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
                pathString = TableUserConf.read(key, defaultValue);
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
//            logger.error(e.toString());
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
        if (TableUserConf.write(key, value) > 0) {
            userConfigValues.put(key, value);
            return true;
        } else {
            return false;
        }
    }

    public static boolean setUserConfigInt(String key, int value) {
        if (TableUserConf.write(key, value) > 0) {
            userConfigValues.put(key, value + "");
            return true;
        } else {
            return false;
        }
    }

    public static boolean setUserConfigValue(String key, boolean value) {
        if (TableUserConf.write(key, value) > 0) {
            userConfigValues.put(key, value ? "true" : "false");
            return true;
        } else {
            return false;
        }
    }

    public static String getSystemConfigValue(String key, String defaultValue) {
        try {
//            logger.debug("getSystemConfigValue:" + key);
            String value;
            if (systemConfigValues.containsKey(key)) {
                value = systemConfigValues.get(key);
            } else {
                value = TableSystemConf.read(key, defaultValue);
                systemConfigValues.put(key, value);
            }
            return value;
        } catch (Exception e) {
            logger.error(e.toString());
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
            logger.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean getSystemConfigBoolean(String key, boolean defaultValue) {
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
            logger.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean getSystemConfigBoolean(String key) {
        return AppVariables.getSystemConfigBoolean(key, true);
    }

    public static boolean setSystemConfigValue(String key, String value) {
        if (TableSystemConf.write(key, value) > 0) {
            systemConfigValues.put(key, value);
            return true;
        } else {
            return false;
        }
    }

    public static boolean setSystemConfigInt(String key, int value) {
        if (TableSystemConf.write(key, value) > 0) {
            systemConfigValues.put(key, value + "");
            return true;
        } else {
            return false;
        }
    }

    public static boolean setSystemConfigValue(String key, boolean value) {
        if (TableSystemConf.write(key, value) > 0) {
            systemConfigValues.put(key, value ? "true" : "false");
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
