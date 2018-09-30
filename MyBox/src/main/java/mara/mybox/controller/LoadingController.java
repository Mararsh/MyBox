package mara.mybox.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 8:14:06
 * @Description
 * @License Apache License Version 2.0
 */
public class LoadingController {

    private static final Logger logger = LogManager.getLogger();
    private Stage myStage;

    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label infoLabel;
    @FXML
    private TextArea text;

    public void init(final Task<?> task) {
        try {
            progressIndicator.setProgress(-1F);
            progressIndicator.progressProperty().bind(task.progressProperty());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void setInfo(String info) {
        infoLabel.setText(info);
    }

    public Stage getMyStage() {
        return myStage;
    }

    public void setMyStage(Stage myStage) {
        this.myStage = myStage;
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

}
