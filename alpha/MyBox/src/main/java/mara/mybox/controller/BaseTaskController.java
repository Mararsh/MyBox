package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
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

    protected void taskCanceled() {
        cancelled = true;
        showLogs(message("Cancel"));
    }

    public void afterTask() {

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
