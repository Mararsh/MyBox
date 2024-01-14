package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DTargetsController extends BaseData2DHandleController {

    @FXML
    protected ControlData2DTarget targetController;
    @FXML
    protected ComboBox<String> colSelector;

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (colSelector != null) {
                colSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkOptions();
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setParameters(ControlData2DLoad tableController) {
        try {
            super.setParameters(tableController);

            if (targetController != null) {
                targetController.setParameters(this, tableController);
            }

            if (rowNumberCheck != null) {
                rowNumberCheck.setSelected(UserConfig.getBoolean(baseName + "CopyRowNumber", false));
                rowNumberCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        rowNumberCheckChanged();
                    }
                });
            }
            if (colNameCheck != null) {
                colNameCheck.setSelected(UserConfig.getBoolean(baseName + "CopyColNames", true));
                colNameCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "CopyColNames", colNameCheck.isSelected());
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void refreshControls() {
        try {
            if (colSelector != null) {
                List<String> names = data2D.columnNames();
                if (names == null || names.isEmpty()) {
                    colSelector.getItems().clear();
                    return;
                }
                String selectedCol = colSelector.getSelectionModel().getSelectedItem();
                isSettingValues = true;
                colSelector.getItems().setAll(names);
                if (selectedCol != null && names.contains(selectedCol)) {
                    colSelector.setValue(selectedCol);
                } else {
                    colSelector.getSelectionModel().select(0);
                }
                isSettingValues = false;
            }

            super.refreshControls();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void rowNumberCheckChanged() {
        UserConfig.setBoolean(baseName + "CopyRowNumber", rowNumberCheck.isSelected());
    }

    // Check when selections are changed
    @Override
    public boolean checkOptions() {
        try {
            if (isSettingValues) {
                return true;
            }
            if (targetController != null) {
                targetController.setNotInTable(isAllPages());
                if (targetController.checkTarget() == null) {
                    outOptionsError(message("SelectToHandle") + ": " + message("Target"));
                    return false;
                }
            }
            return super.checkOptions();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    protected void startOperation() {
        try {
            if (isAllPages()) {
                handleAllTask();
            } else {
                handleRowsTask();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void handleAllTask() {
        if (targetController == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {

            private DataFileCSV csvFile;

            @Override
            protected boolean handle() {
                data2D.startTask(this, filterController.filter);
                csvFile = generatedFile();
                data2D.stopFilter();
                return csvFile != null;
            }

            @Override
            protected void whenSucceeded() {
                popDone();
                DataFileCSV.openCSV(myController, csvFile, targetController.target);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
            }

        };
        start(task);
    }

    public DataFileCSV generatedFile() {
        return null;
    }

    public void handleRowsTask() {
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(this, filterController.filter);
                    ok = handleRows();
                    data2D.stopFilter();
                    return ok;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                ouputRows();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                if (targetController != null) {
                    targetController.refreshControls();
                }
            }

        };
        start(task);
    }

    public boolean handleRows() {
        try {
            outputData = tableFiltered(showRowNumber());
            if (outputData == null) {
                return false;
            }
            if (showColNames()) {
                List<String> names = new ArrayList<>();
                for (Data2DColumn column : outputColumns) {
                    names.add(column.getColumnName());
                }
                outputData.add(0, names);
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public void ouputRows() {
        if (targetController == null || targetController.inTable()) {
            updateTable();
        } else {
            outputExternal();
        }
    }

    public boolean updateTable() {
        try {
            if (targetController == null || !targetController.inTable() || outputData == null) {
                return false;
            }
            int row = targetController.row();
            int col = targetController.col();
            int rowsNumber = tableController.data2D.tableRowsNumber();
            int colsNumber = tableController.data2D.tableColsNumber();
            if (row < 0 || row >= rowsNumber || col < 0 || col >= colsNumber) {
                popError(message("InvalidParameters"));
                return false;
            }
            tableController.isSettingValues = true;
            if (targetController.replaceRadio.isSelected()) {
                for (int r = row; r < Math.min(row + outputData.size(), rowsNumber); r++) {
                    List<String> tableRow = tableController.tableData.get(r);
                    List<String> dataRow = outputData.get(r - row);
                    for (int c = col; c < Math.min(col + dataRow.size(), colsNumber); c++) {
                        tableRow.set(c + 1, dataRow.get(c - col));
                    }
                    tableController.tableData.set(r, tableRow);
                }
            } else {
                List<List<String>> newRows = new ArrayList<>();
                for (int r = 0; r < outputData.size(); r++) {
                    List<String> newRow = tableController.data2D.newRow();
                    List<String> dataRow = outputData.get(r);
                    for (int c = col; c < Math.min(col + dataRow.size(), colsNumber); c++) {
                        newRow.set(c + 1, dataRow.get(c - col));
                    }
                    newRows.add(newRow);
                }
                int index = targetController.insertRadio.isSelected() ? row : row + 1;
                tableController.tableData.addAll(index, newRows);
            }
            tableController.tableView.refresh();
            tableController.isSettingValues = false;
            tableController.tableChanged(true);
            tableController.requestMouse();
            popDone();
            return true;
        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean outputExternal() {
        if (targetController == null || targetController.target == null
                || outputData == null || outputData.isEmpty()) {
            popError(message("NoData"));
            return false;
        }
        String name = targetController.name();
        switch (targetController.target) {
            case "systemClipboard":
                tableController.copyToSystemClipboard(null, outputData);
                break;
            case "myBoxClipboard":
                tableController.toMyBoxClipboard(name, outputColumns, outputData);
                break;
            case "csv":
                DataFileCSVController.open(name, outputColumns, outputData);
                break;
            case "excel":
                DataFileExcelController.open(name, outputColumns, outputData);
                break;
            case "texts":
                DataFileTextController.open(name, outputColumns, outputData);
                break;
            case "matrix":
                MatricesManageController.open(name, outputColumns, outputData);
                break;
            case "table":
                DataTablesController.open(name, outputColumns, outputData);
                break;
            default:
                Data2DTargetExportController.open(outputColumns, outputData, targetController.target, name);
        }
        popDone();
        return true;
    }

}
