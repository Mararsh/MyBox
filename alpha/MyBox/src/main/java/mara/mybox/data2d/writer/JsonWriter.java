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
            if (printFile == null) {
                showInfo(message("InvalidParameter") + ": " + message("TargetFile"));
                return false;
            }
            showInfo(message("Writing") + " " + printFile.getAbsolutePath());
            tmpFile = FileTmpTools.getTempFile(".json");
            fileWriter = new BufferedWriter(new FileWriter(tmpFile, Charset.forName("UTF-8")));
            StringBuilder s = new StringBuilder();
            s.append("{\"Data\": [\n");
            fileWriter.write(s.toString());
            isFirstRow = true;
            status = Status.Openned;
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    @Override
    public void printTargetRow() {
        try {
            if (printRow == null) {
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
                value = printRow.get(i);
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
    public void finishWork() {
        try {
            if (fileWriter == null || printFile == null) {
                showInfo(message("Failed") + ": " + printFile);
                status = Status.Failed;
                return;
            }
            if (isFailed() || tmpFile == null || !tmpFile.exists()) {
                fileWriter.close();
                fileWriter = null;
                FileDeleteTools.delete(tmpFile);
                showInfo(message("Failed") + ": " + printFile);
                status = Status.Failed;
                return;
            }
            if (targetRowIndex == 0) {
                fileWriter.close();
                fileWriter = null;
                FileDeleteTools.delete(tmpFile);
                showInfo(message("NoData") + ": " + printFile);
                status = Status.NoData;
                return;
            }
            fileWriter.write("\n]}\n");
            fileWriter.flush();
            fileWriter.close();
            fileWriter = null;
            if (!FileTools.override(tmpFile, printFile)) {
                FileDeleteTools.delete(tmpFile);
                showInfo(message("Failed") + ": " + printFile);
                status = Status.NoData;
                return;
            }
            if (printFile == null || !printFile.exists()) {
                showInfo(message("Failed") + ": " + printFile);
                status = Status.NoData;
                return;
            }
            recordFileGenerated(printFile, VisitHistory.FileType.JSON);
            status = Status.Created;
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public boolean showResult() {
        if (printFile == null || !printFile.exists()) {
            return false;
        }
        JsonEditorController.open(printFile);
        return true;
    }

}
