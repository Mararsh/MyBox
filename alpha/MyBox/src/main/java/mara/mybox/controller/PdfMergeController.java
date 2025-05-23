package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data.PdfInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.PageExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * @Author Mara
 * @CreateDate 2018-9-10
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfMergeController extends BaseBatchPdfController {

    private PDFMergerUtility mergePdf;
    private PageExtractor extractor;
    private PDDocument targetDoc;

    @FXML
    protected CheckBox deleteCheck;

    public PdfMergeController() {
        baseTitle = Languages.message("MergePdf");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(tableView.getItems()));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean makeMoreParameters() {
        if (mergePdf == null) {
            mergePdf = new PDFMergerUtility();
        }
        targetDoc = PdfTools.createPDF(targetFile);
        if (mergePdf == null || targetDoc == null) {
            return false;
        }
        return super.makeMoreParameters();
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        int generated = 0;
        doc = null;
        try {
            PdfInformation info = currentPdf();
            actualParameters.fromPage = info.getFromPage();
            if (actualParameters.fromPage < 0) {
                actualParameters.fromPage = 0;
            }
            actualParameters.toPage = info.getToPage();
            actualParameters.password = info.getUserPassword();
            File pdfFile = currentSourceFile();
            try (PDDocument pd = Loader.loadPDF(pdfFile, currentParameters.password)) {
                doc = pd;
                if (currentTask == null || !currentTask.isWorking()) {
                    return message("Canceled");
                }
                if (currentParameters.toPage <= 0
                        || currentParameters.toPage > doc.getNumberOfPages()) {
                    currentParameters.toPage = doc.getNumberOfPages();
                }
                currentParameters.currentTargetPath = targetPath;
                extractor = new PageExtractor(doc,
                        currentParameters.fromPage + 1,
                        currentParameters.toPage);  // 1-based, inclusive
                PDDocument subDoc = extractor.extract();
                if (currentTask == null || !currentTask.isWorking()) {
                    return message("Canceled");
                }
                if (subDoc != null) {
                    mergePdf.appendDocument(targetDoc, subDoc);
                    subDoc.close();
                    generated = 1;
                }
                doc.close();
            }

            updateInterface("CompleteFile");
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return MessageFormat.format(Languages.message("HandlePagesGenerateNumber"),
                currentParameters.toPage - currentParameters.fromPage, generated);
    }

    @Override
    public void handleTargetFiles() {
        try {
            if (targetDoc != null) {
                targetDoc.save(targetFile);
                targetDoc.close();
                targetFileGenerated(targetFile);

                if (deleteCheck.isSelected()) {
                    List<PdfInformation> sources = new ArrayList<>();
                    sources.addAll(tableData);
                    for (int i = sources.size() - 1; i >= 0; --i) {
                        try {
                            PdfInformation source = sources.get(i);
                            FileDeleteTools.delete(source.getFile());
                            tableData.remove(i);
                        } catch (Exception e) {
                        }
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        targetDoc = null;
        super.handleTargetFiles();
    }

}
