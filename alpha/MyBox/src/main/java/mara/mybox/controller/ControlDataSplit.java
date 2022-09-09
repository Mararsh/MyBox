package mara.mybox.controller;

import java.util.Arrays;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-9-6
 * @License Apache License Version 2.0
 */
public class ControlDataSplit extends BaseController {

    protected double from, to, interval;
    protected int number, scale;
    protected boolean byInterval;

    @FXML
    protected TextField fromInput, toInput, numberInput, intervalInput;
    @FXML
    protected RadioButton numberRadio, intervalRadio;
    @FXML
    protected ComboBox<String> scaleSelector;

    public void setParameters(String name) {
        try {
            baseName = baseName + "_" + name;

            from = UserConfig.getDouble(baseName + "From", -10);
            fromInput.setText(from + "");

            to = UserConfig.getDouble(baseName + "To", 10);
            toInput.setText(to + "");

            number = UserConfig.getInt(baseName + "Number", 100);
            numberInput.setText(number + "");

            interval = UserConfig.getDouble(baseName + "Inteval", 0.1);
            intervalInput.setText(interval + "");

            byInterval = UserConfig.getBoolean(baseName + "ByInterval", false);
            if (byInterval) {
                intervalRadio.setSelected(true);
            } else {
                numberRadio.setSelected(true);
            }

            scale = UserConfig.getInt(baseName + "Scale", 8);
            if (scale < 0) {
                scale = 8;
            }
            scaleSelector.getItems().addAll(
                    Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
            );
            scaleSelector.getSelectionModel().select(scale + "");

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public boolean checkInputs() {
        try {
            super.initControls();
            try {
                from = Double.valueOf(fromInput.getText().trim());
                fromInput.setStyle(null);
            } catch (Exception e) {
                fromInput.setStyle(UserConfig.badStyle());
                popError(message("InvalidParamter") + ": " + message("From"));
                return false;
            }

            try {
                double t = Double.valueOf(toInput.getText().trim());
                if (t < from) {
                    toInput.setStyle(UserConfig.badStyle());
                    popError(message("InvalidParamter") + ": " + message("To"));
                    return false;
                }
                to = t;
                toInput.setStyle(null);
            } catch (Exception e) {
                toInput.setStyle(UserConfig.badStyle());
                popError(message("InvalidParamter") + ": " + message("To"));
                return false;
            }

            byInterval = intervalRadio.isSelected();

            if (byInterval) {
                numberInput.setStyle(null);
                try {
                    interval = Double.valueOf(intervalInput.getText().trim());
                    intervalInput.setStyle(null);
                } catch (Exception e) {
                    intervalInput.setStyle(UserConfig.badStyle());
                    popError(message("InvalidParamter") + ": " + message("DataInterval"));
                    return false;
                }
            } else {
                intervalInput.setStyle(null);
                try {
                    int n = Integer.valueOf(numberInput.getText().trim());
                    if (n <= 0) {
                        numberInput.setStyle(UserConfig.badStyle());
                        popError(message("InvalidParamter") + ": " + message("NumberOfSplit"));
                        return false;
                    }
                    number = n;
                    numberInput.setStyle(null);
                } catch (Exception e) {
                    numberInput.setStyle(UserConfig.badStyle());
                    popError(message("InvalidParamter") + ": " + message("NumberOfSplit"));
                    return false;
                }
            }

            try {
                int v = Integer.parseInt(scaleSelector.getValue());
                if (v >= 0) {
                    scale = v;
                    scaleSelector.getEditor().setStyle(null);
                } else {
                    scaleSelector.getEditor().setStyle(UserConfig.badStyle());
                    popError(message("InvalidParamter") + ": " + message("DecimalScale"));
                    return false;
                }
            } catch (Exception e) {
                scaleSelector.getEditor().setStyle(UserConfig.badStyle());
                popError(message("InvalidParamter") + ": " + message("DecimalScale"));
                return false;
            }

            UserConfig.setDouble(baseName + "From", from);
            UserConfig.setDouble(baseName + "To", to);
            UserConfig.setDouble(baseName + "Interval", interval);
            UserConfig.setInt(baseName + "Number", number);

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

}
