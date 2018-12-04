/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.ImageInformation;

/**
 * @Author Mara
 * @CreateDate 2018-6-21
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageMetaDataController extends BaseController {

    @FXML
    private TextField fileInput;
    @FXML
    private TextArea metaDataInput;

    @FXML
    private void closeStage(MouseEvent event) {
        try {
            closeStage();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadData(ImageInformation info) {
        try {
            if (info == null || info.getFilename() == null) {
                return;
            }
            fileInput.setText(info.getFilename());
            metaDataInput.setText(info.getMetaData());

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
