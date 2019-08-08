package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import mara.mybox.controller.base.PdfBatchController;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
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

    @FXML
    protected CheckBox separatorCheck;
    @FXML
    protected TextField separatorInput;

    public PdfExtractTextsBatchController() {
        baseTitle = AppVaribles.message("PdfExtractTextsBatch");
        browseTargets = true;
    }

    @Override
    public boolean makeBatchParameters() {
        try {
            super.makeBatchParameters();
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
            if (tFile.exists()) {
                tFile.delete();
            }
            fileWriter = new FileWriter(currentTargetFile, false);
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
            stripper.setStartPage(currentParameters.currentPage);
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
                currentParameters.finalTargetName = currentTargetFile;
                targetFiles.add(new File(currentTargetFile));
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        fileWriter = null;
    }

}
