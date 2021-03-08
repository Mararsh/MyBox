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
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.AppVariables;
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
        baseTitle = AppVariables.message("MergePdf");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableView.getItems())
                            .or(Bindings.isEmpty(targetFileInput.textProperty()))
                            .or(targetFileInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
    public String handleFile(File srcFile, File targetPath) {
        int generated = 0;
        doc = null;
        try {
            currentParameters.currentSourceFile = srcFile;
            PdfInformation info = tableData.get(currentParameters.currentIndex);
            actualParameters.fromPage = info.getFromPage();
            if (actualParameters.fromPage <= 0) {
                actualParameters.fromPage = 1;
            }
            actualParameters.toPage = info.getToPage();
            actualParameters.password = info.getUserPassword();
            try ( PDDocument pd = PDDocument.load(currentParameters.currentSourceFile,
                    currentParameters.password, AppVariables.pdfMemUsage)) {
                doc = pd;
                if (currentParameters.toPage <= 0 || currentParameters.toPage > doc.getNumberOfPages()) {
                    currentParameters.toPage = doc.getNumberOfPages();
                }
                currentParameters.currentTargetPath = targetPath;
                extractor = new PageExtractor(doc, currentParameters.fromPage, currentParameters.toPage);
                PDDocument subDoc = extractor.extract();
                if (subDoc != null) {
                    mergePdf.appendDocument(targetDoc, subDoc);
                    subDoc.close();
                    generated = 1;
                }
                doc.close();
            }

            updateInterface("CompleteFile");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return MessageFormat.format(AppVariables.message("HandlePagesGenerateNumber"),
                currentParameters.toPage - currentParameters.fromPage, generated);
    }

    @Override
    public void donePost() {
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
                            FileTools.delete(source.getFile());
                            tableData.remove(i);
                        } catch (Exception e) {
                        }
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        targetDoc = null;
        super.donePost();
    }

}
