package mara.mybox.controller;

import mara.mybox.controller.base.BaseController;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import static mara.mybox.value.AppVaribles.logger;
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
    private Label infoLabel;
    @FXML
    private TextArea text;

    public void init(final Task<?> task) {
        try {
            progressIndicator.setProgress(-1F);
//            progressIndicator.progressProperty().bind(task.progressProperty());
            loadingTask = task;
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void mybox(ActionEvent event) {
        openStage(CommonValues.MyboxFxml);
    }

    @FXML
    private void cancelAction() {
        if (loadingTask != null) {
            loadingTask.cancel();
            loadingTask = null;
        }
        parentController.taskCanceled(loadingTask);

    }

    public void setInfo(String info) {
        if (loadingTask == null || !loadingTask.isRunning()) {
            return;
        }
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

}
