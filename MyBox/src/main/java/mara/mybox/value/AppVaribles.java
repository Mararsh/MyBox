package mara.mybox.value;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.util.Duration;
import mara.mybox.controller.AlarmClockController;
import mara.mybox.controller.base.BaseController;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.db.TableSystemConf;
import mara.mybox.db.TableUserConf;
import static mara.mybox.value.CommonValues.BundleEnUS;
import static mara.mybox.value.CommonValues.BundleEsES;
import static mara.mybox.value.CommonValues.BundleFrFR;
import static mara.mybox.value.CommonValues.BundleRuRU;
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
public class AppVaribles {

    public static final Logger logger = LogManager.getLogger();
    public static ResourceBundle currentBundle;
    public static Map<String, String> userConfigValues;
    public static Map<String, String> systemConfigValues;
    public static ScheduledExecutorService executorService;
    public static Map<Long, ScheduledFuture<?>> scheduledTasks;
    public static AlarmClockController alarmClockController;
    public static MemoryUsageSetting pdfMemUsage;
    public static int sceneFontSize, fileRecentNumber;
    public static Map<Stage, BaseController> openedStages;
    public static boolean openStageInNewWindow, restoreStagesSize, controlDisplayText;
    public static ControlStyle.ColorStyle ControlColor;
    public static Class env;

    public AppVaribles() {

    }

    public static void initAppVaribles() {
        try {
            userConfigValues = new HashMap();
            systemConfigValues = new HashMap();
            env = userConfigValues.getClass();
            openedStages = new HashMap();
            getBundle();
            getPdfMem();
            openStageInNewWindow = AppVaribles.getUserConfigBoolean("OpenStageInNewWindow", false);
            restoreStagesSize = AppVaribles.getUserConfigBoolean("RestoreStagesSize", true);
            sceneFontSize = AppVaribles.getUserConfigInt("SceneFontSize", 15);
            fileRecentNumber = AppVaribles.getUserConfigInt("FileRecentNumber", 15);
            ControlColor = ControlStyle.getConfigColorStyle();
            controlDisplayText = AppVaribles.getUserConfigBoolean("ControlDisplayText", false);
            setTipTime();
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    // https://stackoverflow.com/questions/26854301/how-to-control-the-javafx-tooltips-delay?noredirect=1
    // https://www.jianshu.com/p/5ecec2c4d224
    public static void setTipTime() {
        try {
            Tooltip tooltip = new Tooltip();
            Class tipClass = tooltip.getClass();
            Field f = tipClass.getDeclaredField("BEHAVIOR");
            f.setAccessible(true);
            Class TooltipBehavior = Class.forName("javafx.scene.control.Tooltip$TooltipBehavior");
            Constructor constructor = TooltipBehavior.getDeclaredConstructor(Duration.class, Duration.class, Duration.class, boolean.class);
            constructor.setAccessible(true);
            f.set(TooltipBehavior, constructor.newInstance(new Duration(10), new Duration(360000), new Duration(10), false));
        } catch (Exception e) {
            logger.error(e.toString());
        }
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
                AppVaribles.currentBundle = CommonValues.BundleZhCN;
                break;
            case "en":
            default:
                AppVaribles.currentBundle = CommonValues.BundleEnUS;
                break;
        }
        return AppVaribles.currentBundle;
    }

    public static String getMessage(String thestr) {
        try {
            return currentBundle.getString(thestr);
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

    public static boolean setOpenStageInNewWindow(boolean value) {
        if (AppVaribles.setUserConfigValue("OpenStageInNewWindow", value)) {
            AppVaribles.openStageInNewWindow = value;
            return true;
        } else {
            return false;
        }
    }

    public static boolean setRestoreStagesSize(boolean value) {
        if (AppVaribles.setUserConfigValue("RestoreStagesSize", value)) {
            AppVaribles.restoreStagesSize = value;
            return true;
        } else {
            return false;
        }
    }

    public static MemoryUsageSetting setPdfMem(String value) {
        switch (value) {
            case "1GB":
                AppVaribles.setUserConfigValue("PdfMemDefault", "1GB");
                AppVaribles.pdfMemUsage = MemoryUsageSetting.setupMixed(1024 * 1024 * 1024, -1);
                break;
            case "2GB":
                AppVaribles.setUserConfigValue("PdfMemDefault", "2GB");
                AppVaribles.pdfMemUsage = MemoryUsageSetting.setupMixed(2048 * 1024 * 1024, -1);
                break;
            case "Unlimit":
                AppVaribles.setUserConfigValue("PdfMemDefault", "Unlimit");
                AppVaribles.pdfMemUsage = MemoryUsageSetting.setupMixed(-1, -1);
                break;
            case "500MB":
            default:
                AppVaribles.setUserConfigValue("PdfMemDefault", "500MB");
                AppVaribles.pdfMemUsage = MemoryUsageSetting.setupMixed(500 * 1024 * 1024, -1);
        }
        return AppVaribles.pdfMemUsage;
    }

    public static MemoryUsageSetting getPdfMem() {
        return setPdfMem(getUserConfigValue("PdfMemDefault", "1GB"));
    }

    public static boolean setSceneFontSize(int size) {
        if (AppVaribles.setUserConfigInt("SceneFontSize", size)) {
            AppVaribles.sceneFontSize = size;
            return true;
        } else {
            return false;
        }
    }

    public static String getStyle() {
        return getUserConfigValue("InterfaceStyle", CommonValues.DefaultStyle);
    }

    public static File getUserTempPath() {
        return AppVaribles.getUserConfigPath(CommonValues.userTempPathKey);
    }

    public static String getImageHisPath() {
        String imageHistoriesPath = AppVaribles.getUserTempPath().getAbsolutePath()
                + File.separator + "imageHistories";
        File path = new File(imageHistoriesPath);
        if (!path.exists()) {
            path.mkdirs();
        }
        return imageHistoriesPath;
    }

    public static int getCommentsDelay() {
        return getUserConfigInt("CommentsDelay", 2000);
    }

    public static boolean isAlphaAsWhite() {
        return AppVaribles.getUserConfigBoolean("AlphaAsWhite", true);
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
        return AppVaribles.getUserConfigBoolean(key, true);
    }

    public static File getUserConfigPath(String key) {
        return getUserConfigPath(key, CommonValues.AppDataRoot);
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
                path = new File(CommonValues.AppDataRoot);
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
        List<String> keys = new ArrayList();
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

    public static boolean deleteSystemConfigValue(String key) {
        if (TableSystemConf.delete(key)) {
            systemConfigValues.remove(key);
            return true;
        } else {
            return false;
        }
    }

    public static void stageOpened(Stage stage, BaseController controller) {
//        logger.debug(stage.getTitle() + " " + AppVaribles.openedStages.size());
        openedStages.put(stage, controller);
    }

    public static void stageClosed(Stage stage) {
//        logger.debug(stage.getTitle() + " " + AppVaribles.openedStages.size());
        openedStages.remove(stage);
    }

    public static boolean stageRecorded(Stage stage) {
        return openedStages.containsKey(stage);
    }

}
