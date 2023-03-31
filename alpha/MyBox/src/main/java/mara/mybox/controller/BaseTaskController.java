package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-5-7
 * @License Apache License Version 2.0
 */
public class BaseTaskController extends BaseLogs {

    protected boolean cancelled, successed;
    protected Date startTime, endTime;
    protected LinkedHashMap<Integer, List<File>> targetFiles;
    protected String lastTargetName;
    protected int targetFilesCount;

    @FXML
    protected Tab logsTab;
    @FXML
    protected CheckBox miaoCheck, openCheck;

    public BaseTaskController() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (miaoCheck != null) {
                miaoCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean("Miao", newValue);
                    }
                });
                miaoCheck.setSelected(UserConfig.getBoolean("Miao", true));
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
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
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            if (startButton != null) {
                StyleTools.setNameIcon(startButton, message("Stop"), "iconStop.png");
                startButton.applyCss();
                startButton.setUserData("started");
            }
            if (tabPane != null && logsTab != null) {
                tabPane.getSelectionModel().select(logsTab);
            }
            beforeTask();
            startTask();
        }
    }

    public void beforeTask() {
        cancelled = false;
        successed = false;
        targetFilesCount = 0;
        targetFiles = new LinkedHashMap<>();
        initLogs();
    }

    public void startTask() {
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                startTime = new Date();
                return doTask();

            }

            @Override
            protected void whenSucceeded() {
                successed = true;
                afterSuccess();
            }

            @Override
            protected void whenCanceled() {
                taskCanceled();
            }

            @Override
            protected void finalAction() {
                super.taskQuit();
                task = null;
                if (startButton != null) {
                    StyleTools.setNameIcon(startButton, message("Start"), "iconStart.png");
                    startButton.applyCss();
                    startButton.setUserData(null);
                }
                updateLogs(message("Completed") + " " + message("Cost")
                        + " " + DateTools.datetimeMsDuration(endTime, startTime), true);
                afterTask();
            }
        };
        start(task, false, null);
    }

    public boolean doTask() {
        return true;
    }

    public void afterSuccess() {

    }

    public void cancelTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @Override
    public void cancelAction() {
        if (task != null && !task.isQuit()) {
            cancelTask();
        } else {
            close();
        }
    }

    protected void taskCanceled() {
        cancelled = true;
        showLogs(message("Cancel"));
    }

    @FXML
    public void openPath() {
        File path = targetPath;
        if (path == null || !path.exists()) {
            if (targetPathController != null) {
                path = targetPathController.file();
            } else if (targetFile != null) {
                path = targetFile.getParentFile();
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
                + message("Size") + ": " + target.length());
        return true;
    }

    public void afterTask() {
        recordTargetFiles();
        if (miaoCheck != null && miaoCheck.isSelected()) {
            SoundTools.miao3();
        }
    }

    public File lastTargetFile() {
        if (lastTargetName != null) {
            return new File(lastTargetName);
        } else if (targetFile != null) {
            return targetFile;
        } else if (targetFileController != null) {
            return targetFileController.file();
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
    public void cleanPane() {
        try {
            cancelTask();
            cancelled = true;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
