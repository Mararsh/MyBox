package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFilter;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import static mara.mybox.value.AppValues.InvalidLong;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-6-1
 * @License Apache License Version 2.0
 */
public class ControlData2DRowFilter extends ControlData2DRowExpression {

    protected DataFilter filter;

    @FXML
    protected ToggleGroup takeGroup;
    @FXML
    protected RadioButton trueRadio, falseRadio;
    @FXML
    protected TextField maxInput;

    @Override
    public void initControls() {
        try {
            super.initControls();

            filter = new DataFilter();
            calculator = filter.calculator;

            takeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    valueChanged(true);
                }
            });

            maxInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    valueChanged(true);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void load(String script, boolean matchTrue) {
        load(script, matchTrue, -1);
    }

    public void load(String script, boolean matchTrue, long max) {
        isSettingValues = true;
        scriptInput.setText(script);
        if (matchTrue) {
            trueRadio.setSelected(true);
        } else {
            falseRadio.setSelected(true);
        }
        maxInput.setText(max > 0 && max != InvalidLong ? max + "" : "");
        isSettingValues = false;
        filter.setSourceScript(script);
        filter.setMatchFalse(!matchTrue);
        filter.setMaxPassed(max > 0 && max != InvalidLong ? max : -1);
    }

    public void load(Data2D data2D, DataFilter filter) {
        this.data2D = data2D;
        if (filter == null) {
            scriptInput.setText(null);
            trueRadio.setSelected(true);
            maxInput.setText(null);
            return;
        }
        load(filter.getSourceScript(), !filter.isMatchFalse(), filter.getMaxPassed());
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
        filter.setMatchFalse(falseRadio.isSelected())
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
        String script = scriptInput.getText();
        if (script == null || script.isBlank()) {
            return script;
        }
        script = script.trim();
        calculator.validateExpression(data2D, script, allPages);
        TableStringValues.add(baseName + "Histories", script);
        error = calculator.getError();
        return script;
    }

    @FXML
    @Override
    protected void showExamples(Event event) {
        PopTools.popRowExpressionExamples(this, event, scriptInput, baseName + "Examples", data2D);
    }

}
