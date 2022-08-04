package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFilter;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-6-1
 * @License Apache License Version 2.0
 */
public class ControlData2DFilter extends ControlData2DRowExpression {

    protected long maxData = -1;
    protected DataFilter filter;

    @FXML
    protected RadioButton trueRadio, othersRadio;
    @FXML
    protected TextField maxInput;

    public ControlData2DFilter() {
        TipsLabelKey = "RowFilterTips";

    }

    @Override
    public void initCalculator() {
        filter = new DataFilter();
        calculator = filter.calculator;
    }

    public void setParameters(BaseController parent) {
        try {
            baseName = parent.baseName;

            if (maxInput != null) {
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
            } else {
                maxData = -1;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setData2D(Data2D data2D) {
        super.setData2D(data2D);
        data2D.filter = filter;
    }

    public void load(String script, boolean reversed) {
        if (script == null || script.isBlank()) {
            scriptInput.clear();
            trueRadio.fire();
        } else {
            scriptInput.setText(script);
            if (reversed) {
                othersRadio.fire();
            } else {
                trueRadio.fire();
            }
        }
    }

    public DataFilter pickValues() {
        filter.setReversed(othersRadio.isSelected())
                .setMaxPassed(maxData).setPassedNumber(0)
                .setSourceScript(scriptInput.getText());
        data2D.setFilter(filter);
        return filter;
    }

    @Override
    public boolean checkExpression(boolean allPages) {
        if (!super.checkExpression(allPages)) {
            return false;
        }
        if (maxInput != null && UserConfig.badStyle().equals(maxInput.getStyle())) {
            error = message("InvalidParameter") + ": " + message("MaxSourceDataTake");
            return false;
        }
        pickValues();
        return true;
    }

}
