package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 */
public class ControlSheetCSV extends ControlSheetFile {

    protected Charset sourceCharset, targetCharset;
    protected CSVFormat sourceCsvFormat;
    protected char sourceCsvDelimiter, targetCsvDelimiter;
    protected boolean autoDetermineSourceCharset, targetWithNames;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.CSV);
    }

    @Override
    protected boolean readDataDefinition() {
        try ( Connection conn = DerbyBase.getConnection()) {
            dataName = sourceFile.getAbsolutePath();
            dataDefinition = tableDataDefinition.read(conn, dataType, dataName);
            if (userSavedDataDefinition && dataDefinition != null) {
                sourceCsvDelimiter = dataDefinition.getDelimiter().charAt(0);
                sourceWithNames = dataDefinition.isHasHeader();
                sourceCharset = Charset.forName(dataDefinition.getCharset());
            }
            if (dataDefinition == null) {
                dataDefinition = DataDefinition.create().setDataName(dataName).setDataType(dataType)
                        .setCharset(sourceCharset.name()).setHasHeader(sourceWithNames)
                        .setDelimiter(sourceCsvDelimiter + "");
                tableDataDefinition.insertData(conn, dataDefinition);
                conn.commit();
            } else {
                if (!userSavedDataDefinition) {
                    dataDefinition.setCharset(sourceCharset.name())
                            .setDelimiter(sourceCsvDelimiter + "")
                            .setHasHeader(sourceWithNames);
                    tableDataDefinition.updateData(conn, dataDefinition);
                    conn.commit();
                }
                savedColumns = tableDataColumn.read(conn, dataDefinition.getDfid());
            }
            userSavedDataDefinition = true;
        } catch (Exception e) {
            loadError = e.toString();
            return false;
        }
        return dataDefinition != null && dataDefinition.getDfid() >= 0;
    }

    @Override
    protected boolean readColumns() {
        columns = new ArrayList<>();
        sourceCsvFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(sourceCsvDelimiter);
        if (!sourceWithNames) {
            return true;
        }
        sourceCsvFormat = sourceCsvFormat.withFirstRecordAsHeader();
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
            List<String> names = parser.getHeaderNames();
            if (names == null) {
                sourceWithNames = false;
                return true;
            }
            for (String name : names) {
                boolean found = false;
                if (savedColumns != null) {
                    for (ColumnDefinition def : savedColumns) {
                        if (def.getName().equals(name)) {
                            columns.add(def);
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    ColumnDefinition column = new ColumnDefinition(name, ColumnDefinition.ColumnType.String);
                    columns.add(column);
                }
            }
            if (ColumnDefinition.valid(this, columns)) {
                tableDataColumn.save(dataDefinition.getDfid(), columns);
            }
        } catch (Exception e) {
            loadError = e.toString();
            return false;
        }
        return true;
    }

    @Override
    protected boolean readTotal() {
        sourceCsvFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(sourceCsvDelimiter);
        if (sourceWithNames) {
            sourceCsvFormat = sourceCsvFormat.withFirstRecordAsHeader();
        }
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                while (backgroundTask != null && !backgroundTask.isCancelled() && iterator.hasNext()) {
                    iterator.next();
                    totalSize++;
                }
                if (backgroundTask == null || backgroundTask.isCancelled()) {
                    totalSize = 0;
                }
            }
        } catch (Exception e) {
            loadError = e.toString();
            return false;
        }
        return true;
    }

    @Override
    protected String[][] readPageData() {
        if (currentPageStart < 1) {
            currentPageStart = 1;
        }
        long end = currentPageStart + pageSize;
        String[][] data = null;
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
            int rowIndex = 0, maxCol = 0;
            List<List<String>> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                if (++rowIndex < currentPageStart) {
                    continue;
                }
                if (rowIndex >= end) {
                    break;
                }
                List<String> row = new ArrayList<>();
                for (int i = 0; i < record.size(); i++) {
                    row.add(record.get(i));
                }
                rows.add(row);
                if (maxCol < row.size()) {
                    maxCol = row.size();
                }
            }
            if (!rows.isEmpty() && maxCol > 0) {
                data = new String[rows.size()][maxCol];
                for (int row = 0; row < rows.size(); row++) {
                    List<String> rowData = rows.get(row);
                    for (int col = 0; col < rowData.size(); col++) {
                        data[row][col] = rowData.get(col);
                    }
                }
            }
        } catch (Exception e) {
            loadError = e.toString();
            MyBoxLog.console(loadError);
        }
        if (data == null) {
            currentPageEnd = currentPageStart;
        } else {
            currentPageEnd = currentPageStart + data.length;
        }
        return data;
    }

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
            if (parser.iterator() == null || !parser.iterator().hasNext()) {
                copyPageData(csvPrinter, cols);
            } else {
                int index = 0;
                for (CSVRecord record : parser) {
                    if (++index < currentPageStart || index >= currentPageEnd) {
                        List<String> values = new ArrayList<>();
                        for (int c : cols) {
                            String d = record.get(c);
                            d = d == null ? "" : d;
                            values.add(d);
                        }
                        csvPrinter.printRecord(values);
                    } else if (index == currentPageStart) {
                        copyPageData(csvPrinter, cols);
                    }
                }
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
                for (int i = 0; i < record.size(); i++) {
                    if (cols.contains(i)) {
                        String v = value;
                        if (AppValues.MyBoxRandomFlag.equals(value)) {
                            if (random == null) {
                                random = new Random();
                            }
                            v = columns.get(i).random(random, maxRandom, scale);
                        }
                        values.add(v);
                    } else {
                        values.add(record.get(i));
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
    protected File pasteFileColValuesDo(int col) {
        if (sourceFile == null || col < 0) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat);
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, sourceCharset), sourceCsvFormat)) {
            if (sourceWithNames) {
                csvPrinter.printRecord(columnNames());
            }
            List<String> values = new ArrayList<>();
//            int row = 0, csize = copiedCol.size();
//            for (CSVRecord record : parser) {
//                if (row < csize) {
//                    for (int i = 0; i < record.size(); i++) {
//                        if (i == col) {
//                            values.add(copiedCol.get(row));
//                        } else {
//                            values.add(record.get(i));
//                        }
//                    }
//                    csvPrinter.printRecord(values);
//                    values.clear();
//                    row++;
//                } else {
//                    csvPrinter.printRecord(record);
//                }
//            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return tmpFile;
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
                ColumnDefinition column = columns.get(col);
                int v = column.compare(row1.get(col), row2.get(col));
                return asc ? v : -v;
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
                for (int i = 0; i < record.size(); i++) {
                    values.add(record.get(i));
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
            if (sourceWithNames) {
                csvPrinter.printRecord(columnNames());
            }
            List<String> values = new ArrayList<>();
            List<Integer> indexs = new ArrayList<>();
            for (int col = 0; col < colsCheck.length; ++col) {
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

    @Override
    public void saveFile() {
        if (sourceFile == null) {
            saveAs();
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                String[][] data;

                @Override
                protected boolean handle() {
                    backup();
                    error = save(sourceFile, sourceCharset, sourceCsvFormat, sourceWithNames);
                    return error == null;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(message("Saved"));
                    recordFileWritten(sourceFile);
                    dataChangedNotify.set(false);
                    loadFile();
                }

            };
            start(task);
        }
    }

    @Override
    public void saveAs() {
        File file = chooseSaveFile();
        if (file == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                String[][] data;

                @Override
                protected boolean handle() {
                    CSVFormat targetCsvFormat = CSVFormat.DEFAULT
                            .withDelimiter(targetCsvDelimiter)
                            .withIgnoreEmptyLines().withTrim().withNullString("");
                    if (targetWithNames) {
                        targetCsvFormat = targetCsvFormat.withFirstRecordAsHeader();
                    }
                    error = save(file, targetCharset, targetCsvFormat, targetWithNames);
                    return error == null;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(message("Saved"));
                    recordFileWritten(file);
                    if (sourceFile == null || saveAsType == SaveAsType.Load) {
                        if (parentController != null) {
                            dataChangedNotify.set(false);
                            parentController.sourceFileChanged(file);
                        } else {
                            DataFileCSVController controller = (DataFileCSVController) WindowTools.openStage(Fxmls.DataFileCSVFxml);
                            controller.sourceFileChanged(file);
                        }

                    } else if (saveAsType == SaveAsType.Open) {
                        DataFileCSVController controller = (DataFileCSVController) WindowTools.openStage(Fxmls.DataFileCSVFxml);
                        controller.sourceFileChanged(file);
                    }
                }

            };
            start(task);
        }
    }

    public String save(File tfile, Charset charset, CSVFormat csvFormat, boolean withName) {
        File tmpFile = TmpFileTools.getTempFile();
        if (columns == null) {
            makeColumns(colsCheck.length);
        }
        if (charset == null) {
            charset = Charset.forName("UTF-8");
        }
        if (sourceFile != null) {
            try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat);
                     CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, charset), csvFormat)) {
                if (withName) {
                    csvPrinter.printRecord(columnNames());
                }
                if (parser.iterator() == null || !parser.iterator().hasNext()) {
                    writePageData(csvPrinter);
                } else {
                    long index = 0;
                    for (CSVRecord record : parser) {
                        if (++index < currentPageStart || index >= currentPageEnd) {    // 1-based, excluded
                            csvPrinter.printRecord(record);
                        } else if (index == currentPageStart) {
                            writePageData(csvPrinter);
                        }
                    }
                }
            } catch (Exception e) {
                MyBoxLog.console(e);
                return e.toString();
            }
        } else {
            try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, charset), csvFormat)) {
                if (withName) {
                    csvPrinter.printRecord(columnNames());
                }
                writePageData(csvPrinter);
            } catch (Exception e) {
                MyBoxLog.console(e);
                return e.toString();
            }
        }
        if (FileTools.rename(tmpFile, tfile, false)) {
            try ( Connection conn = DerbyBase.getConnection();) {
                String dname = tfile.getAbsolutePath();
                tableDataDefinition.clear(conn, dataType, dname);
                DataDefinition def = DataDefinition.create().setDataName(dname).setDataType(dataType)
                        .setCharset(charset.name()).setHasHeader(withName)
                        .setDelimiter(csvFormat.getDelimiter() + "");
                tableDataDefinition.insertData(conn, def);
                if (ColumnDefinition.valid(this, columns)) {
                    tableDataColumn.save(conn, def.getDfid(), columns);
                    conn.commit();
                }
            } catch (Exception e) {
                return e.toString();
            }
            return null;
        } else {
            return "Failed";
        }
    }

    protected void writePageData(CSVPrinter csvPrinter) {
        try {
            if (csvPrinter == null || sheetInputs == null) {
                return;
            }
            for (int r = 0; r < sheetInputs.length; r++) {
                csvPrinter.printRecord(row(r));
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    @Override
    protected boolean saveColumns() {
        if (sourceFile == null) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            String dname = sourceFile.getAbsolutePath();
            tableDataDefinition.clear(conn, dataType, dname);
            DataDefinition def = DataDefinition.create().setDataName(dname).setDataType(dataType)
                    .setCharset(sourceCharset.name()).setHasHeader(sourceWithNames)
                    .setDelimiter(sourceCsvFormat.getDelimiter() + "");
            tableDataDefinition.insertData(conn, def);
            if (ColumnDefinition.valid(this, columns)) {
                tableDataColumn.save(conn, def.getDfid(), columns);
                conn.commit();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

}
