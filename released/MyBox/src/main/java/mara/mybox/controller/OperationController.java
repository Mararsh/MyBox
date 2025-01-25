/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;

/**
 * FXML Controller class
 *
 * @author mara
 */
public class OperationController extends BaseController {

    @FXML
    protected HBox barBox;
    @FXML
    protected Button pauseButton, openTargetButton;
    @FXML
    protected ProgressBar progressBar, fileProgressBar;
    @FXML
    protected Label progressValue, fileProgressValue;

    public void deleteOpenControls() {
        openCheck.setSelected(false);
        barBox.getChildren().removeAll(openCheck, openTargetButton);
    }

    @FXML
    @Override
    public void startAction() {
        if (parentController != null) {
            parentController.startAction();
        }
    }

    @FXML
    public void pauseProcess(ActionEvent event) {
        if (parentController != null) {
            parentController.startAction();
        }
    }

    @FXML
    @Override
    public void openTarget() {
        if (parentController != null) {
            parentController.openTarget();
        }
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
