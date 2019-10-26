package mara.mybox.controller;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 8:14:06
 * @Description
 * @License Apache License Version 2.0
 */
public class LoadingController extends BaseController {

    private Task<?> loadingTask;

    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label infoLabel, timeLabel;
    @FXML
    private TextArea text;

    public LoadingController() {
        baseTitle = AppVariables.message("LoadingPage");
    }

    public void init(final Task<?> task) {
        try {
            loadingTask = task;
            if (task != null) {
                progressIndicator.setProgress(-1F);
                if (timeLabel != null) {
                    showTimer();
                }
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void showTimer() {
        try {
            if (timer != null) {
                timer.cancel();
            }
            final long startTime = new Date().getTime();
            final String prefix = AppVariables.message("StartTime") + ": " + DateTools.nowString()
                    + "   " + AppVariables.message("ElapsedTime") + ": ";
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (loadingTask != null && loadingTask.isCancelled()) {
                                cancelAction();
                                return;
                            }
                            long d = new Date().getTime() - startTime;
                            timeLabel.setText(prefix + DateTools.showTime(d));
                        }
                    });
                }
            }, 0, 1000);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void mybox(ActionEvent event) {
        openStage(CommonValues.MyboxFxml);
    }

    @FXML
    @Override
    public void cancelAction() {
        if (loadingTask != null) {
            if (parentController != null) {
                parentController.taskCanceled(loadingTask);
            }
            loadingTask.cancel();
            loadingTask = null;
        }
        if (timer != null) {
            timer.cancel();
        }
        this.closeStage();

    }

    public void setInfo(String info) {
//        if (loadingTask == null || !loadingTask.isRunning()) {
//            return;
//        }
        infoLabel.setText(info);
    }

    public void setProgress(float value) {
        if (loadingTask == null || !loadingTask.isRunning()) {
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

    @Override
    public boolean checkBeforeNextAction() {
        if (loadingTask != null && loadingTask.isRunning()) {
            loadingTask.cancel();
            loadingTask = null;
        }
        return true;
    }

}
