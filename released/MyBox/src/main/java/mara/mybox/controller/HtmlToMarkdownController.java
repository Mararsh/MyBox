package mara.mybox.controller;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.File;
import java.nio.charset.Charset;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-10-29
 * @License Apache License Version 2.0
 */
public class HtmlToMarkdownController extends BaseBatchFileController {

    protected FlexmarkHtmlConverter mdConverter;

    public HtmlToMarkdownController() {
        baseTitle = Languages.message("HtmlToMarkdown");
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
            MyBoxLog.error(e.toString());
            return false;
        }

        return super.makeMoreParameters();
    }

    @Override
    public boolean matchType(File file) {
        String suffix = FileNameTools.getFileSuffix(file.getName());
        if (suffix == null) {
            return false;
        }
        suffix = suffix.trim().toLowerCase();
        return "html".equals(suffix) || "htm".equals(suffix);
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
            targetFileGenerated(target);
            return Languages.message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return Languages.message("Failed");
        }
    }

    @Override
    public File makeTargetFile(File sourceFile, File targetPath) {
        try {
            String namePrefix = FileNameTools.getFilePrefix(sourceFile.getName());
            String nameSuffix = "";
            if (sourceFile.isFile()) {
                nameSuffix = ".md";
            }
            return makeTargetFile(namePrefix, nameSuffix, targetPath);
        } catch (Exception e) {
            return null;
        }
    }

}
