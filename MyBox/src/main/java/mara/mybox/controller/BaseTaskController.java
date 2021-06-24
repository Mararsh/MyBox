package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-5-7
 * @License Apache License Version 2.0
 */
public class BaseTaskController extends BaseController {

    protected int logsMaxLines, logsTotalLines, logsCacheLines = 200;
    protected boolean cancelled;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab optionsTab, logsTab;
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
        super.initControls();
        initLogs();
    }

    public boolean checkOptions() {
        return true;
    }

    @FXML
    @Override
    public void startAction() {
        if (!checkOptions()) {
            return;
        }
        if (startButton.getUserData() != null) {
            ControlStyle.setNameIcon(startButton, message("Start"), "iconStart.png");
            startButton.applyCss();
            startButton.setUserData(null);
            cancelAction();
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            initLogs();
            ControlStyle.setNameIcon(startButton, message("Stop"), "iconStart.png");
            startButton.applyCss();
            startButton.setUserData("started");
            if (tabPane != null && logsTab != null) {
                tabPane.getSelectionModel().select(logsTab);
            }
            startTask();
        }

    }

    public void startTask() {
        task = new SingletonTask<Void>() {

            @Override
            protected boolean handle() {
                return doTask();

            }

            @Override
            protected void whenSucceeded() {
                afterSuccess();
            }

            @Override
            protected void whenCanceled() {
                updateLogs(message("Cancel"));
            }

            @Override
            protected void finalAction() {
                ControlStyle.setNameIcon(startButton, message("Start"), "iconStart.png");
                startButton.applyCss();
                startButton.setUserData(null);
                updateLogs(message("Completed") + " " + message("Cost")
                        + " " + DateTools.datetimeMsDuration(new Date(), startTime));
            }
        };
        task.setSelf(task);
        Thread thread = new Thread(task);
        thread.setDaemon(false);
        thread.start();
    }

    protected boolean doTask() {
        return true;
    }

    protected void afterSuccess() {
    }

    @Override
    public void cancelAction() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @FXML
    protected void initLogs() {
        logsTextArea.setText("");
        logsTotalLines = 0;
        if (maxLinesinput != null) {
            try {
                logsMaxLines = Integer.parseInt(maxLinesinput.getText());
            } catch (Exception e) {
                logsMaxLines = 5000;
            }
        }
    }

    protected void updateLogs(final String line) {
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
    protected void clearLogs() {
        initLogs();
    }

    protected boolean targetFileGenerated(File target) {
        if (target == null || !target.exists() || target.length() == 0) {
            return false;
        }
        updateLogs(MessageFormat.format(message("FilesGenerated"), target.getAbsolutePath()));
        recordFileWritten(target);
        return true;
    }

    @Override
    public boolean leavingScene() {
        cancelAction();
        cancelled = true;
        return super.leavingScene();
    }

}
