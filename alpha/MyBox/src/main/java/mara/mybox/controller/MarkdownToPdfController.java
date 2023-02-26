package mara.mybox.controller;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import java.io.File;
import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.MarkdownTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-10-17
 * @License Apache License Version 2.0
 */
public class MarkdownToPdfController extends BaseBatchFileController {

    protected Parser htmlParser;
    protected HtmlRenderer htmlRender;
    protected MutableDataHolder htmlOptions;

    @FXML
    protected ControlHtml2PdfOptions optionsController;

    public MarkdownToPdfController() {
        baseTitle = Languages.message("MarkdownToPdf");
        targetFileSuffix = "pdf";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Markdown, VisitHistory.FileType.PDF);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            optionsController.setControls(baseName, false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            htmlOptions = MarkdownTools.htmlOptions();
            htmlParser = Parser.builder(htmlOptions).build();
            htmlRender = HtmlRenderer.builder(htmlOptions).build();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
        return super.makeMoreParameters();
    }

    @Override
    public boolean matchType(File file) {
        String suffix = FileNameTools.suffix(file.getName());
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
                return Languages.message("Skip");
            }
            Node document = htmlParser.parse(TextFileTools.readTexts(srcFile));
            String html = htmlRender.render(document);

            String result = optionsController.html2pdf(html, target);
            if (Languages.message("Successful").equals(result)) {
                targetFileGenerated(target);
            }
            return result;
        } catch (Exception e) {
            updateLogs(e.toString());
            return e.toString();
        }
    }

}
