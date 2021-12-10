package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-11-28
 * @License Apache License Version 2.0
 */
public class Data2DCopyController extends BaseController {

    protected ControlData2DEditTable tableController;
    protected List<Integer> selectedColumnsIndices, selectedRowsIndices;
    protected String value;

    @FXML
    protected ControlData2DSelect selectController;
    @FXML
    protected ToggleGroup locationGroup;
    @FXML
    protected RadioButton frontRadio, endRadio, belowRadio, aboveRadio, scRadio, mcRadio;
    @FXML
    protected ComboBox<String> rowSelector;
    @FXML
    protected HBox rowBox, namesBox;
    @FXML
    protected CheckBox rowNumberCheck, colNameCheck;

    @Override
    public void setStageStatus() {
        setAsPop(baseName);
    }

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            this.tableController = tableController;
            selectController.setParameters(tableController);
            getMyStage().setTitle(tableController.getBaseTitle());

            String location = UserConfig.getString(baseName + "CopyRowsLocation", message("Front"));
            if (location == null || message("Front").equals(location)) {
                frontRadio.fire();
            } else if (message("End").equals(location)) {
                endRadio.fire();
            } else if (message("Below").equals(location)) {
                belowRadio.fire();
            } else if (message("Above").equals(location)) {
                aboveRadio.fire();
            } else {
                frontRadio.fire();
            }
            rowBox.setVisible(belowRadio.isSelected() || aboveRadio.isSelected());
            namesBox.setVisible(scRadio.isSelected() || mcRadio.isSelected());
            locationGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    UserConfig.setString(baseName + "CopyRowsLocation", ((RadioButton) newValue).getText());
                    rowBox.setVisible(belowRadio.isSelected() || aboveRadio.isSelected());
                    namesBox.setVisible(scRadio.isSelected() || mcRadio.isSelected());
                }
            });

            rowNumberCheck.setSelected(UserConfig.getBoolean(baseName + "CopyRowNumber", false));
            rowNumberCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "CopyRowNumber", rowNumberCheck.isSelected());
                }
            });
            colNameCheck.setSelected(UserConfig.getBoolean(baseName + "CopyColNames", true));
            colNameCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "CopyColNames", colNameCheck.isSelected());
                }
            });

            refreshControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void refreshControls() {
        try {
            int thisSelect = rowSelector.getSelectionModel().getSelectedIndex();
            List<String> rows = new ArrayList<>();
            for (long i = 0; i < tableController.tableData.size(); i++) {
                rows.add("" + (i + 1));
            }
            rowSelector.getItems().setAll(rows);
            int tableSelect = tableController.tableView.getSelectionModel().getSelectedIndex();
            rowSelector.getSelectionModel().select(tableSelect >= 0 ? tableSelect : (thisSelect >= 0 ? thisSelect : 0));

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

            if (scRadio.isSelected() || mcRadio.isSelected()) {
                List<List<String>> data = selectController.selectedData();
                if (data == null || data.isEmpty()) {
                    popError(message("NoData"));
                    return;
                }
                if (rowNumberCheck.isSelected()) {
                    for (int i = 0; i < data.size(); i++) {
                        List<String> row = data.get(i);
                        row.add(0, message("Row") + (i + 1));
                    }
                }
                List<String> names;
                if (colNameCheck.isSelected()) {
                    names = selectController.selectedColumnsNames();
                    if (rowNumberCheck.isSelected()) {
                        names.add(0, message("RowNumber"));
                    }
                } else {
                    names = null;
                }
                if (scRadio.isSelected()) {
                    tableController.copyToSystemClipboard(names, data);
                } else {
                    tableController.copyToMyBoxClipboard(names, data);
                }

            } else {
                copyToTable();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void copyToTable() {
        try {
            int index = tableController.tableData.size();
            if (frontRadio.isSelected()) {
                index = 0;
            } else if (index < 0 || endRadio.isSelected()) {
                index = tableController.tableData.size();
            } else if (belowRadio.isSelected()) {
                index++;
            }
            List<List<String>> newRows = new ArrayList<>();
            for (int row : selectedRowsIndices) {
                List<String> tableRow = tableController.tableData.get(row);
                List<String> newRow = tableController.data2D.newRow();
                for (int col : selectedColumnsIndices) {
                    newRow.set(col + 1, tableRow.get(col + 1));
                }
                newRows.add(newRow);
            }
            tableController.isSettingValues = true;
            tableController.tableData.addAll(index, newRows);
            tableController.tableView.scrollTo(index - 5);
            tableController.isSettingValues = false;
            tableController.tableChanged(true);

            popDone();
            refreshControls();
            selectController.refreshControls();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        static
     */
    public static Data2DCopyController open(ControlData2DEditTable tableController) {
        try {
            Data2DCopyController controller = (Data2DCopyController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DCopyFxml, false);
            controller.setParameters(tableController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
