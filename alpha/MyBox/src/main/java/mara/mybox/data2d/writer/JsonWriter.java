package mara.mybox.data2d.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.Charset;
import mara.mybox.controller.BaseController;
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

    public JsonWriter() {
        fileSuffix = "json";
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
            fileWriter = new BufferedWriter(new FileWriter(tmpFile, Charset.forName("UTF-8")));
            StringBuilder s = new StringBuilder();
            s.append("{\"Data\": [\n");
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
            StringBuilder s = new StringBuilder();
            if (isFirstRow) {
                isFirstRow = false;
            } else {
                s.append(",\n");
            }
            s.append(indent).append("{").append("\n");
            boolean firstData = true;
            for (int i = 0; i < headerNames.size(); i++) {
                String value = targetRow.get(i);
                if (value == null) {
                    continue;
                }
                if (!firstData) {
                    s.append(",\n");
                } else {
                    firstData = false;
                }
                s.append(indent).append(indent)
                        .append("\"").append(headerNames.get(i)).append("\": ")
                        .append(JsonTools.encode(value));
            }
            s.append(indent).append("\n").append(indent).append("}");
            fileWriter.write(s.toString());
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
            fileWriter.write("\n]}\n");
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
                taskController.targetFileGenerated(targetFile, VisitHistory.FileType.JSON);
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
        JsonEditorController.open(targetFile);
    }

}
