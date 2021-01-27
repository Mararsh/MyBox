package mara.mybox.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
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
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @Author Mara
 * @CreateDate 2021-1-17
 * @License Apache License Version 2.0
 */
public class DataFileExcelController extends BaseDataFileController {

    protected int sheetsNumber, selectedSheet, currentSheet;  // 1-based
    protected String sheetName;

    @FXML
    protected ComboBox<String> sheetSelector;
    @FXML
    protected CheckBox sourceWithNamesCheck, targetWithNamesCheck, currentOnlyCheck;
    @FXML
    protected FlowPane sheetNumberPane;
    @FXML
    protected Label sheetsNumberLabel;

    public DataFileExcelController() {
        baseTitle = message("EditExcel");
        TipsLabelKey = "DataFileExcelTips";

        SourceFileType = VisitHistory.FileType.Excel;
        SourcePathType = VisitHistory.FileType.Excel;
        TargetPathType = VisitHistory.FileType.Excel;
        TargetFileType = VisitHistory.FileType.Excel;
        AddFileType = VisitHistory.FileType.Excel;
        AddPathType = VisitHistory.FileType.Excel;

        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Excel);

        sourceExtensionFilter = CommonFxValues.ExcelExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            sheetsNumber = 1;
            currentSheet = 1;

            sourceWithNamesCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "SourceWithNames", true));
            sourceWithNamesCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        if (!isSettingValues) {
                            AppVariables.setUserConfigValue(baseName + "SourceWithNames", newValue);
                        }
                    });

            selectedSheet = 1;
            sheetSelector.getItems().addAll(Arrays.asList("1"));
            sheetSelector.setValue("1");
            sheetSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v < 1 || v > sheetsNumber) {
                                pageSelector.getEditor().setStyle(badStyle);
                            } else {
                                selectedSheet = v;
                                pageSelector.getEditor().setStyle(null);
                            }
                        } catch (Exception e) {
                            pageSelector.getEditor().setStyle(badStyle);
                        }
                    });

            targetWithNamesCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "TargetWithNames", true));
            targetWithNamesCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        if (!isSettingValues) {
                            AppVariables.setUserConfigValue(baseName + "TargetWithNames", newValue);
                        }
                    });

            currentOnlyCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "CurrentOnly", false));
            currentOnlyCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        if (!isSettingValues) {
                            AppVariables.setUserConfigValue(baseName + "CurrentOnly", newValue);
                        }
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();

        setControls(baseName);
        createAction();
    }

    public String cellString(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return cell.getNumericCellValue() + "";
            case BOOLEAN:
                return cell.getBooleanCellValue() + "";
            default:
                return cell.getStringCellValue();
        }
    }

    @Override
    public void initFile() {
        sheetName = null;
        sheetsNumber = 1;
        if (fileOptionsBox.getChildren().contains(sheetNumberPane)) {
            fileOptionsBox.getChildren().remove(sheetNumberPane);
        }
        super.initFile();
    }

    @Override
    protected boolean readDataDefinition(boolean pickOptions) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Workbook wb = WorkbookFactory.create(sourceFile)) {
            sheetsNumber = wb.getNumberOfSheets();
            if (currentSheet < 1 || currentSheet > sheetsNumber) {
                currentSheet = 1;
            }
            Sheet sheet = wb.getSheetAt(currentSheet - 1);
            sheetName = sheet.getSheetName();
            dataName = sourceFile.getAbsolutePath() + "-" + sheetName;

            dataDefinition = tableDataDefinition.read(conn, dataType, dataName);

            if (pickOptions || dataDefinition == null) {
                sourceWithNames = sourceWithNamesCheck.isSelected();
            } else {
                sourceWithNames = dataDefinition.isHasHeader();
            }
            boolean changed = pickOptions;
            if (sourceWithNames) {
                Iterator<Row> iterator = sheet.iterator();
                if (iterator.hasNext()) {
                    Row row = iterator.next();
                    if (row == null) {
                        sourceWithNames = false;
                        changed = true;
                    }
                }
            }
            if (dataDefinition == null) {
                dataDefinition = DataDefinition.create().setDataName(dataName).setDataType(dataType)
                        .setHasHeader(sourceWithNames);
                tableDataDefinition.insertData(conn, dataDefinition);
                conn.commit();
            } else {
                if (changed) {
                    dataDefinition.setHasHeader(sourceWithNames);
                    tableDataDefinition.updateData(conn, dataDefinition);
                    conn.commit();
                }
                savedColumns = tableDataColumn.read(conn, dataDefinition.getDfid());
            }
            return dataDefinition.getDfid() >= 0;
        } catch (Exception e) {
            loadError = e.toString();
            return false;
        }

    }

    @Override
    protected boolean readColumns() {
        columns = new ArrayList<>();
        if (!sourceWithNames) {
            if (savedColumns != null) {
                columns = savedColumns;
            }
            return true;
        }
        try ( Workbook wb = WorkbookFactory.create(sourceFile)) {
            Sheet sheet = wb.getSheetAt(currentSheet - 1);
            Iterator<Row> iterator = sheet.iterator();
            if (iterator == null || !iterator.hasNext()) {
                return false;
            }
            Row row = iterator.next();
            if (row == null) {
                sourceWithNames = false;
                return true;
            }
            for (int col = row.getFirstCellNum(); col < row.getLastCellNum(); col++) {
                String name = (message("Field") + (col + 1));
                ColumnType type = ColumnType.String;
                Cell cell = row.getCell(col);
                if (cell != null) {
                    switch (cell.getCellType()) {
                        case NUMERIC:
                            type = ColumnType.Double;
                            break;
                        case BOOLEAN:
                            type = ColumnType.Boolean;
                            break;
                    }
                    String v = cellString(cell);
                    if (!v.isBlank()) {
                        name = v;
                    }
                }
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
                    ColumnDefinition column = new ColumnDefinition(name, type);
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
        try ( Workbook wb = WorkbookFactory.create(sourceFile)) {
            sheetsNumber = wb.getNumberOfSheets();
            if (currentSheet < 1 || currentSheet > sheetsNumber) {
                currentSheet = 1;
            }
            Sheet sheet = wb.getSheetAt(currentSheet - 1);
            Iterator<Row> iterator = sheet.iterator();
            if (iterator == null || !iterator.hasNext()) {
                return false;
            }
            while (iterator.hasNext()) {
                iterator.next();
                totalSize++;
            }
            if (sourceWithNames) {
                totalSize--;
            }
        } catch (Exception e) {
            loadError = e.toString();
            return false;
        }
        return true;
    }

    @Override
    protected void afterFileLoaded() {
        if (sheetsNumber > 1) {
            if (!fileOptionsBox.getChildren().contains(sheetNumberPane)) {
                fileOptionsBox.getChildren().add(0, sheetNumberPane);
            }
            sheetsNumberLabel.setText("/" + sheetsNumber);
            List<String> sheets = new ArrayList<>();
            for (int i = 1; i <= sheetsNumber; i++) {
                sheets.add(i + "");
            }
            isSettingValues = true;
            selectedSheet = selectedSheet > sheetsNumber ? 1 : selectedSheet;
            sheetSelector.getItems().setAll(sheets);
            sheetSelector.setValue(selectedSheet + "");
            isSettingValues = false;
        }
//        updateStatus();
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
        try ( Workbook wb = WorkbookFactory.create(sourceFile)) {
            Sheet sheet = wb.getSheetAt(currentSheet - 1);
            Iterator<Row> iterator = sheet.iterator();
            if (iterator == null || !iterator.hasNext()) {
                return null;
            }
            int rowIndex = 0, maxCol = 0;
            List<List<String>> rows = new ArrayList<>();
            int startRow = (int) currentPageStart;
            int endRow = (int) currentPageEnd;
            if (sourceWithNames) {
                startRow++;
                endRow++;
            }
            while (iterator.hasNext()) {
                Row fileRow = iterator.next();
                ++rowIndex;
                if (fileRow == null) {
                    continue;
                }
                if (rowIndex < startRow) {
                    continue;
                }
                if (rowIndex >= endRow) {
                    break;
                }
                List<String> row = new ArrayList<>();
                for (int cellIndex = fileRow.getFirstCellNum(); cellIndex < fileRow.getLastCellNum(); cellIndex++) {
                    String v = cellString(fileRow.getCell(cellIndex));
                    row.add(v);
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
                    + message("CurrentSheet") + ": " + currentSheet + "\n"
                    + (sheetName == null ? "" : message("SheetName") + ": " + sheetName + "\n")
                    + message("RowsNumber") + ": " + totalSize + "\n"
                    + (columns == null ? "" : message("ColumnsNumber") + ": " + columns.size() + "\n")
                    + message("FirstLineAsNames") + ": " + (sourceWithNames ? message("Yes") : message("No")) + "\n"
                    + message("Load") + ": " + DateTools.nowString());
        }
    }

    @FXML
    public void setSheet() {
        if (sourceFile == null || !checkBeforeNextAction()) {
            return;
        }
        currentSheet = selectedSheet;
        loadFile(false);
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
                    error = save(sourceFile, sourceWithNames);
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
                    error = save(targetFile, targetWithNamesCheck.isSelected());
                    return error == null;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(AppVariables.message("Saved"));
                    if (sourceFile == null || saveAsType == SaveAsType.Load) {
                        dataChanged = false;
                        sourceFileChanged(targetFile);

                    } else if (saveAsType == SaveAsType.Open) {
                        DataFileExcelController controller = (DataFileExcelController) FxmlStage.openStage(CommonValues.DataFileExcelFxml);
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

    public String save(File file, boolean withName) {
        File tmpFile = FileTools.getTempFile();
        if (withName && columns == null) {
            makeColumns(colsCheck.length);
        }
        List<String> otherSheetNames = new ArrayList<>();
        String currentSheetName;
        if (sourceFile != null) {
            try ( Workbook sourceBook = WorkbookFactory.create(sourceFile)) {
                sheetsNumber = sourceBook.getNumberOfSheets();
                if (currentSheet < 1 || currentSheet > sheetsNumber) {
                    currentSheet = 1;
                }
                Sheet sourceSheet = sourceBook.getSheetAt(currentSheet - 1);
                currentSheetName = sourceSheet.getSheetName();

                Workbook targetBook;
                Sheet targetSheet;
                File tmpDataFile = null;
                if (sheetsNumber == 1
                        || (!file.equals(sourceFile) && currentOnlyCheck.isSelected())) {
                    targetBook = new XSSFWorkbook();
                    targetSheet = targetBook.createSheet(currentSheetName);
                } else {
                    tmpDataFile = FileTools.getTempFile();
                    FileTools.copyFile(sourceFile, tmpDataFile);
                    targetBook = WorkbookFactory.create(tmpDataFile);
                    if (!currentOnlyCheck.isSelected()) {
                        for (int i = 0; i < sheetsNumber; i++) {
                            otherSheetNames.add(targetBook.getSheetName(i));
                        }
                        otherSheetNames.remove(currentSheetName);
                    }
                    targetBook.removeSheetAt(currentSheet - 1);
                    targetSheet = targetBook.createSheet(currentSheetName);
                    targetBook.setSheetOrder(targetSheet.getSheetName(), currentSheet - 1);
                }

                int offset = withName ? 1 : 0;
                int targetRowIndex = 0;
                if (withName) {
                    Row targetRow = targetSheet.createRow(targetRowIndex++);
                    for (int col = 0; col < colsCheck.length; col++) {
                        Cell targetCell = targetRow.createCell(col, CellType.STRING);
                        targetCell.setCellValue(colsCheck[col].getText());
                    }
                }
                int startRow = (int) currentPageStart + offset - 1;
                int endRow = (int) currentPageEnd + offset - 1; // exclude
                for (int sourceRowIndex = sourceSheet.getFirstRowNum() + offset; sourceRowIndex <= sourceSheet.getLastRowNum(); sourceRowIndex++) {
                    Row sourceRow = sourceSheet.getRow(sourceRowIndex);
                    if (sourceRowIndex < startRow || sourceRowIndex >= endRow) {
                        Row targetRow = targetSheet.createRow(targetRowIndex++);
                        copyRow(sourceRow, targetRow);
                    } else if (sourceRowIndex == startRow) {
                        for (int inputsRowIndex = 0; inputsRowIndex < inputs.length; inputsRowIndex++) {
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            for (int col = 0; col < inputs[inputsRowIndex].length; col++) {
                                Cell targetCell = targetRow.createCell(col, CellType.STRING);
                                targetCell.setCellValue(inputs[inputsRowIndex][col].getText());
                            }
                        }
                    }
                }
                try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                    targetBook.write(fileOut);
                }
                targetBook.close();
                FileTools.delete(tmpDataFile);
            } catch (Exception e) {
                MyBoxLog.error(e);
                return e.toString();
            }

        } else {
            try ( Workbook targetBook = new XSSFWorkbook()) {
                Sheet targetSheet = targetBook.createSheet();
                currentSheetName = targetSheet.getSheetName();
                int targetRowIndex = 0;
                if (withName) {
                    Row targetRow = targetSheet.createRow(targetRowIndex++);
                    for (int col = 0; col < colsCheck.length; col++) {
                        Cell targetCell = targetRow.createCell(col, CellType.STRING);
                        targetCell.setCellValue(colsCheck[col].getText());
                    }
                }
                for (TextField[] row : inputs) {
                    Row targetRow = targetSheet.createRow(targetRowIndex++);
                    for (int col = 0; col < row.length; col++) {
                        Cell targetCell = targetRow.createCell(col, CellType.STRING);
                        targetCell.setCellValue(row[col].getText());
                    }
                }
                try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                    targetBook.write(fileOut);
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
                return e.toString();
            }
        }
        if (FileTools.rename(tmpFile, file)) {
            saveClomnns(file, currentSheetName, otherSheetNames, withName);
            return null;
        } else {
            return "Failed";
        }
    }

    protected void saveClomnns(File file, String currentSheetName, List<String> otherSheetNames, boolean withName) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            if (currentSheetName != null) {
                String dname = file.getAbsolutePath() + "-" + currentSheetName;
                DataDefinition def = tableDataDefinition.read(conn, dataType, dname);
                if (def == null) {
                    def = DataDefinition.create().setDataType(dataType).setDataName(dname)
                            .setHasHeader(withName);
                    tableDataDefinition.insertData(conn, def);
                } else {
                    def.setHasHeader(withName);
                    tableDataDefinition.updateData(conn, def);
                }
                if (ColumnDefinition.valid(this, columns)) {
                    tableDataColumn.save(conn, def.getDfid(), columns);
                    conn.commit();
                }
            }
            for (String name : otherSheetNames) {
                String sourceDataName = sourceFile.getAbsolutePath() + "-" + name;
                DataDefinition sourceDef = tableDataDefinition.read(conn, dataType, sourceDataName);
                if (sourceDef == null) {
                    continue;
                }
                String targetDataName = file.getAbsolutePath() + "-" + name;
                DataDefinition targetDef = tableDataDefinition.read(conn, dataType, targetDataName);
                if (targetDef == null) {
                    targetDef = DataDefinition.create().setDataType(dataType).setDataName(targetDataName)
                            .setHasHeader(sourceDef.isHasHeader());
                    tableDataDefinition.insertData(conn, targetDef);
                } else {
                    targetDef.setHasHeader(sourceDef.isHasHeader());
                    tableDataDefinition.updateData(conn, targetDef);
                }
                List<ColumnDefinition> sourceColumns = tableDataColumn.read(conn, sourceDef.getDfid());
                if (sourceColumns == null || sourceColumns.isEmpty()) {
                    continue;
                }
                for (ColumnDefinition column : sourceColumns) {
                    column.setDataid(targetDef.getDfid());
                }
                tableDataColumn.save(conn, targetDef.getDfid(), sourceColumns);
                conn.commit();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void copyRow(Row sourceRow, Row targetRow) {
        if (sourceRow == null || targetRow == null) {
            return;
        }
        for (int col = sourceRow.getFirstCellNum(); col < sourceRow.getLastCellNum(); col++) {
            Cell sourceCell = sourceRow.getCell(col);
            if (sourceCell == null) {
                continue;
            }
            CellType type = sourceCell.getCellType();
            if (type == null) {
                type = CellType.STRING;
            }
            Cell targetCell = targetRow.createCell(col, type);
            copyCell(sourceCell, targetCell, type);
        }
    }

    protected void copyCell(Cell sourceCell, Cell targetCell, CellType type) {
        if (sourceCell == null || targetCell == null || type == null) {
            return;
        }
        try {
            switch (type) {
                case STRING:
                    targetCell.setCellValue(sourceCell.getStringCellValue());
                    break;
                case NUMERIC:
                    targetCell.setCellValue(sourceCell.getNumericCellValue());
                    break;
                case BLANK:
                    targetCell.setCellValue("");
                    break;
                case BOOLEAN:
                    targetCell.setCellValue(sourceCell.getBooleanCellValue());
                    break;
                case ERROR:
                    targetCell.setCellErrorValue(sourceCell.getErrorCellValue());
                    break;
                case FORMULA:
                    targetCell.setCellFormula(sourceCell.getCellFormula());
                    break;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void setCell(Cell targetCell, CellType type, String value) {
        if (value == null || targetCell == null || type == null) {
            return;
        }
        try {
            if (type == CellType.NUMERIC) {
                try {
                    long v = Long.parseLong(value);
                    targetCell.setCellValue(v);
                    return;
                } catch (Exception e) {
                }
                try {
                    double v = Double.parseDouble(value);
                    targetCell.setCellValue(v);
                    return;
                } catch (Exception e) {
                }
            }
            targetCell.setCellValue(value);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    protected void setAllColValues(int col) {
        if (sourceFile == null) {
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
                String[][] data;

                @Override
                protected boolean handle() {
                    File tmpFile = FileTools.getTempFile();
                    File tmpDataFile = FileTools.getTempFile();
                    FileTools.copyFile(sourceFile, tmpDataFile);
                    try ( Workbook sourceBook = WorkbookFactory.create(sourceFile);
                             Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
                        sheetsNumber = sourceBook.getNumberOfSheets();
                        if (currentSheet < 1 || currentSheet > sheetsNumber) {
                            currentSheet = 1;
                        }
                        Sheet sourceSheet = sourceBook.getSheetAt(currentSheet - 1);
                        targetBook.removeSheetAt(currentSheet - 1);
                        Sheet targetSheet = targetBook.createSheet(sourceSheet.getSheetName());
                        targetBook.setSheetOrder(targetSheet.getSheetName(), currentSheet - 1);
                        int offset = sourceWithNames ? 1 : 0;
                        int targetRowIndex = 0;
                        if (sourceWithNames) {
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            for (int col = 0; col < colsCheck.length; col++) {
                                Cell targetCell = targetRow.createCell(col);
                                targetCell.setCellValue(colsCheck[col].getText());
                            }
                        }
                        for (int sourceRowIndex = sourceSheet.getFirstRowNum() + offset; sourceRowIndex <= sourceSheet.getLastRowNum(); sourceRowIndex++) {
                            Row sourceRow = sourceSheet.getRow(sourceRowIndex);
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            if (sourceRow == null) {
                                continue;
                            }
                            int targetIndex = col + sourceRow.getFirstCellNum();
                            for (int cellIndex = sourceRow.getFirstCellNum(); cellIndex < sourceRow.getLastCellNum(); cellIndex++) {
                                CellType type = CellType.STRING;
                                Cell sourceCell = sourceRow.getCell(cellIndex);
                                if (sourceCell != null) {
                                    type = sourceCell.getCellType();
                                }
                                Cell targetCell = targetRow.createCell(cellIndex, type);
                                if (targetIndex == cellIndex) {
                                    setCell(targetCell, type, value);
                                } else {
                                    copyCell(sourceCell, targetCell, type);
                                }
                            }
                        }
                        try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                            targetBook.write(fileOut);
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                        return false;
                    }
                    FileTools.delete(tmpDataFile);
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
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
        if (sourceFile == null) {
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
                    try ( Workbook sourceBook = WorkbookFactory.create(sourceFile)) {
                        sheetsNumber = sourceBook.getNumberOfSheets();
                        if (currentSheet < 1 || currentSheet > sheetsNumber) {
                            currentSheet = 1;
                        }
                        Sheet sourceSheet = sourceBook.getSheetAt(currentSheet - 1);
                        int offset = sourceWithNames ? 1 : 0;
                        s = new StringBuilder();
                        for (int sourceRowIndex = sourceSheet.getFirstRowNum() + offset; sourceRowIndex <= sourceSheet.getLastRowNum(); sourceRowIndex++) {
                            Row sourceRow = sourceSheet.getRow(sourceRowIndex);
                            if (sourceRow == null) {
                                continue;
                            }
                            String v = null;
                            int cellIndex = sourceRow.getFirstCellNum() + col;
                            if (cellIndex < sourceRow.getLastCellNum()) {
                                v = cellString(sourceRow.getCell(cellIndex));
                            }
                            v = v == null ? "" : v;
                            s.append(v).append("\n");
                            copiedCol.add(v);
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e);
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
        if (sourceFile == null) {
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
                    File tmpDataFile = FileTools.getTempFile();
                    FileTools.copyFile(sourceFile, tmpDataFile);
                    try ( Workbook sourceBook = WorkbookFactory.create(sourceFile);
                             Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
                        sheetsNumber = sourceBook.getNumberOfSheets();
                        if (currentSheet < 1 || currentSheet > sheetsNumber) {
                            currentSheet = 1;
                        }
                        Sheet sourceSheet = sourceBook.getSheetAt(currentSheet - 1);
                        targetBook.removeSheetAt(currentSheet - 1);
                        Sheet targetSheet = targetBook.createSheet(sourceSheet.getSheetName());
                        targetBook.setSheetOrder(targetSheet.getSheetName(), currentSheet - 1);
                        int offset = sourceWithNames ? 1 : 0;
                        int targetRowIndex = 0;
                        if (sourceWithNames) {
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            for (int col = 0; col < colsCheck.length; col++) {
                                Cell targetCell = targetRow.createCell(col);
                                targetCell.setCellValue(colsCheck[col].getText());
                            }
                        }
                        int row = 0, csize = copiedCol.size();
                        for (int sourceRowIndex = sourceSheet.getFirstRowNum() + offset; sourceRowIndex <= sourceSheet.getLastRowNum(); sourceRowIndex++) {
                            Row sourceRow = sourceSheet.getRow(sourceRowIndex);
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            if (sourceRow == null) {
                                continue;
                            }
                            int targetIndex = col + sourceRow.getFirstCellNum();
                            for (int cellIndex = sourceRow.getFirstCellNum(); cellIndex < sourceRow.getLastCellNum(); cellIndex++) {
                                CellType type = CellType.STRING;
                                Cell sourceCell = sourceRow.getCell(cellIndex);
                                if (sourceCell != null) {
                                    type = sourceCell.getCellType();
                                }
                                Cell targetCell = targetRow.createCell(cellIndex, type);
                                if ((row < csize) && (targetIndex == cellIndex)) {
                                    setCell(targetCell, type, copiedCol.get(row));
                                } else {
                                    copyCell(sourceCell, targetCell, type);
                                }
                            }
                            row++;
                        }
                        try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                            targetBook.write(fileOut);
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                        return false;
                    }
                    FileTools.delete(tmpDataFile);
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
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
        if (sourceFile == null || totalSize <= 0
                || !checkBeforeNextAction()) {
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
                String[][] data;

                @Override
                protected boolean handle() {
                    File tmpFile = FileTools.getTempFile();
                    File tmpDataFile = FileTools.getTempFile();
                    FileTools.copyFile(sourceFile, tmpDataFile);
                    try ( Workbook sourceBook = WorkbookFactory.create(sourceFile);
                             Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
                        sheetsNumber = sourceBook.getNumberOfSheets();
                        if (currentSheet < 1 || currentSheet > sheetsNumber) {
                            currentSheet = 1;
                        }
                        Sheet sourceSheet = sourceBook.getSheetAt(currentSheet - 1);
                        targetBook.removeSheetAt(currentSheet - 1);
                        Sheet targetSheet = targetBook.createSheet(sourceSheet.getSheetName());
                        targetBook.setSheetOrder(targetSheet.getSheetName(), currentSheet - 1);
                        int offset = sourceWithNames ? 1 : 0;
                        int targetRowIndex = 0;
                        if (sourceWithNames) {
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            for (int col = 0; col < columns.size(); col++) {
                                Cell targetCell = targetRow.createCell(col);
                                targetCell.setCellValue(columns.get(col).getName());
                            }
                        }
                        for (int sourceRowIndex = sourceSheet.getFirstRowNum() + offset; sourceRowIndex <= sourceSheet.getLastRowNum(); sourceRowIndex++) {
                            Row sourceRow = sourceSheet.getRow(sourceRowIndex);
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            if (sourceRow == null) {
                                continue;
                            }
                            int targetCol = 0;
                            Cell targetCell;
                            for (int cellIndex = sourceRow.getFirstCellNum(); cellIndex < sourceRow.getLastCellNum(); cellIndex++) {
                                if (col == cellIndex && left) {
                                    targetCell = targetRow.createCell(targetCol++, CellType.STRING);
                                    setCell(targetCell, CellType.STRING, "");
                                }
                                CellType type = CellType.STRING;
                                Cell sourceCell = sourceRow.getCell(cellIndex);
                                if (sourceCell != null) {
                                    type = sourceCell.getCellType();
                                }
                                targetCell = targetRow.createCell(targetCol++, type);
                                copyCell(sourceCell, targetCell, type);
                                if (col == cellIndex && !left) {
                                    targetCell = targetRow.createCell(targetCol++, CellType.STRING);
                                    setCell(targetCell, CellType.STRING, "");
                                }
                            }
                        }
                        try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                            targetBook.write(fileOut);
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                        return false;
                    }
                    FileTools.delete(tmpDataFile);
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
        if (sourceFile == null) {
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
                String[][] data;

                @Override
                protected boolean handle() {
                    File tmpFile = FileTools.getTempFile();
                    File tmpDataFile = FileTools.getTempFile();
                    FileTools.copyFile(sourceFile, tmpDataFile);
                    try ( Workbook sourceBook = WorkbookFactory.create(sourceFile);
                             Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
                        sheetsNumber = sourceBook.getNumberOfSheets();
                        if (currentSheet < 1 || currentSheet > sheetsNumber) {
                            currentSheet = 1;
                        }
                        Sheet sourceSheet = sourceBook.getSheetAt(currentSheet - 1);
                        targetBook.removeSheetAt(currentSheet - 1);
                        Sheet targetSheet = targetBook.createSheet(sourceSheet.getSheetName());
                        targetBook.setSheetOrder(targetSheet.getSheetName(), currentSheet - 1);
                        int offset = sourceWithNames ? 1 : 0;
                        int targetRowIndex = 0;
                        if (sourceWithNames) {
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            for (int col = 0; col < columns.size(); col++) {
                                Cell targetCell = targetRow.createCell(col);
                                targetCell.setCellValue(columns.get(col).getName());
                            }
                        }
                        for (int sourceRowIndex = sourceSheet.getFirstRowNum() + offset; sourceRowIndex <= sourceSheet.getLastRowNum(); sourceRowIndex++) {
                            Row sourceRow = sourceSheet.getRow(sourceRowIndex);
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            if (sourceRow == null) {
                                continue;
                            }
                            int targetCol = 0;
                            Cell targetCell;
                            int targetIndex = col + sourceRow.getFirstCellNum();
                            for (int cellIndex = sourceRow.getFirstCellNum(); cellIndex < sourceRow.getLastCellNum(); cellIndex++) {
                                if (targetIndex == cellIndex) {
                                    continue;
                                }
                                CellType type = CellType.STRING;
                                Cell sourceCell = sourceRow.getCell(cellIndex);
                                if (sourceCell != null) {
                                    type = sourceCell.getCellType();
                                }
                                targetCell = targetRow.createCell(targetCol++, type);
                                copyCell(sourceCell, targetCell, type);
                            }
                        }
                        try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                            targetBook.write(fileOut);
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                        return false;
                    }
                    FileTools.delete(tmpDataFile);
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
                    + message("NoticeAllChangeUnrecover") + "\n" + message("DataFileOrderNotice"))) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    File tmpFile = FileTools.getTempFile();
                    File tmpDataFile = FileTools.getTempFile();
                    FileTools.copyFile(sourceFile, tmpDataFile);
                    try ( Workbook sourceBook = WorkbookFactory.create(sourceFile);
                             Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
                        sheetsNumber = sourceBook.getNumberOfSheets();
                        if (currentSheet < 1 || currentSheet > sheetsNumber) {
                            currentSheet = 1;
                        }
                        Sheet sourceSheet = sourceBook.getSheetAt(currentSheet - 1);
                        targetBook.removeSheetAt(currentSheet - 1);
                        int offset = sourceWithNames ? 1 : 0;
                        List<Row> records = new ArrayList<>();
                        for (int sourceRowIndex = sourceSheet.getFirstRowNum() + offset; sourceRowIndex <= sourceSheet.getLastRowNum(); sourceRowIndex++) {
                            Row sourceRow = sourceSheet.getRow(sourceRowIndex);
                            if (sourceRow == null) {
                                continue;
                            }
                            records.add(sourceRow);
                        }
                        if (records.isEmpty()) {
                            return false;
                        }
                        Collections.sort(records, new Comparator<Row>() {
                            @Override
                            public int compare(Row row1, Row row2) {
                                ColumnDefinition column = columns.get(col);
                                int v = column.compare(cellString(row1.getCell(col)), cellString(row2.getCell(col)));
                                return asc ? v : -v;
                            }
                        });
                        Sheet targetSheet = targetBook.createSheet(sourceSheet.getSheetName());
                        targetBook.setSheetOrder(targetSheet.getSheetName(), currentSheet - 1);
                        int targetRowIndex = 0;
                        if (sourceWithNames) {
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            for (int col = 0; col < columns.size(); col++) {
                                Cell targetCell = targetRow.createCell(col);
                                targetCell.setCellValue(columns.get(col).getName());
                            }
                        }
                        for (Row sourceRow : records) {
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            copyRow(sourceRow, targetRow);
                        }
                        try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                            targetBook.write(fileOut);
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                        return false;
                    }
                    FileTools.delete(tmpDataFile);
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
        if (sourceFile == null) {
            copySelectedCols();
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
                    try ( Workbook sourceBook = WorkbookFactory.create(sourceFile)) {
                        sheetsNumber = sourceBook.getNumberOfSheets();
                        if (currentSheet < 1 || currentSheet > sheetsNumber) {
                            currentSheet = 1;
                        }
                        Sheet sourceSheet = sourceBook.getSheetAt(currentSheet - 1);
                        int offset = sourceWithNames ? 1 : 0;
                        s = new StringBuilder();
                        String p = TextTools.delimiterText(delimiter);
                        for (int sourceRowIndex = sourceSheet.getFirstRowNum() + offset; sourceRowIndex <= sourceSheet.getLastRowNum(); sourceRowIndex++) {
                            Row sourceRow = sourceSheet.getRow(sourceRowIndex);
                            if (sourceRow == null) {
                                continue;
                            }
                            for (int cellIndex = sourceRow.getFirstCellNum(); cellIndex < sourceRow.getLastCellNum(); cellIndex++) {
                                if (colsCheck[cellIndex - sourceRow.getFirstCellNum()].isSelected()) {
                                    String d = cellString(sourceRow.getCell(cellIndex));
                                    s.append(d == null ? "" : d).append(p);
                                }
                            }
                            s.append("\n");
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                        return false;
                    }
                    return true;
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
    public void deleteSelectedCols() {
        if (sourceFile == null) {
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
                    File tmpDataFile = FileTools.getTempFile();
                    FileTools.copyFile(sourceFile, tmpDataFile);
                    try ( Workbook sourceBook = WorkbookFactory.create(sourceFile);
                             Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
                        sheetsNumber = sourceBook.getNumberOfSheets();
                        if (currentSheet < 1 || currentSheet > sheetsNumber) {
                            currentSheet = 1;
                        }
                        Sheet sourceSheet = sourceBook.getSheetAt(currentSheet - 1);
                        targetBook.removeSheetAt(currentSheet - 1);
                        Sheet targetSheet = targetBook.createSheet(sourceSheet.getSheetName());
                        targetBook.setSheetOrder(targetSheet.getSheetName(), currentSheet - 1);
                        int offset = sourceWithNames ? 1 : 0;
                        int targetRowIndex = 0;
                        if (sourceWithNames) {
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            for (int col = 0; col < columns.size(); col++) {
                                Cell targetCell = targetRow.createCell(col);
                                targetCell.setCellValue(columns.get(col).getName());
                            }
                        }
                        for (int sourceRowIndex = sourceSheet.getFirstRowNum() + offset; sourceRowIndex <= sourceSheet.getLastRowNum(); sourceRowIndex++) {
                            Row sourceRow = sourceSheet.getRow(sourceRowIndex);
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            if (sourceRow == null) {
                                continue;
                            }
                            int targetCol = 0;
                            Cell targetCell;
                            for (int cellIndex = sourceRow.getFirstCellNum(); cellIndex < sourceRow.getLastCellNum(); cellIndex++) {
                                if (colsCheck[cellIndex - sourceRow.getFirstCellNum()].isSelected()) {
                                    continue;
                                }
                                CellType type = CellType.STRING;
                                Cell sourceCell = sourceRow.getCell(cellIndex);
                                if (sourceCell != null) {
                                    type = sourceCell.getCellType();
                                }
                                targetCell = targetRow.createCell(targetCol++, type);
                                copyCell(sourceCell, targetCell, type);
                            }
                        }
                        try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                            targetBook.write(fileOut);
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                        return false;
                    }
                    FileTools.delete(tmpDataFile);
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
        if (sourceFile == null) {
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
                StringBuilder s;

                @Override
                protected boolean handle() {
                    File tmpFile = FileTools.getTempFile();
                    File tmpDataFile = FileTools.getTempFile();
                    FileTools.copyFile(sourceFile, tmpDataFile);
                    try ( Workbook sourceBook = WorkbookFactory.create(sourceFile);
                             Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
                        sheetsNumber = sourceBook.getNumberOfSheets();
                        if (currentSheet < 1 || currentSheet > sheetsNumber) {
                            currentSheet = 1;
                        }
                        Sheet sourceSheet = sourceBook.getSheetAt(currentSheet - 1);
                        targetBook.removeSheetAt(currentSheet - 1);
                        Sheet targetSheet = targetBook.createSheet(sourceSheet.getSheetName());
                        targetBook.setSheetOrder(targetSheet.getSheetName(), currentSheet - 1);
                        int offset = sourceWithNames ? 1 : 0;
                        int targetRowIndex = 0;
                        if (sourceWithNames) {
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            for (int col = 0; col < colsCheck.length; col++) {
                                Cell targetCell = targetRow.createCell(col);
                                targetCell.setCellValue(colsCheck[col].getText());
                            }
                        }
                        for (int sourceRowIndex = sourceSheet.getFirstRowNum() + offset; sourceRowIndex <= sourceSheet.getLastRowNum(); sourceRowIndex++) {
                            Row sourceRow = sourceSheet.getRow(sourceRowIndex);
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            if (sourceRow == null) {
                                continue;
                            }
                            for (int cellIndex = sourceRow.getFirstCellNum(); cellIndex < sourceRow.getLastCellNum(); cellIndex++) {
                                CellType type = CellType.STRING;
                                Cell sourceCell = sourceRow.getCell(cellIndex);
                                if (sourceCell != null) {
                                    type = sourceCell.getCellType();
                                }
                                Cell targetCell = targetRow.createCell(cellIndex, type);
                                if (colsCheck[cellIndex - sourceRow.getFirstCellNum()].isSelected()) {
                                    setCell(targetCell, type, value);
                                } else {
                                    copyCell(sourceCell, targetCell, type);
                                }
                            }
                        }
                        try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                            targetBook.write(fileOut);
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                        return false;
                    }
                    FileTools.delete(tmpDataFile);
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
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

    public static DataFileExcelController oneOpen() {
        DataFileExcelController controller = null;
        Stage stage = FxmlStage.findStage(message("EditExcel"));
        if (stage != null && stage.getUserData() != null) {
            try {
                controller = (DataFileExcelController) stage.getUserData();
            } catch (Exception e) {
            }
        }
        if (controller == null) {
            controller = (DataFileExcelController) FxmlStage.openStage(CommonValues.DataFileExcelFxml);
        }
        if (controller != null) {
            controller.getMyStage().toFront();
        }
        return controller;
    }

}
