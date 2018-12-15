/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import mara.mybox.fxml.FxmlTools;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;

/**
 * FXML Controller class
 *
 * @author mara
 */
public class OperationController extends BaseController {

    @FXML
    protected Button startButton;
    @FXML
    protected Button pauseButton;
    @FXML
    protected ProgressBar progressBar;
    @FXML
    protected Label progressValue;
    @FXML
    protected Button openTargetButton;
    @FXML
    protected ProgressBar fileProgressBar;
    @FXML
    protected Label fileProgressValue;
    @FXML
    protected CheckBox miaoCheck;

    @Override
    protected void initializeNext() {
        Tooltip tips = new Tooltip(getMessage("MiaoPrompt"));
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(miaoCheck, tips);

        miaoCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                AppVaribles.setUserConfigValue("Miao", newValue);

            }
        });
        miaoCheck.setSelected(AppVaribles.getUserConfigBoolean("Miao"));

    }

    @FXML
    @Override
    protected void startProcess(ActionEvent event) {
        if (parentController != null) {
            parentController.startProcess(event);
        }
    }

    @FXML
    @Override
    protected void pauseProcess(ActionEvent event) {
        if (parentController != null) {
            parentController.startProcess(event);
        }
    }

    @FXML
    @Override
    protected void openTarget(ActionEvent event) {
        if (parentController != null) {
            parentController.openTarget(event);
        }
    }

    public Button getStartButton() {
        return startButton;
    }

    public void setStartButton(Button startButton) {
        this.startButton = startButton;
    }

    public Button getPauseButton() {
        return pauseButton;
    }

    public void setPauseButton(Button pauseButton) {
        this.pauseButton = pauseButton;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public Label getProgressValue() {
        return progressValue;
    }

    public void setProgressValue(Label progressValue) {
        this.progressValue = progressValue;
    }

    public Button getOpenTargetButton() {
        return openTargetButton;
    }

    public void setOpenTargetButton(Button openTargetButton) {
        this.openTargetButton = openTargetButton;
    }

    public ProgressBar getFileProgressBar() {
        return fileProgressBar;
    }

    public void setFileProgressBar(ProgressBar fileProgressBar) {
        this.fileProgressBar = fileProgressBar;
    }

    public Label getFileProgressValue() {
        return fileProgressValue;
    }

    public void setFileProgressValue(Label fileProgressValue) {
        this.fileProgressValue = fileProgressValue;
    }

    public CheckBox getMiaoCheck() {
        return miaoCheck;
    }

    public void setMiaoCheck(CheckBox miaoCheck) {
        this.miaoCheck = miaoCheck;
    }

}
