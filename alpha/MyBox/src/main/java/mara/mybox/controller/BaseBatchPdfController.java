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
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
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
    protected int fromPage, toPage, startPage;
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

    @FXML
    @Override
    public boolean makePreviewParameters() {
        if (!makeActualParameters()) {
            popError(Languages.message("Invalid"));
            actualParameters = null;
            return false;
        }
        try {
            previewParameters = (ProcessParameters) actualParameters.clone();
        } catch (Exception e) {
            return false;
        }
        PdfInformation info = tableData.get(0);
        int page = info.getFromPage();
        previewParameters.password = info.getUserPassword();
        previewParameters.fromPage = page;
        previewParameters.toPage = page;
        previewParameters.startPage = page;
        previewParameters.currentPage = page;
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
            currentParameters.currentSourceFile = srcFile;
            if (!isPreview) {
                PdfInformation info = tableData.get(currentParameters.currentIndex);
                currentParameters.fromPage = info.getFromPage();
                if (currentParameters.fromPage <= 0) {
                    currentParameters.fromPage = 1;
                }
                currentParameters.toPage = info.getToPage();
                currentParameters.password = info.getUserPassword();
                currentParameters.startPage = currentParameters.fromPage;
                currentParameters.currentPage = currentParameters.fromPage;
            }
            try (PDDocument pd = PDDocument.load(currentParameters.currentSourceFile,
                    currentParameters.password, AppVariables.PdfMemUsage)) {
                doc = pd;
                if (currentParameters.toPage <= 0 || currentParameters.toPage > doc.getNumberOfPages()) {
                    currentParameters.toPage = doc.getNumberOfPages();
                }

                currentParameters.currentTargetPath = targetPath;
                if (currentParameters.targetSubDir) {
                    currentParameters.currentTargetPath = new File(targetPath.getAbsolutePath() + "/"
                            + FileNameTools.prefix(currentParameters.currentSourceFile.getName()));
                    if (!currentParameters.currentTargetPath.exists()) {
                        currentParameters.currentTargetPath.mkdirs();
                    }
                }
                if (preHandlePages(currentTask)) {
                    int total = currentParameters.toPage - currentParameters.fromPage + 1;
                    updateFileProgress(0, total);
                    for (currentParameters.currentPage = currentParameters.startPage;
                            currentParameters.currentPage <= currentParameters.toPage; currentParameters.currentPage++) {
                        if (currentTask == null || currentTask.isCancelled()) {
                            break;
                        }

                        generated += handleCurrentPage(currentTask);

                        updateFileProgress(currentParameters.currentPage - currentParameters.fromPage + 1, total);
                    }
                }
                postHandlePages(currentTask);
                doc.close();
            }
            currentParameters.startPage = 1;
        } catch (InvalidPasswordException e) {
            return Languages.message("PasswordIncorrect");
        } catch (Exception e) {
            return e.toString();
        }
        updateInterface("CompleteFile");
        return MessageFormat.format(Languages.message("HandlePagesGenerateNumber"),
                currentParameters.currentPage - currentParameters.fromPage, generated);
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
