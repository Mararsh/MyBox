package mara.mybox.controller;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.File;
import java.util.Arrays;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-10-17
 * @License Apache License Version 2.0
 */
public class MarkdownToPdfController extends HtmlToPdfController {

    protected Parser htmlParser;
    protected HtmlRenderer htmlRender;
    protected MutableDataHolder htmlOptions;

    public MarkdownToPdfController() {
        baseTitle = Languages.message("MarkdownToPdf");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Markdown, VisitHistory.FileType.PDF);
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            htmlOptions = new MutableDataSet();
            htmlOptions.setFrom(ParserEmulationProfile.valueOf("PEGDOWN"));
            htmlOptions.set(Parser.EXTENSIONS, Arrays.asList(
                    TablesExtension.create()
            ));
            htmlOptions.set(HtmlRenderer.INDENT_SIZE, 4)
                    .set(TablesExtension.TRIM_CELL_WHITESPACE, false)
                    .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
                    .set(TablesExtension.APPEND_MISSING_COLUMNS, true);
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
        String suffix = FileNameTools.getFileSuffix(file.getName());
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

            String result = html2pdf(html, target);
            if (Languages.message("Successful").equals(result)) {
                targetFileGenerated(target);
            }
            return result;
        } catch (Exception e) {
            return e.toString();
//            MyBoxLog.error(e.toString());
//            return AppVariables.message("Failed");
        }
    }

}
