package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import mara.mybox.dev.MyBoxLog;

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
    }

    @Override
    public void checkScript() {
        super.checkScript();
        sourceController.data2D.setFilterReversed(!trueRadio.isSelected());
    }

    public boolean filter(int tableRowNumber) {
        try {
            String script = scriptInput.getText();
            if (script == null || script.isBlank()) {
                return true;
            }
            calculate(tableRowNumber);
            boolean v = "true".equals(scriptResult);
            return trueRadio.isSelected() ? v : !v;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

}
