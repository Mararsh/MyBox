/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-15
 * @Description
 * @License Apache License Version 2.0
 */
public class TitleBarController extends BaseController {

    @FXML
    private Pane titleBarPane;
    @FXML
    private Label title;

    @Override
    public Stage getThisStage() {
        if (thisStage == null) {
            if (titleBarPane != null && titleBarPane.getScene() != null) {
                thisStage = (Stage) titleBarPane.getScene().getWindow();
            }
        }
        return thisStage;
    }

    @Override
    protected void initStage2() {
        setTitle(AppVaribles.getMessage("AppTitle"));
    }

    @FXML
    private void mybox(MouseEvent event) {
        reloadInterface(CommonValues.MyboxInterface);
    }

    public void setTitle(String t) {
        title.setText(t);
    }

}
