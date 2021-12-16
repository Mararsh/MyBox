package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.layout.HBox;
import mara.mybox.data.Data2D;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-11-28
 * @License Apache License Version 2.0
 */
public abstract class Data2DOperationController extends BaseController {

    protected ControlData2DEditTable tableController;
    protected Data2D data2D;
    protected List<Integer> checkedRowsIndices, checkedColsIndices;
    protected List<String> selectedNames, handledNames;
    protected List<List<String>> handledData;
    protected List<Data2DColumn> selectedColumns, handledColumns;
    protected String value;

    @FXML
    protected ControlData2DSelect selectController;
    @FXML
    protected ControlData2DTarget targetController;
    @FXML
    protected HBox namesBox;
    @FXML
    protected CheckBox rowNumberCheck, colNameCheck;
    @FXML
    protected Label noNumberLabel;

    @Override
    public void setStageStatus() {
        setAsPop(baseName);
    }

    public void setParameters(ControlData2DEditTable tableController,
            boolean sourceAll, boolean numberCols, boolean targetTable) {
        try {
            this.tableController = tableController;
            data2D = tableController.data2D;

            selectController.setParameters(tableController, sourceAll, numberCols);
            if (targetController != null) {
                targetController.setParameters(this, targetTable ? tableController : null);
            }

            getMyStage().setTitle(tableController.getBaseTitle());

            selectController.rowGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkAllData();
                }
            });

            if (namesBox != null && targetController != null) {
                namesBox.setVisible(!targetController.isTable());
                targetController.targetGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                        namesBox.setVisible(!targetController.isTable());
                    }
                });
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

            checkNumberCols();

            checkAllData();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkAllData() {

    }

    public void checkNumberCols() {
        if (selectController.numberCols && noNumberLabel != null) {
            List<String> numberColumnNames = data2D.numberColumnNames();
            noNumberLabel.setVisible(numberColumnNames == null || numberColumnNames.isEmpty());
            okButton.setDisable(noNumberLabel.isVisible());
        }
    }

    public void refreshControls() {
        checkNumberCols();
        selectController.refreshAction();
        if (targetController != null) {
            targetController.refreshControls();
        }
    }

    public boolean checkOptions() {
        if (selectController.isAllData() && data2D.isMutiplePages()
                && !tableController.dataController.checkBeforeNextAction()) {
            return false;
        }
        checkedRowsIndices = selectController.checkedRowsIndices();
        checkedColsIndices = selectController.checkedColsIndices();
        if (checkedColsIndices == null || checkedColsIndices.isEmpty()
                || checkedRowsIndices == null || checkedRowsIndices.isEmpty()) {
            popError(message("SelectToHandle"));
            return false;
        }
        return true;
    }

    @FXML
    @Override
    public synchronized void okAction() {
        if (!checkOptions()) {
            return;
        }
        task = new SingletonTask<Void>(this) {

            boolean forTable;

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(task);
                    forTable = targetController != null ? targetController.isTable() : false;
                    if (forTable) {
                        return handleForTable();
                    } else {
                        return handleForExternal();
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (forTable) {
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
                refreshControls();
            }

        };
        start(task);
    }

    public List<List<String>> selectedData() {
        List<List<String>> selectedData;
        if (!selectController.isAllData()) {
            selectedData = selectController.selectedData();
            if (selectedData == null || selectedData.isEmpty()) {
                outError(message("SelectToHandle"));
                return null;
            }
        } else if (tableController.data2D.isMutiplePages()) {
            selectedData = null;
        } else {
            selectedData = selectController.pageData();
        }
        if (selectedData != null && (targetController == null || !targetController.isTable())
                && rowNumberCheck != null && rowNumberCheck.isSelected()) {
            for (int i = 0; i < selectedData.size(); i++) {
                List<String> row = selectedData.get(i);
                row.add(0, (i + 1) + "");
            }
        }
        return selectedData;
    }

    public boolean checkColumns() {
        if (checkedColsIndices == null || checkedColsIndices.isEmpty()) {
            outError(message("SelectToHandle"));
            return false;
        }
        selectedColumns = new ArrayList<>();
        for (int col : checkedColsIndices) {
            selectedColumns.add(data2D.getColumns().get(col));
        }
        if ((targetController == null || !targetController.isTable())
                && (colNameCheck == null || colNameCheck.isSelected())) {
            selectedNames = new ArrayList<>();
            for (Data2DColumn col : selectedColumns) {
                selectedNames.add(col.getName());
            }
            if (rowNumberCheck != null && rowNumberCheck.isSelected()) {
                selectedNames.add(0, message("RowNumber"));
                selectedColumns.add(0, new Data2DColumn(message("RowNumber"), ColumnDefinition.ColumnType.String));
            }
        } else {
            selectedNames = null;
        }
        return true;
    }

    public boolean hanldeData() {
        handledData = selectedData();
        handledNames = selectedNames;
        handledColumns = selectedColumns;
        return true;
    }

    public boolean handleForTable() {
        return checkColumns() && hanldeData();
    }

    public boolean handleForExternal() {
        return checkColumns() && hanldeData();
    }

    public boolean updateTable() {
        try {
            if (targetController == null || handledData == null) {
                return false;
            }
            List<List<String>> newRows = new ArrayList<>();
            int colsNumber = tableController.data2D.tableColsNumber();
            for (List<String> dataRow : handledData) {
                List<String> newRow = tableController.data2D.newRow();
                for (int c = 0; c < Math.min(dataRow.size(), colsNumber); c++) {
                    newRow.set(c + 1, dataRow.get(c));
                }
                newRows.add(newRow);
            }
            int index = targetController.tableIndex();
            tableController.isSettingValues = true;
            tableController.tableData.addAll(index, newRows);
            tableController.tableView.scrollTo(index - 5);
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
                tableController.copyToSystemClipboard(handledNames, handledData);
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

    public void outError(String error) {
        if (task != null) {
            task.setError(error);
        } else {
            popError(error);
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

}
