package mara.mybox.data;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.ControlDataConvert;
import mara.mybox.data.DataFileReader.Operation;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public abstract class DataFile extends Data2D {

    @Override
    public Data2DDefinition queryDefinition(Connection conn) {
        if (conn == null || type == null || file == null) {
            return null;
        }
        return tableData2DDefinition.queryFile(conn, type, file);
    }

    @Override
    public List<String> readColumnNames() {
        if (file == null || !file.exists() || file.length() == 0) {
            hasHeader = false;
            return null;
        }
        DataFileReader reader = DataFileReader.create(this)
                .setReaderTask(task).start(Operation.ReadColumnNames);
        if (reader == null) {
            return null;
        }
        return reader.getNames();
    }

    @Override
    public long readTotal() {
        dataSize = 0;
        if (file == null || !file.exists() || file.length() == 0) {
            return 0;
        }
        DataFileReader reader = DataFileReader.create(this)
                .setReaderTask(backgroundTask).start(Operation.ReadTotal);
        if (reader != null) {
            dataSize = reader.getRowIndex();
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
        DataFileReader reader = DataFileReader.create(this)
                .setReaderTask(task).start(Operation.ReadPage);
        if (reader == null) {
            return null;
        }
        List<List<String>> rows = reader.getRows();
        if (rows != null) {
            endRowOfCurrentPage = startRowOfCurrentPage + rows.size();
        }
        return rows;
    }

    @Override
    public boolean export(ControlDataConvert convertController, List<Integer> cols) {
        if (convertController == null || file == null || !file.exists() || file.length() == 0
                || cols == null || cols.isEmpty()) {
            return false;
        }
        DataFileReader reader = DataFileReader.create(this)
                .setConvertController(convertController).setCols(cols)
                .setReaderTask(task).start(Operation.Export);
        return reader != null && !reader.isFailed();
    }

    @Override
    public List<List<String>> allRows(List<Integer> cols, boolean rowNumber) {
        if (file == null || !file.exists() || file.length() == 0
                || cols == null || cols.isEmpty()) {
            return null;
        }
        DataFileReader reader = DataFileReader.create(this)
                .setCols(cols).setIncludeRowNumber(rowNumber)
                .setReaderTask(task).start(DataFileReader.Operation.ReadCols);
        if (reader == null) {
            return null;
        }
        return reader.getRows();
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
        DataFileReader reader = DataFileReader.create(this)
                .setStatisticData(sData).setCols(cols)
                .setReaderTask(task).start(DataFileReader.Operation.CountSumMinMax);
        if (reader == null) {
            return null;
        }
        reader = DataFileReader.create(this)
                .setStatisticData(sData).setCols(cols).setCountKewness(true)
                .setReaderTask(task).start(DataFileReader.Operation.CountVariancesKewness);
        if (reader == null) {
            return null;
        }
        return sData;
    }

    @Override
    public DataFileCSV copy(List<Integer> cols, boolean includeRowNumber, boolean includeColName) {
        if (file == null || !file.exists() || file.length() == 0 || cols == null || cols.isEmpty()) {
            return null;
        }
        File csvFile = tmpCSV("copy");
        CSVFormat targetFormat = CSVFormat.DEFAULT
                .withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(',');
        long tcolsNumber = 0;
        DataFileReader reader;
        try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFile, Charset.forName("UTF-8")), targetFormat)) {
            List<String> names = new ArrayList<>();
            if (includeRowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getName());
                }
            }
            if (includeColName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            reader = DataFileReader.create(this)
                    .setCsvPrinter(csvPrinter).setCols(cols).setIncludeRowNumber(includeRowNumber)
                    .setReaderTask(task).start(DataFileReader.Operation.Copy);

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

    @Override
    public DataFileCSV percentage(List<String> names, List<Integer> cols, boolean withValues, boolean abs) {
        if (file == null || !file.exists() || file.length() == 0
                || cols == null || cols.isEmpty()) {
            return null;
        }
        DataFileReader reader = DataFileReader.create(this)
                .setCols(cols).setSumAbs(abs)
                .setReaderTask(task).start(DataFileReader.Operation.PercentageSum);
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

            reader = DataFileReader.create(this)
                    .setCsvPrinter(csvPrinter).setColValues(colsSum).setCols(cols)
                    .setWithValues(withValues).setSumAbs(abs)
                    .setReaderTask(task).start(DataFileReader.Operation.Percentage);

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

    @Override
    public DataFileCSV normalizeMinMax(List<Integer> cols, double from, double to,
            boolean rowNumber, boolean colName) {
        if (file == null || !file.exists() || file.length() == 0 || cols == null || cols.isEmpty()) {
            return null;
        }
        int colLen = cols.size();
        DoubleStatistic[] sData = new DoubleStatistic[colLen];
        for (int c = 0; c < colLen; c++) {
            sData[c] = new DoubleStatistic();
        }
        DataFileReader reader = DataFileReader.create(this)
                .setStatisticData(sData).setCols(cols)
                .setReaderTask(task).start(DataFileReader.Operation.CountSumMinMax);
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
                    names.add(columns.get(i).getName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            reader = DataFileReader.create(this)
                    .setStatisticData(sData).setCols(cols).setCsvPrinter(csvPrinter)
                    .setIncludeRowNumber(rowNumber).setFrom(from)
                    .setReaderTask(task).start(DataFileReader.Operation.NormalizeMinMax);

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

    @Override
    public DataFileCSV normalizeSum(List<Integer> cols, boolean rowNumber, boolean colName) {
        if (file == null || !file.exists() || file.length() == 0 || cols == null || cols.isEmpty()) {
            return null;
        }
        int colLen = cols.size();
        DataFileReader reader = DataFileReader.create(this)
                .setCols(cols).setSumAbs(true)
                .setReaderTask(task).start(DataFileReader.Operation.CountSum);
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
                    names.add(columns.get(i).getName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            reader = DataFileReader.create(this)
                    .setColValues(colValues).setCols(cols).setCsvPrinter(csvPrinter)
                    .setIncludeRowNumber(rowNumber)
                    .setReaderTask(task).start(DataFileReader.Operation.NormalizeSum);

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

    @Override
    public DataFileCSV normalizeZscore(List<Integer> cols, boolean rowNumber, boolean colName) {
        if (file == null || !file.exists() || file.length() == 0 || cols == null || cols.isEmpty()) {
            return null;
        }
        int colLen = cols.size();
        int tcolsNumber = 0;
        DoubleStatistic[] sData = new DoubleStatistic[colLen];
        for (int c = 0; c < colLen; c++) {
            sData[c] = new DoubleStatistic();
        }
        DataFileReader reader = DataFileReader.create(this)
                .setStatisticData(sData).setCols(cols)
                .setReaderTask(task).start(DataFileReader.Operation.CountSumMinMax);
        if (reader == null) {
            return null;
        }
        reader = DataFileReader.create(this)
                .setStatisticData(sData).setCols(cols).setCountKewness(false)
                .setReaderTask(task).start(DataFileReader.Operation.CountVariancesKewness);
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
                    names.add(columns.get(i).getName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            reader = DataFileReader.create(this)
                    .setStatisticData(sData).setCols(cols).setCsvPrinter(csvPrinter)
                    .setIncludeRowNumber(rowNumber)
                    .setReaderTask(task).start(DataFileReader.Operation.NormalizeZscore);

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
