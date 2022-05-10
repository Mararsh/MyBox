package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Operations.ObjectType;
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
public abstract class BaseData2DHandleController extends BaseChildController {

    protected ControlData2DEditTable tableController;
    protected Data2D data2D;
    protected List<List<String>> outputData;
    protected List<Data2DColumn> outputColumns;
    protected int scale, defaultScale = 2;
    protected ObjectType objectType;

    @FXML
    protected ControlData2DSource sourceController;
    @FXML
    protected ControlData2DTarget targetController;
    @FXML
    protected CheckBox rowNumberCheck, colNameCheck;
    @FXML
    protected Label dataLabel, infoLabel, dataSelectionLabel;
    @FXML
    protected ComboBox<String> scaleSelector;
    @FXML
    protected ToggleGroup objectGroup;
    @FXML
    protected RadioButton columnsRadio, rowsRadio, allRadio;

    public BaseData2DHandleController() {
        baseTitle = message("Handle");
    }

    @Override
    public void setStageStatus() {
        setAsNormal();
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            objectType = ObjectType.Columns;
            if (objectGroup != null) {
                objectGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                        objectChanged();
                    }
                });
            }

            scale = (short) UserConfig.getInt(baseName + "Scale", defaultScale);
            if (scale < 0) {
                scale = defaultScale;
            }
            if (scaleSelector != null) {
                scaleSelector.getItems().addAll(
                        Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
                );
                scaleSelector.setValue(scale + "");
                scaleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        scaleChanged();
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean scaleChanged() {
        try {
            int v = Integer.parseInt(scaleSelector.getValue());
            if (v >= 0 && v <= 15) {
                scale = (short) v;
                UserConfig.setInt(baseName + "Scale", v);
                scaleSelector.getEditor().setStyle(null);
                return true;
            } else {
                scaleSelector.getEditor().setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            scaleSelector.getEditor().setStyle(UserConfig.badStyle());
        }
        return false;
    }

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            this.tableController = tableController;

            sourceController.setParameters(this, tableController);

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

            setSourceLabel(message("SelectRowsColumnsToHanlde"));

            afterInit();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void afterInit() {
        checkOptions();
    }

    public void objectChanged() {
        if (rowsRadio.isSelected()) {
            objectType = ObjectType.Rows;
        } else if (allRadio != null && allRadio.isSelected()) {
            objectType = ObjectType.All;
        } else {
            objectType = ObjectType.Columns;
        }
    }

    public void rowNumberCheckChanged() {
        UserConfig.setBoolean(baseName + "CopyRowNumber", rowNumberCheck.isSelected());
    }

    public void setSourceLabel(String message) {
        sourceController.setLabel(message);
    }

    public boolean checkOptions() {
        if (tableController == null) {
            return false;
        }
        data2D = tableController.data2D;
        getMyStage().setTitle(baseTitle + (data2D == null ? "" : " - " + data2D.displayName()));

        if (dataLabel != null && data2D != null) {
            dataLabel.setText(data2D.displayName());
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
            outputColumns = sourceController.checkedCols();
            if (showRowNumber()) {
                outputColumns.add(0, new Data2DColumn(message("SourceRowNumber"), ColumnDefinition.ColumnType.Long));
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
                handledCSV.setColumns(outputColumns);
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
                if (targetController == null || targetController.inTable()) {
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
            outputData = sourceController.selectedData(showRowNumber());
            if (outputData == null) {
                return false;
            }
            if (showColNames()) {
                List<String> names = sourceController.checkedColsNames();
                if (showRowNumber()) {
                    names.add(0, message("SourceRowNumber"));
                }
                outputData.add(0, names);
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
                tableController.data2D.moveDownStyles(index, newRows.size());
                tableController.tableData.addAll(index, newRows);
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
                || outputData == null || outputData.isEmpty()) {
            popError(message("NoData"));
            return false;
        }
        switch (targetController.target) {
            case "systemClipboard":
                tableController.copyToSystemClipboard(null, outputData);
                break;
            case "myBoxClipboard":
                tableController.copyToMyBoxClipboard2(outputColumns, outputData);
                break;
            case "csv":
                DataFileCSVController.open(outputColumns, outputData);
                break;
            case "excel":
                DataFileExcelController.open(outputColumns, outputData);
                break;
            case "texts":
                DataFileTextController.open(outputColumns, outputData);
                break;
            case "matrix":
                MatricesManageController controller = MatricesManageController.oneOpen();
                controller.dataController.loadTmpData(outputColumns, outputData);
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
            outputData = null;
            outputColumns = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        get/set
     */
    public ControlData2DEditTable getTableController() {
        return tableController;
    }

    public Data2D getData2D() {
        return data2D;
    }

    public List<Data2DColumn> getOutputColumns() {
        return outputColumns;
    }

    public ControlData2DSource getSourceController() {
        return sourceController;
    }

    public ControlData2DTarget getTargetController() {
        return targetController;
    }

}
