package mara.mybox.controller;

import java.util.Date;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-5-7
 * @License Apache License Version 2.0
 */
public class DataTaskController extends BaseController {

    protected int logsMaxLines, logsTotalLines, logsCacheLines = 200;
    protected boolean cancelled;
    protected String cancelName;

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

    public DataTaskController() {
        cancelName = "Cancel";
    }

    @Override
    public void initializeNext() {
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
        if (message(cancelName).equals(startButton.getText())) {
            cancelAction();
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            initLogs();

            startButton.setText(message(cancelName));
            tabPane.getSelectionModel().select(logsTab);
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
                    updateLogs(message(cancelName), true);
                }

                @Override
                protected void finalAction() {
                    startButton.setText(message("Start"));
                    updateLogs(message("Completed") + " " + message("Cost")
                            + " " + DateTools.datetimeMsDuration(new Date(), startTime),
                            true);
                }
            };
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
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

    protected void updateLogs(final String line, boolean mustWrite) {
        try {
            Platform.runLater(() -> {
                logsTotalLines++;
                if (mustWrite
                        || (verboseCheck != null && verboseCheck.isSelected())
                        || logsTotalLines % 100 == 0) {
                    String s = DateTools.datetimeToString(new Date()) + "  " + line + "\n";
                    logsTextArea.appendText(s);
                    if (logsTotalLines > logsMaxLines + logsCacheLines) {
                        initLogs();
                    }
                }
            });
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @FXML
    protected void clearLogs() {
        initLogs();
    }

    @Override
    public boolean leavingScene() {
        cancelAction();
        cancelled = true;
        return super.leavingScene();
    }

}
