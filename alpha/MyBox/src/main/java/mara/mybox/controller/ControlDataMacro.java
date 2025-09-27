package mara.mybox.controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.table.TableNodeMacro;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.BaseMacro;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-3-20
 * @License Apache License Version 2.0
 */
public class ControlDataMacro extends BaseDataValuesController {

    protected BaseMacro macro;

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

    @Override
    public boolean checkOptions() {
        try {
            macro = BaseMacro.create(scriptArea.getText(), true);
            if (macro == null) {
                popError(message("InvalidParameters") + ": Script");
                return false;
            }
            showLogs("\n\nMacro: " + macro.getScript()
                    + "\nParsed: " + macro.getParameters());
            if (!macro.readParameters()) {
                showLogs(macro.getError());
                return false;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        try {
            TableStringValues.add(baseName + "Histories", macro.getScript());
            macro.setController(this).setTask(currentTask).run();
            error = macro.getError();
            return macro.isOk();
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    @Override
    public void afterSuccess() {
        macro.displayResult();
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
