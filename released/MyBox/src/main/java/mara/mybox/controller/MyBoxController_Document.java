package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_Document extends MyBoxController_Base {

    @FXML
    protected void showDocumentMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem Notes = new MenuItem(message("Notes"));
        Notes.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.NotesFxml);
        });

        MenuItem InformationInTree = new MenuItem(message("InformationInTree"));
        InformationInTree.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.TreeManageFxml);
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

        pdfMenu.getItems().addAll(
                pdfView, PdfPlay, new SeparatorMenuItem(),
                pdfConvertImagesBatch, PdfImagesConvertBatch, pdfConvertHtmlsBatch, pdfCompressImagesBatch, new SeparatorMenuItem(),
                pdfExtractImagesBatch, pdfExtractTextsBatch, pdfOcrBatch, new SeparatorMenuItem(),
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
                textEditer, TextConvert, TextMerge, TextReplaceBatch, TextFilterBatch, TextToHtml, TextToPdf
        );

        MenuItem bytesEditer = new MenuItem(message("BytesEditer"));
        bytesEditer.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.BytesEditorFxml);
        });

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

        MenuItem HtmlSnap = new MenuItem(message("HtmlSnap"));
        HtmlSnap.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.HtmlSnapFxml);
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
                htmlToMarkdown, HtmlToText, HtmlToPdf, HtmlSetCharset, HtmlSetStyle, new SeparatorMenuItem(),
                HtmlMergeAsHtml, HtmlMergeAsMarkdown, HtmlMergeAsPDF, HtmlMergeAsText, HtmlFrameset
        );

        Menu markdownMenu = new Menu("Markdown");

        MenuItem markdownEditor = new MenuItem(message("MarkdownEditer"));
        markdownEditor.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.MarkdownEditorFxml);
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
                markdownEditor, new SeparatorMenuItem(),
                markdownToHtml, MarkdownToText, MarkdownToPdf
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

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                Notes, InformationInTree, new SeparatorMenuItem(),
                pdfMenu, new SeparatorMenuItem(),
                markdownMenu, new SeparatorMenuItem(),
                htmlMenu, new SeparatorMenuItem(),
                textsMenu, new SeparatorMenuItem(),
                msMenu, new SeparatorMenuItem(),
                bytesEditer, new SeparatorMenuItem(),
                TextInMyBoxClipboard, TextInSystemClipboard
        );

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem closeMenu = new MenuItem(message("PopupClose"), StyleTools.getIconImage("iconCancel.png"));
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

}
