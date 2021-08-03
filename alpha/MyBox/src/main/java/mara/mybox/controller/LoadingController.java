package mara.mybox.controller;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 8:14:06
 * @License Apache License Version 2.0
 */
public class LoadingController extends BaseController {

    private Task<?> loadingTask;
    private boolean isCanceled;

    @FXML
    protected ProgressIndicator progressIndicator;
    @FXML
    protected Label infoLabel, timeLabel;
    @FXML
    protected TextArea text;

    public LoadingController() {
        baseTitle = Languages.message("LoadingPage");
    }

    public void init(final Task<?> task) {
        try {
            infoLabel.setText(Languages.message("Handling..."));
            infoLabel.requestFocus();
            loadingTask = task;
            isCanceled = false;
            progressIndicator.setProgress(-1F);
            if (timeLabel != null) {
                showTimer();
            }
            getMyStage().toFront();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void showTimer() {
        try {
            if (timer != null) {
                timer.cancel();
            }
            final Date startTime = new Date();
            final String prefix = Languages.message("StartTime") + ": " + DateTools.nowString()
                    + "   " + Languages.message("ElapsedTime") + ": ";
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
                }
            }, 0, 1000);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        isCanceled = true;
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
        this.closeStage();
    }

    public void setInfo(String info) {
        Platform.runLater(() -> {
            infoLabel.setText(info);
            infoLabel.setWrapText(true);
            infoLabel.setMinHeight(Region.USE_PREF_SIZE);
            infoLabel.applyCss();
        });

    }

    public boolean isRunning() {
        return timer != null;
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

    public Label getInfoLabel() {
        return infoLabel;
    }

    public void setInfoLabel(Label infoLabel) {
        this.infoLabel = infoLabel;
    }

    public TextArea getText() {
        return text;
    }

    public void setText(TextArea text) {
        this.text = text;
    }

    public Task<?> getLoadingTask() {
        return loadingTask;
    }

    public void setLoadingTask(Task<?> loadingTask) {
        this.loadingTask = loadingTask;
    }

    public boolean isIsCanceled() {
        return isCanceled;
    }

    public void setIsCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }

    public Label getTimeLabel() {
        return timeLabel;
    }

    public void setTimeLabel(Label timeLabel) {
        this.timeLabel = timeLabel;
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (loadingTask != null && !loadingTask.isDone()) {
            loadingTask.cancel();
            loadingTask = null;
        }
        return true;
    }
}
