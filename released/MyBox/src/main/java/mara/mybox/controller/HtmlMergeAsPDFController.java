package mara.mybox.controller;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data.FileInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlReadTools;

import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-10-21
 * @License Apache License Version 2.0
 */
public class HtmlMergeAsPDFController extends HtmlToPdfController {

    protected FlexmarkHtmlConverter mdConverter;
    protected StringBuilder mergedHtml;

    @FXML
    protected CheckBox deleteCheck;
    @FXML
    protected ControlFileSelecter targetFileController;

    public HtmlMergeAsPDFController() {
        baseTitle = Languages.message("HtmlMergeAsPDF");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            targetFileController.label(Languages.message("TargetFile"))
                    .isDirectory(false).isSource(false).mustExist(false).permitNull(false)
                    .defaultValue("_" + Languages.message("Merge"))
                    .name(baseName + "TargetFile", false).type(VisitHistory.FileType.PDF);

            targetFileInput = targetFileController.fileInput;

            mdConverter = FlexmarkHtmlConverter.builder(new MutableDataSet()).build();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            targetFile = targetFileController.file;
            if (targetFile == null) {
                return false;
            }
            super.makeMoreParameters();

            mergedHtml = new StringBuilder();
            String head
                    = "<!DOCTYPE html><html>\n"
                    + "    <head>\n"
                    + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n"
                    + "    </head>\n"
                    + "    <body>\n";
            mergedHtml.append(head);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
        return super.makeMoreParameters();
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            String html = TextFileTools.readTexts(srcFile);
            String body = HtmlReadTools.body(html);
            mergedHtml.append(body);
            return Languages.message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return Languages.message("Failed");
        }
    }

    @Override
    public void afterHandleFiles() {
        try {
            mergedHtml.append("    </body>\n</html>\n");
            String mergedString = mergedHtml.toString();
            String css = cssArea.getText().trim();
            if (!css.isBlank()) {
                try {
                    mergedString = PdfConverterExtension.embedCss(mergedString, css);
                } catch (Exception e) {
                }
            }
            try {
                PdfConverterExtension.exportToPdf(targetFile.getAbsolutePath(), mergedString, "", pdfOptions);
            } catch (Exception e) {
            }
            targetFileGenerated(targetFile);
            if (deleteCheck.isSelected()) {
                List<FileInformation> sources = new ArrayList<>();
                sources.addAll(tableData);
                for (int i = sources.size() - 1; i >= 0; --i) {
                    try {
                        FileInformation source = sources.get(i);
                        FileDeleteTools.delete(source.getFile());
                        tableData.remove(i);
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
