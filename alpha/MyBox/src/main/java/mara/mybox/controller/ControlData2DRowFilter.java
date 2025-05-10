package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFilter;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import static mara.mybox.value.AppValues.InvalidLong;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-6-1
 * @License Apache License Version 2.0
 */
public class ControlData2DRowFilter extends ControlData2DRowExpression {

    protected DataFilter filter;
    protected Data2DColumn objectColumn;

    @FXML
    protected ToggleGroup objectGroup, operatorGroup, compareGroup;
    @FXML
    protected FlowPane columnsPane, operatorPane, comparePane;
    @FXML
    protected VBox setBox, columnBox, expressionBox;
    @FXML
    protected HBox valueBox;
    @FXML
    protected RadioButton notFilterRadio, expressionRadio,
            containRadio, containInsensitiveRadio, equalRadio,
            greaterRadio, greaterEqualRadio, lessRadio, lessEqualRadio,
            prefixRadio, suffixRadio, numberRadio, zeroRadio,
            nullRadio, emptyRadio, nullEmptyRadio, trueRadio, falseRadio,
            numberTypeRadio, stringTypeRadio;
    @FXML
    protected TextField maxInput, operandInput;
    @FXML
    protected CheckBox excludedCheck;

    @FXML

    @Override
    public void initControls() {
        try {
            super.initControls();

            filter = new DataFilter();
            calculator = filter.calculator;

            objectGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    objectChanged();
                    valueChanged(true);
                }
            });

            operatorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    operatorChanged();
                    valueChanged(true);
                }
            });

            compareGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    valueChanged(true);
                }
            });

            operandInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    valueChanged(true);
                }
            });

            maxInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    valueChanged(true);
                }
            });

            updateData(null);
            objectChanged();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void updateData(Data2D data) {
        try {
            data2D = data;
            columnsPane.getChildren().clear();
            if (data2D != null) {
                for (int i = 0; i < data2D.colsNumber; i++) {
                    String name = data2D.columnName(i);
                    RadioButton button = new RadioButton(name);
                    button.setToggleGroup(objectGroup);
                    button.setUserData(i);
                    columnsPane.getChildren().add(button);
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void objectChanged() {
        try {
            if (isSettingValues) {
                return;
            }
            setBox.getChildren().clear();
            objectColumn = null;
            if (data2D == null
                    || objectGroup.getSelectedToggle() == null
                    || notFilterRadio.isSelected()) {
                return;
            }

            if (expressionRadio.isSelected()) {
                setBox.getChildren().addAll(expressionBox, excludedCheck);
                refreshStyle(setBox);
                return;
            }

            objectColumn = data2D.column((int) objectGroup.getSelectedToggle().getUserData());
            setBox.getChildren().addAll(columnBox, excludedCheck);
            columnChanged();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void columnChanged() {
        try {
            if (isSettingValues) {
                return;
            }
            columnBox.getChildren().clear();
            if (objectColumn == null) {
                return;
            }
            columnBox.getChildren().add(operatorPane);
            operatorPane.getChildren().clear();
            isSettingValues = true;
            if (objectColumn.isBooleanType()) {
                operatorPane.getChildren().addAll(trueRadio, falseRadio);
                trueRadio.setSelected(true);

            } else if (objectColumn.isDBNumberType()) {
                operatorPane.getChildren().addAll(
                        equalRadio,
                        greaterRadio, greaterEqualRadio, lessRadio, lessEqualRadio,
                        zeroRadio, numberRadio,
                        containRadio, containInsensitiveRadio,
                        prefixRadio, suffixRadio);
                equalRadio.setSelected(true);

            } else {
                operatorPane.getChildren().addAll(
                        containRadio, containInsensitiveRadio,
                        prefixRadio, suffixRadio,
                        equalRadio, numberRadio, zeroRadio,
                        greaterRadio, greaterEqualRadio, lessRadio, lessEqualRadio);
                containRadio.setSelected(true);

            }
            isSettingValues = false;
            operatorPane.getChildren().addAll(nullRadio, emptyRadio, nullEmptyRadio);
            operatorChanged();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void operatorChanged() {
        try {
            if (isSettingValues || objectColumn == null) {
                return;
            }
            if (equalRadio.isSelected()
                    || greaterRadio.isSelected()
                    || greaterEqualRadio.isSelected()
                    || lessRadio.isSelected()
                    || lessEqualRadio.isSelected()) {
                if (!columnBox.getChildren().contains(valueBox)) {
                    columnBox.getChildren().add(valueBox);
                }
                if (!setBox.getChildren().contains(comparePane)) {
                    setBox.getChildren().add(1, comparePane);
                }
                if (objectColumn.isDBNumberType()) {
                    numberTypeRadio.setSelected(true);
                } else {
                    stringTypeRadio.setSelected(true);
                }
            } else if (containRadio.isSelected()
                    || containInsensitiveRadio.isSelected()
                    || containRadio.isSelected()
                    || prefixRadio.isSelected()
                    || suffixRadio.isSelected()) {
                if (!columnBox.getChildren().contains(valueBox)) {
                    columnBox.getChildren().add(valueBox);
                }
                if (setBox.getChildren().contains(comparePane)) {
                    setBox.getChildren().remove(comparePane);
                }
            } else {
                if (columnBox.getChildren().contains(valueBox)) {
                    columnBox.getChildren().remove(valueBox);
                }
                if (setBox.getChildren().contains(comparePane)) {
                    setBox.getChildren().remove(comparePane);
                }
            }
            refreshStyle(columnBox);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void load(String script, boolean matchTrue) {
        load(script, matchTrue, -1);
    }

    public void load(String script, boolean matchTrue, long max) {
        try {
            if (script != null) {
                expressionRadio.setSelected(true);
            }
            isSettingValues = true;
            scriptInput.setText(script);
            excludedCheck.setSelected(!matchTrue);
            maxInput.setText(max > 0 && max != InvalidLong ? max + "" : "");
            isSettingValues = false;
            filter.setSourceScript(script);
            filter.setMatchFalse(!matchTrue);
            filter.setMaxPassed(max > 0 && max != InvalidLong ? max : -1);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void load(Data2D data2D, DataFilter filter) {
        updateData(data2D);
        if (filter == null) {
            isSettingValues = true;
            scriptInput.setText(null);
            excludedCheck.setSelected(false);
            maxInput.setText(null);
            isSettingValues = false;
            return;
        }
        load(filter.getSourceScript(), !filter.isMatchFalse(), filter.getMaxPassed());
    }

    public String makeScript() {
        try {
            if (notFilterRadio.isSelected()) {
                return null;
            }

            if (expressionRadio.isSelected()) {
                return scriptInput.getText();
            }

            if (objectColumn == null) {
                error = message("InvalidParameter") + ": " + message("Object");
                return null;
            }

            String script = null;
            String value = operandInput.getText();
            String valueError = message("InvalidParameter") + ": " + message("Value");
            String name = objectColumn.getColumnName();
            String placeholder = "#{" + name + "}";
            String placeholderString = "( #{" + name + "} + '' )";
            boolean isStringType = objectColumn.isDBStringType();
            boolean isNumberType = objectColumn.isDBNumberType();
            boolean asNumberType = numberTypeRadio.isSelected();

            if (trueRadio.isSelected()) {
                script = placeholder + " == true";

            } else if (falseRadio.isSelected()) {
                script = placeholder + " == false";

            } else if (equalRadio.isSelected()) {
                if (value == null || value.isEmpty()) {
                    error = valueError;
                    return null;
                }
                if (isNumberType) {
                    if (asNumberType) {
                        script = placeholder + " == " + value;
                    } else {
                        script = placeholderString + " == '" + value + "'";
                    }
                } else {
                    if (asNumberType) {
                        script = "parseFloat(" + placeholder + ") " + " == " + value;
                    } else {
                        script = placeholder + " == '" + value + "'";
                    }
                }

            } else if (numberRadio.isSelected()) {
                script = "!isNaN(" + placeholder + " - 1)";

            } else if (zeroRadio.isSelected()) {
                if (isNumberType) {
                    script = placeholder + " == 0";
                } else {
                    script = "parseFloat(" + placeholder + ") == 0";
                }

            } else if (nullRadio.isSelected()) {
                script = placeholder + " == null";

            } else if (emptyRadio.isSelected()) {
                if (isStringType) {
                    script = "(" + placeholder + " != null) && ("
                            + placeholder + " == '')";
                } else {
                    script = "(" + placeholder + " != null) && ("
                            + placeholderString + " == '')";
                }

            } else if (nullEmptyRadio.isSelected()) {
                if (isStringType) {
                    script = "(" + placeholder + " == null) || ("
                            + placeholder + " == '')";
                } else {
                    script = "(" + placeholder + " == null) || ("
                            + placeholderString + " == '')";
                }

            } else if (prefixRadio.isSelected()) {
                if (value == null || value.isEmpty()) {
                    error = valueError;
                    return null;
                }
                if (isStringType) {
                    script = "(" + placeholder + " != null) && ("
                            + placeholder + ".startsWith('" + value + "'))";
                } else {
                    script = "(" + placeholder + " != null) && ("
                            + placeholderString + ".startsWith('" + value + "'))";
                }

            } else if (suffixRadio.isSelected()) {
                if (value == null || value.isEmpty()) {
                    error = valueError;
                    return null;
                }
                if (isStringType) {
                    script = "(" + placeholder + " != null) && ("
                            + placeholder + ".endsWith('" + value + "'))";
                } else {
                    script = "(" + placeholder + " != null) && ("
                            + placeholderString + ".endsWith('" + value + "'))";
                }

            } else if (containRadio.isSelected()) {
                if (value == null || value.isEmpty()) {
                    error = valueError;
                    return null;
                }
                if (isStringType) {
                    script = "(" + placeholder + " != null) && ("
                            + placeholder + ".search(/" + value + "/) >= 0)";
                } else {
                    script = "(" + placeholder + " != null) && ("
                            + placeholderString + ".search(/" + value + "/) >= 0)";
                }

            } else if (containInsensitiveRadio.isSelected()) {
                if (value == null || value.isEmpty()) {
                    error = valueError;
                    return null;
                }
                if (isStringType) {
                    script = "(" + placeholder + " != null) && ("
                            + placeholder + ".search(/" + value + "/ig) >= 0)";
                } else {
                    script = "(" + placeholder + " != null) && ("
                            + placeholderString + ".search(/" + value + "/ig) >= 0)";
                }

            } else if (greaterRadio.isSelected()) {
                if (value == null || value.isEmpty()) {
                    error = valueError;
                    return null;
                }
                if (isNumberType) {
                    if (asNumberType) {
                        script = placeholder + " > " + value;
                    } else {
                        script = placeholderString + " > '" + value + "'";
                    }
                } else {
                    if (asNumberType) {
                        script = "parseFloat(" + placeholderString + ") " + " > " + value;
                    } else {
                        script = placeholder + " > '" + value + "'";
                    }
                }

            } else if (greaterEqualRadio.isSelected()) {
                if (value == null || value.isEmpty()) {
                    error = valueError;
                    return null;
                }
                if (isNumberType) {
                    if (asNumberType) {
                        script = placeholder + " >= " + value;
                    } else {
                        script = placeholderString + " >= '" + value + "'";
                    }
                } else {
                    if (asNumberType) {
                        script = "parseFloat(" + placeholderString + ") " + " >= " + value;
                    } else {
                        script = placeholder + " >= '" + value + "'";
                    }
                }

            } else if (lessRadio.isSelected()) {
                if (value == null || value.isEmpty()) {
                    error = valueError;
                    return null;
                }
                if (isNumberType) {
                    if (asNumberType) {
                        script = placeholder + " < " + value;
                    } else {
                        script = placeholderString + " < '" + value + "'";
                    }
                } else {
                    if (asNumberType) {
                        script = "parseFloat(" + placeholderString + ") " + " < " + value;
                    } else {
                        script = placeholder + " < '" + value + "'";
                    }
                }

            } else if (lessEqualRadio.isSelected()) {
                if (value == null || value.isEmpty()) {
                    error = valueError;
                    return null;
                }
                if (isNumberType) {
                    if (asNumberType) {
                        script = placeholder + " <= " + value;
                    } else {
                        script = placeholderString + " <= '" + value + "'";
                    }
                } else {
                    if (asNumberType) {
                        script = "parseFloat(" + placeholderString + ") " + " <= " + value;
                    } else {
                        script = placeholder + " <= '" + value + "'";
                    }
                }

            }

            return script;
        } catch (Exception e) {
            MyBoxLog.error(e);
            error = e.toString();
            return null;
        }
    }

    public DataFilter pickFilter(boolean allPages) {
        long max = checkMax();
        if (error != null) {
            popError(error);
            return null;
        }
        String script = checkScript(allPages);
        if (error != null) {
            if (!PopTools.askSure(getTitle(),
                    message("RowExpressionLooksInvalid") + ": \n" + error,
                    message("SureContinue"))) {
                return null;
            }
        }
        filter.setMatchFalse(excludedCheck.isSelected())
                .setMaxPassed(max > 0 && max != InvalidLong ? max : -1)
                .setPassedNumber(0)
                .setSourceScript(script);
        if (data2D != null) {
            data2D.setFilter(filter);
        }
        return filter;
    }

    public long checkMax() {
        error = null;
        String maxs = maxInput.getText();
        long maxFilteredNumber = -1;
        if (maxs != null && !maxs.isBlank()) {
            try {
                maxFilteredNumber = Long.parseLong(maxs);
            } catch (Exception e) {
                error = message("InvalidParameter") + ": " + message("MaxFilteredDataTake");
                return InvalidLong;
            }
        }
        return maxFilteredNumber;
    }

    public String checkScript(boolean allPages) {
        error = null;
        if (data2D == null || !data2D.hasColumns()) {
            error = message("InvalidData");
            return null;
        }
        String script = makeScript();
        if (error != null || script == null || script.isBlank()) {
            return null;
        }
        script = script.trim();
        calculator.validateExpression(data2D, script, allPages);
        TableStringValues.add(baseName + "Histories", script);
        error = calculator.getError();
        return script;
    }

    public boolean excluded() {
        return excludedCheck.isSelected();
    }

    @FXML
    @Override
    protected void showExamples(Event event) {
        PopTools.popRowExpressionExamples(this, event, valueInput, baseName + "Examples", data2D);
    }

    @FXML
    public void popConditionHistories(Event event) {
        if (UserConfig.getBoolean(baseName + "ConditionHistoriesPopWhenMouseHovering", false)) {
            showConditionHistories(event);
        }
    }

    @FXML
    public void showConditionHistories(Event event) {
        PopTools.popSavedValues(this, operandInput, event, baseName + "ConditionHistories");
    }

}
