package mara.mybox.data2d.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.Charset;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataFileTextWriter extends Data2DWriter {

    protected BufferedWriter fileWriter;
    protected Charset charset;
    protected String delimiter;

    public DataFileTextWriter() {
        fileSuffix = "txt";
        charset = Charset.forName("utf-8");
        delimiter = ",";
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
            if (fileWriter == null) {
                fileWriter = new BufferedWriter(new FileWriter(tmpFile, charset));
            }
            if (writeHeader) {
                TextFileTools.writeLine(task(), fileWriter, headerNames, delimiter);
            }
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
            TextFileTools.writeLine(task(), fileWriter, targetRow, delimiter);
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
            fileWriter.flush();
            fileWriter.close();
            fileWriter = null;
            if (isFailed() || tmpFile == null || !tmpFile.exists()
                    || !FileTools.override(tmpFile, targetFile)) {
                FileDeleteTools.delete(tmpFile);
                return;
            }
            if (targetFile == null || targetFile.exists()) {
                return;
            }
            if (recordTargetFile && taskController != null) {
                taskController.targetFileGenerated(targetFile, VisitHistory.FileType.Text);
            }
            if (recordTargetData) {
                if (targetData == null) {
                    targetData = Data2D.create(Data2DDefinition.DataType.Texts);
                }
                targetData.setTask(task()).setFile(targetFile)
                        .setCharset(charset)
                        .setDelimiter(delimiter)
                        .setHasHeader(writeHeader)
                        .setDataName(dataName)
                        .setColsNumber(columns.size())
                        .setRowsNumber(targetRowIndex);
                Data2D.saveAttributes(conn, targetData, columns);
            }
            created = true;
        } catch (Exception e) {
            handleError(e.toString());
        }
    }

    /*
        get/set
     */
    public Charset getCharset() {
        return charset;
    }

    public DataFileTextWriter setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public DataFileTextWriter setDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

}
