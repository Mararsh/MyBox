package mara.mybox.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.controller.BaseController.logger;
import static mara.mybox.objects.AppVaribles.getConfigValue;

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
    private RadioMenuItem chineseMenuItem, englishMenuItem;
    @FXML
    private RadioMenuItem replaceWhiteMenu, replaceBlackMenu, pdf500mbRadio, pdf1gbRadio, pdf2gbRadio, pdfUnlimitRadio;
    @FXML
    private CheckMenuItem showCommentsCheck, stopAlarmCheck;
    @FXML
    private Menu settingsMenu;

    @Override
    protected void initializeNext() {
        try {

            settingsMenu.setOnShowing(new EventHandler<Event>() {
                @Override
                public void handle(Event e) {
                    checkAlpha();
                    checkPdfMem();
                }
            });
            checkLanguage();
            checkAlpha();
            checkPdfMem();
            stopAlarmCheck.setSelected(AppVaribles.getConfigBoolean("StopAlarmsWhenExit"));
            showCommentsCheck.setSelected(AppVaribles.showComments);

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private void checkLanguage() {
        if (AppVaribles.CurrentBundle == CommonValues.BundleZhCN) {
            chineseMenuItem.setSelected(true);
        } else {
            englishMenuItem.setSelected(true);
        }
    }

    private void checkAlpha() {
        if (AppVaribles.alphaAsBlack) {
            replaceBlackMenu.setSelected(true);
        } else {
            replaceWhiteMenu.setSelected(true);
        }
    }

    private void checkPdfMem() {
        String pm = getConfigValue("PdfMemDefault", "1GB");
        switch (pm) {
            case "1GB":
                pdf1gbRadio.setSelected(true);
                break;
            case "2GB":
                pdf2gbRadio.setSelected(true);
                break;
            case "Unlimit":
                pdfUnlimitRadio.setSelected(true);
                break;
            case "500MB":
            default:
                pdf500mbRadio.setSelected(true);
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
    private void setStopAlarm(ActionEvent event) {
        AppVaribles.setConfigValue("StopAlarmsWhenExit", stopAlarmCheck.isSelected());
    }

    @FXML
    private void showCommentsAction(ActionEvent event) {
        AppVaribles.setConfigValue("ShowComments", showCommentsCheck.isSelected());
        AppVaribles.showComments = showCommentsCheck.isSelected();
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
    private void PdfMem500MB(ActionEvent event) {
        AppVaribles.setPdfMem("500MB");
    }

    @FXML
    private void PdfMem1GB(ActionEvent event) {
        AppVaribles.setPdfMem("1GB");
    }

    @FXML
    private void PdfMem2GB(ActionEvent event) {
        AppVaribles.setPdfMem("2GB");
    }

    @FXML
    private void pdfMemUnlimit(ActionEvent event) {
        AppVaribles.setPdfMem("Unlimit");
    }

    @FXML
    private void clearSettings(ActionEvent event) {
        try {
            File configFile = new File(CommonValues.UserConfigFile);
            if (!configFile.exists()) {
                configFile.createNewFile();
            } else {
                try (FileWriter fileWriter = new FileWriter(configFile)) {
                    fileWriter.write("");
                    fileWriter.flush();
                }
                popInformation(AppVaribles.getMessage("Successful"));
            }
        } catch (Exception e) {
            popError(e.toString());
        }
    }

    @FXML
    private void setDefaultStyle(ActionEvent event) {
        setInterfaceStyle(CommonValues.DefaultStyle);
    }

    @FXML
    private void setWhiteOnBlackStyle(ActionEvent event) {
        setInterfaceStyle(CommonValues.WhiteOnBlackStyle);
    }

    @FXML
    private void setYellowOnBlackStyle(ActionEvent event) {
        setInterfaceStyle(CommonValues.YellowOnBlackStyle);
    }

    @FXML
    private void setWhiteOnGreenStyle(ActionEvent event) {
        setInterfaceStyle(CommonValues.WhiteOnGreenStyle);
    }

    @FXML
    private void setCaspianStyle(ActionEvent event) {
        setInterfaceStyle(CommonValues.caspianStyle);
    }

    @FXML
    private void setGreenOnBlackStyle(ActionEvent event) {
        setInterfaceStyle(CommonValues.GreenOnBlackStyle);
    }

    @FXML
    private void setPinkOnBlackStyle(ActionEvent event) {
        setInterfaceStyle(CommonValues.PinkOnBlackStyle);
    }

    @FXML
    private void setBlackOnYellowStyle(ActionEvent event) {
        setInterfaceStyle(CommonValues.BlackOnYellowStyle);
    }

    @FXML
    private void setWhiteOnPurpleStyle(ActionEvent event) {
        setInterfaceStyle(CommonValues.WhiteOnPurpleStyle);
    }

    @FXML
    private void setWhiteOnBlueStyle(ActionEvent event) {
        setInterfaceStyle(CommonValues.WhiteOnBlueStyle);
    }

    @Override
    public void setInterfaceStyle(String style) {
        try {
            AppVaribles.currentStyle = style;
            AppVaribles.setConfigValue("InterfaceStyle", AppVaribles.currentStyle);
            if (parentController != null) {
                parentController.setInterfaceStyle(AppVaribles.currentStyle);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void exit(ActionEvent event) {
        // This statement is internel call to close the stage, so itself can not tigger stageClosing()
        closeStage();
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
    private void openImagesCombinePdf(ActionEvent event) {
        reloadStage(CommonValues.ImagesCombinePdfFxml, AppVaribles.getMessage("ImagesCombinePdf"));
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
    private void openMergePdf(ActionEvent event) {
        reloadStage(CommonValues.PdfMergeFxml, AppVaribles.getMessage("MergePdf"));
    }

    @FXML
    private void openSplitPdf(ActionEvent event) {
        reloadStage(CommonValues.PdfSplitFxml, AppVaribles.getMessage("SplitPdf"));
    }

    @FXML
    private void openCompressPdfImages(ActionEvent event) {
        reloadStage(CommonValues.PdfCompressImagesFxml, AppVaribles.getMessage("CompressPdfImages"));
    }

    @FXML
    private void openCompressPdfImagesBatch(ActionEvent event) {
        reloadStage(CommonValues.PdfCompressImagesBatchFxml, AppVaribles.getMessage("CompressPdfImagesBatch"));
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
    private void openImageManufactureEffects(ActionEvent event) {
        ImageManufactureController controller
                = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
        controller.setInitTab("effects");
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
    private void openImageManufactureCutMargins(ActionEvent event) {
        ImageManufactureController controller
                = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
        controller.setInitTab("cutMargins");
    }

    @FXML
    private void openImageManufactureAddMargins(ActionEvent event) {
        ImageManufactureController controller
                = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
        controller.setInitTab("addMargins");
    }

    @FXML
    private void openImageManufactureBatch(ActionEvent event) {
        reloadStage(CommonValues.ImageManufactureBatchFxml, AppVaribles.getMessage("ImageManufactureBatch"));
    }

    @FXML
    private void openImageManufactureBatchSize(ActionEvent event) {
        reloadStage(CommonValues.ImageManufactureBatchSizeFxml, AppVaribles.getMessage("ImageManufactureBatchSize"));
    }

    @FXML
    private void openImageManufactureBatchCrop(ActionEvent event) {
        reloadStage(CommonValues.ImageManufactureBatchCropFxml, AppVaribles.getMessage("ImageManufactureBatchCrop"));
    }

    @FXML
    private void openImageManufactureBatchColor(ActionEvent event) {
        reloadStage(CommonValues.ImageManufactureBatchColorFxml, AppVaribles.getMessage("ImageManufactureBatchColor"));
    }

    @FXML
    private void openImageManufactureBatchEffects(ActionEvent event) {
        reloadStage(CommonValues.ImageManufactureBatchEffectsFxml, AppVaribles.getMessage("ImageManufactureBatchEffects"));
    }

    @FXML
    private void openImageManufactureBatchFilters(ActionEvent event) {
        reloadStage(CommonValues.ImageManufactureBatchFiltersFxml, AppVaribles.getMessage("ImageManufactureBatchFilters"));
    }

    @FXML
    private void openImageManufactureBatchReplaceColor(ActionEvent event) {
        reloadStage(CommonValues.ImageManufactureBatchReplaceColorFxml, AppVaribles.getMessage("ImageManufactureBatchReplaceColor"));
    }

    @FXML
    private void openImageManufactureBatchWatermark(ActionEvent event) {
        reloadStage(CommonValues.ImageManufactureBatchWatermarkFxml, AppVaribles.getMessage("ImageManufactureBatchWatermark"));
    }

    @FXML
    private void openImageManufactureBatchArc(ActionEvent event) {
        reloadStage(CommonValues.ImageManufactureBatchArcFxml, AppVaribles.getMessage("ImageManufactureBatchArc"));
    }

    @FXML
    private void openImageManufactureBatchShadow(ActionEvent event) {
        reloadStage(CommonValues.ImageManufactureBatchShadowFxml, AppVaribles.getMessage("ImageManufactureBatchShadow"));
    }

    @FXML
    private void openImageManufactureBatchTransform(ActionEvent event) {
        reloadStage(CommonValues.ImageManufactureBatchTransformFxml, AppVaribles.getMessage("ImageManufactureBatchTransform"));
    }

    @FXML
    private void openImageManufactureBatchAddMargins(ActionEvent event) {
        reloadStage(CommonValues.ImageManufactureBatchAddMarginsFxml, AppVaribles.getMessage("ImageManufactureBatchAddMargins"));
    }

    @FXML
    private void openImageManufactureBatchCutMargins(ActionEvent event) {
        reloadStage(CommonValues.ImageManufactureBatchCutMarginsFxml, AppVaribles.getMessage("ImageManufactureBatchCutMargins"));
    }

    @FXML
    private void openImageSplit(ActionEvent event) {
        reloadStage(CommonValues.ImageSplitFxml, AppVaribles.getMessage("ImageSplit"));
    }

    @FXML
    private void openImageCombine(ActionEvent event) {
        reloadStage(CommonValues.ImagesCombineFxml, AppVaribles.getMessage("ImageCombine"));
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
    private void openHtmlEditor2(ActionEvent event) {
        HtmlEditorController controller
                = (HtmlEditorController) reloadStage(CommonValues.HtmlEditorFxml, AppVaribles.getMessage("HtmlEditor"));
//        controller.switchBroswerTab();
    }

    @FXML
    private void openTextEditor(ActionEvent event) {
        reloadStage(CommonValues.TextEditorFxml, AppVaribles.getMessage("TextEditor"));
    }

    @FXML
    private void openWeiboSnap(ActionEvent event) {
        WeiboSnapController controller
                = (WeiboSnapController) reloadStage(CommonValues.WeiboSnapFxml, AppVaribles.getMessage("WeiboSnap"));
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

            return parentController.stageClosing();

        } catch (Exception e) {
            return false;
        }

    }

}
