package mara.mybox.value;

import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.io.File;
import java.sql.Connection;
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
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ImageClipboardMonitor;
import mara.mybox.fxml.TextClipboardMonitor;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleData;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.isChinese;
import static mara.mybox.value.Languages.sysDefaultLanguage;

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
    public static int sceneFontSize, fileRecentNumber, iconSize, thumbnailWidth,
            titleTrimSize, menuMaxLen;
    public static long maxDemoImage;
    public static boolean isTesting, handlingExit, ShortcutsCanNotOmitCtrlAlt, icons40px,
            closeCurrentWhenOpenTool, branchWindowIconifyParent, recordWindowsSizeLocation, controlDisplayText,
            commitModificationWhenDataCellLoseFocus,
            ignoreDbUnavailable, popErrorLogs, saveDebugLogs, detailedDebugLogs,
            rejectInvalidValueWhenEdit, rejectInvalidValueWhenSave,
            useChineseWhenBlankTranslation;
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
            ignoreDbUnavailable = false;
            ErrorNotify = new SimpleBooleanProperty(false);
            isTesting = false;

            loadAppVaribles();

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

    public static void loadAppVaribles() {
        try {
            Connection conn = null;
            try {
                conn = DerbyBase.getConnection();
            } catch (Exception e) {
                MyBoxLog.console(e.toString());
            }
            closeCurrentWhenOpenTool = UserConfig.getBoolean(conn, "CloseCurrentWhenOpenTool", false);
            branchWindowIconifyParent = UserConfig.getBoolean(conn, "BranchWindowIconifyParent", true);
            recordWindowsSizeLocation = UserConfig.getBoolean(conn, "RecordWindowsSizeLocation", true);
            sceneFontSize = UserConfig.getInt(conn, "SceneFontSize", 15);
            fileRecentNumber = UserConfig.getInt(conn, "FileRecentNumber", VisitHistory.Default_Max_Histories);
            iconSize = UserConfig.getInt(conn, "IconSize", 20);
            ControlColor = StyleTools.getColorStyle(UserConfig.getString(conn, "ControlColor", "red"));
            controlDisplayText = UserConfig.getBoolean(conn, "ControlDisplayText", false);
            icons40px = UserConfig.getBoolean(conn, "Icons40px", Toolkit.getDefaultToolkit().getScreenResolution() <= 120);
            thumbnailWidth = UserConfig.getInt(conn, "ThumbnailWidth", 100);
            maxDemoImage = UserConfig.getLong(conn, "MaxDemoImage", 1000000);
            titleTrimSize = UserConfig.getInt(conn, "TitleTrimSize", 60);
            menuMaxLen = UserConfig.getInt(conn, "MenuMaxLen", 80);
            ShortcutsCanNotOmitCtrlAlt = UserConfig.getBoolean(conn, "ShortcutsCanNotOmitCtrlAlt", false);
            useChineseWhenBlankTranslation = UserConfig.getBoolean(conn,
                    "UseChineseWhenBlankTranslation", isChinese(sysDefaultLanguage()));

            commitModificationWhenDataCellLoseFocus = UserConfig.getBoolean(conn, "CommitModificationWhenDataCellLoseFocus", true);
            rejectInvalidValueWhenEdit = UserConfig.getBoolean(conn, "Data2DValidateEdit", false);
            rejectInvalidValueWhenSave = UserConfig.getBoolean(conn, "Data2DValidateSave", true);

            saveDebugLogs = UserConfig.getBoolean(conn, "SaveDebugLogs", false);
            detailedDebugLogs = UserConfig.getBoolean(conn, "DetailedDebugLogs", false);
            popErrorLogs = UserConfig.getBoolean(conn, "PopErrorLogs", true);

            Database.BatchSize = UserConfig.getLong(conn, "DatabaseBatchSize", 500);
            if (Database.BatchSize <= 0) {
                Database.BatchSize = 500;
                UserConfig.setLong(conn, "DatabaseBatchSize", 500);
            }

            ImageRenderHints.loadImageRenderHints(conn);

            if (conn != null) {
                conn.close();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void lostFocusCommitData(boolean auto) {
        AppVariables.commitModificationWhenDataCellLoseFocus = auto;
        UserConfig.setBoolean("CommitModificationWhenDataCellLoseFocus", auto);
    }

}
