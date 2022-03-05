package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import jdk.jshell.VarSnippet;
import mara.mybox.data.JShellSnippet;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.HtmlWriteTools;
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
    protected TextArea codesArea;
    @FXML
    protected Button deleteSnippetsButton;
    @FXML
    protected CheckBox varCheck;
    @FXML
    protected TableColumn<JShellSnippet, String> sidColumn, typeColumn, subTypeColumn,
            nameColumn, statusColumn, valueColumn, sourceColumn;
    @FXML
    protected ControlWebView webViewController;

    public JShellController() {
        baseTitle = message("JShell");
        TipsLabelKey = "NotesComments";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            webViewController.setParent(this);

            recoverAction();

            varCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
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
                try {
                    TableStringValues.add("JShellHistories", codes);
                    List<SnippetEvent> events = jShell.eval(codes);
                    for (SnippetEvent e : events) {
                        if (this.isCancelled()) {
                            output(message("Canceled"));
                            return true;
                        }
                        output("<div><font color=\"blue\"><b>&gt;&nbsp;"
                                + HtmlWriteTools.stringToHtml(e.snippet().source()) + "</b></font></div>");
//                output(e.status() + " " + e.snippet().kind() + " " + e.snippet().subKind() + " " + e.value());
                        if (e.causeSnippet() != null) {
                            continue;
                        }
                        switch (e.status()) {
                            case VALID:
                                if (e.value() != null) {
                                    output(message("Value") + ": " + HtmlWriteTools.stringToHtml(e.value()) + "<br>");
                                } else {
                                    output(message("Successful") + "<br>");
                                }
                                break;
                            case RECOVERABLE_DEFINED:
                                output("With unresolved references" + "<br>");
                                break;
                            case RECOVERABLE_NOT_DEFINED:
                                output("Possibly reparable, failed" + "<br>");
                                break;
                            case REJECTED:
                                output(message("Failed") + "<br>");
                                break;
                        }
                    }
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
            outputs += msg;
            String html = HtmlWriteTools.html(null, "<body>" + outputs + "</body>");
            webViewController.loadContents(html);
        });

    }

    @FXML
    protected void clearCodes() {
        codesArea.clear();
    }

    @FXML
    protected synchronized void refreshSnippets() {
        tableData.clear();
        if (varCheck.isSelected()) {
            for (VarSnippet snippet : jShell.variables().toList()) {
                try {
                    tableData.add(new JShellSnippet(jShell, snippet));
                } catch (Exception e) {
                    output(HtmlWriteTools.stringToHtml(e.toString()));
                }
            }
        } else {
            for (Snippet snippet : jShell.snippets().toList()) {
                try {
                    tableData.add(new JShellSnippet(jShell, snippet));
                } catch (Exception e) {
                    output(HtmlWriteTools.stringToHtml(e.toString()));
                }
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
        tableData.clear();
    }

    // https://stackoverflow.com/questions/53867043/what-are-the-limits-to-jshell?r=SearchResults
    @FXML
    protected void popExamplesMenu(MouseEvent mouseEvent) {
        try {
            MenuController controller = MenuController.open(this, codesArea, mouseEvent.getScreenX(), mouseEvent.getScreenY());

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
            Button cButton = new Button(message("Clear"));
            cButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    codesArea.clear();
                }
            });
            topButtons.add(cButton);
            controller.addFlowPane(topButtons);

            PopTools.addButtonsPane(controller, codesArea, Arrays.asList(
                    ";", " , ", "( )", " = ", " { } ", "[ ]", "\"", " + ", " - ", " * ", " / ",
                    " == ", " != ", " >= ", " > ", " <= ", " < ",
                    " int ", "double ", "float ", "String ", ""
            ));

            PopTools.addButtonsPane(controller, codesArea, Arrays.asList(
                    "int a = 2, b = -9;", " double d1, d2 = 5d, d3 = -6.3; ", "String s1 =\"Hello\";",
                    "float[] fArray = new float[3]; ", "Date date = new Date(); "
            ));

            PopTools.addButtonsPane(controller, codesArea, Arrays.asList(
                    " a + d2 * 3 / 7 - 1 ", " d1 == d3 ", " b >= d2 ", " d1 != d2 ",
                    " s1.equalsIgnoreCase(\"hello\") ", " s1.equals(\"hello\") ",
                    " s1 + \" World\" ", " s1.startsWith(\"H\") ", " s1.endsWith(\"o\") ",
                    " s1.split(\"o\")", " s1.substring(2, 5) ", " s1.charAt(3) "
            ));

            PopTools.addButtonsPane(controller, codesArea, Arrays.asList(
                    " Math.abs(d3) ", " Math.sqrt(d3) ", " Math.cbrt(d3) ", " Math.exp(d3) ", " Math.pow(d2, 3d) ",
                    " Math.cos(d3) ", " Math.cosh(d3) ", " Math.acos(d3) ",
                    " Math.sin(d3) ", " Math.sinh(d3) ", " Math.asin(d3) ",
                    " Math.tan(d3) ", " Math.tanh(d3) ", " Math.atan(d3) ", " Math.atan2(d3) ",
                    " Math.expm1(d3) ", " Math.log(d3) ", " Math.log10(d3) ", " Math.log1p(d3) ",
                    " Math.ceil(d3) ", " Math.floor(d3) ", " Math.round(d3) ", " Math.rint(d3) ",
                    " Math.scalb(d3, 3) ", " Math.signum(d3) ", " Math.random() ",
                    " Math.max(d1, d3) ", " Math.min(d2, d3) ",
                    " Math.toDegrees(d3) ", " Math.toRadians(d3) "
            ));

            Hyperlink alink = new Hyperlink("Learning the Java Language");
            alink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openLink("https://docs.oracle.com/javase/tutorial/java/index.html");
                }
            });
            controller.addNode(alink);

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
    protected void popCodesHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, codesArea, mouseEvent, "JShellHistories");
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
        webViewController.loadContents("");
    }

}
