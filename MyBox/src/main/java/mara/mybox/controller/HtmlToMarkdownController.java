package mara.mybox.controller;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.File;
import java.nio.charset.Charset;
import mara.mybox.data.VisitHistory;
import mara.mybox.tools.FileTools;
import mara.mybox.data.tools.VisitHistoryTools;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2019-10-29
 * @License Apache License Version 2.0
 */
public class HtmlToMarkdownController extends FilesBatchController {

    protected FlexmarkHtmlConverter mdConverter;

    public HtmlToMarkdownController() {
        baseTitle = AppVariables.message("HtmlToMarkdown");

        SourceFileType = VisitHistory.FileType.Html;
        SourcePathType = VisitHistory.FileType.Html;
        TargetPathType = VisitHistory.FileType.Markdown;
        TargetFileType = VisitHistory.FileType.Markdown;
        AddFileType = VisitHistory.FileType.Html;
        AddPathType = VisitHistory.FileType.Html;

        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Html);
        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Markdown);

        sourceExtensionFilter = CommonFxValues.HtmlExtensionFilter;
        targetExtensionFilter = CommonFxValues.MarkdownExtensionFilter;
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
        String suffix = FileTools.getFileSuffix(file.getName());
        if (suffix == null) {
            return false;
        }
        suffix = suffix.trim().toLowerCase();
        return "html".equals(suffix) || "htm".equals(suffix);
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            countHandling(srcFile);
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVariables.message("Skip");
            }

            String html = FileTools.readTexts(srcFile);
            String md = mdConverter.convert(html);
            FileTools.writeFile(target, md, Charset.forName("utf-8"));
            targetFileGenerated(target);
            return AppVariables.message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return AppVariables.message("Failed");
        }
    }

    @Override
    public File makeTargetFile(File sourceFile, File targetPath) {
        try {
            String namePrefix = FileTools.getFilePrefix(sourceFile.getName());
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
