package mara.mybox.controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-3-20
 * @License Apache License Version 2.0
 */
public class BaseJavaScriptController extends BaseDataValuesController {

    protected ControlWebView htmlWebView;
    protected String script, results;

    @FXML
    protected TextArea scriptInput;
    @FXML
    protected CheckBox wrapCheck;

    public BaseJavaScriptController() {
        TipsLabelKey = "JavaScriptTips";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Javascript);
    }

    @Override
    public void initEditor() {
        try {
            valueInput = scriptInput;
            valueWrapCheck = wrapCheck;
            valueName = "script";
            super.initEditor();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(ControlWebView sourceWebView) {
        htmlWebView = sourceWebView;
        initEditor();
    }

    @Override
    public boolean checkOptions() {
        if (htmlWebView == null) {
            popError(message("InvalidParameters") + ": Source WebView ");
            return false;
        }
        script = scriptInput.getText();
        if (script == null || script.isBlank()) {
            popError(message("InvalidParameters") + ": JavaScript");
            return false;
        }
        script = script.trim();
        results = null;
        return true;
    }

    @Override
    public void startTask() {
        try {
            Object o = htmlWebView.executeScript(script);
            if (o != null) {
                results = o.toString();
            } else {
                results = "";
            }
            TableStringValues.add(baseName + "Histories", script);
            error = null;
            taskSuccessed = true;
        } catch (Exception e) {
            error = e.toString();
            taskSuccessed = false;
        }
        closeTask(taskSuccessed);
    }

    @FXML
    public void selectAction(Event event) {
        DataSelectJavaScriptController.open(this, scriptInput);
    }

    @FXML
    public void saveAction(Event event) {
        ControlDataJavascript.loadScript(this, scriptInput.getText());
    }

    @FXML
    public void popJavascriptHelps(Event event) {
        if (UserConfig.getBoolean("JavaScriptHelpsPopWhenMouseHovering", false)) {
            showJavascriptHelps(event);
        }
    }

    @FXML
    public void showJavascriptHelps(Event event) {
        popEventMenu(event, HelpTools.javascriptHelps());
    }

    @FXML
    protected void popExamplesMenu(MouseEvent event) {
        if (UserConfig.getBoolean(interfaceName + "ExamplesPopWhenMouseHovering", false)) {
            showExamplesMenu(event);
        }
    }

    @FXML
    protected void showExamplesMenu(Event event) {
        PopTools.popJavaScriptExamples(this, event, scriptInput, interfaceName + "Examples", null);
    }

}
