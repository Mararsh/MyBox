package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import mara.mybox.calculation.ExpressionCalculator;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-6-4
 * @License Apache License Version 2.0
 */
public class ControlData2DRowExpression extends ControlDataRowExpression {

    public ExpressionCalculator calculator;
    protected Data2D data2D;

    @Override
    public void initControls() {
        try {
            super.initControls();

            calculator = new ExpressionCalculator();

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

    public boolean checkExpression(boolean allPages) {
        error = null;
        if (data2D == null || !data2D.isValidDefinition()) {
            error = message("InvalidData");
            return false;
        }
        String script = scriptInput.getText();
        if (script == null || script.isBlank()) {
            return true;
        }
        TableStringValues.add(baseName + "Histories", script.trim());
        if (calculator.validateExpression(data2D, script, allPages)) {
            return true;
        } else {
            error = calculator.getError();
            return false;
        }
    }

    @FXML
    @Override
    public void saveAction() {
        ControlDataRowExpression.open(this, scriptInput.getText());
    }

    @FXML
    @Override
    public void selectAction() {
        DataSelectRowExpressionController.open(this);
    }

    @FXML
    @Override
    protected void showExamples(Event event) {
        PopTools.popRowExpressionExamples(this, event, scriptInput, baseName + "Examples", data2D);
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
