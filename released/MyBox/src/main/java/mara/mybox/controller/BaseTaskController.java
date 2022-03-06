package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
public class BaseTaskController extends BaseController {

    protected int logsMaxLines, logsTotalLines, logsCacheLines = 200;
    protected boolean cancelled, successed;
    protected Date startTime, endTime;

    @FXML
    protected Tab logsTab;
    @FXML
    protected CheckBox verboseCheck;
    @FXML
    protected TextArea logsTextArea;
    @FXML
    protected TextField maxLinesinput;

    public BaseTaskController() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            logsMaxLines = UserConfig.getInt("TaskMaxLinesNumber", 5000);
            if (logsMaxLines <= 0) {
                logsMaxLines = 5000;
            }
            if (maxLinesinput != null) {
                maxLinesinput.setText(logsMaxLines + "");
                maxLinesinput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> v, String ov, String nv) {
                        try {
                            int iv = Integer.parseInt(maxLinesinput.getText());
                            if (iv > 0) {
                                logsMaxLines = iv;
                                maxLinesinput.setStyle(null);
                                UserConfig.setInt("TaskMaxLinesNumber", logsMaxLines);
                            } else {
                                maxLinesinput.setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            maxLinesinput.setStyle(UserConfig.badStyle());
                        }
                    }
                });

            }

            initLogs();
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
        if (startButton.getUserData() != null) {
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
            StyleTools.setNameIcon(startButton, message("Stop"), "iconStop.png");
            startButton.applyCss();
            startButton.setUserData("started");
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
                StyleTools.setNameIcon(startButton, message("Start"), "iconStart.png");
                startButton.applyCss();
                startButton.setUserData(null);
                updateLogs(message("Completed") + " " + message("Cost")
                        + " " + DateTools.datetimeMsDuration(endTime, startTime));
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
    public void initLogs() {
        if (logsTextArea == null) {
            return;
        }
        logsTextArea.setText("");
        logsTotalLines = 0;
    }

    public void updateLogs(final String line) {
        try {
            if (logsTextArea == null) {
                return;
            }
            Platform.runLater(() -> {
                String s = DateTools.datetimeToString(new Date()) + "  " + line + "\n";
                logsTextArea.appendText(s);
                logsTotalLines++;
                if (logsTotalLines > logsMaxLines + logsCacheLines) {
                    logsTextArea.deleteText(0, 1);
                }
            });
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void clearLogs() {
        initLogs();
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
