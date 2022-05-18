package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.MapContext;

/**
 * @Author Mara
 * @CreateDate 2022-5-17
 * @License Apache License Version 2.0
 */
public class JexlEditor extends TreeNodeEditor {

    protected JexlController jexlController;
    protected String outputs = "";
    protected JexlEngine jexlEngine;
    protected JexlContext jexlContext;
    protected JexlScript jexlScript;

    @FXML
    protected Button clearCodesButton;
    @FXML
    protected TextArea parsedArea;

    public JexlEditor() {
        defaultExt = "java";
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(clearCodesButton, new Tooltip(message("Clear") + "\nCTRL+g"));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void setParameters(JexlController jexlController) {
        try {
            this.jexlController = jexlController;
            jexlEngine = new JexlBuilder().cache(512).strict(true).silent(false).create();
            resetContext();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public synchronized void resetContext() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                try {
                    jexlContext = new MapContext();
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void finalAction() {
                cancelAction();
            }
        };
        start(task);
    }

    @Override
    protected void showEditorPane() {
    }

    @FXML
    @Override
    public void goAction() {
        try {
            String inputs = valueInput.getText();
            if (inputs == null || inputs.isBlank()) {
                popError(message("NoInput"));
                return;
            }
            jexlScript = jexlEngine.createScript(inputs);
            String parsed = jexlScript.getParsedText();
            parsedArea.setText(parsed);
            TableStringValues.add("JexlHistories", parsed);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void startAction() {
        if (startButton.getUserData() != null) {
            cancelAction();
            return;
        }
        String script = parsedArea.getText();
        if (script == null || script.isBlank()) {
            popError(message("NoInput"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        StyleTools.setNameIcon(startButton, message("Stop"), "iconStop.png");
        startButton.applyCss();
        startButton.setUserData("started");
        task = new SingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                return runScript(script);
            }

            @Override
            protected void finalAction() {
                cancelAction();
                editNode(null);
            }
        };
        start(task);
    }

    protected boolean runScript(String script) {
        try {
            if (script == null || script.isBlank()) {
                return false;
            }
            Object results = jexlScript.execute(jexlContext);
            String snippetOutputs = DateTools.nowString()
                    + "<div class=\"valueText\" >"
                    + HtmlWriteTools.stringToHtml(parsedArea.getText())
                    + "</div>";
            snippetOutputs += "<div class=\"valueBox\">"
                    + HtmlWriteTools.stringToHtml(results.toString()) + "</div>";
            output(snippetOutputs);

        } catch (Exception e) {
            output(e.toString());
        }
        return true;
    }

    @Override
    public void cancelAction() {
        if (task != null) {
            task.cancel();
        }
        StyleTools.setNameIcon(startButton, message("Start"), "iconStart.png");
        startButton.applyCss();
        startButton.setUserData(null);
    }

    protected void output(String msg) {
        Platform.runLater(() -> {
            outputs += msg + "<br><br>";
            String html = HtmlWriteTools.html(null, HtmlStyles.DefaultStyle, "<body>" + outputs + "</body>");
            jexlController.webViewController.loadContents(html);
        });

    }

    // https://commons.apache.org/proper/commons-jexl/reference/syntax.html
    @FXML
    protected void popSyntaxMenu(MouseEvent mouseEvent) {
        try {
            MenuController controller = MenuController.open(jexlController, valueInput,
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

            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    "int maxInt = Integer.MAX_VALUE, minInt = Integer.MIN_VALUE;",
                    "double maxDouble = Double.MAX_VALUE, minDouble = Double.MIN_VALUE;",
                    "float maxFloat = Float.MAX_VALUE, minFloat = Float.MIN_VALUE;",
                    "long maxLong = Long.MAX_VALUE, minLong = Long.MIN_VALUE;",
                    "short maxShort = Short.MAX_VALUE, minShort = Short.MIN_VALUE;",
                    "String s1 =\"Hello\";",
                    "String[] sArray = new String[3]; "
            ));
            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    ";", " , ", "( )", " = ", " { } ", "[ ]", "\"", " + ", " - ", " * ", " / ",
                    " == ", " != ", " >= ", " > ", " <= ", " < "
            ));
            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
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

            Hyperlink blink = new Hyperlink("JEXL Reference");
            blink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openLink("https://commons.apache.org/proper/commons-jexl/reference/index.html");
                }
            });
            controller.addNode(blink);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void popHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, valueInput, mouseEvent, "JexlHistories");
    }

    @Override
    public void cleanPane() {
        try {
            cancelAction();
            jexlContext = null;
            jexlEngine = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
