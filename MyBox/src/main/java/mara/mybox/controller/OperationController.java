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
import mara.mybox.controller.base.BaseController;
import mara.mybox.value.AppVaribles;

/**
 * FXML Controller class
 *
 * @author mara
 */
public class OperationController extends BaseController {

    @FXML
    public Button pauseButton, openTargetButton;
    @FXML
    public ProgressBar progressBar, fileProgressBar;
    @FXML
    public Label progressValue, fileProgressValue;
    @FXML
    public CheckBox miaoCheck, openCheck;

    @Override
    public void initializeNext() {

        if (miaoCheck != null) {
            miaoCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVaribles.setUserConfigValue("Miao", newValue);

                }
            });
            miaoCheck.setSelected(AppVaribles.getUserConfigBoolean("Miao"));
        }

        if (openCheck != null) {
            openCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVaribles.setUserConfigValue("OpenWhenComplete", newValue);

                }
            });
            openCheck.setSelected(AppVaribles.getUserConfigBoolean("OpenWhenComplete"));
        }

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
    public void openTarget(ActionEvent event) {

        if (parentController != null) {

            parentController.openTarget(event);
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
