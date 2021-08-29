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
import javafx.scene.control.TextField;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
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
    protected boolean autoDetermineSourceCharset, autoDetermineTargetCharset, targetWithNames;

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
    protected File setFileColValuesDo(int col, String value) {
        if (sourceFile == null || col < 0 || value == null) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat);
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, sourceCharset), sourceCsvFormat)) {
            if (sourceWithNames) {
                csvPrinter.printRecord(columnNames());
            }
            List<String> values = new ArrayList<>();
            for (CSVRecord record : parser) {
                for (int i = 0; i < record.size(); i++) {
                    if (i == col) {
                        values.add(value);
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
    protected File insertFileColDo(int col, boolean left, int number) {
        if (sourceFile == null || col < 0 || number < 1) {
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
            for (CSVRecord record : parser) {
                for (int i = 0; i < record.size(); i++) {
                    values.add(record.get(i));
                }
                values.addAll(col + (left ? 0 : 1), newValues);
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
    protected File DeleteFileColDo(int col) {
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
            for (CSVRecord record : parser) {
                for (int i = 0; i < record.size(); i++) {
                    values.add(record.get(i));
                }
                values.remove(col);
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
    protected File deleteFileAllCols() {
        if (sourceFile == null) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, sourceCharset), sourceCsvFormat)) {
            if (sourceWithNames) {
                csvPrinter.printRecord(columnNames());
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return tmpFile;
    }

    @Override
    protected File orderFileColDo(int col, boolean asc) {
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
    protected File fileSelectedCols(List<Integer> cols) {
        if (sourceFile == null || cols == null || cols.isEmpty()) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat);
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, Charset.forName("utf-8")), CSVFormat.DEFAULT)) {
            List<String> names = new ArrayList<>();
            for (int c : cols) {
                names.add(colsCheck[c].getText());
            }
            csvPrinter.printRecord(names);
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
                    for (TextField[] rowInputs : sheetInputs) {
                        List<String> values = new ArrayList<>();
                        for (int c : cols) {
                            String d = rowInputs[c].getText();
                            d = d == null ? "" : d;
                            values.add(d);
                        }
                        csvPrinter.printRecord(values);
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
    protected File deleteFileSelectedColsDo() {
        if (sourceFile == null) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat);
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, sourceCharset), sourceCsvFormat)) {
            if (sourceWithNames) {
                csvPrinter.printRecord(columnNames());
            }
            List<String> values = new ArrayList<>();
            for (CSVRecord record : parser) {
                for (int i = 0; i < colsCheck.length; ++i) {
                    if (!colsCheck[i].isSelected()) {
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
    protected File setFileSelectedColsDo(String value) {
        if (sourceFile == null) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat);
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, sourceCharset), sourceCsvFormat)) {
            if (sourceWithNames) {
                csvPrinter.printRecord(columnNames());
            }
            List<String> values = new ArrayList<>();
            for (CSVRecord record : parser) {
                for (int i = 0; i < colsCheck.length; ++i) {
                    if (colsCheck[i].isSelected()) {
                        values.add(value);
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
                    popInformation(Languages.message("Saved"));
                    dataChangedNotify.set(false);
                    loadFile();
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
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
                    Charset tCharset;
                    if (autoDetermineTargetCharset) {
                        tCharset = sourceCharset;
                    } else {
                        tCharset = targetCharset;
                    }
                    CSVFormat targetCsvFormat = CSVFormat.DEFAULT
                            .withDelimiter(targetCsvDelimiter)
                            .withIgnoreEmptyLines().withTrim().withNullString("");
                    if (targetWithNames) {
                        targetCsvFormat = targetCsvFormat.withFirstRecordAsHeader();
                    }
                    error = save(file, tCharset, targetCsvFormat, targetWithNames);
                    return error == null;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(Languages.message("Saved"));
                    recordFileWritten(file);
                    if (sourceFile == null || saveAsType == SaveAsType.Load) {
                        if (fileController != null) {
                            dataChangedNotify.set(false);
                            fileController.sourceFileChanged(file);
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
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    public String save(File file, Charset charset, CSVFormat csvFormat, boolean withName) {
        File tmpFile = TmpFileTools.getTempFile();
        if (withName && columns == null) {
            makeColumns(colsCheck.length);
        }
        if (sourceFile != null) {
            try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat);
                     CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, charset), csvFormat)) {
                if (withName) {
                    csvPrinter.printRecord(columnNames());
                }
                long index = 0;
                for (CSVRecord record : parser) {
                    if (++index < currentPageStart || index >= currentPageEnd) {    // 1-based, excluded
                        csvPrinter.printRecord(record);
                    } else if (index == currentPageStart && sheetInputs != null) {
                        for (int j = 0; j < sheetInputs.length; j++) {
                            csvPrinter.printRecord(row(j));
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
                if (sheetInputs != null) {
                    for (int j = 0; j < sheetInputs.length; j++) {
                        csvPrinter.printRecord(row(j));
                    }
                }
            } catch (Exception e) {
                MyBoxLog.console(e);
                return e.toString();
            }
        }
        if (FileTools.rename(tmpFile, file)) {
            try ( Connection conn = DerbyBase.getConnection();) {
                String dname = file.getAbsolutePath();
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
