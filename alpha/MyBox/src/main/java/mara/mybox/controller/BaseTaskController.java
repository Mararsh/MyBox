package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.DateTools;
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
    protected LinkedHashMap<File, Integer> targetFiles;
    protected String lastTargetName;

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
        if (task != null) {
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
            targetFiles = new LinkedHashMap<>();
        }
        targetFiles.put(target, type);
        lastTargetName = target.getAbsolutePath();
        showLogs(MessageFormat.format(message("FilesGenerated"), lastTargetName) + " "
                + message("Size") + ": " + target.length());
        return true;
    }

    public void afterTask() {
        recordTargetFiles();
    }

    public void recordTargetFiles() {
        if (targetFiles != null && !targetFiles.isEmpty()) {
            try (Connection conn = DerbyBase.getConnection()) {
                for (File file : targetFiles.keySet()) {
                    int type = targetFiles.get(file);
                    recordFileWritten(conn, file, type, type);
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
