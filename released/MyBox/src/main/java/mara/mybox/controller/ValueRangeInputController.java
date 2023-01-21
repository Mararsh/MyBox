package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.ValueRange;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-11-15
 * @License Apache License Version 2.0
 */
public class ValueRangeInputController extends BaseInputController {

    protected Data2DColumn column;
    protected ValueRange valueRange;

    @FXML
    protected TextField startInput, endInput;
    @FXML
    protected CheckBox includeStartCheck, includeEndCheck;
    @FXML
    protected Button examplesStartButton, examplesEndButton;

    public void setParameters(BaseController parent, Data2DColumn column, ValueRange range) {
        try {
            if (column == null) {
                close();
                return;
            }
            setParameters(parent, null);
            setCommentsLabel(message("Column") + ": " + column.getColumnName());
            this.column = column;
            this.valueRange = range;
            setRange(range);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());

        }
    }

    public void setRange(ValueRange range) {
        valueRange = range;
        if (valueRange == null) {
            valueRange = new ValueRange();
        }
        if (valueRange.getStart() != null) {
            startInput.setText(valueRange.getStart() + "");
        } else {
            startInput.clear();
        }
        if (valueRange.getEnd() != null) {
            endInput.setText(valueRange.getEnd() + "");
        } else {
            endInput.clear();
        }
        examplesStartButton.setVisible(column.isDateType());
        examplesEndButton.setVisible(column.isDateType());
        includeStartCheck.setSelected(valueRange.isIncludeStart());
        includeEndCheck.setSelected(valueRange.isIncludeEnd());
    }

    public ValueRange getRange() {
        if (valueRange == null) {
            valueRange = new ValueRange();
        }
        Object v;
        try {
            if (column.isDateType()) {
                v = DateTools.datetimeToString(DateTools.encodeDate(startInput.getText()).getTime());
            } else {
                v = Double.parseDouble(startInput.getText());
            }
        } catch (Exception e) {
            popError(message("InvalidData") + ": " + message("Start"));
            return null;
        }
        valueRange.setStart(v);
        try {
            if (column.isDateType()) {
                v = DateTools.datetimeToString(DateTools.encodeDate(endInput.getText()).getTime());
            } else {
                v = Double.parseDouble(endInput.getText());
            }
        } catch (Exception e) {
            popError(message("InvalidData") + ": " + message("End"));
            return null;
        }
        valueRange.setEnd(v);
        valueRange.setIncludeStart(includeStartCheck.isSelected());
        valueRange.setIncludeEnd(includeEndCheck.isSelected());
        return valueRange;
    }

    @FXML
    public void popStartExamples(MouseEvent mouseEvent) {
        popMenu = PopTools.popDatetimeExamples(this, popMenu, startInput, mouseEvent);
    }

    @FXML
    public void popEndExamples(MouseEvent mouseEvent) {
        popMenu = PopTools.popDatetimeExamples(this, popMenu, endInput, mouseEvent);
    }


    /*
        static
     */
    public static ValueRangeInputController open(BaseController parent, Data2DColumn column, ValueRange range) {
        try {
            ValueRangeInputController controller = (ValueRangeInputController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ValueRangeInputFxml, false);
            controller.setParameters(parent, column, range);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
