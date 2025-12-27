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
            if (writeComments && targetComments != null && !targetComments.isBlank()) {
                printer.printComment(targetComments);
            }
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
            if (printer == null || printFile == null) {
                showInfo(message("Failed") + ": " + printFile);
                status = Status.Failed;
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
            recordFileGenerated(printFile, VisitHistory.FileType.CSV);
            if (recordTargetData) {
                if (targetData == null) {
                    targetData = Data2D.create(Data2DDefinition.DataType.CSV);
                }
                targetData.setCharset(charset).setDelimiter(delimiter);
                saveTargetData(writeHeader && headerNames != null, columns);
            }
            status = Status.Created;
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
