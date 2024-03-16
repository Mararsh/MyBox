package mara.mybox.data2d.writer;

import java.io.FileWriter;
import java.nio.charset.Charset;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
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
public class DataFileCSVWriter extends Data2DWriter {

    protected CSVPrinter printer;
    protected Charset charset;
    protected String delimiter;

    public DataFileCSVWriter() {
        fileSuffix = "csv";
        charset = Charset.forName("utf-8");
        delimiter = ",";
    }

    @Override
    public boolean openWriter() {
        try {
            if (!super.openWriter()) {
                return false;
            }
            MyBoxLog.console(operate != null);
            if (targetFile == null) {
                showInfo(message("InvalidParameter") + ": " + message("TargetFile"));
                return false;
            }
            showInfo(message("Writing") + " " + targetFile.getAbsolutePath());
            tmpFile = FileTmpTools.getTempFile();
            if (printer == null) {
                printer = new CSVPrinter(new FileWriter(tmpFile, charset),
                        CsvTools.csvFormat(delimiter));
            }
            if (writeHeader && headerNames != null) {
                printer.printRecord(headerNames);
            }
            MyBoxLog.console(isFailed());
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
            MyBoxLog.console(isFailed() + "  " + tmpFile.exists());
            if (isFailed() || tmpFile == null || !tmpFile.exists()
                    || !FileTools.override(tmpFile, targetFile)) {
                FileDeleteTools.delete(tmpFile);
                return;
            }
            MyBoxLog.console(targetFile);
            if (targetFile == null || !targetFile.exists()) {
                return;
            }
            recordFileGenerated(targetFile, VisitHistory.FileType.CSV);
            if (recordTargetData) {
                if (targetData == null) {
                    targetData = Data2D.create(Data2DDefinition.DataType.CSV);
                }
                targetData.setTask(task()).setFile(targetFile)
                        .setCharset(charset)
                        .setDelimiter(delimiter)
                        .setHasHeader(writeHeader)
                        .setDataName(dataName)
                        .setColsNumber(columns.size())
                        .setRowsNumber(targetRowIndex);
                Data2D.saveAttributes(conn(), targetData, columns);
            }
            created = true;
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    /*
        get/set
     */
    public CSVPrinter getPrinter() {
        return printer;
    }

    public DataFileCSVWriter setCsvPrinter(CSVPrinter csvPrinter) {
        this.printer = csvPrinter;
        return this;
    }

    public Charset getCharset() {
        return charset;
    }

    public DataFileCSVWriter setCharset(Charset cvsCharset) {
        this.charset = cvsCharset;
        return this;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public DataFileCSVWriter setDelimiter(String csvDelimiter) {
        this.delimiter = csvDelimiter;
        return this;
    }

}
