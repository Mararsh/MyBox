package mara.mybox.objects;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import mara.mybox.controller.AlarmClockController;
import mara.mybox.controller.BaseController;
import static mara.mybox.objects.CommonValues.BundleEnUS;
import static mara.mybox.objects.CommonValues.BundleEsES;
import static mara.mybox.objects.CommonValues.BundleFrFR;
import static mara.mybox.objects.CommonValues.BundleRuRU;
import static mara.mybox.objects.CommonValues.BundleZhCN;
import mara.mybox.tools.ConfigTools;
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
    public static ResourceBundle CurrentBundle = CommonValues.BundleDefault;
    public static Map<String, String> configValues = new HashMap();
    public static ScheduledExecutorService executorService;
    public static Map<Long, ScheduledFuture<?>> scheduledTasks;
    public static BaseController currentController;
    public static AlarmClockController alarmClockController;
    public static boolean showComments = true;
    public static boolean alphaAsBlack = false;
    public static int commentsDelay = 3000;
    public static MemoryUsageSetting PdfMemUsage;
    public static String currentStyle;

    public AppVaribles() {
    }

    public static void initAppVaribles() {
        showComments = getConfigBoolean("ShowComments", true);
        alphaAsBlack = getConfigBoolean("AlphaAsBlack", false);
        setCurrentBundle();
        setPdfMem(getConfigValue("PdfMemDefault", "1GB"));
        currentStyle = getConfigValue("InterfaceStyle", CommonValues.MyBoxStyle);

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

    public static String getConfigValue(String key, String defaultValue) {
        try {
            if (configValues.containsKey(key)) {
                return configValues.get(key);
            }
            String value = ConfigTools.readConfigValue(key);
            if (value == null && defaultValue != null) {
                value = defaultValue;
                setConfigValue(key, value);
            }
            return value;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static int getConfigInt(String key, int defaultValue) {
        String v = getConfigValue(key, defaultValue + "");
        try {
            int value = Integer.parseInt(v);
            return value;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean setConfigInt(String key, int value) {
        if (ConfigTools.writeConfigValue(key, value + "")) {
            configValues.put(key, value + "");
            return true;
        } else {
            return false;
        }
    }

    public static boolean getConfigBoolean(String key, boolean defaultValue) {
        String v = getConfigValue(key, defaultValue + "");
        return (v != null) && v.equals("true");
    }

    public static boolean getConfigBoolean(String key) {
        String v = getConfigValue(key, "true");
        return (v != null) && v.equals("true");
    }

    public static boolean setConfigValue(String key, String value) {
        if (ConfigTools.writeConfigValue(key, value)) {
            configValues.put(key, value);
            return true;
        } else {
            return false;
        }
    }

    public static boolean setConfigValue(String key, boolean value) {
        return setConfigValue(key, value ? "true" : "false");
    }

    public static void setCurrentBundle() {
        String lang = getConfigValue("language", null);
        setCurrentBundle(lang);
    }

    public static void setCurrentBundle(String lang) {
        if (lang == null) {
            lang = Locale.getDefault().getLanguage().toLowerCase();
        }
        setConfigValue("language", lang);
        switch (lang.toLowerCase()) {
            case "zh":
                AppVaribles.CurrentBundle = CommonValues.BundleZhCN;
                break;
            case "en":
            default:
                AppVaribles.CurrentBundle = CommonValues.BundleEnUS;
                break;
        }
    }

    public static void setPdfMem(String value) {
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
    }

}
