package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.JShellTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-5-17
 * @License Apache License Version 2.0
 */
public class ControlDataJEXL extends ControlDataJShell {

    @FXML
    protected TextArea contextInput;
    @FXML
    protected CheckBox wrapContentCheck;
    @FXML
    protected TextField parametersInput;

    public ControlDataJEXL() {
        TipsLabelKey = "JEXLTips";
    }

    @Override
    public void initEditor() {
        try {
            super.initEditor();
            valueName = "script";

            wrapContentCheck.setSelected(UserConfig.getBoolean(baseName + "ContentWrap", false));
            wrapContentCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "ContentWrap", newValue);
                    contextInput.setWrapText(newValue);
                }
            });
            contextInput.setWrapText(wrapContentCheck.isSelected());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void editValues() {
        try {
            isSettingValues = true;
            if (nodeEditor.currentNode != null) {
                codesInput.setText(nodeEditor.currentNode.getStringValue("script"));
                contextInput.setText(nodeEditor.currentNode.getStringValue("context"));
                parametersInput.setText(nodeEditor.currentNode.getStringValue("parameters"));
            } else {
                codesInput.clear();
                contextInput.clear();
                parametersInput.clear();
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
            String script = codesInput.getText();
            if (script != null && !script.isBlank()) {
                script = script.trim();
                TableStringValues.add(baseName + "Histories", script);
                node.setValue("script", script);
            } else {
                node.setValue("script", null);
            }

            String context = contextInput.getText();
            if (context != null && !context.isBlank()) {
                context = context.trim();
                TableStringValues.add("JexlContextHistories", context);
                node.setValue("context", context);
            } else {
                node.setValue("context", null);
            }

            String parameters = parametersInput.getText();
            if (parameters != null && !parameters.isBlank()) {
                parameters = parameters.trim();
                TableStringValues.add("JexlParamtersHistories", parameters);
                node.setValue("parameters", parameters);
            } else {
                node.setValue("parameters", null);
            }
            return node;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    @Override
    public synchronized void resetJShell() {
        reset();
        resetTask = new FxTask<Void>(this) {

            private String paths;

            @Override
            protected boolean handle() {
                try {
                    jShell = JShellTools.initJEXL();
                    paths = JShellTools.classPath(jShell) + System.getProperty("java.class.path");
                    paths = paths.replace(";", ";\n");
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                pathsController.pathsArea.setText(paths);
            }

        };
        start(resetTask, true);
    }

    @Override
    protected boolean handleCodes(String script) {
        try {
            if (script == null || script.isBlank()) {
                return false;
            }
            TableStringValues.add(baseName + "Histories", script.trim());
            String jexlContext = "jexlContext.clear();";
            runSnippet(jexlContext, jexlContext);
            String contexts = contextInput.getText();
            if (contexts != null && !contexts.isBlank()) {
                jexlContext = contexts.trim();
                TableStringValues.add("JexlContextHistories", jexlContext);
                runCodes(jexlContext);
            }

            String jexlScriptOrignal = "jexlScript = jexlEngine.createScript(\"" + script + "\");\n";
            String jexlScript = "jexlScript = jexlEngine.createScript(\""
                    + StringTools.replaceLineBreak(script) + "\");\n";
            runSnippet(jexlScriptOrignal, jexlScript);

            String parameters = parametersInput.getText();
            String execute;
            if (parameters != null && !parameters.isBlank()) {
                parameters = parameters.trim();
                TableStringValues.add("JexlParamtersHistories", parameters);
                execute = "jexlScript.execute(jexlContext, " + parameters + ");";
            } else {
                execute = "jexlScript.execute(jexlContext);";
            }
            runSnippet(execute);

            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    public void edit(String script, String context, String parameters) {
        codesInput.setText(script);
        contextInput.setText(context);
        parametersInput.setText(parameters);
    }

    @FXML
    protected void popScriptExamples(MouseEvent event) {
        if (UserConfig.getBoolean(interfaceName + "ScriptExamplesPopWhenMouseHovering", false)) {
            jexlScriptExamples(event);
        }
    }

    @FXML
    protected void showScriptExamples(ActionEvent event) {
        jexlScriptExamples(event);
    }

    protected void jexlScriptExamples(Event event) {
        try {
            MenuController controller = PopTools.valuesMenu(nodeEditor, codesInput,
                    interfaceName + "ScriptExamples", message("Syntax"), event);

            PopTools.addButtonsPane(controller, Arrays.asList(
                    " new('java.math.BigDecimal', 9) ", " new('java.lang.Double', 10d) ",
                    " new('java.lang.Long', 10) ", " new('java.lang.Integer', 10) ",
                    " new('java.lang.String', 'Hello') ", "  new('java.util.Date') "
            ));
            PopTools.addButtonsPane(controller, Arrays.asList(
                    " true ", " false ", " null ", " empty(x) ", " size(x) ",
                    " 3 =~ [1,'2',3, 'hello'] ", " 2 !~ {1,'2',3, 'hello'} ",
                    " 'hello'.startsWith('hell') ", " 'hello'.endsWith('ll') ",
                    " not 'hello'.startsWith('hell') "
            ));
            PopTools.addButtonsPane(controller, Arrays.asList(
                    " = ", " + ", " - ", " * ", " / ", ";", " , ",
                    "( )", " { } ", "[ ]", "\"\"", "''", " : ", " .. "
            ));
            PopTools.addButtonsPane(controller, Arrays.asList(
                    " == ", " != ", " >= ", " > ", " <= ", " < ",
                    " && ", " and ", " || ", " or ", " !", " not ",
                    " =~ ", " !~ "
            ));
            PopTools.addButtonsPane(controller, Arrays.asList(
                    "var list = [ 'A', 'B', 'C', 'D' ];\n"
                    + "return list.size();",
                    "var set = { 'A', 'B', 'C', 'D' };\n"
                    + "return set.toString();",
                    "var map = { 'A': 1,'B': 2,'C': 3,'D': 4 };\n"
                    + "return map.toString();"
            ));

            List<Node> buttons = new ArrayList<>();
            Button includeButton = new Button("StringTools.include('abc1233hello','3{2}',caseInsensitive)");
            includeButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    codesInput.setText(includeButton.getText());
                    controller.getThisPane().requestFocus();
                    codesInput.requestFocus();

                    if (contextInput.getText() == null
                            || !contextInput.getText().contains("jexlContext.set(\"StringTools\", mara.mybox.tools.StringTools.class);")) {
                        contextInput.appendText("jexlContext.set(\"StringTools\", mara.mybox.tools.StringTools.class);\n");
                    }
                    if (contextInput.getText() == null
                            || !contextInput.getText().contains("jexlContext.set(\"caseInsensitive\",")) {
                        contextInput.appendText("jexlContext.set(\"caseInsensitive\", true);\n");
                    }
                }
            });
            buttons.add(includeButton);

            Button matchButton = new Button("StringTools.match('abc1233hello','\\\\S*3{2,}\\\\S*',caseInsensitive);");
            matchButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    codesInput.setText(matchButton.getText());
                    controller.getThisPane().requestFocus();
                    codesInput.requestFocus();

                    if (contextInput.getText() == null
                            || !contextInput.getText().contains("jexlContext.set(\"StringTools\", mara.mybox.tools.StringTools.class);")) {
                        contextInput.appendText("jexlContext.set(\"StringTools\", mara.mybox.tools.StringTools.class);\n");
                    }
                    if (contextInput.getText() == null
                            || !contextInput.getText().contains("jexlContext.set(\"caseInsensitive\",")) {
                        contextInput.appendText("jexlContext.set(\"caseInsensitive\", true);\n");
                    }
                }
            });
            buttons.add(matchButton);

            Button scaleButton = new Button("DoubleTools.scale(52362.18903, 2)");
            scaleButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    codesInput.setText(scaleButton.getText());
                    controller.getThisPane().requestFocus();
                    codesInput.requestFocus();

                    if (contextInput.getText() == null
                            || !contextInput.getText().contains("jexlContext.set(\"DoubleTools\", mara.mybox.tools.DoubleTools.class);")) {
                        contextInput.appendText("jexlContext.set(\"DoubleTools\", mara.mybox.tools.DoubleTools.class);\n");
                    }
                }
            });
            buttons.add(scaleButton);

            Button formatButton = new Button("NumberTools.format(52362.18903, 2)");
            formatButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    codesInput.setText(formatButton.getText());
                    controller.getThisPane().requestFocus();
                    codesInput.requestFocus();

                    if (contextInput.getText() == null
                            || !contextInput.getText().contains("jexlContext.set(\"DoubleTools\", mara.mybox.tools.DoubleTools.class);")) {
                        contextInput.appendText("jexlContext.set(\"DoubleTools\", mara.mybox.tools.DoubleTools.class);\n");
                    }
                }
            });
            buttons.add(formatButton);

            Button percentageButton = new Button("DoubleTools.percentage(647, 2916, 2)");
            percentageButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    codesInput.setText(percentageButton.getText());
                    controller.getThisPane().requestFocus();
                    codesInput.requestFocus();

                    if (contextInput.getText() == null
                            || !contextInput.getText().contains("jexlContext.set(\"DoubleTools\", mara.mybox.tools.DoubleTools.class);")) {
                        contextInput.appendText("jexlContext.set(\"DoubleTools\", mara.mybox.tools.DoubleTools.class);\n");
                    }
                }
            });
            buttons.add(percentageButton);

            controller.addFlowPane(buttons);

            buttons = new ArrayList<>();
            Button mathButton = new Button("Math.E + Math.exp(x)");
            mathButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    codesInput.setText(mathButton.getText());
                    controller.getThisPane().requestFocus();
                    codesInput.requestFocus();

                    if (contextInput.getText() == null
                            || !contextInput.getText().contains("jexlContext.set(\"Math\", Math.class);")) {
                        contextInput.appendText("jexlContext.set(\"Math\", Math.class);\n");
                    }
                    if (contextInput.getText() == null
                            || !contextInput.getText().contains("jexlContext.set(\"x\",")) {
                        contextInput.appendText("jexlContext.set(\"x\", 9);\n");
                    }
                }
            });
            buttons.add(mathButton);

            Button funButton = new Button("var circleArea = function(r) \n"
                    + "{ Math.PI * r * r };\n"
                    + "return circleArea (2.6);");
            funButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    codesInput.setText(funButton.getText());
                    controller.getThisPane().requestFocus();
                    codesInput.requestFocus();

                    if (contextInput.getText() == null
                            || !contextInput.getText().contains("jexlContext.set(\"Math\", Math.class);")) {
                        contextInput.appendText("jexlContext.set(\"Math\", Math.class);\n");
                    }
                }
            });
            buttons.add(funButton);

            Button whileButton = new Button("var s = 'hello ';\n"
                    + "while (s.length() < len) {\n"
                    + "   s += 'a';\n"
                    + "}\n"
                    + "return s;\n");
            whileButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    codesInput.setText(whileButton.getText());
                    controller.getThisPane().requestFocus();
                    codesInput.requestFocus();

                    if (contextInput.getText() == null
                            || !contextInput.getText().contains("jexlContext.set(\"len\",")) {
                        contextInput.appendText("jexlContext.set(\"len\", 8);\n");
                    }
                }
            });
            buttons.add(whileButton);

            controller.addFlowPane(buttons);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void addContext() {
        contextInput.appendText("jexlContext.set(\"name\", value);\n");
    }

    @FXML
    public void clearContext() {
        contextInput.clear();
    }

    @FXML
    protected void popContextExamples(MouseEvent event) {
        if (UserConfig.getBoolean(interfaceName + "ContextExamplesPopWhenMouseHovering", false)) {
            jexlContextExamples(event);
        }
    }

    @FXML
    protected void showContextExamples(ActionEvent event) {
        jexlContextExamples(event);
    }

    protected void jexlContextExamples(Event event) {
        try {
            MenuController controller = PopTools.valuesMenu(nodeEditor, contextInput,
                    interfaceName + "ContextExamples", message("Syntax"), event);

            PopTools.addButtonsPane(controller, Arrays.asList(
                    "jexlContext.set(\"Math\", Math.class);\n",
                    "jexlContext.set(\"BigDecimal\", new java.math.BigDecimal(10));\n",
                    "jexlContext.set(\"df\", \"#,###\");\n"
                    + "jexlContext.set(\"DecimalFormat\", new java.text.DecimalFormat(df));\n",
                    "jexlContext.set(\"StringTools\", mara.mybox.tools.StringTools.class);\n",
                    "jexlContext.set(\"DoubleTools\", mara.mybox.tools.DoubleTools.class);\n",
                    "jexlContext.set(\"DateTools\", mara.mybox.tools.DateTools.class);\n",
                    "jexlContext.set(\"x\", 5);\n",
                    "jexlContext.set(\"x\", 5);\n",
                    "jexlContext.set(\"s\", \"hello\");\n"
            ));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    protected void popContextHistories(Event event) {
        if (UserConfig.getBoolean("JexlContextHistoriesPopWhenMouseHovering", false)) {
            showContextHistories(event);
        }
    }

    @FXML
    protected void showContextHistories(Event event) {
        PopTools.popSavedValues(this, contextInput, event, "JexlContextHistories");
    }

    @FXML
    public void clearParameters() {
        parametersInput.clear();
    }

    @FXML
    protected void popParametersHistories(Event event) {
        if (UserConfig.getBoolean("JexlParamtersHistoriesPopWhenMouseHovering", false)) {
            showParametersHistories(event);
        }
    }

    @FXML
    protected void showParametersHistories(Event event) {
        PopTools.popSavedValues(this, parametersInput, event, "JexlParamtersHistories");
    }

    @FXML
    public void popJexlHelps(Event event) {
        if (UserConfig.getBoolean("JexlHelpsPopWhenMouseHovering", false)) {
            showJexlHelps(event);
        }
    }

    @FXML
    public void showJexlHelps(Event event) {
        popEventMenu(event, HelpTools.jexlHelps());
    }

}
