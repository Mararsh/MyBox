package mara.mybox.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import mara.mybox.controller.ControlDataConvert;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class DataFileText extends DataFile {

    public DataFileText() {
        type = Type.Texts;
    }

    public void setOptions(boolean hasHeader, Charset charset, String delimiter) {
        options = new HashMap<>();
        options.put("hasHeader", hasHeader);
        options.put("charset", charset);
        options.put("delimiter", delimiter);
    }

    @Override
    public void applyOptions() {
        try {
            if (options == null) {
                return;
            }
            if (options.containsKey("hasHeader")) {
                hasHeader = (boolean) (options.get("hasHeader"));
            }
            if (options.containsKey("charset")) {
                charset = (Charset) (options.get("charset"));
            }
            if (options.containsKey("delimiter")) {
                delimiter = (String) (options.get("delimiter"));
            }
        } catch (Exception e) {
        }
    }

    @Override
    public boolean checkForLoad() {
        if (charset == null && file != null) {
            charset = TextFileTools.charset(file);
        }
        if (charset == null) {
            charset = Charset.forName("UTF-8");
        }
        if (delimiter == null || delimiter.isEmpty()) {
            delimiter = guessDelimiter();
        }
        if (delimiter == null || delimiter.isEmpty()) {
            delimiter = ",";
        }
        return super.checkForLoad();
    }

    public String guessDelimiter() {
        String[] values = {",", " ", "    ", "        ", "\t", "|", "@",
            "#", ";", ":", "*", "%", "$", "_", "&", "-", "=", "!", "\"",
            "'", "<", ">"};
        return guessDelimiter(values);
    }

    public String guessDelimiter(String[] values) {
        if (file == null || values == null) {
            return null;
        }
        if (charset == null) {
            charset = TextFileTools.charset(file);
        }
        if (charset == null) {
            charset = Charset.forName("UTF-8");
        }
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            String line1 = reader.readLine();
            if (line1 == null) {
                return null;
            }
            int[] count1 = new int[values.length];
            int maxCount1 = 0, maxCountIndex1 = -1;
            for (int i = 0; i < values.length; i++) {
                count1[i] = FindReplaceString.count(line1, values[i]);
//                MyBoxLog.console(">>" + values[i] + "<<<   " + count1[i]);
                if (count1[i] > maxCount1) {
                    maxCount1 = count1[i];
                    maxCountIndex1 = i;
                }
            }
//            MyBoxLog.console(maxCount1);
            String line2 = reader.readLine();
            if (line2 == null) {
                if (maxCountIndex1 >= 0) {
                    return values[maxCountIndex1];
                } else {
                    hasHeader = false;
                    return null;
                }
            }
            int[] count2 = new int[values.length];
            int maxCount2 = 0, maxCountIndex2 = -1;
            for (int i = 0; i < values.length; i++) {
                count2[i] = FindReplaceString.count(line2, values[i]);
//                MyBoxLog.console(">>" + values[i] + "<<<   " + count1[i]);
                if (count1[i] == count2[i] && count2[i] > maxCount2) {
                    maxCount2 = count2[i];
                    maxCountIndex2 = i;
                }
            }
//            MyBoxLog.console(maxCount2);
            if (maxCountIndex2 >= 0) {
                return values[maxCountIndex2];
            } else {
                if (maxCountIndex1 >= 0) {
                    return values[maxCountIndex1];
                } else {
                    hasHeader = false;
                    return null;
                }
            }
        } catch (Exception e) {
        }
        hasHeader = false;
        return null;
    }

    @Override
    public List<String> readColumns() {
        if (file == null || !file.exists() || file.length() == 0) {
            return null;
        }
        List<String> names = null;
        checkForLoad();
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            List<String> values = readValidLine(reader);
            if (hasHeader && StringTools.noDuplicated(values, true)) {
                names = values;
            } else {
                hasHeader = false;
                names = new ArrayList<>();
                for (int i = 1; i <= values.size(); i++) {
                    names.add(colPrefix() + i);
                }
            }
            if (!hasHeader && names != null) {
                int len = names.size();
                names = new ArrayList<>();
                for (int i = 1; i <= len; i++) {
                    names.add(colPrefix() + i);
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

    protected List<String> parseFileLine(String line) {
        return TextTools.parseLine(line, delimiter);
    }

    protected List<String> readValidLine(BufferedReader reader) {
        List<String> names = null;
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                names = parseFileLine(line);
                if (names != null && !names.isEmpty()) {
                    break;
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
        if (file == null || !file.exists() || file.length() == 0) {
            return 0;
        }
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (backgroundTask == null || backgroundTask.isCancelled()) {
                    dataSize = 0;
                    break;
                }
                List<String> row = parseFileLine(line);
                if (row != null && !row.isEmpty()) {
                    dataSize++;
                }
            }
        } catch (Exception e) {
            if (backgroundTask != null) {
                backgroundTask.setError(e.toString());
            }
            MyBoxLog.console(e);
            return -1;
        }
        if (hasHeader && dataSize > 0) {
            dataSize--;
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
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            if (hasHeader) {
                readValidLine(reader);
            }
            String line;
            long rowIndex = -1;
            long end = startRowOfCurrentPage + pageSize;
            int columnsNumber = columnsNumber();
            while ((line = reader.readLine()) != null && task != null && !task.isCancelled()) {
                List<String> record = parseFileLine(line);
                if (record == null || record.isEmpty()) {
                    continue;
                }
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
        if (targetData == null || !(targetData instanceof DataFileText)) {
            return false;
        }
        DataFileText targetTextFile = (DataFileText) targetData;
        File tmpFile = TmpFileTools.getTempFile();
        File tFile = targetTextFile.getFile();
        if (tFile == null) {
            return false;
        }
        targetTextFile.checkForLoad();
        Charset tCharset = targetTextFile.getCharset();
        String tDelimiter = targetTextFile.getDelimiter();
        checkForLoad();
        boolean tHasHeader = targetTextFile.isHasHeader();
        if (file != null && file.exists() && file.length() > 0) {
            try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset));
                     BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, tCharset, false))) {
                List<String> colsNames = columnNames();
                if (hasHeader) {
                    readValidLine(reader);
                }
                if (tHasHeader && colsNames != null) {
                    TextFileTools.writeLine(writer, colsNames, tDelimiter);
                } else {
                    targetTextFile.setHasHeader(false);
                }
                long rowIndex = -1;
                String line;
                while ((line = reader.readLine()) != null && task != null && !task.isCancelled()) {
                    List<String> row = parseFileLine(line);
                    if (row == null || row.isEmpty()) {
                        continue;
                    }
                    if (++rowIndex < startRowOfCurrentPage || rowIndex >= endRowOfCurrentPage) {
                        TextFileTools.writeLine(writer, fileRow(row), tDelimiter);
                    } else if (rowIndex == startRowOfCurrentPage) {
                        writePageData(writer, tDelimiter);
                    }
                }
                if (rowIndex < 0) {
                    writePageData(writer, tDelimiter);
                }
                writer.flush();
            } catch (Exception e) {
                MyBoxLog.console(e);
                if (task != null) {
                    task.setError(e.toString());
                }
                return false;
            }
        } else {
            try ( BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, tCharset, false))) {
                List<String> colsNames = columnNames();
                if (tHasHeader && colsNames != null) {
                    TextFileTools.writeLine(writer, colsNames, tDelimiter);
                } else {
                    targetTextFile.setHasHeader(false);
                }
                writePageData(writer, tDelimiter);
            } catch (Exception e) {
                MyBoxLog.console(e);
                if (task != null) {
                    task.setError(e.toString());
                }
                return false;
            }
        }
        return FileTools.rename(tmpFile, tFile, false);
    }

    public boolean writePageData(BufferedWriter writer, String delimiter) {
        try {
            if (writer == null || delimiter == null) {
                return false;
            }
            if (!isColumnsValid()) {
                return true;
            }
            for (int r = 0; r < tableRowsNumber(); r++) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                TextFileTools.writeLine(writer, tableRowWithoutNumber(r), delimiter);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    public File tmpFile(List<String> cols, List<List<String>> data) {
        try {
            if (cols == null || cols.isEmpty()) {
                if (data == null || data.isEmpty()) {
                    return null;
                }
            }
            File tmpFile = TmpFileTools.txtFile();
            String fDelimiter = ",";
            try ( BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, Charset.forName("UTF-8"), false))) {
                if (cols != null && !cols.isEmpty()) {
                    TextFileTools.writeLine(writer, cols, fDelimiter);
                }
                if (data != null) {
                    for (int r = 0; r < data.size(); r++) {
                        if (task != null && task.isCancelled()) {
                            break;
                        }
                        TextFileTools.writeLine(writer, data.get(r), fDelimiter);
                    }
                }
            }
            return tmpFile;
        } catch (Exception e) {
            MyBoxLog.console(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
    }

    @Override
    public boolean export(ControlDataConvert convertController, List<Integer> colIndices) {
        if (convertController == null || file == null || !file.exists() || file.length() == 0
                || colIndices == null || colIndices.isEmpty()) {
            return false;
        }
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            if (hasHeader) {
                readValidLine(reader);
            }
            String line;
            while ((line = reader.readLine()) != null && task != null && !task.isCancelled()) {
                List<String> dataRow = parseFileLine(line);
                if (dataRow == null || dataRow.isEmpty()) {
                    continue;
                }
                export(convertController, colIndices, dataRow);
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
        if (file == null || !file.exists() || file.length() == 0
                || cols == null || cols.isEmpty()) {
            return null;
        }
        List<List<String>> rows = new ArrayList<>();
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            if (hasHeader) {
                readValidLine(reader);
            }
            String line;
            while ((line = reader.readLine()) != null && task != null && !task.isCancelled()) {
                List<String> record = parseFileLine(line);
                if (record == null || record.isEmpty()) {
                    continue;
                }
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
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return rows;
    }

    @Override
    public DoubleStatistic[] statisticData(List<Integer> cols) {
        if (file == null || !file.exists() || file.length() == 0
                || cols == null || cols.isEmpty()) {
            return null;
        }
        int colLen = cols.size();
        DoubleStatistic[] sData = new DoubleStatistic[colLen];
        for (int c = 0; c < colLen; c++) {
            sData[c] = new DoubleStatistic();
        }
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            if (hasHeader) {
                readValidLine(reader);
            }
            String line;
            while ((line = reader.readLine()) != null && task != null && !task.isCancelled()) {
                List<String> record = parseFileLine(line);
                if (record == null || record.isEmpty()) {
                    continue;
                }
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
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            if (hasHeader) {
                readValidLine(reader);
            }
            String line;
            while ((line = reader.readLine()) != null && task != null && !task.isCancelled()) {
                List<String> record = parseFileLine(line);
                if (record == null || record.isEmpty()) {
                    continue;
                }
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
    public boolean setValue(List<Integer> cols, String value) {
        if (file == null || !file.exists() || file.length() == 0
                || cols == null || cols.isEmpty()) {
            return false;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, charset, false))) {
            List<String> names = columnNames();
            if (hasHeader && names != null) {
                readValidLine(reader);
                TextFileTools.writeLine(writer, names, delimiter);
            }
            String line;
            boolean isRandom = "MyBox##random".equals(value);
            Random random = new Random();
            while ((line = reader.readLine()) != null && task != null && !task.isCancelled()) {
                List<String> record = parseFileLine(line);
                if (record == null || record.isEmpty()) {
                    continue;
                }
                List<String> row = new ArrayList<>();
                for (int i = 0; i < columns.size(); i++) {
                    if (cols.contains(i)) {
                        if (isRandom) {
                            row.add(random(random, i));
                        } else {
                            row.add(value);
                        }
                    } else if (i < record.size()) {
                        row.add(record.get(i));
                    } else {
                        row.add(null);
                    }
                }
                TextFileTools.writeLine(writer, row, delimiter);
            }
        } catch (Exception e) {
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
        File csvFile = TmpFileTools.getPathTempFile(AppPaths.getGeneratedPath(), ".csv");
        CSVFormat targetFormat = CSVFormat.DEFAULT
                .withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(',');
        int tcolsNumber = 0, trowsNumber = 0;
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset));
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFile, charset), targetFormat)) {
            if (hasHeader) {
                readValidLine(reader);
            }
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
            String line;
            while ((line = reader.readLine()) != null && task != null && !task.isCancelled()) {
                List<String> dataRow = parseFileLine(line);
                if (dataRow == null || dataRow.isEmpty()) {
                    continue;
                }
                List<String> row = new ArrayList<>();
                if (rowNumber) {
                    row.add((trowsNumber + 1) + "");
                }
                for (int i : cols) {
                    if (i >= 0 && i < dataRow.size()) {
                        row.add(dataRow.get(i));
                    } else {
                        row.add(null);
                    }
                }
                csvPrinter.printRecord(row);
                trowsNumber++;
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
                    .setDelimiter(",").setHasHeader(colName)
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
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            if (hasHeader) {
                readValidLine(reader);
            }
            String line;
            while ((line = reader.readLine()) != null && task != null && !task.isCancelled()) {
                List<String> dataRow = parseFileLine(line);
                if (dataRow == null || dataRow.isEmpty()) {
                    continue;
                }
                for (int c = 0; c < colLen; c++) {
                    int col = cols.get(c);
                    if (col < 0 || col >= dataRow.size()) {
                        continue;
                    }
                    double v = doubleValue(dataRow.get(col));
                    sum[c] += v;
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        File csvFile = TmpFileTools.getPathTempFile(AppPaths.getGeneratedPath(), ".csv");
        CSVFormat targetFormat = CSVFormat.DEFAULT
                .withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(',');
        int trowsNumber = 0;
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset));
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

            if (hasHeader) {
                readValidLine(reader);
            }
            String line;
            while ((line = reader.readLine()) != null && task != null && !task.isCancelled()) {
                List<String> dataRow = parseFileLine(line);
                if (dataRow == null || dataRow.isEmpty()) {
                    continue;
                }
                row = new ArrayList<>();
                row.add(trowsNumber++ + "");
                for (int c = 0; c < colLen; c++) {
                    int col = cols.get(c);
                    double v = 0;
                    if (col >= 0 && col < dataRow.size()) {
                        v = doubleValue(dataRow.get(col));
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
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        if (csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(charset)
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(names.size()).setRowsNumber(trowsNumber);
            return targetData;
        } else {
            return null;
        }
    }

    @Override
    public List<List<String>> allRows(List<Integer> cols, boolean rowNumber) {
        if (file == null || !file.exists() || file.length() == 0
                || cols == null || cols.isEmpty()) {
            return null;
        }
        List<List<String>> rows = new ArrayList<>();
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            if (hasHeader) {
                readValidLine(reader);
            }
            String line;
            int index = 1;
            while ((line = reader.readLine()) != null && task != null && !task.isCancelled()) {
                List<String> record = parseFileLine(line);
                if (record == null || record.isEmpty()) {
                    continue;
                }
                List<String> row = new ArrayList<>();
                if (rowNumber) {
                    row.add(index++ + "");
                }
                for (int col : cols) {
                    if (col >= 0 && col < record.size()) {
                        row.add(record.get(col));
                    } else {
                        row.add(null);
                    }
                }
                rows.add(row);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return rows;
    }

    public static DataFileText toText(DataFileCSV csvData) {
        if (csvData == null) {
            return null;
        }
        File txtFile = TmpFileTools.txtFile();
        if (FileCopyTools.copyFile(csvData.getFile(), txtFile)) {
            DataFileText targetData = new DataFileText();
            targetData.cloneAll(csvData);
            targetData.setType(Type.Texts).setFile(txtFile);
            return targetData;
        } else {
            return null;
        }

    }

}
