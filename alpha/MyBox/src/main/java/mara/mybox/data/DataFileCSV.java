package mara.mybox.data;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import mara.mybox.controller.ControlDataConvert;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextTools;
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
        if (file == null || !file.exists() || file.length() == 0) {
            return null;
        }
        List<String> names = null;
        checkForLoad();
        if (hasHeader) {
            try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat())) {
                List<String> values = parser.getHeaderNames();
                if (StringTools.noDuplicated(values, true)) {
                    names = values;
                }
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
        if (file == null || !file.exists() || file.length() == 0) {
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
        if (file == null || !file.exists() || file.length() == 0) {
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
        if (file != null && file.exists() && file.length() > 0) {
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
        File tmpFile = TmpFileTools.csvFile();
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
        if (convertController == null || file == null || !file.exists() || file.length() == 0
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
    public List<List<String>> allRows(List<Integer> cols, boolean rowNumber) {
        if (file == null || !file.exists() || file.length() == 0 || cols == null || cols.isEmpty()) {
            return null;
        }
        List<List<String>> rows = new ArrayList<>();
        try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat())) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                int index = 1;
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
                            if (row.isEmpty()) {
                                continue;
                            }
                            if (rowNumber) {
                                row.add(0, index++ + "");
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
            MyBoxLog.error(e);
            return null;
        }
        return rows;
    }

    @Override
    public DoubleStatistic[] statisticData(List<Integer> cols) {
        if (file == null || !file.exists() || file.length() == 0 || cols == null || cols.isEmpty()) {
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
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        boolean allInvalid = true;
        for (int c = 0; c < colLen; c++) {
            if (sData[c].count != 0) {
                sData[c].mean = sData[c].sum / sData[c].count;
                allInvalid = false;
            } else {
                sData[c].mean = 0;
                sData[c].variance = 0;
                sData[c].skewness = 0;
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
            if (task != null) {
                task.setError(e.toString());
            }
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
    public boolean setValue(List<Integer> cols, String value) {
        if (file == null || !file.exists() || file.length() == 0 || cols == null || cols.isEmpty()) {
            return false;
        }
        File tmpFile = TmpFileTools.getTempFile();
        CSVFormat format = cvsFormat();
        try ( CSVParser parser = CSVParser.parse(file, charset, format);
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, charset), format)) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                if (hasHeader) {
                    try {
                        csvPrinter.printRecord(parser.getHeaderNames());
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
                boolean isRandom = "MyBox##random".equals(value);
                boolean isRandomNn = "MyBox##randomNn".equals(value);
                Random random = new Random();
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        CSVRecord record = iterator.next();
                        if (record != null) {
                            List<String> row = new ArrayList<>();
                            for (int i = 0; i < columns.size(); i++) {
                                if (cols.contains(i)) {
                                    if (isRandom) {
                                        row.add(random(random, i, false));
                                    } else if (isRandomNn) {
                                        row.add(random(random, i, true));
                                    } else {
                                        row.add(value);
                                    }
                                } else if (i < record.size()) {
                                    row.add(record.get(i));
                                } else {
                                    row.add(null);
                                }
                            }
                            csvPrinter.printRecord(row);
                        }
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
        return FileTools.rename(tmpFile, file, false);
    }

    @Override
    public DataFileCSV copy(List<Integer> cols, boolean rowNumber, boolean colName) {
        if (file == null || !file.exists() || file.length() == 0 || cols == null || cols.isEmpty()) {
            return null;
        }
        File csvFile = tmpFile("copy");
        CSVFormat targetFormat = CSVFormat.DEFAULT
                .withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(delimiter.charAt(0));
        int tcolsNumber = 0, trowsNumber = 0;
        try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat());
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFile, charset), targetFormat)) {

            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        CSVRecord record = iterator.next();
                        if (record != null) {
                            List<String> row = new ArrayList<>();
                            if (rowNumber) {
                                row.add((trowsNumber + 1) + "");
                            }
                            for (int i : cols) {
                                if (i >= 0 && i < record.size()) {
                                    row.add(record.get(i));
                                } else {
                                    row.add(null);
                                }
                            }
                            csvPrinter.printRecord(row);
                            trowsNumber++;
                        }
                    } catch (Exception e) {  // skip  bad lines
                        MyBoxLog.error(e);
                    }
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(charset)
                    .setDelimiter(delimiter).setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(trowsNumber);
            return targetData;
        } else {
            return null;
        }
    }

    @Override
    public DataFileCSV percentage(List<String> names, List<Integer> cols, boolean withValues) {
        if (file == null || !file.exists() || file.length() == 0
                || cols == null || cols.isEmpty()) {
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
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        File csvFile = tmpFile("percentage");
        CSVFormat targetFormat = CSVFormat.DEFAULT
                .withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(delimiter.charAt(0));
        int trowsNumber = 0;
        try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat());
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFile, charset), targetFormat)) {
            csvPrinter.printRecord(names);
            List<String> row = new ArrayList<>();
            row.add(message("Summation"));
            for (int c = 0; c < colLen; c++) {
                row.add(DoubleTools.scale(sum[c], scale) + "");
                if (withValues) {
                    row.add("");
                }
            }
            csvPrinter.printRecord(row);
            trowsNumber++;
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        CSVRecord record = iterator.next();
                        if (record != null) {
                            row = new ArrayList<>();
                            row.add(trowsNumber++ + "");
                            for (int c = 0; c < colLen; c++) {
                                int col = cols.get(c);
                                double v = 0;
                                if (col >= 0 && col < record.size()) {
                                    v = doubleValue(record.get(col));
                                }
                                if (withValues) {
                                    row.add(DoubleTools.scale(v, scale) + "");
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
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(charset)
                    .setDelimiter(delimiter).setHasHeader(true)
                    .setColsNumber(names.size()).setRowsNumber(trowsNumber);
            return targetData;
        } else {
            return null;
        }
    }

    @Override
    public DataFileCSV normalizeMinMax(List<Integer> cols, double from, double to,
            boolean rowNumber, boolean colName) {
        if (file == null || !file.exists() || file.length() == 0 || cols == null || cols.isEmpty()) {
            return null;
        }
        int colLen = cols.size();
        double[] max = new double[colLen];
        double[] min = new double[colLen];
        for (int c = 0; c < colLen; c++) {
            max[c] = -Double.MAX_VALUE;
            min[c] = Double.MAX_VALUE;
        }
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
                                if (v > max[c]) {
                                    max[c] = v;
                                }
                                if (v < min[c]) {
                                    min[c] = v;
                                }
                            }
                        }
                    } catch (Exception e) {  // skip  bad lines
                        MyBoxLog.error(e);
                    }
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        double[] k = new double[colLen];
        for (int c = 0; c < colLen; c++) {
            double d = max[c] - min[c];
            k[c] = (to - from) / (d == 0 ? Double.MIN_VALUE : d);
        }
        File csvFile = tmpFile("normalizeMinMax");
        CSVFormat targetFormat = CSVFormat.DEFAULT
                .withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(delimiter.charAt(0));
        int tcolsNumber = 0, trowsNumber = 0;
        try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat());
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFile, charset), targetFormat)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        CSVRecord record = iterator.next();
                        if (record != null) {
                            List<String> row = new ArrayList<>();
                            if (rowNumber) {
                                row.add((trowsNumber + 1) + "");
                            }
                            for (int c = 0; c < colLen; c++) {
                                int col = cols.get(c);
                                if (col < 0 || col >= record.size()) {
                                    row.add(null);
                                } else {
                                    double v = doubleValue(record.get(col));
                                    v = from + k[c] * (v - min[c]);
                                    row.add(DoubleTools.scale(v, scale) + "");
                                }
                            }
                            csvPrinter.printRecord(row);
                            trowsNumber++;
                        }
                    } catch (Exception e) {  // skip  bad lines
                        MyBoxLog.error(e);
                    }
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(charset)
                    .setDelimiter(delimiter).setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(trowsNumber);
            return targetData;
        } else {
            return null;
        }
    }

    @Override
    public DataFileCSV normalizeSum(List<Integer> cols, boolean rowNumber, boolean colName) {
        if (file == null || !file.exists() || file.length() == 0 || cols == null || cols.isEmpty()) {
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
                                sum[c] += Math.abs(v);
                            }
                        }
                    } catch (Exception e) {  // skip  bad lines
                        MyBoxLog.error(e);
                    }
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        double[] k = new double[colLen];
        for (int c = 0; c < colLen; c++) {
            if (sum[c] == 0) {
                k[c] = 1d / Double.MIN_VALUE;
            } else {
                k[c] = 1d / sum[c];
            }
        }
        File csvFile = tmpFile("normalizeSum");
        CSVFormat targetFormat = CSVFormat.DEFAULT
                .withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(delimiter.charAt(0));
        int tcolsNumber = 0, trowsNumber = 0;
        try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat());
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFile, charset), targetFormat)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        CSVRecord record = iterator.next();
                        if (record != null) {
                            List<String> row = new ArrayList<>();
                            if (rowNumber) {
                                row.add((trowsNumber + 1) + "");
                            }
                            for (int c = 0; c < colLen; c++) {
                                int col = cols.get(c);
                                if (col < 0 || col >= record.size()) {
                                    row.add(null);
                                } else {
                                    double v = doubleValue(record.get(col));
                                    v = v * k[c];
                                    row.add(DoubleTools.scale(v, scale) + "");
                                }
                            }
                            csvPrinter.printRecord(row);
                            trowsNumber++;
                        }
                    } catch (Exception e) {  // skip  bad lines
                        MyBoxLog.error(e);
                    }
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(charset)
                    .setDelimiter(delimiter).setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(trowsNumber);
            return targetData;
        } else {
            return null;
        }
    }

    @Override
    public DataFileCSV normalizeZscore(List<Integer> cols, boolean rowNumber, boolean colName) {
        if (file == null || !file.exists() || file.length() == 0 || cols == null || cols.isEmpty()) {
            return null;
        }
        int colLen = cols.size();
        int tcolsNumber = 0, trowsNumber = 0;
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
                                sum[c] += Math.abs(v);
                            }
                            trowsNumber++;
                        }
                    } catch (Exception e) {  // skip  bad lines
                        MyBoxLog.error(e);
                    }
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        double[] mean = new double[colLen];
        for (int c = 0; c < colLen; c++) {
            mean[c] = sum[c] / trowsNumber;
        }
        double[] variance = new double[colLen];
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
                                variance[c] += Math.pow(v - mean[c], 2);
                            }
                        }
                    } catch (Exception e) {  // skip  bad lines
                        MyBoxLog.error(e);
                    }
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        for (int c = 0; c < colLen; c++) {
            variance[c] = Math.sqrt(variance[c] / trowsNumber);
        }
        File csvFile = tmpFile("normalizeZscore");
        CSVFormat targetFormat = CSVFormat.DEFAULT
                .withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(delimiter.charAt(0));
        try ( CSVParser parser = CSVParser.parse(file, charset, cvsFormat());
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFile, charset), targetFormat)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                trowsNumber = 0;
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        CSVRecord record = iterator.next();
                        if (record != null) {
                            List<String> row = new ArrayList<>();
                            if (rowNumber) {
                                row.add((trowsNumber + 1) + "");
                            }
                            for (int c = 0; c < colLen; c++) {
                                int col = cols.get(c);
                                if (col < 0 || col >= record.size()) {
                                    row.add(null);
                                } else {
                                    double v = doubleValue(record.get(col));
                                    v = (v - mean[c]) / variance[c];
                                    row.add(DoubleTools.scale(v, scale) + "");
                                }
                            }
                            csvPrinter.printRecord(row);
                            trowsNumber++;
                        }
                    } catch (Exception e) {  // skip  bad lines
                        MyBoxLog.error(e);
                    }
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(charset)
                    .setDelimiter(delimiter).setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(trowsNumber);
            return targetData;
        } else {
            return null;
        }
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
