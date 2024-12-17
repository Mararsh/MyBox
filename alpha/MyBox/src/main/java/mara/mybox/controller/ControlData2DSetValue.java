package mara.mybox.controller;

import java.sql.Connection;
import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.data.SetValue;
import mara.mybox.data.SetValue.ValueType;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.DerbyBase;
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
    protected ToggleGroup valueGroup, numberGroup;
    @FXML
    protected RadioButton zeroRadio, oneRadio, emptyRadio, nullRadio, randomRadio, randomNnRadio,
            valueRadio, scaleRadio, prefixRadio, suffixRadio, numberRadio, expressionRadio,
            numberSuffixRadio, numberPrefixRadio, numberReplaceRadio,
            numberSuffixStringRadio, numberPrefixStringRadio,
            gaussianDistributionRadio, identifyRadio, upperTriangleRadio, lowerTriangleRadio;
    @FXML
    protected TextField startInput, digitInput;
    @FXML
    protected TextArea valueInput, prefixInput, suffixInput, numberSuffixInput, numberPrefixInput;
    @FXML
    protected ComboBox<String> scaleSelector;
    @FXML
    protected ControlData2DRowExpression expressionController;
    @FXML
    protected CheckBox fillZeroCheck;
    @FXML
    protected VBox setBox, expBox, numberBox;
    @FXML
    protected Label matrixLabel;
    @FXML
    protected FlowPane scalePane;

    public void setParameter(Data2DSetValuesController handleController) {
        try {
            this.handleController = handleController;

            thisPane.getChildren().remove(tabPane);
            setBox.getChildren().clear();

            valueGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    setPane();
                }
            });

            initSetValue();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setPane() {
        try {
            if (isSettingValues) {
                return;
            }
            setBox.getChildren().clear();

            if (valueRadio.isSelected()) {
                setBox.getChildren().add(valueInput);

            } else if (scaleRadio.isSelected()) {
                setBox.getChildren().add(scalePane);

            } else if (prefixRadio.isSelected()) {
                setBox.getChildren().add(prefixInput);

            } else if (suffixRadio.isSelected()) {
                setBox.getChildren().add(suffixInput);

            } else if (numberRadio.isSelected()) {
                setBox.getChildren().add(numberBox);
                if (numberBox.getChildren().contains(numberSuffixInput)) {
                    numberBox.getChildren().remove(numberSuffixInput);
                }
                if (numberBox.getChildren().contains(numberPrefixInput)) {
                    numberBox.getChildren().remove(numberPrefixInput);
                }
                if (numberSuffixStringRadio.isSelected()) {
                    numberBox.getChildren().add(numberSuffixInput);
                }
                if (numberPrefixStringRadio.isSelected()) {
                    numberBox.getChildren().add(numberPrefixInput);
                }

            } else if (gaussianDistributionRadio.isSelected()
                    || identifyRadio.isSelected()
                    || upperTriangleRadio.isSelected()
                    || lowerTriangleRadio.isSelected()) {
                setBox.getChildren().add(matrixLabel);

            } else if (expressionRadio.isSelected()) {
                setBox.getChildren().add(expBox);

            }
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
            isSettingValues = true;
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

            expressionController.scriptInput.setText(UserConfig.getString(conn, baseName + "Expression", ""));

            isSettingValues = false;

            setPane();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setData2D(Data2D data2D) {
        this.data2D = data2D;
        expressionController.setData2D(data2D);
    }

    public void setMatrixPane(boolean isAvailable) {
        if (!isAvailable) {
            if (gaussianDistributionRadio.isSelected() || identifyRadio.isSelected()
                    || upperTriangleRadio.isSelected() || lowerTriangleRadio.isSelected()) {
                valueRadio.setSelected(true);
            }
        }
        matrixLabel.setVisible(isAvailable);
        gaussianDistributionRadio.setVisible(isAvailable);
        identifyRadio.setVisible(isAvailable);
        upperTriangleRadio.setVisible(isAvailable);
        lowerTriangleRadio.setVisible(isAvailable);
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
                if (handleController.sourceController.filteredRowsIndices.size()
                        != handleController.checkedColsIndices.size()) {
                    outError(message("MatricesCannotCalculateShouldSqure"));
                    return false;
                }
                if (handleController.sourceController.filteredRowsIndices.size() % 2 == 0) {
                    outError(message("MatricesCannotCalculateShouldOdd"));
                    return false;
                }
                setValue.setType(ValueType.GaussianDistribution);
            } else if (identifyRadio.isSelected()) {
                if (handleController.sourceController.filteredRowsIndices.size()
                        != handleController.checkedColsIndices.size()) {
                    outError(message("MatricesCannotCalculateShouldSqure"));
                    return false;
                }
                setValue.setType(ValueType.GaussianDistribution);
            } else if (upperTriangleRadio.isSelected()) {
                if (handleController.sourceController.filteredRowsIndices.size()
                        != handleController.checkedColsIndices.size()) {
                    outError(message("MatricesCannotCalculateShouldSqure"));
                    return false;
                }
                setValue.setType(ValueType.UpperTriangle);
            } else if (lowerTriangleRadio.isSelected()) {
                if (handleController.sourceController.filteredRowsIndices.size()
                        != handleController.checkedColsIndices.size()) {
                    outError(message("MatricesCannotCalculateShouldSqure"));
                    return false;
                }
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

            setValue.setInvalidAs(handleController.invalidAs);

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
