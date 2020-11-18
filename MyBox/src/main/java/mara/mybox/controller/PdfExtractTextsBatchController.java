package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonValues;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * @Author Mara
 * @CreateDate 2018-7-4
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfExtractTextsBatchController extends PdfBatchController {

    protected String separator;
    protected FileWriter fileWriter;
    protected PDFTextStripper stripper;
    protected File tmpFile;

    @FXML
    protected CheckBox separatorCheck;
    @FXML
    protected TextField separatorInput;

    public PdfExtractTextsBatchController() {
        baseTitle = AppVariables.message("PdfExtractTextsBatch");
        browseTargets = true;
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
            logger.error(e.toString());
            stripper = null;
            return false;
        }
    }

    @Override
    public boolean preHandlePages() {
        try {
            File tFile = makeTargetFile(FileTools.getFilePrefix(currentParameters.currentSourceFile.getName()),
                    ".txt", currentParameters.currentTargetPath);
            currentTargetFile = tFile.getAbsolutePath();
            tmpFile = FileTools.getTempFile();
            fileWriter = new FileWriter(tmpFile, Charset.forName("utf-8"), false);
        } catch (Exception e) {
            logger.error(e.toString());
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
            logger.error(e.toString());
        }
        return len;
    }

    @Override
    public void postHandlePages() {
        try {
            if (fileWriter != null) {
                fileWriter.close();
                File tFile = new File(currentTargetFile);
                if (tFile.exists()) {
                    tFile.delete();
                }
                tmpFile.renameTo(tFile);
                targetFileGenerated(tFile);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        fileWriter = null;
    }

    @FXML
    public void ocr() {
        PdfOcrBatchController controller = (PdfOcrBatchController) openStage(CommonValues.PdfOCRBatchFxml);
        if (!tableData.isEmpty()) {
            controller.tableData.addAll(tableData);
        }
    }

}
