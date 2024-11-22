package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
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
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import jdk.jshell.JShell;
import jdk.jshell.SourceCodeAnalysis;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.JShellTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class ControlDataJShell extends BaseDataValuesController {

    protected String outputs = "";
    protected JShell jShell;
    protected FxTask resetTask;

    @FXML
    protected TextArea codesInput;
    @FXML
    protected CheckBox wrapCheck;
    @FXML
    protected Button clearCodesButton, suggestionsButton;
    @FXML
    protected ControlWebView webViewController;
    @FXML
    protected JShellSnippets snippetsController;
    @FXML
    protected JShellPaths pathsController;

    public ControlDataJShell() {
        TipsLabelKey = "JShellTips";
    }

    @Override
    public void initEditor() {
        try {
            codesInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue v, String ov, String nv) {
                    valueChanged(true);
                }
            });

            wrapCheck.setSelected(UserConfig.getBoolean(baseName + "Wrap", false));
            wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Wrap", newValue);
                    codesInput.setWrapText(newValue);
                }
            });
            codesInput.setWrapText(wrapCheck.isSelected());

            webViewController.setParent(nodeEditor, ControlWebView.ScrollType.Bottom);
            snippetsController.setParameters(this);

            resetJShell();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void editValues() {
        try {
            isSettingValues = true;
            if (nodeEditor.currentNode != null) {
                codesInput.setText(nodeEditor.currentNode.getStringValue("codes"));
            } else {
                codesInput.clear();
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
            String text = codesInput.getText();
            node.setValue("codes", text == null ? null : text.trim());
            return node;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    public synchronized void resetJShell() {
        reset();
        resetTask = new FxTask<Void>(this) {
            @Override
            protected boolean handle() {
                try {
                    if (jShell == null) {
                        jShell = JShell.create();
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                pathsController.resetPaths(jShell);
                snippetsController.refreshSnippets();
            }

        };
        start(resetTask, true);
    }

    @FXML
    @Override
    public void startAction() {
        String codes = codesInput.getText();
        if (codes == null || codes.isBlank()) {
            popError(message("NoInput"));
            return;
        }
        if (startButton.getUserData() != null) {
            cancelAction();
            return;
        }
        StyleTools.setNameIcon(startButton, message("Stop"), "iconStop.png");
        startButton.applyCss();
        startButton.setUserData("started");
        showRightPane();
        task = new FxSingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                return handleCodes(codes);
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                cancelAction();
            }
        };
        start(task);
    }

    protected boolean handleCodes(String codes) {
        TableStringValues.add("JShellHistories", codes.trim());
        return runCodes(codes);
    }

    protected boolean runCodes(String codes) {
        try {
            if (codes == null || codes.isBlank()) {
                return false;
            }
            String leftCodes = codes;
            while (leftCodes != null && !leftCodes.isBlank()) {
                SourceCodeAnalysis.CompletionInfo info = jShell.sourceCodeAnalysis().analyzeCompletion(leftCodes);
                if (!runSnippet(info.source())) {
                    return false;
                }
                leftCodes = info.remaining();
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    protected boolean runSnippet(String source) {
        return runSnippet(source, source);
    }

    protected boolean runSnippet(String orignalSource, String source) {
        try {
            if (source == null || source.isBlank()) {
                return false;
            }
            String snippet = orignalSource == null ? source.trim() : orignalSource.trim();
            String snippetOutputs = DateTools.nowString()
                    + "<div class=\"valueText\" >"
                    + HtmlWriteTools.stringToHtml(snippet)
                    + "</div>";
            String results = JShellTools.runSnippet(jShell, orignalSource, source);
            snippetOutputs += "<div class=\"valueBox\">"
                    + HtmlWriteTools.stringToHtml(results.trim()) + "</div>";
            output(snippetOutputs);
        } catch (Exception e) {
            output(e.toString());
        }
        return true;
    }

    @FXML
    @Override
    public void clearAction() {
        codesInput.clear();
    }

    public void edit(String script) {
        codesInput.setText(script);
    }

    @Override
    public void cancelAction() {
        if (task != null) {
            task.cancel();
        }
        reset();
    }

    public void reset() {
        if (resetTask != null) {
            resetTask.cancel();
        }
        if (jShell != null) {
            jShell.stop();
        }
        snippetsController.refreshSnippets();
        StyleTools.setNameIcon(startButton, message("Start"), "iconStart.png");
        startButton.applyCss();
        startButton.setUserData(null);
    }

    protected void output(String msg) {
        Platform.runLater(() -> {
            outputs += msg + "<br><br>";
            String html = HtmlWriteTools.html(null, HtmlStyles.DefaultStyle, "<body>" + outputs + "</body>");
            webViewController.loadContents(html);
        });
        Platform.requestNextPulse();
    }

    @FXML
    protected void popHistories(Event event) {
        if (UserConfig.getBoolean(baseName + "CodesHistoriesPopWhenMouseHovering", false)) {
            showHistories(event);
        }
    }

    @FXML
    protected void showHistories(Event event) {
        PopTools.popStringValues(this, codesInput, event, baseName + "CodesHistories", false);
    }

    // https://stackoverflow.com/questions/53867043/what-are-the-limits-to-jshell?r=SearchResults
    @FXML
    protected void popSyntaxMenu(MouseEvent event) {
        if (UserConfig.getBoolean(interfaceName + "SyntaxPopWhenMouseHovering", false)) {
            showSyntaxMenu(event);
        }
    }

    @FXML
    protected void showSyntaxMenu(Event event) {
        try {
            String menuName = interfaceName + "Syntax";
            MenuController controller = MenuController.open(nodeEditor, codesInput, event, menuName, false);
            controller.setTitleLabel(message("Syntax"));

            List<Node> topButtons = new ArrayList<>();
            Button newLineButton = new Button();
            newLineButton.setGraphic(StyleTools.getIconImageView("iconTurnOver.png"));
            NodeStyleTools.setTooltip(newLineButton, new Tooltip(message("Newline")));
            newLineButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    codesInput.replaceText(codesInput.getSelection(), "\n");
                    codesInput.requestFocus();
                }
            });
            topButtons.add(newLineButton);

            Button clearInputButton = new Button();
            clearInputButton.setGraphic(StyleTools.getIconImageView("iconClear.png"));
            NodeStyleTools.setTooltip(clearInputButton, new Tooltip(message("ClearInputArea")));
            clearInputButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    codesInput.clear();
                }
            });
            topButtons.add(clearInputButton);

            controller.addFlowPane(topButtons);

            PopTools.addButtonsPane(controller, codesInput, Arrays.asList(
                    "int maxInt = Integer.MAX_VALUE, minInt = Integer.MIN_VALUE;",
                    "double maxDouble = Double.MAX_VALUE, minDouble = -Double.MAX_VALUE;",
                    "float maxFloat = Float.MAX_VALUE, minFloat = Float.MIN_VALUE;",
                    "long maxLong = Long.MAX_VALUE, minLong = Long.MIN_VALUE;",
                    "short maxShort = Short.MAX_VALUE, minShort = Short.MIN_VALUE;",
                    "String s1 =\"Hello\";",
                    "String[] sArray = new String[3]; "
            ));
            PopTools.addButtonsPane(controller, codesInput, Arrays.asList(
                    ";", " , ", "( )", " = ", " { } ", "[ ]", "\"", " + ", " - ", " * ", " / "
            ));
            PopTools.addButtonsPane(controller, codesInput, Arrays.asList(
                    " == ", " != ", " >= ", " > ", " <= ", " < ", " && ", " || ", " ! "
            ));
            PopTools.addButtonsPane(controller, codesInput, Arrays.asList(
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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void popSuggesions() {
        PopTools.popJShellSuggesions(nodeEditor, jShell, codesInput);
    }

    @FXML
    public void popJavaHelps(Event event) {
        if (UserConfig.getBoolean("JavaHelpsPopWhenMouseHovering", false)) {
            showJavaHelps(event);
        }
    }

    @FXML
    public void showJavaHelps(Event event) {
        popEventMenu(event, HelpTools.javaHelps());
    }

    /*
        right pane
     */
    @FXML
    protected void showHtmlStyle(Event event) {
        PopTools.popHtmlStyle(event, webViewController);
    }

    @FXML
    protected void popHtmlStyle(Event event) {
        if (UserConfig.getBoolean("HtmlStylesPopWhenMouseHovering", false)) {
            showHtmlStyle(event);
        }
    }

    @FXML
    public void editResults() {
        webViewController.editAction();
    }

    @FXML
    public void clearResults() {
        outputs = "";
        webViewController.clear();
    }

    @Override
    public boolean controlAlt1() {
        popSuggesions();
        return true;
    }

    @Override
    public void cleanPane() {
        try {
            cancelAction();
            resetTask = null;
            jShell = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
