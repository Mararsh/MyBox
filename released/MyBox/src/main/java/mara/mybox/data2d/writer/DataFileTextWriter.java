package mara.mybox.data2d.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.Charset;
import mara.mybox.data2d.Data2D;
import static mara.mybox.data2d.DataFileText.CommentsMarker;
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
            tmpFile = FileTmpTools.getTempFile(".txt");
            if (charset == null) {
                charset = Charset.forName("utf-8");
            }
            if (delimiter == null) {
                delimiter = ",";
            }
            fileWriter = new BufferedWriter(new FileWriter(tmpFile, charset));
            if (writeComments && targetComments != null && !targetComments.isBlank()) {
                for (String line : targetComments.split("\n")) {
                    fileWriter.write(CommentsMarker + " " + line + "\n");
                }
            }
            if (writeHeader && headerNames != null) {
                TextFileTools.writeLine(task(), fileWriter, headerNames, delimiter);
            }
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
            TextFileTools.writeLine(task(), fileWriter, printRow, delimiter);
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
            fileWriter.flush();
            fileWriter.close();
            fileWriter = null;
            if (isFailed() || tmpFile == null || !tmpFile.exists()) {
                FileDeleteTools.delete(tmpFile);
                showInfo(message("Failed") + ": " + printFile);
                status = Status.Failed;
                return;
            }
            if (targetRowIndex == 0) {
                FileDeleteTools.delete(tmpFile);
                showInfo(message("NoData") + ": " + printFile);
                status = Status.NoData;
                return;
            }
            if (!FileTools.override(tmpFile, printFile)) {
                FileDeleteTools.delete(tmpFile);
                showInfo(message("Failed") + ": " + printFile);
                status = Status.Failed;
                return;
            }
            if (printFile == null || !printFile.exists()) {
                showInfo(message("Failed") + ": " + printFile);
                status = Status.Failed;
                return;
            }
            recordFileGenerated(printFile, VisitHistory.FileType.Text);
            if (recordTargetData) {
                recordTargetData();
            }
            status = Status.Created;
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    public void recordTargetData() {
        try {
            if (targetData == null) {
                targetData = Data2D.create(Data2DDefinition.DataType.Texts);
            }
            targetData.setCharset(charset).setDelimiter(delimiter);
            saveTargetData(writeHeader && headerNames != null, columns);
        } catch (Exception e) {
            showError(e.toString());
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
