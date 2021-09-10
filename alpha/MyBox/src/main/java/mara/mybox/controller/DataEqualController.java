package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppValues;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public class DataEqualController extends BaseDataOperationController {

    protected String value;

    @FXML
    protected ToggleGroup valueGroup;
    @FXML
    protected RadioButton zeroRadio, oneRadio, blankRadio, randomRadio, setRadio;
    @FXML
    protected TextField valueInput;

    @Override
    public void setParameters(ControlSheet sheetController, int row, int col) {
        try {
            super.setParameters(sheetController, row, col);

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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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

            List<Integer> cols = cols();
            if (rowAllRadio.isSelected()) {
                sheetController.setCols(cols, value);

            } else {
                sheetController.setRowsCols(rows(), cols, value);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
