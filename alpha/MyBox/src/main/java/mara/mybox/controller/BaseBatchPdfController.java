package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import javafx.fxml.FXML;
import mara.mybox.data.PdfInformation;
import mara.mybox.data.ProcessParameters;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileNameTools;
import static mara.mybox.value.Languages.message;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class BaseBatchPdfController extends BaseBatchController<PdfInformation> {

    protected ControlPdfsTable pdfsTableController;
    protected String password, currentTargetFile;
    protected int fromPage, toPage, startPage;  // 0-based, exclude end
    protected PDDocument doc;

    public BaseBatchPdfController() {
        targetSubdirKey = "PdfCreatSubdir";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            pdfsTableController = (ControlPdfsTable) tableController;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.PDF);
    }

    public PdfInformation currentPdf() {
        try {
            return (PdfInformation) currentParameters.currentSourceFile;
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    @Override
    public boolean makePreviewParameters() {
        if (!makeActualParameters()) {
            popError(message("Invalid"));
            actualParameters = null;
            return false;
        }
        try {
            previewParameters = (ProcessParameters) actualParameters.clone();
        } catch (Exception e) {
            return false;
        }
        PdfInformation info = tableData.get(0);
        pageIndex = info.getFromPage();
        previewParameters.password = info.getUserPassword();
        previewParameters.fromPage = pageIndex;
        previewParameters.toPage = pageIndex;
        previewParameters.startPage = pageIndex;
        previewParameters.status = "start";
        currentParameters = previewParameters;
        isPreview = true;
        return true;
    }

    // page number is 1-based.
    // Notice: Some APIs of PdfBox are 0-base and others are 1-based.
    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        int generated = 0;
        doc = null;
        try {
            if (!isPreview) {
                PdfInformation info = currentPdf();
                currentParameters.fromPage = info.getFromPage();
                if (currentParameters.fromPage < 0) {
                    currentParameters.fromPage = 0;
                }
                currentParameters.toPage = info.getToPage();
                currentParameters.password = info.getUserPassword();
                pageIndex = currentParameters.fromPage;
                currentParameters.startPage = pageIndex;
            }
            try (PDDocument pd = Loader.loadPDF(srcFile, currentParameters.password)) {
                doc = pd;
                if (currentParameters.toPage <= 0
                        || currentParameters.toPage > doc.getNumberOfPages()) {
                    currentParameters.toPage = doc.getNumberOfPages();
                }

                currentParameters.currentTargetPath = targetPath;
                if (currentParameters.targetSubDir) {
                    currentParameters.currentTargetPath = new File(targetPath.getAbsolutePath() + "/"
                            + FileNameTools.prefix(srcFile.getName()));
                    if (!currentParameters.currentTargetPath.exists()) {
                        currentParameters.currentTargetPath.mkdirs();
                    }
                }
                if (preHandlePages(currentTask)) {
                    int total = currentParameters.toPage - currentParameters.fromPage;
                    updateFileProgress(0, total);
                    for (pageIndex = currentParameters.startPage;
                            pageIndex < currentParameters.toPage; pageIndex++) {
                        if (currentTask == null || currentTask.isCancelled()) {
                            break;
                        }

                        generated += handleCurrentPage(currentTask);

                        updateFileProgress(pageIndex - currentParameters.fromPage + 1, total);
                    }
                }
                postHandlePages(currentTask);
                if (doc != null) {
                    doc.close();
                    doc = null;
                }
            }
            currentParameters.startPage = 0;
        } catch (InvalidPasswordException e) {
            return message("PasswordIncorrect");
        } catch (Exception e) {
            showLogs(e.toString());
            return message("Failed");
        }
        updateInterface("CompleteFile");
        showLogs(MessageFormat.format(message("HandlePagesGenerateNumber"),
                pageIndex - currentParameters.fromPage, generated));
        return message("Successful");
    }

    public boolean preHandlePages(FxTask currentTask) {
        return true;
    }

    public void postHandlePages(FxTask currentTask) {

    }

    public int handleCurrentPage(FxTask currentTask) {
        return 0;
    }

}
