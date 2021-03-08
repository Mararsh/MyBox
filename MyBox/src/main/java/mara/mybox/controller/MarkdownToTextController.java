package mara.mybox.controller;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.TextCollectingVisitor;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2020-10-17
 * @License Apache License Version 2.0
 */
public class MarkdownToTextController extends BaseBatchFileController {

    protected Parser textParser, docxParser;
    protected MutableDataSet textOptions;
    protected TextCollectingVisitor textCollectingVisitor;

    public MarkdownToTextController() {
        baseTitle = AppVariables.message("MarkdownToText");

        SourceFileType = VisitHistory.FileType.Markdown;
        SourcePathType = VisitHistory.FileType.Markdown;
        TargetPathType = VisitHistory.FileType.Text;
        TargetFileType = VisitHistory.FileType.Text;
        AddFileType = VisitHistory.FileType.Markdown;
        AddPathType = VisitHistory.FileType.Markdown;

        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Markdown);
        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Text);

        sourceExtensionFilter = CommonFxValues.MarkdownExtensionFilter;
        targetExtensionFilter = CommonFxValues.TextExtensionFilter;
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            DataHolder textHolder = PegdownOptionsAdapter.flexmarkOptions(Extensions.ALL);
            textOptions = new MutableDataSet();
            textOptions.set(Parser.EXTENSIONS, textHolder.get(Parser.EXTENSIONS));
            textParser = Parser.builder(textOptions).build();
            textCollectingVisitor = new TextCollectingVisitor();

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
        return "md".equals(suffix);
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVariables.message("Skip");
            }
            String md = FileTools.readTexts(srcFile);
            Node document = textParser.parse(md);
            String text = textCollectingVisitor.collectAndGetText(document);

            FileTools.writeFile(target, text, Charset.forName("utf-8"));
            updateLogs(MessageFormat.format(message("ConvertSuccessfully"),
                    srcFile.getAbsolutePath(), target.getAbsolutePath()));
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
                nameSuffix = ".txt";
            }
            return makeTargetFile(namePrefix, nameSuffix, targetPath);
        } catch (Exception e) {
            return null;
        }
    }

}
