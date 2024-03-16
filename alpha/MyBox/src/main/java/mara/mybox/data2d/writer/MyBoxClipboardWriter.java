package mara.mybox.data2d.writer;

import java.io.FileWriter;
import java.nio.charset.Charset;
import mara.mybox.controller.DataInMyBoxClipboardController;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataClipboard;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class MyBoxClipboardWriter extends Data2DWriter {

    protected CSVPrinter printer;

    public MyBoxClipboardWriter() {
        fileSuffix = "csv";
    }

    @Override
    public boolean openWriter() {
        try {
            if (!super.openWriter()) {
                return false;
            }
            targetFile = DataClipboard.newFile();
            if (targetFile == null) {
                showInfo(message("InvalidParameter") + ": " + message("TargetFile"));
                return false;
            }
            showInfo(message("Writing") + " " + targetFile.getAbsolutePath());
            tmpFile = FileTmpTools.getTempFile();
            printer = new CSVPrinter(new FileWriter(tmpFile,
                    Charset.forName("UTF-8")), CsvTools.csvFormat());
            printer.printRecord(headerNames);
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
            printer.printRecord(targetRow);
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public void closeWriter() {
        try {
            created = false;
            if (printer == null) {
                return;
            }
            printer.flush();
            printer.close();
            printer = null;
            if (isFailed() || tmpFile == null || !tmpFile.exists()
                    || !FileTools.override(tmpFile, targetFile)) {
                FileDeleteTools.delete(tmpFile);
                return;
            }
            if (targetFile == null || !targetFile.exists()) {
                return;
            }
            targetData = Data2D.create(Data2DDefinition.DataType.MyBoxClipboard);
            targetData.setTask(task()).setFile(targetFile)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",")
                    .setHasHeader(true)
                    .setDataName(dataName)
                    .setColsNumber(columns.size())
                    .setRowsNumber(targetRowIndex);
            Data2D.saveAttributes(conn(), targetData, columns);
            DataInMyBoxClipboardController.update();
            created = true;
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public void showResult() {
        if (targetData == null) {
            return;
        }
        DataInMyBoxClipboardController.open(targetData);
    }

}
