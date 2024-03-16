package mara.mybox.data2d.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.Charset;
import mara.mybox.controller.XmlEditorController;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class XmlWriter extends Data2DWriter {

    protected BufferedWriter fileWriter;

    public XmlWriter() {
        fileSuffix = "xml";
    }

    @Override
    public boolean openWriter() {
        try {
            if (!super.openWriter()) {
                return false;
            }
            if (targetFile == null) {
                showInfo(message("InvalidParameter") + ": " + message("TargetFile"));
                return false;
            }
            showInfo(message("Writing") + " " + targetFile.getAbsolutePath());
            tmpFile = FileTmpTools.getTempFile();
            fileWriter = new BufferedWriter(new FileWriter(tmpFile, Charset.forName("UTF-8")));
            StringBuilder s = new StringBuilder();
            s.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                    .append("<Data>\n");
            fileWriter.write(s.toString());
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    @Override
    public void printTargetRow() {
        try {
            if (targetRow == null) {
                return;
            }
            StringBuilder s = new StringBuilder();
            s.append(indent).append("<Row>").append("\n");
            for (int i = 0; i < headerNames.size(); i++) {
                String value = targetRow.get(i);
                if (value == null || value.isBlank()) {
                    continue;
                }
                s.append(indent).append(indent)
                        .append("<Col name=\"").append(headerNames.get(i)).append("\" >")
                        .append("<![CDATA[").append(value).append("]]>")
                        .append("</Col>").append("\n");
            }
            s.append(indent).append("</Row>").append("\n");
            fileWriter.write(s.toString());
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public void closeWriter() {
        try {
            created = false;
            if (fileWriter == null || targetFile == null) {
                return;
            }
            if (isFailed() || tmpFile == null || !tmpFile.exists()) {
                fileWriter.close();
                fileWriter = null;
                FileDeleteTools.delete(tmpFile);
                return;
            }
            fileWriter.write("</Data>\n");
            fileWriter.flush();
            fileWriter.close();
            fileWriter = null;
            if (!FileTools.override(tmpFile, targetFile)) {
                FileDeleteTools.delete(tmpFile);
                return;
            }
            if (targetFile == null || !targetFile.exists()) {
                return;
            }
            recordFileGenerated(targetFile, VisitHistory.FileType.XML);
            created = true;
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public void showResult() {
        if (targetFile == null) {
            return;
        }
        XmlEditorController.open(targetFile);
    }

}
