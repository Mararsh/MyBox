package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;

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

    @Override
    public boolean checkExpression() {
        if (super.checkExpression()) {
            String script = scriptInput.getText();
            data2D.setRowFilter(script);
            data2D.setFilterReversed(!trueRadio.isSelected());
            return true;
        } else {
            return false;
        }
    }

}
