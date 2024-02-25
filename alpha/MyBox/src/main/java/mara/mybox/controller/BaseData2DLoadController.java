package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.input.Clipboard;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2DTools;
import mara.mybox.data2d.DataClipboard;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFileExcel;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.TmpTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Data2DDefinition.DataType;
import static mara.mybox.db.data.Data2DDefinition.DataType.CSV;
import static mara.mybox.db.data.Data2DDefinition.DataType.Excel;
import static mara.mybox.db.data.Data2DDefinition.DataType.MyBoxClipboard;
import static mara.mybox.db.data.Data2DDefinition.DataType.Texts;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.DoubleMatrixTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-12-17
 * @License Apache License Version 2.0
 */
public class BaseData2DLoadController extends BaseData2DTableController {

    /*
        data
     */
    public boolean createData(DataType type) {
        if (!checkBeforeNextAction()) {
            return false;
        }
        resetStatus();
        data2D = Data2D.create(type);
        setData(data2D);
        dataSizeLoaded = true;
        postLoadedTableData();
        return true;
    }

    public void setData(Data2D data) {
        try {
            if (data == null) {
                data2D = Data2D.create(DataType.CSV);
            } else {
                data2D = data;
            }
            tableData2DDefinition = data2D.getTableData2DDefinition();
            tableData2DColumn = data2D.getTableData2DColumn();

            showPaginationPane(!data2D.isTmpData() && !data2D.isMatrix());

//            switch (data2D.getType()) {
//                case CSV:
//                case MyBoxClipboard:
//                    setFileType(VisitHistory.FileType.CSV);
//                    break;
//                case Excel:
//                    setFileType(VisitHistory.FileType.Excel);
//                    break;
//                case Texts:
//                    setFileType(VisitHistory.FileType.Text);
//                    break;
//                default:
//                    setFileType(VisitHistory.FileType.CSV);
//            }
            validateData();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean loadDef(Data2DDefinition def) {
        if (def == null) {
            return createData(DataType.CSV);
        }
        if (!checkBeforeNextAction()) {
            return false;
        }
        resetStatus();
        data2D = Data2D.create(def.getType());
        data2D.cloneAll(def);
        setData(data2D);
        readDefinition();
        return true;
    }

    public boolean loadNull() {
        return createData(DataType.CSV);
    }

    public synchronized void readDefinition() {
        if (data2D == null) {
            loadNull();
            return;
        }
        if (!checkInvalidFile()) {
            return;
        }
        resetStatus();
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    data2D.startTask(this, null);
                    data2D.readDataDefinition(conn);
                    if (isCancelled()) {
                        return false;
                    }
                    return data2D.readColumns(conn);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                Data2D s = data2D;
                data2D = null;
                resetView(false);
                data2D = s;
                loadPage();
            }

        };
        start(task, thisPane);
    }

