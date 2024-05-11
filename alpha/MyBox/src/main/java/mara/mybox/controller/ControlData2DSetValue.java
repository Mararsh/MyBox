package mara.mybox.controller;

import java.sql.Connection;
import java.util.Arrays;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.data.SetValue;
import mara.mybox.data.SetValue.ValueType;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
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
    protected ToggleGroup valueGroup, nonnumericGroup, numberGroup;
    @FXML
    protected RadioButton zeroRadio, oneRadio, emptyRadio, nullRadio, randomRadio, randomNnRadio,
            valueRadio, scaleRadio, prefixRadio, suffixRadio, numberRadio, expressionRadio,
            skipNonnumericRadio, zeroNonnumericRadio, emptyNonnumericRadio,
            keepNonnumericRadio, nullNonnumericRadio,
            numberSuffixRadio, numberPrefixRadio, numberReplaceRadio,
            numberSuffixStringRadio, numberPrefixStringRadio,
            gaussianDistributionRadio, identifyRadio, upperTriangleRadio, lowerTriangleRadio;
    @FXML
    protected TextField valueInput, prefixInput, suffixInput, startInput, digitInput,
            numberSuffixInput, numberPrefixInput;
    @FXML
    protected ComboBox<String> scaleSelector;
    @FXML
    protected FlowPane scalePane, numberPane, matrixPane;
    @FXML
    protected ControlData2DRowExpression expressionController;
    @FXML
    protected CheckBox fillZeroCheck;
    @FXML
    protected VBox expBox;

    public void setParameter(Data2DSetValuesController handleController) {
        try {
            this.handleController = handleController;
            initSetValue();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initSetValue() {
        try (Connection conn = DerbyBase.getConnection()) {
            setValue = new SetValue();
            String valueType = UserConfig.getString(conn, baseName + "ValueType", "Value");
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
                case "Empty":
                    emptyRadio.setSelected(true);
                    break;
                case "Null":
                    nullRadio.setSelected(true);
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
                case "NumberSuffix":
                    numberRadio.setSelected(true);
                    numberSuffixRadio.setSelected(true);
                    break;
                case "NumberPrefix":
                    numberRadio.setSelected(true);
                    numberPrefixRadio.setSelected(true);
                    break;
                case "NumberReplace":
                    numberRadio.setSelected(true);
                    numberReplaceRadio.setSelected(true);
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

            valueInput.setText(UserConfig.getString(conn, baseName + "ValueString", ""));
            prefixInput.setText(UserConfig.getString(conn, baseName + "Prefix", ""));
            suffixInput.setText(UserConfig.getString(conn, baseName + "Suffix", ""));
            startInput.setText(UserConfig.getInt(conn, baseName + "Start", 1) + "");
            numberSuffixInput.setText(UserConfig.getString(conn, baseName + "NumberSuffix", ""));
            numberPrefixInput.setText(UserConfig.getString(conn, baseName + "NumberPrefix", ""));
            if (UserConfig.getBoolean(conn, baseName + "FillZero", true)) {
                int digit = UserConfig.getInt(conn, baseName + "Digit", 0);
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
            int scale = UserConfig.getInt(conn, baseName + "Scale", 5);
            if (scale < 0) {
                scale = 5;
            }
            scaleSelector.getItems().addAll(
                    Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
            );
            scaleSelector.setValue(scale + "");

            String nonnumeric = UserConfig.getString(conn, baseName + "Nonnumeric", "Keep");
            if ("Skip".equals(nonnumeric)) {
                skipNonnumericRadio.setSelected(true);
            } else if ("Zero".equals(nonnumeric)) {
                zeroNonnumericRadio.setSelected(true);
            } else if ("empty".equals(nonnumeric)) {
                emptyNonnumericRadio.setSelected(true);
            } else if ("null".equals(nonnumeric)) {
                nullNonnumericRadio.setSelected(true);
            } else {
                keepNonnumericRadio.setSelected(true);
            }
            expressionController.scriptInput.setText(UserConfig.getString(conn, baseName + "Expression", ""));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setData2D(Data2D data2D) {
        this.data2D = data2D;
        expressionController.setData2D(data2D);
    }

    public void setMatrixPane(boolean isAvailable) {
        if (isAvailable) {
            matrixPane.setDisable(false);

        } else {
            matrixPane.setDisable(true);
            if (gaussianDistributionRadio.isSelected() || identifyRadio.isSelected()
                    || upperTriangleRadio.isSelected() || lowerTriangleRadio.isSelected()) {
                valueRadio.setSelected(true);
            }
        }
    }

    public boolean pickValues() {
        if (handleController == null) {
            return false;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            setValue.init();
            if (valueRadio.isSelected()) {
                String v = valueInput.getText();
                setValue.setType(ValueType.Value).setValue(v);
                UserConfig.setString(conn, baseName + "ValueString", v);
            } else if (zeroRadio.isSelected()) {
                setValue.setType(ValueType.Zero).setValue("0");
            } else if (oneRadio.isSelected()) {
                setValue.setType(ValueType.One).setValue("1");
            } else if (emptyRadio.isSelected()) {
                setValue.setType(ValueType.Empty).setValue("");
            } else if (nullRadio.isSelected()) {
                setValue.setType(ValueType.Null).setValue(null);
            } else if (randomRadio.isSelected()) {
                setValue.setType(ValueType.Random);
            } else if (randomNnRadio.isSelected()) {
                setValue.setType(ValueType.RandomNonNegative);
            } else if (scaleRadio.isSelected()) {
                setValue.setType(ValueType.Scale);
                int v = -1;
                try {
                    v = Integer.parseInt(scaleSelector.getValue());
                } catch (Exception e) {
                }
                if (v >= 0 && v <= 15) {
                    UserConfig.setInt(conn, baseName + "Scale", v);
                } else {
                    outError(message("Invalid") + ": " + message("DecimalScale"));
                    return false;
                }
                setValue.setScale(v);
                if (skipNonnumericRadio.isSelected()) {
                    setValue.setInvalidAs(InvalidAs.Skip);
                } else if (zeroNonnumericRadio.isSelected()) {
                    setValue.setInvalidAs(InvalidAs.Zero);
                } else if (emptyNonnumericRadio.isSelected()) {
                    setValue.setInvalidAs(InvalidAs.Empty);
                } else if (nullNonnumericRadio.isSelected()) {
                    setValue.setInvalidAs(InvalidAs.Null);
                } else {
                    setValue.setInvalidAs(InvalidAs.Keep);
                }
                UserConfig.setString(conn, baseName + "Nonnumeric", setValue.getInvalidAs().name());
            } else if (prefixRadio.isSelected()) {
                String v = prefixInput.getText();
                setValue.setType(ValueType.Prefix).setValue(v);
                if (v == null || v.isEmpty()) {
                    outError(message("Invalid") + ": " + message("AddPrefix"));
                    return false;
                } else {
                    UserConfig.setString(conn, baseName + "Prefix", v);
                }
            } else if (suffixRadio.isSelected()) {
                String v = suffixInput.getText();
                setValue.setType(ValueType.Suffix).setValue(v);
                if (v == null || v.isEmpty()) {
                    outError(message("Invalid") + ": " + message("AppendSuffix"));
                    return false;
                } else {
                    UserConfig.setString(conn, baseName + "Suffix", v);
                }
            } else if (numberRadio.isSelected()) {
                int start;
                try {
                    start = Integer.parseInt(startInput.getText().trim());
                    UserConfig.setInt(conn, baseName + "Start", start);
                } catch (Exception e) {
                    outError(message("Invalid") + ": " + message("SequenceNumber") + " - " + message("From"));
                    return false;
                }
                if (numberPrefixRadio.isSelected()) {
                    setValue.setType(ValueType.NumberPrefix);
                } else if (numberReplaceRadio.isSelected()) {
                    setValue.setType(ValueType.NumberReplace);
                } else if (numberSuffixRadio.isSelected()) {
                    setValue.setType(ValueType.NumberSuffix);
                } else if (numberSuffixStringRadio.isSelected()) {
                    String s = numberSuffixInput.getText();
                    setValue.setType(ValueType.NumberSuffixString).setValue(s);
                    UserConfig.setString(conn, baseName + "NumberSuffix", s);
                } else if (numberPrefixStringRadio.isSelected()) {
                    String s = numberPrefixInput.getText();
                    setValue.setType(ValueType.NumberPrefixString).setValue(s);
                    UserConfig.setString(conn, baseName + "NumberPrefix", s);
                } else {
                    outError(message("Invalid") + ": " + message("SequenceNumber"));
                    return false;
                }
                setValue.setFillZero(false).setAotoDigit(false);
                int digit = -1;
                if (fillZeroCheck.isSelected()) {
                    setValue.setFillZero(true);
                    try {
                        String v = digitInput.getText();
                        if (v == null || v.isBlank()) {
                            digit = 0;
                            setValue.setAotoDigit(true);
                        } else {
                            digit = Integer.parseInt(digitInput.getText());
                        }
                        UserConfig.setInt(conn, baseName + "Digit", digit);
                    } catch (Exception e) {
                        outError(message("Invalid") + ": " + message("SequenceNumber") + " - " + message("Digit"));
                        return false;
                    }
                }
                setValue.setStart(start).setDigit(digit);
                UserConfig.setBoolean(conn, baseName + "FillZero", setValue.isFillZero());
                UserConfig.setBoolean(conn, baseName + "AutoDigit", setValue.isAotoDigit());
            } else if (gaussianDistributionRadio.isSelected()) {
                setValue.setType(ValueType.GaussianDistribution);
            } else if (identifyRadio.isSelected()) {
                setValue.setType(ValueType.GaussianDistribution);
            } else if (upperTriangleRadio.isSelected()) {
                setValue.setType(ValueType.UpperTriangle);
            } else if (lowerTriangleRadio.isSelected()) {
                setValue.setType(ValueType.LowerTriangle);
            } else if (expressionRadio.isSelected()) {
                if (!expressionController.checkExpression(handleController.isAllPages())) {
                    handleController.tabPane.getSelectionModel().select(handleController.valuesTab);
                    alertError(message("Invalid") + ": " + message("RowExpression") + "\n" + expressionController.error);
                    return false;
                }
                String v = expressionController.scriptInput.getText();
                setValue.setType(ValueType.Expression).setValue(v);
                UserConfig.setString(conn, baseName + "Expression", v);
            }
            UserConfig.setString(conn, baseName + "ValueType", setValue.getType().name());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void outError(String error) {
        if (error != null && !error.isBlank()) {
            handleController.popError(error);
            handleController.tabPane.getSelectionModel().select(handleController.valuesTab);
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
