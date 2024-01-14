package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-5-17
 * @License Apache License Version 2.0
 */
public class WordToHtmlController extends BaseBatchFileController {

    protected Charset charset;

    public WordToHtmlController() {
        baseTitle = message("WordToHtml");
        targetFileSuffix = "html";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.WordS, VisitHistory.FileType.Html);
        charset = Charset.defaultCharset();
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return message("Skip");
            }
            File tmpFile = MicrosoftDocumentTools.word2HtmlFile(srcFile, charset);
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Canceled");
            }
            if (!FileTools.override(tmpFile, target)) {
                return message("Failed");
            }
            targetFileGenerated(target);
            return message("Successful");
        } catch (Exception e) {
            updateLogs(e.toString());
            return e.toString();
        }
    }

}
