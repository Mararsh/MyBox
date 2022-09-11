package mara.mybox.controller;

import java.util.Arrays;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-9-8
 * @License Apache License Version 2.0
 */
public class FunctionBinaryEditor extends TreeNodeEditor {

    protected FunctionUnaryController functionController;
    protected String outputs = "";

    public FunctionBinaryEditor() {
        defaultExt = "txt";
    }

    protected void setParameters(FunctionUnaryController functionController) {
        this.functionController = functionController;
    }

    @Override
    protected void showEditorPane() {
    }

    @FXML
    protected void popScriptExamples(MouseEvent event) {
        if (UserConfig.getBoolean(interfaceName + "ScriptExamplesPopWhenMouseHovering", true)) {
            scriptExamples(event);
        }
    }

    @FXML
    protected void showScriptExamples(ActionEvent event) {
        scriptExamples(event);
    }

    protected void scriptExamples(Event event) {
        try {
            MenuController controller = PopTools.popJavaScriptExamples(this, event, valueInput, interfaceName + "ScriptExamples");

            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    "Math.PI", "Math.E", "Math.random()", "Math.abs(x)",
                    "Math.pow(x,2)", "Math.pow(x,3)", "Math.sqrt(x)", "Math.pow(x,1d/3)",
                    "Math.pow(3, x)", "Math.exp(x)",
                    "Math.log(x)", "Math.sin(x)", "Math.cos(x)", "Math.tan(x)"
            ), true, 4);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void popScriptHistories(MouseEvent mouseEvent) {
        if (UserConfig.getBoolean(interfaceName + "ScriptHistoriesPopWhenMouseHovering", true)) {
            PopTools.popStringValues(this, valueInput, mouseEvent, interfaceName + "ScriptHistories", false, true);
        }
    }

    @FXML
    protected void showScriptHistories(ActionEvent event) {
        PopTools.popStringValues(this, valueInput, event, interfaceName + "ScriptHistories", false, true);
    }

    @FXML
    public void clearDomain() {
        moreInput.clear();
    }

    @FXML
    protected void popDomainExamples(MouseEvent event) {
        if (UserConfig.getBoolean(interfaceName + "DomainExamplesPopWhenMouseHovering", true)) {
            domainExamples(event);
        }
    }

    @FXML
    protected void showDomainExamples(ActionEvent event) {
        domainExamples(event);
    }

    protected void domainExamples(Event event) {
        try {
            MenuController controller = PopTools.popJavaScriptExamples(this, event, moreInput, interfaceName + "DomainExamples");

            PopTools.addButtonsPane(controller, moreInput, Arrays.asList(
                    "x > 0", "x >= 0", "x < 0", "x <= 0", "x != 0", "x != 1",
                    "x >= -1 && x <= 1", "( x - Math.PI / 2 ) % Math.PI != 0"
            ), true, 4);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void popDomainHistories(MouseEvent mouseEvent) {
        if (UserConfig.getBoolean(interfaceName + "DomainHistoriesPopWhenMouseHovering", true)) {
            PopTools.popStringValues(this, moreInput, mouseEvent, interfaceName + "DomainHistories", false, true);
        }
    }

    @FXML
    protected void showDomainHistories(ActionEvent event) {
        PopTools.popStringValues(this, moreInput, event, interfaceName + "DomainHistories", false, true);
    }

}
