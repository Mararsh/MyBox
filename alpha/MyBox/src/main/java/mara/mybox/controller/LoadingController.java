package mara.mybox.controller;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 8:14:06
 * @License Apache License Version 2.0
 */
public class LoadingController extends BaseLogsController {

    private Task<?> loadingTask;
    protected SimpleBooleanProperty canceled;

    @FXML
    protected ProgressIndicator progressIndicator;
    @FXML
    protected Label timeLabel;

    public LoadingController() {
        canceled = new SimpleBooleanProperty();
    }

    public void init(final Task<?> task) {
        try {
            loadingTask = task;
            canceled.set(false);
            progressIndicator.setProgress(-1F);
            if (timeLabel != null) {
                showTimer();
            }
            getMyStage().toFront();
            if (task != null && (task instanceof FxTask)) {
                FxTask stask = (FxTask) task;
                setTitle(stask.getController().getTitle());
                setInfo(getTitle());
            } else {
                setInfo(message("Handling..."));
            }
            logsTextArea.requestFocus();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void showTimer() {
        try {
            if (timer != null) {
                timer.cancel();
            }
            final Date startTime = new Date();
            final String prefix = message("StartTime") + ": " + DateTools.nowString()
                    + "   " + message("ElapsedTime") + ": ";
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        if (loadingTask != null && loadingTask.isCancelled()) {
                            cancelAction();
                            return;
                        }
                        timeLabel.setText(prefix + DateTools.datetimeMsDuration(new Date(), startTime));
                    });
                    Platform.requestNextPulse();
                }
            }, 0, 1000);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        clear();
        closeStage();
    }

    public void clear() {
        canceled.set(true);
        if (loadingTask != null) {
            if (parentController != null) {
                parentController.taskCanceled(loadingTask);
            }
            loadingTask.cancel();
            loadingTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void setInfo(String info) {
        updateLogs(info, true);
    }

    public String getInfo() {
        return logsTextArea.getText();
    }

    public boolean isRunning() {
        return timer != null;
    }

    public boolean canceled() {
        return canceled != null && canceled.get();
    }

    public void setProgress(float value) {
        if (loadingTask == null || loadingTask.isDone()) {
            return;
        }
        progressIndicator.setProgress(value);
    }

    public ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    public void setProgressIndicator(ProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
    }

    public Task<?> getLoadingTask() {
        return loadingTask;
    }

    public void setLoadingTask(Task<?> loadingTask) {
        this.loadingTask = loadingTask;
    }

    public Label getTimeLabel() {
        return timeLabel;
    }

    public void setTimeLabel(Label timeLabel) {
        this.timeLabel = timeLabel;
    }

    @Override
    public void cleanPane() {
        try {
            clear();
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
