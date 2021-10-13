package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppValues;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2021-9-6
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetText_Operations extends ControlSheetText_File {

    @Override
    protected File fileCopyCols(List<Integer> cols, boolean withNames) {
        if (sourceFile == null || cols == null || cols.isEmpty()) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset));
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, Charset.forName("utf-8")), CSVFormat.DEFAULT)) {
            if (sourceWithNames) {
                readNames(reader);
            }
            String line;
            int rowIndex = -1;
            while ((line = reader.readLine()) != null) {
                List<String> row = parseFileLine(line);
                if (row == null || row.isEmpty()) {
                    continue;
                }
                if (++rowIndex < startRowOfCurrentPage || rowIndex >= endRowOfCurrentPage) {
                    List<String> values = new ArrayList<>();
                    for (int c : cols) {
                        if (c >= row.size()) {
                            break;
                        }
                        String d = row.get(c);
                        d = d == null ? "" : d;
                        values.add(d);
                    }
                    csvPrinter.printRecord(values);
                } else if (rowIndex == startRowOfCurrentPage) {
                    copyPageData(csvPrinter, cols);
                }
            }
            if (rowIndex < 0) {
                copyPageData(csvPrinter, cols);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
        }
        return tmpFile;
    }

    @Override
    protected File fileSetCols(List<Integer> cols, String value) {
        if (sourceFile == null || cols == null || cols.isEmpty()) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, sourceCharset, false))) {
            String delimiter = TextTools.delimiterValue(sourceDelimiterName);
            if (sourceWithNames) {
                TextFileTools.writeLine(writer, readNames(reader), delimiter);
            }
            String line;
            Random random = null;
            while ((line = reader.readLine()) != null) {
                List<String> row = parseFileLine(line);
                if (row == null || row.isEmpty()) {
                    continue;
                }
                String rowString = "";
                int len = row.size();
                for (int c = 0; c < len; c++) {
                    if (cols.contains(c)) {
                        String v = value;
                        if (AppValues.MyBoxRandomFlag.equals(value)) {
                            if (random == null) {
                                random = new Random();
                            }
                            v = columns.get(c).random(random, maxRandom, scale);
                        }
                        rowString += v;
                    } else {
                        rowString += row.get(c);
                    }
                    if (c < len - 1) {
                        rowString += delimiter;
                    }
                }
                writer.write(rowString);
            }
            writer.flush();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
        }
        return tmpFile;
    }

    @Override
    public File filePaste(ControlSheetCSV sourceController, int row, int col, boolean enlarge) {
        if (sourceController == null || sourceController.sourceFile == null || sourceFile == null || row < 0 || col < 0) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( CSVParser sourceParser = CSVParser.parse(sourceController.sourceFile, sourceController.sourceCharset, sourceController.sourceCsvFormat);
                 BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, sourceCharset, false))) {
            String delimiter = TextTools.delimiterValue(sourceDelimiterName);
            if (sourceWithNames) {
                TextFileTools.writeLine(writer, readNames(reader), delimiter);
            }
            Iterator<CSVRecord> sourceIterator = sourceParser.iterator();
            List<String> values = new ArrayList<>();
            int colsSize;
            int targetColsSize = columns.size();
            if (enlarge && col + sourceController.colsNumber > targetColsSize) {
                colsSize = col + sourceController.colsNumber;
            } else {
                colsSize = targetColsSize;
            }
            long rowsIndex = 0, sourceRowsSize = sourceController.rowsTotal(), targetRowsSize = rowsTotal(), rowsSize;
            if (enlarge && row + sourceRowsSize > targetRowsSize) {
                rowsSize = row + sourceRowsSize;
            } else {
                rowsSize = targetRowsSize;
            }
            String line;
            while (rowsIndex < row && rowsIndex < rowsSize && (line = reader.readLine()) != null) {
                List<String> rowData = parseFileLine(line);
                if (rowData == null || rowData.isEmpty()) {
                    continue;
                }
                makeRow(rowData, colsSize, values);
                TextFileTools.writeLine(writer, values, delimiter);
                rowsIndex++;
            }
            while (rowsIndex >= row && rowsIndex < rowsSize && rowsIndex < row + sourceRowsSize && sourceIterator.hasNext()) {
                List<String> rowData = TextTools.parseLine(reader.readLine(), sourceDelimiterName);
                makeRow(sourceIterator.next(), rowData, col, colsSize, values);
                TextFileTools.writeLine(writer, values, delimiter);
                rowsIndex++;
            }
            while (rowsIndex < rowsSize && (line = reader.readLine()) != null) {
                List<String> rowData = parseFileLine(line);
                if (rowData == null || rowData.isEmpty()) {
                    continue;
                }
                makeRow(rowData, colsSize, values);
                TextFileTools.writeLine(writer, values, delimiter);
                rowsIndex++;
            }
            writer.flush();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return tmpFile;
    }

    protected void makeRow(List<String> rowData, int colsSize, List<String> values) {
        try {
            values.clear();
            int colIndex = 0;
            for (; colIndex < Math.min(colsSize, rowData.size()); colIndex++) {
                values.add(rowData.get(colIndex));
            }
            for (; colIndex < colsSize; colIndex++) {
                values.add(defaultColValue);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void makeRow(CSVRecord sourceRecord, List<String> rowData,
            int col, int colsSize, List<String> values) {
        try {
            values.clear();
            int colsIndex = 0;
            while (colsIndex < col && colsIndex < colsSize) {
                if (rowData != null && colsIndex < rowData.size()) {
                    values.add(rowData.get(colsIndex));
                } else {
                    values.add(defaultColValue);
                }
                colsIndex++;
            }
            while (colsIndex >= col && colsIndex < colsSize && colsIndex < col + sourceRecord.size()) {
                values.add(sourceRecord.get(colsIndex - col));
                colsIndex++;
            }
            while (colsIndex < colsSize) {
                if (rowData != null && colsIndex < rowData.size()) {
                    values.add(rowData.get(colsIndex));
                } else {
                    values.add(defaultColValue);
                }
                colsIndex++;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected File fileSortCol(int col, boolean asc) {
        if (sourceFile == null || col < 0) {
            return null;
        }
        List<List<String>> records = new ArrayList<>();
        List<String> names = null;
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset))) {
            if (sourceWithNames) {
                names = readNames(reader);
            }
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> row = TextTools.parseLine(line, sourceDelimiterName);
                if (row == null || row.isEmpty()) {
                    continue;
                }
                records.add(row);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        if (records.isEmpty()) {
            return null;
        }
        Collections.sort(records, new Comparator<List<String>>() {
            @Override
            public int compare(List<String> row1, List<String> row2) {
                try {
                    ColumnDefinition column = columns.get(col);
                    int v = column.compare(row1.get(col), row2.get(col));
                    return asc ? v : -v;
                } catch (Exception e) {
                    return 0;
                }
            }
        });
        File tmpFile = TmpFileTools.getTempFile();
        try ( BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, sourceCharset, false))) {
            String delimiter = TextTools.delimiterValue(sourceDelimiterName);
            if (sourceWithNames) {
                TextFileTools.writeLine(writer, names, delimiter);
            }
            for (List<String> record : records) {
                TextFileTools.writeLine(writer, record, delimiter);
            }
            writer.flush();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return tmpFile;
    }

    @Override
    protected File fileAddCols(int col, boolean left, int number) {
        if (sourceFile == null || number < 1) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, sourceCharset, false))) {
            String delimiter = TextTools.delimiterValue(sourceDelimiterName);
            if (sourceWithNames) {
                if (readNames(reader) != null) {
                    TextFileTools.writeLine(writer, columnNames(), delimiter);
                }
            }
            List<String> newValues = new ArrayList<>();
            for (int i = 0; i < number; i++) {
                newValues.add(defaultColValue);
            }
            List<String> values = new ArrayList<>();
            int index;
            if (col < 0) {
                index = 0;
            } else {
                index = col + (left ? 0 : 1);
            }
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> row = TextTools.parseLine(line, sourceDelimiterName);
                if (row == null || row.isEmpty()) {
                    continue;
                }
                values.addAll(row);
                values.addAll(index, newValues);
                TextFileTools.writeLine(writer, values, delimiter);
                values.clear();
            }
            writer.flush();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return tmpFile;
    }

    @Override
    protected File fileDeleteAll(boolean keepCols) {
        if (sourceFile == null) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, sourceCharset, false))) {
            if (sourceWithNames && keepCols) {
                String delimiter = TextTools.delimiterValue(sourceDelimiterName);
                TextFileTools.writeLine(writer, columnNames(), delimiter);
                writer.flush();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return tmpFile;
    }

    @Override
    protected File fileDeleteCols(List<Integer> cols) {
        if (sourceFile == null || cols == null || cols.isEmpty()) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, sourceCharset, false))) {
            String delimiter = TextTools.delimiterValue(sourceDelimiterName);
            List<String> colsNames = columnNames();
            if (sourceWithNames) {
                TextFileTools.writeLine(writer, colsNames, delimiter);
            }
            List<String> values = new ArrayList<>();
            List<Integer> indexs = new ArrayList<>();
            for (int col = 0; col < colsNames.size(); ++col) {
                if (!cols.contains(col)) {
                    indexs.add(col);
                }
            }
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> row = TextTools.parseLine(line, sourceDelimiterName);
                if (row == null || row.isEmpty()) {
                    continue;
                }
                for (int i = 0; i < indexs.size(); ++i) {
                    values.add(row.get(indexs.get(i)));
                }
                TextFileTools.writeLine(writer, values, delimiter);
                values.clear();
            }
            writer.flush();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return tmpFile;
    }

}
