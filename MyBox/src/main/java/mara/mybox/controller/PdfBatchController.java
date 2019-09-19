package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import javafx.fxml.FXML;
import mara.mybox.data.PdfInformation;
import mara.mybox.data.VisitHistory;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonImageValues;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class PdfBatchController extends BatchController<PdfInformation> {

    protected PDFsTableController pdfsTableController;
    protected String password, currentTargetFile;
    protected int fromPage, toPage, startPage;
    protected PDDocument doc;
    protected boolean needUserPassword, needOwnerPassword;

    public PdfBatchController() {
        SourceFileType = VisitHistory.FileType.PDF;
        SourcePathType = VisitHistory.FileType.PDF;
        TargetPathType = VisitHistory.FileType.PDF;
        TargetFileType = VisitHistory.FileType.PDF;
        AddFileType = VisitHistory.FileType.PDF;
        AddPathType = VisitHistory.FileType.PDF;

        sourcePathKey = "PdfFilePath";
        targetPathKey = "PdfFilePath";

        needUserPassword = true;
        needOwnerPassword = false;

        targetSubdirKey = "PdfCreatSubdir";
        previewKey = "PdfPreview";

        sourceExtensionFilter = CommonImageValues.PdfExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();
            allowPaused = true;
            pdfsTableController = (PDFsTableController) tableController;

            if (!needUserPassword) {
                tableView.getColumns().remove(pdfsTableController.getUserPasswordColumn());
            }
            if (!needOwnerPassword) {
                tableView.getColumns().remove(pdfsTableController.getOwnerPasswordColumn());
            }

        } catch (Exception e) {
            logger.error(e.toString());
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

        previewParameters = copyParameters(actualParameters);
        int page = 0;
        if (previewInput != null) {
            try {
                page = Integer.parseInt(previewInput.getText()) - 1;
            } catch (Exception e) {
            }
        }
        if (page <= 0) {
            page = tableData.get(0).getFromPage();
            if (previewInput != null) {
                previewInput.setText(page + "");
            }
        }
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
    public String handleFile(File srcFile, File targetPath) {
        int generated = 0;
        doc = null;
        if (PdfTools.isPDF(srcFile)) {
            try {
                currentParameters.currentSourceFile = srcFile;
                if (!isPreview) {
                    PdfInformation info = tableData.get(currentParameters.currentIndex);
                    actualParameters.fromPage = info.getFromPage();
                    if (actualParameters.fromPage <= 0) {
                        actualParameters.fromPage = 1;
                    }
                    actualParameters.toPage = info.getToPage();
                    actualParameters.password = info.getUserPassword();
                    actualParameters.startPage = actualParameters.fromPage;
                    actualParameters.currentPage = actualParameters.fromPage;
                }

                try (PDDocument pd = PDDocument.load(currentParameters.currentSourceFile,
                        currentParameters.password, AppVariables.pdfMemUsage)) {
                    doc = pd;
                    if (currentParameters.toPage <= 0 || currentParameters.toPage > doc.getNumberOfPages()) {
                        currentParameters.toPage = doc.getNumberOfPages();
                    }

                    currentParameters.currentTargetPath = targetPath;
                    if (currentParameters.targetSubDir) {
                        currentParameters.currentTargetPath = new File(targetPath.getAbsolutePath() + "/"
                                + FileTools.getFilePrefix(currentParameters.currentSourceFile.getName()));
                        if (!currentParameters.currentTargetPath.exists()) {
                            currentParameters.currentTargetPath.mkdirs();
                        }
                    }
                    if (preHandlePages()) {
                        int total = currentParameters.toPage - currentParameters.fromPage + 1;
                        updateFileProgress(0, total);
                        for (currentParameters.currentPage = currentParameters.startPage;
                                currentParameters.currentPage <= currentParameters.toPage; currentParameters.currentPage++) {
                            if (task.isCancelled()) {
                                break;
                            }

                            generated += handleCurrentPage();

                            updateFileProgress(currentParameters.currentPage - currentParameters.fromPage + 1, total);

                            currentParameters.currentTotalHandled++;
                        }
                    }
                    postHandlePages();
                    doc.close();
                }
                currentParameters.startPage = 1;
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }
        updateInterface("CompleteFile");
        return MessageFormat.format(AppVariables.message("HandlePagesGenerateNumber"),
                currentParameters.currentPage - currentParameters.fromPage, generated);
    }

    public boolean preHandlePages() {
        return true;
    }

    public void postHandlePages() {

    }

    public int handleCurrentPage() {
//        currentParameters.finalTargetName = target.getAbsolutePath();
//        targetFiles.add(target);
        return 0;
    }

}
