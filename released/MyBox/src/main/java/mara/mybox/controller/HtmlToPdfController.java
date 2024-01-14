package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-10-17
 * @License Apache License Version 2.0
 */
public class HtmlToPdfController extends BaseBatchFileController {

    @FXML
    protected ControlHtml2PdfOptions optionsController;

    public HtmlToPdfController() {
        baseTitle = message("HtmlToPdf");
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
            MyBoxLog.error(e);
        }
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return message("Skip");
            }
            String html = TextFileTools.readTexts(currentTask, srcFile);
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Canceled");
            }
            if (html == null) {
                return message("Failed");
            }
            String result = optionsController.html2pdf(currentTask, html, target);
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Canceled");
            }
            if (message("Successful").equals(result)) {
                targetFileGenerated(target);
            }
            return result;
        } catch (Exception e) {
            updateLogs(e.toString());
            return e.toString();
        }
    }

}
