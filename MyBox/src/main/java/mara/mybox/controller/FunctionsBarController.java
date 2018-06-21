/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-15
 * @Description
 * @License Apache License Version 2.0
 */
public class FunctionsBarController extends BaseController {

    @FXML
    private Pane functionsBarPane;

    @FXML
    private void pdfTools() {
        reloadStage(CommonValues.PdfConvertPictureFxml);
    }

    @FXML
    private void imageTools() {
        popInformation(AppVaribles.getMessage("Developing..."));
    }

    @FXML
    private void fileTools() {
        popInformation(AppVaribles.getMessage("Developing..."));
    }

}
