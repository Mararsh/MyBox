package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import jdk.jshell.JShell;
import jdk.jshell.SourceCodeAnalysis;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-5-17
 * @License Apache License Version 2.0
 */
public class JexlEditor extends JShellEditor {

    @FXML
    protected TextArea contextArea;
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
                    jShell = JShell.create();
                    jShell.addToClasspath(System.getProperty("java.class.path"));
                    String initCodes = "import org.apache.commons.jexl3.JexlBuilder;\n"
                            + "import org.apache.commons.jexl3.JexlEngine;\n"
                            + "import org.apache.commons.jexl3.JexlScript;\n"
                            + "import org.apache.commons.jexl3.MapContext;\n"
                            + "JexlEngine  jexlEngine = new JexlBuilder().cache(512).strict(true).silent(false).create();\n"
                            + "MapContext jexlContext = new MapContext();"
                            + "JexlScript jexlScript;";
                    String leftCodes = initCodes;
                    while (leftCodes != null && !leftCodes.isBlank()) {
                        SourceCodeAnalysis.CompletionInfo info = jShell.sourceCodeAnalysis().analyzeCompletion(leftCodes);
                        String snippet = info.source().trim();
                        jShell.eval(snippet);
                        leftCodes = info.remaining();
                    }
                    paths = expValue("System.getProperty(\"java.class.path\")")
                            + System.getProperty("java.class.path");
                    if (paths.startsWith("\"") && paths.endsWith("\"")) {
                        paths = paths.substring(1, paths.length() - 1);
                    }
                    paths += System.getProperty("java.class.path");
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
            String contexts = contextArea.getText();
            if (contexts != null && !contexts.isBlank()) {
                jexlContext += "\n" + contexts.trim();
            }
            runCodes(jexlContext);

            String jexlScriptOrignal = "jexlScript = jexlEngine.createScript(\""
                    + script + "\");\n";
            String jexlScript = "jexlScript = jexlEngine.createScript(\""
                    + script.replaceAll("\n", "  ") + "\");\n";
            runSnippet(jexlScriptOrignal, jexlScript);

