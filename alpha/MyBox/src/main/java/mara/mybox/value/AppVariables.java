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
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ImageClipboardMonitor;
import mara.mybox.fxml.TextClipboardMonitor;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleData;
import mara.mybox.fxml.style.StyleTools;
import org.apache.pdfbox.io.MemoryUsageSetting;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class AppVariables {

    public static String[] AppArgs;
    public static File MyboxConfigFile, MyBoxLogsPath;
    public static String MyboxDataPath, AlarmClocksFile, CurrentLangName;
    public static File MyBoxTempPath, MyBoxDerbyPath, MyBoxLanguagesPath;
    public static List<File> MyBoxReservePaths;
    public static ResourceBundle CurrentBundle;
    public static Map<String, String> UserConfigValues = new HashMap<>();
    public static Map<String, String> SystemConfigValues = new HashMap<>();
    public static ScheduledExecutorService ExecutorService;
    public static Map<String, ScheduledFuture<?>> ScheduledTasks;
    public static AlarmClockController AlarmClockController;
    public static MemoryUsageSetting PdfMemUsage;
    public static int sceneFontSize, fileRecentNumber, iconSize, thumbnailWidth;
    public static long maxDemoImage;
    public static boolean isTesting, handlingExit, ShortcutsCanNotOmitCtrlAlt, icons40px,
            closeCurrentWhenOpenTool, recordWindowsSizeLocation, controlDisplayText,
            commitModificationWhenDataCellLoseFocus,
            ignoreDbUnavailable, popErrorLogs, saveDebugLogs, detailedDebugLogs;
    public static TextClipboardMonitor TextClipMonitor;
    public static ImageClipboardMonitor ImageClipMonitor;
    public static Timer ExitTimer;
    public static SimpleBooleanProperty ErrorNotify;
    public static Map<RenderingHints.Key, Object> ImageHints;
    public static StyleData.StyleColor ControlColor;

    public static void initAppVaribles() {
        try {
            UserConfigValues.clear();
            SystemConfigValues.clear();
            CurrentLangName = Languages.getLangName();
            CurrentBundle = Languages.getBundle();
            UserConfig.getPdfMem();
            closeCurrentWhenOpenTool = UserConfig.getBoolean("CloseCurrentWhenOpenTool", false);
            recordWindowsSizeLocation = UserConfig.getBoolean("RecordWindowsSizeLocation", true);
            sceneFontSize = UserConfig.getInt("SceneFontSize", 15);
            fileRecentNumber = UserConfig.getInt("FileRecentNumber", VisitHistory.Default_Max_Histories);
            iconSize = UserConfig.getInt("IconSize", 20);
            thumbnailWidth = UserConfig.getInt("ThumbnailWidth", 100);
            maxDemoImage = UserConfig.getLong("MaxDemoImage", 1000000);
            ControlColor = StyleTools.getConfigStyleColor();
            controlDisplayText = UserConfig.getBoolean("ControlDisplayText", false);
            icons40px = UserConfig.getBoolean("Icons40px", Toolkit.getDefaultToolkit().getScreenResolution() <= 120);
            ShortcutsCanNotOmitCtrlAlt = UserConfig.getBoolean("ShortcutsCanNotOmitCtrlAlt", false);
            commitModificationWhenDataCellLoseFocus = UserConfig.getBoolean("CommitModificationWhenDataCellLoseFocus", true);
            saveDebugLogs = UserConfig.getBoolean("SaveDebugLogs", false);
            detailedDebugLogs = UserConfig.getBoolean("DetailedDebugLogs", false);
            ignoreDbUnavailable = false;
            popErrorLogs = UserConfig.getBoolean("PopErrorLogs", true);
            ErrorNotify = new SimpleBooleanProperty(false);
            isTesting = false;

            Database.BatchSize = UserConfig.getLong("DatabaseBatchSize", 500);
            if (Database.BatchSize <= 0) {
                Database.BatchSize = 500;
                UserConfig.setLong("DatabaseBatchSize", 500);
            }

            ImageRenderHints.loadImageRenderHints();

            if (ExitTimer != null) {
                ExitTimer.cancel();
            }
            ExitTimer = new Timer();
            ExitTimer.schedule(new TimerTask() {

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

    public static void lostFocusCommitData(boolean auto) {
        AppVariables.commitModificationWhenDataCellLoseFocus = auto;
        UserConfig.setBoolean("CommitModificationWhenDataCellLoseFocus", auto);
    }

}
