package mara.mybox.controller;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import mara.mybox.MyBox;
import mara.mybox.data.AlarmClock;
import mara.mybox.db.DerbyBase;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.ConfigTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import static mara.mybox.value.AppVariables.scheduledTasks;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:48:15
 * @Description
 * @License Apache License Version 2.0
 */
public class MyBoxController extends BaseController {

    private Popup imagePop;
    private ImageView view;
    private Text text;

    @FXML
    private VBox menuBox, imageBox, pdfBox, fileBox, recentBox, networkBox, dataBox,
            settingsBox, aboutBox, mediaBox;
    @FXML
    private CheckBox imageCheck;

    public MyBoxController() {
        baseTitle = AppVariables.message("AppTitle");

    }

    @Override
    public void initializeNext() {
        try {
            makeImagePopup();
            initAlocks();
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            thisPane.getScene().getWindow().focusedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) -> {
                if (!newV) {
                    hideMenu(null);
                }
            });

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    private void makeImagePopup() {
        try {
            imagePop = new Popup();
            imagePop.setWidth(600);
            imagePop.setHeight(600);

            VBox vbox = new VBox();
            VBox.setVgrow(vbox, Priority.ALWAYS);
            HBox.setHgrow(vbox, Priority.ALWAYS);
            vbox.setMaxWidth(Double.MAX_VALUE);
            vbox.setMaxHeight(Double.MAX_VALUE);
            vbox.setStyle("-fx-background-color: white;");
            imagePop.getContent().add(vbox);

            view = new ImageView();
            view.setFitWidth(500);
            view.setFitHeight(500);
            vbox.getChildren().add(view);

            text = new Text();
            text.setStyle("-fx-font-size: 1.2em;");

            vbox.getChildren().add(text);
            vbox.setPadding(new Insets(15, 15, 15, 15));

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private void initAlocks() {
        try {
            List<AlarmClock> alarms = AlarmClock.readAlarmClocks();
            if (alarms != null) {
                for (AlarmClock alarm : alarms) {
                    if (alarm.isIsActive()) {
                        AlarmClock.scehduleAlarmClock(alarm);
                    }
                }
                if (scheduledTasks != null && scheduledTasks.size() > 0) {
                    bottomLabel.setText(MessageFormat.format(AppVariables.message("AlarmClocksRunning"), scheduledTasks.size()));
                }
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private void showMenu(Region box, MouseEvent event) {
        if (popMenu == null || popMenu.isShowing()) {
            return;
        }
        FxmlControl.locateCenter(box, popMenu);
    }

    public void locateImage(Node region, boolean right) {
        if (!imageCheck.isSelected()) {
            imagePop.hide();
            return;
        }
        Bounds bounds = region.localToScreen(region.getBoundsInLocal());
        double x = right ? (bounds.getMaxX() + 200) : (bounds.getMinX() - 550);
        imagePop.show(region, x, bounds.getMinY() - 50);
        FxmlControl.refreshStyle(imagePop.getOwnerNode().getParent());
    }

    @FXML
    private void hideMenu(MouseEvent event) {
        if (popMenu != null) {
            popMenu.hide();
            popMenu = null;
        }
        imagePop.hide();
    }

    @FXML
    private void showPdfMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem pdfHtmlViewer = new MenuItem(AppVariables.message("PdfHtmlViewer"));
        pdfHtmlViewer.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PdfHtmlViewerFxml);
        });

        MenuItem pdfView = new MenuItem(AppVariables.message("PdfView"));
        pdfView.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PdfViewFxml);
        });

        MenuItem PDFAttributes = new MenuItem(AppVariables.message("PDFAttributes"));
        PDFAttributes.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PdfAttributesFxml);
        });

        MenuItem PDFAttributesBatch = new MenuItem(AppVariables.message("PDFAttributesBatch"));
        PDFAttributesBatch.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PdfAttributesBatchFxml);
        });

        MenuItem pdfExtractImagesBatch = new MenuItem(AppVariables.message("PdfExtractImagesBatch"));
        pdfExtractImagesBatch.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PdfExtractImagesBatchFxml);
        });

        MenuItem pdfExtractTextsBatch = new MenuItem(AppVariables.message("PdfExtractTextsBatch"));
        pdfExtractTextsBatch.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PdfExtractTextsBatchFxml);
        });

        MenuItem pdfConvertImagesBatch = new MenuItem(AppVariables.message("PdfConvertImagesBatch"));
        pdfConvertImagesBatch.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PdfConvertImagesBatchFxml);
        });

        MenuItem pdfOcrBatch = new MenuItem(AppVariables.message("PdfOCRBatch"));
        pdfOcrBatch.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PdfOCRBatchFxml);
        });

        MenuItem pdfConvertHtmlsBatch = new MenuItem(AppVariables.message("PdfConvertHtmlsBatch"));
        pdfConvertHtmlsBatch.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PdfConvertHtmlsBatchFxml);
        });

        MenuItem imagesCombinePdf = new MenuItem(AppVariables.message("ImagesCombinePdf"));
        imagesCombinePdf.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ImagesCombinePdfFxml);
        });

        MenuItem pdfCompressImagesBatch = new MenuItem(AppVariables.message("PdfCompressImagesBatch"));
        pdfCompressImagesBatch.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PdfCompressImagesBatchFxml);
        });

        MenuItem pdfMerge = new MenuItem(AppVariables.message("MergePdf"));
        pdfMerge.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PdfMergeFxml);
        });

        MenuItem PdfSplitBatch = new MenuItem(AppVariables.message("PdfSplitBatch"));
        PdfSplitBatch.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PdfSplitBatchFxml);
        });

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                pdfHtmlViewer, pdfView, new SeparatorMenuItem(),
                pdfConvertHtmlsBatch, pdfConvertImagesBatch, pdfExtractImagesBatch, pdfExtractTextsBatch,
                pdfOcrBatch, pdfCompressImagesBatch, new SeparatorMenuItem(),
                PdfSplitBatch, pdfMerge, imagesCombinePdf, new SeparatorMenuItem(),
                PDFAttributes, PDFAttributesBatch
        );

        showMenu(pdfBox, event);

        view.setImage(new Image("img/PdfTools.png"));
        text.setText(message("PdfToolsImageTips"));
        text.setWrappingWidth(500);
        locateImage(pdfBox, true);

    }

    @FXML
    private void showImageMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem imageViewer = new MenuItem(AppVariables.message("ImageViewer"));
        imageViewer.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ImageViewerFxml);
        });

        MenuItem imagesBrowser = new MenuItem(AppVariables.message("ImagesBrowser"));
        imagesBrowser.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ImagesBrowserFxml);
        });

        MenuItem imageData = new MenuItem(AppVariables.message("ImageAnalyse"));
        imageData.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ImageAnalyseFxml);
        });

        MenuItem ImageManufacture = new MenuItem(AppVariables.message("ImageManufacture"));
        ImageManufacture.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ImageManufactureFxml);
        });

        MenuItem imageConverterBatch = new MenuItem(AppVariables.message("ImageConverterBatch"));
        imageConverterBatch.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ImageConverterBatchFxml);
        });

        MenuItem imageStatistic = new MenuItem(AppVariables.message("ImageStatistic"));
        imageStatistic.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ImageStatisticFxml);
        });

        MenuItem imageOCR = new MenuItem(AppVariables.message("ImageOCR"));
        imageOCR.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ImageOCRFxml);
        });

        MenuItem imageOCRBatch = new MenuItem(AppVariables.message("ImageOCRBatch"));
        imageOCRBatch.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ImageOCRBatchFxml);
        });

        MenuItem convolutionKernelManager = new MenuItem(AppVariables.message("ConvolutionKernelManager"));
        convolutionKernelManager.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ConvolutionKernelManagerFxml);
        });

        MenuItem pixelsCalculator = new MenuItem(AppVariables.message("PixelsCalculator"));
        pixelsCalculator.setOnAction((ActionEvent event1) -> {
            openStage(CommonValues.PixelsCalculatorFxml);
        });

        MenuItem colorPalette = new MenuItem(AppVariables.message("ColorPalette"));
        colorPalette.setOnAction((ActionEvent event1) -> {
            openStage(CommonValues.ColorPaletteFxml);
        });

        MenuItem ManageColors = new MenuItem(AppVariables.message("ManageColors"));
        ManageColors.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ManageColorsFxml);
        });

        Menu manufactureBatchMenu = makeImageBatchToolsMenu();
        Menu framesMenu = makeImageFramesMenu();
        Menu partMenu = makeImagePartMenu();
        Menu mergeMenu = makeImageMergeMenu();
        Menu csMenu = makeColorSpaceMenu();

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        String os = System.getProperty("os.name").toLowerCase();
        popMenu.getItems().addAll(
                imageViewer, imagesBrowser, imageData, new SeparatorMenuItem(),
                ImageManufacture, manufactureBatchMenu,
                imageConverterBatch, imageOCR, imageOCRBatch, new SeparatorMenuItem(),
                framesMenu, mergeMenu, partMenu, new SeparatorMenuItem(),
                convolutionKernelManager, pixelsCalculator, colorPalette, ManageColors,
                csMenu);

        showMenu(imageBox, event);

        view.setImage(new Image("img/ImageTools.png"));
        text.setText(message("ImageToolsImageTips"));
        locateImage(imageBox, true);

    }

    private Menu makeImageBatchToolsMenu() {
        if (popMenu != null) {
            popMenu.hide();
        }

        MenuItem imageSizeMenu = new MenuItem(AppVariables.message("Size"));
        imageSizeMenu.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageManufactureBatchSizeFxml);
        });

        MenuItem imageCropMenu = new MenuItem(AppVariables.message("Crop"));
        imageCropMenu.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageManufactureBatchCropFxml);
        });

        MenuItem imageColorMenu = new MenuItem(AppVariables.message("Color"));
        imageColorMenu.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageManufactureBatchColorFxml);
        });

        MenuItem imageEffectsMenu = new MenuItem(AppVariables.message("Effects"));
        imageEffectsMenu.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageManufactureBatchEffectsFxml);
        });

        MenuItem imageEnhancementMenu = new MenuItem(AppVariables.message("Enhancement"));
        imageEnhancementMenu.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageManufactureBatchEnhancementFxml);
        });

        MenuItem imageReplaceColorMenu = new MenuItem(AppVariables.message("ReplaceColor"));
        imageReplaceColorMenu.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageManufactureBatchReplaceColorFxml);
        });

        MenuItem imageTextMenu = new MenuItem(AppVariables.message("Text"));
        imageTextMenu.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageManufactureBatchTextFxml);
        });

        MenuItem imageArcMenu = new MenuItem(AppVariables.message("Arc"));
        imageArcMenu.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageManufactureBatchArcFxml);
        });

        MenuItem imageShadowMenu = new MenuItem(AppVariables.message("Shadow"));
        imageShadowMenu.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageManufactureBatchShadowFxml);
        });

        MenuItem imageTransformMenu = new MenuItem(AppVariables.message("Transform"));
        imageTransformMenu.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageManufactureBatchTransformFxml);
        });

        MenuItem imageMarginsMenu = new MenuItem(AppVariables.message("Margins"));
        imageMarginsMenu.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageManufactureBatchMarginsFxml);
        });

        Menu manufactureBatchMenu = new Menu(AppVariables.message("ImageManufactureBatch"));
        manufactureBatchMenu.getItems().addAll(imageSizeMenu, imageCropMenu, imageColorMenu,
                imageEffectsMenu, imageEnhancementMenu, imageReplaceColorMenu, imageTextMenu,
                imageArcMenu, imageShadowMenu, imageTransformMenu, imageMarginsMenu);
        return manufactureBatchMenu;

    }

    private Menu makeImageFramesMenu() {
        if (popMenu != null) {
            popMenu.hide();
        }

        MenuItem imageGifViewer = new MenuItem(AppVariables.message("ImageGifViewer"));
        imageGifViewer.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageGifViewerFxml);
        });

        MenuItem imageGifEditer = new MenuItem(AppVariables.message("ImageGifEditer"));
        imageGifEditer.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageGifEditerFxml);
        });

        MenuItem imageTiffEditer = new MenuItem(AppVariables.message("ImageTiffEditer"));
        imageTiffEditer.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageTiffEditerFxml);
        });

        MenuItem imageFramesViewer = new MenuItem(AppVariables.message("ImageFramesViewer"));
        imageFramesViewer.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageFramesViewerFxml);
        });

        Menu manufactureSubMenu = new Menu(AppVariables.message("MultipleFramesImageFile"));
        manufactureSubMenu.getItems().addAll(imageFramesViewer, imageTiffEditer, imageGifViewer, imageGifEditer);
        return manufactureSubMenu;

    }

    private Menu makeImagePartMenu() {
        if (popMenu != null) {
            popMenu.hide();
        }

        MenuItem ImageSplit = new MenuItem(AppVariables.message("ImageSplit"));
        ImageSplit.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageSplitFxml);
        });

        MenuItem ImageSample = new MenuItem(AppVariables.message("ImageSubsample"));
        ImageSample.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageSampleFxml);
        });

        MenuItem imageAlphaExtract = new MenuItem(AppVariables.message("ImageAlphaExtract"));
        imageAlphaExtract.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageAlphaExtractBatchFxml);
        });

        Menu manufactureSubMenu = new Menu(AppVariables.message("ImagePart"));
        manufactureSubMenu.getItems().addAll(ImageSplit, ImageSample, imageAlphaExtract);
        return manufactureSubMenu;

    }

    private Menu makeImageMergeMenu() {
        if (popMenu != null) {
            popMenu.hide();
        }

        MenuItem ImageCombine = new MenuItem(AppVariables.message("ImagesCombine"));
        ImageCombine.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImagesCombineFxml);
        });

        MenuItem imagesCombinePdf = new MenuItem(AppVariables.message("ImagesCombinePdf"));
        imagesCombinePdf.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImagesCombinePdfFxml);
        });

        MenuItem imageAlphaAdd = new MenuItem(AppVariables.message("ImageAlphaAdd"));
        imageAlphaAdd.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageAlphaAddBatchFxml);
        });

        Menu manufactureSubMenu = new Menu(AppVariables.message("MergeImages"));
        manufactureSubMenu.getItems().addAll(ImageCombine, imagesCombinePdf, imageAlphaAdd);
        return manufactureSubMenu;

    }

    private Menu makeColorSpaceMenu() {
        if (popMenu != null) {
            popMenu.hide();
        }
        MenuItem IccEditor = new MenuItem(AppVariables.message("IccProfileEditor"));
        IccEditor.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.IccProfileEditorFxml);
        });

        MenuItem ChromaticityDiagram = new MenuItem(AppVariables.message("DrawChromaticityDiagram"));
        ChromaticityDiagram.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ChromaticityDiagramFxml);
        });

        MenuItem ChromaticAdaptationMatrix = new MenuItem(AppVariables.message("ChromaticAdaptationMatrix"));
        ChromaticAdaptationMatrix.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ChromaticAdaptationMatrixFxml);
        });

        MenuItem ColorPalette = new MenuItem(AppVariables.message("ColorPalette"));
        ColorPalette.setOnAction((ActionEvent event) -> {
            openStage(CommonValues.ColorPaletteFxml);
        });

        MenuItem ColorConversion = new MenuItem(AppVariables.message("ColorConversion"));
        ColorConversion.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ColorConversionFxml);
        });

        MenuItem RGBColorSpaces = new MenuItem(AppVariables.message("RGBColorSpaces"));
        RGBColorSpaces.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.RGBColorSpacesFxml);
        });

        MenuItem RGB2XYZConversionMatrix = new MenuItem(AppVariables.message("LinearRGB2XYZMatrix"));
        RGB2XYZConversionMatrix.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.RGB2XYZConversionMatrixFxml);
        });

        MenuItem RGB2RGBConversionMatrix = new MenuItem(AppVariables.message("LinearRGB2RGBMatrix"));
        RGB2RGBConversionMatrix.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.RGB2RGBConversionMatrixFxml);
        });

        MenuItem Illuminants = new MenuItem(AppVariables.message("Illuminants"));
        Illuminants.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.IlluminantsFxml);
        });

        Menu csMenu = new Menu(AppVariables.message("ColorSpace"));
        csMenu.getItems().addAll(ChromaticityDiagram, IccEditor,
                //                ColorConversion,
                RGBColorSpaces, RGB2XYZConversionMatrix, RGB2RGBConversionMatrix,
                Illuminants, ChromaticAdaptationMatrix);
        return csMenu;

    }

    @FXML
    private void showNetworkMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem htmlEditor = new MenuItem(AppVariables.message("HtmlEditor"));
        htmlEditor.setOnAction((ActionEvent event1) -> {
            HtmlEditorController controller
                    = (HtmlEditorController) loadScene(CommonValues.HtmlEditorFxml);
//                controller.switchBroswerTab();
        });

        MenuItem weiboSnap = new MenuItem(AppVariables.message("WeiboSnap"));
        weiboSnap.setOnAction((ActionEvent event1) -> {
            WeiboSnapController controller
                    = (WeiboSnapController) loadScene(CommonValues.WeiboSnapFxml);
        });

        MenuItem markdownEditor = new MenuItem(AppVariables.message("MarkdownEditer"));
        markdownEditor.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.MarkdownEditorFxml);
        });

        MenuItem markdownToHtml = new MenuItem(AppVariables.message("MarkdownToHtml"));
        markdownToHtml.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.MarkdownToHtmlFxml);
        });

        MenuItem webBrowserHtml = new MenuItem(AppVariables.message("WebBrowser"));
        webBrowserHtml.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.WebBrowserFxml);
        });

        MenuItem htmlToMarkdown = new MenuItem(AppVariables.message("HtmlToMarkdown"));
        htmlToMarkdown.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.HtmlToMarkdownFxml);
        });

        MenuItem DownloadManage = new MenuItem(AppVariables.message("DownloadManage"));
        DownloadManage.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.DownloadFxml);
        });

        MenuItem SecurityCertificates = new MenuItem(AppVariables.message("SecurityCertificates"));
        SecurityCertificates.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.SecurityCertificatesFxml);
        });

        MenuItem RestoreCheckingSSLCertifications = new MenuItem(AppVariables.message("RestoreCheckingSSLCertifications"));
        RestoreCheckingSSLCertifications.setOnAction((ActionEvent event1) -> {
            restoreCheckingSSL();
        });

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                htmlEditor, webBrowserHtml, SecurityCertificates, RestoreCheckingSSLCertifications, new SeparatorMenuItem(),
                markdownEditor, htmlToMarkdown, markdownToHtml, new SeparatorMenuItem(),
                DownloadManage, new SeparatorMenuItem(),
                weiboSnap
        );
        showMenu(networkBox, event);

        view.setImage(new Image("img/NetworkTools.png"));
        text.setText(message("NetworkToolsImageTips"));
        locateImage(networkBox, true);

    }

    @FXML
    private void showFileMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem filesRename = new MenuItem(AppVariables.message("FilesRename"));
        filesRename.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesRenameFxml);
        });

        MenuItem dirSynchronize = new MenuItem(AppVariables.message("DirectorySynchronize"));
        dirSynchronize.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.DirectorySynchronizeFxml);
        });

        MenuItem filesArrangement = new MenuItem(AppVariables.message("FilesArrangement"));
        filesArrangement.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesArrangementFxml);
        });

        MenuItem textEditer = new MenuItem(AppVariables.message("TextEditer"));
        textEditer.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.TextEditerFxml);
        });

        MenuItem textEncodingBatch = new MenuItem(AppVariables.message("TextEncodingBatch"));
        textEncodingBatch.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.TextEncodingBatchFxml);
        });

        MenuItem textLineBreakBatch = new MenuItem(AppVariables.message("TextLineBreakBatch"));
        textLineBreakBatch.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.TextLineBreakBatchFxml);
        });

        MenuItem bytesEditer = new MenuItem(AppVariables.message("BytesEditer"));
        bytesEditer.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.BytesEditerFxml);
        });

        MenuItem fileCut = new MenuItem(AppVariables.message("FileCut"));
        fileCut.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FileCutFxml);
        });

        MenuItem filesMerge = new MenuItem(AppVariables.message("FilesMerge"));
        filesMerge.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesMergeFxml);
        });

        MenuItem filesDelete = new MenuItem(AppVariables.message("FilesDelete"));
        filesDelete.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesDeleteFxml);
        });

        MenuItem DeleteEmptyDirectories = new MenuItem(AppVariables.message("DeleteEmptyDirectories"));
        DeleteEmptyDirectories.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesDeleteEmptyDirFxml);
        });

        MenuItem DeleteNestedDirectories = new MenuItem(AppVariables.message("DeleteNestedDirectories"));
        DeleteNestedDirectories.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesDeleteNestedDirFxml);
        });

        MenuItem filesCopy = new MenuItem(AppVariables.message("FilesCopy"));
        filesCopy.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesCopyFxml);
        });

        MenuItem filesMove = new MenuItem(AppVariables.message("FilesMove"));
        filesMove.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesMoveFxml);
        });

        MenuItem filesFind = new MenuItem(AppVariables.message("FilesFind"));
        filesFind.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesFindFxml);
        });

        MenuItem filesCompare = new MenuItem(AppVariables.message("FilesCompare"));
        filesCompare.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesCompareFxml);
        });

        MenuItem filesRedundancy = new MenuItem(AppVariables.message("FilesRedundancy"));
        filesRedundancy.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesRedundancyFxml);
        });

        MenuItem filesArchiveCompress = new MenuItem(AppVariables.message("FilesArchiveCompress"));
        filesArchiveCompress.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesArchiveCompressFxml);
        });

        MenuItem filesCompress = new MenuItem(AppVariables.message("FilesCompressBatch"));
        filesCompress.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesCompressBatchFxml);
        });

        MenuItem filesDecompressUnarchive = new MenuItem(AppVariables.message("FileDecompressUnarchive"));
        filesDecompressUnarchive.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FileDecompressUnarchiveFxml);
        });

        MenuItem filesDecompressUnarchiveBatch = new MenuItem(AppVariables.message("FilesDecompressUnarchiveBatch"));
        filesDecompressUnarchiveBatch.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesDecompressUnarchiveBatchFxml);
        });

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                textEditer, bytesEditer, new SeparatorMenuItem(),
                textEncodingBatch, textLineBreakBatch, fileCut, filesMerge,
                filesArchiveCompress, filesCompress,
                filesDecompressUnarchive, filesDecompressUnarchiveBatch,
                filesArrangement, dirSynchronize, new SeparatorMenuItem(),
                filesFind, filesCompare, filesRedundancy,
                filesRename, filesDelete, filesCopy, filesMove,
                DeleteEmptyDirectories, DeleteNestedDirectories
        );

        showMenu(fileBox, event);

        view.setImage(new Image("img/FileTools.png"));
        text.setText(message("FileToolsImageTips"));
        locateImage(fileBox, false);
    }

    @FXML
    private void showSettingsMenu(MouseEvent event) {
        hideMenu(event);

        String lang = AppVariables.getLanguage();
        List<MenuItem> langItems = new ArrayList();
        ToggleGroup langGroup = new ToggleGroup();
        RadioMenuItem English = new RadioMenuItem("English");
        English.setToggleGroup(langGroup);
        English.setOnAction((ActionEvent event1) -> {
            if (isSettingValues) {
                return;
            }
            AppVariables.setLanguage("en");
            loadScene(myFxml);
        });
        langItems.add(English);
        if ("en".equals(lang)) {
            isSettingValues = true;
            English.setSelected(true);
            isSettingValues = false;
        }
        RadioMenuItem Chinese = new RadioMenuItem("中文");
        Chinese.setToggleGroup(langGroup);
        Chinese.setOnAction((ActionEvent event1) -> {
            if (isSettingValues) {
                return;
            }
            AppVariables.setLanguage("zh");
            loadScene(myFxml);
        });
        langItems.add(Chinese);
        if ("zh".equals(lang)) {
            isSettingValues = true;
            Chinese.setSelected(true);
            isSettingValues = false;
        }

        List<String> languages = ConfigTools.languages();
        if (languages != null && !languages.isEmpty()) {

            for (int i = 0; i < languages.size(); ++i) {
                final String name = languages.get(i);
                RadioMenuItem langItem = new RadioMenuItem(name);
                langItem.setToggleGroup(langGroup);
                langItem.setOnAction((ActionEvent event1) -> {
                    if (isSettingValues) {
                        return;
                    }
                    AppVariables.setLanguage(name);
                    loadScene(myFxml);
                });
                langItems.add(langItem);
                if (name.equals(lang)) {
                    isSettingValues = true;
                    langItem.setSelected(true);
                    isSettingValues = false;
                }
            }
        }

        MenuItem ManageLanguages = new MenuItem(AppVariables.message("ManageLanguages"));
        ManageLanguages.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.MyBoxLanguagesFxml);
        });

        CheckMenuItem disableHidpi = new CheckMenuItem(message("DisableHiDPI"));
        disableHidpi.setOnAction((ActionEvent event1) -> {
            if (isSettingValues) {
                return;
            }
            AppVariables.disableHiDPI = disableHidpi.isSelected();
            ConfigTools.writeConfigValue("DisableHidpi", AppVariables.disableHiDPI ? "true" : "false");
            Platform.runLater(() -> {
                try {
                    MyBox.restart();
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
            });
        });
        isSettingValues = true;
        AppVariables.disableHiDPI = "true".equals(ConfigTools.readValue("DisableHidpi"));
        disableHidpi.setSelected(AppVariables.disableHiDPI);
        isSettingValues = false;

        CheckMenuItem derbyServer = new CheckMenuItem(message("DerbyServerMode"));
        derbyServer.setOnAction((ActionEvent event1) -> {
            if (isSettingValues) {
                return;
            }
            derbyServer.setDisable(true);
            DerbyBase.mode = derbyServer.isSelected() ? "client" : "embedded";
            ConfigTools.writeConfigValue("DerbyMode", DerbyBase.mode);
            Platform.runLater(() -> {
                try {
                    String ret = DerbyBase.startDerby();
                    if (ret != null) {
                        popInformation(ret, 6000);
                        isSettingValues = true;
                        derbyServer.setSelected("client".equals(DerbyBase.mode));
                        isSettingValues = false;
                    } else {
                        popFailed();
                    }
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                derbyServer.setDisable(false);
            });
        });
        isSettingValues = true;
        derbyServer.setSelected("client".equals(DerbyBase.mode));
        isSettingValues = false;

        MenuItem mybox = new MenuItem(AppVariables.message("MyBoxProperties"));
        mybox.setOnAction((ActionEvent event1) -> {
            openStage(CommonValues.MyBoxPropertiesFxml);
        });

        MenuItem settings = new MenuItem(AppVariables.message("SettingsDot"));
        settings.setOnAction((ActionEvent event1) -> {
            BaseController c = openStage(CommonValues.SettingsFxml);
        });

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(langItems);
        popMenu.getItems().addAll(ManageLanguages, new SeparatorMenuItem(),
                disableHidpi, derbyServer, new SeparatorMenuItem(),
                mybox, new SeparatorMenuItem(),
                settings);

        showMenu(settingsBox, event);

        view.setImage(new Image("img/Settings.png"));
        text.setText(message("SettingsImageTips"));
        locateImage(settingsBox, true);
    }

    @FXML
    private void showRecentMenu(MouseEvent event) {
        hideMenu(event);

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(getRecentMenu());
        showMenu(recentBox, event);

        view.setImage(new Image("img/RecentAccess.png"));
        text.setText(message("RecentAccessImageTips"));
        locateImage(recentBox, true);
    }

    @FXML
    private void showDataMenu(MouseEvent event) {
        hideMenu(event);

        Menu csMenu = makeColorSpaceMenu();

        MenuItem imageData = new MenuItem(AppVariables.message("ImageAnalyse"));
        imageData.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ImageAnalyseFxml);
        });

        MenuItem MatricesCalculation = new MenuItem(AppVariables.message("MatricesCalculation"));
        MatricesCalculation.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.MatricesCalculationFxml);
        });

        MenuItem barcodeCreator = new MenuItem(AppVariables.message("BarcodeCreator"));
        barcodeCreator.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.BarcodeCreatorFxml);
        });

        MenuItem barcodeDecoder = new MenuItem(AppVariables.message("BarcodeDecoder"));
        barcodeDecoder.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.BarcodeDecoderFxml);
        });

        MenuItem messageDigest = new MenuItem(AppVariables.message("MessageDigest"));
        messageDigest.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.MessageDigestFxml);
        });

        MenuItem GeographyCode = new MenuItem(AppVariables.message("GeographyCode"));
        GeographyCode.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.GeographyCodeFxml);
        });

