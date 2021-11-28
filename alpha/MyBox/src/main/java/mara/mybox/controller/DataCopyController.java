package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-11-28
 * @License Apache License Version 2.0
 */
public class DataCopyController extends BaseController {

    protected ControlData2DEditTable tableController;
    protected String value;

    @FXML
    protected ControlListCheckBox rowsListController, colsListController;
    @FXML
    protected ToggleGroup locationGroup;
    @FXML
    protected RadioButton frontRadio, endRadio, belowRadio, aboveRadio, scRadio, mcRadio;
    @FXML
    protected ComboBox<String> rowSelector;
    @FXML
    protected HBox rowBox;
    @FXML
    protected Button selectAllRowsButton, selectNoneRowsButton, selectAllColsButton, selectNoneColsButton;

    @Override
    public void setStageStatus() {
        setAsPopup(baseName);
    }

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            this.tableController = tableController;
            this.baseName = tableController.baseName;

            getMyStage().setTitle(tableController.getBaseTitle());

            rowsListController.setParent(tableController);
            colsListController.setParent(tableController);

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
            locationGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    UserConfig.setString(baseName + "CopyRowsLocation", ((RadioButton) newValue).getText());
                    rowBox.setVisible(belowRadio.isSelected() || aboveRadio.isSelected());
                }
            });

            makeControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void makeControls() {
        try {
            List<String> rows = new ArrayList<>();
            for (long i = 0; i < tableController.tableData.size(); i++) {
                rows.add("" + (i + 1));
            }
            rowsListController.setValues(rows);

            colsListController.setValues(tableController.data2D.columnNames());

            rowSelector.getItems().setAll(rows);
            rowSelector.getSelectionModel().select(0);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void selectAllRows() {
        rowsListController.checkAll();
    }

    @FXML
    public void selectNoneRows() {
        rowsListController.checkNone();
    }

    @FXML
    public void selectAllCols() {
        colsListController.checkAll();
    }

    @FXML
    public void selectNoneCols() {
        colsListController.checkNone();
    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (scRadio.isSelected() || mcRadio.isSelected()) {
                copyToClipboard();
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
            int colsNumber = tableController.data2D.columnsNumber();
            for (int row = 0; row < tableController.tableData.size(); row++) {
                if (!rowsListController.isChecked(row)) {
                    continue;
                }
                List<String> dataRow = tableController.tableData.get(row);
                List<String> newRow = tableController.newData();
                for (int col = 0; col < colsNumber; col++) {
                    if (!colsListController.isChecked(col)) {
                        continue;
                    }
                    newRow.set(col + 1, dataRow.get(col + 1));
                }
                newRows.add(newRow);
            }
            tableController.isSettingValues = true;
            tableController.tableData.addAll(index, newRows);
            tableController.tableView.scrollTo(index - 5);
            tableController.isSettingValues = false;
            tableController.tableChanged(true);

            makeControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void copyToClipboard() {
        try {
            List<List<String>> data = new ArrayList<>();
            List<String> names = new ArrayList<>();
            int colsNumber = tableController.data2D.columnsNumber();
            for (int col = 0; col < colsNumber; col++) {
                if (colsListController.isChecked(col)) {
                    names.add(colsListController.value(col));
                }
            }
            for (int row = 0; row < tableController.tableData.size(); row++) {
                if (!rowsListController.isChecked(row)) {
                    continue;
                }
                List<String> dataRow = tableController.tableData.get(row);
                List<String> newRow = new ArrayList<>();
                for (int col = 0; col < colsNumber; col++) {
                    if (!colsListController.isChecked(col)) {
                        continue;
                    }
                    newRow.add(dataRow.get(col + 1));
                }
                data.add(newRow);
            }

            if (scRadio.isSelected()) {
                String text = TextTools.dataText(data, ",", names, null);
                TextClipboardTools.copyToSystemClipboard(this, text);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    /*
        static
     */
    public static DataCopyController open(ControlData2DEditTable tableController) {
        try {
            DataCopyController controller = (DataCopyController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.DataCopyFxml, false);
            controller.setParameters(tableController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
