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
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.StyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.value.Languages;

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
        if (startButton.getUserData() != null) {
            StyleTools.setNameIcon(startButton, Languages.message("Start"), "iconStart.png");
            startButton.applyCss();
            startButton.setUserData(null);
            cancelAction();
            return;
        }
        if (!checkOptions()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            initLogs();
            StyleTools.setNameIcon(startButton, Languages.message("Stop"), "iconStart.png");
            startButton.applyCss();
            startButton.setUserData("started");
            if (tabPane != null && logsTab != null) {
                tabPane.getSelectionModel().select(logsTab);
            }
            beforeTask();
            startTask();
        }

    }

    protected void beforeTask() {
    }

    public void startTask() {
        task = new SingletonTask<Void>(this) {

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
                updateLogs(Languages.message("Cancel"));
            }

            @Override
            protected void finalAction() {
                task = null;
                StyleTools.setNameIcon(startButton, Languages.message("Start"), "iconStart.png");
                startButton.applyCss();
                startButton.setUserData(null);
                updateLogs(Languages.message("Completed") + " " + Languages.message("Cost")
                        + " " + DateTools.datetimeMsDuration(new Date(), startTime));
                afterTask();
            }
        };
        start(task, false, null);
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

    protected void afterTask() {

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
        updateLogs(MessageFormat.format(Languages.message("FilesGenerated"), target.getAbsolutePath()));
        recordFileWritten(target);
        return true;
    }

    @Override
    public void cleanPane() {
        try {
            cancelAction();
            cancelled = true;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
