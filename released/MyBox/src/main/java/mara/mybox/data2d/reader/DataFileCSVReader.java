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
    public void scanFile() {
        File validFile = FileTools.removeBOM(task(), sourceFile);
        if (validFile == null || isStopped()) {
            return;
        }
        readerCSV.checkForLoad();
        try (CSVParser parser = CSVParser.parse(validFile, readerCSV.getCharset(), readerCSV.cvsFormat())) {
            csvParser = parser;
            iterator = parser.iterator();
            operate.handleData();
            csvParser = null;
            parser.close();
        } catch (Exception e) {
            showError(e.toString());
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
                    showError(e.toString());
                }
            } else {
                while (iterator.hasNext() && !isStopped()) {
                    readFileRecord();
                    if (sourceRow != null && !sourceRow.isEmpty()) {
                        break;
                    }
                }
            }
            readerHasHeader = false;
            makeHeader();
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public void readTotal() {
        try {
            sourceIndex = 0;
            if (iterator == null) {
                return;
            }
            while (iterator.hasNext()) {
                if (isStopped()) {
                    sourceIndex = 0;
                    return;
                }
                readFileRecord();
                if (sourceRow != null && !sourceRow.isEmpty()) {
                    ++sourceIndex;
                }
            }
        } catch (Exception e) {
            showError(e.toString());
            setFailed();
        }
    }

    // sourceIndex is 1-base while pageStartIndex and pageEndIndex are 0-based
    @Override
    public void readPage() {
        try {
            if (iterator == null) {
                return;
            }
            sourceIndex = 0;
            while (iterator.hasNext() && !isStopped()) {
                readFileRecord();
                if (sourceRow == null || sourceRow.isEmpty()) {
                    continue;
                }
                if (sourceIndex++ < pageStartIndex) {
                    continue;
                }
                if (sourceIndex > pageEndIndex) {
                    stop();
                    break;
                }
                makePageRow();
            }
        } catch (Exception e) {
            showError(e.toString());
            setFailed();
        }
    }

    public void readFileRecord() {
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
            showError(e.toString());
        }
    }

    @Override
    public void readRows() {
        try {
            if (iterator == null) {
                return;
            }
            sourceIndex = 0;
            long fileIndex = -1;
            long startIndex = sourceData.startRowOfCurrentPage;
            long endIndex = sourceData.endRowOfCurrentPage;
            while (iterator.hasNext() && !isStopped()) {
                try {
                    readFileRecord();
                    if (sourceRow == null || sourceRow.isEmpty()) {
                        continue;
                    }
                    fileIndex++;

                    if (fileIndex < startIndex || fileIndex >= endIndex) {
                        ++sourceIndex;
                        handleRow();

                    } else if (fileIndex == startIndex) {
                        scanPage();
                    }

                } catch (Exception e) {  // skip  bad lines
//                    showError(e.toString());
//                    setFailed();
                }
            }
        } catch (Exception e) {
            showError(e.toString());
            setFailed();
        }
    }

}
