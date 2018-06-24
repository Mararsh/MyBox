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
import mara.mybox.objects.ImageFileInformation;

/**
 * FXML Controller class
 *
 * @author mara
 */
public class ImageMetaDataController extends BaseController {

    @FXML
    private TextField fileInput;
    @FXML
    private TextArea metaDataInput;

    @FXML
    private void closeStage(MouseEvent event) {
        try {
            getMyStage().close();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadData(ImageFileInformation info) {
        try {
            if (info == null || info.getFile() == null) {
                return;
            }
            fileInput.setText(info.getFile().getPath());
            metaDataInput.setText(info.getMetaData());

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
