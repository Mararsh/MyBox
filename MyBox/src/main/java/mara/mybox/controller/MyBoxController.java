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
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.AlarmClock;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlWindow;
import mara.mybox.tools.ConfigTools;
import mara.mybox.value.AppVariables;
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
    protected VBox menuBox, imageBox, documentBox, fileBox, recentBox, networkBox, dataBox,
            settingsBox, aboutBox, mediaBox;
    @FXML
    protected CheckBox imageCheck;

    public MyBoxController() {
        baseTitle = AppVariables.message("AppTitle");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            makeImagePopup();
            initAlocks();
            AppVariables.checkTextClipboardMonitor();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            thisPane.getScene().getWindow().focusedProperty().addListener(
                    (ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) -> {
                        if (!newV) {
                            hideMenu(null);
                        }
                    });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    private void makeImagePopup() {
        try {
            imagePop = new Popup();
            imagePop.setWidth(650);
            imagePop.setHeight(650);

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
            MyBoxLog.debug(e.toString());
        }
    }

    private void initAlocks() {
        try {
            List<AlarmClock> alarms = AlarmClock.readAlarmClocks();
            if (alarms != null) {
                for (AlarmClock alarm : alarms) {
                    if (alarm.isIsActive()) {
                        AlarmClock.scheduleAlarmClock(alarm);
                    }
                }
                if (scheduledTasks != null && scheduledTasks.size() > 0) {
                    bottomLabel.setText(MessageFormat.format(AppVariables.message("AlarmClocksRunning"), scheduledTasks.size()));
                }
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
    protected void hideMenu(MouseEvent event) {
        if (popMenu != null) {
            popMenu.hide();
            popMenu = null;
        }
        imagePop.hide();
    }

    @FXML
    protected void showDocumentMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem Notes = new MenuItem(AppVariables.message("Notes"));
        Notes.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.NotesFxml);
        });

        Menu pdfMenu = new Menu("PDF");

        MenuItem pdfHtmlViewer = new MenuItem(AppVariables.message("PdfHtmlViewer"));
        pdfHtmlViewer.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PdfHtmlViewerFxml);
        });

        MenuItem pdfView = new MenuItem(AppVariables.message("PdfView"));
        pdfView.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PdfViewFxml);
        });

        MenuItem PdfPlay = new MenuItem(AppVariables.message("PdfPlay"));
        PdfPlay.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ImagesPlayFxml);
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
            loadScene(CommonValues.ImagesEditorFxml);
        });

        MenuItem pdfCompressImagesBatch = new MenuItem(AppVariables.message("PdfCompressImagesBatch"));
        pdfCompressImagesBatch.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PdfCompressImagesBatchFxml);
        });

        MenuItem PdfImagesConvertBatch = new MenuItem(AppVariables.message("PdfImagesConvertBatch"));
        PdfImagesConvertBatch.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PdfImagesConvertBatchFxml);
        });

        MenuItem pdfMerge = new MenuItem(AppVariables.message("MergePdf"));
        pdfMerge.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PdfMergeFxml);
        });

        MenuItem PdfSplitBatch = new MenuItem(AppVariables.message("PdfSplitBatch"));
        PdfSplitBatch.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PdfSplitBatchFxml);
        });

        pdfMenu.getItems().addAll(
                pdfView, pdfHtmlViewer, PdfPlay, new SeparatorMenuItem(),
                pdfConvertImagesBatch, PdfImagesConvertBatch, pdfConvertHtmlsBatch, pdfCompressImagesBatch, new SeparatorMenuItem(),
                pdfExtractImagesBatch, pdfExtractTextsBatch, pdfOcrBatch, new SeparatorMenuItem(),
                PdfSplitBatch, pdfMerge, imagesCombinePdf, new SeparatorMenuItem(),
                PDFAttributes, PDFAttributesBatch
        );

        Menu textsMenu = new Menu(message("Texts"));

        MenuItem textEditer = new MenuItem(AppVariables.message("TextEditer"));
        textEditer.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.TextEditerFxml);
        });

        MenuItem TextConvert = new MenuItem(AppVariables.message("TextConvertSplit"));
        TextConvert.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.TextFilesConvertFxml);
        });

        MenuItem TextMerge = new MenuItem(AppVariables.message("TextFilesMerge"));
        TextMerge.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.TextFilesMergeFxml);
        });

        MenuItem TextReplaceBatch = new MenuItem(AppVariables.message("TextReplaceBatch"));
        TextReplaceBatch.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.TextReplaceBatchFxml);
        });

        MenuItem TextToHtml = new MenuItem(AppVariables.message("TextToHtml"));
        TextToHtml.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.TextToHtmlFxml);
        });

        textsMenu.getItems().addAll(
                textEditer, TextConvert, TextMerge, TextReplaceBatch, TextToHtml
        );

        MenuItem bytesEditer = new MenuItem(AppVariables.message("BytesEditer"));
        bytesEditer.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.BytesEditerFxml);
        });

        Menu htmlMenu = new Menu(message("Html"));

        MenuItem htmlEditor = new MenuItem(AppVariables.message("HtmlEditor"));
        htmlEditor.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.HtmlEditorFxml);
        });

        MenuItem htmlToMarkdown = new MenuItem(AppVariables.message("HtmlToMarkdown"));
        htmlToMarkdown.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.HtmlToMarkdownFxml);
        });

        MenuItem HtmlToText = new MenuItem(AppVariables.message("HtmlToText"));
        HtmlToText.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.HtmlToTextFxml);
        });

        MenuItem HtmlToPdf = new MenuItem(AppVariables.message("HtmlToPdf"));
        HtmlToPdf.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.HtmlToPdfFxml);
        });

        MenuItem HtmlSetCharset = new MenuItem(AppVariables.message("HtmlSetCharset"));
        HtmlSetCharset.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.HtmlSetCharsetFxml);
        });

        MenuItem HtmlSetStyle = new MenuItem(AppVariables.message("HtmlSetStyle"));
        HtmlSetStyle.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.HtmlSetStyleFxml);
        });

        MenuItem HtmlSnap = new MenuItem(AppVariables.message("HtmlSnap"));
        HtmlSnap.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.HtmlSnapFxml);
        });

        MenuItem WebFind = new MenuItem(AppVariables.message("WebFind"));
        WebFind.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.WebFindFxml);
        });

        MenuItem WebElements = new MenuItem(AppVariables.message("WebElements"));
        WebElements.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.WebElementsFxml);
        });

        MenuItem HtmlMergeAsHtml = new MenuItem(AppVariables.message("HtmlMergeAsHtml"));
        HtmlMergeAsHtml.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.HtmlMergeAsHtmlFxml);
        });

        MenuItem HtmlMergeAsMarkdown = new MenuItem(AppVariables.message("HtmlMergeAsMarkdown"));
        HtmlMergeAsMarkdown.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.HtmlMergeAsMarkdownFxml);
        });

        MenuItem HtmlMergeAsPDF = new MenuItem(AppVariables.message("HtmlMergeAsPDF"));
        HtmlMergeAsPDF.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.HtmlMergeAsPDFFxml);
        });

        MenuItem HtmlMergeAsText = new MenuItem(AppVariables.message("HtmlMergeAsText"));
        HtmlMergeAsText.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.HtmlMergeAsTextFxml);
        });

        MenuItem HtmlFrameset = new MenuItem(AppVariables.message("HtmlFrameset"));
        HtmlFrameset.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.HtmlFramesetFxml);
        });

        htmlMenu.getItems().addAll(
                htmlEditor, WebFind, WebElements, HtmlSnap, new SeparatorMenuItem(),
                htmlToMarkdown, HtmlToText, HtmlToPdf, HtmlSetCharset, HtmlSetStyle, new SeparatorMenuItem(),
                HtmlMergeAsHtml, HtmlMergeAsMarkdown, HtmlMergeAsPDF, HtmlMergeAsText, HtmlFrameset
        );

        Menu markdownMenu = new Menu("Markdown");

        MenuItem markdownEditor = new MenuItem(AppVariables.message("MarkdownEditer"));
        markdownEditor.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.MarkdownEditorFxml);
        });

        MenuItem markdownToHtml = new MenuItem(AppVariables.message("MarkdownToHtml"));
        markdownToHtml.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.MarkdownToHtmlFxml);
        });

        MenuItem MarkdownToText = new MenuItem(AppVariables.message("MarkdownToText"));
        MarkdownToText.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.MarkdownToTextFxml);
        });

        MenuItem MarkdownToPdf = new MenuItem(AppVariables.message("MarkdownToPdf"));
        MarkdownToPdf.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.MarkdownToPdfFxml);
        });

        markdownMenu.getItems().addAll(
                markdownEditor, new SeparatorMenuItem(),
                markdownToHtml, MarkdownToText, MarkdownToPdf
        );

        Menu msMenu = new Menu(message("MicrosoftDocumentFormats"));

        MenuItem ExtractTextsFromMS = new MenuItem(AppVariables.message("ExtractTextsFromMS"));
        ExtractTextsFromMS.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ExtractTextsFromMSFxml);
        });

        MenuItem WordView = new MenuItem(AppVariables.message("WordView"));
        WordView.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.WordViewFxml);
        });

        MenuItem WordToHtml = new MenuItem(AppVariables.message("WordToHtml"));
        WordToHtml.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.WordToHtmlFxml);
        });

        MenuItem PptView = new MenuItem(AppVariables.message("PptView"));
        PptView.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PptViewFxml);
        });

        MenuItem PptToImages = new MenuItem(AppVariables.message("PptToImages"));
        PptToImages.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PptToImagesFxml);
        });

        MenuItem PptExtract = new MenuItem(AppVariables.message("PptExtract"));
        PptExtract.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PptExtractFxml);
        });

        MenuItem PptxMerge = new MenuItem(AppVariables.message("PptxMerge"));
        PptxMerge.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PptxMergeFxml);
        });

        MenuItem PptSplit = new MenuItem(AppVariables.message("PptSplit"));
        PptSplit.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.PptSplitFxml);
        });

        MenuItem imagesCombinePPT = new MenuItem(AppVariables.message("ImagesCombinePPT"));
        imagesCombinePPT.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ImagesEditorFxml);
        });

        MenuItem PptPlay = new MenuItem(AppVariables.message("PptPlay"));
        PptPlay.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ImagesPlayFxml);
        });

        MenuItem TextInMyBoxClipboard = new MenuItem(AppVariables.message("TextInMyBoxClipboard"));
        TextInMyBoxClipboard.setOnAction((ActionEvent event1) -> {
            TextInMyBoxClipboardController.oneOpen();
        });

        MenuItem TextInSystemClipboard = new MenuItem(AppVariables.message("TextInSystemClipboard"));
        TextInSystemClipboard.setOnAction((ActionEvent event1) -> {
            TextInSystemClipboardController.oneOpen();
        });

        msMenu.getItems().addAll(
                WordView, WordToHtml, new SeparatorMenuItem(),
                PptView, PptToImages, PptExtract, PptSplit, PptxMerge, imagesCombinePPT, PptPlay, new SeparatorMenuItem(),
                ExtractTextsFromMS
        );

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                Notes, new SeparatorMenuItem(),
                pdfMenu, new SeparatorMenuItem(),
                markdownMenu, new SeparatorMenuItem(),
                htmlMenu, new SeparatorMenuItem(),
                textsMenu, new SeparatorMenuItem(),
                msMenu, new SeparatorMenuItem(),
                bytesEditer, new SeparatorMenuItem(),
                TextInMyBoxClipboard, TextInSystemClipboard
        );

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem closeMenu = new MenuItem(message("PopupClose"));
        closeMenu.setStyle("-fx-text-fill: #2e598a;");
        closeMenu.setOnAction((ActionEvent cevent) -> {
            popMenu.hide();
            popMenu = null;
        });
        popMenu.getItems().add(closeMenu);

        showMenu(documentBox, event);

        view.setImage(new Image("img/DocumentTools.png"));
        text.setText(message("DocumentToolsImageTips"));
        text.setWrappingWidth(500);
        locateImage(documentBox, true);

    }

    @FXML
    protected void showImageMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem imageViewer = new MenuItem(AppVariables.message("ImageViewer"));
        imageViewer.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ImageViewerFxml);
        });

        MenuItem imagesBrowser = new MenuItem(AppVariables.message("ImagesBrowser"));
        imagesBrowser.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ImagesBrowserFxml);
        });

        MenuItem ImageAnalyse = new MenuItem(AppVariables.message("ImageAnalyse"));
        ImageAnalyse.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ImageAnalyseFxml);
        });

        MenuItem ImagesPlay = new MenuItem(AppVariables.message("ImagesPlay"));
        ImagesPlay.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ImagesPlayFxml);
        });

        MenuItem ImageManufacture = new MenuItem(AppVariables.message("ImageManufacture"));
        ImageManufacture.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ImageManufactureFxml);
        });

        MenuItem ImagesEditor = new MenuItem(AppVariables.message("ImagesEditor"));
        ImagesEditor.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ImagesEditorFxml);
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

        MenuItem ManageColors = new MenuItem(AppVariables.message("ManageColors"));
        ManageColors.setOnAction((ActionEvent event1) -> {
            ColorsManageController.oneOpen(null);
        });

        MenuItem ImagesInMyBoxClipboard = new MenuItem(AppVariables.message("ImagesInMyBoxClipboard"));
        ImagesInMyBoxClipboard.setOnAction((ActionEvent event1) -> {
            ImagesInMyBoxClipboardController.oneOpen();
        });

        MenuItem ImagesInSystemClipboard = new MenuItem(AppVariables.message("ImagesInSystemClipboard"));
        ImagesInSystemClipboard.setOnAction((ActionEvent event1) -> {
            ImagesInSystemClipboardController.oneOpen();
        });

        Menu miscellaneousMenu = new Menu(AppVariables.message("Miscellaneous"));
        miscellaneousMenu.getItems().addAll(
                convolutionKernelManager, pixelsCalculator
        );

        Menu manufactureBatchMenu = makeImageBatchToolsMenu();
        Menu partMenu = makeImagePartMenu();
        Menu mergeMenu = makeImageMergeMenu();
        Menu csMenu = makeColorSpaceMenu();

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                imageViewer, imagesBrowser, ImageAnalyse, ImagesPlay, new SeparatorMenuItem(),
                ImageManufacture, manufactureBatchMenu, ImagesEditor, mergeMenu, partMenu, imageConverterBatch, new SeparatorMenuItem(),
                imageOCR, imageOCRBatch, new SeparatorMenuItem(),
                ManageColors, csMenu, new SeparatorMenuItem(),
                ImagesInMyBoxClipboard, ImagesInSystemClipboard, miscellaneousMenu);

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem closeMenu = new MenuItem(message("PopupClose"));
        closeMenu.setStyle("-fx-text-fill: #2e598a;");
        closeMenu.setOnAction((ActionEvent cevent) -> {
            popMenu.hide();
            popMenu = null;
        });
        popMenu.getItems().add(closeMenu);

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

        MenuItem imagePasteMenu = new MenuItem(AppVariables.message("Paste"));
        imagePasteMenu.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageManufactureBatchPasteFxml);
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
        manufactureBatchMenu.getItems().addAll(imageSizeMenu, imageCropMenu, imagePasteMenu,
                imageColorMenu, imageEffectsMenu, imageEnhancementMenu, imageReplaceColorMenu,
                imageTextMenu, imageArcMenu, imageShadowMenu, imageTransformMenu, imageMarginsMenu);
        return manufactureBatchMenu;

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

        MenuItem ImagesSplice = new MenuItem(AppVariables.message("ImagesSplice"));
        ImagesSplice.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImagesSpliceFxml);
        });

        MenuItem imageAlphaAdd = new MenuItem(AppVariables.message("ImageAlphaAdd"));
        imageAlphaAdd.setOnAction((ActionEvent event) -> {
            loadScene(CommonValues.ImageAlphaAddBatchFxml);
        });

        Menu manufactureSubMenu = new Menu(AppVariables.message("MergeImages"));
        manufactureSubMenu.getItems().addAll(ImagesSplice, imageAlphaAdd);
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
    protected void showNetworkMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem weiboSnap = new MenuItem(AppVariables.message("WeiboSnap"));
        weiboSnap.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.WeiboSnapFxml);
        });

        MenuItem webBrowserHtml = new MenuItem(AppVariables.message("WebBrowser"));
        webBrowserHtml.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.WebBrowserFxml);
        });

        MenuItem WebFavorites = new MenuItem(AppVariables.message("WebFavorites"));
        WebFavorites.setOnAction((ActionEvent event1) -> {
            WebFavoritesController.oneOpen();
            closeStage();
        });

        MenuItem WebHistories = new MenuItem(AppVariables.message("WebHistories"));
        WebHistories.setOnAction((ActionEvent event1) -> {
            WebHistoriesController.oneOpen();
            closeStage();
        });

        MenuItem ConvertUrl = new MenuItem(AppVariables.message("ConvertUrl"));
        ConvertUrl.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.NetworkConvertUrlFxml);
        });

        MenuItem QueryAddress = new MenuItem(AppVariables.message("QueryNetworkAddress"));
        QueryAddress.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.NetworkQueryAddressFxml);
        });

        MenuItem QueryDNSBatch = new MenuItem(AppVariables.message("QueryDNSBatch"));
        QueryDNSBatch.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.NetworkQueryDNSBatchFxml);
        });

        MenuItem DownloadFirstLevelLinks = new MenuItem(AppVariables.message("DownloadFirstLevelLinks"));
        DownloadFirstLevelLinks.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.DownloadFirstLevelLinksFxml);
        });

        MenuItem SecurityCertificates = new MenuItem(AppVariables.message("SecurityCertificates"));
        SecurityCertificates.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.SecurityCertificatesFxml);
        });

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                DownloadFirstLevelLinks, weiboSnap, new SeparatorMenuItem(),
                webBrowserHtml, WebFavorites, WebHistories, new SeparatorMenuItem(),
                QueryAddress, QueryDNSBatch, ConvertUrl, new SeparatorMenuItem(),
                SecurityCertificates
        );

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem closeMenu = new MenuItem(message("PopupClose"));
        closeMenu.setStyle("-fx-text-fill: #2e598a;");
        closeMenu.setOnAction((ActionEvent cevent) -> {
            popMenu.hide();
            popMenu = null;
        });
        popMenu.getItems().add(closeMenu);

        showMenu(networkBox, event);

        view.setImage(new Image("img/NetworkTools.png"));
        text.setText(message("NetworkToolsImageTips"));
        locateImage(networkBox, true);

    }

    @FXML
    protected void showFileMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem filesArrangement = new MenuItem(AppVariables.message("FilesArrangement"));
        filesArrangement.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesArrangementFxml);
        });

        MenuItem dirSynchronize = new MenuItem(AppVariables.message("DirectorySynchronize"));
        dirSynchronize.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.DirectorySynchronizeFxml);
        });

        MenuItem filesRename = new MenuItem(AppVariables.message("FilesRename"));
        filesRename.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesRenameFxml);
        });

        MenuItem fileCut = new MenuItem(AppVariables.message("FileCut"));
        fileCut.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FileCutFxml);
        });

        MenuItem filesMerge = new MenuItem(AppVariables.message("FilesMerge"));
        filesMerge.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesMergeFxml);
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

        MenuItem filesDelete = new MenuItem(AppVariables.message("FilesDelete"));
        filesDelete.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesDeleteFxml);
        });

        MenuItem DeleteEmptyDirectories = new MenuItem(AppVariables.message("DeleteEmptyDirectories"));
        DeleteEmptyDirectories.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesDeleteEmptyDirFxml);
        });

        MenuItem DeleteSysTemporaryPathFiles = new MenuItem(AppVariables.message("DeleteSysTemporaryPathFiles"));
        DeleteSysTemporaryPathFiles.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesDeleteSysTempFxml);
        });

        MenuItem DeleteNestedDirectories = new MenuItem(AppVariables.message("DeleteNestedDirectories"));
        DeleteNestedDirectories.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FilesDeleteNestedDirFxml);
        });

        Menu fileDeleteMenu = new Menu(AppVariables.message("FilesDelete"));
        fileDeleteMenu.getItems().addAll(
                DeleteSysTemporaryPathFiles, DeleteEmptyDirectories, filesDelete, DeleteNestedDirectories
        );

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

        Menu archiveCompressMenu = new Menu(AppVariables.message("FilesArchiveCompress"));
        archiveCompressMenu.getItems().addAll(
                filesDecompressUnarchive, filesDecompressUnarchiveBatch,
                filesArchiveCompress, filesCompress
        );

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                filesArrangement, dirSynchronize, new SeparatorMenuItem(),
                fileCut, filesMerge, new SeparatorMenuItem(),
                filesFind, filesRedundancy, filesCompare, new SeparatorMenuItem(),
                filesRename, filesCopy, filesMove, new SeparatorMenuItem(),
                fileDeleteMenu, new SeparatorMenuItem(),
                archiveCompressMenu
        );

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem closeMenu = new MenuItem(message("PopupClose"));
        closeMenu.setStyle("-fx-text-fill: #2e598a;");
        closeMenu.setOnAction((ActionEvent cevent) -> {
            popMenu.hide();
            popMenu = null;
        });
        popMenu.getItems().add(closeMenu);

        showMenu(fileBox, event);

        view.setImage(new Image("img/FileTools.png"));
        text.setText(message("FileToolsImageTips"));
        locateImage(fileBox, false);
    }

    @FXML
    protected void showSettingsMenu(MouseEvent event) {
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

        List<String> languages = ConfigTools.userLanguages();
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
                    MyBoxLog.debug(e.toString());
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
        popMenu.getItems().addAll(
                derbyServer, new SeparatorMenuItem(),
                mybox, new SeparatorMenuItem(),
                settings);

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem closeMenu = new MenuItem(message("PopupClose"));
        closeMenu.setStyle("-fx-text-fill: #2e598a;");
        closeMenu.setOnAction((ActionEvent cevent) -> {
            popMenu.hide();
            popMenu = null;
        });
        popMenu.getItems().add(closeMenu);

        showMenu(settingsBox, event);

        view.setImage(new Image("img/Settings.png"));
        text.setText(message("SettingsImageTips"));
        locateImage(settingsBox, true);
    }

    @FXML
    protected void showRecentMenu(MouseEvent event) {
        hideMenu(event);

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(getRecentMenu());

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem closeMenu = new MenuItem(message("PopupClose"));
        closeMenu.setStyle("-fx-text-fill: #2e598a;");
        closeMenu.setOnAction((ActionEvent cevent) -> {
            popMenu.hide();
            popMenu = null;
        });
        popMenu.getItems().add(closeMenu);

        showMenu(recentBox, event);

        view.setImage(new Image("img/RecentAccess.png"));
        text.setText(message("RecentAccessImageTips"));
        locateImage(recentBox, true);
    }

    @FXML
    protected void showDataMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem EditExcel = new MenuItem(AppVariables.message("EditExcel"));
        EditExcel.setOnAction((ActionEvent event1) -> {
            DataFileExcelController c = (DataFileExcelController) loadScene(CommonValues.DataFileExcelFxml);
            c.newSheet(3, 3);
        });

        MenuItem ExcelConvert = new MenuItem(AppVariables.message("ExcelConvert"));
        ExcelConvert.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.DataFileExcelConvertFxml);
        });

        MenuItem ExtractTextsFromMS = new MenuItem(AppVariables.message("ExtractTextsFromMS"));
        ExtractTextsFromMS.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ExtractTextsFromMSFxml);
        });

        MenuItem ExcelMerge = new MenuItem(AppVariables.message("ExcelMerge"));
        ExcelMerge.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.DataFileExcelMergeFxml);
        });

        MenuItem EditCSV = new MenuItem(AppVariables.message("EditCSV"));
        EditCSV.setOnAction((ActionEvent event1) -> {
            DataFileCSVController c = (DataFileCSVController) loadScene(CommonValues.DataFileCSVFxml);
            c.newSheet(3, 3);
        });

        MenuItem CsvConvert = new MenuItem(AppVariables.message("CsvConvert"));
        CsvConvert.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.DataFileCSVConvertFxml);
        });

        MenuItem CsvMerge = new MenuItem(AppVariables.message("CsvMerge"));
        CsvMerge.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.DataFileCSVMergeFxml);
        });

        Menu DataFile = new Menu(AppVariables.message("DataFile"));
        DataFile.getItems().addAll(
                EditCSV, CsvConvert, CsvMerge, new SeparatorMenuItem(),
                EditExcel, ExcelConvert, ExcelMerge, ExtractTextsFromMS
        );

        MenuItem DataClipboard = new MenuItem(AppVariables.message("DataClipboard"));
        DataClipboard.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.DataClipboardFxml);
        });

        MenuItem Dataset = new MenuItem(AppVariables.message("Dataset"));
        Dataset.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.DatasetFxml);
        });

        MenuItem GeographyCode = new MenuItem(AppVariables.message("GeographyCode"));
        GeographyCode.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.GeographyCodeFxml);
        });

        MenuItem LocationInMap = new MenuItem(AppVariables.message("LocationInMap"));
        LocationInMap.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.LocationInMapFxml);
        });

        MenuItem LocationData = new MenuItem(AppVariables.message("LocationData"));
        LocationData.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.LocationDataFxml);
        });

        MenuItem ConvertCoordinate = new MenuItem(AppVariables.message("ConvertCoordinate"));
        ConvertCoordinate.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.ConvertCoordinateFxml);
        });

        MenuItem LocationsDataInMap = new MenuItem(AppVariables.message("LocationsDataInMap"));
        LocationsDataInMap.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.LocationsDataInMapFxml);
        });

        Menu locationApplicationsMenu = new Menu(AppVariables.message("LocationApplications"));
        locationApplicationsMenu.getItems().addAll(
                LocationData, LocationsDataInMap
        );

        MenuItem EpidemicReport = new MenuItem(AppVariables.message("EpidemicReport"));
        EpidemicReport.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.EpidemicReportsFxml);
        });

        MenuItem MatricesManage = new MenuItem(AppVariables.message("MatricesManage"));
        MatricesManage.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.MatricesManageFxml);
        });

        MenuItem MatrixUnaryCalculation = new MenuItem(AppVariables.message("MatrixUnaryCalculation"));
        MatrixUnaryCalculation.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.MatrixUnaryCalculationFxml);
        });

        MenuItem MatricesBinaryCalculation = new MenuItem(AppVariables.message("MatricesBinaryCalculation"));
        MatricesBinaryCalculation.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.MatricesBinaryCalculationFxml);
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

        MenuItem Base64Conversion = new MenuItem(AppVariables.message("Base64Conversion"));
        Base64Conversion.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.Base64Fxml);
        });

        MenuItem TTC2TTF = new MenuItem(AppVariables.message("TTC2TTF"));
        TTC2TTF.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FileTTC2TTFFxml);
        });

        Menu miscellaneousMenu = new Menu(AppVariables.message("Miscellaneous"));
        miscellaneousMenu.getItems().addAll(
                barcodeCreator, barcodeDecoder, new SeparatorMenuItem(),
                messageDigest, Base64Conversion, new SeparatorMenuItem(),
                TTC2TTF
        );

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                DataFile, DataClipboard, new SeparatorMenuItem(),
                MatricesManage, MatrixUnaryCalculation, MatricesBinaryCalculation, new SeparatorMenuItem(),
                GeographyCode, LocationInMap, LocationData, ConvertCoordinate, new SeparatorMenuItem(),
                EpidemicReport, new SeparatorMenuItem(),
                miscellaneousMenu
        );

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem closeMenu = new MenuItem(message("PopupClose"));
        closeMenu.setStyle("-fx-text-fill: #2e598a;");
        closeMenu.setOnAction((ActionEvent cevent) -> {
            popMenu.hide();
            popMenu = null;
        });
        popMenu.getItems().add(closeMenu);

        showMenu(dataBox, event);

        view.setImage(new Image("img/DataTools.png"));
        text.setText(message("DataToolsImageTips"));
        locateImage(dataBox, true);
    }

    @FXML
    protected void showMediaMenu(MouseEvent event) {
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

        Menu FFmpegConversionMenu = new Menu(AppVariables.message("FFmpegConvertMedias"));
        FFmpegConversionMenu.getItems().addAll(
                FFmpegConversionFiles, FFmpegConversionStreams);

        MenuItem FFmpegMergeImages = new MenuItem(AppVariables.message("FFmpegMergeImagesInformation"));
        FFmpegMergeImages.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FFmpegMergeImagesFxml);
        });

        MenuItem FFmpegMergeImageFiles = new MenuItem(AppVariables.message("FFmpegMergeImagesFiles"));
        FFmpegMergeImageFiles.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FFmpegMergeImageFilesFxml);
        });

        MenuItem screenRecorder = new MenuItem(AppVariables.message("FFmpegScreenRecorder"));
        screenRecorder.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.FFmpegScreenRecorderFxml);
        });

        Menu FFmpegMergeMenu = new Menu(AppVariables.message("FFmpegMergeImages"));
        FFmpegMergeMenu.getItems().addAll(
                FFmpegMergeImageFiles, FFmpegMergeImages);

        MenuItem alarmClock = new MenuItem(AppVariables.message("AlarmClock"));
        alarmClock.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.AlarmClockFxml);
        });

        MenuItem GameElimniation = new MenuItem(AppVariables.message("GameElimniation"));
        GameElimniation.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.GameElimniationFxml);
        });

        MenuItem GameMine = new MenuItem(AppVariables.message("GameMine"));
        GameMine.setOnAction((ActionEvent event1) -> {
            loadScene(CommonValues.GameMineFxml);
        });

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                mediaPlayer, mediaLists, new SeparatorMenuItem(),
                screenRecorder,
                FFmpegConversionMenu, FFmpegMergeMenu,
                FFprobe, FFmpegInformation, new SeparatorMenuItem(),
                alarmClock, new SeparatorMenuItem(),
                GameElimniation, GameMine
        );

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem closeMenu = new MenuItem(message("PopupClose"));
        closeMenu.setStyle("-fx-text-fill: #2e598a;");
        closeMenu.setOnAction((ActionEvent cevent) -> {
            popMenu.hide();
            popMenu = null;
        });
        popMenu.getItems().add(closeMenu);

        showMenu(mediaBox, event);

        view.setImage(new Image("img/MediaTools.png"));
        text.setText(message("MediaToolsImageTips"));
        locateImage(mediaBox, false);
    }

    @FXML
    protected void showAboutMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem About = new MenuItem(AppVariables.message("About"));
        About.setOnAction((ActionEvent event1) -> {
            FxmlWindow.about();
        });

        MenuItem FunctionsList = new MenuItem(AppVariables.message("FunctionsList"));
        FunctionsList.setOnAction((ActionEvent event1) -> {
            openStage(CommonValues.FunctionsListFxml);
        });

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                About, FunctionsList
        );

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem closeMenu = new MenuItem(message("PopupClose"));
        closeMenu.setStyle("-fx-text-fill: #2e598a;");
        closeMenu.setOnAction((ActionEvent cevent) -> {
            popMenu.hide();
            popMenu = null;
        });
        popMenu.getItems().add(closeMenu);

        showMenu(aboutBox, event);

        view.setImage(new Image("img/About.png"));
        text.setText(message("AboutImageTips"));
        locateImage(aboutBox, true);
    }

}
