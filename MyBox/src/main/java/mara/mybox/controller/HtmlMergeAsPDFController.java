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
import mara.mybox.data.VisitHistory;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

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
        baseTitle = AppVariables.message("HtmlMergeAsPDF");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            targetFileController.label(message("TargetFile"))
                    .isDirectory(false).isSource(false).mustExist(false).permitNull(false)
                    .defaultValue("_" + message("Merge"))
                    .name(baseName + "TargetFile", false).type(VisitHistory.FileType.PDF);

            targetFileInput = targetFileController.fileInput;

            mdConverter = FlexmarkHtmlConverter.builder(new MutableDataSet()).build();

        } catch (Exception e) {
            logger.error(e.toString());
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
                    = "<html>\n"
                    + "    <head>\n"
                    + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n"
                    + "    </head>\n"
                    + "    <body>\n";
            mergedHtml.append(head);
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
        return super.makeMoreParameters();
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            countHandling(srcFile);
            String html = FileTools.readTexts(srcFile);
            String body = HtmlTools.body(html);
            mergedHtml.append(body);
            return AppVariables.message("Successful");
        } catch (Exception e) {
            logger.error(e.toString());
            return AppVariables.message("Failed");
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
                        source.getFile().delete();
                        tableData.remove(i);
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