            String parameters = parametersInput.getText();
            String execute;
            if (parameters != null && !parameters.isBlank()) {
                execute = "jexlScript.execute(jexlContext, " + parameters.trim() + ");";
            } else {
                execute = "jexlScript.execute(jexlContext);";
            }
            runCodes(execute);

            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    // https://commons.apache.org/proper/commons-jexl/reference/syntax.html
    @FXML
    protected void popScriptExamples(MouseEvent mouseEvent) {
        try {
            MenuController controller = MenuController.open(jShellController, valueInput,
                    mouseEvent.getScreenX(), mouseEvent.getScreenY() + 20);
            controller.setTitleLabel(message("Syntax"));

            List<Node> topButtons = new ArrayList<>();
            Button newLineButton = new Button(message("Newline"));
            newLineButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    valueInput.replaceText(valueInput.getSelection(), "\n");
                    valueInput.requestFocus();
                }
            });
            topButtons.add(newLineButton);
            Button clearInputButton = new Button(message("ClearInputArea"));
            clearInputButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    valueInput.clear();
                }
            });
            topButtons.add(clearInputButton);
            controller.addFlowPane(topButtons);
            controller.addNode(new Separator());

            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    " new('java.math.BigDecimal', 9) ", " new('java.lang.Double', 10d) ",
                    " new('java.lang.Long', 10) ", " new('java.lang.Integer', 10) ",
                    " new('java.lang.String', 'Hello') ", "  new('java.util.Date') "
            ));
            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    " true ", " false ", " null ", " empty(x) ", " size(x) ",
                    " 3 =~ [1,'2',3, 'hello'] ", " 2 !~ {1,'2',3, 'hello'} ",
                    " 'hello'.startsWith('hell') ", " 'hello'.endsWith('ll') ",
                    " not 'hello'.startsWith('hell') "
            ));
            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    " = ", " + ", " - ", " * ", " / ", ";", " , ",
                    "( )", " { } ", "[ ]", "\"\"", "''", " : ", " .. ",
                    " == ", " != ", " >= ", " > ", " <= ", " < ",
                    " && ", " and ", " || ", " or ", " !", " not ",
                    " =~ ", " !~ "
            ));
            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    "var array = [ 'A', 'B', 'C', 'D' ];\n"
                    + "return size(array);",
                    "var list = [ 'A', 'B', 'C', 'D' ];\n"
                    + "return list.size();",
                    "var set = { 'A', 'B', 'C', 'D' };\n"
                    + "return set.toString();",
                    "var map = { 'A': 1,'B': 2,'C': 3,'D': 4 };\n"
                    + "return map.toString();"
            ), false);
            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    "var d = new('java.lang.Double', 0d);\n"
                    + "return 'min_double=' + d.MIN_VALUE   +  '   max_double=' + d.MAX_VALUE;",
                    "var array = [ 'A', 'B', 'C', 'D' ];\n"
                    + "var arrayString = '';\n"
                    + "for(var i : 1 .. size (array)) {\n"
                    + "  if (empty (arrayString)) {\n"
                    + "    arrayString = array[i-1];\n"
                    + "  } else {\n"
                    + "    arrayString += ' , ' + array[i-1];\n"
                    + "  }\n"
                    + "};\n"
                    + "return arrayString;"
            ), false);

            List<Node> buttons = new ArrayList<>();
            Button ebutton = new Button("Math.E + Math.exp(x)");
            ebutton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    valueInput.setText(ebutton.getText());
                    controller.getThisPane().requestFocus();
                    valueInput.requestFocus();

                    if (!contextArea.getText().contains("jexlContext.set(\"Math\", Math.class);")) {
                        contextArea.appendText("jexlContext.set(\"Math\", Math.class);\n");
                    }
                    if (!contextArea.getText().contains("jexlContext.set(\"x\",")) {
                        contextArea.appendText("jexlContext.set(\"x\", 9);\n");
                    }
                }
            });
            buttons.add(ebutton);
            Button cbutton = new Button("var circleArea = function(r) \n"
                    + "{ Math.PI * r * r };\n"
                    + "return circleArea (2.6);");
            cbutton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    valueInput.setText(cbutton.getText());
                    controller.getThisPane().requestFocus();
                    valueInput.requestFocus();

                    if (!contextArea.getText().contains("jexlContext.set(\"Math\", Math.class);")) {
                        contextArea.appendText("jexlContext.set(\"Math\", Math.class);\n");
                    }
                }
            });
            buttons.add(cbutton);
            Button sbutton = new Button("var s = 'hello ';\n"
                    + "while (s.length() < len) {\n"
                    + "   s += 'a';\n"
                    + "}\n"
                    + "return s;\n");
            sbutton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    valueInput.setText(sbutton.getText());
                    controller.getThisPane().requestFocus();
                    valueInput.requestFocus();

                    if (!contextArea.getText().contains("jexlContext.set(\"len\",")) {
                        contextArea.appendText("jexlContext.set(\"len\", 8);\n");
                    }
                }
            });
            buttons.add(sbutton);
            controller.addFlowPane(buttons);

            Hyperlink elink = new Hyperlink("JEXL Reference");
            elink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openLink("https://commons.apache.org/proper/commons-jexl/reference/index.html");
                }
            });
            controller.addNode(elink);

            Hyperlink jlink = new Hyperlink("Java Development Kit (JDK) APIs");
            jlink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openLink("https://docs.oracle.com/en/java/javase/17/docs/api/index.html");
                }
            });
            controller.addNode(jlink);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void popScriptHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, valueInput, mouseEvent, "JexlScriptHistories", true);
    }

    @FXML
    public void addContext() {
        contextArea.appendText("jexlContext.set(\"name\", value);\n");
    }

    @FXML
    public void clearContext() {
        contextArea.clear();
    }

    @FXML
    protected void popContextExamples(MouseEvent mouseEvent) {
        try {
            MenuController controller = MenuController.open(jShellController, valueInput,
                    mouseEvent.getScreenX(), mouseEvent.getScreenY() + 20);
            controller.setTitleLabel(message("Syntax"));

            List<Node> topButtons = new ArrayList<>();
            Button newLineButton = new Button(message("Newline"));
            newLineButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    valueInput.replaceText(valueInput.getSelection(), "\n");
                    valueInput.requestFocus();
                }
            });
            topButtons.add(newLineButton);
            Button clearInputButton = new Button(message("ClearInputArea"));
            clearInputButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    valueInput.clear();
                }
            });
            topButtons.add(clearInputButton);
            controller.addFlowPane(topButtons);
            controller.addNode(new Separator());

            PopTools.addButtonsPane(controller, contextArea, Arrays.asList(
                    "jexlContext.set(\"Math\", Math.class);\n",
                    "jexlContext.set(\"BigDecimal\", new java.math.BigDecimal(10));\n",
                    "jexlContext.set(\"df\", \"#,###\");\n"
                    + "jexlContext.set(\"DecimalFormat\", new java.text.DecimalFormat(df));\n",
                    "jexlContext.set(\"x\", 5);\n",
                    "jexlContext.set(\"s\", \"hello\");\n"
            ));

            Hyperlink elink = new Hyperlink("JEXL Overview");
            elink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openLink("https://commons.apache.org/proper/commons-jexl/index.html");
                }
            });
            controller.addNode(elink);

            Hyperlink jlink = new Hyperlink("Java Development Kit (JDK) APIs");
            jlink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openLink("https://docs.oracle.com/en/java/javase/17/docs/api/index.html");
                }
            });
            controller.addNode(jlink);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void popContextHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, contextArea, mouseEvent, "JexlContextHistories", true);
    }

}
