package mara.mybox.data2d.writer;

import java.io.FileWriter;
import java.nio.charset.Charset;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
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
            tmpFile = FileTmpTools.getTempFile(".csv");
            if (charset == null) {
                charset = Charset.forName("utf-8");
            }
            if (delimiter == null) {
                delimiter = ",";
            }
            printer = new CSVPrinter(new FileWriter(tmpFile, charset),
                    CsvTools.csvFormat(delimiter));
            if (writeHeader && headerNames != null) {
                printer.printRecord(headerNames);
            }
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
    public void closeWriter() {
        try {
            created = false;
            if (printer == null) {
                showInfo(message("Failed") + ": " + printFile);
                return;
            }
            printer.flush();
            printer.close();
            printer = null;
            if (isFailed() || tmpFile == null || !tmpFile.exists()
                    || !FileTools.override(tmpFile, printFile)) {
                FileDeleteTools.delete(tmpFile);
                showInfo(message("Failed") + ": " + printFile);
                return;
            }
            if (printFile == null || !printFile.exists()) {
                showInfo(message("Failed") + ": " + printFile);
                return;
            }
            recordFileGenerated(printFile, VisitHistory.FileType.CSV);
            if (recordTargetData) {
                if (targetData == null) {
                    targetData = Data2D.create(Data2DDefinition.DataType.CSV);
                }
                targetData.setTask(task())
                        .setFile(printFile)
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
