package mara.mybox.data2d.writer;

import java.util.ArrayList;
import java.util.List;
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
    }

    @Override
    public boolean openWriter() {
        try {
            if (!super.openWriter()) {
                return false;
            }
            if (printFile == null) {
                showInfo(message("InvalidParameter") + ": " + message("TargetFile"));
                return false;
            }
            showInfo(message("Writing") + " " + printFile.getAbsolutePath());
            tmpFile = FileTmpTools.getTempFile(".pdf");
            if (pdfTable == null) {
                pdfTable = PaginatedPdfTable.create();
            }
            pdfTable.setColumns(headerNames).createDoc(tmpFile);
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    @Override
    public void printTargetRow() {
        try {
            if (printRow == null) {
                return;
            }
            if (pageRows == null) {
                pageRows = new ArrayList<>();
            }
            if (pageRows.size() >= pdfTable.getRowsPerPage()) {
                pdfTable.writePage(pageRows);
                pageRows = new ArrayList<>();
            }
            pageRows.add(printRow);
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public void closeWriter() {
        try {
            created = false;
            if (pdfTable == null) {
                showInfo(message("Failed") + ": " + printFile);
                return;
            }
            if (pageRows != null && !pageRows.isEmpty()) {
                pdfTable.writePage(pageRows);
            }
            pageRows = null;
            pdfTable.closeDoc();
            pdfTable = null;
            if (isFailed() || tmpFile == null || !tmpFile.exists()
                    || !FileTools.override(tmpFile, printFile)) {
                FileDeleteTools.delete(tmpFile);
                showInfo(message("Failed") + ": " + printFile);
                return;
            }
            if (printFile == null || !printFile.exists()) {
                showInfo(message("Failed") + ": " + printFile);
                return;
            }
            recordFileGenerated(printFile, VisitHistory.FileType.PDF);
            created = true;
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public boolean showResult() {
        if (printFile == null || !printFile.exists()) {
            return false;
        }
        PdfViewController.open(printFile);
        return true;
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
