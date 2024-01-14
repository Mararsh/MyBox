package mara.mybox.controller;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.File;
import java.nio.charset.Charset;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-10-29
 * @License Apache License Version 2.0
 */
public class HtmlToMarkdownController extends BaseBatchFileController {

    protected FlexmarkHtmlConverter mdConverter;

    public HtmlToMarkdownController() {
        baseTitle = message("HtmlToMarkdown");
        targetFileSuffix = "md";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html, VisitHistory.FileType.Markdown);
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            mdConverter = FlexmarkHtmlConverter.builder(new MutableDataSet()).build();

        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }

        return super.makeMoreParameters();
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
            String md = mdConverter.convert(html);
            TextFileTools.writeFile(target, md, Charset.forName("utf-8"));
            if (target.exists() && target.length() > 0) {
                targetFileGenerated(target);
                return message("Successful");
            } else {
                return message("Failed");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return message("Failed");
        }
    }

}
