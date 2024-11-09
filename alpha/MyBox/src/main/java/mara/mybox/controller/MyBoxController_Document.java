package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_Document extends MyBoxController_Base {

    @FXML
    public void popDocumentMenu(Event event) {
        if (UserConfig.getBoolean("MyBoxHomeMenuPopWhenMouseHovering", true)) {
            showDocumentMenu(event);
        }
    }

    @FXML
    protected void showDocumentMenu(Event event) {

        MenuItem Notes = new MenuItem(message("Notes"));
        Notes.setOnAction((ActionEvent event1) -> {
            DataTreeController.noteTree();
        });

        MenuItem InformationInTree = new MenuItem(message("InformationInTree"));
        InformationInTree.setOnAction((ActionEvent event1) -> {
            DataTreeController.infoTree();
        });

        Menu pdfMenu = new Menu("PDF");

        MenuItem pdfView = new MenuItem(message("PdfView"));
        pdfView.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfViewFxml);
        });

        MenuItem PdfPlay = new MenuItem(message("PdfPlay"));
        PdfPlay.setOnAction((ActionEvent event1) -> {
            ImagesPlayController c = (ImagesPlayController) loadScene(Fxmls.ImagesPlayFxml);
            c.pdfRadio.setSelected(true);
        });

        MenuItem PDFAttributes = new MenuItem(message("PDFAttributes"));
        PDFAttributes.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfAttributesFxml);
        });

        MenuItem PDFAttributesBatch = new MenuItem(message("PDFAttributesBatch"));
        PDFAttributesBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfAttributesBatchFxml);
        });

        MenuItem pdfExtractImagesBatch = new MenuItem(message("PdfExtractImagesBatch"));
        pdfExtractImagesBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfExtractImagesBatchFxml);
        });

        MenuItem pdfExtractTextsBatch = new MenuItem(message("PdfExtractTextsBatch"));
        pdfExtractTextsBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfExtractTextsBatchFxml);
        });

        MenuItem pdfConvertImagesBatch = new MenuItem(message("PdfConvertImagesBatch"));
        pdfConvertImagesBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfConvertImagesBatchFxml);
        });

        MenuItem pdfOcrBatch = new MenuItem(message("PdfOCRBatch"));
        pdfOcrBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfOCRBatchFxml);
        });

        MenuItem pdfConvertHtmlsBatch = new MenuItem(message("PdfConvertHtmlsBatch"));
        pdfConvertHtmlsBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfConvertHtmlsBatchFxml);
        });

        MenuItem imagesCombinePdf = new MenuItem(message("ImagesCombinePdf"));
        imagesCombinePdf.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImagesEditorFxml);
        });

        MenuItem pdfCompressImagesBatch = new MenuItem(message("PdfCompressImagesBatch"));
        pdfCompressImagesBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfCompressImagesBatchFxml);
        });

        MenuItem PdfImagesConvertBatch = new MenuItem(message("PdfImagesConvertBatch"));
        PdfImagesConvertBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfImagesConvertBatchFxml);
        });

        MenuItem pdfMerge = new MenuItem(message("MergePdf"));
        pdfMerge.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfMergeFxml);
        });

        MenuItem PdfSplitBatch = new MenuItem(message("PdfSplitBatch"));
        PdfSplitBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfSplitBatchFxml);
        });

        MenuItem PdfAddWatermark = new MenuItem(message("PdfAddWatermark"));
        PdfAddWatermark.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfAddWatermarkBatchFxml);
        });

        pdfMenu.getItems().addAll(
                pdfView, PdfPlay, new SeparatorMenuItem(),
                pdfConvertImagesBatch, PdfImagesConvertBatch, pdfConvertHtmlsBatch, pdfCompressImagesBatch, new SeparatorMenuItem(),
                pdfExtractImagesBatch, pdfExtractTextsBatch, pdfOcrBatch, PdfAddWatermark, new SeparatorMenuItem(),
                PdfSplitBatch, pdfMerge, imagesCombinePdf, new SeparatorMenuItem(),
                PDFAttributes, PDFAttributesBatch
        );

        Menu textsMenu = new Menu(message("Texts"));

        MenuItem textEditer = new MenuItem(message("TextEditer"));
        textEditer.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.TextEditorFxml);
        });

        MenuItem TextConvert = new MenuItem(message("TextConvertSplit"));
        TextConvert.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.TextFilesConvertFxml);
        });

        MenuItem TextMerge = new MenuItem(message("TextFilesMerge"));
        TextMerge.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.TextFilesMergeFxml);
        });

        MenuItem TextFindBatch = new MenuItem(message("TextFindBatch"));
        TextFindBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.TextFindBatchFxml);
        });

        MenuItem TextReplaceBatch = new MenuItem(message("TextReplaceBatch"));
        TextReplaceBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.TextReplaceBatchFxml);
        });

        MenuItem TextFilterBatch = new MenuItem(message("TextFilterBatch"));
        TextFilterBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.TextFilterBatchFxml);
        });

        MenuItem TextToHtml = new MenuItem(message("TextToHtml"));
        TextToHtml.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.TextToHtmlFxml);
        });

        MenuItem TextToPdf = new MenuItem(message("TextToPdf"));
        TextToPdf.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.TextToPdfFxml);
        });

        textsMenu.getItems().addAll(
                textEditer, TextFindBatch, TextReplaceBatch, TextFilterBatch, TextConvert, TextMerge, TextToHtml, TextToPdf
        );

        Menu bytesMenu = new Menu(message("Bytes"));

        MenuItem bytesEditer = new MenuItem(message("BytesEditer"));
        bytesEditer.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.BytesEditorFxml);
        });

        MenuItem BytesFindBatch = new MenuItem(message("BytesFindBatch"));
        BytesFindBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.BytesFindBatchFxml);
        });

        bytesMenu.getItems().addAll(
                bytesEditer, BytesFindBatch
        );

        Menu htmlMenu = new Menu(message("Html"));

        MenuItem htmlEditor = new MenuItem(message("HtmlEditor"));
        htmlEditor.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlEditorFxml);
        });

        MenuItem htmlToMarkdown = new MenuItem(message("HtmlToMarkdown"));
        htmlToMarkdown.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlToMarkdownFxml);
        });

        MenuItem HtmlToText = new MenuItem(message("HtmlToText"));
        HtmlToText.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlToTextFxml);
        });

        MenuItem HtmlToPdf = new MenuItem(message("HtmlToPdf"));
        HtmlToPdf.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlToPdfFxml);
        });

        MenuItem HtmlSetCharset = new MenuItem(message("HtmlSetCharset"));
        HtmlSetCharset.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlSetCharsetFxml);
        });

        MenuItem HtmlSetStyle = new MenuItem(message("HtmlSetStyle"));
        HtmlSetStyle.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlSetStyleFxml);
        });

        MenuItem HtmlSetEquiv = new MenuItem(message("HtmlSetEquiv"));
        HtmlSetStyle.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlSetEquivFxml);
        });

        MenuItem HtmlSnap = new MenuItem(message("HtmlSnap"));
        HtmlSnap.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlSnapFxml);
        });

        MenuItem HtmlTypesetting = new MenuItem(message("HtmlTypesetting"));
        HtmlTypesetting.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlTypesettingFxml);
        });

        MenuItem WebFind = new MenuItem(message("WebFind"));
        WebFind.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlFindFxml);
        });

        MenuItem HtmlExtractTables = new MenuItem(message("HtmlExtractTables"));
        HtmlExtractTables.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlExtractTablesFxml);
        });

        MenuItem WebElements = new MenuItem(message("WebElements"));
        WebElements.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlElementsFxml);
        });

        MenuItem HtmlMergeAsHtml = new MenuItem(message("HtmlMergeAsHtml"));
        HtmlMergeAsHtml.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlMergeAsHtmlFxml);
        });

        MenuItem HtmlMergeAsMarkdown = new MenuItem(message("HtmlMergeAsMarkdown"));
        HtmlMergeAsMarkdown.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlMergeAsMarkdownFxml);
        });

        MenuItem HtmlMergeAsPDF = new MenuItem(message("HtmlMergeAsPDF"));
        HtmlMergeAsPDF.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlMergeAsPDFFxml);
        });

        MenuItem HtmlMergeAsText = new MenuItem(message("HtmlMergeAsText"));
        HtmlMergeAsText.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlMergeAsTextFxml);
        });

        MenuItem HtmlFrameset = new MenuItem(message("HtmlFrameset"));
        HtmlFrameset.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlFramesetFxml);
        });

        htmlMenu.getItems().addAll(
                htmlEditor, WebFind, WebElements, HtmlSnap, HtmlExtractTables, new SeparatorMenuItem(),
                HtmlTypesetting, htmlToMarkdown, HtmlToText, HtmlToPdf, HtmlSetCharset, HtmlSetStyle, HtmlSetEquiv, new SeparatorMenuItem(),
                HtmlMergeAsHtml, HtmlMergeAsMarkdown, HtmlMergeAsPDF, HtmlMergeAsText, HtmlFrameset
        );

        Menu markdownMenu = new Menu("Markdown");

        MenuItem markdownEditor = new MenuItem(message("MarkdownEditer"));
        markdownEditor.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.MarkdownEditorFxml);
        });

        MenuItem markdownOptions = new MenuItem(message("MarkdownOptions"));
        markdownOptions.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.MarkdownOptionsFxml);
        });

        MenuItem MarkdownTypesetting = new MenuItem(message("MarkdownTypesetting"));
        MarkdownTypesetting.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.MarkdownTypesettingFxml);
        });

        MenuItem markdownToHtml = new MenuItem(message("MarkdownToHtml"));
        markdownToHtml.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.MarkdownToHtmlFxml);
        });

        MenuItem MarkdownToText = new MenuItem(message("MarkdownToText"));
        MarkdownToText.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.MarkdownToTextFxml);
        });

        MenuItem MarkdownToPdf = new MenuItem(message("MarkdownToPdf"));
        MarkdownToPdf.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.MarkdownToPdfFxml);
        });

        markdownMenu.getItems().addAll(
                markdownEditor, markdownOptions, new SeparatorMenuItem(),
                MarkdownTypesetting, markdownToHtml, MarkdownToText, MarkdownToPdf
        );

        Menu jsonMenu = new Menu("JSON");

        MenuItem jsonEditorMenu = new MenuItem(message("JsonEditor"));
        jsonEditorMenu.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.JsonEditorFxml);
        });

        MenuItem jsonTypesettingMenu = new MenuItem(message("JsonTypesetting"));
        jsonTypesettingMenu.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.JsonTypesettingFxml);
        });

        jsonMenu.getItems().addAll(
                jsonEditorMenu, jsonTypesettingMenu
        );

        Menu xmlMenu = new Menu("XML");

        MenuItem xmlEditorMenu = new MenuItem(message("XmlEditor"));
        xmlEditorMenu.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.XmlEditorFxml);
        });

        MenuItem xmlTypesettingMenu = new MenuItem(message("XmlTypesetting"));
        xmlTypesettingMenu.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.XmlTypesettingFxml);
        });

        xmlMenu.getItems().addAll(
                xmlEditorMenu, xmlTypesettingMenu
        );

        Menu msMenu = new Menu(message("MicrosoftDocumentFormats"));

        MenuItem ExtractTextsFromMS = new MenuItem(message("ExtractTextsFromMS"));
        ExtractTextsFromMS.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ExtractTextsFromMSFxml);
        });

        MenuItem WordView = new MenuItem(message("WordView"));
        WordView.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.WordViewFxml);
        });

        MenuItem WordToHtml = new MenuItem(message("WordToHtml"));
        WordToHtml.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.WordToHtmlFxml);
        });

        MenuItem WordToPdf = new MenuItem(message("WordToPdf"));
        WordToPdf.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.WordToPdfFxml);
        });

        MenuItem PptView = new MenuItem(message("PptView"));
        PptView.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PptViewFxml);
        });

        MenuItem PptToImages = new MenuItem(message("PptToImages"));
        PptToImages.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PptToImagesFxml);
        });

        MenuItem PptToPdf = new MenuItem(message("PptToPdf"));
        PptToPdf.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PptToPdfFxml);
        });

        MenuItem PptExtract = new MenuItem(message("PptExtract"));
        PptExtract.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PptExtractFxml);
        });

        MenuItem PptxMerge = new MenuItem(message("PptxMerge"));
        PptxMerge.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PptxMergeFxml);
        });

        MenuItem PptSplit = new MenuItem(message("PptSplit"));
        PptSplit.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PptSplitFxml);
        });

        MenuItem imagesCombinePPT = new MenuItem(message("ImagesCombinePPT"));
        imagesCombinePPT.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImagesEditorFxml);
        });

        MenuItem PptPlay = new MenuItem(message("PptPlay"));
        PptPlay.setOnAction((ActionEvent event1) -> {
            ImagesPlayController c = (ImagesPlayController) loadScene(Fxmls.ImagesPlayFxml);
            c.pptRadio.setSelected(true);
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
        items.addAll(Arrays.asList(Notes, InformationInTree, new SeparatorMenuItem(),
                pdfMenu, markdownMenu, jsonMenu, xmlMenu, htmlMenu, textsMenu, msMenu, bytesMenu, new SeparatorMenuItem(),
                TextInMyBoxClipboard, TextInSystemClipboard));

        items.add(new SeparatorMenuItem());

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean("MyBoxHomeMenuPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean("MyBoxHomeMenuPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        popCenterMenu(documentBox, items);

    }

}
