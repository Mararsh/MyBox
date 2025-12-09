package mara.mybox.fxml;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.ColorsManageController;
import mara.mybox.controller.DataInMyBoxClipboardController;
import mara.mybox.controller.DataInSystemClipboardController;
import mara.mybox.controller.DataTreeController;
import mara.mybox.controller.GeographyCodeController;
import mara.mybox.controller.ImageInMyBoxClipboardController;
import mara.mybox.controller.ImageInSystemClipboardController;
import mara.mybox.controller.ImagesPlayController;
import mara.mybox.controller.TextInMyBoxClipboardController;
import mara.mybox.controller.TextInSystemClipboardController;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2025-12-9
 * @License Apache License Version 2.0
 */
public class MenuTools {

    public static List<MenuItem> toolsMenu(BaseController controller, Event event) {
        Menu documentMenu = new Menu(message("Document"));
        documentMenu.getItems().addAll(documentToolsMenu(controller, event));

        Menu imageMenu = new Menu(message("Image"));
        imageMenu.getItems().addAll(imageToolsMenu(controller, event));

        Menu fileMenu = new Menu(message("File"));
        fileMenu.getItems().addAll(fileToolsMenu(controller, event));

        Menu networkMenu = new Menu(message("Network"));
        documentMenu.getItems().addAll(networkToolsMenu(controller, event));

        Menu dataMenu = new Menu(message("Data"));
        dataMenu.getItems().addAll(dataToolsMenu(controller, event));

        Menu mediaMenu = new Menu(message("Media"));
        mediaMenu.getItems().addAll(mediaToolsMenu(controller, event));

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(documentMenu, imageMenu, fileMenu,
                networkMenu, dataMenu, mediaMenu));

