package mara.mybox.controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.table.TableNodeMacro;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-3-20
 * @License Apache License Version 2.0
 */
public class ControlDataMacro extends BaseDataValuesController {

    protected String outputs = "";

    @FXML
    protected TextArea scriptArea;
    @FXML
    protected CheckBox wrapCheck;

    @Override
    public void initEditor() {
        try {
            valueInput = scriptArea;
            valueWrapCheck = wrapCheck;
            valueName = "script";
            super.initEditor();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void load(String script) {
        scriptArea.setText(script);
    }

    @FXML
    public void popMacroHelps(Event event) {
        if (UserConfig.getBoolean("MacroHelpsPopWhenMouseHovering", false)) {
            showMacroHelps(event);
        }
    }

    @FXML
    public void showMacroHelps(Event event) {
        popEventMenu(event, HelpTools.macroHelps());
    }

    @FXML
    protected void popExamplesMenu(MouseEvent event) {
        if (UserConfig.getBoolean(interfaceName + "ExamplesPopWhenMouseHovering", false)) {
            showExamplesMenu(event);
        }
    }

    @FXML
    protected void showExamplesMenu(Event event) {
        PopTools.popMacroExamples(this, event, scriptArea, interfaceName + "Examples");
    }

    /*
        static
     */
    public static DataTreeNodeEditorController open(BaseController parent, String script) {
        try {
            DataTreeNodeEditorController controller
                    = DataTreeNodeEditorController.openTable(parent, new TableNodeMacro());
            ((ControlDataMacro) controller.valuesController).load(script);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
