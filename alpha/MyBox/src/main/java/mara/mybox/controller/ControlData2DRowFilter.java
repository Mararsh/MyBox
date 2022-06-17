package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-6-1
 * @License Apache License Version 2.0
 */
public class ControlData2DRowFilter extends ControlData2DRowExpression {

    protected long maxData = -1;

    @FXML
    protected RadioButton trueRadio;
    @FXML
    protected TextField maxInput;

    public ControlData2DRowFilter() {
        TipsLabelKey = "RowFilterTips";
        hisName = "RowFilterHistories";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            maxData = UserConfig.getLong(baseName + "MaxDataNumber", -1);
            if (maxData > 0) {
                maxInput.setText(maxData + "");
            }
            maxInput.setStyle(null);
            maxInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    String maxs = maxInput.getText();
                    if (maxs == null || maxs.isBlank()) {
                        maxData = -1;
                        maxInput.setStyle(null);
                        UserConfig.setLong(baseName + "MaxDataNumber", -1);
                    } else {
                        try {
                            maxData = Long.parseLong(maxs);
                            maxInput.setStyle(null);
                            UserConfig.setLong(baseName + "MaxDataNumber", maxData);
                        } catch (Exception e) {
                            maxInput.setStyle(UserConfig.badStyle());
                        }
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkExpression(boolean allPages) {
        if (!super.checkExpression(allPages)) {
            return false;
        }
        if (UserConfig.badStyle().equals(maxInput.getStyle())) {
            error = message("InvalidParameter") + ": " + message("MaxDataTake");
            return false;
        }
        data2D.expressionCalculator.setMaxFilterPassed(maxData)
                .setFilterScript(scriptInput.getText())
                .setFilterReversed(!trueRadio.isSelected());
        return true;
    }

}
