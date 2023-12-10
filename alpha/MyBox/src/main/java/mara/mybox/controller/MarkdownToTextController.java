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
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

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
        baseTitle = Languages.message("MarkdownToText");
        targetFileSuffix = "txt";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Markdown, VisitHistory.FileType.Text);
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
            String md = TextFileTools.readTexts(task, srcFile);
            if (md == null) {
                if (task == null || !task.isWorking()) {
                    return message("Canceled");
                } else {
                    return message("Failed");
                }
            }
            Node document = textParser.parse(md);
            String text = textCollectingVisitor.collectAndGetText(document);

            TextFileTools.writeFile(target, text, Charset.forName("utf-8"));
            updateLogs(MessageFormat.format(Languages.message("ConvertSuccessfully"),
                    srcFile.getAbsolutePath(), target.getAbsolutePath()));
            targetFileGenerated(target);
            return Languages.message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e);
            return Languages.message("Failed");
        }
    }

}
