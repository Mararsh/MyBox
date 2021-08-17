package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MainMenuController_Document extends MainMenuController_Window {

    @FXML
    protected void openNotes(ActionEvent event) {
        loadScene(Fxmls.NotesFxml);
    }

    @FXML
    protected void openPdfView(ActionEvent event) {
        loadScene(Fxmls.PdfViewFxml);
    }

    @FXML
    protected void openPDFAttributes(ActionEvent event) {
        loadScene(Fxmls.PdfAttributesFxml);
    }

    @FXML
    protected void openPDFAttributesBatch(ActionEvent event) {
        loadScene(Fxmls.PdfAttributesBatchFxml);
    }

    @FXML
    protected void openPdfConvertImagesBatch(ActionEvent event) {
        loadScene(Fxmls.PdfConvertImagesBatchFxml);
    }

    @FXML
    protected void openPdfConvertHtmlsBatch(ActionEvent event) {
        loadScene(Fxmls.PdfConvertHtmlsBatchFxml);
    }

    @FXML
    protected void openPdfExtractTextsBatch(ActionEvent event) {
        loadScene(Fxmls.PdfExtractTextsBatchFxml);
    }

    @FXML
    protected void openPdfExtractImagesBatch(ActionEvent event) {
        loadScene(Fxmls.PdfExtractImagesBatchFxml);
    }

    @FXML
    protected void openPdfImagesConvertBatch(ActionEvent event) {
        loadScene(Fxmls.PdfImagesConvertBatchFxml);
    }

    @FXML
    protected void openMergePdf(ActionEvent event) {
        loadScene(Fxmls.PdfMergeFxml);
    }

    @FXML
    protected void openPdfSplitBatch(ActionEvent event) {
        loadScene(Fxmls.PdfSplitBatchFxml);
    }

    @FXML
    protected void openPdfOCRBatch(ActionEvent event) {
        loadScene(Fxmls.PdfOCRBatchFxml);
    }

    @FXML
    protected void pdfPlay(ActionEvent event) {
        ImagesPlayController c = (ImagesPlayController) loadScene(Fxmls.ImagesPlayFxml);
        c.pdfRadio.fire();
    }

    @FXML
    protected void openCompressPdfImagesBatch(ActionEvent event) {
        loadScene(Fxmls.PdfCompressImagesBatchFxml);
    }

    @FXML
    protected void openMarkdownEditer(ActionEvent event) {
        loadScene(Fxmls.MarkdownEditorFxml);
    }

    @FXML
    protected void openMarkdownToHtml(ActionEvent event) {
        loadScene(Fxmls.MarkdownToHtmlFxml);
    }

    @FXML
    protected void openMarkdownToText(ActionEvent event) {
        loadScene(Fxmls.MarkdownToTextFxml);
    }

    @FXML
    protected void openMarkdownToPdf(ActionEvent event) {
        loadScene(Fxmls.MarkdownToPdfFxml);
    }

    @FXML
    protected void openHtmlToMarkdown(ActionEvent event) {
        loadScene(Fxmls.HtmlToMarkdownFxml);
    }

    @FXML
    protected void openHtmlToText(ActionEvent event) {
        loadScene(Fxmls.HtmlToTextFxml);
    }

    @FXML
    protected void openHtmlToPdf(ActionEvent event) {
        loadScene(Fxmls.HtmlToPdfFxml);
    }

    @FXML
    protected void openHtmlSetCharset(ActionEvent event) {
        loadScene(Fxmls.HtmlSetCharsetFxml);
    }

    @FXML
    protected void openHtmlSetStyle(ActionEvent event) {
        loadScene(Fxmls.HtmlSetStyleFxml);
    }

    @FXML
    protected void openHtmlSnap(ActionEvent event) {
        loadScene(Fxmls.HtmlSnapFxml);
    }

    @FXML
    protected void openHtmlMergeAsHtml(ActionEvent event) {
        loadScene(Fxmls.HtmlMergeAsHtmlFxml);
    }

    @FXML
    protected void openHtmlMergeAsMarkdown(ActionEvent event) {
        loadScene(Fxmls.HtmlMergeAsMarkdownFxml);
    }

    @FXML
    protected void openHtmlMergeAsPDF(ActionEvent event) {
        loadScene(Fxmls.HtmlMergeAsPDFFxml);
    }

    @FXML
    protected void openHtmlMergeAsText(ActionEvent event) {
        loadScene(Fxmls.HtmlMergeAsTextFxml);
    }

    @FXML
    protected void openHtmlFrameset(ActionEvent event) {
        loadScene(Fxmls.HtmlFramesetFxml);
    }

    @FXML
    protected void openHtmlEditor(ActionEvent event) {
        loadScene(Fxmls.HtmlEditorFxml);
    }

    @FXML
    protected void openTextEditer(ActionEvent event) {
        loadScene(Fxmls.TextEditorFxml);
    }

    @FXML
    protected void openTextConvert(ActionEvent event) {
        loadScene(Fxmls.TextFilesConvertFxml);
    }

    @FXML
    protected void openTextMerge(ActionEvent event) {
        loadScene(Fxmls.TextFilesMergeFxml);
    }

    @FXML
    protected void openTextReplaceBatch(ActionEvent event) {
        loadScene(Fxmls.TextReplaceBatchFxml);
    }

    @FXML
    protected void TextFilterBatch(ActionEvent event) {
        loadScene(Fxmls.TextFilterBatchFxml);
    }

    @FXML
    protected void openTextToHtml(ActionEvent event) {
        loadScene(Fxmls.TextToHtmlFxml);
    }

    @FXML
    protected void openBytesEditer(ActionEvent event) {
        loadScene(Fxmls.BytesEditorFxml);
    }

    @FXML
    protected void extractTextsFromMS(ActionEvent event) {
        loadScene(Fxmls.ExtractTextsFromMSFxml);
    }

    @FXML
    protected void WordView(ActionEvent event) {
        loadScene(Fxmls.WordViewFxml);
    }

    @FXML
    protected void WordToHtml(ActionEvent event) {
        loadScene(Fxmls.WordToHtmlFxml);
    }

    @FXML
    protected void PptView(ActionEvent event) {
        loadScene(Fxmls.PptViewFxml);
    }

    @FXML
    protected void PptToImages(ActionEvent event) {
        loadScene(Fxmls.PptToImagesFxml);
    }

    @FXML
    protected void PptExtract(ActionEvent event) {
        loadScene(Fxmls.PptExtractFxml);
    }

    @FXML
    protected void PptxMerge(ActionEvent event) {
        loadScene(Fxmls.PptxMergeFxml);
    }

    @FXML
    protected void PptSplit(ActionEvent event) {
        loadScene(Fxmls.PptSplitFxml);
    }

    @FXML
    protected void pptPlay(ActionEvent event) {
        ImagesPlayController c = (ImagesPlayController) loadScene(Fxmls.ImagesPlayFxml);
        c.pptRadio.fire();
    }

    @FXML
    protected void TextInMyBoxClipboard(ActionEvent event) {
        TextInMyBoxClipboardController.oneOpen();
    }

    @FXML
    protected void TextInSystemClipboard(ActionEvent event) {
        TextInSystemClipboardController.oneOpen();
    }

}
