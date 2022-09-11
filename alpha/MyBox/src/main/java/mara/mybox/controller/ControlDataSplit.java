package mara.mybox.controller;

import javafx.fxml.FXML;
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
    protected int number;
    protected boolean byInterval;

    @FXML
    protected TextField fromInput, toInput, numberInput, intervalInput;
    @FXML
    protected RadioButton numberRadio, intervalRadio;

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
