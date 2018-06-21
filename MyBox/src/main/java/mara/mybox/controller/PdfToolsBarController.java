/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import mara.mybox.objects.CommonValues;

/**
 * FXML Controller class
 *
 * @author mara
 */
public class PdfToolsBarController extends BaseController {

    @FXML
    private HBox pdfToolsBarPane;
    @FXML
    private Label barTips;

    @FXML
    void showTips(MouseEvent event) {
        barTips.setVisible(true);
    }

    @FXML
    void hideTips(MouseEvent event) {
        barTips.setVisible(false);
    }

    @FXML
    private void callConvertPicturesBatch(MouseEvent event) {
        MouseButton button = event.getButton();
        switch (button) {
            case SECONDARY:
                openStage(CommonValues.PdfConvertPictureBatchFxml, false);
                break;
            case PRIMARY:
            case MIDDLE:
            default:
                reloadStage(CommonValues.PdfConvertPictureBatchFxml);
        }
    }

    @FXML
    private void callConvertPictures(MouseEvent event) {
        MouseButton button = event.getButton();
        switch (button) {
            case SECONDARY:
                openStage(CommonValues.PdfConvertPictureFxml, false);
                break;
            case PRIMARY:
            case MIDDLE:
            default:
                reloadStage(CommonValues.PdfConvertPictureFxml);
        }
    }

}
