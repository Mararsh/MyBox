package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-10-17
 * @License Apache License Version 2.0
 */
public class TextToPdfController extends BaseBatchFileController {

    @FXML
    protected ControlHtml2PdfOptions optionsController;

    public TextToPdfController() {
        baseTitle = message("TextToPdf");
        targetFileSuffix = "pdf";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text, VisitHistory.FileType.PDF);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            optionsController.setControls(baseName, false);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return message("Skip");
            }
            String texts = TextFileTools.readTexts(task, srcFile);
            if (texts == null) {
                if (task == null || !task.isWorking()) {
                    return message("Canceled");
                } else {
                    return message("Failed");
                }
            }
            String html = HtmlWriteTools.textToHtml(texts);
            String result = optionsController.html2pdf(task, html, target);
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
