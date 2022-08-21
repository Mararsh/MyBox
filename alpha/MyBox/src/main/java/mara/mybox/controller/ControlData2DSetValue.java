package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import mara.mybox.data.SetValue;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public class ControlData2DSetValue extends BaseController {

    protected Data2DSetValuesController handleController;
    protected Data2D data2D;
    protected SetValue setValue;

    @FXML
    protected ToggleGroup valueGroup, nonnumericGroup;
    @FXML
    protected RadioButton zeroRadio, oneRadio, blankRadio, randomRadio, randomNnRadio,
            valueRadio, scaleRadio, prefixRadio, suffixRadio, numberRadio, expressionRadio,
            skipNonnumericRadio, zeroNonnumericRadio, blankNonnumericRadio,
            gaussianDistributionRadio, identifyRadio, upperTriangleRadio, lowerTriangleRadio;
    @FXML
    protected TextField valueInput, prefixInput, suffixInput, startInput, digitInput;
    @FXML
    protected ComboBox<String> scaleSelector;
    @FXML
    protected FlowPane scalePane, numberPane, matrixPane;
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
                    zeroRadio.setSelected(true);
                    break;
                case "One":
                    oneRadio.setSelected(true);
                    break;
                case "Blank":
                    blankRadio.setSelected(true);
                    break;
                case "Random":
                    randomRadio.setSelected(true);
                    break;
                case "RandomNonNegative":
                    randomNnRadio.setSelected(true);
                    break;
                case "Scale":
                    scaleRadio.setSelected(true);
                    break;
                case "Prefix":
                    prefixRadio.setSelected(true);
                    break;
                case "Suffix":
                    suffixRadio.setSelected(true);
                    break;
                case "SuffixNumber":
                    numberRadio.setSelected(true);
                    break;
                case "Expression":
                    expressionRadio.setSelected(true);
                    break;
                case "GaussianDistribution":
                    gaussianDistributionRadio.setSelected(true);
                    break;
                case "Identify":
                    identifyRadio.setSelected(true);
                    break;
                case "UpperTriangle":
                    upperTriangleRadio.setSelected(true);
                    break;
                case "LowerTriangle":
                    lowerTriangleRadio.setSelected(true);
                    break;
                default:
                    valueRadio.setSelected(true);
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
            int scale = UserConfig.getInt(baseName + "Scale", 5);
            if (scale < 0) {
                scale = 5;
            }
            scaleSelector.getItems().addAll(
                    Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
            );
            scaleSelector.setValue(scale + "");
            scaleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkSelection();
                }
            });
            String nonnumeric = UserConfig.getString(baseName + "Nonnumeric", "Skip");
            if ("Zero".equals(nonnumeric)) {
                zeroNonnumericRadio.setSelected(true);
            } else if ("Blank".equals(nonnumeric)) {
                blankNonnumericRadio.setSelected(true);
            } else {
                skipNonnumericRadio.setSelected(true);
            }
            nonnumericGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkSelection();
                }
            });
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
            scalePane.disableProperty().bind(scaleRadio.selectedProperty().not());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameter(Data2DSetValuesController handleController) {
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
        try {
            if (handleController == null) {
                return true;
            }
            setValue.init();
            outError(null);
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
            } else if (scaleRadio.isSelected()) {
                setValue.setType(SetValue.ValueType.Scale);
                int v = -1;
                try {
                    v = Integer.parseInt(scaleSelector.getValue());
                } catch (Exception e) {
                }
                if (v >= 0 && v <= 15) {
                    UserConfig.setInt(baseName + "Scale", v);
                    scaleSelector.getEditor().setStyle(null);
                } else {
                    scaleSelector.getEditor().setStyle(UserConfig.badStyle());
                    outError(message("Invalid") + ": " + message("DecimalScale"));
                    return false;
                }
                setValue.setScale(v);
                if (zeroNonnumericRadio.isSelected()) {
                    setValue.setInvalidAs(InvalidAs.Zero);
                } else if (blankNonnumericRadio.isSelected()) {
                    setValue.setInvalidAs(InvalidAs.Blank);
                } else {
                    setValue.setInvalidAs(InvalidAs.Skip);
                }
                UserConfig.setString(baseName + "Nonnumeric", setValue.getInvalidAs().name());
            } else if (prefixRadio.isSelected()) {
                String v = prefixInput.getText();
                setValue.setType(SetValue.ValueType.Prefix).setValue(v);
                if (v == null || v.isEmpty()) {
                    outError(message("Invalid") + ": " + message("AddPrefix"));
                    return false;
                } else {
                    UserConfig.setString(baseName + "Prefix", v);
                }
            } else if (suffixRadio.isSelected()) {
                String v = suffixInput.getText();
                setValue.setType(SetValue.ValueType.Suffix).setValue(v);
                if (v == null || v.isEmpty()) {
                    outError(message("Invalid") + ": " + message("AppendSuffix"));
                    return false;
                } else {
                    UserConfig.setString(baseName + "Suffix", v);
                }
            } else if (numberRadio.isSelected()) {
                setValue.setType(SetValue.ValueType.SuffixNumber).setFillZero(false).setAotoDigit(false);
                int start;
                try {
                    start = Integer.valueOf(startInput.getText().trim());
                    UserConfig.setInt(baseName + "Start", start);
                } catch (Exception e) {
                    outError(message("Invalid") + ": " + message("AddSequenceNumber") + " - " + message("Start"));
                    return false;
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
                        outError(message("Invalid") + ": " + message("AddSequenceNumber") + " - " + message("Digit"));
                        return false;
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
                if (!expressionController.checkExpression(handleController.isAllPages())) {
                    handleController.tabPane.getSelectionModel().select(handleController.optionsTab);
                    alertError(message("Invalid") + ": " + message("RowExpression") + "\n" + expressionController.error);
                    return false;
                } else {
                    UserConfig.setString(baseName + "Expression", v);
                }
            }
            UserConfig.setString(baseName + "ValueType", setValue.getType().name());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public void outError(String error) {
        if (error != null && !error.isBlank()) {
            handleController.outOptionsError(error);
            handleController.tabPane.getSelectionModel().select(handleController.optionsTab);
        }
    }

    public String value() {
        return setValue.getValue();
    }

    public void setValue(String value) {
        setValue.setValue(value);
    }

    public String scale(String value) {
        return setValue.scale(value);
    }

}
