package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.JShellTools;

/**
 * @Author Mara
 * @CreateDate 2022-5-17
 * @License Apache License Version 2.0
 */
public class JexlEditor extends JShellEditor {

    @FXML
    protected TextField parametersInput;

    public JexlEditor() {
        defaultExt = "txt";
    }

    @FXML
    @Override
    public synchronized void resetJShell() {
        reset();
        resetTask = new SingletonTask<Void>(this) {

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
                jShellController.pathsController.pathsArea.setText(paths);
            }

        };
        start(resetTask, true);
    }

    @Override
    protected void showEditorPane() {
    }

    @Override
    protected boolean handleCodes(String script) {
        try {
            if (script == null || script.isBlank()) {
                return false;
            }
            TableStringValues.add("JexlScriptHistories", script.trim());
            String jexlContext = "jexlContext.clear();";
            runSnippet(jexlContext, jexlContext);
            String contexts = moreInput.getText();
            if (contexts != null && !contexts.isBlank()) {
                jexlContext = contexts.trim();
                TableStringValues.add("JexlContextHistories", jexlContext);
                runCodes(jexlContext);
            }

            String jexlScriptOrignal = "jexlScript = jexlEngine.createScript(\"" + script + "\");\n";
            String jexlScript = "jexlScript = jexlEngine.createScript(\"" + script.replaceAll("\n", " ") + "\");\n";
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

    @FXML
    protected void popScriptExamples(MouseEvent mouseEvent) {
        PopTools.popJEXLScriptExamples(this, valueInput, moreInput, mouseEvent);
    }

    @FXML
    protected void popScriptHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, valueInput, mouseEvent, "JexlScriptHistories");
    }

    @FXML
    public void addContext() {
        moreInput.appendText("jexlContext.set(\"name\", value);\n");
    }

    @FXML
    public void clearContext() {
        moreInput.clear();
    }

    @FXML
    protected void popContextExamples(MouseEvent mouseEvent) {
        PopTools.popJEXLContextExamples(this, moreInput, mouseEvent);
    }

    @FXML
    protected void popContextHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, moreInput, mouseEvent, "JexlContextHistories");
    }

    @FXML
    public void clearParameters() {
        parametersInput.clear();
    }

    @FXML
    protected void popParametersHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, parametersInput, mouseEvent, "JexlParamtersHistories");
    }

}
