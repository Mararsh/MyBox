package mara.mybox.data2d.writer;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.PdfViewController;
import mara.mybox.data.PaginatedPdfTable;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class PdfWriter extends Data2DWriter {

    protected PaginatedPdfTable pdfTable;
    protected List<List<String>> pageRows;

    public PdfWriter() {
        fileSuffix = "pdf";
        pdfTable = PaginatedPdfTable.create();
    }

    @Override
    public boolean openWriter() {
        try {
            targetFile = makeTargetFile();
            if (targetFile == null) {
                showInfo((skip ? message("Skipped") : message("Failed")) + ": " + fileSuffix);
                return false;
            }
            showInfo(message("Writing") + " " + targetFile.getAbsolutePath());
            tmpFile = FileTmpTools.getTempFile();
            pdfTable.setColumns(headerNames).createDoc(tmpFile);
            return true;
        } catch (Exception e) {
            handleError(e.toString());
            return false;
        }
    }

    @Override
    public void printTargetRow() {
        try {
            if (targetRow == null) {
                return;
            }
            if (pageRows == null) {
                pageRows = new ArrayList<>();
            }
            if (pageRows.size() >= pdfTable.getRowsPerPage()) {
                pdfTable.writePage(pageRows);
                pageRows = new ArrayList<>();
            }
            pageRows.add(targetRow);
        } catch (Exception e) {
            handleError(e.toString());
        }
    }

    @Override
    public void closeWriter() {
        try {
            created = false;
            if (pdfTable == null) {
                return;
            }
            if (isFailed() || tmpFile == null || !tmpFile.exists()) {
                pdfTable.closeDoc();
                pdfTable = null;
                pageRows = null;
                FileDeleteTools.delete(tmpFile);
                return;
            }
            if (pageRows != null && !pageRows.isEmpty()) {
                pdfTable.writePage(pageRows);
                pageRows = null;
            }
            pdfTable.closeDoc();
            if (!FileTools.override(tmpFile, targetFile)) {
                FileDeleteTools.delete(tmpFile);
                return;
            }
            if (targetFile == null || targetFile.exists()) {
                return;
            }
            if (recordTargetFile && taskController != null) {
                taskController.targetFileGenerated(targetFile, VisitHistory.FileType.PDF);
            }
            pdfTable = null;
            created = true;
        } catch (Exception e) {
            handleError(e.toString());
        }
    }

    @Override
    public void openFile(BaseController controller) {
        if (targetFile == null) {
            return;
        }
        PdfViewController.open(targetFile);
    }

    /*
        get/set
     */
    public PaginatedPdfTable getPdfTable() {
        return pdfTable;
    }

    public PdfWriter setPdfTable(PaginatedPdfTable pdfTable) {
        this.pdfTable = pdfTable;
        return this;
    }

}
