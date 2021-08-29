package mara.mybox.value;

import java.awt.Toolkit;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import mara.mybox.controller.AlarmClockController;
import mara.mybox.db.table.TableStringValue;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ImageClipboardMonitor;
import mara.mybox.fxml.StyleData;
import mara.mybox.fxml.StyleTools;
import mara.mybox.fxml.TextClipboardMonitor;
import mara.mybox.tools.FileNameTools;
import static mara.mybox.value.Languages.getBundle;
import static mara.mybox.value.Languages.getTableBundle;
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
    public static File MyBoxTempPath, MyBoxDerbyPath, MyBoxLanguagesPath, MyBoxDownloadsPath;
    public static List<File> MyBoxReservePaths;
    public static ResourceBundle currentBundle, currentTableBundle;
    public static Map<String, String> userConfigValues = new HashMap<>();
    public static Map<String, String> systemConfigValues = new HashMap<>();
    public static ScheduledExecutorService executorService;
    public static Map<Long, ScheduledFuture<?>> scheduledTasks;
    public static AlarmClockController alarmClockController;
    public static MemoryUsageSetting pdfMemUsage;
    public static int sceneFontSize, fileRecentNumber, iconSize, thumbnailWidth;
    public static boolean openStageInNewWindow, restoreStagesSize, controlDisplayText,
            hidpiIcons, ignoreDbUnavailable, popErrorLogs, saveDebugLogs, detailedDebugLogs;
    public static StyleData.StyleColor ControlColor;
    public static TextClipboardMonitor textClipboardMonitor;
    public static ImageClipboardMonitor imageClipboardMonitor;

    public AppVariables() {
    }

    public static void initAppVaribles() {
        try {
            userConfigValues.clear();
            systemConfigValues.clear();
            getBundle();
            getTableBundle();
            getPdfMem();
            openStageInNewWindow = UserConfig.getBoolean("OpenStageInNewWindow", false);
            restoreStagesSize = UserConfig.getBoolean("RestoreStagesSize", true);
            sceneFontSize = UserConfig.getInt("SceneFontSize", 15);
            fileRecentNumber = UserConfig.getInt("FileRecentNumber", 16);
            iconSize = UserConfig.getInt("IconSize", 20);
            thumbnailWidth = UserConfig.getInt("ThumbnailWidth", 100);
            ControlColor = StyleTools.getConfigStyleColor();
            controlDisplayText = UserConfig.getBoolean("ControlDisplayText", false);
            hidpiIcons = UserConfig.getBoolean("HidpiIcons", Toolkit.getDefaultToolkit().getScreenResolution() > 120);
            saveDebugLogs = UserConfig.getBoolean("SaveDebugLogs", false);
            detailedDebugLogs = UserConfig.getBoolean("DetailedDebugLogs", false);
            ignoreDbUnavailable = false;
            popErrorLogs = true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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

    public static String getDataClipboardPath() {
        String dataClipboardPath = MyboxDataPath + File.separator + "dataClipboard";
        File path = new File(dataClipboardPath);
        if (!path.exists()) {
            path.mkdirs();
        }
        return dataClipboardPath;
    }

    public static String getFileBackupsPath(File file) {
        if (file == null) {
            return null;
        }
        String key = "BackupPath-" + file;
        String fileBackupsPath = TableStringValue.read(key);
        if (fileBackupsPath == null) {
            fileBackupsPath = MyboxDataPath + File.separator + "fileBackups" + File.separator
                    + FileNameTools.getFilePrefix(file.getName()) + FileNameTools.getFileSuffix(file.getName())
                    + (new Date()).getTime() + File.separator;
            TableStringValue.write(key, fileBackupsPath);
        }
        File path = new File(fileBackupsPath);
        if (!path.exists()) {
            path.mkdirs();
        }
        return fileBackupsPath;
    }

}
