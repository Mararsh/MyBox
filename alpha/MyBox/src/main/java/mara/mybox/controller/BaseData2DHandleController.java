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
import javafx.scene.image.ImageView;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Operations.ObjectType;
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
public abstract class BaseData2DHandleController extends BaseData2DSourceController {

    protected List<List<String>> outputData;
    protected List<Data2DColumn> outputColumns;
    protected int scale, defaultScale = 2;
    protected ObjectType objectType;
    protected double invalidAs;

    @FXML
    protected ControlData2DTarget targetController;
    @FXML
    protected CheckBox rowNumberCheck, colNameCheck;
    @FXML
    protected Label infoLabel, dataSelectionLabel;
    @FXML
    protected ComboBox<String> scaleSelector;
    @FXML
    protected ToggleGroup objectGroup;
    @FXML
    protected RadioButton columnsRadio, rowsRadio, allRadio,
            skipNonnumericRadio, zeroNonnumericRadio;
    @FXML
    protected ImageView tableTipsView;
    @FXML
    protected ComboBox<String> colSelector;

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
                objectChanged();
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

            if (skipNonnumericRadio != null) {
                if (UserConfig.getBoolean(baseName + "SkipNonnumeric", true)) {
                    skipNonnumericRadio.setSelected(true);
                } else {
                    zeroNonnumericRadio.setSelected(true);
                }
            }

            if (colSelector != null) {
                colSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkOptions();
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
            setParameters(this, tableController);

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

            loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });
            selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void refreshControls() {
        try {
            super.refreshControls();

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

            checkOptions();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void sourceChanged() {
        if (tableController == null) {
            return;
        }
        super.sourceChanged();
        getMyStage().setTitle(baseTitle + (data2D == null ? "" : " - " + data2D.displayName()));
    }

    public void objectChanged() {
        checkObject();
    }

    public void checkObject() {
        if (rowsRadio == null) {
            return;
        }
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

    // Check when selections are changed
    public boolean checkOptions() {
        try {
            if (isSettingValues) {
                return true;
            }
            outOptionsError(null);
            if (data2D == null || !data2D.hasData()) {
                outOptionsError(message("NoData"));
                return false;
            }
            if (!checkSelections()) {
                return false;
            }
            if (targetController != null) {
                targetController.setNotInTable(isAllPages());
                if (targetController.checkTarget() == null) {
                    outOptionsError(message("SelectToHandle") + ": " + message("Target"));
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void outOptionsError(String error) {
        if (error != null && !error.isBlank()) {
            popError(error);
        }
    }

    // Check when "OK"/"Start" button is clicked
    public boolean initData() {
        try {
            checkObject();

            if (skipNonnumericRadio != null) {
                invalidAs = skipNonnumericRadio.isSelected() ? Double.NaN : 0;
            } else {
                invalidAs = 0;
            }

            outputColumns = checkedColumns;
            if (showRowNumber()) {
                outputColumns.add(0, new Data2DColumn(message("SourceRowNumber"), ColumnDefinition.ColumnType.Long));
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (!checkOptions() || !initData()) {
            return;
        }
        preprocessStatistic();
    }

    public void preprocessStatistic() {
        String script = data2D.filterScipt();
        if (script == null || script.isBlank()) {
            startOperation();
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                data2D.setTask(task);
                ok = data2D.fillFilterStatistic();
                return ok;
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                task = null;
                if (ok) {
                    startOperation();
                }
            }

        };
        start(task);
    }

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
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            private DataFileCSV csvFile;

            @Override
            protected boolean handle() {
                data2D.startTask(task, filterController.filter);
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
            protected void whenFailed() {
                if (isCancelled()) {
                    return;
                }
                outTaskError(error);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                task = null;
            }

        };
        start(task);
    }

    public DataFileCSV generatedFile() {
        return null;
    }

    public void handleRowsTask() {
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(task, filterController.filter);
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
            protected void whenFailed() {
                if (isCancelled()) {
                    return;
                }
                outTaskError(error);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
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
            outputData = filtered(showRowNumber());
            if (outputData == null) {
                return false;
            }
            if (showColNames()) {
                List<String> names = checkedColsNames;
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
        }
        popDone();
        return true;
    }

    public void outTaskError(String error) {
        if (error != null && !error.isBlank()) {
            //https://db.apache.org/derby/docs/10.15/ref/rrefsqljvarsamp.html#rrefsqljvarsamp
            if (error.contains("java.sql.SQLDataException: 22003 : [0] DOUBLE")) {
                alertError(error + "\n\n" + message("DataOverflow"));
            } else {
                alertError(error);
            }
        } else {
            popFailed();
        }
    }

    public void cloneOptions(BaseData2DHandleController sourceController) {
        if (sourceController.allPagesRadio.isSelected()) {
            allPagesRadio.setSelected(true);
        } else if (sourceController.currentPageRadio.isSelected()) {
            currentPageRadio.setSelected(true);
        } else {
            selectedRadio.setSelected(true);
        }
        UserConfig.setLong(baseName + "MaxDataNumber", sourceController.filterController.maxData);
        filterController.maxInput.setText(sourceController.filterController.maxData + "");
        filterController.load(sourceController.filterController.scriptInput.getText(),
                sourceController.filterController.othersRadio.isSelected());
        scaleSelector.getSelectionModel().select(sourceController.scale + "");
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    @Override
    public boolean keyESC() {
        close();
        return false;
    }

    @Override
    public boolean keyF6() {
        close();
        return false;
    }

    @Override
    public void cleanPane() {
        try {
            super.cleanPane();
            tableController = null;
            data2D = null;
            outputData = null;
            outputColumns = null;
        } catch (Exception e) {
        }
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

    public ControlData2DTarget getTargetController() {
        return targetController;
    }

}
