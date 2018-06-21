package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:48:15
 * @Description
 * @License Apache License Version 2.0
 */
public class MyBoxController extends BaseController {

    @Override
    protected void initializeNext() {
    }

    @FXML
    private void pdfTools() {
        reloadStage(CommonValues.PdfConvertPictureFxml, AppVaribles.getMessage("PdfConvertPictures"));
    }

    @FXML
    private void imageTools() {
        reloadStage(CommonValues.ImageViewerFxml, AppVaribles.getMessage("ImageViewer"));
    }

    @FXML
    private void fileTools() {
        popInformation(AppVaribles.getMessage("Developing..."));
    }

    @FXML
    private void setEnglish(MouseEvent event) {
        AppVaribles.CurrentBundle = CommonValues.BundleEnUS;
        reloadStage(myFxml);
    }

    @FXML
    private void setChinese(MouseEvent event) {
        AppVaribles.CurrentBundle = CommonValues.BundleZhCN;
        reloadStage(myFxml);
    }

    @FXML
    private void showAbout(MouseEvent event) {
        openStage(CommonValues.AboutFxml, true);
    }
}
