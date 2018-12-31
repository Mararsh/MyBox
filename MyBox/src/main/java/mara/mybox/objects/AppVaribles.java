package mara.mybox.objects;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import mara.mybox.controller.AlarmClockController;
import mara.mybox.db.TableSystemConf;
import mara.mybox.db.TableUserConf;
import static mara.mybox.objects.CommonValues.BundleEnUS;
import static mara.mybox.objects.CommonValues.BundleEsES;
import static mara.mybox.objects.CommonValues.BundleFrFR;
import static mara.mybox.objects.CommonValues.BundleRuRU;
import static mara.mybox.objects.CommonValues.BundleZhCN;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.pdfbox.io.MemoryUsageSetting;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:59:49
 * @Description
 * @License Apache License Version 2.0
 */
public class AppVaribles {

    public static final Logger logger = LogManager.getLogger();
    public static ResourceBundle CurrentBundle;
    public static Map<String, String> userConfigValues;
    public static Map<String, String> systemConfigValues;
    public static ScheduledExecutorService executorService;
    public static Map<Long, ScheduledFuture<?>> scheduledTasks;
    public static AlarmClockController alarmClockController;
    public static MemoryUsageSetting PdfMemUsage;

    public AppVaribles() {
    }

    public static void initAppVaribles() {
        userConfigValues = new HashMap();
        systemConfigValues = new HashMap();
        getBundle();
        getPdfMem();
    }

    public static void clear() {
        new TableUserConf().clear();
        initAppVaribles();
    }

    public static String getLanguage() {
        return getUserConfigValue("language", Locale.getDefault().getLanguage().toLowerCase());
    }

    public static ResourceBundle setLanguage(String lang) {
        if (lang == null) {
            lang = Locale.getDefault().getLanguage().toLowerCase();
        }
        AppVaribles.setUserConfigValue("language", lang);
        return getBundle();
    }

    public static ResourceBundle getBundle() {
        String lang = getLanguage();
        switch (lang.toLowerCase()) {
            case "zh":
                AppVaribles.CurrentBundle = CommonValues.BundleZhCN;
                break;
            case "en":
            default:
                AppVaribles.CurrentBundle = CommonValues.BundleEnUS;
                break;
        }
        return AppVaribles.CurrentBundle;
    }

    public static String getMessage(String thestr) {
        try {
            return CurrentBundle.getString(thestr);
        } catch (Exception e) {
            return thestr;
        }
    }

    public static String getMessage(String language, String thestr) {
        try {
            if (thestr.trim().isEmpty()) {
                return thestr;
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
                case "fr":
                case "fr_fr":
                    value = BundleFrFR.getString(thestr);
                    break;
                case "es":
                case "es_es":
                    value = BundleEsES.getString(thestr);
                    break;
                case "ru":
                case "ru_ru":
                    value = BundleRuRU.getString(thestr);
                    break;
            }
//            logger.debug(language + " " + thestr + " " + value);
            return value;
        } catch (Exception e) {
//            logger.debug(e.toString());
            return thestr;
        }
    }

    public static MemoryUsageSetting setPdfMem(String value) {
        switch (value) {
            case "1GB":
                AppVaribles.setUserConfigValue("PdfMemDefault", "1GB");
                AppVaribles.PdfMemUsage = MemoryUsageSetting.setupMixed(1024 * 1024 * 1024, -1);
                break;
            case "2GB":
                AppVaribles.setUserConfigValue("PdfMemDefault", "2GB");
                AppVaribles.PdfMemUsage = MemoryUsageSetting.setupMixed(2048 * 1024 * 1024, -1);
                break;
            case "Unlimit":
                AppVaribles.setUserConfigValue("PdfMemDefault", "Unlimit");
                AppVaribles.PdfMemUsage = MemoryUsageSetting.setupMixed(-1, -1);
                break;
            case "500MB":
            default:
                AppVaribles.setUserConfigValue("PdfMemDefault", "500MB");
                AppVaribles.PdfMemUsage = MemoryUsageSetting.setupMixed(500 * 1024 * 1024, -1);
        }
        return AppVaribles.PdfMemUsage;
    }

    public static MemoryUsageSetting getPdfMem() {
        return setPdfMem(getUserConfigValue("PdfMemDefault", "1GB"));
    }

    public static String getStyle() {
        return getUserConfigValue("InterfaceStyle", CommonValues.DefaultStyle);
    }

    public static String getTempPath() {
        return getUserConfigValue("TempDir", CommonValues.UserFilePath);
    }

    public static File getTempPathFile() {
        File tempdir = new File(AppVaribles.getTempPath());
        if (!tempdir.exists()) {
            tempdir.mkdirs();
        }
        return tempdir;
    }

    public static String getImageHisPath() {
        String imageHistoriesPath = AppVaribles.getTempPath() + File.separator + "imageHistories";
        File path = new File(imageHistoriesPath);
        if (!path.exists()) {
            path.mkdirs();
        }
        return imageHistoriesPath;
    }

    public static int getCommentsDelay() {
        return getUserConfigInt("CommentsDelay", 3000);
    }

    public static boolean isShowComments() {
        return AppVaribles.getUserConfigBoolean("ShowComments", true);
    }

    public static boolean isAlphaAsBlack() {
        return AppVaribles.getUserConfigBoolean("AlphaAsBlack", false);
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
            logger.error(e.toString());
            return null;
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
            logger.error(e.toString());
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
            logger.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean getUserConfigBoolean(String key) {
        return AppVaribles.getUserConfigBoolean(key, true);
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
        return AppVaribles.getSystemConfigBoolean(key, true);
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

}
