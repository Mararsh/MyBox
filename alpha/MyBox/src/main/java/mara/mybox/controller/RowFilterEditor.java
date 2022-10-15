package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.TreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-10-15
 * @License Apache License Version 2.0
 */
public class RowFilterEditor extends TreeNodeEditor {

    protected RowFilterController manageController;
    protected long maxData = -1;

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

            if (maxInput != null) {
                maxData = UserConfig.getLong(baseName + "MaxDataNumber", -1);
                if (maxData > 0) {
                    maxInput.setText(maxData + "");
                }
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
    protected synchronized void editNode(TreeNode node) {
        super.editNode(node);
        isSettingValues = true;
        if (node != null) {
            String script = node.getValue();
            String more = node.getMore();
            if (more != null && more.contains(TreeNode.TagsSeparater)) {
                try {
                    String[] v = more.split(TreeNode.TagsSeparater);
                    load(script, StringTools.isTrue(v[0]), Long.parseLong(v[1]));
                } catch (Exception e) {
                    load(script, true, -1);
                }
            } else {
                load(script, true, -1);
            }
        } else {
            load(null, true, -1);
        }
        isSettingValues = false;
    }

    @Override
    public TreeNode pickNodeData() {
        TreeNode node = super.pickNodeData();
        if (node != null) {
            String more = trueRadio.isSelected() ? "true" : "false";
            more += TreeNode.TagsSeparater;
            more += maxData > 0 ? maxData + "" : "";
            node.setMore(more);
        }
        return node;
    }

    @FXML
    protected void popScriptExamples(MouseEvent mouseEvent) {
        if (UserConfig.getBoolean(interfaceName + "ExamplesPopWhenMouseHovering", false)) {
            scriptExamples(mouseEvent);
        }
    }

    @FXML
    protected void showScriptExamples(ActionEvent event) {
        scriptExamples(event);
    }

    protected void scriptExamples(Event event) {
        try {
            MenuController controller = PopTools.popJavaScriptExamples(this, event, valueInput, interfaceName + "Examples");
            PopTools.rowExpressionButtons(controller, valueInput, message("Column") + "1");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void popScriptHistories(MouseEvent event) {
        if (UserConfig.getBoolean(interfaceName + "HistoriesPopWhenMouseHovering", false)) {
            PopTools.popStringValues(this, valueInput, event, interfaceName + "Histories", false, true);
        }
    }

    @FXML
    protected void showScriptHistories(ActionEvent event) {
        PopTools.popStringValues(this, valueInput, event, interfaceName + "Histories", false, true);
    }

}
