package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.color.CIEData;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-6-2
 * @License Apache License Version 2.0
 */
public class XYZController extends BaseController {

    protected double x, y, z;
    protected double[] relative;
    protected ValueType valueType;
    protected int scale = 8;

    public enum ValueType {
        Relative, Normalized, Tristimulus
    }

    @FXML
    protected TextField xInput, yInput, zInput;
    @FXML
    protected ToggleGroup valueGroup;
    @FXML
    protected Label xLabel, yLabel, zLabel, commentsLabel;

    public XYZController() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            valueGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkValueType();
                }
            });

            xInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkInputs();
                }
            });
            yInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkInputs();
                }
            });
            zInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkInputs();
                }
            });

            checkValueType();

        } catch (Exception e) {

        }
    }

    public void checkValueType() {
        isSettingValues = true;
        xInput.setText("");
        yInput.setText("");
        zInput.setText("");
        yInput.setDisable(false);
        zInput.setDisable(false);
        if (commentsLabel != null) {
            commentsLabel.setText("");
            commentsLabel.setStyle(null);
        }

        RadioButton selected = (RadioButton) valueGroup.getSelectedToggle();
        if (message("NormalizedValuesCC").equals(selected.getText())) {
            valueType = ValueType.Normalized;
            xLabel.setText("x");
            yLabel.setText("y");
            zLabel.setText("z");
            zInput.setDisable(true);
        } else if (message("Tristimulus").equals(selected.getText())) {
            valueType = ValueType.Tristimulus;
            xLabel.setText("X'");
            yLabel.setText("Y'");
            zLabel.setText("Z'");
        } else {
            valueType = ValueType.Relative;
            xLabel.setText("X");
            yLabel.setText("Y");
            yInput.setText("1.0");
            yInput.setDisable(true);
            zLabel.setText("Z");
        }
        isSettingValues = false;
        checkValues();
    }

    public void checkValues() {
        checkInputs();
    }

    public void checkInputs() {
        if (isSettingValues) {
            return;
        }
        if (commentsLabel != null) {
            commentsLabel.setText("");
            commentsLabel.setStyle(null);
        }
        try {
            double v = Double.parseDouble(xInput.getText());
            if (v < 0) {
                xInput.setStyle(badStyle);
                return;
            } else {
                if (valueType == ValueType.Normalized) {
                    if (v > 1.0) {
                        xInput.setStyle(badStyle);
                        if (commentsLabel != null) {
                            commentsLabel.setText(message("NormalizeError"));
                            commentsLabel.setStyle(badStyle);
                        }
                        return;
                    }
                }
                x = v;
                xInput.setStyle(null);
            }
        } catch (Exception e) {
            xInput.setStyle(badStyle);
            return;
        }

        try {
            double v = Double.parseDouble(yInput.getText());
            if (v < 0) {
                yInput.setStyle(badStyle);
                return;
            } else {
                if (valueType == ValueType.Normalized) {
                    if (v > 1.0) {
                        yInput.setStyle(badStyle);
                        if (commentsLabel != null) {
                            commentsLabel.setText(message("NormalizeError"));
                            commentsLabel.setStyle(badStyle);
                        }
                        return;
                    }
                }
                y = v;
                yInput.setStyle(null);
            }
        } catch (Exception e) {
            yInput.setStyle(badStyle);
            return;
        }

        if (valueType == ValueType.Normalized) {
            isSettingValues = true;
            z = DoubleTools.scale(1 - x - y, scale);
            zInput.setText(z + "");
            isSettingValues = false;
        } else {
            try {
                double v = Double.parseDouble(zInput.getText());
                z = v;
                zInput.setStyle(null);
            } catch (Exception e) {
                zInput.setStyle(badStyle);
                return;
            }
        }
        relative = CIEData.relative(x, y, z);

    }

}
