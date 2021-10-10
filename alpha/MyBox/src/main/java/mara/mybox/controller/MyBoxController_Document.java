package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_Document extends MyBoxController_Base {

    @FXML
    protected void showDocumentMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem Notes = new MenuItem(Languages.message("Notes"));
        Notes.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.NotesFxml);
        });

        Menu pdfMenu = new Menu("PDF");

        MenuItem pdfView = new MenuItem(Languages.message("PdfView"));
        pdfView.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfViewFxml);
        });

        MenuItem PdfPlay = new MenuItem(Languages.message("PdfPlay"));
        PdfPlay.setOnAction((ActionEvent event1) -> {
            ImagesPlayController c = (ImagesPlayController) loadScene(Fxmls.ImagesPlayFxml);
            c.pdfRadio.fire();
        });

        MenuItem PDFAttributes = new MenuItem(Languages.message("PDFAttributes"));
        PDFAttributes.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfAttributesFxml);
        });

        MenuItem PDFAttributesBatch = new MenuItem(Languages.message("PDFAttributesBatch"));
        PDFAttributesBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfAttributesBatchFxml);
        });

        MenuItem pdfExtractImagesBatch = new MenuItem(Languages.message("PdfExtractImagesBatch"));
        pdfExtractImagesBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfExtractImagesBatchFxml);
        });

        MenuItem pdfExtractTextsBatch = new MenuItem(Languages.message("PdfExtractTextsBatch"));
        pdfExtractTextsBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfExtractTextsBatchFxml);
        });

        MenuItem pdfConvertImagesBatch = new MenuItem(Languages.message("PdfConvertImagesBatch"));
        pdfConvertImagesBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfConvertImagesBatchFxml);
        });

        MenuItem pdfOcrBatch = new MenuItem(Languages.message("PdfOCRBatch"));
        pdfOcrBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfOCRBatchFxml);
        });

        MenuItem pdfConvertHtmlsBatch = new MenuItem(Languages.message("PdfConvertHtmlsBatch"));
        pdfConvertHtmlsBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfConvertHtmlsBatchFxml);
        });

        MenuItem imagesCombinePdf = new MenuItem(Languages.message("ImagesCombinePdf"));
        imagesCombinePdf.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImagesEditorFxml);
        });

        MenuItem pdfCompressImagesBatch = new MenuItem(Languages.message("PdfCompressImagesBatch"));
        pdfCompressImagesBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfCompressImagesBatchFxml);
        });

        MenuItem PdfImagesConvertBatch = new MenuItem(Languages.message("PdfImagesConvertBatch"));
        PdfImagesConvertBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfImagesConvertBatchFxml);
        });

        MenuItem pdfMerge = new MenuItem(Languages.message("MergePdf"));
        pdfMerge.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfMergeFxml);
        });

        MenuItem PdfSplitBatch = new MenuItem(Languages.message("PdfSplitBatch"));
        PdfSplitBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PdfSplitBatchFxml);
        });

        pdfMenu.getItems().addAll(
                pdfView, PdfPlay, new SeparatorMenuItem(),
                pdfConvertImagesBatch, PdfImagesConvertBatch, pdfConvertHtmlsBatch, pdfCompressImagesBatch, new SeparatorMenuItem(),
                pdfExtractImagesBatch, pdfExtractTextsBatch, pdfOcrBatch, new SeparatorMenuItem(),
                PdfSplitBatch, pdfMerge, imagesCombinePdf, new SeparatorMenuItem(),
                PDFAttributes, PDFAttributesBatch
        );

        Menu textsMenu = new Menu(Languages.message("Texts"));

        MenuItem textEditer = new MenuItem(Languages.message("TextEditer"));
        textEditer.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.TextEditorFxml);
        });

        MenuItem TextConvert = new MenuItem(Languages.message("TextConvertSplit"));
        TextConvert.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.TextFilesConvertFxml);
        });

        MenuItem TextMerge = new MenuItem(Languages.message("TextFilesMerge"));
        TextMerge.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.TextFilesMergeFxml);
        });

        MenuItem TextReplaceBatch = new MenuItem(Languages.message("TextReplaceBatch"));
        TextReplaceBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.TextReplaceBatchFxml);
        });

        MenuItem TextFilterBatch = new MenuItem(Languages.message("TextFilterBatch"));
        TextFilterBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.TextFilterBatchFxml);
        });

        MenuItem TextToHtml = new MenuItem(Languages.message("TextToHtml"));
        TextToHtml.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.TextToHtmlFxml);
        });

        MenuItem TextToPdf = new MenuItem(Languages.message("TextToPdf"));
        TextToPdf.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.TextToPdfFxml);
        });

        textsMenu.getItems().addAll(
                textEditer, TextConvert, TextMerge, TextReplaceBatch, TextFilterBatch, TextToHtml, TextToPdf
        );

        MenuItem bytesEditer = new MenuItem(Languages.message("BytesEditer"));
        bytesEditer.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.BytesEditorFxml);
        });

        Menu htmlMenu = new Menu(Languages.message("Html"));

        MenuItem htmlEditor = new MenuItem(Languages.message("HtmlEditor"));
        htmlEditor.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlEditorFxml);
        });

        MenuItem htmlToMarkdown = new MenuItem(Languages.message("HtmlToMarkdown"));
        htmlToMarkdown.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlToMarkdownFxml);
        });

        MenuItem HtmlToText = new MenuItem(Languages.message("HtmlToText"));
        HtmlToText.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlToTextFxml);
        });

        MenuItem HtmlToPdf = new MenuItem(Languages.message("HtmlToPdf"));
        HtmlToPdf.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlToPdfFxml);
        });

        MenuItem HtmlSetCharset = new MenuItem(Languages.message("HtmlSetCharset"));
        HtmlSetCharset.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlSetCharsetFxml);
        });

        MenuItem HtmlSetStyle = new MenuItem(Languages.message("HtmlSetStyle"));
        HtmlSetStyle.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlSetStyleFxml);
        });

        MenuItem HtmlSnap = new MenuItem(Languages.message("HtmlSnap"));
        HtmlSnap.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlSnapFxml);
        });

        MenuItem WebFind = new MenuItem(Languages.message("WebFind"));
        WebFind.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlFindFxml);
        });

        MenuItem WebElements = new MenuItem(Languages.message("WebElements"));
        WebElements.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlElementsFxml);
        });

        MenuItem HtmlMergeAsHtml = new MenuItem(Languages.message("HtmlMergeAsHtml"));
        HtmlMergeAsHtml.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlMergeAsHtmlFxml);
        });

        MenuItem HtmlMergeAsMarkdown = new MenuItem(Languages.message("HtmlMergeAsMarkdown"));
        HtmlMergeAsMarkdown.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlMergeAsMarkdownFxml);
        });

        MenuItem HtmlMergeAsPDF = new MenuItem(Languages.message("HtmlMergeAsPDF"));
        HtmlMergeAsPDF.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlMergeAsPDFFxml);
        });

        MenuItem HtmlMergeAsText = new MenuItem(Languages.message("HtmlMergeAsText"));
        HtmlMergeAsText.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlMergeAsTextFxml);
        });

        MenuItem HtmlFrameset = new MenuItem(Languages.message("HtmlFrameset"));
        HtmlFrameset.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlFramesetFxml);
        });

        htmlMenu.getItems().addAll(
                htmlEditor, WebFind, WebElements, HtmlSnap, new SeparatorMenuItem(),
                htmlToMarkdown, HtmlToText, HtmlToPdf, HtmlSetCharset, HtmlSetStyle, new SeparatorMenuItem(),
                HtmlMergeAsHtml, HtmlMergeAsMarkdown, HtmlMergeAsPDF, HtmlMergeAsText, HtmlFrameset
        );

        Menu markdownMenu = new Menu("Markdown");

        MenuItem markdownEditor = new MenuItem(Languages.message("MarkdownEditer"));
        markdownEditor.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.MarkdownEditorFxml);
        });

        MenuItem markdownToHtml = new MenuItem(Languages.message("MarkdownToHtml"));
        markdownToHtml.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.MarkdownToHtmlFxml);
        });

        MenuItem MarkdownToText = new MenuItem(Languages.message("MarkdownToText"));
        MarkdownToText.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.MarkdownToTextFxml);
        });

        MenuItem MarkdownToPdf = new MenuItem(Languages.message("MarkdownToPdf"));
        MarkdownToPdf.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.MarkdownToPdfFxml);
        });

        markdownMenu.getItems().addAll(
                markdownEditor, new SeparatorMenuItem(),
                markdownToHtml, MarkdownToText, MarkdownToPdf
        );

        Menu msMenu = new Menu(Languages.message("MicrosoftDocumentFormats"));

        MenuItem ExtractTextsFromMS = new MenuItem(Languages.message("ExtractTextsFromMS"));
        ExtractTextsFromMS.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ExtractTextsFromMSFxml);
        });

        MenuItem WordView = new MenuItem(Languages.message("WordView"));
        WordView.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.WordViewFxml);
        });

        MenuItem WordToHtml = new MenuItem(Languages.message("WordToHtml"));
        WordToHtml.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.WordToHtmlFxml);
        });

        MenuItem WordToPdf = new MenuItem(Languages.message("WordToPdf"));
        WordToPdf.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.WordToPdfFxml);
        });

        MenuItem PptView = new MenuItem(Languages.message("PptView"));
        PptView.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PptViewFxml);
        });

        MenuItem PptToImages = new MenuItem(Languages.message("PptToImages"));
        PptToImages.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PptToImagesFxml);
        });

        MenuItem PptToPdf = new MenuItem(Languages.message("PptToPdf"));
        PptToPdf.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PptToPdfFxml);
        });

        MenuItem PptExtract = new MenuItem(Languages.message("PptExtract"));
        PptExtract.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PptExtractFxml);
        });

        MenuItem PptxMerge = new MenuItem(Languages.message("PptxMerge"));
        PptxMerge.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PptxMergeFxml);
        });

        MenuItem PptSplit = new MenuItem(Languages.message("PptSplit"));
        PptSplit.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.PptSplitFxml);
        });

        MenuItem imagesCombinePPT = new MenuItem(Languages.message("ImagesCombinePPT"));
        imagesCombinePPT.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImagesEditorFxml);
        });

        MenuItem PptPlay = new MenuItem(Languages.message("PptPlay"));
        PptPlay.setOnAction((ActionEvent event1) -> {
            ImagesPlayController c = (ImagesPlayController) loadScene(Fxmls.ImagesPlayFxml);
            c.pptRadio.fire();
        });

        MenuItem TextInMyBoxClipboard = new MenuItem(Languages.message("TextInMyBoxClipboard"));
        TextInMyBoxClipboard.setOnAction((ActionEvent event1) -> {
            TextInMyBoxClipboardController.oneOpen();
        });

        MenuItem TextInSystemClipboard = new MenuItem(Languages.message("TextInSystemClipboard"));
        TextInSystemClipboard.setOnAction((ActionEvent event1) -> {
            TextInSystemClipboardController.oneOpen();
        });

        msMenu.getItems().addAll(
                WordView, WordToHtml, WordToPdf, new SeparatorMenuItem(),
                PptView, PptToImages, PptToPdf, PptExtract, PptSplit, PptxMerge, imagesCombinePPT, PptPlay, new SeparatorMenuItem(),
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
        MenuItem closeMenu = new MenuItem(Languages.message("PopupClose"));
        closeMenu.setStyle("-fx-text-fill: #2e598a;");
        closeMenu.setOnAction((ActionEvent cevent) -> {
            popMenu.hide();
            popMenu = null;
        });
        popMenu.getItems().add(closeMenu);

        showMenu(documentBox, event);

        view.setImage(new Image("img/DocumentTools.png"));
        text.setText(Languages.message("DocumentToolsImageTips"));
        text.setWrappingWidth(500);
        locateImage(documentBox, true);

    }

}
