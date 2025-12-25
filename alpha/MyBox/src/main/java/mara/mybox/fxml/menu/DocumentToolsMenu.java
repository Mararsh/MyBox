package mara.mybox.fxml.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.DataTreeController;
import mara.mybox.controller.ImagesPlayController;
import mara.mybox.controller.TextInMyBoxClipboardController;
import mara.mybox.controller.TextInSystemClipboardController;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-12-9
 * @License Apache License Version 2.0
 */
public class DocumentToolsMenu {

    public static List<MenuItem> menusList(BaseController controller) {

        Menu treeMenu = new Menu(message("InformationInTree"));

        MenuItem TextTree = new MenuItem(message("TextTree"));
        TextTree.setOnAction((ActionEvent event) -> {
            DataTreeController.textTree(controller, true);
        });

        MenuItem HtmlTree = new MenuItem(message("HtmlTree"));
        HtmlTree.setOnAction((ActionEvent event) -> {
            DataTreeController.htmlTree(controller, true);
        });

        treeMenu.getItems().addAll(TextTree, HtmlTree);

        Menu pdfMenu = new Menu("PDF");

        MenuItem pdfView = new MenuItem(message("PdfView"));
        pdfView.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.PdfViewFxml);
        });

        MenuItem PdfPlay = new MenuItem(message("PdfPlay"));
        PdfPlay.setOnAction((ActionEvent event) -> {
            ImagesPlayController c = (ImagesPlayController) controller.openScene(Fxmls.ImagesPlayFxml);
            c.setAsPDF();
        });

        MenuItem PDFAttributes = new MenuItem(message("PDFAttributes"));
        PDFAttributes.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.PdfAttributesFxml);
        });

        MenuItem PDFAttributesBatch = new MenuItem(message("PDFAttributesBatch"));
        PDFAttributesBatch.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.PdfAttributesBatchFxml);
        });

        MenuItem pdfExtractImagesBatch = new MenuItem(message("PdfExtractImagesBatch"));
        pdfExtractImagesBatch.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.PdfExtractImagesBatchFxml);
        });

        MenuItem pdfExtractTextsBatch = new MenuItem(message("PdfExtractTextsBatch"));
        pdfExtractTextsBatch.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.PdfExtractTextsBatchFxml);
        });

        MenuItem pdfConvertImagesBatch = new MenuItem(message("PdfConvertImagesBatch"));
        pdfConvertImagesBatch.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.PdfConvertImagesBatchFxml);
        });

        MenuItem pdfOcrBatch = new MenuItem(message("PdfOCRBatch"));
        pdfOcrBatch.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.PdfOCRBatchFxml);
        });

        MenuItem pdfConvertHtmlsBatch = new MenuItem(message("PdfConvertHtmlsBatch"));
        pdfConvertHtmlsBatch.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.PdfConvertHtmlsBatchFxml);
        });

        MenuItem imagesCombinePdf = new MenuItem(message("ImagesCombinePdf"));
        imagesCombinePdf.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImagesEditorFxml);
        });

        MenuItem pdfCompressImagesBatch = new MenuItem(message("PdfCompressImagesBatch"));
        pdfCompressImagesBatch.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.PdfCompressImagesBatchFxml);
        });

        MenuItem PdfImagesConvertBatch = new MenuItem(message("PdfImagesConvertBatch"));
        PdfImagesConvertBatch.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.PdfImagesConvertBatchFxml);
        });

        MenuItem pdfMerge = new MenuItem(message("MergePdf"));
        pdfMerge.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.PdfMergeFxml);
        });

        MenuItem PdfSplitBatch = new MenuItem(message("PdfSplitBatch"));
        PdfSplitBatch.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.PdfSplitBatchFxml);
        });

        MenuItem PdfAddWatermarkBatch = new MenuItem(message("PdfAddWatermarkBatch"));
        PdfAddWatermarkBatch.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.PdfAddWatermarkBatchFxml);
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
        textEditer.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.TextEditorFxml);
        });

        MenuItem TextConvert = new MenuItem(message("TextConvertSplit"));
        TextConvert.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.TextFilesConvertFxml);
        });

        MenuItem TextMerge = new MenuItem(message("TextFilesMerge"));
        TextMerge.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.TextFilesMergeFxml);
        });

        MenuItem TextFindBatch = new MenuItem(message("TextFindBatch"));
        TextFindBatch.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.TextFindBatchFxml);
        });

        MenuItem TextReplaceBatch = new MenuItem(message("TextReplaceBatch"));
        TextReplaceBatch.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.TextReplaceBatchFxml);
        });

        MenuItem TextFilterBatch = new MenuItem(message("TextFilterBatch"));
        TextFilterBatch.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.TextFilterBatchFxml);
        });

        MenuItem TextToHtml = new MenuItem(message("TextToHtml"));
        TextToHtml.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.TextToHtmlFxml);
        });

        MenuItem TextToPdf = new MenuItem(message("TextToPdf"));
        TextToPdf.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.TextToPdfFxml);
        });

        textsMenu.getItems().addAll(
                textEditer, TextFindBatch, TextReplaceBatch, TextFilterBatch, TextConvert, TextMerge, TextToHtml, TextToPdf
        );

        Menu bytesMenu = new Menu(message("Bytes"));

        MenuItem bytesEditer = new MenuItem(message("BytesEditer"));
        bytesEditer.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.BytesEditorFxml);
        });

        MenuItem BytesFindBatch = new MenuItem(message("BytesFindBatch"));
        BytesFindBatch.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.BytesFindBatchFxml);
        });

        bytesMenu.getItems().addAll(
                bytesEditer, BytesFindBatch
        );

        Menu htmlMenu = new Menu(message("Html"));

        MenuItem htmlEditor = new MenuItem(message("HtmlEditor"));
        htmlEditor.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.HtmlEditorFxml);
        });

        MenuItem htmlToMarkdown = new MenuItem(message("HtmlToMarkdown"));
        htmlToMarkdown.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.HtmlToMarkdownFxml);
        });

        MenuItem HtmlToText = new MenuItem(message("HtmlToText"));
        HtmlToText.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.HtmlToTextFxml);
        });

        MenuItem HtmlToPdf = new MenuItem(message("HtmlToPdf"));
        HtmlToPdf.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.HtmlToPdfFxml);
        });

        MenuItem HtmlSetCharset = new MenuItem(message("HtmlSetCharset"));
        HtmlSetCharset.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.HtmlSetCharsetFxml);
        });

        MenuItem HtmlSetStyle = new MenuItem(message("HtmlSetStyle"));
        HtmlSetStyle.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.HtmlSetStyleFxml);
        });

        MenuItem HtmlSetEquiv = new MenuItem(message("HtmlSetEquiv"));
        HtmlSetStyle.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.HtmlSetEquivFxml);
        });

        MenuItem HtmlSnap = new MenuItem(message("HtmlSnap"));
        HtmlSnap.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.HtmlSnapFxml);
        });

        MenuItem HtmlTypesetting = new MenuItem(message("HtmlTypesetting"));
        HtmlTypesetting.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.HtmlTypesettingFxml);
        });

        MenuItem WebFind = new MenuItem(message("WebFind"));
        WebFind.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.HtmlFindFxml);
        });

        MenuItem HtmlExtractTables = new MenuItem(message("HtmlExtractTables"));
        HtmlExtractTables.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.HtmlExtractTablesFxml);
        });

        MenuItem WebElements = new MenuItem(message("WebElements"));
        WebElements.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.HtmlElementsFxml);
        });

        MenuItem HtmlMergeAsHtml = new MenuItem(message("HtmlMergeAsHtml"));
        HtmlMergeAsHtml.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.HtmlMergeAsHtmlFxml);
        });

        MenuItem HtmlMergeAsMarkdown = new MenuItem(message("HtmlMergeAsMarkdown"));
        HtmlMergeAsMarkdown.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.HtmlMergeAsMarkdownFxml);
        });

        MenuItem HtmlMergeAsPDF = new MenuItem(message("HtmlMergeAsPDF"));
        HtmlMergeAsPDF.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.HtmlMergeAsPDFFxml);
        });

        MenuItem HtmlMergeAsText = new MenuItem(message("HtmlMergeAsText"));
        HtmlMergeAsText.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.HtmlMergeAsTextFxml);
        });

        MenuItem HtmlFrameset = new MenuItem(message("HtmlFrameset"));
        HtmlFrameset.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.HtmlFramesetFxml);
        });

        htmlMenu.getItems().addAll(
                htmlEditor, WebFind, WebElements, HtmlSnap, HtmlExtractTables, new SeparatorMenuItem(),
                HtmlTypesetting, htmlToMarkdown, HtmlToText, HtmlToPdf, HtmlSetCharset, HtmlSetStyle, HtmlSetEquiv, new SeparatorMenuItem(),
                HtmlMergeAsHtml, HtmlMergeAsMarkdown, HtmlMergeAsPDF, HtmlMergeAsText, HtmlFrameset
        );

        Menu markdownMenu = new Menu("Markdown");

        MenuItem markdownEditor = new MenuItem(message("MarkdownEditer"));
        markdownEditor.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.MarkdownEditorFxml);
        });

        MenuItem markdownOptions = new MenuItem(message("MarkdownOptions"));
        markdownOptions.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.MarkdownOptionsFxml);
        });

        MenuItem MarkdownTypesetting = new MenuItem(message("MarkdownTypesetting"));
        MarkdownTypesetting.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.MarkdownTypesettingFxml);
        });

        MenuItem markdownToHtml = new MenuItem(message("MarkdownToHtml"));
        markdownToHtml.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.MarkdownToHtmlFxml);
        });

        MenuItem MarkdownToText = new MenuItem(message("MarkdownToText"));
        MarkdownToText.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.MarkdownToTextFxml);
        });

        MenuItem MarkdownToPdf = new MenuItem(message("MarkdownToPdf"));
        MarkdownToPdf.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.MarkdownToPdfFxml);
        });

        markdownMenu.getItems().addAll(
                markdownEditor, markdownOptions, new SeparatorMenuItem(),
                MarkdownTypesetting, markdownToHtml, MarkdownToText, MarkdownToPdf
        );

        Menu jsonMenu = new Menu("JSON");

        MenuItem jsonEditorMenu = new MenuItem(message("JsonEditor"));
        jsonEditorMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.JsonEditorFxml);
        });

        MenuItem jsonTypesettingMenu = new MenuItem(message("JsonTypesetting"));
        jsonTypesettingMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.JsonTypesettingFxml);
        });

        jsonMenu.getItems().addAll(
                jsonEditorMenu, jsonTypesettingMenu
        );

        Menu xmlMenu = new Menu("XML");

        MenuItem xmlEditorMenu = new MenuItem(message("XmlEditor"));
        xmlEditorMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.XmlEditorFxml);
        });

        MenuItem xmlTypesettingMenu = new MenuItem(message("XmlTypesetting"));
        xmlTypesettingMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.XmlTypesettingFxml);
        });

        xmlMenu.getItems().addAll(
                xmlEditorMenu, xmlTypesettingMenu
        );

        Menu msMenu = new Menu(message("MicrosoftDocumentFormats"));

        MenuItem ExtractTextsFromMS = new MenuItem(message("ExtractTextsFromMS"));
        ExtractTextsFromMS.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ExtractTextsFromMSFxml);
        });

        MenuItem WordView = new MenuItem(message("WordView"));
        WordView.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.WordViewFxml);
        });

        MenuItem WordToHtml = new MenuItem(message("WordToHtml"));
        WordToHtml.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.WordToHtmlFxml);
        });

        MenuItem WordToPdf = new MenuItem(message("WordToPdf"));
        WordToPdf.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.WordToPdfFxml);
        });

        MenuItem PptView = new MenuItem(message("PptView"));
        PptView.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.PptViewFxml);
        });

        MenuItem PptToImages = new MenuItem(message("PptToImages"));
        PptToImages.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.PptToImagesFxml);
        });

        MenuItem PptToPdf = new MenuItem(message("PptToPdf"));
        PptToPdf.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.PptToPdfFxml);
        });

        MenuItem PptExtract = new MenuItem(message("PptExtract"));
        PptExtract.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.PptExtractFxml);
        });

        MenuItem PptxMerge = new MenuItem(message("PptxMerge"));
        PptxMerge.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.PptxMergeFxml);
        });

        MenuItem PptSplit = new MenuItem(message("PptSplit"));
        PptSplit.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.PptSplitFxml);
        });

        MenuItem imagesCombinePPT = new MenuItem(message("ImagesCombinePPT"));
        imagesCombinePPT.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImagesEditorFxml);
        });

        MenuItem PptPlay = new MenuItem(message("PptPlay"));
        PptPlay.setOnAction((ActionEvent event) -> {
            ImagesPlayController c = (ImagesPlayController) controller.openScene(Fxmls.ImagesPlayFxml);
            c.setAsPPT();
        });

        MenuItem TextInMyBoxClipboard = new MenuItem(message("TextInMyBoxClipboard"));
        TextInMyBoxClipboard.setOnAction((ActionEvent event) -> {
            TextInMyBoxClipboardController.oneOpen();
        });

        MenuItem TextInSystemClipboard = new MenuItem(message("TextInSystemClipboard"));
        TextInSystemClipboard.setOnAction((ActionEvent event) -> {
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

}
