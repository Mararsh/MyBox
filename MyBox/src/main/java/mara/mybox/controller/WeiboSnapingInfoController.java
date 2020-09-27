package mara.mybox.controller;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2018-9-16
 * @Description
 * @License Apache License Version 2.0
 */
public class WeiboSnapingInfoController extends BaseController {

    private WeiboSnapRunController parent;

    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label infoLabel;
    @FXML
    private TextArea infoText, errorText;
    @FXML
    private Button stopButton, reloadButton;

    public WeiboSnapingInfoController() {
        baseTitle = AppVariables.message("WeiboSnap");

    }

    public void init(final Task<?> task) {
        try {
            progressIndicator.setProgress(-1F);
            progressIndicator.progressProperty().bind(task.progressProperty());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void setInfo(String str) {
        infoLabel.setText(str);
    }

    public void setText(String str) {
        infoText.setText(str);
    }

    public String getText() {
        return infoText.getText();
    }

    public void addText(String str) {
        infoText.setStyle("-fx-font-size: 1em;");
        infoText.setText(infoText.getText().concat(str));
    }

    public void addLine(String str) {
        if (infoText.getText().trim().isEmpty()) {
            infoText.setText(str);
        } else {
            infoText.setText(infoText.getText().concat("\n" + str));
        }
    }

    public void showError(String str) {
        errorText.setStyle("-fx-text-fill: #961c1c; -fx-font-size: 1em;");
        errorText.setText(str);
    }

    public void showMem(String str) {
        errorText.setStyle("-fx-text-fill: #2e598a; -fx-font-size: 1em; ");
        errorText.setText(str);
    }

    @FXML
    private void stopAction(ActionEvent event) {
        parent.endSnap();
    }

    @FXML
    private void openPath(ActionEvent event) {
        parent.openPath();
    }

    @FXML
    private void reloadAction(ActionEvent event) {
        parent.reloadPage();
    }

    public WeiboSnapRunController getParent() {
        return parent;
    }

    public void setParent(final WeiboSnapRunController parent) {
        this.parent = parent;
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

    public TextArea getInfoText() {
        return infoText;
    }

    public void setInfoText(TextArea infoText) {
        this.infoText = infoText;
    }

    public void setThisPane(VBox thisPane) {
        this.thisPane = thisPane;
    }

    public Button getStopButton() {
        return stopButton;
    }

    public void setStopButton(Button stopButton) {
        this.stopButton = stopButton;
    }

    public Button getReloadButton() {
        return reloadButton;
    }

    public void setReloadButton(Button reloadButton) {
        this.reloadButton = reloadButton;
    }

}
