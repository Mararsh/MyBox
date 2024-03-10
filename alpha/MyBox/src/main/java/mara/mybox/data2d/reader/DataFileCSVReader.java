package mara.mybox.data2d.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mara.mybox.data2d.DataFileCSV;
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
    protected CSVParser csvParser;

    public DataFileCSVReader(DataFileCSV data) {
        readerCSV = data;
        sourceData = data;
    }

    @Override
    public void scanData() {
        if (!FileTools.hasData(sourceFile)) {
            return;
        }
        readerCSV.checkForLoad();
        File validFile = FileTools.removeBOM(task(), sourceFile);
        if (validFile == null || isStopped()) {
            return;
        }
        try (CSVParser parser = CSVParser.parse(validFile, readerCSV.getCharset(), readerCSV.cvsFormat())) {
            csvParser = parser;
            iterator = parser.iterator();
            operate.handleData();
            csvParser = null;
            parser.close();
        } catch (Exception e) {
            handleError(e.toString());
            setFailed();
        }
    }

    @Override
    public void readColumnNames() {
        try {
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
                    handleError(e.toString());
                }
            } else {
                while (iterator.hasNext() && !isStopped()) {
                    readRecord();
                    if (sourceRow != null && !sourceRow.isEmpty()) {
                        break;
                    }
                }
            }
            readerHasHeader = false;
            handleHeader();
        } catch (Exception e) {
            handleError(e.toString());
        }
    }

    @Override
    public void readTotal() {
        try {
            if (iterator == null) {
                return;
            }
            rowIndex = 0;
            while (iterator.hasNext()) {
                if (isStopped()) {
                    rowIndex = 0;
                    return;
                }
                readRecord();
                if (sourceRow != null && !sourceRow.isEmpty()) {
                    ++rowIndex;
                }
            }
        } catch (Exception e) {
            handleError(e.toString());
            setFailed();
        }
    }

    // rowIndex is 1-base while pageStartIndex and pageEndIndex are 0-based
    @Override
    public void readPage() {
        try {
            if (iterator == null) {
                return;
            }
            rowIndex = 0;
            while (iterator.hasNext() && !isStopped()) {
                readRecord();
                if (sourceRow == null || sourceRow.isEmpty()) {
                    continue;
                }
                if (rowIndex++ < pageStartIndex) {
                    continue;
                }
                if (rowIndex > pageEndIndex) {
                    stop();
                    break;
                }
                handlePageRow();
            }
        } catch (Exception e) {
            handleError(e.toString());
            setFailed();
        }
    }

    @Override
    public void readRows() {
        try {
            if (iterator == null) {
                return;
            }
            rowIndex = 0;
            while (iterator.hasNext() && !isStopped()) {
                readRecord();
                if (sourceRow == null || sourceRow.isEmpty()) {
                    continue;
                }
                ++rowIndex;
                handleRow();
            }
        } catch (Exception e) {
            handleError(e.toString());
            setFailed();
        }
    }

    public void readRecord() {
        try {
            sourceRow = null;
            if (isStopped() || iterator == null) {
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
            handleError(e.toString());
        }
    }

}
