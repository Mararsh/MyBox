package mara.mybox.controller;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import java.io.File;
import java.nio.charset.Charset;
import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-6-8
 * @License Apache License Version 2.0
 */
public class MarkdownTypesettingController extends BaseBatchFileController {

    protected MutableDataHolder htmlOptions;
    protected Parser htmlParser;
    protected HtmlRenderer htmlRenderer;
    protected FlexmarkHtmlConverter mdConverter;

    @FXML
    protected ControlMarkdownOptions optionsController;

    public MarkdownTypesettingController() {
        baseTitle = message("MarkdownTypesetting");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Markdown);
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            htmlOptions = optionsController.options();
            htmlParser = Parser.builder(htmlOptions).build();
            htmlRenderer = HtmlRenderer.builder(htmlOptions).build();
            mdConverter = FlexmarkHtmlConverter.builder(htmlOptions).build();
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
            String md = TextFileTools.readTexts(currentTask, srcFile);
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Canceled");
            }
            if (md == null) {
                return message("Failed");
            }
            Node document = htmlParser.parse(md);
            String html = htmlRenderer.render(document);
            md = mdConverter.convert(html);
            TextFileTools.writeFile(target, md, Charset.forName("utf-8"));
            if (target.exists() && target.length() > 0) {
                targetFileGenerated(target);
                return message("Successful");
            } else {
                return message("Failed");
            }
        } catch (Exception e) {
            updateLogs(e.toString());
            return message("Failed");
        }
    }

}
