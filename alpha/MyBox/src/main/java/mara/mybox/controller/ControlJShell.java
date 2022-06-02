package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import jdk.jshell.JShell;
import mara.mybox.data2d.Data2D;
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
 * @CreateDate 2022-6-1
 * @License Apache License Version 2.0
 */
public class ControlJShell extends TreeNodesController {

    protected Data2D data2d;
    protected JShell jShell;

    @FXML
    protected TextArea scriptInput;
    @FXML
    protected JShellPaths pathsController;

    public ControlJShell() {
        baseTitle = message("JShell");
        TipsLabelKey = "JShellTips";
        category = TreeNode.JShellCode;
    }

    public void setParamters(Data2D data2d) {
        this.data2d = data2d;
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
                    jShell = JShell.create();
//                    jShell.addToClasspath(System.getProperty("java.class.path"));
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

//    public String runScript() {
//        try {
//            String parameters = parametersInput.getText();
//            String execute;
//            if (parameters != null && !parameters.isBlank()) {
//                parameters = parameters.trim();
//                execute = "jexlScript.execute(jexlContext, " + parameters + ");";
//            } else {
//                execute = "jexlScript.execute(jexlContext);";
//            }
//            return JShellTools.expValue(jShell, execute);
//        } catch (Exception e) {
//            MyBoxLog.error(e);
//            return null;
//        }
//    }
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
        if (item == null) {
            return;
        }
        TreeNode node = item.getValue();
        if (node == null) {
            return;
        }
        scriptInput.setText(node.getValue());
    }

    @FXML
    public void dataAction() {
        JShellController.open(scriptInput.getText());
    }

    @FXML
    public void clearScript() {
        scriptInput.clear();
    }

    @FXML
    public void popNames(MouseEvent mouseEvent) {
        if (data2d == null) {
            return;
        }
        List<String> values = new ArrayList<>();
        values.add(message("RowNumber2"));
        values.addAll(data2d.columnNames());
        PopTools.popStringValues(this, scriptInput, mouseEvent, values);
    }

    @FXML
    protected void popScriptExamples(MouseEvent mouseEvent) {
        try {
            MenuController controller = MenuController.open(this, scriptInput,
                    mouseEvent.getScreenX(), mouseEvent.getScreenY() + 20);
            controller.setTitleLabel(message("Syntax"));

            List<Node> topButtons = new ArrayList<>();
            Button newLineButton = new Button(message("Newline"));
            newLineButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    scriptInput.replaceText(scriptInput.getSelection(), "\n");
                    scriptInput.requestFocus();
                }
            });
            topButtons.add(newLineButton);
            Button clearInputButton = new Button(message("ClearInputArea"));
            clearInputButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    scriptInput.clear();
                }
            });
            topButtons.add(clearInputButton);
            controller.addFlowPane(topButtons);

            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    "int maxInt = Integer.MAX_VALUE, minInt = Integer.MIN_VALUE;",
                    "double maxDouble = Double.MAX_VALUE, minDouble = Double.MIN_VALUE;",
                    "float maxFloat = Float.MAX_VALUE, minFloat = Float.MIN_VALUE;",
                    "long maxLong = Long.MAX_VALUE, minLong = Long.MIN_VALUE;",
                    "short maxShort = Short.MAX_VALUE, minShort = Short.MIN_VALUE;",
                    "String s1 =\"Hello\";",
                    "String[] sArray = new String[3]; "
            ));
            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    ";", " , ", "( )", " = ", " { } ", "[ ]", "\"", " + ", " - ", " * ", " / "
            ));
            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    " == ", " != ", " >= ", " > ", " <= ", " < ", " && ", " || ", " ! "
            ));
            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    "if (3 > 2) {\n"
                    + "   int a = 1;\n"
                    + "}",
                    "for (int i = 0; i < 5; ++i) {\n"
                    + "    double d = i / 2d - 1;\n"
                    + "}",
                    "while (true) {\n"
                    + "    double d = Math.PI;\n"
                    + "    if ( d > 3 ) break;\n"
                    + "}"
            ));

            Hyperlink alink = new Hyperlink("Learning the Java Language");
            alink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openLink("https://docs.oracle.com/javase/tutorial/java/index.html");
                }
            });
            controller.addNode(alink);

            Hyperlink jlink = new Hyperlink("Java Development Kit (JDK) APIs");
            jlink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openLink("https://docs.oracle.com/en/java/javase/17/docs/api/index.html");
                }
            });
            controller.addNode(jlink);

            Hyperlink blink = new Hyperlink("Full list of Math functions");
            blink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openLink("https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Math.html");
                }
            });
            controller.addNode(blink);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void popScriptHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, scriptInput, mouseEvent, "JexlScriptHistories", true);
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
