package mara.mybox.controller;

import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.InfoNode;
import static mara.mybox.db.data.InfoNode.ValueSeparater;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-10-15
 * @License Apache License Version 2.0
 */
public class RowFilterEditor extends InfoTreeNodeEditor {

    protected RowFilterController manageController;
    protected long maxData = -1;

    @FXML
    protected ToggleGroup takeGroup;
    @FXML
    protected RadioButton trueRadio, othersRadio;
    @FXML
    protected TextField maxInput;

    public RowFilterEditor() {
        defaultExt = "js";
    }

    protected void setParameters(RowFilterController manageController) {
        try {
            this.manageController = manageController;
            baseName = manageController.baseName;

            if (takeGroup != null) {
                takeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        valueChanged(true);
                    }
                });

            }

            if (maxInput != null) {
                maxData = -1;
                maxInput.setStyle(null);
                maxInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        String maxs = maxInput.getText();
                        if (maxs == null || maxs.isBlank()) {
                            maxData = -1;
                            maxInput.setStyle(null);
                        } else {
                            try {
                                maxData = Long.parseLong(maxs);
                                maxInput.setStyle(null);
                                valueChanged(true);
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
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void showEditorPane() {
    }

    public void clear() {
        isSettingValues = true;
        valueInput.clear();
        maxInput.clear();
        trueRadio.setSelected(true);
        isSettingValues = false;
    }

    public void load(String script, boolean isTrue, long max) {
        clear();
        isSettingValues = true;
        if (script == null || script.isBlank()) {
            valueInput.clear();
            trueRadio.setSelected(true);
        } else {
            valueInput.setText(script);
            if (isTrue) {
                trueRadio.setSelected(true);
            } else {
                othersRadio.setSelected(true);
            }
        }
        maxInput.setText(max > 0 ? max + "" : "");
        isSettingValues = false;
    }

    @Override
    protected void editValue(InfoNode node) {
        Map<String, String> values = InfoNode.parseInfo(node);
        if (values != null) {
            long max = -1;
            try {
                max = Long.parseLong(values.get("Maximum"));
            } catch (Exception e) {
            }
            load(values.get("Script"), !StringTools.isFalse(values.get("Condition")), max);
        } else {
            load(null, true, -1);
        }
    }

    @Override
    protected InfoNode pickValue(InfoNode node) {
        if (node == null) {
            return null;
        }
        String script = valueInput.getText();
        String info;
        if (trueRadio.isSelected() && maxData <= 0) {
            info = script;
        } else {
            info = (script == null ? "" : script.trim()) + ValueSeparater + "\n"
                    + (trueRadio.isSelected() ? "true" : "false") + ValueSeparater + "\n"
                    + (maxData > 0 ? maxData + "" : "");
        }
        return node.setInfo(info);
    }

    @FXML
    protected void popScriptExamples(MouseEvent mouseEvent) {
        if (UserConfig.getBoolean(interfaceName + "ExamplesPopWhenMouseHovering", false)) {
            showScriptExamples(mouseEvent);
        }
    }

    @FXML
    protected void showScriptExamples(Event event) {
        PopTools.popJavaScriptExamples(this, event, valueInput, interfaceName + "Examples", null);
    }

    @FXML
    protected void popScriptHistories(Event event) {
        if (UserConfig.getBoolean(interfaceName + "HistoriesPopWhenMouseHovering", false)) {
            showScriptHistories(event);
        }
    }

    @FXML
    protected void showScriptHistories(Event event) {
        PopTools.popStringValues(this, valueInput, event, interfaceName + "Histories", false);
    }

    @FXML
    public void popRowExpressionHelps(Event event) {
        if (UserConfig.getBoolean("RowExpressionsHelpsPopWhenMouseHovering", false)) {
            showRowExpressionHelps(event);
        }
    }

    @FXML
    public void showRowExpressionHelps(Event event) {
        popEventMenu(event, HelpTools.rowExpressionHelps());
    }

}
