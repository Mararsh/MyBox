package mara.mybox.controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableNodeRowExpression;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-10-15
 * @License Apache License Version 2.0
 */
public class ControlDataRowExpression extends BaseDataValuesController {

    @FXML
    protected TextArea scriptInput;
    @FXML
    protected CheckBox wrapCheck;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Javascript);
    }

    @Override
    public void initControls() {
        try {
            baseName = "DataRowExpression";
            valueInput = scriptInput;
            valueWrapCheck = wrapCheck;
            valueName = "script";

            super.initControls();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void edit(String script) {
        if (!checkBeforeNextAction()) {
            return;
        }
        isSettingValues = true;
        scriptInput.setText(script);
        isSettingValues = false;
        valueChanged(true);
    }

    @FXML
    public void scriptAction() {
        DataSelectJavaScriptController.open(this, scriptInput);
    }

    @FXML
    protected void popExamples(MouseEvent mouseEvent) {
        if (UserConfig.getBoolean(baseName + "ExamplesPopWhenMouseHovering", false)) {
            showExamples(mouseEvent);
        }
    }

    @FXML
    protected void showExamples(Event event) {
        PopTools.popJavaScriptExamples(this, event, scriptInput, baseName + "Examples", null);
    }

    @FXML
    public void popHelps(Event event) {
        if (UserConfig.getBoolean("RowExpressionsHelpsPopWhenMouseHovering", false)) {
            showHelps(event);
        }
    }

    @FXML
    public void showHelps(Event event) {
        popEventMenu(event, HelpTools.rowExpressionHelps());
    }

    /*
        static
     */
    public static DataTreeNodeEditorController open(BaseController parent, String script) {
        try {
            DataTreeNodeEditorController controller
                    = DataTreeNodeEditorController.openTable(parent, new TableNodeRowExpression());
            ((ControlDataRowExpression) controller.valuesController).edit(script);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
