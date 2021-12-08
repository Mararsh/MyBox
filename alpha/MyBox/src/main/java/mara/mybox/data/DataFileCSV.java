package mara.mybox.data;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mara.mybox.controller.ControlDataConvert;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TmpFileTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class DataFileCSV extends DataFileText {

    public DataFileCSV() {
        type = Type.CSV;
    }

    @Override
    public String guessDelimiter() {
        String[] values = {",", " ", "|", "@", "#", ";", ":", "*", ".",
            "%", "$", "_", "&", "-", "=", "!", "\"", "'", "<", ">"};
        return guessDelimiter(values);
    }

    public CSVFormat cvsFormat() {
        if (delimiter == null || delimiter.isEmpty()) {
            delimiter = ",";
        }
        CSVFormat csvFormat = CSVFormat.DEFAULT
                .withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(delimiter.charAt(0));
        if (hasHeader) {
            csvFormat = csvFormat.withFirstRecordAsHeader();
        }
        return csvFormat;
    }

    @Override
    public List<String> readColumns() {
        if (file == null) {
            return null;
        }
        List<String> names = null;
        checkAttributes();
        if (hasHeader) {
            try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat())) {
                names = parser.getHeaderNames();
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.console(e);
            }
        }
        if (names == null) {
            hasHeader = false;
            try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat())) {
                Iterator<CSVRecord> iterator = parser.iterator();
                if (iterator != null && iterator.hasNext()) {
                    CSVRecord record = iterator.next();
                    if (record != null) {
                        names = new ArrayList<>();
                        for (int i = 1; i <= record.size(); i++) {
                            names.add(colPrefix() + i);
                        }
                    }
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.console(e);
            }
        }
        return names;
    }

    @Override
    public long readTotal() {
        dataSize = 0;
        if (file == null) {
            return dataSize;
        }
        try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat())) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                while (iterator.hasNext()) {
                    if (backgroundTask == null || backgroundTask.isCancelled()) {
                        dataSize = 0;
                        break;
                    }
                    try {
                        if (iterator.next() != null) {
                            dataSize++;
                        }
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
            }
        } catch (Exception e) {
            if (backgroundTask != null) {
                backgroundTask.setError(e.toString());
            }
            MyBoxLog.console(e);
            return -1;
        }
        return dataSize;
    }

    @Override
    public List<List<String>> readPageData() {
        if (file == null) {
            startRowOfCurrentPage = endRowOfCurrentPage = 0;
            return null;
        }
        if (startRowOfCurrentPage < 0) {
            startRowOfCurrentPage = 0;
        }
        endRowOfCurrentPage = startRowOfCurrentPage;
        List<List<String>> rows = new ArrayList<>();
        try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat())) {
            long rowIndex = -1;
            int columnsNumber = columnsNumber();
            long end = startRowOfCurrentPage + pageSize;

            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        CSVRecord record = iterator.next();
                        if (record != null) {
                            if (++rowIndex < startRowOfCurrentPage) {
                                continue;
                            }
                            if (rowIndex >= end) {
                                break;
                            }
                            List<String> row = new ArrayList<>();
                            for (int i = 0; i < Math.min(record.size(), columnsNumber); i++) {
                                row.add(record.get(i));
                            }
                            for (int col = row.size(); col < columnsNumber; col++) {
                                row.add(defaultColValue());
                            }
                            row.add(0, "" + (rowIndex + 1));
                            rows.add(row);
                        }
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
            return null;
        }
        endRowOfCurrentPage = startRowOfCurrentPage + rows.size();
        return rows;
    }

    @Override
    public boolean savePageData(Data2D targetData) {
        if (targetData == null || !(targetData instanceof DataFileCSV)) {
            return false;
        }
        File tmpFile = TmpFileTools.getTempFile();
        File tFile = getFile();
        if (tFile == null) {
            return false;
        }
        checkAttributes();
        Charset tCharset = getCharset();
        boolean tHasHeader = isHasHeader();
        CSVFormat tFormat = cvsFormat();
        checkAttributes();
        if (file != null) {
            try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat());
                     CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, tCharset), tFormat)) {
                if (tHasHeader) {
                    writeHeader(csvPrinter);
                }
                long index = -1;
                long pageStart = getStartRowOfCurrentPage(), pageEnd = getEndRowOfCurrentPage();
                Iterator<CSVRecord> iterator = parser.iterator();
                if (iterator != null) {
                    while (iterator.hasNext() && task != null && !task.isCancelled()) {
                        try {
                            CSVRecord record = iterator.next();
                            if (record != null) {
                                if (++index < pageStart || index >= pageEnd) {
                                    writeFileRow(csvPrinter, record);
                                } else if (index == pageStart) {
                                    if (!writePageData(csvPrinter)) {
                                        return false;
                                    }
                                }
                            }
                        } catch (Exception e) {  // skip  bad lines
//                            MyBoxLog.debug(e);
                        }
                    }
                }
                if (index < 0) {
                    if (!writePageData(csvPrinter)) {
                        return false;
                    }
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
                if (task != null) {
                    task.setError(e.toString());
                }
                return false;
            }
        } else {
            try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, tCharset), tFormat)) {
                if (tHasHeader) {
                    writeHeader(csvPrinter);
                }
                if (!writePageData(csvPrinter)) {
                    return false;
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
                if (task != null) {
                    task.setError(e.toString());
                }
                return false;
            }
        }
        return FileTools.rename(tmpFile, tFile, false);
    }

    public boolean writeHeader(CSVPrinter csvPrinter) {
        try {
            if (csvPrinter == null || !isColumnsValid()) {
                return false;
            }
            List<String> names = columnNames();
            if (names != null) {
                csvPrinter.printRecord(columnNames());
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    public boolean writePageData(CSVPrinter csvPrinter) {
        try {
            if (csvPrinter == null || !isColumnsValid()) {
                return false;
            }
            for (int r = 0; r < tableRowsNumber(); r++) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                csvPrinter.printRecord(tableRow(r));
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    public void writeFileRow(CSVPrinter csvPrinter, CSVRecord record) {
        try {
            List<String> row = new ArrayList<>();
            for (String v : record) {
                row.add(v);
            }
            csvPrinter.printRecord(fileRow(row));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public File tmpFile(List<String> cols, List<List<String>> data) {
        if (cols == null || cols.isEmpty()) {
            if (data == null || data.isEmpty()) {
                return null;
            }
        }
        CSVFormat csvFormat = CSVFormat.DEFAULT
                .withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(',');
        if (cols != null && !cols.isEmpty()) {
            csvFormat = csvFormat.withFirstRecordAsHeader();
        }
        File tmpFile = TmpFileTools.getTempFile(".csv");
        try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, Charset.forName("UTF-8")), csvFormat)) {
            if (cols != null && !cols.isEmpty()) {
                csvPrinter.printRecord(cols);
            }
            if (data != null) {
                for (int r = 0; r < data.size(); r++) {
                    if (task != null && task.isCancelled()) {
                        break;
                    }
                    csvPrinter.printRecord(data.get(r));
                }
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
        return tmpFile;
    }

    @Override
    public boolean export(ControlDataConvert convertController, List<Integer> colIndices, boolean rowNumber) {
        if (convertController == null || file == null
                || colIndices == null || colIndices.isEmpty()) {
            return false;
        }
        try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat())) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                int index = 0;
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        CSVRecord record = iterator.next();
                        if (record == null) {
                            continue;
                        }
                        List<String> exportRow = new ArrayList<>();
                        if (rowNumber) {
                            exportRow.add(message("Row") + ++index);
                        }
                        for (Integer col : colIndices) {
                            String value = null;
                            if (col >= 0 && col < record.size()) {
                                value = record.get(col);
                            }
                            exportRow.add(value);
                        }
                        convertController.writeRow(exportRow);
                    } catch (Exception e) {  // skip  bad lines
//                            MyBoxLog.debug(e);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            convertController.updateLogs(e.toString());
            return false;
        }
        task = null;
        return true;
    }

}
