package mara.mybox.data2d;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.ControlDataConvert;
import mara.mybox.data.DoubleStatistic;
import mara.mybox.data2d.Data2DReader.Operation;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public abstract class Data2D_Operations extends Data2D_Edit {

    public boolean export(ControlDataConvert convertController, List<Integer> cols) {
        if (convertController == null || cols == null || cols.isEmpty()) {
            return false;
        }
        Data2DReader reader = Data2DReader.create(this)
                .setConvertController(convertController).setCols(cols)
                .setReaderTask(task).start(Operation.Export);
        return reader != null && !reader.isFailed();
    }

    public long writeTable(Connection conn, TableData2D targetTable, List<Integer> cols) {
        if (conn == null || targetTable == null || cols == null || cols.isEmpty()) {
            return -1;
        }
        Data2DReader reader = Data2DReader.create(this)
                .setConn(conn).setTableData2D(targetTable).setCols(cols)
                .setReaderTask(task).start(Operation.WriteTable);
        if (reader != null && !reader.isFailed()) {
            return reader.getCount();
        } else {
            return -1;
        }
    }

    public List<List<String>> allRows(List<Integer> cols, boolean rowNumber) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        Data2DReader reader = Data2DReader.create(this)
                .setCols(cols).setIncludeRowNumber(rowNumber)
                .setReaderTask(task).start(Data2DReader.Operation.ReadCols);
        if (reader == null) {
            return null;
        }
        return reader.getRows();
    }

    public DoubleStatistic[] statisticData(List<Integer> cols) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        int colLen = cols.size();
        DoubleStatistic[] sData = new DoubleStatistic[colLen];
        for (int c = 0; c < colLen; c++) {
            sData[c] = new DoubleStatistic();
        }
        Data2DReader reader = Data2DReader.create(this)
                .setStatisticData(sData).setCols(cols)
                .setReaderTask(task).start(Data2DReader.Operation.CountSumMinMax);
        if (reader == null) {
            return null;
        }
        reader = Data2DReader.create(this)
                .setStatisticData(sData).setCols(cols).setCountKewness(true)
                .setReaderTask(task).start(Data2DReader.Operation.CountVariancesKewness);
        if (reader == null) {
            return null;
        }
        return sData;
    }

    public DataFileCSV copy(List<Integer> cols, boolean includeRowNumber, boolean includeColName) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        File csvFile = tmpCSV("copy");
        CSVFormat targetFormat = CSVFormat.DEFAULT
                .withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(',');
        long tcolsNumber = 0;
        Data2DReader reader;
        try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFile, Charset.forName("UTF-8")), targetFormat)) {
            List<String> names = new ArrayList<>();
            if (includeRowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                }
            }
            if (includeColName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            reader = Data2DReader.create(this)
                    .setCsvPrinter(csvPrinter).setCols(cols).setIncludeRowNumber(includeRowNumber)
                    .setReaderTask(task).start(Data2DReader.Operation.Copy);

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(includeColName)
                    .setColsNumber(tcolsNumber).setRowsNumber(reader.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV percentage(List<String> names, List<Integer> cols, boolean withValues, boolean abs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        Data2DReader reader = Data2DReader.create(this)
                .setCols(cols).setSumAbs(abs)
                .setReaderTask(task).start(Data2DReader.Operation.PercentageSum);
        if (reader == null) {
            return null;
        }
        double[] colsSum = reader.getColValues();
        File csvFile = tmpCSV("percentage");
        CSVFormat targetFormat = CSVFormat.DEFAULT
                .withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(',');
        try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFile, Charset.forName("UTF-8")), targetFormat)) {
            csvPrinter.printRecord(names);
            List<String> row = new ArrayList<>();
            row.add(message("Count"));
            for (int c = 0; c < cols.size(); c++) {
                row.add(DoubleTools.scale(colsSum[c], scale) + "");
                if (withValues) {
                    row.add("100");
                }
            }
            csvPrinter.printRecord(row);

            reader = Data2DReader.create(this)
                    .setCsvPrinter(csvPrinter).setColValues(colsSum).setCols(cols)
                    .setWithValues(withValues).setSumAbs(abs)
                    .setReaderTask(task).start(Data2DReader.Operation.Percentage);

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(names.size()).setRowsNumber(reader.getRowIndex() + 1);
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeMinMax(List<Integer> cols, double from, double to,
            boolean rowNumber, boolean colName) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        int colLen = cols.size();
        DoubleStatistic[] sData = new DoubleStatistic[colLen];
        for (int c = 0; c < colLen; c++) {
            sData[c] = new DoubleStatistic();
        }
        Data2DReader reader = Data2DReader.create(this)
                .setStatisticData(sData).setCols(cols)
                .setReaderTask(task).start(Data2DReader.Operation.CountSumMinMax);
        if (reader == null) {
            return null;
        }
        for (int c = 0; c < colLen; c++) {
            double d = sData[c].maximum - sData[c].minimum;
            sData[c].mean = (to - from) / (d == 0 ? Double.MIN_VALUE : d);
        }
        File csvFile = tmpCSV("normalizeMinMax");
        CSVFormat targetFormat = CSVFormat.DEFAULT
                .withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(',');
        int tcolsNumber = 0;
        try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFile, Charset.forName("UTF-8")), targetFormat)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            reader = Data2DReader.create(this)
                    .setStatisticData(sData).setCols(cols).setCsvPrinter(csvPrinter)
                    .setIncludeRowNumber(rowNumber).setFrom(from)
                    .setReaderTask(task).start(Data2DReader.Operation.NormalizeMinMax);

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(reader.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeSum(List<Integer> cols, boolean rowNumber, boolean colName) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        int colLen = cols.size();
        Data2DReader reader = Data2DReader.create(this)
                .setCols(cols).setSumAbs(true)
                .setReaderTask(task).start(Data2DReader.Operation.CountSum);
        if (reader == null) {
            return null;
        }
        double[] colValues = reader.getColValues();
        for (int c = 0; c < colLen; c++) {
            if (colValues[c] == 0) {
                colValues[c] = 1d / Double.MIN_VALUE;
            } else {
                colValues[c] = 1d / colValues[c];
            }
        }
        File csvFile = tmpCSV("normalizeSum");
        CSVFormat targetFormat = CSVFormat.DEFAULT
                .withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(',');
        int tcolsNumber = 0;
        try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFile, Charset.forName("UTF-8")), targetFormat)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            reader = Data2DReader.create(this)
                    .setColValues(colValues).setCols(cols).setCsvPrinter(csvPrinter)
                    .setIncludeRowNumber(rowNumber)
                    .setReaderTask(task).start(Data2DReader.Operation.NormalizeSum);

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(reader.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeZscore(List<Integer> cols, boolean rowNumber, boolean colName) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        int colLen = cols.size();
        int tcolsNumber = 0;
        DoubleStatistic[] sData = new DoubleStatistic[colLen];
        for (int c = 0; c < colLen; c++) {
            sData[c] = new DoubleStatistic();
        }
        Data2DReader reader = Data2DReader.create(this)
                .setStatisticData(sData).setCols(cols)
                .setReaderTask(task).start(Data2DReader.Operation.CountSumMinMax);
        if (reader == null) {
            return null;
        }
        reader = Data2DReader.create(this)
                .setStatisticData(sData).setCols(cols).setCountKewness(false)
                .setReaderTask(task).start(Data2DReader.Operation.CountVariancesKewness);
        if (reader == null) {
            return null;
        }
        File csvFile = tmpCSV("normalizeZscore");
        CSVFormat targetFormat = CSVFormat.DEFAULT
                .withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(',');
        try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFile, Charset.forName("UTF-8")), targetFormat)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            reader = Data2DReader.create(this)
                    .setStatisticData(sData).setCols(cols).setCsvPrinter(csvPrinter)
                    .setIncludeRowNumber(rowNumber)
                    .setReaderTask(task).start(Data2DReader.Operation.NormalizeZscore);

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(reader.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

}
