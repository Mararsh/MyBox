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

    protected DataClipboard clip;
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
            if (printFile == null) {
                printFile = DataClipboard.newFile();
            }
            if (printFile == null) {
                showInfo(message("InvalidParameter") + ": " + message("TargetFile"));
                return false;
            }
            showInfo(message("Writing") + " " + printFile.getAbsolutePath());
            tmpFile = FileTmpTools.getTempFile();
            printer = new CSVPrinter(new FileWriter(tmpFile,
                    Charset.forName("UTF-8")), CsvTools.csvFormat());
            if (writeHeader && headerNames != null) {
                printer.printRecord(headerNames);
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
            printer.printRecord(printRow);
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public void finishWork() {
        try {
            if (printer == null) {
                showInfo(message("Failed") + ": " + printFile);
                return;
            }
            printer.flush();
            printer.close();
            printer = null;
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
            if (targetData == null) {
                targetData = Data2D.create(Data2DDefinition.DataType.MyBoxClipboard);
            }
            targetData.setCharset(Charset.forName("UTF-8")).setDelimiter(",");
            saveTargetData(true, columns);

            DataInMyBoxClipboardController.update();
            showInfo(message("Generated") + ": " + printFile + "  "
                    + message("FileSize") + ": " + printFile.length());
            showInfo(message("RowsNumber") + ": " + targetRowIndex);
            status = Status.Created;
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public boolean showResult() {
        if (targetData == null) {
            return false;
        }
        DataInMyBoxClipboardController.open(targetData);
        return true;
    }

}
