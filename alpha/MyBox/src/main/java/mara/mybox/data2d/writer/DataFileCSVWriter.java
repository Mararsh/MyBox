package mara.mybox.data2d.writer;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataFileCSVWriter extends Data2DWriter {

    protected DataFileCSV sourceCSV;
    protected Iterator<CSVRecord> iterator;
    protected CSVParser csvParser;
    protected CSVPrinter csvPrinter;

    public DataFileCSVWriter(DataFileCSV data) {
        this.sourceCSV = data;
        init(data);
    }

    @Override
    public void scanData() {
        if (!FileTools.hasData(sourceFile)) {
            return;
        }
        File tmpFile = FileTmpTools.getTempFile();
        Charset charset = sourceCSV.getCharset();
        CSVFormat format = sourceCSV.cvsFormat();
        File validFile = FileTools.removeBOM(task, sourceFile);
        if (validFile == null || writerStopped()) {
            return;
        }
        rowIndex = 0;
        count = 0;
        try (CSVParser parser = CSVParser.parse(validFile, charset, format);
                CSVPrinter printer = new CSVPrinter(new FileWriter(tmpFile, charset), format)) {
            csvParser = parser;
            csvPrinter = printer;
            iterator = parser.iterator();
            failed = !handleRows();
            csvPrinter = null;
            csvParser = null;
            printer.close();
            parser.close();
            if (failed) {
                FileDeleteTools.delete(tmpFile);
            } else {
                failed = !FileTools.rename(tmpFile, sourceFile, false);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            failed = true;
        }
        if (failed) {
            writerStopped = true;
        }
    }

    public boolean handleRows() {
        if (iterator == null) {
            return false;
        }
        try {
            if (sourceCSV.isHasHeader()) {
                try {
                    csvPrinter.printRecord(csvParser.getHeaderNames());
                } catch (Exception e) {  // skip  bad lines
                }
            }
            if (isClearData()) {
                count = data2D.getDataSize();
                return true;
            }
            while (iterator.hasNext() && !writerStopped()) {
                readRow();
                if (sourceRow == null || sourceRow.isEmpty()) {
                    continue;
                }
                ++rowIndex;
                handleRow();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
        return true;
    }

    public void readRow() {
        try {
            sourceRow = null;
            if (writerStopped() || iterator == null) {
                return;
            }
            CSVRecord csvRecord = iterator.next();
            if (csvRecord == null) {
                return;
            }
            sourceRow = new ArrayList<>();
            for (String v : csvRecord) {
                sourceRow.add(v);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    @Override
    public void writeRow() {
        try {
            if (writerStopped() || targetRow == null) {
                return;
            }
            csvPrinter.printRecord(targetRow);
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

}
