package mara.mybox.controller;

import java.io.File;
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
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppValues;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetCSV_Operations extends ControlSheetCSV_File {

    @Override
    protected File fileCopyCols(List<Integer> cols, boolean withNames) {
        if (sourceFile == null || cols == null || cols.isEmpty()) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat);
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, Charset.forName("utf-8")), CSVFormat.DEFAULT)) {
            if (withNames) {
                List<String> names = new ArrayList<>();
                for (int c : cols) {
                    names.add(colsCheck[c].getText());
                }
                csvPrinter.printRecord(names);
            }
            int index = -1;
            for (CSVRecord record : parser) {
                if (++index < startRowOfCurrentPage || index >= endRowOfCurrentPage) {
                    List<String> values = new ArrayList<>();
                    for (int c : cols) {
                        if (c >= record.size()) {
                            break;
                        }
                        String d = record.get(c);
                        d = d == null ? "" : d;
                        values.add(d);
                    }
                    csvPrinter.printRecord(values);
                } else if (index == startRowOfCurrentPage) {
                    copyPageData(csvPrinter, cols);
                }
            }
            if (index < 0) {
                copyPageData(csvPrinter, cols);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return tmpFile;
    }

    @Override
    protected File fileSetCols(List<Integer> cols, String value) {
        if (sourceFile == null || cols == null || cols.isEmpty()) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat);
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, sourceCharset), sourceCsvFormat)) {
            if (sourceWithNames) {
                csvPrinter.printRecord(columnNames());
            }
            List<String> values = new ArrayList<>();
            Random random = null;
            for (CSVRecord record : parser) {
                for (int c = 0; c < record.size(); c++) {
                    if (cols.contains(c)) {
                        String v = value;
                        if (AppValues.MyBoxRandomFlag.equals(value)) {
                            if (random == null) {
                                random = new Random();
                            }
                            v = columns.get(c).random(random, maxRandom, scale);
                        }
                        values.add(v);
                    } else {
                        values.add(record.get(c));
                    }
                }
                csvPrinter.printRecord(values);
                values.clear();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
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
                 CSVParser targetParser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat);
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, sourceCharset), sourceCsvFormat)) {
            if (sourceWithNames) {
                csvPrinter.printRecord(columnNames());
            }
            Iterator<CSVRecord> sourceIterator = sourceParser.iterator();
            Iterator<CSVRecord> targetIterator = targetParser.iterator();
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
            while (rowsIndex < row && rowsIndex < rowsSize && targetIterator.hasNext()) {
                writeTarget(csvPrinter, targetIterator.next(), colsSize, values);
                rowsIndex++;
            }
            while (rowsIndex >= row && rowsIndex < rowsSize && rowsIndex < row + sourceRowsSize && sourceIterator.hasNext()) {
                writeSource(csvPrinter, sourceIterator.next(),
                        targetIterator.hasNext() ? targetIterator.next() : null, col, colsSize, values);
                rowsIndex++;
            }
            while (rowsIndex < rowsSize && targetIterator.hasNext()) {
                writeTarget(csvPrinter, targetIterator.next(), colsSize, values);
                rowsIndex++;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return tmpFile;
    }

    protected void writeTarget(CSVPrinter csvPrinter, CSVRecord record, int colsSize, List<String> values) {
        try {
            values.clear();
            int colIndex = 0;
            for (; colIndex < Math.min(colsSize, record.size()); colIndex++) {
                values.add(record.get(colIndex));
            }
            for (; colIndex < colsSize; colIndex++) {
                values.add(defaultColValue);
            }
            csvPrinter.printRecord(values);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void writeSource(CSVPrinter csvPrinter, CSVRecord sourceRecord, CSVRecord targetRecord,
            int col, int colsSize, List<String> values) {
        try {
            values.clear();
            int colsIndex = 0;
            while (colsIndex < col && colsIndex < colsSize) {
                if (targetRecord != null && colsIndex < targetRecord.size()) {
                    values.add(targetRecord.get(colsIndex));
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
                if (targetRecord != null && colsIndex < targetRecord.size()) {
                    values.add(targetRecord.get(colsIndex));
                } else {
                    values.add(defaultColValue);
                }
                colsIndex++;
            }
            csvPrinter.printRecord(values);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected File fileSortCol(int col, boolean asc) {
        if (sourceFile == null || col < 0) {
            return null;
        }
        List<CSVRecord> records;
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
            records = parser.getRecords();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        if (records == null || records.isEmpty()) {
            return null;
        }
        Collections.sort(records, new Comparator<CSVRecord>() {
            @Override
            public int compare(CSVRecord row1, CSVRecord row2) {
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
        try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, sourceCharset), sourceCsvFormat)) {
            if (sourceWithNames) {
                csvPrinter.printRecord(columnNames());
            }
            for (CSVRecord record : records) {
                csvPrinter.printRecord(record);
            }
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
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat);
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, sourceCharset), sourceCsvFormat)) {
            if (sourceWithNames) {
                csvPrinter.printRecord(columnNames());
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
            for (CSVRecord record : parser) {
                for (int c = 0; c < record.size(); c++) {
                    values.add(record.get(c));
                }
                values.addAll(index, newValues);
                csvPrinter.printRecord(values);
                values.clear();
            }
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
        try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, sourceCharset), sourceCsvFormat)) {
            if (sourceWithNames && keepCols) {
                csvPrinter.printRecord(columnNames());
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
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat);
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, sourceCharset), sourceCsvFormat)) {
            List<String> colsNames = columnNames();
            if (sourceWithNames) {
                csvPrinter.printRecord(colsNames);
            }
            List<String> values = new ArrayList<>();
            List<Integer> indexs = new ArrayList<>();
            for (int col = 0; col < colsNames.size(); ++col) {
                if (!cols.contains(col)) {
                    indexs.add(col);
                }
            }
            for (CSVRecord record : parser) {
                for (int i = 0; i < indexs.size(); ++i) {
                    values.add(record.get(indexs.get(i)));
                }
                csvPrinter.printRecord(values);
                values.clear();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return tmpFile;
    }

}
