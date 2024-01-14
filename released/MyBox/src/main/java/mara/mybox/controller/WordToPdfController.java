package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.MicrosoftDocumentTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-10
 * @License Apache License Version 2.0
 */
public class WordToPdfController extends BaseBatchFileController {

    protected Charset charset;

    @FXML
    protected ControlHtml2PdfOptions optionsController;

    public WordToPdfController() {
        baseTitle = Languages.message("WordToPdf");
        targetFileSuffix = "pdf";
        charset = Charset.forName("UTf-8");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.WordS, VisitHistory.FileType.PDF);
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
            String html = MicrosoftDocumentTools.word2Html(currentTask, srcFile, charset);
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Canceled");
            }
            if (html == null) {
                return message("Failed");
            }
            String result = optionsController.html2pdf(currentTask, html, target);
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
