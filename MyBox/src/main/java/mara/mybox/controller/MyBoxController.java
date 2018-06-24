package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:48:15
 * @Description
 * @License Apache License Version 2.0
 */
public class MyBoxController extends BaseController {

    private ContextMenu pdfMenus, imageMenu, fileMenu;

    @FXML
    private VBox imageBox;

    @FXML
    private VBox pdfBox;

    @FXML
    private VBox fileBox;

    @Override
    protected void initializeNext() {

        pdfMenus = new ContextMenu();
        MenuItem pdfExtractImages = new MenuItem(AppVaribles.getMessage("PdfExtractImages"));
        pdfExtractImages.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.PdfExtractImagesFxml, AppVaribles.getMessage("PdfExtractImages"));
            }
        });
        MenuItem pdfExtractImagesBatch = new MenuItem(AppVaribles.getMessage("PdfExtractImagesBatch"));
        pdfExtractImagesBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.PdfExtractImagesBatchFxml, AppVaribles.getMessage("PdfExtractImagesBatch"));
            }
        });
        MenuItem pdfConvertImages = new MenuItem(AppVaribles.getMessage("PdfConvertImages"));
        pdfConvertImages.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.PdfConvertImagesFxml, AppVaribles.getMessage("PdfConvertImages"));
            }
        });
        MenuItem pdfConvertImagesBatch = new MenuItem(AppVaribles.getMessage("PdfConvertImagesBatch"));
        pdfConvertImagesBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.PdfConvertImagesBatchFxml, AppVaribles.getMessage("PdfConvertImagesBatch"));
            }
        });
        pdfMenus.getItems().add(pdfExtractImages);
        pdfMenus.getItems().add(pdfExtractImagesBatch);
        pdfMenus.getItems().add(pdfConvertImages);
        pdfMenus.getItems().add(pdfConvertImagesBatch);

        imageMenu = new ContextMenu();
        MenuItem imageViewer = new MenuItem(AppVaribles.getMessage("ImageViewer"));
        imageViewer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImageViewerFxml, AppVaribles.getMessage("ImageViewer"));
            }
        });
        imageMenu.getItems().add(imageViewer);

    }

    @FXML
    void showPdfMenu(MouseEvent event) {
        if (pdfMenus.isShowing()) {
            return;
        }
        Bounds bounds = pdfBox.localToScreen(pdfBox.getBoundsInLocal());
        pdfMenus.show(pdfBox, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() / 2);

    }

    @FXML
    void hidePdfMenu(MouseEvent event) {
//        pdfMenus.hide();
    }

    @FXML
    void showImageMenu(MouseEvent event) {
        if (imageMenu.isShowing()) {
            return;
        }
        Bounds bounds = imageBox.localToScreen(imageBox.getBoundsInLocal());
        imageMenu.show(imageBox, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() / 2);

    }

    @FXML
    void hideImageMenu(MouseEvent event) {

    }

    @FXML
    void showFileMenu(MouseEvent event) {

    }

    @FXML
    void hideFileMenu(MouseEvent event) {

    }

    @FXML
    private void pdfTools() {
        reloadStage(CommonValues.PdfConvertImagesFxml, AppVaribles.getMessage("PdfConvertImages"));
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