//        MenuItem GeographyRegion = new MenuItem(AppVariables.message("GeographyRegion"));
//        GeographyRegion.setOnAction((ActionEvent event1) -> {
//            loadScene(CommonValues.GeographyRegionFxml);
//        });
        MenuItem LocationsData = new MenuItem(AppVariables.message("LocationsData"));
        LocationsData.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.LocationsDataFxml);
        });

        MenuItem LocationsDataInMap = new MenuItem(AppVariables.message("LocationsDataInMap"));
        LocationsDataInMap.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.LocationsDataInMapFxml);
        });

        MenuItem LocationInMap = new MenuItem(AppVariables.message("LocationInMap"));
        LocationInMap.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.LocationInMapFxml);
        });

        MenuItem EpidemicReport = new MenuItem(AppVariables.message("EpidemicReport"));
        EpidemicReport.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.EpidemicReportsFxml);
        });

        MenuItem FetchNPCData = new MenuItem(AppVariables.message("FetchNPCData"));
        FetchNPCData.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.EpidemicReportsFetchNPCDataFxml);
        });

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                MatricesCalculation, new SeparatorMenuItem(),
                imageData, csMenu, new SeparatorMenuItem(),
                barcodeCreator, barcodeDecoder, new SeparatorMenuItem(),
                messageDigest, new SeparatorMenuItem(),
                GeographyCode, LocationInMap, LocationsData, LocationsDataInMap, new SeparatorMenuItem(),
                EpidemicReport, FetchNPCData
        );

        showMenu(dataBox, event);

        view.setImage(new Image("img/DataTools.png"));
        text.setText(message("DataToolsImageTips"));
        locateImage(dataBox, true);
    }

    @FXML
    private void showMediaMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem mediaPlayer = new MenuItem(AppVariables.message("MediaPlayer"));
        mediaPlayer.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.MediaPlayerFxml);
        });

        MenuItem mediaLists = new MenuItem(AppVariables.message("ManageMediaLists"));
        mediaLists.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.MediaListFxml);
        });

        MenuItem FFmpegInformation = new MenuItem(AppVariables.message("FFmpegInformation"));
        FFmpegInformation.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FFmpegInformationFxml);
        });

        MenuItem FFprobe = new MenuItem(AppVariables.message("FFmpegProbeMediaInformation"));
        FFprobe.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FFmpegProbeMediaInformationFxml);
        });

        MenuItem FFmpegConversionFiles = new MenuItem(AppVariables.message("FFmpegConvertMediaFiles"));
        FFmpegConversionFiles.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FFmpegConvertMediaFilesFxml);
        });

        MenuItem FFmpegConversionStreams = new MenuItem(AppVariables.message("FFmpegConvertMediaStreams"));
        FFmpegConversionStreams.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FFmpegConvertMediaStreamsFxml);
        });

        MenuItem FFmpegMergeImages = new MenuItem(AppVariables.message("FFmpegMergeImages"));
        FFmpegMergeImages.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FFmpegMergeImagesFxml);
        });

        MenuItem FFmpegMergeImageFiles = new MenuItem(AppVariables.message("FFmpegMergeImageFiles"));
        FFmpegMergeImageFiles.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FFmpegMergeImageFilesFxml);
        });

        MenuItem recordImages = new MenuItem(AppVariables.message("RecordImagesInSystemClipBoard"));
        recordImages.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.RecordImagesInSystemClipboardFxml);
        });

        MenuItem alarmClock = new MenuItem(AppVariables.message("AlarmClock"));
        alarmClock.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.AlarmClockFxml);
        });

        MenuItem GameElimniation = new MenuItem(AppVariables.message("GameElimniation"));
        GameElimniation.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.GameElimniationFxml);
        });

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                mediaPlayer, mediaLists, new SeparatorMenuItem(),
                FFmpegConversionStreams, FFmpegConversionFiles, FFmpegMergeImages, FFmpegMergeImageFiles,
                FFprobe, FFmpegInformation, new SeparatorMenuItem(),
                recordImages, new SeparatorMenuItem(), alarmClock, new SeparatorMenuItem(),
                GameElimniation
        );

        showMenu(mediaBox, event);

        view.setImage(new Image("img/MediaTools.png"));
        text.setText(message("MediaToolsImageTips"));
        locateImage(mediaBox, false);
    }

    @FXML
    private void showAboutImage(MouseEvent event) {
        hideMenu(event);

        view.setImage(new Image("img/About.png"));
        text.setText(message("AboutImageTips"));
        locateImage(aboutBox, false);
    }

    @FXML
    private void showAbout(MouseEvent event) {
        hideMenu(event);
        openStage(CommonValues.AboutFxml);
    }

}
