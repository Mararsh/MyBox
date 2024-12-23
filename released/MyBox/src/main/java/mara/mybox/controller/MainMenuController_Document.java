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
    protected void HtmlTree(ActionEvent event) {
        DataTreeController.htmlTree(parentController, false);
    }

    @FXML
    protected void TextTree(ActionEvent event) {
        DataTreeController.textTree(parentController, false);
    }

    @FXML
    protected void openPdfView(ActionEvent event) {
        openScene(Fxmls.PdfViewFxml);
    }

    @FXML
    protected void openPDFAttributes(ActionEvent event) {
        openScene(Fxmls.PdfAttributesFxml);
    }

    @FXML
    protected void openPDFAttributesBatch(ActionEvent event) {
        openScene(Fxmls.PdfAttributesBatchFxml);
    }

    @FXML
    protected void openPdfConvertImagesBatch(ActionEvent event) {
        openScene(Fxmls.PdfConvertImagesBatchFxml);
    }

    @FXML
    protected void openPdfConvertHtmlsBatch(ActionEvent event) {
        openScene(Fxmls.PdfConvertHtmlsBatchFxml);
    }

    @FXML
    protected void openPdfExtractTextsBatch(ActionEvent event) {
        openScene(Fxmls.PdfExtractTextsBatchFxml);
    }

    @FXML
    protected void openPdfExtractImagesBatch(ActionEvent event) {
        openScene(Fxmls.PdfExtractImagesBatchFxml);
    }

    @FXML
    protected void openPdfImagesConvertBatch(ActionEvent event) {
        openScene(Fxmls.PdfImagesConvertBatchFxml);
    }

    @FXML
    protected void openMergePdf(ActionEvent event) {
        openScene(Fxmls.PdfMergeFxml);
    }

    @FXML
    protected void openPdfSplitBatch(ActionEvent event) {
        openScene(Fxmls.PdfSplitBatchFxml);
    }

    @FXML
    protected void openPdfOCRBatch(ActionEvent event) {
        openScene(Fxmls.PdfOCRBatchFxml);
    }

    @FXML
    protected void pdfPlay(ActionEvent event) {
        ImagesPlayController c = (ImagesPlayController) loadScene(Fxmls.ImagesPlayFxml);
        c.pdfRadio.setSelected(true);
    }

    @FXML
    protected void openPdfAddWatermark(ActionEvent event) {
        openScene(Fxmls.PdfAddWatermarkBatchFxml);
    }

    @FXML
    protected void openCompressPdfImagesBatch(ActionEvent event) {
        openScene(Fxmls.PdfCompressImagesBatchFxml);
    }

    @FXML
    protected void openMarkdownEditer(ActionEvent event) {
        openScene(Fxmls.MarkdownEditorFxml);
    }

    @FXML
    protected void markdownOptions(ActionEvent event) {
        openScene(Fxmls.MarkdownOptionsFxml);
    }

    @FXML
    protected void MarkdownTypesetting(ActionEvent event) {
        openScene(Fxmls.MarkdownTypesettingFxml);
    }

    @FXML
    protected void openMarkdownToHtml(ActionEvent event) {
        openScene(Fxmls.MarkdownToHtmlFxml);
    }

    @FXML
    protected void openMarkdownToText(ActionEvent event) {
        openScene(Fxmls.MarkdownToTextFxml);
    }

    @FXML
    protected void openMarkdownToPdf(ActionEvent event) {
        openScene(Fxmls.MarkdownToPdfFxml);
    }

    @FXML
    protected void openHtmlEditor(ActionEvent event) {
        openScene(Fxmls.HtmlEditorFxml);
    }

    @FXML
    protected void htmlFind(ActionEvent event) {
        openScene(Fxmls.HtmlFindFxml);
    }

    @FXML
    protected void htmlElements(ActionEvent event) {
        openScene(Fxmls.HtmlElementsFxml);
    }

    @FXML
    protected void openHtmlToMarkdown(ActionEvent event) {
        openScene(Fxmls.HtmlToMarkdownFxml);
    }

    @FXML
    protected void openHtmlToText(ActionEvent event) {
        openScene(Fxmls.HtmlToTextFxml);
    }

    @FXML
    protected void openHtmlToPdf(ActionEvent event) {
        openScene(Fxmls.HtmlToPdfFxml);
    }

    @FXML
    protected void openHtmlSetCharset(ActionEvent event) {
        openScene(Fxmls.HtmlSetCharsetFxml);
    }

    @FXML
    protected void openHtmlSetStyle(ActionEvent event) {
        openScene(Fxmls.HtmlSetStyleFxml);
    }

    @FXML
    protected void openHtmlSetEquiv(ActionEvent event) {
        openScene(Fxmls.HtmlSetEquivFxml);
    }

    @FXML
    protected void openHtmlSnap(ActionEvent event) {
        openScene(Fxmls.HtmlSnapFxml);
    }

    @FXML
    protected void HtmlTypesetting(ActionEvent event) {
        openScene(Fxmls.HtmlTypesettingFxml);
    }

    @FXML
    protected void htmlExtractTables(ActionEvent event) {
        openScene(Fxmls.HtmlExtractTablesFxml);
    }

    @FXML
    protected void openHtmlMergeAsHtml(ActionEvent event) {
        openScene(Fxmls.HtmlMergeAsHtmlFxml);
    }

    @FXML
    protected void openHtmlMergeAsMarkdown(ActionEvent event) {
        openScene(Fxmls.HtmlMergeAsMarkdownFxml);
    }

    @FXML
    protected void openHtmlMergeAsPDF(ActionEvent event) {
        openScene(Fxmls.HtmlMergeAsPDFFxml);
    }

    @FXML
    protected void openHtmlMergeAsText(ActionEvent event) {
        openScene(Fxmls.HtmlMergeAsTextFxml);
    }

    @FXML
    protected void openHtmlFrameset(ActionEvent event) {
        openScene(Fxmls.HtmlFramesetFxml);
    }

    @FXML
    protected void JsonEditor(ActionEvent event) {
        openScene(Fxmls.JsonEditorFxml);
    }

    @FXML
    protected void JsonTypesetting(ActionEvent event) {
        openScene(Fxmls.JsonTypesettingFxml);
    }

    @FXML
    protected void XmlEditor(ActionEvent event) {
        openScene(Fxmls.XmlEditorFxml);
    }

    @FXML
    protected void XmlTypesetting(ActionEvent event) {
        openScene(Fxmls.XmlTypesettingFxml);
    }

    @FXML
    protected void openTextEditer(ActionEvent event) {
        openScene(Fxmls.TextEditorFxml);
    }

    @FXML
    protected void openTextConvert(ActionEvent event) {
        openScene(Fxmls.TextFilesConvertFxml);
    }

    @FXML
    protected void openTextMerge(ActionEvent event) {
        openScene(Fxmls.TextFilesMergeFxml);
    }

    @FXML
    protected void openTextFindBatch(ActionEvent event) {
        openScene(Fxmls.TextFindBatchFxml);
    }

    @FXML
    protected void openTextReplaceBatch(ActionEvent event) {
        openScene(Fxmls.TextReplaceBatchFxml);
    }

    @FXML
    protected void TextFilterBatch(ActionEvent event) {
        openScene(Fxmls.TextFilterBatchFxml);
    }

    @FXML
    protected void textToHtml(ActionEvent event) {
        openScene(Fxmls.TextToHtmlFxml);
    }

    @FXML
    protected void textToPdf(ActionEvent event) {
        openScene(Fxmls.TextToPdfFxml);
    }

    @FXML
    protected void openBytesEditer(ActionEvent event) {
        openScene(Fxmls.BytesEditorFxml);
    }

    @FXML
    protected void BytesFindBatch(ActionEvent event) {
        openScene(Fxmls.BytesFindBatchFxml);
    }

    @FXML
    protected void extractTextsFromMS(ActionEvent event) {
        openScene(Fxmls.ExtractTextsFromMSFxml);
    }

    @FXML
    protected void WordView(ActionEvent event) {
        openScene(Fxmls.WordViewFxml);
    }

    @FXML
    protected void WordToHtml(ActionEvent event) {
        openScene(Fxmls.WordToHtmlFxml);
    }

    @FXML
    protected void WordToPdf(ActionEvent event) {
        openScene(Fxmls.WordToPdfFxml);
    }

    @FXML
    protected void PptView(ActionEvent event) {
        openScene(Fxmls.PptViewFxml);
    }

    @FXML
    protected void PptToImages(ActionEvent event) {
        openScene(Fxmls.PptToImagesFxml);
    }

    @FXML
    protected void PptToPdf(ActionEvent event) {
        openScene(Fxmls.PptToPdfFxml);
    }

    @FXML
    protected void PptExtract(ActionEvent event) {
        openScene(Fxmls.PptExtractFxml);
    }

    @FXML
    protected void PptxMerge(ActionEvent event) {
        openScene(Fxmls.PptxMergeFxml);
    }

    @FXML
    protected void PptSplit(ActionEvent event) {
        openScene(Fxmls.PptSplitFxml);
    }

    @FXML
    protected void pptPlay(ActionEvent event) {
        ImagesPlayController c = (ImagesPlayController) loadScene(Fxmls.ImagesPlayFxml);
        c.pptRadio.setSelected(true);
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
