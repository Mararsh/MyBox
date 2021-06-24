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
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.db.table.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2020-12-24
 * @License Apache License Version 2.0
 */
public class DataFileCSVController extends BaseDataFileController {

    protected Charset sourceCharset;
    protected CSVFormat sourceCsvFormat;
    protected char sourceDelimiter;

    @FXML
    protected ControlCsvOptions csvReadController, csvWriteController;
    @FXML
    protected Label savedLabel, dataSizeLabel, selectedLabel;

    public DataFileCSVController() {
        baseTitle = message("EditCSV");
        TipsLabelKey = "DataFileCSVTips";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.CSV);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            csvReadController.setControls(baseName + "Read");
            csvWriteController.setControls(baseName + "Write");

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setFile(File file, boolean withName) {
        sourceFile = file;
        csvReadController.withNamesCheck.setSelected(withName);
        csvReadController.commaRadio.fire();
        initCurrentPage();
        loadFile(true);
    }

    @Override
    protected boolean readDataDefinition(boolean pickOptions) {
        try ( Connection conn = DerbyBase.getConnection()) {
            dataName = sourceFile.getAbsolutePath();
            dataDefinition = tableDataDefinition.read(conn, dataType, dataName);
            if (pickOptions || dataDefinition == null) {
                sourceDelimiter = csvReadController.delimiter;
                sourceWithNames = csvReadController.withNamesCheck.isSelected();
                if (csvReadController.autoDetermine) {
                    sourceCharset = FileTools.charset(sourceFile);
                } else {
                    sourceCharset = csvReadController.charset;
                }
            } else {
                sourceDelimiter = dataDefinition.getDelimiter().charAt(0);
                sourceWithNames = dataDefinition.isHasHeader();
                sourceCharset = Charset.forName(dataDefinition.getCharset());
            }
            if (dataDefinition == null) {
                dataDefinition = DataDefinition.create().setDataName(dataName).setDataType(dataType)
                        .setCharset(sourceCharset.name()).setHasHeader(sourceWithNames)
                        .setDelimiter(sourceDelimiter + "");
                tableDataDefinition.insertData(conn, dataDefinition);
                conn.commit();
            } else {
                if (pickOptions) {
                    dataDefinition.setCharset(sourceCharset.name())
                            .setDelimiter(sourceDelimiter + "")
                            .setHasHeader(sourceWithNames);
                    tableDataDefinition.updateData(conn, dataDefinition);
                    conn.commit();
                }
                savedColumns = tableDataColumn.read(conn, dataDefinition.getDfid());
            }
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
                .withDelimiter(sourceDelimiter);
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
                    ColumnDefinition column = new ColumnDefinition(name, ColumnType.String);
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
                .withDelimiter(sourceDelimiter);
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
    protected void updateStatus() {
        if (sourceFile == null) {
            loadedLabel.setText("");
        } else {
            loadedLabel.setText(message("File") + ": " + sourceFile.getAbsolutePath() + "\n"
                    + message("Charset") + ": " + sourceCharset + "\n"
                    + message("Delimiter") + ": " + csvReadController.delimiter + "\n"
                    + message("RowsNumber") + ": " + totalSize + "\n"
                    + (columns == null ? "" : message("ColumnsNumber") + ": " + columns.size() + "\n")
                    + message("FirstLineAsNames") + ": " + (sourceWithNames ? message("Yes") : message("No")) + "\n"
                    + message("Load") + ": " + DateTools.nowString());
        }

    }

    @FXML
    @Override
    public void saveAction() {
        if (sourceFile == null) {
            saveAsAction();
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
                    popInformation(AppVariables.message("Saved"));
                    dataChanged = false;
                    loadFile();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        String name = null;
        if (sourceFile != null) {
            name = FileTools.getFilePrefix(sourceFile.getName());
        }
        targetFile = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                name, targetExtensionFilter);
        if (targetFile == null) {
            return;
        }
        recordFileWritten(targetFile);
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                String[][] data;

                @Override
                protected boolean handle() {
                    Charset targetCharset;
                    if (csvWriteController.autoDetermine) {
                        targetCharset = sourceCharset;
                    } else {
                        targetCharset = csvWriteController.charset;
                    }
                    CSVFormat targetCsvFormat = CSVFormat.DEFAULT
                            .withDelimiter(csvWriteController.delimiter)
                            .withIgnoreEmptyLines().withTrim().withNullString("");
                    boolean targetWithNames = csvWriteController.withNamesCheck.isSelected();
                    if (targetWithNames) {
                        targetCsvFormat = targetCsvFormat.withFirstRecordAsHeader();
                    }
                    error = save(targetFile, targetCharset, targetCsvFormat, targetWithNames);
                    return error == null;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(AppVariables.message("Saved"));
                    if (sourceFile == null || saveAsType == SaveAsType.Load) {
                        dataChanged = false;
                        sourceFileChanged(targetFile);

                    } else if (saveAsType == SaveAsType.Open) {
                        DataFileCSVController controller = (DataFileCSVController) FxmlStage.openStage(CommonValues.DataFileCSVFxml);
                        controller.sourceFileChanged(targetFile);
                    }

                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    public String save(File file, Charset charset, CSVFormat csvFormat, boolean withName) {
        File tmpFile = FileTools.getTempFile();
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
                    } else if (index == currentPageStart && inputs != null) {
                        for (int j = 0; j < inputs.length; j++) {
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
                if (inputs != null) {
                    for (int j = 0; j < inputs.length; j++) {
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
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
        return true;
    }

    @FXML
    public void editTextFile() {
        if (sourceFile == null) {
            return;
        }
        TextEditerController controller = (TextEditerController) FxmlStage.openStage(CommonValues.TextEditerFxml);
        controller.openTextFile(sourceFile);
        controller.toFront();
    }

    @Override
    protected File setAllColValues(int col, String value) {
        if (sourceFile == null || col < 0 || value == null) {
            return null;
        }
        File tmpFile = FileTools.getTempFile();
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
    protected StringBuilder copyAllColValues(int col) {
        if (sourceFile == null || col < 0) {
            return null;
        }
        StringBuilder s = null;
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
            int index = 0;
            for (CSVRecord record : parser) {
                if (++index < currentPageStart || index >= currentPageEnd) {
                    copiedCol.add(record.get(col));
                } else if (index == currentPageStart) {
                    copyPageCol(col);
                }
            }
            for (String v : copiedCol) {
                if (s == null) {
                    s = new StringBuilder();
                    s.append(v);
                } else {
                    s.append("\n").append(v);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return s;
    }

    @Override
    protected File pasteAllColValues(int col) {
        if (sourceFile == null || col < 0) {
            return null;
        }
        File tmpFile = FileTools.getTempFile();
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat);
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, sourceCharset), sourceCsvFormat)) {
            if (sourceWithNames) {
                csvPrinter.printRecord(columnNames());
            }
            List<String> values = new ArrayList<>();
            int row = 0, csize = copiedCol.size();
            for (CSVRecord record : parser) {
                if (row < csize) {
                    for (int i = 0; i < record.size(); i++) {
                        if (i == col) {
                            values.add(copiedCol.get(row));
                        } else {
                            values.add(record.get(i));
                        }
                    }
                    csvPrinter.printRecord(values);
                    values.clear();
                    row++;
                } else {
                    csvPrinter.printRecord(record);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return tmpFile;
    }

    @Override
    protected File insertFileCol(int col, boolean left, int number) {
        if (sourceFile == null || col < 0 || number < 1) {
            return null;
        }
        File tmpFile = FileTools.getTempFile();
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat);
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, sourceCharset), sourceCsvFormat)) {
            if (sourceWithNames) {
                csvPrinter.printRecord(columnNames());
            }
            List<String> values = new ArrayList<>();
            List<String> newValues = new ArrayList<>();
            for (int i = 1; i < number; i++) {
                newValues.add(defaultColValue);
            }
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
    protected File DeleteFileCol(int col) {
        if (sourceFile == null || col < 0) {
            return null;
        }
        File tmpFile = FileTools.getTempFile();
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
        File tmpFile = FileTools.getTempFile();
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
    protected File orderFileCol(int col, boolean asc) {
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
        File tmpFile = FileTools.getTempFile();
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
    protected StringBuilder copyAllSelectedCols() {
        if (sourceFile == null) {
            return null;
        }
        StringBuilder s = null;
        copiedLines = 0;
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
            String delimiterString = TextTools.delimiterText(textController.delimiter);
            int index = 0;
            for (CSVRecord record : parser) {
                if (index < currentPageStart || index >= currentPageEnd) {
                    String rowString = null;
                    for (int i = 0; i < colsCheck.length; ++i) {
                        if (colsCheck[i].isSelected()) {
                            String d = record.get(i);
                            d = d == null ? "" : d;
                            if (rowString == null) {
                                rowString = d;
                            } else {
                                rowString += delimiterString + d;
                            }
                        }
                    }
                    rowString = rowString == null ? "" : rowString;
                    if (s == null) {
                        s = new StringBuilder();
                        s.append(rowString);
                    } else {
                        s.append("\n").append(rowString);
                    }
                    copiedLines++;
                } else if (index == currentPageStart) {
                    s = copyPageCols(s, delimiterString);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return s;
    }

    @Override
    protected File deleteFileSelectedCols() {
        if (sourceFile == null) {
            return null;
        }
        File tmpFile = FileTools.getTempFile();
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
    protected File setAllSelectedCols(String value) {
        if (sourceFile == null) {
            return null;
        }
        File tmpFile = FileTools.getTempFile();
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

    public static DataFileCSVController oneOpen() {
        DataFileCSVController controller = null;
        Stage stage = FxmlStage.findStage(message("EditCSV"));
        if (stage != null && stage.getUserData() != null) {
            try {
                controller = (DataFileCSVController) stage.getUserData();
            } catch (Exception e) {
            }
        }
        if (controller == null) {
            controller = (DataFileCSVController) FxmlStage.openStage(CommonValues.DataFileCSVFxml);
        }
        if (controller != null) {
            controller.getMyStage().requestFocus();
        }
        return controller;
    }

}
