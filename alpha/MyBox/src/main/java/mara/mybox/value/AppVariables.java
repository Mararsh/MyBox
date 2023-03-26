package mara.mybox.value;

import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import javafx.beans.property.SimpleBooleanProperty;
import mara.mybox.controller.AlarmClockController;
import mara.mybox.db.Database;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ImageClipboardMonitor;
import mara.mybox.fxml.TextClipboardMonitor;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleData;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.getBundle;
import static mara.mybox.value.UserConfig.getPdfMem;
import org.apache.pdfbox.io.MemoryUsageSetting;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class AppVariables {

    public static String[] appArgs;
    public static File MyboxConfigFile, MyBoxLogsPath;
    public static String MyboxDataPath, AlarmClocksFile;
    public static File MyBoxTempPath, MyBoxDerbyPath, MyBoxLanguagesPath;
    public static List<File> MyBoxReservePaths;
    public static ResourceBundle currentBundle;
    public static Map<String, String> userConfigValues = new HashMap<>();
    public static Map<String, String> systemConfigValues = new HashMap<>();
    public static ScheduledExecutorService executorService;
    public static Map<String, ScheduledFuture<?>> scheduledTasks;
    public static AlarmClockController alarmClockController;
    public static MemoryUsageSetting pdfMemUsage;
    public static int sceneFontSize, fileRecentNumber, iconSize, thumbnailWidth;
    public static boolean isChinese, isTesting, handlingExit,
            closeCurrentWhenOpenTool, recordWindowsSizeLocation, controlDisplayText,
            hidpiIcons, shortcutsOmitCtrlAlt,
            ignoreDbUnavailable, popErrorLogs, saveDebugLogs, detailedDebugLogs;
    public static TextClipboardMonitor textClipboardMonitor;
    public static ImageClipboardMonitor imageClipboardMonitor;
    public static Timer exitTimer;
    public static SimpleBooleanProperty errorNotify;
    public static Map<RenderingHints.Key, Object> imageRenderHints;
    public static StyleData.StyleColor ControlColor;

    public static void initAppVaribles() {
        try {
            userConfigValues.clear();
            systemConfigValues.clear();
            getBundle();
            getPdfMem();
            closeCurrentWhenOpenTool = UserConfig.getBoolean("CloseCurrentWhenOpenTool", false);
            recordWindowsSizeLocation = UserConfig.getBoolean("RecordWindowsSizeLocation", true);
            sceneFontSize = UserConfig.getInt("SceneFontSize", 15);
            fileRecentNumber = UserConfig.getInt("FileRecentNumber", 16);
            iconSize = UserConfig.getInt("IconSize", 20);
            thumbnailWidth = UserConfig.getInt("ThumbnailWidth", 100);
            ControlColor = StyleTools.getConfigStyleColor();
            controlDisplayText = UserConfig.getBoolean("ControlDisplayText", false);
            hidpiIcons = UserConfig.getBoolean("HidpiIcons", Toolkit.getDefaultToolkit().getScreenResolution() > 120);
            shortcutsOmitCtrlAlt = UserConfig.getBoolean("ShortcutsOmitCtrlAlt", true);
            saveDebugLogs = UserConfig.getBoolean("SaveDebugLogs", false);
            detailedDebugLogs = UserConfig.getBoolean("DetailedDebugLogs", false);
            ignoreDbUnavailable = false;
            popErrorLogs = UserConfig.getBoolean("PopErrorLogs", true);
            errorNotify = new SimpleBooleanProperty(false);
            isChinese = Languages.isChinese();
            isTesting = false;

            Database.BatchSize = UserConfig.getLong("DatabaseBatchSize", 500);
            if (Database.BatchSize <= 0) {
                Database.BatchSize = 500;
                UserConfig.setLong("DatabaseBatchSize", 500);
            }

            ImageRenderHints.loadImageRenderHints();

            if (exitTimer != null) {
                exitTimer.cancel();
            }
            exitTimer = new Timer();
            exitTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    if (handlingExit) {
                        return;
                    }
                    System.gc();
                    WindowTools.checkExit();
                }
            }, 0, 3000);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
