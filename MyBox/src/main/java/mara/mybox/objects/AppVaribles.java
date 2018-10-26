package mara.mybox.objects;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import mara.mybox.controller.AlarmClockController;
import mara.mybox.controller.BaseController;
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

    private static final Logger logger = LogManager.getLogger();
    public static ResourceBundle CurrentBundle;
    public static Map<String, String> configValues;
    public static ScheduledExecutorService executorService;
    public static Map<Long, ScheduledFuture<?>> scheduledTasks;
    public static BaseController currentController;
    public static AlarmClockController alarmClockController;
    public static MemoryUsageSetting PdfMemUsage;

    public AppVaribles() {
    }

    public static void initAppVaribles() {
        configValues = new HashMap();
        getBundle();
        getPdfMem();
    }

    public static void clear() {
        new TableUserConf().clear();
        initAppVaribles();
    }

    public static String getLanguage() {
        return getConfigValue("language", Locale.getDefault().getLanguage().toLowerCase());
    }

    public static ResourceBundle setLanguage(String lang) {
        if (lang == null) {
            lang = Locale.getDefault().getLanguage().toLowerCase();
        }
        setConfigValue("language", lang);
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
                AppVaribles.setConfigValue("PdfMemDefault", "1GB");
                AppVaribles.PdfMemUsage = MemoryUsageSetting.setupMixed(1024 * 1024 * 1024, -1);
                break;
            case "2GB":
                AppVaribles.setConfigValue("PdfMemDefault", "2GB");
                AppVaribles.PdfMemUsage = MemoryUsageSetting.setupMixed(2048 * 1024 * 1024, -1);
                break;
            case "Unlimit":
                AppVaribles.setConfigValue("PdfMemDefault", "Unlimit");
                AppVaribles.PdfMemUsage = MemoryUsageSetting.setupMixed(-1, -1);
                break;
            case "500MB":
            default:
                AppVaribles.setConfigValue("PdfMemDefault", "500MB");
                AppVaribles.PdfMemUsage = MemoryUsageSetting.setupMixed(500 * 1024 * 1024, -1);
        }
        return AppVaribles.PdfMemUsage;
    }

    public static MemoryUsageSetting getPdfMem() {
        return setPdfMem(getConfigValue("PdfMemDefault", "1GB"));
    }

    public static String getStyle() {
        return getConfigValue("InterfaceStyle", CommonValues.DefaultStyle);
    }

    public static String getTempPath() {
        return getConfigValue("TempDir", CommonValues.UserFilePath);
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
        return getConfigInt("CommentsDelay", 3000);
    }

    public static boolean isShowComments() {
        return getConfigBoolean("ShowComments", true);
    }

    public static boolean isAlphaAsBlack() {
        return getConfigBoolean("AlphaAsBlack", false);
    }

    public static String getConfigValue(String key, String defaultValue) {
        try {
//            logger.debug("getConfigValue:" + key);
            String value;
            if (configValues.containsKey(key)) {
                value = configValues.get(key);
            } else {
                value = TableUserConf.read(key, defaultValue);
                configValues.put(key, value);
            }
            return value;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static int getConfigInt(String key, int defaultValue) {
        try {
            int v;
            if (configValues.containsKey(key)) {
                v = Integer.valueOf(configValues.get(key));
            } else {
                v = TableUserConf.readInt(key, defaultValue);
                configValues.put(key, v + "");
            }
            return v;
        } catch (Exception e) {
            logger.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean getConfigBoolean(String key, boolean defaultValue) {
        try {
            boolean v;
            if (configValues.containsKey(key)) {
                v = configValues.get(key).equals("true");
            } else {
                v = TableUserConf.readBoolean(key, defaultValue);
                configValues.put(key, v ? "true" : "false");
            }
            return v;
        } catch (Exception e) {
            logger.error(e.toString());
            return defaultValue;
        }
    }

    public static boolean getConfigBoolean(String key) {
        return getConfigBoolean(key, true);
    }

    public static boolean setConfigValue(String key, String value) {
        if (TableUserConf.write(key, value) > 0) {
            configValues.put(key, value);
            return true;
        } else {
            return false;
        }
    }

    public static boolean setConfigInt(String key, int value) {
        if (TableUserConf.write(key, value) > 0) {
            configValues.put(key, value + "");
            return true;
        } else {
            return false;
        }
    }

    public static boolean setConfigValue(String key, boolean value) {
        if (TableUserConf.write(key, value) > 0) {
            configValues.put(key, value ? "true" : "false");
            return true;
        } else {
            return false;
        }
    }

}