    public boolean checkInvalidFile() {
        if (data2D == null) {
            return false;
        }
        File file = data2D.getFile();
        if (file == null || file.exists()) {
            return true;
        }
        FxTask nullTask = new FxTask<Void>(this) {
            @Override
            protected boolean handle() {
                try {
                    tableData2DDefinition.deleteData(data2D);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                loadNull();
            }
        };
        start(nullTask, false);
        return false;
    }

    public void loadData(List<String> cols, List<List<String>> data) {
        loadData(null, data2D.toColumns(cols), data);
    }

    public void loadData(String name, List<Data2DColumn> cols, List<List<String>> data) {
        try {
            if (data2D == null) {
                data2D = Data2D.create(DataType.CSV);
            } else if (!checkBeforeNextAction()) {
                return;
            } else {
                data2D.resetData();
            }
            resetStatus();
            List<Data2DColumn> columns = new ArrayList<>();
            if (cols == null || cols.isEmpty()) {
                data2D.setHasHeader(false);
                if (data != null && !data.isEmpty()) {
                    for (int i = 0; i < data.get(0).size(); i++) {
                        Data2DColumn column = new Data2DColumn(data2D.colPrefix() + (i + 1), data2D.defaultColumnType());
                        columns.add(column);
                    }
                }
            } else {
                data2D.setHasHeader(true);
                for (Data2DColumn col : cols) {
                    columns.add(col.cloneAll());
                }
            }
            for (Data2DColumn column : columns) {
                column.setIndex(data2D.newColumnIndex());
            }
            data2D.setColumns(columns);
            StringTable validateTable = Data2DTools.validate(columns);
            List<List<String>> rows = new ArrayList<>();
            if (data != null) {
                for (int i = 0; i < data.size(); i++) {
                    List<String> row = new ArrayList<>();
                    row.add("-1");
                    row.addAll(data.get(i));
                    rows.add(row);
                }
            }
            data2D.checkForLoad();
            data2D.setDataName(name);
            resetView(false);
            setData(data2D);
            displayTmpData(rows);
            if (validateTable != null && !validateTable.isEmpty()) {
                validateTable.htmlTable();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean displayTmpData(List<List<String>> newData) {
        try {
            makeColumns();
            isSettingValues = true;
            tableData.setAll(newData);
            data2D.setPageData(tableData);
            isSettingValues = false;
            dataSizeLoaded = true;
            postLoadedTableData();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void loadCSVData(DataFileCSV csvData) {
        if (csvData == null || csvData.getFile() == null || !csvData.getFile().exists()) {
            popError("Nonexistent");
            return;
        }
        if (data2D == null) {
            data2D = Data2D.create(DataType.CSV);
        } else if (!checkBeforeNextAction()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {
            private Data2D targetData;

            @Override
            protected boolean handle() {
                try {
                    switch (data2D.getType()) {
                        case Texts:
                            targetData = csvData.cloneAll();
                            targetData.setType(Data2DDefinition.DataType.Texts).setD2did(-1);
                            targetData.saveAttributes();
                            recordFileWritten(targetData.getFile(), VisitHistory.FileType.Text);
                            break;
                        case CSV:
                            targetData = csvData;
                            targetData.saveAttributes();
                            recordFileWritten(targetData.getFile(), VisitHistory.FileType.CSV);
                            break;
                        case Excel: {
                            DataFileExcel excelData = DataFileExcel.toExcel(this, csvData);
                            if (excelData != null) {
                                recordFileWritten(excelData.getFile(), VisitHistory.FileType.Excel);
                            }
                            targetData = excelData;
                            break;
                        }
                        case DatabaseTable: {
                            String name = csvData.dataName();
                            if (name.startsWith(TmpTable.TmpTablePrefix)
                                    || name.startsWith(TmpTable.TmpTablePrefix.toLowerCase())) {
                                name = name.substring(TmpTable.TmpTablePrefix.length());
                            }
                            DataTable dataTable = csvData.toTable(this, name);
                            targetData = dataTable;
                            break;
                        }
                        case MyBoxClipboard: {
                            DataClipboard clip = DataClipboard.toClip(this, csvData);
                            targetData = clip;
                            break;
                        }
                        case Matrix: {
                            DataMatrix matrix = DataMatrix.toMatrix(this, csvData);
                            targetData = matrix;
                            break;
                        }
                    }
                    return targetData != null;
                } catch (Exception e) {
                    error = e.toString();
                    MyBoxLog.console(error);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                loadDef(targetData);
            }

        };
        start(task, thisPane);
    }

    public void loadTableData(DataTable dataTable) {
        if (dataTable == null) {
            popError("Nonexistent");
            return;
        }
        if (data2D == null) {
            data2D = Data2D.create(DataType.CSV);
        } else if (!checkBeforeNextAction()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {
            private Data2D targetData;

            @Override
            protected boolean handle() {
                try {
                    switch (data2D.getType()) {
                        case Texts:
                            targetData = DataTable.toText(this, dataTable);
                            if (targetData != null) {
                                recordFileWritten(targetData.getFile(), VisitHistory.FileType.Text);
                            }
                            break;
                        case CSV:
                            targetData = DataTable.toCSV(this, dataTable);
                            if (targetData != null) {
                                recordFileWritten(targetData.getFile(), VisitHistory.FileType.CSV);
                            }
                            break;
                        case Excel: {
                            targetData = DataTable.toExcel(this, dataTable);
                            if (targetData != null) {
                                recordFileWritten(targetData.getFile(), VisitHistory.FileType.Excel);
                            }
                            break;
                        }
                        case DatabaseTable: {
                            targetData = dataTable;
                            break;
                        }
                        case MyBoxClipboard: {
                            targetData = DataTable.toClip(this, dataTable);
                            break;
                        }
                        case Matrix: {
                            targetData = DataTable.toMatrix(this, dataTable);
                            break;
                        }
                    }
                    return targetData != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                loadDef(targetData);
            }
        };
        start(task, thisPane);
    }

    public void loadMatrix(double[][] matrix) {
        if (matrix == null || matrix.length == 0) {
            loadNull();
            return;
        }
        loadData(null, data2D.tmpColumns(matrix[0].length), DoubleMatrixTools.toList(matrix));
    }

    @Override
    public void sourceFileChanged(File file) {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            resetStatus();
            setData(Data2D.create(Data2DDefinition.type(file)));
            data2D.initFile(file);
            readDefinition();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        action
     */
    @FXML
    public void renameAction(BaseTablePagesController parent, int index, Data2DDefinition targetData) {
        String newName = PopTools.askValue(getTitle(), message("CurrentName") + ":" + targetData.getDataName(),
                message("NewName"), targetData.getDataName() + "m");
        if (newName == null || newName.isBlank()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private Data2DDefinition def;

            @Override
            protected boolean handle() {
                targetData.setDataName(newName);
                def = tableData2DDefinition.updateData(targetData);
                return def != null;
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                if (parent != null) {
                    parent.tableData.set(index, def);
                }
                if (def.getD2did() == data2D.getD2did()) {
                    data2D.setDataName(newName);
                    if (parent != null) {
                        parent.updateStatus();
                    }
                    updateStatus();
                }

            }

        };
        start(task);
    }

    @FXML
    @Override
    public void copyToSystemClipboard() {
        if (!validateData()) {
            return;
        }
        Data2DCopyController controller = Data2DCopyController.open(this);
        controller.targetController.systemClipboardRadio.setSelected(true);
    }

    public void copyToSystemClipboard(List<String> names, List<List<String>> data) {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            String text = TextTools.rowsText(null, data, ",", names, null);
            TextClipboardTools.copyToSystemClipboard(this, text);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void copyToMyBoxClipboard(List<String> names, List<List<String>> data) {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            toMyBoxClipboard(null, data2D.toColumns(names), data);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void toMyBoxClipboard(String name, List<Data2DColumn> cols, List<List<String>> data) {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            FxTask copyTask = new FxTask<Void>(this) {

                private DataClipboard clip;

                @Override
                protected boolean handle() {
                    clip = DataClipboard.create(task, name, cols, data);
                    return clip != null;
                }

                @Override
                protected void whenSucceeded() {
                    DataInMyBoxClipboardController.open(clip);
                    popDone();
                }

            };
            start(copyTask, false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void pasteContentInSystemClipboard() {
        try {
            if (data2D == null) {
                return;
            }
            String text = Clipboard.getSystemClipboard().getString();
            if (text == null || text.isBlank()) {
                popError(message("NoTextInClipboard"));
            }
            Data2DPasteContentInSystemClipboardController.open(this, text);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void pasteContentInMyboxClipboard() {
        try {
            if (data2D == null) {
                return;
            }
            Data2DPasteContentInMyBoxClipboardController.open(this);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void myBoxClipBoard() {
        Data2DPasteContentInMyBoxClipboardController.open(this);
    }

    @FXML
    public void verifyAction() {
        StringTable results = verifyResults();
        if (results.isEmpty()) {
            popInformation(message("AllValuesValid"), 5000);
            return;
        }
        results.htmlTable();
    }

    public StringTable verifyResults() {
        try {
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("Row"), message("Column"), message("Invalid")));
            StringTable stringTable = new StringTable(names, data2D.displayName());
            for (int r = 0; r < tableData.size(); r++) {
                List<String> dataRow = tableData.get(r);
                for (int c = 0; c < data2D.columnsNumber(); c++) {
                    Data2DColumn column = data2D.column(c);
                    if (column.isAuto()) {
                        continue;
                    }
                    String value = dataRow.get(c + 1);
                    String item = null;
                    if (column.isNotNull() && (value == null || value.isBlank())) {
                        item = message("Null");
                    } else if (!column.validValue(value)) {
                        item = message(column.getType().name());
                    } else if (!data2D.validValue(value)) {
                        item = message("TextDataComments");
                    }
                    if (item == null) {
                        continue;
                    }
                    List<String> invalid = new ArrayList<>();
                    invalid.addAll(Arrays.asList((r + 1) + "", column.getColumnName(), item));
                    stringTable.add(invalid);
                }
            }
            return stringTable;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
