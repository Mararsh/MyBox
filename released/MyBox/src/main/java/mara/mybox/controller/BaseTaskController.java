package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-5-7
 * @License Apache License Version 2.0
 */
public class BaseTaskController extends BaseLogs {

    protected boolean cancelled, successed;
    protected Date startTime, endTime;

    @FXML
    protected Tab logsTab;

    public BaseTaskController() {
    }

    public boolean checkOptions() {
        return true;
    }

    @FXML
    @Override
    public void startAction() {
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
                cancelled = true;
                updateLogs(message("Cancel"));
            }

            @Override
            protected void finalAction() {
                endTime = new Date();
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

    public void afterTask() {

    }

    @FXML
    public void openPath() {
        if (targetPath == null || !targetPath.exists()) {
            return;
        }
        browseURI(targetPath.toURI());
        recordFileOpened(targetPath);
    }

    public boolean targetFileGenerated(File target) {
        return targetFileGenerated(target, TargetFileType, true);
    }

    public boolean targetFileGenerated(File target, boolean record) {
        return targetFileGenerated(target, TargetFileType, record);
    }

    public boolean targetFileGenerated(File target, int type) {
        return targetFileGenerated(target, type, true);
    }

    public boolean targetFileGenerated(File target, int type, boolean record) {
        if (target == null || !target.exists() || target.length() == 0) {
            return false;
        }
        updateLogs(MessageFormat.format(message("FilesGenerated"), target.getAbsolutePath()));
        if (record) {
            recordFileWritten(target, type, type);
        }
        return true;
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
