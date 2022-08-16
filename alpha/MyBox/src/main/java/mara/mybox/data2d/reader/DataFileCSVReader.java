package mara.mybox.data2d.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataFileCSVReader extends Data2DReader {

    protected DataFileCSV readerCSV;
    protected Iterator<CSVRecord> iterator;
    CSVParser csvParser;

    public DataFileCSVReader(DataFileCSV data) {
        this.readerCSV = data;
        init(data);
    }

    @Override
    public void scanData() {
        if (!FileTools.hasData(sourceFile)) {
            return;
        }
        readerCSV.checkForLoad();
        File validFile = FileTools.removeBOM(sourceFile);
        try ( CSVParser parser = CSVParser.parse(validFile, readerCSV.getCharset(), readerCSV.cvsFormat())) {
            csvParser = parser;
            iterator = parser.iterator();
            handleData();
            csvParser = null;
            parser.close();
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            failed = true;
        }
    }

    @Override
    public void readColumnNames() {
        if (csvParser == null) {
            return;
        }
        sourceRow = null;
        if (readerHasHeader) {
            try {
                List<String> values = csvParser.getHeaderNames();
                if (StringTools.noDuplicated(values, true)) {
                    names = new ArrayList<>();
                    names.addAll(values);
                    return;
                } else {
                    sourceRow = new ArrayList<>();
                    sourceRow.addAll(values);
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
                if (task != null) {
                    task.setError(e.toString());
                }
            }
        } else {
            while (iterator.hasNext() && !readerStopped()) {
                readRecord();
                if (sourceRow != null && !sourceRow.isEmpty()) {
                    break;
                }
            }
        }
        readerHasHeader = false;
        handleHeader();
    }

    @Override
    public void readTotal() {
        if (iterator == null) {
            return;
        }
        rowIndex = 0;
        while (iterator.hasNext()) {
            if (readerStopped()) {
                rowIndex = 0;
                return;
            }
            readRecord();
            if (sourceRow != null && !sourceRow.isEmpty()) {
                ++rowIndex;
            }
        }
    }

    @Override
    public void readPage() {
        if (iterator == null) {
            return;
        }
        rowIndex = 0;
        while (iterator.hasNext() && !readerStopped()) {
            readRecord();
            if (sourceRow == null || sourceRow.isEmpty()) {
                continue;
            }
            if (rowIndex++ < rowsStart) {
                continue;
            }
            if (rowIndex > rowsEnd) {
                readerStopped = true;
                break;
            }
            handlePageRow();
        }
    }

    @Override
    public void readRows() {
        if (iterator == null) {
            return;
        }
        rowIndex = 0;
        while (iterator.hasNext() && !readerStopped()) {
            readRecord();
            if (sourceRow == null || sourceRow.isEmpty()) {
                continue;
            }
            ++rowIndex;
            handleRow();

        }
    }

    public void readRecord() {
        try {
            sourceRow = null;
            if (readerStopped() || iterator == null) {
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

}
