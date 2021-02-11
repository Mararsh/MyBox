package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.db.table.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
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

        SourceFileType = VisitHistory.FileType.CSV;
        SourcePathType = VisitHistory.FileType.CSV;
        TargetPathType = VisitHistory.FileType.CSV;
        TargetFileType = VisitHistory.FileType.CSV;
        AddFileType = VisitHistory.FileType.CSV;
        AddPathType = VisitHistory.FileType.CSV;

        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.CSV);

        sourceExtensionFilter = CommonFxValues.CsvExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
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

    @Override
    protected boolean readDataDefinition(boolean pickOptions) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
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
        return dataDefinition.getDfid() >= 0;
    }

    @Override
    protected boolean readColumns() {
        sourceCsvFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(sourceDelimiter);
        if (sourceWithNames) {
            sourceCsvFormat = sourceCsvFormat.withFirstRecordAsHeader();
        }
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
            if (sourceWithNames) {
                for (String name : parser.getHeaderNames()) {
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
                    if (found) {
                        continue;
                    }
                    ColumnDefinition column = new ColumnDefinition(name, ColumnType.String);
                    columns.add(column);
                }
            } else {
                Iterator<CSVRecord> iterator = parser.iterator();
                if (iterator == null || !iterator.hasNext()) {
                    return false;
                }
                CSVRecord record = iterator.next();
                if (savedColumns != null) {
                    columns = savedColumns;
                } else {
                    columns = new ArrayList<>();
                }
                for (int i = columns.size() + 1; i <= record.size(); i++) {
                    ColumnDefinition column = new ColumnDefinition(message("Field") + i, ColumnType.String);
                    columns.add(column);
                }
            }
            if (ColumnDefinition.valid(this, columns)) {
                tableDataColumn.save(dataDefinition.getDfid(), columns);
            }
            return true;
        } catch (Exception e) {
            loadError = e.toString();
            return false;
        }
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
            if (iterator == null || !iterator.hasNext()) {
                return false;
            }
            while (backgroundTask != null && !backgroundTask.isCancelled() && iterator.hasNext()) {
                iterator.next();
                totalSize++;
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
        if (currentPageEnd < currentPageStart) {
            currentPageEnd = currentPageStart + pageSize;
        }
        String[][] data = null;
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
            int rowIndex = 0, maxCol = 0;
            List<List<String>> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                if (++rowIndex < currentPageStart) {
                    continue;
                }
                if (rowIndex >= currentPageEnd) {
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
        if (!totalRead) {
            return;
        }
        if (sourceFile == null) {
            saveAsAction();
            return;
        }
        if (!ColumnDefinition.valid(this, columns)) {
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
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (!ColumnDefinition.valid(this, columns)) {
            return;
        }
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
            thread.setDaemon(true);
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
                    } else if (index == currentPageStart) {
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
                for (int j = 0; j < inputs.length; j++) {
                    csvPrinter.printRecord(row(j));
                }
            } catch (Exception e) {
                MyBoxLog.console(e);
                return e.toString();
            }
        }
        if (FileTools.rename(tmpFile, file)) {
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
                String dname = file.getAbsolutePath();
                DataDefinition def = tableDataDefinition.read(conn, dataType, dname);
                if (def == null) {
                    def = DataDefinition.create().setDataName(dname).setDataType(dataType)
                            .setCharset(charset.name()).setHasHeader(withName)
                            .setDelimiter(csvFormat.getDelimiter() + "");
                    tableDataDefinition.insertData(conn, def);
                } else {
                    def.setCharset(charset.name()).setHasHeader(withName)
                            .setDelimiter(csvFormat.getDelimiter() + "");
                    tableDataDefinition.updateData(conn, def);
                }
                tableDataColumn.save(conn, def.getDfid(), columns);
            } catch (Exception e) {
                return e.toString();
            }
            return null;
        } else {
            return "Failed";
        }
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
    protected void setAllColValues(int col) {
        if (sourceFile == null || pagesNumber <= 1) {
            SetPageColValues(col);
        }
        if (totalSize <= 0 || !checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            String value = askValue(colName(col) + " - " + message("SetAllColValues") + "\n" + message("NoticeAllChangeUnrecover"),
                    "", "");
            if (!dataValid(col, value)) {
                popError(message("InvalidData"));
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
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
                        error = e.toString();
                        return false;
                    }
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
                    loadPage(currentPage);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    protected void copyAllColValues(int col) {
        if (sourceFile == null || pagesNumber <= 1) {
            copyPageColValues(col);
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                StringBuilder s;

                @Override
                protected boolean handle() {
                    copiedCol = new ArrayList<>();
                    try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
                        s = new StringBuilder();
                        for (CSVRecord record : parser) {
                            String v = record.get(col);
                            s.append(v == null ? "" : v).append("\n");
                            copiedCol.add(v);
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return !copiedCol.isEmpty();
                }

                @Override
                protected void whenSucceeded() {
                    if (FxmlControl.copyToSystemClipboard(s.toString())) {
                        popInformation(message("CopiedInSheet"));
                    } else {
                        popFailed();
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    protected void pasteAllColValues(int col) {
        if (copiedCol == null || copiedCol.isEmpty() || totalSize <= 0) {
            popError(message("NoData"));
            return;
        }
        if (sourceFile == null || pagesNumber <= 1) {
            pastePageColValues(col);
            return;
        }
        if (!checkBeforeNextAction()) {
            return;
        }
        if (!FxmlControl.askSure(baseTitle, colName(col) + " - " + message("PasteAllCol") + "\n"
                + message("NoticeAllChangeUnrecover"))) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
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
                        error = e.toString();
                        return false;
                    }
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
                    loadPage(currentPage);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    protected void insertFileCol(int col, boolean left) {
        if (sourceFile == null || pagesNumber <= 1) {
            insertPageCol(col, left);
            return;
        }
        if (totalSize <= 0 || !checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            String notice = colName(col) + " - " + (left ? message("InsertColLeft") : message("InsertColRight"))
                    + "\n" + message("NoticeAllChangeUnrecover");
            int offset = left ? 0 : 1;
            String name = message("Field") + (col + offset + 1);
            name = FxmlControl.askValue(baseTitle, notice, message("Name"), name);
            if (name == null) {
                return;
            }
            ColumnDefinition column = new ColumnDefinition(name, ColumnDefinition.ColumnType.String);
            columns.add(left ? col : col + 1, column);
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
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
                            values.add(col + offset, "");
                            csvPrinter.printRecord(values);
                            values.clear();
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
                    loadPage(currentPage);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    protected void DeleteFileCol(int col) {
        if (sourceFile == null || pagesNumber <= 1) {
            deletePageCol(col);
            return;
        }
        if (totalSize <= 0 || !checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            if (!FxmlControl.askSure(baseTitle, colName(col) + " - " + message("DeleteCol") + "\n"
                    + message("NoticeAllChangeUnrecover"))) {
                return;
            }
            columns.remove(col);
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
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
                        error = e.toString();
                        return false;
                    }
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
                    loadPage(currentPage);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    protected void orderCol(int col, boolean asc) {
        if (sourceFile == null || pagesNumber <= 1) {
            super.orderCol(col, asc);
            return;
        }
        if (totalSize <= 0 || !checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            if (!FxmlControl.askSure(baseTitle, colName(col) + " - "
                    + (asc ? message("Ascending") : message("Descending")) + "\n"
                    + message("DataFileOrderNotice"))) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    List<CSVRecord> records;
                    try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
                        records = parser.getRecords();
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    if (records == null || records.isEmpty()) {
                        return false;
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
                        error = e.toString();
                        return false;
                    }
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
                    loadPage(currentPage);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    protected void copyAllSelectedCols() {
        int cols = 0;
        for (CheckBox c : colsCheck) {
            if (c.isSelected()) {
                cols++;
            }
        }
        if (cols < 1) {
            popError(message("NoData"));
            return;
        }
        if (sourceFile == null || pagesNumber <= 1) {
            copySelectedCols();
            return;
        }
        int selectedCols = cols;
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private StringBuilder s;
                private int lines;

                @Override
                protected boolean handle() {
                    try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
                        s = new StringBuilder();
                        String p = TextTools.delimiterText(delimiter);
                        lines = 0;
                        for (CSVRecord record : parser) {
                            String row = null;
                            for (int i = 0; i < colsCheck.length; ++i) {
                                if (colsCheck[i].isSelected()) {
                                    String d = record.get(i);
                                    d = d == null ? "" : d;
                                    if (row == null) {
                                        row = d;
                                    } else {
                                        row += p + d;
                                    }
                                }
                            }
                            if (row != null) {
                                s.append(row).append("\n");
                                lines++;
                            }
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (FxmlControl.copyToSystemClipboard(s.toString())) {
                        popInformation(message("CopiedToSystemClipboard") + "\n"
                                + message("RowsNumber") + ":" + lines + "\n"
                                + message("ColumnsNumber") + ":" + selectedCols);
                    } else {
                        popFailed();
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void deleteSelectedCols() {
        if (sourceFile == null || pagesNumber <= 1) {
            super.deleteSelectedCols();
            return;
        }
        if (!checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            if (!FxmlControl.askSure(baseTitle, message("DeleteSelectedCols") + "\n"
                    + message("NoticeAllChangeUnrecover"))) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
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
                        error = e.toString();
                        return false;
                    }
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
                    loadPage(currentPage);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    protected void setAllSelectedCols() {
        if (sourceFile == null || pagesNumber <= 1) {
            setSelectedCols();
            return;
        }
        if (!checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            String value = FxmlControl.askValue(baseTitle, message("NoticeAllChangeUnrecover"),
                    message("SetAllSelectedColsValues"), "");
            if (value == null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
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
                        error = e.toString();
                        return false;
                    }
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
                    loadPage(currentPage);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
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
