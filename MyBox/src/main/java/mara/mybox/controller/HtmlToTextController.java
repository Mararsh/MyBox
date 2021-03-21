package mara.mybox.controller;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.TextCollectingVisitor;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.File;
import java.nio.charset.Charset;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2020-10-17
 * @License Apache License Version 2.0
 */
public class HtmlToTextController extends BaseBatchFileController {

    protected FlexmarkHtmlConverter mdConverter;
    protected MutableDataSet parserOptions;
    protected MutableDataSet textOptions;
    protected Parser textParser;
    protected TextCollectingVisitor textCollectingVisitor;

    public HtmlToTextController() {
        baseTitle = AppVariables.message("HtmlToText");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html, VisitHistory.FileType.Text);
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            mdConverter = FlexmarkHtmlConverter.builder(new MutableDataSet()).build();

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
        return "html".equals(suffix) || "htm".equals(suffix);
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVariables.message("Skip");
            }

            String html = FileTools.readTexts(srcFile);
            String md = mdConverter.convert(html);
            Node document = textParser.parse(md);
            String text = textCollectingVisitor.collectAndGetText(document);

            FileTools.writeFile(target, text, Charset.forName("utf-8"));
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
