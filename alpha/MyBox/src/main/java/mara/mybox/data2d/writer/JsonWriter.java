package mara.mybox.data2d.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.Charset;
import mara.mybox.controller.JsonEditorController;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.JsonTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class JsonWriter extends Data2DWriter {

    protected BufferedWriter fileWriter;
    protected boolean isFirstRow, isFirstField;

    public JsonWriter() {
        fileSuffix = "json";
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
            tmpFile = FileTmpTools.getTempFile(".json");
            fileWriter = new BufferedWriter(new FileWriter(tmpFile, Charset.forName("UTF-8")));
            StringBuilder s = new StringBuilder();
            s.append("{\"Data\": [\n");
            fileWriter.write(s.toString());
            isFirstRow = true;
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
            if (isFirstRow) {
                isFirstRow = false;
            } else {
                s.append(",\n");
            }
            s.append(indent).append("{").append("\n");
            isFirstField = true;
            for (int i = 0; i < headerNames.size(); i++) {
                value = targetRow.get(i);
                if (value == null) {
                    continue;
                }
                if (isFirstField) {
                    isFirstField = false;
                } else {
                    s.append(",\n");
                }
                s.append(indent).append(indent)
                        .append("\"").append(headerNames.get(i)).append("\": ")
                        .append(JsonTools.encode(value));
            }
            s.append(indent).append("\n").append(indent).append("}");
            fileWriter.write(s.toString());
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public void closeWriter() {
        try {
            created = false;
            if (fileWriter == null) {
                showInfo(message("Failed") + ": " + targetFile);
                return;
            }
            if (isFailed() || tmpFile == null || !tmpFile.exists()) {
                fileWriter.close();
                fileWriter = null;
                FileDeleteTools.delete(tmpFile);
                showInfo(message("Failed") + ": " + targetFile);
                return;
            }
            fileWriter.write("\n]}\n");
            fileWriter.flush();
            fileWriter.close();
            fileWriter = null;
            if (!FileTools.override(tmpFile, targetFile)) {
                FileDeleteTools.delete(tmpFile);
                showInfo(message("Failed") + ": " + targetFile);
                return;
            }
            if (targetFile == null || !targetFile.exists()) {
                showInfo(message("Failed") + ": " + targetFile);
                return;
            }
            recordFileGenerated(targetFile, VisitHistory.FileType.JSON);
            created = true;
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public void showResult() {
        if (targetFile == null || !targetFile.exists()) {
            showError(message("Failed"));
            return;
        }
        JsonEditorController.open(targetFile);
    }

}
