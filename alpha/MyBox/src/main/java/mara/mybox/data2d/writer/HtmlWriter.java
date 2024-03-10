package mara.mybox.data2d.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.Charset;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.WebBrowserController;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class HtmlWriter extends Data2DWriter {

    protected BufferedWriter fileWriter;
    protected String css;

    public HtmlWriter() {
        fileSuffix = "htm";
        css = HtmlStyles.TableStyle;
    }

    @Override
    public boolean openWriter() {
        try {
            targetFile = makeTargetFile();
            if (targetFile == null) {
                showInfo((skip ? message("Skipped") : message("Failed")) + ": " + fileSuffix);
                return false;
            }
            showInfo(message("Writing") + " " + targetFile.getAbsolutePath());
            tmpFile = FileTmpTools.getTempFile();
            fileWriter = new BufferedWriter(new FileWriter(tmpFile, Charset.forName("utf-8")));
            StringBuilder s = new StringBuilder();
            s.append("<!DOCTYPE html><HTML>\n").
                    append(indent).append("<HEAD>\n").
                    append(indent).append(indent).
                    append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n");
            if (css != null && !css.isBlank()) {
                s.append(indent).append(indent).append("<style type=\"text/css\">\n");
                s.append(indent).append(indent).append(indent).append(css).append("\n");
                s.append(indent).append(indent).append("</style>\n");
            }
            s.append(indent).append("</HEAD>\n").append(indent).append("<BODY>\n");
            s.append(StringTable.tablePrefix(new StringTable(headerNames)));
            fileWriter.write(s.toString());
            return true;
        } catch (Exception e) {
            handleError(e.toString());
            return false;
        }
    }

    @Override
    public void printTargetRow() {
        try {
            if (targetRow == null) {
                return;
            }
            fileWriter.write(StringTable.tableRow(targetRow));
        } catch (Exception e) {
            handleError(e.toString());
        }
    }

    @Override
    public void closeWriter() {
        try {
            created = false;
            if (fileWriter == null) {
                return;
            }
            if (isFailed() || tmpFile == null || !tmpFile.exists()) {
                fileWriter.close();
                fileWriter = null;
                FileDeleteTools.delete(tmpFile);
                return;
            }
            fileWriter.write(StringTable.tableSuffix(new StringTable(headerNames)));
            fileWriter.write(indent + "<BODY>\n</HTML>\n");
            fileWriter.flush();
            fileWriter.close();
            fileWriter = null;
            if (!FileTools.override(tmpFile, targetFile)) {
                FileDeleteTools.delete(tmpFile);
                return;
            }
            if (targetFile == null || targetFile.exists()) {
                return;
            }
            if (recordTargetFile && taskController != null) {
                taskController.targetFileGenerated(targetFile, VisitHistory.FileType.Html);
            }
            created = true;
        } catch (Exception e) {
            handleError(e.toString());
        }
    }

    @Override
    public void openFile(BaseController controller) {
        if (targetFile == null) {
            return;
        }
        WebBrowserController.openFile(targetFile);
    }

    /*
        get/set
     */
    public String getCss() {
        return css;
    }

    public HtmlWriter setCss(String css) {
        this.css = css;
        return this;
    }

}
