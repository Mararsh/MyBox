package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-10-17
 * @License Apache License Version 2.0
 */
public class HtmlToTextController extends BaseBatchFileController {

    public HtmlToTextController() {
        baseTitle = Languages.message("HtmlToText");
        targetFileSuffix = "txt";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html, VisitHistory.FileType.Text);
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
            html = HtmlWriteTools.htmlToText(html);
            if (html == null) {
                return message("Failed");
            }
            TextFileTools.writeFile(target, html, Charset.forName("utf-8"));
            targetFileGenerated(target);
            return Languages.message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e);
            return Languages.message("Failed");
        }
    }

}
