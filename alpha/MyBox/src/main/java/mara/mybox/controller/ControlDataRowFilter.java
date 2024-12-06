package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableNodeRowFilter;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-10-15
 * @License Apache License Version 2.0
 */
public class ControlDataRowFilter extends BaseDataValuesController {

    @FXML
    protected TextArea scriptInput;
    @FXML
    protected ToggleGroup takeGroup;
    @FXML
    protected RadioButton trueRadio, othersRadio;
    @FXML
    protected TextField maxInput;
    @FXML
    protected CheckBox wrapCheck;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Javascript);
    }

    @Override
    public void initEditor() {
        try {
            valueInput = scriptInput;
            valueWrapCheck = wrapCheck;
            baseName = "DataRowFilter";

            super.initEditor();

            takeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    valueChanged(true);
                }
            });

            maxInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    valueChanged(true);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void editValues() {
        try {
            isSettingValues = true;
            if (nodeEditor.currentNode != null) {
                scriptInput.setText(nodeEditor.currentNode.getStringValue("script"));
                trueRadio.setSelected(nodeEditor.currentNode.getBooleanValue("match_true"));
                maxInput.setText(nodeEditor.currentNode.getIntValue("max_match") + "");
            } else {
                scriptInput.clear();
                trueRadio.setSelected(true);
                maxInput.clear();
            }
            isSettingValues = false;
            valueChanged(false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected DataNode pickValues(DataNode node) {
        try {
            String maxs = maxInput.getText();
            long maxData;
            if (maxs == null || maxs.isBlank()) {
                maxData = -1;
            } else {
                try {
                    maxData = Long.parseLong(maxs);
                } catch (Exception e) {
                    return null;
                }
            }
            node.setValue("max_match", maxData);
            String script = scriptInput.getText();
            node.setValue("script", script == null ? null : script.trim());
            node.setValue("match_true", trueRadio.isSelected());
            return node;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public void edit(String script, boolean matchTrue, long max) {
        if (!checkBeforeNextAction()) {
            return;
        }
        isSettingValues = true;
        scriptInput.setText(script);
        trueRadio.setSelected(matchTrue);
        maxInput.setText(max > 0 ? max + "" : "");
        isSettingValues = false;
        valueChanged(true);
    }

    @FXML
    public void scriptAction() {
        DataSelectJavaScriptController.open(this, scriptInput);
    }

    @FXML
    protected void popScriptExamples(MouseEvent mouseEvent) {
        if (UserConfig.getBoolean(baseName + "ExamplesPopWhenMouseHovering", false)) {
            showScriptExamples(mouseEvent);
        }
    }

    @FXML
    protected void showScriptExamples(Event event) {
        PopTools.popJavaScriptExamples(this, event, scriptInput, baseName + "Examples", null);
    }

    @FXML
    protected void popScriptHistories(Event event) {
        if (UserConfig.getBoolean(baseName + "HistoriesPopWhenMouseHovering", false)) {
            showScriptHistories(event);
        }
    }

    @FXML
    protected void showScriptHistories(Event event) {
        PopTools.popStringValues(this, scriptInput, event, baseName + "Histories", false);
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

    /*
        static
     */
    public static DataTreeNodeEditorController open(BaseController parent,
            String script, boolean matchTrue, long max) {
        try {
            DataTreeNodeEditorController controller = DataTreeNodeEditorController.open(parent);
            controller.setTable(new TableNodeRowFilter());
            ((ControlDataRowFilter) controller.dataController).edit(script, matchTrue, max);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
