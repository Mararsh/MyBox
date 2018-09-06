package mara.mybox.controller;

import java.awt.Desktop;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.controller.BaseController.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class MainMenuController extends BaseController {

    @FXML
    private Pane mainMenuPane;
    @FXML
    private ToggleGroup langGroup, alarmGroup, alphaGroup;
    @FXML
    private RadioMenuItem chineseMenuItem, englishMenuItem, keepAlarmMenuItem, stopAlarmMenuItem, showCommentsMenu, hideCommentsMenu;
    @FXML
    private RadioMenuItem replaceWhiteMenu, replaceBlackMenu;
    @FXML
    private Menu homeMenu, pdfMenu, imageMenu, fileMenu, deskstopMenu, helpMenu;

    @Override
    protected void initializeNext() {

        if (AppVaribles.CurrentBundle == CommonValues.BundleZhCN) {
            chineseMenuItem.setSelected(true);
        } else {
            englishMenuItem.setSelected(true);
        }

        if (AppVaribles.getConfigBoolean("StopAlarmsWhenExit")) {
            stopAlarmMenuItem.setSelected(true);
        } else {
            keepAlarmMenuItem.setSelected(true);
        }

        if (AppVaribles.showComments) {
            showCommentsMenu.setSelected(true);
        } else {
            hideCommentsMenu.setSelected(true);
        }

        if (AppVaribles.alphaAsBlack) {
            replaceBlackMenu.setSelected(true);
        } else {
            replaceWhiteMenu.setSelected(true);
        }
    }

    @FXML
    private void showHome(ActionEvent event) {
        openStage(CommonValues.MyboxFxml, false, true);
    }

    @FXML
    private void setChinese(ActionEvent event) {
        AppVaribles.setCurrentBundle("zh");
        reloadStage(parentFxml, AppVaribles.getMessage("AppTitle"));
    }

    @FXML
    private void setEnglish(ActionEvent event) {
        AppVaribles.setCurrentBundle("en");
        reloadStage(parentFxml, AppVaribles.getMessage("AppTitle"));
    }

    @FXML
    private void setKeepAlarm(ActionEvent event) {
        AppVaribles.setConfigValue("StopAlarmsWhenExit", false);
    }

    @FXML
    private void setStopAlarm(ActionEvent event) {
        AppVaribles.setConfigValue("StopAlarmsWhenExit", true);
    }

    @FXML
    private void showCommentsAction(ActionEvent event) {
        AppVaribles.setConfigValue("ShowComments", true);
        AppVaribles.showComments = true;
    }

    @FXML
    private void hideCommentsAction(ActionEvent event) {
        AppVaribles.setConfigValue("ShowComments", false);
        AppVaribles.showComments = false;
    }

    @FXML
    private void replaceWhiteAction(ActionEvent event) {
        AppVaribles.setConfigValue("AlphaAsBlack", false);
        AppVaribles.alphaAsBlack = false;
    }

    @FXML
    private void replaceBlackAction(ActionEvent event) {
        AppVaribles.setConfigValue("AlphaAsBlack", true);
        AppVaribles.alphaAsBlack = true;
    }

    @FXML
    private void exit(ActionEvent event) {
        if (stageClosing()) {
            // This statement is internel call to close the stage, so itself can not tigger stageClosing()
            getMyStage().close();
        }
    }

    @FXML
    private void openPdfConvertImages(ActionEvent event) {
        reloadStage(CommonValues.PdfConvertImagesFxml, AppVaribles.getMessage("PdfConvertImages"));
    }

    @FXML
    private void openPdfConvertImagesBatch(ActionEvent event) {
        reloadStage(CommonValues.PdfConvertImagesBatchFxml, AppVaribles.getMessage("PdfConvertImagesBatch"));
    }

    @FXML
    private void openPdfExtractImages(ActionEvent event) {
        reloadStage(CommonValues.PdfExtractImagesFxml, AppVaribles.getMessage("PdfExtractImages"));
    }

    @FXML
    private void openPdfExtractTexts(ActionEvent event) {
        reloadStage(CommonValues.PdfExtractTextsFxml, AppVaribles.getMessage("PdfExtractTexts"));
    }

    @FXML
    private void openPdfExtractTextsBatch(ActionEvent event) {
        reloadStage(CommonValues.PdfExtractTextsBatchFxml, AppVaribles.getMessage("PdfExtractTextsBatch"));
    }

    @FXML
    private void openPdfExtractImagesBatch(ActionEvent event) {
        reloadStage(CommonValues.PdfExtractImagesBatchFxml, AppVaribles.getMessage("PdfExtractImagesBatch"));
    }

    @FXML
    private void openImageViewer(ActionEvent event) {
        reloadStage(CommonValues.ImageViewerFxml, AppVaribles.getMessage("ImageViewer"));
    }

    @FXML
    private void openMultipleImagesViewer(ActionEvent event) {
        reloadStage(CommonValues.ImagesViewerFxml, AppVaribles.getMessage("MultipleImagesViewer"));
    }

    @FXML
    private void openImageConverter(ActionEvent event) {
        reloadStage(CommonValues.ImageConverterFxml, AppVaribles.getMessage("ImageConverter"));
    }

    @FXML
    private void openImageConverterBatch(ActionEvent event) {
        reloadStage(CommonValues.ImageConverterBatchFxml, AppVaribles.getMessage("ImageConverterBatch"));
    }

    @FXML
    private void openImageManufacture(ActionEvent event) {
        reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
    }

    @FXML
    private void openImageManufactureSize(ActionEvent event) {
        ImageManufactureController controller
                = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
        controller.setInitTab("size");
    }

    @FXML
    private void openImageManufactureCrop(ActionEvent event) {
        ImageManufactureController controller
                = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
        controller.setInitTab("crop");
    }

    @FXML
    private void openImageManufactureColor(ActionEvent event) {
        ImageManufactureController controller
                = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
        controller.setInitTab("color");
    }

    @FXML
    private void openImageManufactureFilters(ActionEvent event) {
        ImageManufactureController controller
                = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
        controller.setInitTab("filters");
    }

    @FXML
    private void openImageManufactureReplaceColor(ActionEvent event) {
        ImageManufactureController controller
                = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
        controller.setInitTab("replaceColor");
    }

    @FXML
    private void openImageManufactureWatermark(ActionEvent event) {
        ImageManufactureController controller
                = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
        controller.setInitTab("watermark");
    }

    @FXML
    private void openImageManufactureArc(ActionEvent event) {
        ImageManufactureController controller
                = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
        controller.setInitTab("arc");
    }

    @FXML
    private void openImageManufactureShadow(ActionEvent event) {
        ImageManufactureController controller
                = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
        controller.setInitTab("shadow");
    }

    @FXML
    private void openImageManufactureTransform(ActionEvent event) {
        ImageManufactureController controller
                = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
        controller.setInitTab("transform");
    }

    @FXML
    private void openImageManufactureCutEdges(ActionEvent event) {
        ImageManufactureController controller
                = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
        controller.setInitTab("cutEdges");
    }

    @FXML
    private void openImageManufactureAddEdges(ActionEvent event) {
        ImageManufactureController controller
                = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
        controller.setInitTab("addEdges");
    }

    @FXML
    private void openImageSplit(ActionEvent event) {
        reloadStage(CommonValues.ImageSplitFxml, AppVaribles.getMessage("ImageSplit"));
    }

    @FXML
    private void openImageCombine(ActionEvent event) {
        reloadStage(CommonValues.ImageCombineFxml, AppVaribles.getMessage("ImageCombine"));
    }

    @FXML
    private void openColorPalette(ActionEvent event) {
        openStage(CommonValues.ColorPaletteFxml, AppVaribles.getMessage("ColorPalette"), false, false);
    }

    @FXML
    private void openPixelsCalculator(ActionEvent event) {
        openStage(CommonValues.PixelsCalculatorFxml, AppVaribles.getMessage("PixelsCalculator"), false, false);
    }

    @FXML
    private void openFilesRename(ActionEvent event) {
        reloadStage(CommonValues.FilesRenameFxml, AppVaribles.getMessage("FilesRename"));
    }

    @FXML
    private void openDirsRename(ActionEvent event) {
        reloadStage(CommonValues.DirectoriesRenameFxml, AppVaribles.getMessage("DirectoriesRename"));
    }

    @FXML
    private void openDirectorySynchronize(ActionEvent event) {
        reloadStage(CommonValues.DirectorySynchronizeFxml, AppVaribles.getMessage("DirectorySynchronize"));
    }

    @FXML
    private void openFilesArrangement(ActionEvent event) {
        reloadStage(CommonValues.FilesArrangementFxml, AppVaribles.getMessage("FilesArrangement"));
    }

    @FXML
    private void openAlarmClock(ActionEvent event) {
        reloadStage(CommonValues.AlarmClockFxml, AppVaribles.getMessage("AlarmClock"));
    }

    @FXML
    private void openHtmlEditor(ActionEvent event) {
        reloadStage(CommonValues.HtmlEditorFxml, AppVaribles.getMessage("HtmlEditor"));
    }

    @FXML
    private void openTextEditor(ActionEvent event) {
        reloadStage(CommonValues.TextEditorFxml, AppVaribles.getMessage("TextEditor"));
    }

    @FXML
    private void showImageHelp(ActionEvent event) {
        try {
            File help = FxmlTools.getUserFile(getClass(), "/docs/ImageHelp.html", "ImageHelp.html");
            Desktop.getDesktop().browse(help.toURI());
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void showAbout(ActionEvent event) {
        openStage(CommonValues.AboutFxml, true);
    }

    @Override
    public Stage getMyStage() {
        if (myStage == null) {
            if (mainMenuPane != null && mainMenuPane.getScene() != null) {
                myStage = (Stage) mainMenuPane.getScene().getWindow();
            }
        }
        return myStage;
    }

    @Override
    public boolean stageReloading() {
        try {
//            logger.debug("stageReloading");

            parentController.stageClosing();

            return true;

        } catch (Exception e) {
            return false;
        }

    }

}
