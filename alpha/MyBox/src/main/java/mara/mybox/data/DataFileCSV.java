package mara.mybox.data;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import mara.mybox.controller.ControlDataConvert;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppValues;
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
        String[] values = {",", " ", "|", "@", "#", ";", ":", "*",
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
        checkForLoad();
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
        if (file == null) {
            return null;
        }
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
        DataFileCSV targetCSVFile = (DataFileCSV) targetData;
        File tmpFile = TmpFileTools.getTempFile();
        File tFile = targetCSVFile.getFile();
        if (tFile == null) {
            return false;
        }
        targetCSVFile.checkForLoad();
        Charset tCharset = targetCSVFile.getCharset();
        boolean tHasHeader = targetCSVFile.isHasHeader();
        CSVFormat tFormat = targetCSVFile.cvsFormat();
        checkForLoad();
        if (file != null) {
            try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat());
                     CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, tCharset), tFormat)) {
                if (tHasHeader) {
                    writeHeader(csvPrinter);
                }
                long index = -1;
                Iterator<CSVRecord> iterator = parser.iterator();
                if (iterator != null) {
                    while (iterator.hasNext() && task != null && !task.isCancelled()) {
                        try {
                            CSVRecord record = iterator.next();
                            if (record != null) {
                                if (++index < startRowOfCurrentPage || index >= endRowOfCurrentPage) {
                                    writeFileRow(csvPrinter, record);
                                } else if (index == startRowOfCurrentPage) {
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
            if (csvPrinter == null) {
                return false;
            }
            if (!isColumnsValid()) {
                return true;
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
            if (csvPrinter == null) {
                return false;
            }
            if (!isColumnsValid()) {
                return true;
            }
            for (int r = 0; r < tableRowsNumber(); r++) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                csvPrinter.printRecord(tableRowWithoutNumber(r));
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
    public boolean export(ControlDataConvert convertController, List<Integer> colIndices) {
        if (convertController == null || file == null
                || colIndices == null || colIndices.isEmpty()) {
            return false;
        }
        try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat())) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        CSVRecord record = iterator.next();
                        if (record == null) {
                            continue;
                        }
                        List<String> exportRow = new ArrayList<>();
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

    @Override
    public List<List<String>> allRows(List<Integer> cols) {
        if (file == null || cols == null || cols.isEmpty()) {
            return null;
        }
        List<List<String>> rows = new ArrayList<>();
        try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat())) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        CSVRecord record = iterator.next();
                        if (record != null) {
                            List<String> row = new ArrayList<>();
                            for (int col : cols) {
                                if (col >= 0 && col < record.size()) {
                                    row.add(record.get(col));
                                } else {
                                    row.add(null);
                                }
                            }
                            if (!row.isEmpty()) {
                                rows.add(row);
                            }
                        }
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return rows;
    }

    @Override
    public DoubleStatistic[] statisticData(List<Integer> cols) {
        if (file == null || cols == null || cols.isEmpty()) {
            return null;
        }
        int colLen = cols.size();
        DoubleStatistic[] sData = new DoubleStatistic[colLen];
        for (int c = 0; c < colLen; c++) {
            sData[c] = new DoubleStatistic();
        }
        try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat())) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        CSVRecord record = iterator.next();
                        if (record != null) {
                            for (int c = 0; c < colLen; c++) {
                                sData[c].count++;
                                int col = cols.get(c);
                                if (col < 0 || col >= record.size()) {
                                    continue;
                                }
                                double v = doubleValue(record.get(col));
                                sData[c].sum += v;
                                if (v > sData[c].maximum) {
                                    sData[c].maximum = v;
                                }
                                if (v < sData[c].minimum) {
                                    sData[c].minimum = v;
                                }
                            }
                        }
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        boolean allInvalid = true;
        for (int c = 0; c < colLen; c++) {
            if (sData[c].count != 0) {
                sData[c].mean = sData[c].sum / sData[c].count;
                allInvalid = false;
            } else {
                sData[c].mean = AppValues.InvalidDouble;
                sData[c].variance = AppValues.InvalidDouble;
                sData[c].skewness = AppValues.InvalidDouble;
            }
        }
        if (allInvalid) {
            return sData;
        }
        try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat())) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        CSVRecord record = iterator.next();
                        if (record != null) {
                            for (int c = 0; c < colLen; c++) {
                                if (sData[c].count == 0) {
                                    continue;
                                }
                                int col = cols.get(c);
                                if (col < 0 || col >= record.size()) {
                                    continue;
                                }
                                double v = doubleValue(record.get(col));
                                sData[c].variance += Math.pow(v - sData[c].mean, 2);
                                sData[c].skewness += Math.pow(v - sData[c].mean, 3);
                            }
                        }
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        for (int c = 0; c < colLen; c++) {
            if (sData[c].count == 0) {
                continue;
            }
            sData[c].variance = Math.sqrt(sData[c].variance / sData[c].count);
            sData[c].skewness = Math.cbrt(sData[c].skewness / sData[c].count);
        }

        return sData;
    }

    @Override
    public File percentage(List<String> names, List<Integer> cols, boolean withValues) {
        if (file == null || cols == null || cols.isEmpty()) {
            return null;
        }
        int colLen = cols.size();
        double[] sum = new double[colLen];
        try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat())) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        CSVRecord record = iterator.next();
                        if (record != null) {
                            for (int c = 0; c < colLen; c++) {
                                int col = cols.get(c);
                                if (col < 0 || col >= record.size()) {
                                    continue;
                                }
                                double v = doubleValue(record.get(col));
                                sum[c] += v;
                            }
                        }
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        File csvFile = TmpFileTools.getPathTempFile(AppPaths.getGeneratedPath(), ".csv");
        CSVFormat csvFormat = CSVFormat.DEFAULT
                .withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(',').withFirstRecordAsHeader();
        try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat());
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFile, Charset.forName("UTF-8")), csvFormat)) {
            csvPrinter.printRecord(names);
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                int rowIndex = 0;
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        CSVRecord record = iterator.next();
                        if (record != null) {
                            List<String> row = new ArrayList<>();
                            row.add(rowIndex++ + "");
                            for (int c = 0; c < colLen; c++) {
                                int col = cols.get(c);
                                double v = 0;
                                if (col >= 0 && col < record.size()) {
                                    v = doubleValue(record.get(col));
                                }
                                if (withValues) {
                                    row.add(DoubleTools.format(v, scale));
                                }
                                if (sum[c] == 0) {
                                    row.add("0");
                                } else {
                                    row.add(DoubleTools.percentage(v, sum[c]));
                                }
                            }
                            csvPrinter.printRecord(row);
                        }
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return csvFile;
    }

    /*
        static
     */
    public static LinkedHashMap<File, Boolean> save(File path, String filePrefix, List<StringTable> tables) {
        if (tables == null || tables.isEmpty()) {
            return null;
        }
        try {
            LinkedHashMap<File, Boolean> files = new LinkedHashMap<>();
            String[][] data;
            int count = 1;
            CSVFormat csvFormat = CSVFormat.DEFAULT
                    .withDelimiter(',')
                    .withIgnoreEmptyLines().withTrim().withNullString("");
            for (StringTable stringTable : tables) {
                List<List<String>> tableData = stringTable.getData();
                if (tableData == null || tableData.isEmpty()) {
                    continue;
                }
                data = TextTools.toArray(tableData);
                if (data == null || data.length == 0) {
                    continue;
                }
                List<String> names = stringTable.getNames();
                boolean withName = names != null && !names.isEmpty();
                String title = stringTable.getTitle();
                File csvFile = new File(path + File.separator
                        + FileNameTools.filter((filePrefix == null || filePrefix.isBlank() ? "" : filePrefix + "_")
                                + (title == null || title.isBlank() ? "_" + count : title))
                        + ".csv");
                try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFile, Charset.forName("UTF-8")), csvFormat)) {
                    if (withName) {
                        csvPrinter.printRecord(names);
                    }
                    for (int r = 0; r < data.length; r++) {
                        csvPrinter.printRecord(Arrays.asList(data[r]));
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
                if (csvFile.exists()) {
                    files.put(csvFile, withName);
                    count++;
                }
            }
            return files;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }

    }

}
