package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-5-7
 * @License Apache License Version 2.0
 */
public class BaseTaskController extends BaseLogsController {

    protected boolean taskCancelled, taskSuccessed;
    protected Date startTime, endTime;
    protected LinkedHashMap<Integer, List<File>> targetFiles;
    protected String lastTargetName;
    protected int targetFilesCount;
    protected final SimpleBooleanProperty taskClosedNotify;

    @FXML
    protected Tab logsTab;

    public BaseTaskController() {
        taskClosedNotify = new SimpleBooleanProperty(false);
    }

    public boolean checkOptions() {
        return true;
    }

    @FXML
    @Override
    public void startAction() {
        runTask();
    }

    public void runTask() {
        if (startButton != null && startButton.getUserData() != null) {
            StyleTools.setNameIcon(startButton, message("Start"), "iconStart.png");
            startButton.applyCss();
            startButton.setUserData(null);
            cancelTask();
            return;
        }
        if (!checkOptions()) {
            return;
        }
        beforeTask();
        startTask();
    }

    public void beforeTask() {
        if (task != null) {
            task.cancel();
        }
        if (startButton != null) {
            StyleTools.setNameIcon(startButton, message("Stop"), "iconStop.png");
            startButton.applyCss();
            startButton.setUserData("started");
        }
        if (tabPane != null && logsTab != null) {
            tabPane.getSelectionModel().select(logsTab);
        }
        taskCancelled = false;
        taskSuccessed = false;
        targetFilesCount = 0;
        targetFiles = new LinkedHashMap<>();
        initLogs();

    }

    public void startTask() {
        defaultStartTask();
    }

    public void defaultStartTask() {
        startTime = new Date();
        updateLogs(message("Start") + ": " + DateTools.dateToString(startTime), true);
        taskSuccessed = false;
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                taskSuccessed = doTask(this);
                return taskSuccessed;
            }

            @Override
            protected void whenSucceeded() {
                afterSuccess();
            }

            @Override
            protected void whenCanceled() {
                taskCanceled();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                closeTask(ok);
            }
        };
        start(task, false);
    }

    public boolean doTask(FxTask currentTask) {
        return true;
    }

    public void afterSuccess() {

    }

    public void closeTask(boolean ok) {
        if (startButton != null) {
            StyleTools.setNameIcon(startButton, message("Start"), "iconStart.png");
            startButton.applyCss();
            startButton.setUserData(null);
        }
        if (startTime != null) {
            if (endTime == null) {
                endTime = new Date();
            }
            updateLogs(message("Completed") + " " + message("Cost")
                    + " " + DateTools.datetimeMsDuration(endTime, startTime), true);
        }
        handleTargetFiles();
        taskClosedNotify.set(!taskClosedNotify.get());
        if (miaoCheck != null && miaoCheck.isSelected()
                && AppVariables.autoTestingController == null) {
            SoundTools.miao3();
        }
        afterTask(ok);
        if (ok && closeAfterCheck != null && closeAfterCheck.isSelected()) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        close();
                    });
                }
            }, 500);
        }
    }

    public void handleTargetFiles() {
        recordTargetFiles();
        if (openCheck != null && openCheck.isSelected()) {
            openTarget();
        }
    }

    public void afterTask(boolean ok) {
    }

    public void cancelTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        if (task != null && !task.isQuit()) {
            cancelTask();
        } else {
            close();
        }
    }

    protected void taskCanceled() {
        taskCancelled = true;
        showLogs(message("Cancelled"));
    }

    @FXML
    @Override
    public void openTarget() {
        File path = targetPath;
        if (path == null || !path.exists()) {
            if (targetPathController != null) {
                path = targetPathController.pickFile();
            } else if (targetFile != null) {
                path = targetFile.getParentFile();
            } else if (targetFiles != null) {
                for (int type : targetFiles.keySet()) {
                    List<File> files = targetFiles.get(type);
                    if (files == null || files.isEmpty()) {
                        continue;
                    }
                    path = files.get(0).getParentFile();
                    break;
                }
            }
            if (path == null || !path.exists()) {
                return;
            }
        }
        browseURI(path.toURI());
        recordFileOpened(path);
    }

    public boolean targetFileGenerated(File target) {
        return targetFileGenerated(target, TargetFileType);
    }

    public boolean targetFileGenerated(File target, int type) {
        if (target == null || !target.exists()) {
            return false;
        }
        if (targetFiles == null) {
            targetFilesCount = 0;
            targetFiles = new LinkedHashMap<>();
        }
        putTargetFile(target, type);
        showLogs(MessageFormat.format(message("FilesGenerated"), lastTargetName) + " "
                + message("Size") + ": " + FileTools.showFileSize(target.length()));
        return true;
    }

    public File lastTargetFile() {
        if (lastTargetName != null) {
            return new File(lastTargetName);
        } else if (targetFile != null) {
            return targetFile;
        } else if (targetFileController != null) {
            return targetFileController.pickFile();
        }
        return null;
    }

    public void putTargetFile(File target, int type) {
        try {
            targetFilesCount++;
            lastTargetName = target.getAbsolutePath();
            List<File> files = targetFiles.get(type);
            if (files == null) {
                files = new ArrayList<>();
            }
            files.add(target);
            int size = files.size();
            if (size > AppVariables.fileRecentNumber) {
                files = files.subList(size - AppVariables.fileRecentNumber, size);
            }
            targetFiles.put(type, files);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void putTargetFile(List<File> tfiles, int type) {
        try {
            if (tfiles == null || tfiles.isEmpty()) {
                return;
            }
            int size = tfiles.size();
            targetFilesCount += size;
            lastTargetName = tfiles.get(size - 1).getAbsolutePath();
            List<File> files = targetFiles.get(type);
            if (files == null) {
                files = new ArrayList<>();
            }
            files.addAll(tfiles);
            size = files.size();
            if (size > AppVariables.fileRecentNumber) {
                files = files.subList(size - AppVariables.fileRecentNumber, size);
            }
            targetFiles.put(type, files);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void recordTargetFiles() {
        if (targetFiles != null && !targetFiles.isEmpty()) {
            try (Connection conn = DerbyBase.getConnection()) {
                for (int type : targetFiles.keySet()) {
                    List<File> files = targetFiles.get(type);
                    if (files == null) {
                        continue;
                    }
                    int size = files.size();
                    if (size > AppVariables.fileRecentNumber) {
                        files = files.subList(size - AppVariables.fileRecentNumber, size);
                    }
                    for (File file : files) {
                        recordFileWritten(conn, file, type, type);
                    }
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
        }
    }

    @Override
    public boolean controlAltL() {
        if (tabPane != null && logsTab != null && clearButton != null) {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == logsTab) {
                clearLogs();
                return true;
            }
        }
        return super.controlAltL();
    }

    @Override
    public void cleanPane() {
        try {
            cancelTask();
            taskCancelled = true;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
