package mara.mybox.controller;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.File;
import java.nio.charset.Charset;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-10-29
 * @License Apache License Version 2.0
 */
public class HtmlToMarkdownController extends BaseBatchFileController {

    protected FlexmarkHtmlConverter mdConverter;

    public HtmlToMarkdownController() {
        baseTitle = Languages.message("HtmlToMarkdown");
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
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return Languages.message("Skip");
            }

            String html = TextFileTools.readTexts(srcFile);
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
            return Languages.message("Failed");
        }
    }

}
