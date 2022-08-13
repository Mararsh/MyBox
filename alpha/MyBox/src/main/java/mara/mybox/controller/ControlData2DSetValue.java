package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import mara.mybox.data.SetValue;
import mara.mybox.data2d.Data2D;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public class ControlData2DSetValue extends BaseController {

    protected BaseData2DHandleController handleController;
    protected Data2D data2D;
    protected SetValue setValue;

    @FXML
    protected ToggleGroup valueGroup;
    @FXML
    protected RadioButton zeroRadio, oneRadio, blankRadio, randomRadio, randomNnRadio,
            valueRadio, prefixRadio, suffixRadio, numberRadio, expressionRadio,
            gaussianDistributionRadio, identifyRadio, upperTriangleRadio, lowerTriangleRadio;
    @FXML
    protected TextField valueInput, prefixInput, suffixInput, startInput, digitInput;
    @FXML
    protected FlowPane matrixPane, numberPane;
    @FXML
    protected ControlData2DRowExpression expressionController;
    @FXML
    protected CheckBox errorContinueCheck, fillZeroCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();

            setValue = new SetValue();
            String valueType = UserConfig.getString(baseName + "ValueType", "Value");
            if (valueType == null) {
                valueType = "Value";
            }
            switch (valueType) {
                case "Zero":
                    zeroRadio.fire();
                    break;
                case "One":
                    oneRadio.fire();
                    break;
                case "Blank":
                    blankRadio.fire();
                    break;
                case "Random":
                    randomRadio.fire();
                    break;
                case "RandomNonNegative":
                    randomNnRadio.fire();
                    break;
                case "Prefix":
                    prefixRadio.fire();
                    break;
                case "Suffix":
                    suffixRadio.fire();
                    break;
                case "SuffixNumber":
                    numberRadio.fire();
                    break;
                case "Expression":
                    expressionRadio.fire();
                    break;
                case "GaussianDistribution":
                    gaussianDistributionRadio.fire();
                    break;
                case "Identify":
                    identifyRadio.fire();
                    break;
                case "UpperTriangle":
                    upperTriangleRadio.fire();
                    break;
                case "LowerTriangle":
                    lowerTriangleRadio.fire();
                    break;
                default:
                    valueRadio.fire();
            }
            valueInput.setText(UserConfig.getString(baseName + "ValueString", ""));
            prefixInput.setText(UserConfig.getString(baseName + "Prefix", ""));
            suffixInput.setText(UserConfig.getString(baseName + "Suffix", ""));
            startInput.setText(UserConfig.getInt(baseName + "Start", 1) + "");
            if (UserConfig.getBoolean(baseName + "FillZero", true)) {
                int digit = UserConfig.getInt(baseName + "Digit", 0);
                if (digit > 0) {
                    digitInput.setText(digit + "");
                } else {
                    digitInput.setText("");
                }
                fillZeroCheck.setSelected(true);
            } else {
                fillZeroCheck.setSelected(false);
                digitInput.setText("");
            }
            expressionController.scriptInput.setText(UserConfig.getString(baseName + "Expression", ""));

            checkSelection();

            valueGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkSelection();
                }
            });

            expressionController.thisPane.disableProperty().bind(expressionRadio.selectedProperty().not());
            valueInput.disableProperty().bind(valueRadio.selectedProperty().not());
            prefixInput.disableProperty().bind(prefixRadio.selectedProperty().not());
            suffixInput.disableProperty().bind(suffixRadio.selectedProperty().not());
            numberPane.disableProperty().bind(numberRadio.selectedProperty().not());
            digitInput.disableProperty().bind(fillZeroCheck.selectedProperty().not());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameter(BaseData2DHandleController handleController) {
        try {
            this.handleController = handleController;
            expressionController.calculator = handleController.filterController.calculator;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void setData2D(Data2D data2D) {
        this.data2D = data2D;
        expressionController.setData2D(data2D);
    }

    public boolean checkSelection() {
        if (handleController == null) {
            return true;
        }
        boolean ok = true;
        setValue.init();
        if (valueRadio.isSelected()) {
            String v = valueInput.getText();
            setValue.setType(SetValue.ValueType.Value).setValue(v);
            UserConfig.setString(baseName + "ValueString", v);
        } else if (zeroRadio.isSelected()) {
            setValue.setType(SetValue.ValueType.Zero).setValue("0");
        } else if (oneRadio.isSelected()) {
            setValue.setType(SetValue.ValueType.One).setValue("1");
        } else if (blankRadio.isSelected()) {
            setValue.setType(SetValue.ValueType.Blank).setValue("");
        } else if (randomRadio.isSelected()) {
            setValue.setType(SetValue.ValueType.Random);
        } else if (randomNnRadio.isSelected()) {
            setValue.setType(SetValue.ValueType.RandomNonNegative);
        } else if (prefixRadio.isSelected()) {
            String v = prefixInput.getText();
            if (v == null || v.isEmpty()) {
                popError(message("Invalid") + ": " + message("AddPrefix"));
                ok = false;
            } else {
                UserConfig.setString(baseName + "Prefix", v);
            }
            setValue.setType(SetValue.ValueType.Prefix).setValue(v);
        } else if (suffixRadio.isSelected()) {
            String v = suffixInput.getText();
            if (v == null || v.isEmpty()) {
                popError(message("Invalid") + ": " + message("AppendSuffix"));
                ok = false;
            } else {
                UserConfig.setString(baseName + "Suffix", v);
            }
            setValue.setType(SetValue.ValueType.Suffix).setValue(v);
        } else if (numberRadio.isSelected()) {
            setValue.setType(SetValue.ValueType.SuffixNumber).setFillZero(false).setAotoDigit(false);
            int start;
            try {
                start = Integer.valueOf(startInput.getText().trim());
                UserConfig.setInt(baseName + "Start", start);
            } catch (Exception e) {
                popError(message("Invalid") + ": " + message("AddSequenceNumber") + " - " + message("Start"));
                start = 0;
                ok = false;
            }
            int digit = -1;
            if (fillZeroCheck.isSelected()) {
                setValue.setFillZero(true);
                try {
                    String v = digitInput.getText();
                    if (v == null || v.isBlank()) {
                        digit = 0;
                        setValue.setAotoDigit(true);
                    } else {
                        digit = Integer.valueOf(digitInput.getText());
                    }
                    UserConfig.setInt(baseName + "Digit", digit);
                } catch (Exception e) {
                    popError(message("Invalid") + ": " + message("AddSequenceNumber") + " - " + message("Digit"));
                    digit = -2;
                    ok = false;
                }
            }
            setValue.setStart(start).setDigit(digit);
            UserConfig.setBoolean(baseName + "FillZero", setValue.isFillZero());
            UserConfig.setBoolean(baseName + "AutoDigit", setValue.isAotoDigit());
        } else if (gaussianDistributionRadio.isSelected()) {
            setValue.setType(SetValue.ValueType.GaussianDistribution);
        } else if (identifyRadio.isSelected()) {
            setValue.setType(SetValue.ValueType.GaussianDistribution);
        } else if (upperTriangleRadio.isSelected()) {
            setValue.setType(SetValue.ValueType.UpperTriangle);
        } else if (lowerTriangleRadio.isSelected()) {
            setValue.setType(SetValue.ValueType.LowerTriangle);
        } else if (expressionRadio.isSelected()) {
            String v = expressionController.scriptInput.getText();
            setValue.setType(SetValue.ValueType.Expression).setValue(v);
            ok = expressionController.checkExpression(handleController.isAllPages());
            if (!ok && data2D.getError() != null) {
                alertError(message("Invalid") + ": " + message("RowExpression") + "\n" + data2D.getError());
            } else {
                UserConfig.setString(baseName + "Expression", v);
            }
        }
        UserConfig.setString(baseName + "ValueType", setValue.getType().name());
        return ok;
    }

    public String value() {
        return setValue.getValue();
    }

    public void setValue(String value) {
        setValue.setValue(value);
    }

}
