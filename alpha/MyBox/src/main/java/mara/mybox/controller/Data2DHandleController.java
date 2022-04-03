package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFile;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public abstract class Data2DHandleController extends BaseChildController {

    protected ControlData2DEditTable tableController;
    protected Data2D data2D;
    protected List<List<String>> handledData;
    protected List<Data2DColumn> handledColumns;

    @FXML
    protected ControlData2DSource sourceController;
    @FXML
    protected ControlData2DTarget targetController;
    @FXML
    protected CheckBox rowNumberCheck, colNameCheck;
    @FXML
    protected Label dataLabel, infoLabel;

    @Override
    public void setStageStatus() {
        setAsNormal();
    }

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            this.tableController = tableController;

            sourceController.setParameters(this, tableController);
            sourceController.setLabel(message("SelectRowsColumnsToHanlde"));

            if (targetController != null) {
                targetController.setParameters(this, tableController);
            }

            if (rowNumberCheck != null) {
                rowNumberCheck.setSelected(UserConfig.getBoolean(baseName + "CopyRowNumber", false));
                rowNumberCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "CopyRowNumber", rowNumberCheck.isSelected());
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

            sourceController.loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });
            sourceController.selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });
            checkOptions();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean checkOptions() {
        if (tableController == null) {
            return false;
        }
        data2D = tableController.data2D;
        getMyStage().setTitle(tableController.getTitle());

        if (dataLabel != null) {
            dataLabel.setText(tableController.data2D.displayName());
        }
        if (infoLabel != null) {
            infoLabel.setText("");
        }
        if (!sourceController.checkSelections()) {
            if (infoLabel != null) {
                infoLabel.setText(message("SelectToHandle"));
            }
            okButton.setDisable(true);
            return false;
        }
        if (targetController != null && targetController.checkTarget() == null) {
            if (infoLabel != null) {
                infoLabel.setText(message("SelectToHandle"));
            }
            okButton.setDisable(true);
            return false;
        }
        okButton.setDisable(false);
        return true;
    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (!checkOptions()) {
                return;
            }
            handledColumns = sourceController.checkedCols();
            if (showRowNumber()) {
                handledColumns.add(0, new Data2DColumn(message("SourceRowNumber"), ColumnDefinition.ColumnType.Long));
            }
            if (sourceController.allPages()) {
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
        task = new SingletonTask<Void>(this) {

            private DataFile handledFile;

            @Override
            protected boolean handle() {
                data2D.setTask(task);
                DataFileCSV handledCSV = generatedFile();
                if (handledCSV == null) {
                    return false;
                }
                handledCSV.setColumns(handledColumns);
                handledFile = handledCSV.convert(myController, handledCSV, targetController.target);
                return handledFile != null;
            }

            @Override
            protected void whenSucceeded() {
                popDone();
                handledFile.output(myController, handledFile, targetController.target);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.setTask(null);
                task = null;
            }

        };
        start(task);
    }

    public DataFileCSV generatedFile() {
        return null;
    }

    public synchronized void handleRowsTask() {
        if (targetController == null) {
            return;
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(task);
                    return handleRows();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (targetController.inTable()) {
                    updateTable();
                } else {
                    outputExternal();
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.setTask(null);
                task = null;
                if (targetController != null) {
                    targetController.refreshControls();
                }
            }

        };
        start(task);
    }

    public boolean showColNames() {
        return colNameCheck != null && colNameCheck.isSelected();
    }

    public boolean showRowNumber() {
        return rowNumberCheck != null && rowNumberCheck.isSelected();
    }

    public boolean handleRows() {
        try {
            handledData = sourceController.selectedData(showRowNumber());
            if (handledData == null) {
                return false;
            }
            if (showColNames()) {
                List<String> names = sourceController.checkedColsNames();
                if (showRowNumber()) {
                    names.add(0, message("SourceRowNumber"));
                }
                handledData.add(0, names);
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public boolean updateTable() {
        try {
            if (targetController == null || !targetController.inTable() || handledData == null) {
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
                for (int r = row; r < Math.min(row + handledData.size(), rowsNumber); r++) {
                    List<String> tableRow = tableController.tableData.get(r);
                    List<String> dataRow = handledData.get(r - row);
                    for (int c = col; c < Math.min(col + dataRow.size(), colsNumber); c++) {
                        tableRow.set(c + 1, dataRow.get(c - col));
                    }
                    tableController.tableData.set(r, tableRow);
                }
            } else {
                List<List<String>> newRows = new ArrayList<>();
                for (int r = 0; r < handledData.size(); r++) {
                    List<String> newRow = tableController.data2D.newRow();
                    List<String> dataRow = handledData.get(r);
                    for (int c = col; c < Math.min(col + dataRow.size(), colsNumber); c++) {
                        newRow.set(c + 1, dataRow.get(c - col));
                    }
                    newRows.add(newRow);
                }
                tableController.tableData.addAll(targetController.insertRadio.isSelected() ? row : row + 1, newRows);
            }
            tableController.tableView.refresh();
            tableController.isSettingValues = false;
            tableController.tableChanged(true);
            popDone();
            return true;
        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public boolean outputExternal() {
        if (targetController == null || targetController.target == null
                || handledData == null || handledData.isEmpty()) {
            popError(message("NoData"));
            return false;
        }
        switch (targetController.target) {
            case "systemClipboard":
                tableController.copyToSystemClipboard(null, handledData);
                break;
            case "myBoxClipboard":
                tableController.copyToMyBoxClipboard2(handledColumns, handledData);
                break;
            case "csv":
                DataFileCSVController.open(handledColumns, handledData);
                break;
            case "excel":
                DataFileExcelController.open(handledColumns, handledData);
                break;
            case "texts":
                DataFileTextController.open(handledColumns, handledData);
                break;
            case "matrix":
                MatricesManageController controller = MatricesManageController.oneOpen();
                controller.dataController.loadTmpData(handledColumns, handledData);
                break;
        }
        popDone();
        return true;
    }

    @Override
    public void cleanPane() {
        try {
            tableController = null;
            data2D = null;
            handledData = null;
            handledColumns = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
