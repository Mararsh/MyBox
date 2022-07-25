package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import mara.mybox.data2d.Data2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.RowFilter;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-6-1
 * @License Apache License Version 2.0
 */
public class ControlData2DRowFilter extends ControlData2DRowExpression {

    protected long maxData = -1;
    protected RowFilter rowFilter;

    @FXML
    protected RadioButton trueRadio, othersRadio;
    @FXML
    protected TextField maxInput;

    public ControlData2DRowFilter() {
        TipsLabelKey = "RowFilterTips";

    }

    @Override
    public void initCalculator() {
        rowFilter = new RowFilter();
        calculator = rowFilter.calculator;
    }

    public void setParameters(BaseController parent, ControlData2DEditTable tableController) {
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
        this.data2D = data2D;
        rowFilter.data2D = data2D;
        data2D.rowFilter = rowFilter;
    }

    public void load(RowFilter rowFilter) {
        if (rowFilter == null) {
            scriptInput.clear();
            trueRadio.fire();
        } else {
            scriptInput.setText(rowFilter.script);
            if (rowFilter.reversed) {
                othersRadio.fire();
            } else {
                trueRadio.fire();
            }
        }
    }

    public RowFilter pickValues() {
        rowFilter.setReversed(othersRadio.isSelected())
                .setMaxPassed(maxData).setPassedNumber(0)
                .setScript(scriptInput.getText())
                .setData2D(data2D);
        data2D.setRowFilter(rowFilter);
        return rowFilter;
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
