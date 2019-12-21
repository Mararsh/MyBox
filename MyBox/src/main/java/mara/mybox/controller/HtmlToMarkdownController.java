package mara.mybox.controller;

import com.ibm.icu.text.MessageFormat;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.File;
import mara.mybox.data.VisitHistory;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2019-10-29
 * @License Apache License Version 2.0
 */
public class HtmlToMarkdownController extends FilesBatchController {

    protected MutableDataSet parserOptions;

    public HtmlToMarkdownController() {
        baseTitle = AppVariables.message("HtmlToMarkdown");

        SourceFileType = VisitHistory.FileType.Html;
        SourcePathType = VisitHistory.FileType.Html;
        TargetPathType = VisitHistory.FileType.Markdown;
        TargetFileType = VisitHistory.FileType.Markdown;
        AddFileType = VisitHistory.FileType.Html;
        AddPathType = VisitHistory.FileType.Html;

        sourcePathKey = "HtmlFilePath";
        targetPathKey = "MarkdownFilePath";

        sourceExtensionFilter = CommonFxValues.HtmlExtensionFilter;
        targetExtensionFilter = CommonFxValues.MarkdownExtensionFilter;
    }

    @Override
    public boolean makeBatchParameters() {
        try {
            parserOptions = new MutableDataSet();
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }

        return super.makeBatchParameters();
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
            showHandling(srcFile);
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVariables.message("Skip");
            }

            String html = FileTools.readTexts(srcFile);
            String md = FlexmarkHtmlConverter.builder(parserOptions).build().
                    convert(html);
            FileTools.writeFile(target, md);
            updateLogs(MessageFormat.format(message("ConvertSuccessfully"),
                    srcFile.getAbsolutePath(), target.getAbsolutePath()));
            currentParameters.finalTargetName = target.getAbsolutePath();
            targetFiles.add(target);
            return AppVariables.message("Successful");
        } catch (Exception e) {
            logger.error(e.toString());
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
