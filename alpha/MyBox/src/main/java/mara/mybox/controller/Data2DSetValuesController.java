package mara.mybox.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public class Data2DSetValuesController extends BaseController {

    protected ControlData2DEditTable tableController;
    protected String value;

    @FXML
    protected ControlListCheckBox rowsListController, colsListController;
    @FXML
    protected ToggleGroup valueGroup;
    @FXML
    protected RadioButton zeroRadio, oneRadio, blankRadio, randomRadio, setRadio;
    @FXML
    protected TextField valueInput;
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
            rowsListController.setParent(tableController);
            colsListController.setParent(tableController);

            getMyStage().setTitle(tableController.getBaseTitle());

            value = UserConfig.getString(baseName + "Value", "0");
            switch (value) {
                case "0":
                    zeroRadio.fire();
                    break;
                case "1":
                    oneRadio.fire();
                    break;
                case "blank":
                    blankRadio.fire();
                    break;
                case AppValues.MyBoxRandomFlag:
                    randomRadio.fire();
                    break;
                default:
                    valueInput.setText(value);
                    setRadio.fire();
            }
            valueGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    valueInput.setStyle(null);
                    if (setRadio.isSelected()) {
                        value = valueInput.getText();
                    } else if (zeroRadio.isSelected()) {
                        value = "0";
                    } else if (oneRadio.isSelected()) {
                        value = "1";
                    } else if (blankRadio.isSelected()) {
                        value = " ";
                    } else if (randomRadio.isSelected()) {
                        value = AppValues.MyBoxRandomFlag;
                    }
                    UserConfig.setString(baseName + "Value", value);
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

            colsListController.setValues(tableController.data2D.editableColumnNames());
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
            if (setRadio.isSelected()) {
                value = valueInput.getText();
            } else if (zeroRadio.isSelected()) {
                value = "0";
            } else if (oneRadio.isSelected()) {
                value = "1";
            } else if (blankRadio.isSelected()) {
                value = " ";
            } else if (randomRadio.isSelected()) {
                value = AppValues.MyBoxRandomFlag;
            }
            UserConfig.setString(baseName + "Value", value);

            Random random = new Random();
            tableController.isSettingValues = true;
            boolean rowChanged = false, colChanged;
            Map<String, Integer> colIndex = new HashMap<>();
            for (String name : colsListController.getValues()) {
                colIndex.put(name, tableController.data2D.tableCol(name));
            }
            for (int row = 0; row < tableController.tableData.size(); row++) {
                if (!rowsListController.isChecked(row)) {
                    continue;
                }
                List<String> values = tableController.tableData.get(row);
                colChanged = false;
                for (int i = 0; i < colsListController.size(); i++) {
                    if (!colsListController.isChecked(i)) {
                        continue;
                    }
                    int col = colIndex.get(colsListController.value(i));
                    if (col < 0) {
                        continue;
                    }
                    String v = value;
                    if (randomRadio.isSelected()) {
                        v = tableController.data2D.random(random, col);
                    }
                    values.set(col + 1, v);
                    colChanged = true;
                }
                if (colChanged) {
                    tableController.tableData.set(row, values);
                    rowChanged = true;
                }
            }
            tableController.isSettingValues = false;
            if (rowChanged) {
                tableController.tableChanged(true);
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
    public static Data2DSetValuesController open(ControlData2DEditTable tableController) {
        try {
            Data2DSetValuesController controller = (Data2DSetValuesController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DSetValuesFxml, false);
            controller.setParameters(tableController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
