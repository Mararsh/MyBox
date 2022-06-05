package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import mara.mybox.db.table.TableStringValues;

/**
 * @Author Mara
 * @CreateDate 2022-6-1
 * @License Apache License Version 2.0
 */
public class ControlData2DRowFilter extends ControlData2DRowExpression {

    @FXML
    protected RadioButton trueRadio;

    public ControlData2DRowFilter() {
        TipsLabelKey = "RowFilterTips";
        hisName = "RowFilterHistories";
    }

    public void checkScript() {
        String script = scriptInput.getText();
        if (script != null && !script.isBlank()) {
            TableStringValues.add(hisName, script.trim());
        }
        sourceController.data2D.setRowFilter(script);
        sourceController.data2D.setFilterReversed(!trueRadio.isSelected());
    }

}
