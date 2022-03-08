package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis.CompletionInfo;
import mara.mybox.data.JShellSnippet;
import mara.mybox.db.data.NamedValues;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-4
 * @License Apache License Version 2.0
 */
public class JShellController extends BaseTableViewController<JShellSnippet> {

    protected JShell jShell;
    protected String outputs = "";

    @FXML
    protected ControlNamedValues listController;
    @FXML
    protected TextArea codesArea;
    @FXML
    protected Button deleteSnippetsButton;
    @FXML
    protected CheckBox variablesCheck, declarationsCheck, statementsCheck, methodsCheck,
            importsCheck, expressionsCheck, errorsCheck;
    @FXML
    protected TableColumn<JShellSnippet, String> sidColumn, typeColumn, subTypeColumn,
            nameColumn, statusColumn, valueColumn, sourceColumn, some1Column, some2Column;
    @FXML
    protected ControlWebView webViewController;

    public JShellController() {
        baseTitle = message("JShell");
        TipsLabelKey = "JShellTips";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            listController.loadList("JShellSnippets");
            listController.uesNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    NamedValues selected = listController.tableView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        codesArea.setText(selected.getValue());
                    }
                }
            });

            webViewController.setParent(this, ControlWebView.ScrollType.Bottom);

            recoverAction();

            variablesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    refreshSnippets();
                }
            });
            declarationsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    refreshSnippets();
                }
            });
            statementsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    refreshSnippets();
                }
            });
            methodsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    refreshSnippets();
                }
            });
            importsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    refreshSnippets();
                }
            });
            expressionsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    refreshSnippets();
                }
            });
            errorsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    refreshSnippets();
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initColumns() {
        try {
            super.initColumns();

            sidColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            subTypeColumn.setCellValueFactory(new PropertyValueFactory<>("subType"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            sourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
            some1Column.setCellValueFactory(new PropertyValueFactory<>("some1"));
            some2Column.setCellValueFactory(new PropertyValueFactory<>("some2"));

            checkButtons();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();
        boolean isEmpty = tableData == null || tableData.isEmpty();
        boolean none = isEmpty || tableView.getSelectionModel().getSelectedItem() == null;
        deleteSnippetsButton.setDisable(none);
    }

    @FXML
    @Override
    public synchronized void recoverAction() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                try {
                    jShell = JShell.create();
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

    @FXML
    @Override
    public void startAction() {
        if (startButton.getUserData() != null) {
            cancelAction();
            return;
        }
        String codes = codesArea.getText();
        if (codes == null || codes.isBlank()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        StyleTools.setNameIcon(startButton, message("Stop"), "iconStop.png");
        startButton.applyCss();
        startButton.setUserData("started");
        codesArea.clear();
        task = new SingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                return runCodes(codes);
            }

            @Override
            protected void finalAction() {
                cancelAction();
            }
        };
        start(task);
    }

    protected boolean runCodes(String codes) {
        try {
            TableStringValues.add("JShellHistories", codes);
            String leftCodes = codes;
            while (leftCodes != null && !leftCodes.isBlank()) {
                CompletionInfo info = jShell.sourceCodeAnalysis().analyzeCompletion(leftCodes);
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
        try {
            if (source == null || source.isBlank()) {
                return false;
            }
            List<SnippetEvent> events = jShell.eval(source);
            String snippetOutputs = DateTools.nowString()
                    + "<div class=\"valueText\" >"
                    + HtmlWriteTools.stringToHtml(source)
                    + "</div>";
            String results = "";
            for (int i = 0; i < events.size(); i++) {
                if (task == null || task.isCancelled()) {
                    output(message("Canceled"));
                    return true;
                }
                SnippetEvent e = events.get(i);
                JShellSnippet jShellSnippet = new JShellSnippet(jShell, e.snippet());
                if (i > 0) {
                    results += "\n";
                }
                results += "id: " + jShellSnippet.getId() + "\n";
                if (jShellSnippet.getStatus() != null) {
                    results += "status: " + jShellSnippet.getStatus() + "\n";
                }
                if (jShellSnippet.getType() != null) {
                    results += "type: " + jShellSnippet.getType() + "\n";
                }
                if (jShellSnippet.getName() != null) {
                    results += "name: " + jShellSnippet.getName() + "\n";
                }
                if (jShellSnippet.getValue() != null) {
                    results += "value: " + jShellSnippet.getValue() + "\n";
                }
            }
            snippetOutputs += "<div class=\"valueBox\">"
                    + HtmlWriteTools.stringToHtml(results) + "</div>";
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
        jShell.stop();
        refreshSnippets();
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

    }

    @FXML
    protected void clearCodes() {
        codesArea.clear();
    }

    @FXML
    @Override
    public void saveAction() {
        String codes = codesArea.getText();
        if (codes == null || codes.isBlank()) {
            popError(message("NoData"));
            return;
        }
        String name = PopTools.askValue(getTitle(), null, message("Name"), "");
        listController.save(name, codes);

    }

    @FXML
    @Override
    public void saveAsAction() {
        String codes = codesArea.getText();
        if (codes == null || codes.isBlank()) {
            popError(message("NoData"));
            return;
        }
        File file = chooseSaveFile("JShellSnippets-" + DateTools.nowFileString() + ".java");
        if (file == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                File tfile = TextFileTools.writeFile(file, codes, Charset.forName("UTF-8"));
                return tfile != null && tfile.exists();
            }

            @Override
            protected void whenSucceeded() {
                popInformation(message("Saved"));
                recordFileWritten(file);
            }

        };
        start(task);
    }

    @Override
    public void sourceFileChanged(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        codesArea.clear();
        task = new SingletonTask<Void>(this) {

            String codes;

            @Override
            protected boolean handle() {
                codes = TextFileTools.readTexts(file);
                return codes != null;
            }

            @Override
            protected void whenSucceeded() {
                codesArea.setText(codes);
                recordFileOpened(file);
            }

        };
        start(task);
    }

    @FXML
    protected synchronized void refreshSnippets() {
        tableData.clear();
        for (Snippet snippet : jShell.snippets().toList()) {
            try {
                switch (snippet.kind()) {
                    case VAR:
                        if (variablesCheck.isSelected()) {
                            tableData.add(new JShellSnippet(jShell, snippet));
                        }
                        break;
                    case TYPE_DECL:
                        if (declarationsCheck.isSelected()) {
                            tableData.add(new JShellSnippet(jShell, snippet));
                        }
                        break;
                    case STATEMENT:
                        if (statementsCheck.isSelected()) {
                            tableData.add(new JShellSnippet(jShell, snippet));
                        }
                        break;
                    case METHOD:
                        if (methodsCheck.isSelected()) {
                            tableData.add(new JShellSnippet(jShell, snippet));
                        }
                        break;
                    case IMPORT:
                        if (importsCheck.isSelected()) {
                            tableData.add(new JShellSnippet(jShell, snippet));
                        }
                        break;
                    case EXPRESSION:
                        if (expressionsCheck.isSelected()) {
                            tableData.add(new JShellSnippet(jShell, snippet));
                        }
                        break;
                    default:
                        if (errorsCheck.isSelected()) {
                            tableData.add(new JShellSnippet(jShell, snippet));
                        }
                        break;
                }
            } catch (Exception e) {
//                output(HtmlWriteTools.stringToHtml(e.toString()));
            }
        }
    }

    @FXML
    protected void deleteSnippets() {
        List<JShellSnippet> selected = new ArrayList<>();
        selected.addAll(tableView.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) {
            return;
        }
        for (JShellSnippet snippet : selected) {
            jShell.drop(snippet.getSnippet());
        }
        refreshSnippets();
    }

    @FXML
    protected void clearSnippets() {
        for (JShellSnippet snippet : tableData) {
            jShell.drop(snippet.getSnippet());
        }
        refreshSnippets();
    }

    // https://stackoverflow.com/questions/53867043/what-are-the-limits-to-jshell?r=SearchResults
    @FXML
    protected void popExpressionsMenu(MouseEvent mouseEvent) {
        try {
            MenuController controller = MenuController.open(this, codesArea, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            controller.setTitleLabel(message("Expressions"));

            List<Node> topButtons = new ArrayList<>();
            Button newLineButton = new Button(message("Newline"));
            newLineButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    codesArea.replaceText(codesArea.getSelection(), "\n");
                    codesArea.requestFocus();
                }
            });
            topButtons.add(newLineButton);
            Button clearInputButton = new Button(message("ClearInputArea"));
            clearInputButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    codesArea.clear();
                }
            });
            topButtons.add(clearInputButton);
            controller.addFlowPane(topButtons);

            PopTools.addButtonsPane(controller, codesArea, Arrays.asList(
                    " Math.E ", " Math.PI ", " Math.abs(d3) ", " Math.exp(d3) ",
                    " Math.sqrt(d3) ", " Math.cbrt(d3) ", " Math.pow(d2, 3d) ",
                    " Math.cos(d3) ", " Math.cosh(d3) ", " Math.acos(d3) ",
                    " Math.sin(d3) ", " Math.sinh(d3) ", " Math.asin(d3) ",
                    " Math.tan(d3) ", " Math.tanh(d3) ", " Math.atan(d3) ", " Math.atan2(d3) ",
                    " Math.expm1(d3) ", " Math.log(d3) ", " Math.log10(d3) ", " Math.log1p(d3) ",
                    " Math.ceil(d3) ", " Math.floor(d3) ", " Math.round(d3) ", " Math.rint(d3) ",
                    " Math.scalb(d3, 3) ", " Math.signum(d3) ", " Math.random() ",
                    " Math.max(d1, d3) ", " Math.min(d2, d3) ",
                    " Math.toDegrees(d3) ", " Math.toRadians(d3) "
            ));

            PopTools.addButtonsPane(controller, codesArea, Arrays.asList(
                    " a + d2 * 3 / 7 - 1 ", " d1 == d3 ", " b >= d2 ", " d1 != d2 ",
                    " s1.equalsIgnoreCase(\"hello\") ", " s1.equals(\"hello\") ",
                    " s1 + \" World\" ", " s1.startsWith(\"H\") ", " s1.endsWith(\"o\") ",
                    " s1.split(\"o\")", " s1.substring(2, 5) ", " s1.charAt(3) "
            ));

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
    protected void popSyntaxMenu(MouseEvent mouseEvent) {
        try {
            MenuController controller = MenuController.open(this, codesArea, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            controller.setTitleLabel(message("Syntax"));

            List<Node> topButtons = new ArrayList<>();
            Button newLineButton = new Button(message("Newline"));
            newLineButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    codesArea.replaceText(codesArea.getSelection(), "\n");
                    codesArea.requestFocus();
                }
            });
            topButtons.add(newLineButton);
            Button clearInputButton = new Button(message("ClearInputArea"));
            clearInputButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    codesArea.clear();
                }
            });
            topButtons.add(clearInputButton);
            controller.addFlowPane(topButtons);

            PopTools.addButtonsPane(controller, codesArea, Arrays.asList(
                    ";", " , ", "( )", " = ", " { } ", "[ ]", "\"", " + ", " - ", " * ", " / ",
                    " == ", " != ", " >= ", " > ", " <= ", " < "
            ));
            PopTools.addButtonsPane(controller, codesArea, Arrays.asList(
                    " int ", "double ", "float ", "long ", "short ", "String "
            ));
            PopTools.addButtonsPane(controller, codesArea, Arrays.asList(
                    "if (3 > 2) {\n"
                    + "   int a = 1;\n"
                    + "}",
                    "for (int i = 0; i < 5; ++i) {\n"
                    + "    double d = i / 2d - 1;\n"
                    + "}"
            ));

            PopTools.addButtonsPane(controller, codesArea, Arrays.asList(
                    "int a = 2, b = -9;", " double d1, d2 = 5d, d3 = -6.3; ", "String s1 =\"Hello\";",
                    "float[] fArray = new float[3]; "
            ));

            Map<String, String> examples = new HashMap<>();
            examples.put(message("CircleArea"),
                    "double circleAreaByDiameter(double diameter) {\n"
                    + "         double radius = diameter / 2;\n"
                    + "         return   Math.PI *  radius * radius ;\n"
                    + "}\n"
                    + "\n"
                    + "circleAreaByDiameter(120) + circleAreaByDiameter(30);\n");
            examples.put(message("RoundDouble"),
                    "import java.math.BigDecimal;\n"
                    + "import java.math.RoundingMode;\n"
                    + "double scale(double v, int scale) {\n"
                    + "          BigDecimal b = new BigDecimal(v);\n"
                    + "          return b.setScale(scale, RoundingMode.HALF_UP).doubleValue();\n"
                    + "}"
                    + "\n"
                    + "scale(Math.PI, 3)");
            examples.put(message("FormatDouble"),
                    "import java.math.BigDecimal;\n"
                    + "import java.math.RoundingMode;\n"
                    + "double scale(double v, int scale) {\n"
                    + "          BigDecimal b = new BigDecimal(v);\n"
                    + "          return b.setScale(scale, RoundingMode.HALF_UP).doubleValue();\n"
                    + "}\n"
                    + "\n"
                    + "import java.text.DecimalFormat;\n"
                    + "String formatDouble(double data, int scale) {\n"
                    + "        try {\n"
                    + "               String format = \"#,###\";\n"
                    + "                if (scale > 0) {\n"
                    + "                    format += \".\" + \"#\".repeat(scale);\n"
                    + "                }\n"
                    + "                DecimalFormat df = new DecimalFormat(format);\n"
                    + "                return df.format(scale(data, scale));\n"
                    + "         } catch (Exception e) {\n"
                    + "                return e.toString();\n"
                    + "         }\n"
                    + "}\n"
                    + "\n"
                    + "double circleAreaByRadius(double radius) {\n"
                    + "         return   Math.PI *  radius * radius ;\n"
                    + "}\n"
                    + "\n"
                    + "formatDouble(circleAreaByRadius(273.4), 4)");
            PopTools.addButtonsPane(controller, codesArea, examples);

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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popHtmlStyle(MouseEvent mouseEvent) {
        PopTools.popHtmlStyle(mouseEvent, webViewController);
    }

    @FXML
    public void editResults() {
        webViewController.editAction();
    }

    @FXML
    public void clearResults() {
        outputs = "";
        webViewController.loadContents("");
    }

    @Override
    public void cleanPane() {
        try {
            cancelAction();
            jShell = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
