package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data.FileInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-10-21
 * @License Apache License Version 2.0
 */
public class HtmlMergeAsPDFController extends BaseBatchFileController {

    protected StringBuilder mergedHtml;

    @FXML
    protected ControlHtml2PdfOptions optionsController;
    @FXML
    protected CheckBox deleteCheck;

    public HtmlMergeAsPDFController() {
        baseTitle = message("HtmlMergeAsPDF");
        targetFileSuffix = "pdf";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html, VisitHistory.FileType.PDF);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            optionsController.setControls(baseName, true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            if (targetFileController != null) {
                targetFile = targetFileController.file;
            }
            if (targetFile == null) {
                return false;
            }
            targetFile = makeTargetFile(targetFile, targetFile.getParentFile());
            if (targetFile == null) {
                return false;
            }
            mergedHtml = new StringBuilder();
            String head
                    = "<!DOCTYPE html><html>\n"
                    + "    <head>\n"
                    + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n"
                    + "    </head>\n"
                    + "    <body>\n";
            mergedHtml.append(head);
            return super.makeMoreParameters();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            String body = HtmlReadTools.body(TextFileTools.readTexts(srcFile), false);
            mergedHtml.append(body);
            return message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return message("Failed");
        }
    }

    @Override
    public void afterHandleFiles() {
        try {
            mergedHtml.append("    </body>\n</html>\n");
            String result = optionsController.html2pdf(mergedHtml.toString(), targetFile);
            if (!message("Successful").equals(result)) {
                updateLogs(result, true, true);
                return;
            }
            targetFileGenerated(targetFile);
            if (deleteCheck.isSelected()) {
                List<FileInformation> sources = new ArrayList<>();
                sources.addAll(tableData);
                for (int i = sources.size() - 1; i >= 0; --i) {
                    try {
                        FileInformation source = sources.get(i);
                        FileDeleteTools.delete(source.getFile());
                        tableData.remove(i);
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
            updateLogs(e.toString(), true, true);
        }
    }

}
