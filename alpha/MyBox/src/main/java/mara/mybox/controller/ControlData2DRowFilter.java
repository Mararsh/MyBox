package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import mara.mybox.calculation.ExpressionCalculator;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFilter;
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
public class ControlData2DRowFilter extends ControlDataRowFilter {

    protected DataFilter filter;
    public ExpressionCalculator calculator;
    protected Data2D data2D;

    @Override
    public void initControls() {
        try {
            baseName = "DataRowFilter";
            super.initControls();

            filter = new DataFilter();
            calculator = filter.calculator;

            wrapCheck.setSelected(UserConfig.getBoolean(baseName + "Wrap", false));
            wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Wrap", newValue);
                    scriptInput.setWrapText(newValue);
                }
            });
            scriptInput.setWrapText(wrapCheck.isSelected());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setData2D(Data2D data2D) {
        this.data2D = data2D;
    }

    public void load(String script, boolean isTrue) {
        load(script, isTrue, -1);
    }

    public void load(String script, boolean isTrue, long max) {
        isSettingValues = true;
        scriptInput.setText(script);
        if (isTrue) {
            trueRadio.setSelected(true);
        } else {
            othersRadio.setSelected(true);
        }
        maxInput.setText(max > 0 && max != InvalidLong ? max + "" : "");
        isSettingValues = false;
        filter.setSourceScript(script);
        filter.setReversed(!isTrue);
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
        load(filter.getSourceScript(), !filter.isReversed(), filter.getMaxPassed());
    }

    public DataFilter pickFilter(boolean allPages) {
        long max = checkMax();
        if (error != null) {
            popError(error);
            return null;
        }
        String script = checkScript(allPages);
        if (error != null) {
            popError(error);
            return null;
        }
        filter.setReversed(othersRadio.isSelected())
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
        if (data2D == null || !data2D.isValidDefinition()) {
            error = message("InvalidData");
            return null;
        }
        String script = scriptInput.getText();
        if (script == null || script.isBlank()) {
            return script;
        }
        script = script.trim();
        if (calculator.validateExpression(data2D, script, allPages)) {
            TableStringValues.add(baseName + "Histories", script);
            return script;
        } else {
            error = calculator.getError();
            return null;
        }
    }

    @FXML
    @Override
    public void clearAction() {
        scriptInput.clear();
    }

    @FXML
    @Override
    public void saveAction() {
        long maxFilteredNumber = checkMax();
        if (error != null) {
            popError(error);
            return;
        }
        ControlDataRowFilter.open(this,
                scriptInput.getText(), trueRadio.isSelected(), maxFilteredNumber);
    }

    @FXML
    @Override
    public void selectAction() {
        DataSelectRowFilterController.open(this);
    }

    @FXML
    public void popPlaceholders(Event event) {
        if (UserConfig.getBoolean(baseName + "PlaceholdersPopWhenMouseHovering", false)) {
            showPlaceholders(event);
        }
    }

    @FXML
    public void showPlaceholders(Event event) {
        PopTools.popDataPlaceHolders(this, event, scriptInput, baseName + "Placeholders", data2D);
    }

}
