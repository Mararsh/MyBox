package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * @Author Mara
 * @CreateDate 2018-7-4
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfExtractTextsBatchController extends BaseBatchPdfController {

    protected String separator;
    protected FileWriter fileWriter;
    protected PDFTextStripper stripper;
    protected File tmpFile;

    @FXML
    protected CheckBox separatorCheck;
    @FXML
    protected TextField separatorInput;

    public PdfExtractTextsBatchController() {
        baseTitle = Languages.message("PdfExtractTextsBatch");
        browseTargets = true;
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.PDF, VisitHistory.FileType.Text);
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            if (!super.makeMoreParameters()) {
                return false;
            }
            separator = separatorInput.getText();
            if (!separatorCheck.isSelected() || separator == null || separator.isEmpty()) {
                separator = null;
            }
            stripper = new PDFTextStripper();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            stripper = null;
            return false;
        }
    }

    @Override
    public boolean preHandlePages() {
        try {
            File tFile = makeTargetFile(FileNameTools.prefix(currentParameters.currentSourceFile.getName()),
                    ".txt", currentParameters.currentTargetPath);
            currentTargetFile = tFile.getAbsolutePath();
            tmpFile = FileTmpTools.getTempFile();
            fileWriter = new FileWriter(tmpFile, Charset.forName("utf-8"), false);
        } catch (Exception e) {
            MyBoxLog.error(e);
            fileWriter = null;
        }
        return fileWriter != null;
    }

    @Override
    public int handleCurrentPage() {
        int len = 0;
        try {
            stripper.setStartPage(currentParameters.currentPage);  // 1-based
            stripper.setEndPage(currentParameters.currentPage);
            String text = stripper.getText(doc);
            if (text != null && !text.trim().isEmpty()) {
                fileWriter.write(text);
                if (separator != null) {
                    String s = separator.replace("<Page Number>", currentParameters.currentPage + " ");
                    s = s.replace("<Total Number>", doc.getNumberOfPages() + "");
                    fileWriter.write(s);
                    fileWriter.write(System.getProperty("line.separator"));
                }
                fileWriter.flush();
                len += text.length();
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return len;
    }

    @Override
    public void postHandlePages() {
        try {
            if (fileWriter != null) {
                fileWriter.close();
                File tFile = new File(currentTargetFile);
                if (FileTools.rename(tmpFile, tFile)) {
                    targetFileGenerated(tFile);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        fileWriter = null;
    }

    @FXML
    public void ocr() {
        PdfOcrBatchController controller = (PdfOcrBatchController) openStage(Fxmls.PdfOCRBatchFxml);
        if (!tableData.isEmpty()) {
            controller.tableData.addAll(tableData);
        }
    }

}
