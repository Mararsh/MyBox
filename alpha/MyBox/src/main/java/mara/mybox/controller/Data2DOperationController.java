package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Toggle;
import javafx.scene.layout.HBox;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-11-28
 * @License Apache License Version 2.0
 */
public abstract class Data2DOperationController extends BaseController {

    protected ControlData2DEditTable tableController;
    protected List<Integer> selectedColumnsIndices, selectedRowsIndices;
    protected List<List<String>> selectedData, handledData;
    protected List<String> selectedNames, handledNames;
    protected boolean sourceAll;
    protected String value;

    @FXML
    protected ControlData2DSelect selectController;
    @FXML
    protected ControlData2DTarget targetController;
    @FXML
    protected HBox namesBox;
    @FXML
    protected CheckBox rowNumberCheck, colNameCheck;

    @Override
    public void setStageStatus() {
        setAsPop(baseName);
    }

    public void setParameters(ControlData2DEditTable tableController, boolean sourceAll, boolean targetTable) {
        try {
            this.tableController = tableController;
            getMyStage().setTitle(tableController.getBaseTitle());

            selectController.setParameters(tableController, sourceAll);
            targetController.setParameters(this, targetTable ? tableController : null);

            if (namesBox != null) {
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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            selectedRowsIndices = selectController.selectedRowsIndices();
            selectedColumnsIndices = selectController.selectedColumnsIndices();
            if (selectedColumnsIndices.isEmpty() || selectedRowsIndices.isEmpty()) {
                popError(message("SelectToHandle"));
                return;
            }

            boolean ok;
            if (targetController.isTable()) {
                ok = handleForTable();
            } else {
                ok = handleForExternal();
            }
            if (!ok) {
                return;
            }
            popDone();
            refreshControls();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void refreshControls() {
        selectController.refreshControls();
        targetController.refreshControls();
    }

    public boolean checkData() {
        sourceAll = selectController.isAllData();
        boolean isTargetNotTable = !targetController.isTable();
        if (!sourceAll) {
            selectedData = selectController.selectedData();
            if (selectedData == null || selectedData.isEmpty()) {
                popError(message("NoData"));
                return false;
            }
            if (isTargetNotTable && rowNumberCheck != null && rowNumberCheck.isSelected()) {
                for (int i = 0; i < selectedData.size(); i++) {
                    List<String> row = selectedData.get(i);
                    row.add(0, (i + 1) + "");
                }
            }
        } else {
            selectedData = null;
        }

        if (isTargetNotTable && (colNameCheck == null || colNameCheck.isSelected())) {
            selectedNames = selectController.selectedColumnsNames();
            if (rowNumberCheck != null && rowNumberCheck.isSelected()) {
                selectedNames.add(0, message("RowNumber"));
            }

        } else {
            selectedNames = null;
        }
        return true;
    }

    public boolean hanldeData() {
        return false;
    }

    public boolean handleForTable() {
        if (!checkData() || !hanldeData()) {
            return false;
        }
        return updateTable();
    }

    public boolean handleForExternal() {
        if (!checkData() || !hanldeData()) {
            return false;
        }
        return outputExternal();
    }

    public boolean updateTable() {
        try {
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
            tableController.isSettingValues = false;
            tableController.tableChanged(true);
            return true;
        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public boolean outputExternal() {
        if (targetController.target == null || handledData == null || handledData.isEmpty()) {
            return false;
        }
        switch (targetController.target) {
            case "systemClipboard":
                tableController.copyToSystemClipboard(handledNames, handledData);
                break;
            case "myBoxClipboard":
                tableController.copyToMyBoxClipboard(handledNames, handledData);
                break;
            case "csv":
                DataFileCSVController.open(handledNames, handledData);
                break;
            case "excel":
                DataFileExcelController.open(handledNames, handledData);
                break;
            case "texts":
                DataFileTextController.open(handledNames, handledData);
                break;
            case "matrix":
                MatricesManageController controller = MatricesManageController.oneOpen();
                controller.dataController.loadTmpData(handledNames, handledData);
                break;
        }
        return true;
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

}
