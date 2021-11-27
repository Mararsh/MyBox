package mara.mybox.data;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TmpFileTools;
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
        type = Type.DataFileCSV;
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
        try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat())) {
            if (hasHeader) {
                names = parser.getHeaderNames();
            } else {
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
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
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
                            row.add("" + (rowIndex + 1));
                            for (int i = 0; i < record.size(); i++) {
                                row.add(record.get(i));
                            }
                            for (int col = row.size(); col < columnsNumber; col++) {
                                row.add(defaultColValue());
                            }
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
        DataFileCSV targetCSVFile = (DataFileCSV) targetData;
        File tmpFile = TmpFileTools.getTempFile();
        File tFile = targetCSVFile.getFile();
        if (tFile == null) {
            return false;
        }
        Charset tCharset = targetCSVFile.getCharset();
        if (tCharset == null) {
            tCharset = Charset.forName("UTF-8");
        }
        boolean tHasHeader = targetCSVFile.isHasHeader();
        CSVFormat tFormat = targetCSVFile.cvsFormat();
        if (file != null) {
            try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat());
                     CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, tCharset), tFormat)) {
                if (tHasHeader) {
                    csvPrinter.printRecord(columnNames());
                }
                long index = -1;
                long pageStart = getStartRowOfCurrentPage(), pageEnd = getEndRowOfCurrentPage();
                Iterator<CSVRecord> iterator = parser.iterator();
                if (iterator != null) {
                    while (iterator.hasNext()) {
                        if (task == null || task.isCancelled()) {
                            break;
                        }
                        try {
                            CSVRecord record = iterator.next();
                            if (record != null) {
                                if (++index < pageStart || index >= pageEnd) {
                                    csvPrinter.printRecord(recordRow(record));
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
                    csvPrinter.printRecord(columnNames());
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

    public boolean writePageData(CSVPrinter csvPrinter) {
        try {
            if (csvPrinter == null) {
                return false;
            }
            for (int r = 0; r < tableRowsNumber(); r++) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                csvPrinter.printRecord(pageRow(r));
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

}
