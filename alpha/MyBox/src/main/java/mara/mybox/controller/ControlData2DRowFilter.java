package mara.mybox.controller;

import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFilter;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-6-1
 * @License Apache License Version 2.0
 */
public class ControlData2DRowFilter extends ControlData2DRowExpression {

    protected long maxFilteredNumber = -1;
    protected DataFilter filter;

    @FXML
    protected RadioButton trueRadio, othersRadio;
    @FXML
    protected TextField maxInput;

    public ControlData2DRowFilter() {
        category = InfoNode.RowFilter;
    }

    @Override
    public void initCalculator() {
        filter = new DataFilter();
        calculator = filter.calculator;
    }

    public void setParameters(BaseController parent) {
        try {
            baseName = parent.baseName;

            maxFilteredNumber = -1;
            if (maxInput != null) {
                maxInput.setStyle(null);
                maxInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        String maxs = maxInput.getText();
                        if (maxs == null || maxs.isBlank()) {
                            maxFilteredNumber = -1;
                            maxInput.setStyle(null);
                        } else {
                            try {
                                maxFilteredNumber = Long.parseLong(maxs);
                                maxInput.setStyle(null);
                            } catch (Exception e) {
                                maxInput.setStyle(UserConfig.badStyle());
                            }
                        }
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void editNode(TreeItem<InfoNode> item) {
        try {
            if (item == null) {
                return;
            }
            InfoNode node = item.getValue();
            if (node == null) {
                return;
            }
            Map<String, String> values = InfoNode.parseInfo(node);
            scriptInput.setText(values.get("Script"));
            if (maxInput != null) {
                maxInput.setText(values.get("Maximum"));
            }
            trueRadio.setSelected(!StringTools.isFalse(values.get("True")));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    public void clear() {
        isSettingValues = true;
        scriptInput.clear();
        if (maxInput != null) {
            maxInput.clear();
        }
        placeholdersList.getItems().clear();
        trueRadio.setSelected(true);
        isSettingValues = false;
        filter.clear();
    }

    public void load(String script, boolean isTrue) {
        load(script, isTrue, -1);
    }

    public void load(String script, boolean isTrue, long max) {
        clear();
        isSettingValues = true;
        if (script == null || script.isBlank()) {
            scriptInput.clear();
            trueRadio.setSelected(true);
        } else {
            scriptInput.setText(script);
            if (isTrue) {
                trueRadio.setSelected(true);
            } else {
                othersRadio.setSelected(true);
            }
        }
        if (maxInput != null) {
            maxInput.setText(max > 0 ? max + "" : "");
        }
        isSettingValues = false;
        filter.setSourceScript(script);
        filter.setReversed(!isTrue);
        filter.setMaxPassed(max);
    }

    public void load(Data2D data2D, DataFilter filter) {
        setData2D(data2D);
        if (filter == null) {
            clear();
            return;
        }
        load(filter.getSourceScript(), !filter.isReversed(), filter.getMaxPassed());
    }

    public DataFilter pickValues() {
        filter.setReversed(othersRadio.isSelected())
                .setMaxPassed(maxFilteredNumber).setPassedNumber(0)
                .setSourceScript(scriptInput.getText());
        if (data2D != null) {
            data2D.setFilter(filter);
        }
        return filter;
    }

    @Override
    public boolean checkExpression(boolean allPages) {
        if (!super.checkExpression(allPages)) {
            return false;
        }
        if (maxInput != null && UserConfig.badStyle().equals(maxInput.getStyle())) {
            error = message("InvalidParameter") + ": " + message("MaxFilteredDataTake");
            return false;
        }
        pickValues();
        return true;
    }

    @FXML
    @Override
    public void editAction() {
        RowFilterController.open(scriptInput.getText(), trueRadio.isSelected(), maxFilteredNumber);
    }

    @FXML
    @Override
    public void dataAction() {
        RowFilterController.open();
    }

}
