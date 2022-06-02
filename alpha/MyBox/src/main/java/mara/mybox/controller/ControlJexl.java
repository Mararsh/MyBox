package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import jdk.jshell.JShell;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.db.table.TableTreeNode;
import mara.mybox.db.table.TableTreeNodeTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.JShellTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-5-31
 * @License Apache License Version 2.0
 */
public class ControlJexl extends TreeNodesController {

    protected ControlData2DLoad data2dControler;
    protected JShell jShell;

    @FXML
    protected TextArea scriptInput, contextInput;
    @FXML
    protected TextField parametersInput;

    public ControlJexl() {
        baseTitle = message("JEXL");
        TipsLabelKey = "JEXLTips";
        category = TreeNode.JEXL;
    }

    public void setParamters(ControlData2DLoad data2dControler) {
        this.data2dControler = data2dControler;
        tableTreeNode = new TableTreeNode();
        tableTreeNodeTag = new TableTreeNodeTag();
        loadTree(null);
        initJShell();
    }

    public void initJShell() {
        if (jShell != null) {
            jShell.stop();
        }
        backgroundTask = new SingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                try {
                    jShell = JShellTools.initJEXL();
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }
        };
        start(backgroundTask, false);
    }

    public boolean pickInputs() {
        try {
            String script = scriptInput.getText();
            if (script == null || script.isBlank()) {
                return false;
            }
            TableStringValues.add("JexlScriptHistories", script.trim());
            String jexlScriptOrignal = "jexlScript = jexlEngine.createScript(\"" + script + "\");\n";
            String jexlScript = "jexlScript = jexlEngine.createScript(\"" + script.replaceAll("\n", " ") + "\");\n";
            JShellTools.runSnippet(jShell, jexlScriptOrignal, jexlScript);

            String jexlContext = "jexlContext.clear();";
            JShellTools.runSnippet(jShell, jexlContext);
            String contexts = contextInput.getText();
            if (contexts != null && !contexts.isBlank()) {
                jexlContext = contexts.trim();
                TableStringValues.add("JexlContextHistories", jexlContext);
                JShellTools.runScript(jShell, jexlContext);
            }

            String parameters = parametersInput.getText();
            if (parameters != null && !parameters.isBlank()) {
                parameters = parameters.trim();
                TableStringValues.add("JexlParamtersHistories", parameters);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean applyValues(Map<String, Object> values) {
        try {
            if (values == null) {
                return false;
            }
            String jexlContext = "";
            for (String key : values.keySet()) {
                Object v = values.get(key);
                if (!(v instanceof Number)) {
                    v = "'" + v + "'";
                }
                jexlContext += "jexlContext.set('" + key + "', " + v + ");\n";
            }
            return JShellTools.runScript(jShell, jexlContext);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public String runScript() {
        try {
            String parameters = parametersInput.getText();
            String execute;
            if (parameters != null && !parameters.isBlank()) {
                parameters = parameters.trim();
                execute = "jexlScript.execute(jexlContext, " + parameters + ");";
            } else {
                execute = "jexlScript.execute(jexlContext);";
            }
            return JShellTools.expValue(jShell, execute);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    protected void doubleClicked(TreeItem<TreeNode> item) {
        editNode(item);
    }

    @Override
    public void itemSelected(TreeItem<TreeNode> item) {
        editNode(item);
    }

    @Override
    protected void editNode(TreeItem<TreeNode> item) {
        scriptInput.clear();
        contextInput.clear();
        parametersInput.clear();
        if (item == null) {
            return;
        }
        TreeNode node = item.getValue();
        if (node == null) {
            return;
        }
        scriptInput.setText(node.getValue());
        contextInput.setText(node.getMore());
    }

    @FXML
    public void dataAction() {
        JexlController.open(scriptInput.getText(), contextInput.getText(), parametersInput.getText());
    }

    @FXML
    public void clearScript() {
        scriptInput.clear();
    }

    @FXML
    public void popNames(MouseEvent mouseEvent) {
        if (data2dControler == null) {
            return;
        }
        List<String> values = new ArrayList<>();
        values.add(message("RowNumber2"));
        values.addAll(data2dControler.data2D.columnNames());
        PopTools.popStringValues(this, scriptInput, mouseEvent, values);
    }

    @FXML
    protected void popScriptExamples(MouseEvent mouseEvent) {
        PopTools.popJEXLScriptExamples(this, scriptInput, contextInput, mouseEvent);
    }

    @FXML
    protected void popScriptHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, scriptInput, mouseEvent, "JexlScriptHistories", true);
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
    protected void popContextExamples(MouseEvent mouseEvent) {
        PopTools.popJEXLContextExamples(this, contextInput, mouseEvent);
    }

    @FXML
    protected void popContextHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, contextInput, mouseEvent, "JexlContextHistories", true);
    }

    @FXML
    public void clearParameters() {
        parametersInput.clear();
    }

    @FXML
    protected void popParametersHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, parametersInput, mouseEvent, "JexlParamtersHistories", true);
    }

    @FXML
    public void popSuggesions() {
        PopTools.popJShellSuggesions(this, jShell, scriptInput);
    }

    @Override
    public boolean controlAlt1() {
        popSuggesions();
        return true;
    }

    @Override
    public void cleanPane() {
        try {
            if (jShell != null) {
                jShell.stop();
            }
            jShell = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
