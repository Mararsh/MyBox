/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * @Author Mara
 * @CreateDate 2018-6-15
 * @Description
 * @License Apache License Version 2.0
 */
public class FunctionsBarController extends BaseController {

    @FXML
    private Pane functionsBarPane;

    @Override
    public Stage getThisStage() {
        if (thisStage == null) {
            if (functionsBarPane != null && functionsBarPane.getScene() != null) {
                thisStage = (Stage) functionsBarPane.getScene().getWindow();
            }
        }
        return thisStage;
    }

}