        return items;
    }

    public static List<MenuItem> documentToolsMenu(BaseController controller, Event event) {

        Menu treeMenu = new Menu(message("InformationInTree"));

        MenuItem TextTree = new MenuItem(message("TextTree"));
        TextTree.setOnAction((ActionEvent event1) -> {
            DataTreeController.textTree(controller, true);
        });

        MenuItem HtmlTree = new MenuItem(message("HtmlTree"));
        HtmlTree.setOnAction((ActionEvent event1) -> {
            DataTreeController.htmlTree(controller, true);
        });

        treeMenu.getItems().addAll(TextTree, HtmlTree);

        Menu pdfMenu = new Menu("PDF");

        MenuItem pdfView = new MenuItem(message("PdfView"));
        pdfView.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.PdfViewFxml);
        });

        MenuItem PdfPlay = new MenuItem(message("PdfPlay"));
        PdfPlay.setOnAction((ActionEvent event1) -> {
            ImagesPlayController c = (ImagesPlayController) controller.loadScene(Fxmls.ImagesPlayFxml);
            c.setAsPDF();
        });

        MenuItem PDFAttributes = new MenuItem(message("PDFAttributes"));
        PDFAttributes.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.PdfAttributesFxml);
        });

        MenuItem PDFAttributesBatch = new MenuItem(message("PDFAttributesBatch"));
        PDFAttributesBatch.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.PdfAttributesBatchFxml);
        });

        MenuItem pdfExtractImagesBatch = new MenuItem(message("PdfExtractImagesBatch"));
        pdfExtractImagesBatch.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.PdfExtractImagesBatchFxml);
        });

        MenuItem pdfExtractTextsBatch = new MenuItem(message("PdfExtractTextsBatch"));
        pdfExtractTextsBatch.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.PdfExtractTextsBatchFxml);
        });

        MenuItem pdfConvertImagesBatch = new MenuItem(message("PdfConvertImagesBatch"));
        pdfConvertImagesBatch.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.PdfConvertImagesBatchFxml);
        });

        MenuItem pdfOcrBatch = new MenuItem(message("PdfOCRBatch"));
        pdfOcrBatch.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.PdfOCRBatchFxml);
        });

        MenuItem pdfConvertHtmlsBatch = new MenuItem(message("PdfConvertHtmlsBatch"));
        pdfConvertHtmlsBatch.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.PdfConvertHtmlsBatchFxml);
        });

        MenuItem imagesCombinePdf = new MenuItem(message("ImagesCombinePdf"));
        imagesCombinePdf.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.ImagesEditorFxml);
        });

        MenuItem pdfCompressImagesBatch = new MenuItem(message("PdfCompressImagesBatch"));
        pdfCompressImagesBatch.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.PdfCompressImagesBatchFxml);
        });

        MenuItem PdfImagesConvertBatch = new MenuItem(message("PdfImagesConvertBatch"));
        PdfImagesConvertBatch.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.PdfImagesConvertBatchFxml);
        });

        MenuItem pdfMerge = new MenuItem(message("MergePdf"));
        pdfMerge.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.PdfMergeFxml);
        });

        MenuItem PdfSplitBatch = new MenuItem(message("PdfSplitBatch"));
        PdfSplitBatch.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.PdfSplitBatchFxml);
        });

        MenuItem PdfAddWatermarkBatch = new MenuItem(message("PdfAddWatermarkBatch"));
        PdfAddWatermarkBatch.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.PdfAddWatermarkBatchFxml);
        });

        pdfMenu.getItems().addAll(
                pdfView, PdfPlay, new SeparatorMenuItem(),
                pdfConvertImagesBatch, PdfImagesConvertBatch, pdfConvertHtmlsBatch, pdfCompressImagesBatch, new SeparatorMenuItem(),
                pdfExtractImagesBatch, pdfExtractTextsBatch, pdfOcrBatch, PdfAddWatermarkBatch, new SeparatorMenuItem(),
                PdfSplitBatch, pdfMerge, imagesCombinePdf, new SeparatorMenuItem(),
                PDFAttributes, PDFAttributesBatch
        );

        Menu textsMenu = new Menu(message("Texts"));

        MenuItem textEditer = new MenuItem(message("TextEditer"));
        textEditer.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.TextEditorFxml);
        });

        MenuItem TextConvert = new MenuItem(message("TextConvertSplit"));
        TextConvert.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.TextFilesConvertFxml);
        });

        MenuItem TextMerge = new MenuItem(message("TextFilesMerge"));
        TextMerge.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.TextFilesMergeFxml);
        });

        MenuItem TextFindBatch = new MenuItem(message("TextFindBatch"));
        TextFindBatch.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.TextFindBatchFxml);
        });

        MenuItem TextReplaceBatch = new MenuItem(message("TextReplaceBatch"));
        TextReplaceBatch.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.TextReplaceBatchFxml);
        });

        MenuItem TextFilterBatch = new MenuItem(message("TextFilterBatch"));
        TextFilterBatch.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.TextFilterBatchFxml);
        });

        MenuItem TextToHtml = new MenuItem(message("TextToHtml"));
        TextToHtml.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.TextToHtmlFxml);
        });

        MenuItem TextToPdf = new MenuItem(message("TextToPdf"));
        TextToPdf.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.TextToPdfFxml);
        });

        textsMenu.getItems().addAll(
                textEditer, TextFindBatch, TextReplaceBatch, TextFilterBatch, TextConvert, TextMerge, TextToHtml, TextToPdf
        );

        Menu bytesMenu = new Menu(message("Bytes"));

        MenuItem bytesEditer = new MenuItem(message("BytesEditer"));
        bytesEditer.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.BytesEditorFxml);
        });

        MenuItem BytesFindBatch = new MenuItem(message("BytesFindBatch"));
        BytesFindBatch.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.BytesFindBatchFxml);
        });

        bytesMenu.getItems().addAll(
                bytesEditer, BytesFindBatch
        );

        Menu htmlMenu = new Menu(message("Html"));

        MenuItem htmlEditor = new MenuItem(message("HtmlEditor"));
        htmlEditor.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.HtmlEditorFxml);
        });

        MenuItem htmlToMarkdown = new MenuItem(message("HtmlToMarkdown"));
        htmlToMarkdown.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.HtmlToMarkdownFxml);
        });

        MenuItem HtmlToText = new MenuItem(message("HtmlToText"));
        HtmlToText.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.HtmlToTextFxml);
        });

        MenuItem HtmlToPdf = new MenuItem(message("HtmlToPdf"));
        HtmlToPdf.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.HtmlToPdfFxml);
        });

        MenuItem HtmlSetCharset = new MenuItem(message("HtmlSetCharset"));
        HtmlSetCharset.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.HtmlSetCharsetFxml);
        });

        MenuItem HtmlSetStyle = new MenuItem(message("HtmlSetStyle"));
        HtmlSetStyle.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.HtmlSetStyleFxml);
        });

        MenuItem HtmlSetEquiv = new MenuItem(message("HtmlSetEquiv"));
        HtmlSetStyle.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.HtmlSetEquivFxml);
        });

        MenuItem HtmlSnap = new MenuItem(message("HtmlSnap"));
        HtmlSnap.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.HtmlSnapFxml);
        });

        MenuItem HtmlTypesetting = new MenuItem(message("HtmlTypesetting"));
        HtmlTypesetting.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.HtmlTypesettingFxml);
        });

        MenuItem WebFind = new MenuItem(message("WebFind"));
        WebFind.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.HtmlFindFxml);
        });

        MenuItem HtmlExtractTables = new MenuItem(message("HtmlExtractTables"));
        HtmlExtractTables.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.HtmlExtractTablesFxml);
        });

        MenuItem WebElements = new MenuItem(message("WebElements"));
        WebElements.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.HtmlElementsFxml);
        });

        MenuItem HtmlMergeAsHtml = new MenuItem(message("HtmlMergeAsHtml"));
        HtmlMergeAsHtml.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.HtmlMergeAsHtmlFxml);
        });

        MenuItem HtmlMergeAsMarkdown = new MenuItem(message("HtmlMergeAsMarkdown"));
        HtmlMergeAsMarkdown.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.HtmlMergeAsMarkdownFxml);
        });

        MenuItem HtmlMergeAsPDF = new MenuItem(message("HtmlMergeAsPDF"));
        HtmlMergeAsPDF.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.HtmlMergeAsPDFFxml);
        });

        MenuItem HtmlMergeAsText = new MenuItem(message("HtmlMergeAsText"));
        HtmlMergeAsText.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.HtmlMergeAsTextFxml);
        });

        MenuItem HtmlFrameset = new MenuItem(message("HtmlFrameset"));
        HtmlFrameset.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.HtmlFramesetFxml);
        });

        htmlMenu.getItems().addAll(
                htmlEditor, WebFind, WebElements, HtmlSnap, HtmlExtractTables, new SeparatorMenuItem(),
                HtmlTypesetting, htmlToMarkdown, HtmlToText, HtmlToPdf, HtmlSetCharset, HtmlSetStyle, HtmlSetEquiv, new SeparatorMenuItem(),
                HtmlMergeAsHtml, HtmlMergeAsMarkdown, HtmlMergeAsPDF, HtmlMergeAsText, HtmlFrameset
        );

        Menu markdownMenu = new Menu("Markdown");

        MenuItem markdownEditor = new MenuItem(message("MarkdownEditer"));
        markdownEditor.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.MarkdownEditorFxml);
        });

        MenuItem markdownOptions = new MenuItem(message("MarkdownOptions"));
        markdownOptions.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.MarkdownOptionsFxml);
        });

        MenuItem MarkdownTypesetting = new MenuItem(message("MarkdownTypesetting"));
        MarkdownTypesetting.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.MarkdownTypesettingFxml);
        });

        MenuItem markdownToHtml = new MenuItem(message("MarkdownToHtml"));
        markdownToHtml.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.MarkdownToHtmlFxml);
        });

        MenuItem MarkdownToText = new MenuItem(message("MarkdownToText"));
        MarkdownToText.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.MarkdownToTextFxml);
        });

        MenuItem MarkdownToPdf = new MenuItem(message("MarkdownToPdf"));
        MarkdownToPdf.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.MarkdownToPdfFxml);
        });

        markdownMenu.getItems().addAll(
                markdownEditor, markdownOptions, new SeparatorMenuItem(),
                MarkdownTypesetting, markdownToHtml, MarkdownToText, MarkdownToPdf
        );

        Menu jsonMenu = new Menu("JSON");

        MenuItem jsonEditorMenu = new MenuItem(message("JsonEditor"));
        jsonEditorMenu.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.JsonEditorFxml);
        });

        MenuItem jsonTypesettingMenu = new MenuItem(message("JsonTypesetting"));
        jsonTypesettingMenu.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.JsonTypesettingFxml);
        });

        jsonMenu.getItems().addAll(
                jsonEditorMenu, jsonTypesettingMenu
        );

        Menu xmlMenu = new Menu("XML");

        MenuItem xmlEditorMenu = new MenuItem(message("XmlEditor"));
        xmlEditorMenu.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.XmlEditorFxml);
        });

        MenuItem xmlTypesettingMenu = new MenuItem(message("XmlTypesetting"));
        xmlTypesettingMenu.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.XmlTypesettingFxml);
        });

        xmlMenu.getItems().addAll(
                xmlEditorMenu, xmlTypesettingMenu
        );

        Menu msMenu = new Menu(message("MicrosoftDocumentFormats"));

        MenuItem ExtractTextsFromMS = new MenuItem(message("ExtractTextsFromMS"));
        ExtractTextsFromMS.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.ExtractTextsFromMSFxml);
        });

        MenuItem WordView = new MenuItem(message("WordView"));
        WordView.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.WordViewFxml);
        });

        MenuItem WordToHtml = new MenuItem(message("WordToHtml"));
        WordToHtml.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.WordToHtmlFxml);
        });

        MenuItem WordToPdf = new MenuItem(message("WordToPdf"));
        WordToPdf.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.WordToPdfFxml);
        });

        MenuItem PptView = new MenuItem(message("PptView"));
        PptView.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.PptViewFxml);
        });

        MenuItem PptToImages = new MenuItem(message("PptToImages"));
        PptToImages.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.PptToImagesFxml);
        });

        MenuItem PptToPdf = new MenuItem(message("PptToPdf"));
        PptToPdf.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.PptToPdfFxml);
        });

        MenuItem PptExtract = new MenuItem(message("PptExtract"));
        PptExtract.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.PptExtractFxml);
        });

        MenuItem PptxMerge = new MenuItem(message("PptxMerge"));
        PptxMerge.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.PptxMergeFxml);
        });

        MenuItem PptSplit = new MenuItem(message("PptSplit"));
        PptSplit.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.PptSplitFxml);
        });

        MenuItem imagesCombinePPT = new MenuItem(message("ImagesCombinePPT"));
        imagesCombinePPT.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.ImagesEditorFxml);
        });

        MenuItem PptPlay = new MenuItem(message("PptPlay"));
        PptPlay.setOnAction((ActionEvent event1) -> {
            ImagesPlayController c = (ImagesPlayController) controller.loadScene(Fxmls.ImagesPlayFxml);
            c.setAsPPT();
        });

        MenuItem TextInMyBoxClipboard = new MenuItem(message("TextInMyBoxClipboard"));
        TextInMyBoxClipboard.setOnAction((ActionEvent event1) -> {
            TextInMyBoxClipboardController.oneOpen();
        });

        MenuItem TextInSystemClipboard = new MenuItem(message("TextInSystemClipboard"));
        TextInSystemClipboard.setOnAction((ActionEvent event1) -> {
            TextInSystemClipboardController.oneOpen();
        });

        msMenu.getItems().addAll(
                WordView, WordToHtml, WordToPdf, new SeparatorMenuItem(),
                PptView, PptToImages, PptToPdf, PptExtract, PptSplit, PptxMerge, imagesCombinePPT, PptPlay, new SeparatorMenuItem(),
                ExtractTextsFromMS
        );

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(treeMenu, new SeparatorMenuItem(),
                pdfMenu, markdownMenu, jsonMenu, xmlMenu, htmlMenu, textsMenu, msMenu, bytesMenu, new SeparatorMenuItem(),
                TextInMyBoxClipboard, TextInSystemClipboard));

        return items;

    }

    public static List<MenuItem> imageToolsMenu(BaseController controller, Event event) {
        MenuItem EditImage = new MenuItem(message("EditImage"));
        EditImage.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.ImageEditorFxml);
        });

        MenuItem imageScope = new MenuItem(message("ImageScope"));
        imageScope.setOnAction((ActionEvent event1) -> {
            DataTreeController.imageScope(controller, true);
        });

        MenuItem imageOptions = new MenuItem(message("ImageOptions"));
        imageOptions.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.ImageShapeOptionsFxml);
        });

        MenuItem ManageColors = new MenuItem(message("ManageColors"));
        ManageColors.setOnAction((ActionEvent event1) -> {
            ColorsManageController.oneOpen();
        });

        MenuItem QueryColor = new MenuItem(message("QueryColor"));
        QueryColor.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.ColorQueryFxml);
        });

        MenuItem blendColors = new MenuItem(message("BlendColors"));
        blendColors.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.ColorsBlendFxml);
        });

        MenuItem ImagesInMyBoxClipboard = new MenuItem(message("ImagesInMyBoxClipboard"));
        ImagesInMyBoxClipboard.setOnAction((ActionEvent event1) -> {
            ImageInMyBoxClipboardController.oneOpen();
        });

        MenuItem ImagesInSystemClipboard = new MenuItem(message("ImagesInSystemClipboard"));
        ImagesInSystemClipboard.setOnAction((ActionEvent event1) -> {
            ImageInSystemClipboardController.oneOpen();
        });

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(
                EditImage, imageManufactureMenu(controller), imageBatchMenu(controller), svgMenu(controller),
                imageScope, imageOptions, new SeparatorMenuItem(),
                ManageColors, QueryColor, blendColors, colorSpaceMenu(controller), new SeparatorMenuItem(),
                ImagesInMyBoxClipboard, ImagesInSystemClipboard, miscellaneousMenu(controller)));

        return items;

    }

    public static Menu imageManufactureMenu(BaseController controller) {

        MenuItem ImageAnalyse = new MenuItem(message("ImageAnalyse"));
        ImageAnalyse.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.ImageAnalyseFxml);
        });

        MenuItem ImagesEditor = new MenuItem(message("ImagesEditor"));
        ImagesEditor.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.ImagesEditorFxml);
        });

        MenuItem ImagesSplice = new MenuItem(message("ImagesSplice"));
        ImagesSplice.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImagesSpliceFxml);
        });

        MenuItem ImageSplit = new MenuItem(message("ImageSplit"));
        ImageSplit.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageSplitFxml);
        });

        MenuItem ImageSample = new MenuItem(message("ImageSample"));
        ImageSample.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageSampleFxml);
        });

        MenuItem ImageRepeat = new MenuItem(message("ImageRepeatTile"));
        ImageRepeat.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageRepeatFxml);
        });

        MenuItem ImagesPlay = new MenuItem(message("ImagesPlay"));
        ImagesPlay.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.ImagesPlayFxml);
        });

        MenuItem imagesBrowser = new MenuItem(message("ImagesBrowser"));
        imagesBrowser.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.ImagesBrowserFxml);
        });

        MenuItem imageOCR = new MenuItem(message("ImageOCR"));
        imageOCR.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.ImageOCRFxml);
        });

        Menu manufactureMenu = new Menu(message("ImageManufacture"));

        manufactureMenu.getItems().addAll(
                ImageAnalyse, imageOCR, new SeparatorMenuItem(),
                ImageRepeat, ImagesSplice, ImageSplit, ImageSample, new SeparatorMenuItem(),
                ImagesPlay, ImagesEditor, imagesBrowser);

        return manufactureMenu;

    }

    public static Menu imageBatchMenu(BaseController controller) {

        MenuItem imageAlphaAdd = new MenuItem(message("ImageAlphaAdd"));
        imageAlphaAdd.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageAlphaAddBatchFxml);
        });

        MenuItem imageAlphaExtract = new MenuItem(message("ImageAlphaExtract"));
        imageAlphaExtract.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageAlphaExtractBatchFxml);
        });

        MenuItem SvgFromImage = new MenuItem(message("ImageToSvg"));
        SvgFromImage.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.SvgFromImageBatchFxml);
        });

        MenuItem imageConverterBatch = new MenuItem(message("FormatsConversion"));
        imageConverterBatch.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.ImageConverterBatchFxml);
        });

        MenuItem imageOCRBatch = new MenuItem(message("ImageOCRBatch"));
        imageOCRBatch.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.ImageOCRBatchFxml);
        });

        Menu imageBatchMenu = new Menu(message("ImageBatch"));
        imageBatchMenu.getItems().addAll(
                imageColorBatchMenu(controller), imagePixelsBatchMenu(controller), imageModifyBatchMenu(controller), new SeparatorMenuItem(),
                imageConverterBatch, imageAlphaExtract, imageAlphaAdd, SvgFromImage, imageOCRBatch);
        return imageBatchMenu;

    }

    public static Menu imageColorBatchMenu(BaseController controller) {

        MenuItem imageReplaceColorMenu = new MenuItem(message("ReplaceColor"));
        imageReplaceColorMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageReplaceColorBatchFxml);
        });

        MenuItem imageBlendColorMenu = new MenuItem(message("BlendColor"));
        imageBlendColorMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageBlendColorBatchFxml);
        });

        MenuItem imageAdjustColorMenu = new MenuItem(message("AdjustColor"));
        imageAdjustColorMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageAdjustColorBatchFxml);
        });

        MenuItem imageBlackWhiteMenu = new MenuItem(message("BlackOrWhite"));
        imageBlackWhiteMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageBlackWhiteBatchFxml);
        });

        MenuItem imageGreyMenu = new MenuItem(message("Grey"));
        imageGreyMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageGreyBatchFxml);
        });

        MenuItem imageSepiaMenu = new MenuItem(message("Sepia"));
        imageSepiaMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageSepiaBatchFxml);
        });

        MenuItem imageReduceColorsMenu = new MenuItem(message("ReduceColors"));
        imageReduceColorsMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageReduceColorsBatchFxml);
        });

        MenuItem imageThresholdingsMenu = new MenuItem(message("Thresholding"));
        imageThresholdingsMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageThresholdingBatchFxml);
        });

        Menu imageColorBatchMenu = new Menu(message("Color"));
        imageColorBatchMenu.getItems().addAll(
                imageReplaceColorMenu, imageBlendColorMenu, imageAdjustColorMenu,
                imageBlackWhiteMenu, imageGreyMenu, imageSepiaMenu,
                imageReduceColorsMenu, imageThresholdingsMenu
        );
        return imageColorBatchMenu;
    }

    public static Menu imagePixelsBatchMenu(BaseController controller) {
        MenuItem imageMosaicMenu = new MenuItem(message("Mosaic"));
        imageMosaicMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageMosaicBatchFxml);
        });

        MenuItem imageFrostedGlassMenu = new MenuItem(message("FrostedGlass"));
        imageFrostedGlassMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageGlassBatchFxml);
        });

        MenuItem imageShadowMenu = new MenuItem(message("Shadow"));
        imageShadowMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageShadowBatchFxml);
        });

        MenuItem imageSmoothMenu = new MenuItem(message("Smooth"));
        imageSmoothMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageSmoothBatchFxml);
        });

        MenuItem imageSharpenMenu = new MenuItem(message("Sharpen"));
        imageSharpenMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageSharpenBatchFxml);
        });

        MenuItem imageContrastMenu = new MenuItem(message("Contrast"));
        imageContrastMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageContrastBatchFxml);
        });

        MenuItem imageEdgeMenu = new MenuItem(message("EdgeDetection"));
        imageEdgeMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageEdgeBatchFxml);
        });

        MenuItem imageEmbossMenu = new MenuItem(message("Emboss"));
        imageEmbossMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageEmbossBatchFxml);
        });

        MenuItem imageConvolutionMenu = new MenuItem(message("Convolution"));
        imageConvolutionMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageConvolutionBatchFxml);
        });

        Menu imagePixelsMenu = new Menu(message("Pixels"));
        imagePixelsMenu.getItems().addAll(
                imageMosaicMenu, imageFrostedGlassMenu, imageShadowMenu,
                imageSmoothMenu, imageSharpenMenu,
                imageContrastMenu, imageEdgeMenu, imageEmbossMenu, imageConvolutionMenu);
        return imagePixelsMenu;

    }

    public static Menu imageModifyBatchMenu(BaseController controller) {
        MenuItem imageSizeMenu = new MenuItem(message("Size"));
        imageSizeMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageSizeBatchFxml);
        });

        MenuItem imageCropMenu = new MenuItem(message("Crop"));
        imageCropMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageCropBatchFxml);
        });

        MenuItem imagePasteMenu = new MenuItem(message("Paste"));
        imagePasteMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImagePasteBatchFxml);
        });

        MenuItem imageTextMenu = new MenuItem(message("Text"));
        imageTextMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageTextBatchFxml);
        });

        MenuItem imageRoundMenu = new MenuItem(message("Round"));
        imageRoundMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageRoundBatchFxml);
        });

        MenuItem imageRotateMenu = new MenuItem(message("Rotate"));
        imageRotateMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageRotateBatchFxml);
        });

        MenuItem imageMirrorMenu = new MenuItem(message("Mirror"));
        imageMirrorMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageMirrorBatchFxml);
        });

        MenuItem imageShearMenu = new MenuItem(message("Shear"));
        imageShearMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageShearBatchFxml);
        });

        MenuItem imageMarginsMenu = new MenuItem(message("Margins"));
        imageMarginsMenu.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ImageMarginsBatchFxml);
        });

        Menu imagePixelsMenu = new Menu(message("Modify"));
        imagePixelsMenu.getItems().addAll(
                imageSizeMenu, imageMarginsMenu, imageCropMenu, imageRoundMenu,
                imageRotateMenu, imageMirrorMenu, imageShearMenu,
                imagePasteMenu, imageTextMenu);
        return imagePixelsMenu;

    }

    public static Menu svgMenu(BaseController controller) {
        MenuItem EditSVG = new MenuItem(message("SVGEditor"));
        EditSVG.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.SvgEditorFxml);
        });

        MenuItem SvgTypesetting = new MenuItem(message("SvgTypesetting"));
        SvgTypesetting.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.SvgTypesettingFxml);
        });

        MenuItem SvgToImage = new MenuItem(message("SvgToImage"));
        SvgToImage.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.SvgToImageFxml);
        });

        MenuItem SvgToPDF = new MenuItem(message("SvgToPDF"));
        SvgToPDF.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.SvgToPDFFxml);
        });

        MenuItem SvgFromImage = new MenuItem(message("ImageToSvg"));
        SvgFromImage.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.SvgFromImageBatchFxml);
        });

        Menu svgMenu = new Menu(message("SVG"));
        svgMenu.getItems().addAll(EditSVG, SvgTypesetting, SvgToImage, SvgToPDF, SvgFromImage);
        return svgMenu;

    }

    public static Menu colorSpaceMenu(BaseController controller) {
        MenuItem IccEditor = new MenuItem(message("IccProfileEditor"));
        IccEditor.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.IccProfileEditorFxml);
        });

        MenuItem ChromaticityDiagram = new MenuItem(message("DrawChromaticityDiagram"));
        ChromaticityDiagram.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ChromaticityDiagramFxml);
        });

        MenuItem ChromaticAdaptationMatrix = new MenuItem(message("ChromaticAdaptationMatrix"));
        ChromaticAdaptationMatrix.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ChromaticAdaptationMatrixFxml);
        });

        MenuItem ColorConversion = new MenuItem(message("ColorConversion"));
        ColorConversion.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.ColorConversionFxml);
        });

        MenuItem RGBColorSpaces = new MenuItem(message("RGBColorSpaces"));
        RGBColorSpaces.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.RGBColorSpacesFxml);
        });

        MenuItem RGB2XYZConversionMatrix = new MenuItem(message("LinearRGB2XYZMatrix"));
        RGB2XYZConversionMatrix.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.RGB2XYZConversionMatrixFxml);
        });

        MenuItem RGB2RGBConversionMatrix = new MenuItem(message("LinearRGB2RGBMatrix"));
        RGB2RGBConversionMatrix.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.RGB2RGBConversionMatrixFxml);
        });

        MenuItem Illuminants = new MenuItem(message("Illuminants"));
        Illuminants.setOnAction((ActionEvent event) -> {
            controller.loadScene(Fxmls.IlluminantsFxml);
        });

        Menu csMenu = new Menu(message("ColorSpace"));
        csMenu.getItems().addAll(ChromaticityDiagram, IccEditor,
                //                ColorConversion,
                RGBColorSpaces, RGB2XYZConversionMatrix, RGB2RGBConversionMatrix,
                Illuminants, ChromaticAdaptationMatrix);
        return csMenu;

    }

    public static Menu miscellaneousMenu(BaseController controller) {

        MenuItem ImageBase64 = new MenuItem(message("ImageBase64"));
        ImageBase64.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.ImageBase64Fxml);
        });

        MenuItem convolutionKernelManager = new MenuItem(message("ConvolutionKernelManager"));
        convolutionKernelManager.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.ConvolutionKernelManagerFxml);
        });

        MenuItem pixelsCalculator = new MenuItem(message("PixelsCalculator"));
        pixelsCalculator.setOnAction((ActionEvent event1) -> {
            controller.openStage(Fxmls.PixelsCalculatorFxml);
        });

        Menu miscellaneousMenu = new Menu(message("Miscellaneous"));
        miscellaneousMenu.getItems().addAll(
                ImageBase64, convolutionKernelManager, pixelsCalculator
        );

        return miscellaneousMenu;

    }

    public static List<MenuItem> fileToolsMenu(BaseController controller, Event event) {
        MenuItem filesArrangement = new MenuItem(Languages.message("FilesArrangement"));
        filesArrangement.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FilesArrangementFxml);
        });

        MenuItem dirSynchronize = new MenuItem(Languages.message("DirectorySynchronize"));
        dirSynchronize.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.DirectorySynchronizeFxml);
        });

        MenuItem filesRename = new MenuItem(Languages.message("FilesRename"));
        filesRename.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FilesRenameFxml);
        });

        MenuItem fileCut = new MenuItem(Languages.message("FileCut"));
        fileCut.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FileCutFxml);
        });

        MenuItem filesMerge = new MenuItem(Languages.message("FilesMerge"));
        filesMerge.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FilesMergeFxml);
        });

        MenuItem filesCopy = new MenuItem(Languages.message("FilesCopy"));
        filesCopy.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FilesCopyFxml);
        });

        MenuItem filesMove = new MenuItem(Languages.message("FilesMove"));
        filesMove.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FilesMoveFxml);
        });

        MenuItem filesFind = new MenuItem(Languages.message("FilesFind"));
        filesFind.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FilesFindFxml);
        });

        MenuItem filesCompare = new MenuItem(Languages.message("FilesCompare"));
        filesCompare.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FilesCompareFxml);
        });

        MenuItem filesRedundancy = new MenuItem(Languages.message("FilesRedundancy"));
        filesRedundancy.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FilesRedundancyFxml);
        });

        MenuItem filesDelete = new MenuItem(Languages.message("FilesDelete"));
        filesDelete.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FilesDeleteFxml);
        });

        MenuItem DeleteEmptyDirectories = new MenuItem(Languages.message("DeleteEmptyDirectories"));
        DeleteEmptyDirectories.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FilesDeleteEmptyDirFxml);
        });

        MenuItem DeleteJavaTemporaryPathFiles = new MenuItem(Languages.message("DeleteJavaIOTemporaryPathFiles"));
        DeleteJavaTemporaryPathFiles.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FilesDeleteJavaTempFxml);
        });

        MenuItem DeleteNestedDirectories = new MenuItem(Languages.message("DeleteNestedDirectories"));
        DeleteNestedDirectories.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FilesDeleteNestedDirFxml);
        });

        Menu fileDeleteMenu = new Menu(Languages.message("Delete"));
        fileDeleteMenu.getItems().addAll(
                DeleteJavaTemporaryPathFiles, DeleteEmptyDirectories, filesDelete, DeleteNestedDirectories
        );

        MenuItem filesArchiveCompress = new MenuItem(Languages.message("FilesArchiveCompress"));
        filesArchiveCompress.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FilesArchiveCompressFxml);
        });

        MenuItem filesCompress = new MenuItem(Languages.message("FilesCompressBatch"));
        filesCompress.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FilesCompressBatchFxml);
        });

        MenuItem filesDecompressUnarchive = new MenuItem(Languages.message("FileDecompressUnarchive"));
        filesDecompressUnarchive.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FileDecompressUnarchiveFxml);
        });

        MenuItem filesDecompressUnarchiveBatch = new MenuItem(Languages.message("FilesDecompressUnarchiveBatch"));
        filesDecompressUnarchiveBatch.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FilesDecompressUnarchiveBatchFxml);
        });

        Menu archiveCompressMenu = new Menu(Languages.message("FilesArchiveCompress"));
        archiveCompressMenu.getItems().addAll(
                filesDecompressUnarchive, filesDecompressUnarchiveBatch,
                filesArchiveCompress, filesCompress
        );

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(filesArrangement, dirSynchronize, new SeparatorMenuItem(),
                archiveCompressMenu, new SeparatorMenuItem(),
                fileCut, filesMerge, new SeparatorMenuItem(),
                filesFind, filesRedundancy, filesCompare, new SeparatorMenuItem(),
                filesRename, filesCopy, filesMove, new SeparatorMenuItem(),
                fileDeleteMenu));

        return items;

    }

    public static List<MenuItem> networkToolsMenu(BaseController controller, Event event) {
        MenuItem weiboSnap = new MenuItem(message("WeiboSnap"));
        weiboSnap.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.WeiboSnapFxml);
        });

        MenuItem webBrowserHtml = new MenuItem(message("WebBrowser"));
        webBrowserHtml.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.WebBrowserFxml);
        });

        MenuItem WebFavorites = new MenuItem(message("WebFavorites"));
        WebFavorites.setOnAction((ActionEvent event1) -> {
            DataTreeController.webFavorite(controller, true);
        });

        MenuItem WebHistories = new MenuItem(message("WebHistories"));
        WebHistories.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.WebHistoriesFxml);
        });

        MenuItem ConvertUrl = new MenuItem(message("ConvertUrl"));
        ConvertUrl.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.NetworkConvertUrlFxml);
        });

        MenuItem QueryAddress = new MenuItem(message("QueryNetworkAddress"));
        QueryAddress.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.NetworkQueryAddressFxml);
        });

        MenuItem QueryDNSBatch = new MenuItem(message("QueryDNSBatch"));
        QueryDNSBatch.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.NetworkQueryDNSBatchFxml);
        });

        MenuItem DownloadFirstLevelLinks = new MenuItem(message("DownloadHtmls"));
        DownloadFirstLevelLinks.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.DownloadFirstLevelLinksFxml);
        });

        MenuItem RemotePathManage = new MenuItem(message("RemotePathManage"));
        RemotePathManage.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.RemotePathManageFxml);
        });

        MenuItem RemotePathSynchronizeFromLocal = new MenuItem(message("RemotePathSynchronizeFromLocal"));
        RemotePathSynchronizeFromLocal.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.RemotePathSynchronizeFromLocalFxml);
        });

        MenuItem SecurityCertificates = new MenuItem(message("SecurityCertificates"));
        SecurityCertificates.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.SecurityCertificatesFxml);
        });

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(webBrowserHtml, WebFavorites, WebHistories, new SeparatorMenuItem(),
                RemotePathManage, RemotePathSynchronizeFromLocal, new SeparatorMenuItem(),
                QueryAddress, QueryDNSBatch, ConvertUrl, SecurityCertificates, new SeparatorMenuItem(),
                DownloadFirstLevelLinks, weiboSnap));

        return items;

    }

    public static List<MenuItem> dataToolsMenu(BaseController controller, Event event) {
        MenuItem DataManufacture = new MenuItem(message("DataManufacture"));
        DataManufacture.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.Data2DManufactureFxml);
        });

        MenuItem ManageData = new MenuItem(message("ManageData"));
        ManageData.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.Data2DManageFxml);
        });

        MenuItem SpliceData = new MenuItem(message("SpliceData"));
        SpliceData.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.Data2DSpliceFxml);
        });

        MenuItem RowExpression = new MenuItem(message("RowExpression"));
        RowExpression.setOnAction((ActionEvent event1) -> {
            DataTreeController.rowExpression(controller, true);
        });

        MenuItem DataColumn = new MenuItem(message("DataColumn"));
        DataColumn.setOnAction((ActionEvent event1) -> {
            DataTreeController.dataColumn(controller, true);
        });

        MenuItem DataInSystemClipboard = new MenuItem(message("DataInSystemClipboard"));
        DataInSystemClipboard.setOnAction((ActionEvent event1) -> {
            DataInSystemClipboardController.oneOpen();
        });

        MenuItem DataInMyBoxClipboard = new MenuItem(message("DataInMyBoxClipboard"));
        DataInMyBoxClipboard.setOnAction((ActionEvent event1) -> {
            DataInMyBoxClipboardController c = DataInMyBoxClipboardController.oneOpen();
        });

        MenuItem ExcelConvert = new MenuItem(message("ExcelConvert"));
        ExcelConvert.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.DataFileExcelConvertFxml);
        });

        MenuItem ExcelMerge = new MenuItem(message("ExcelMerge"));
        ExcelMerge.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.DataFileExcelMergeFxml);
        });

        MenuItem CsvConvert = new MenuItem(message("CsvConvert"));
        CsvConvert.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.DataFileCSVConvertFxml);
        });

        MenuItem CsvMerge = new MenuItem(message("CsvMerge"));
        CsvMerge.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.DataFileCSVMergeFxml);
        });

        MenuItem TextDataConvert = new MenuItem(message("TextDataConvert"));
        TextDataConvert.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.DataFileTextConvertFxml);
        });

        MenuItem TextDataMerge = new MenuItem(message("TextDataMerge"));
        TextDataMerge.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.DataFileTextMergeFxml);
        });

        Menu dataFile = new Menu(message("DataFile"));
        dataFile.getItems().addAll(CsvConvert, CsvMerge, new SeparatorMenuItem(),
                ExcelConvert, ExcelMerge, new SeparatorMenuItem(),
                TextDataConvert, TextDataMerge);

        MenuItem GeographyCode = new MenuItem(message("GeographyCode"));
        GeographyCode.setOnAction((ActionEvent event1) -> {
            GeographyCodeController.open(controller, true, true);
        });

        MenuItem LocationInMap = new MenuItem(message("LocationInMap"));
        LocationInMap.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.LocationInMapFxml);
        });

        MenuItem ConvertCoordinate = new MenuItem(message("ConvertCoordinate"));
        ConvertCoordinate.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.ConvertCoordinateFxml);
        });

        Menu Location = new Menu(message("Location"));
        Location.getItems().addAll(
                GeographyCode, LocationInMap, ConvertCoordinate
        );

        MenuItem MatricesManage = new MenuItem(message("MatricesManage"));
        MatricesManage.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.MatricesManageFxml);
        });

        MenuItem MatrixUnaryCalculation = new MenuItem(message("MatrixUnaryCalculation"));
        MatrixUnaryCalculation.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.MatrixUnaryCalculationFxml);
        });

        MenuItem MatricesBinaryCalculation = new MenuItem(message("MatricesBinaryCalculation"));
        MatricesBinaryCalculation.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.MatricesBinaryCalculationFxml);
        });

        Menu matrix = new Menu(message("Matrix"));
        matrix.getItems().addAll(
                MatricesManage, MatrixUnaryCalculation, MatricesBinaryCalculation
        );

        MenuItem DatabaseSQL = new MenuItem(message("DatabaseSQL"));
        DatabaseSQL.setOnAction((ActionEvent event1) -> {
            DataTreeController.sql(controller, true);
        });

        MenuItem DatabaseTable = new MenuItem(message("DatabaseTable"));
        DatabaseTable.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.DataTablesFxml);
        });

        MenuItem databaseTableDefinition = new MenuItem(message("TableDefinition"));
        databaseTableDefinition.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.DatabaseTableDefinitionFxml);
        });

        Menu database = new Menu(message("Database"));
        database.getItems().addAll(
                DatabaseTable, DatabaseSQL, databaseTableDefinition
        );

        MenuItem jshell = new MenuItem(message("JShell"));
        jshell.setOnAction((ActionEvent event1) -> {
            DataTreeController.jShell(controller, true);
        });

        MenuItem jexl = new MenuItem(message("JEXL"));
        jexl.setOnAction((ActionEvent event1) -> {
            DataTreeController.jexl(controller, true);
        });

        MenuItem JavaScript = new MenuItem("JavaScript");
        JavaScript.setOnAction((ActionEvent event1) -> {
            DataTreeController.javascript(controller, true);
        });

        Menu calculation = new Menu(message("ScriptAndExperssion"));
        calculation.getItems().addAll(
                JavaScript, jshell, jexl
        );

        MenuItem MathFunction = new MenuItem(message("MathFunction"));
        MathFunction.setOnAction((ActionEvent event1) -> {
            DataTreeController.mathFunction(controller, true);
        });

        MenuItem barcodeCreator = new MenuItem(message("BarcodeCreator"));
        barcodeCreator.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.BarcodeCreatorFxml);
        });

        MenuItem barcodeDecoder = new MenuItem(message("BarcodeDecoder"));
        barcodeDecoder.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.BarcodeDecoderFxml);
        });

        MenuItem messageDigest = new MenuItem(message("MessageDigest"));
        messageDigest.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.MessageDigestFxml);
        });

        MenuItem Base64Conversion = new MenuItem(message("Base64Conversion"));
        Base64Conversion.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.Base64Fxml);
        });

        MenuItem TTC2TTF = new MenuItem(message("TTC2TTF"));
        TTC2TTF.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FileTTC2TTFFxml);
        });

        Menu miscellaneousMenu = new Menu(message("Miscellaneous"));
        miscellaneousMenu.getItems().addAll(
                barcodeCreator, barcodeDecoder, new SeparatorMenuItem(),
                messageDigest, Base64Conversion, new SeparatorMenuItem(),
                TTC2TTF
        );

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(
                DataManufacture, ManageData, new SeparatorMenuItem(),
                dataFile, matrix, database, new SeparatorMenuItem(),
                SpliceData, DataColumn, RowExpression,
                DataInSystemClipboard, DataInMyBoxClipboard, new SeparatorMenuItem(),
                calculation, MathFunction, new SeparatorMenuItem(),
                Location, miscellaneousMenu));

        return items;

    }

    public static List<MenuItem> mediaToolsMenu(BaseController controller, Event event) {
        MenuItem mediaPlayer = new MenuItem(Languages.message("MediaPlayer"));
        mediaPlayer.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.MediaPlayerFxml);
        });

        MenuItem mediaLists = new MenuItem(Languages.message("ManageMediaLists"));
        mediaLists.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.MediaListFxml);
        });

        MenuItem FFmpegInformation = new MenuItem(Languages.message("FFmpegInformation"));
        FFmpegInformation.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FFmpegInformationFxml);
        });

        MenuItem FFprobe = new MenuItem(Languages.message("FFmpegProbeMediaInformation"));
        FFprobe.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FFmpegProbeMediaInformationFxml);
        });

        MenuItem FFmpegConversionFiles = new MenuItem(Languages.message("FFmpegConvertMediaFiles"));
        FFmpegConversionFiles.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FFmpegConvertMediaFilesFxml);
        });

        MenuItem FFmpegConversionStreams = new MenuItem(Languages.message("FFmpegConvertMediaStreams"));
        FFmpegConversionStreams.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FFmpegConvertMediaStreamsFxml);
        });

        Menu FFmpegConversionMenu = new Menu(Languages.message("FFmpegConvertMedias"));
        FFmpegConversionMenu.getItems().addAll(
                FFmpegConversionFiles, FFmpegConversionStreams);

        MenuItem FFmpegMergeImages = new MenuItem(Languages.message("FFmpegMergeImagesInformation"));
        FFmpegMergeImages.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FFmpegMergeImagesFxml);
        });

        MenuItem FFmpegMergeImageFiles = new MenuItem(Languages.message("FFmpegMergeImagesFiles"));
        FFmpegMergeImageFiles.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FFmpegMergeImageFilesFxml);
        });

        MenuItem screenRecorder = new MenuItem(Languages.message("FFmpegScreenRecorder"));
        screenRecorder.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.FFmpegScreenRecorderFxml);
        });

        Menu FFmpegMergeMenu = new Menu(Languages.message("FFmpegMergeImages"));
        FFmpegMergeMenu.getItems().addAll(
                FFmpegMergeImageFiles, FFmpegMergeImages);

        MenuItem alarmClock = new MenuItem(Languages.message("AlarmClock"));
        alarmClock.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.AlarmClockFxml);
        });

        MenuItem GameElimniation = new MenuItem(Languages.message("GameElimniation"));
        GameElimniation.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.GameElimniationFxml);
        });

        MenuItem GameMine = new MenuItem(Languages.message("GameMine"));
        GameMine.setOnAction((ActionEvent event1) -> {
            controller.loadScene(Fxmls.GameMineFxml);
        });

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(mediaPlayer, mediaLists, new SeparatorMenuItem(),
                screenRecorder,
                FFmpegConversionMenu, FFmpegMergeMenu,
                FFprobe, FFmpegInformation, new SeparatorMenuItem(),
                //                alarmClock, new SeparatorMenuItem(),
                GameElimniation, GameMine));

        return items;
    }

    public static List<MenuItem> helpMenu(BaseController controller, Event event) {
        MenuItem Overview = new MenuItem(message("Overview"));
        Overview.setOnAction((ActionEvent event1) -> {
            String lang = Languages.embedFileLang();
            File file = FxFileTools.getInternalFile("/doc/" + lang + "/MyBox-Overview-" + lang + ".pdf",
                    "doc", "MyBox-Overview-" + lang + ".pdf");
            if (file != null && file.exists()) {
                PopTools.browseURI(controller, file.toURI());
            }
        });

        MenuItem Shortcuts = new MenuItem(message("Shortcuts"));
        Shortcuts.setOnAction((ActionEvent event1) -> {
            controller.openStage(Fxmls.ShortcutsFxml);
        });

        MenuItem FunctionsList = new MenuItem(message("FunctionsList"));
        FunctionsList.setOnAction((ActionEvent event1) -> {
            controller.openStage(Fxmls.FunctionsListFxml);
        });

        MenuItem InterfaceTips = new MenuItem(message("InterfaceTips"));
        InterfaceTips.setOnAction((ActionEvent event1) -> {
            controller.openHtml(HelpTools.makeInterfaceTips(AppVariables.CurrentLangName));
        });

        MenuItem AboutTreeInformation = new MenuItem(message("AboutTreeInformation"));
        AboutTreeInformation.setOnAction((ActionEvent event1) -> {
            controller.openHtml(HelpTools.aboutTreeInformation());
        });

        MenuItem AboutImageScope = new MenuItem(message("AboutImageScope"));
        AboutImageScope.setOnAction((ActionEvent event1) -> {
            controller.openHtml(HelpTools.aboutImageScope());
        });

        MenuItem AboutData2D = new MenuItem(message("AboutData2D"));
        AboutData2D.setOnAction((ActionEvent event1) -> {
            controller.openHtml(HelpTools.aboutData2D());
        });

        MenuItem AboutRowExpression = new MenuItem(message("AboutRowExpression"));
        AboutRowExpression.setOnAction((ActionEvent event1) -> {
            controller.openHtml(HelpTools.aboutRowExpression());
        });

        MenuItem AboutGroupingRows = new MenuItem(message("AboutGroupingRows"));
        AboutGroupingRows.setOnAction((ActionEvent event1) -> {
            controller.openHtml(HelpTools.aboutGroupingRows());
        });

        MenuItem AboutDataAnalysis = new MenuItem(message("AboutDataAnalysis"));
        AboutDataAnalysis.setOnAction((ActionEvent event1) -> {
            controller.openHtml(HelpTools.aboutDataAnalysis());
        });

        MenuItem AboutCoordinateSystem = new MenuItem(message("AboutCoordinateSystem"));
        AboutCoordinateSystem.setOnAction((ActionEvent event1) -> {
            controller.openHtml(HelpTools.aboutCoordinateSystem());
        });

        MenuItem AboutColor = new MenuItem(message("AboutColor"));
        AboutColor.setOnAction((ActionEvent event1) -> {
            controller.openHtml(HelpTools.aboutColor());
        });

        MenuItem AboutMedia = new MenuItem(message("AboutMedia"));
        AboutMedia.setOnAction((ActionEvent event1) -> {
            controller.openHtml(HelpTools.aboutMedia());
        });

        MenuItem AboutMacro = new MenuItem(message("AboutMacro"));
        AboutMacro.setOnAction((ActionEvent event1) -> {
            controller.openHtml(HelpTools.aboutMacro());
        });

        MenuItem SomeLinks = new MenuItem(message("SomeLinks"));
        SomeLinks.setOnAction((ActionEvent event1) -> {
            controller.openHtml(HelpTools.usefulLinks(AppVariables.CurrentLangName));
        });

        MenuItem imagesStories = new MenuItem(message("StoriesOfImages"));
        imagesStories.setOnAction((ActionEvent event1) -> {
            HelpTools.imageStories(controller);
        });

        MenuItem ReadMe = new MenuItem(message("ReadMe"));
        ReadMe.setOnAction((ActionEvent event1) -> {
            HelpTools.readMe(controller);
        });

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(Overview, Shortcuts, FunctionsList, new SeparatorMenuItem(),
                InterfaceTips, AboutTreeInformation, AboutImageScope,
                AboutData2D, AboutRowExpression, AboutGroupingRows, AboutDataAnalysis,
                AboutCoordinateSystem, AboutColor, AboutMedia, AboutMacro,
                SomeLinks, imagesStories, ReadMe));

        return items;
    }

    public static CheckMenuItem popCheckMenu(Event mevent, String name) {
        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"),
                StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean(name + "MenuPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean(name + "MenuPopWhenMouseHovering", popItem.isSelected());
            }
        });
        return popItem;
    }

    public static boolean isPopMenu(String name) {
        return UserConfig.getBoolean(name + "MenuPopWhenMouseHovering", true);
    }

}
