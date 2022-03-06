package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-10-17
 * @License Apache License Version 2.0
 */
public class HtmlToPdfController extends BaseBatchFileController {

    @FXML
    protected ControlHtml2PdfOptions optionsController;

    public HtmlToPdfController() {
        baseTitle = Languages.message("HtmlToPdf");
        targetFileSuffix = "pdf";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html, VisitHistory.FileType.PDF);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            optionsController.setControls(baseName, true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean matchType(File file) {
        String suffix = FileNameTools.suffix(file.getName());
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
                return Languages.message("Skip");
            }
            String html = TextFileTools.readTexts(srcFile);
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
