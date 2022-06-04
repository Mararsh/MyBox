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
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mara.mybox.data.FindReplaceString;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.db.table.TableTreeNode;
import mara.mybox.db.table.TableTreeNodeTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-6-4
 * @License Apache License Version 2.0
 */
public class ControlData2DRowExpression extends TreeNodesController {

    protected ControlData2DSource sourceController;
    protected WebEngine webEngine;
    protected String scriptResult;
    protected FindReplaceString findReplace;

    @FXML
    protected TextArea scriptInput;

    public ControlData2DRowExpression() {
        baseTitle = "JavaScript";
        category = TreeNode.JavaScript;
        TipsLabelKey = "RowExpressionTips";
    }

    public void setParamters(ControlData2DSource sourceController) {
        this.sourceController = sourceController;
        tableTreeNode = new TableTreeNode();
        tableTreeNodeTag = new TableTreeNodeTag();
        webEngine = new WebView().getEngine();
        loadTree(null);
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
    public void editAction() {
        JavaScriptController.open(scriptInput.getText());
    }

    @FXML
    public void clearScript() {
        scriptInput.clear();
    }

    @FXML
    protected void popScriptExamples(MouseEvent mouseEvent) {
        try {
            MenuController controller = MenuController.open(this, scriptInput,
                    mouseEvent.getScreenX(), mouseEvent.getScreenY() + 20);
            controller.setTitleLabel(message("Examples"));

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

            List<String> colnames = sourceController.data2D.columnNames();
            List<String> names = new ArrayList<>();
            names.add(message("TableRowNumber"));
            names.add(message("DataRowNumber"));
            names.addAll(colnames);
            for (int i = 0; i < names.size(); i++) {
                names.set(i, "#{" + names.get(i) + "}");
            }
            PopTools.addButtonsPane(controller, scriptInput, names);

            if (!colnames.isEmpty()) {
                String col1 = colnames.get(0);
                PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                        "'#{" + col1 + "}'.search(/Hello/ig) >= 0",
                        "'#{" + col1 + "}'.length > 0",
                        "'#{" + col1 + "}'.indexOf('Hello') == 3",
                        "'#{" + col1 + "}'.startsWith('Hello')",
                        "'#{" + col1 + "}'.endsWith('Hello')",
                        "var array = [ 'A', 'B', 'C', 'D' ];\n"
                        + "array.includes('#{" + col1 + "}')"
                ));
                PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                        "#{" + message("DataRowNumber") + "} % 2 == 0",
                        "#{" + message("DataRowNumber") + "} >= 9 && #{" + message("DataRowNumber") + "} <= 24",
                        "#{" + col1 + "} == 0",
                        "#{" + col1 + "} >= 5",
                        "#{" + col1 + "} < 0 || #{" + col1 + "} > 100 ",
                        "#{" + col1 + "} != 6"
                ));
            }

            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    " var ", " = ", ";", " += ", " -= ", " *= ", " /= ", " %= ",
                    " + ", " - ", " * ", " / ", " % ", "++ ", "-- ",
                    " , ", "( )", " { } ", "[ ]", "\" \"", "' '", ".", " this"
            ));
            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    " == ", " === ", " != ", " !== ", " true ", " false ", " null ", " undefined ",
                    " >= ", " > ", " <= ", " < ", " && ", " || ", " ! "
            ));
            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    "''.search(//ig) >= 0", "''.length > 0", "''.indexOf('') >= 0",
                    "''.startsWith('')", "''.endsWith('')", "''.replace(//ig,'')"
            ));

            Hyperlink jlink = new Hyperlink("Learn JavaScript ");
            jlink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openLink("https://www.tutorialsteacher.com/javascript");
                }
            });
            controller.addNode(jlink);

            Hyperlink alink = new Hyperlink("JavaScript Tutorial");
            alink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openLink("https://www.w3school.com.cn/js/index.asp");
                }
            });
            controller.addNode(alink);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void popScriptHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, scriptInput, mouseEvent, "JavaScriptHistories", true);
    }

    public void checkScript() {
        String script = scriptInput.getText();
        if (script != null && !script.isBlank()) {
            TableStringValues.add("JavaScriptHistories", script.trim());
        }
        sourceController.data2D.setFilterScript(script);
        sourceController.data2D.setFilterReversed(false);
    }

    public String replaceAll(String string, String find, String replace) {
        if (findReplace == null) {
            findReplace = FindReplaceString.create().setOperation(FindReplaceString.Operation.ReplaceAll)
                    .setIsRegex(false).setCaseInsensitive(false).setMultiline(false);
        }
        findReplace.setInputString(string).setFindString(find).setReplaceString(replace).setAnchor(0).run();
        return findReplace.getOutputString();
    }

    public String makeScript(int tableRowNumber) {
        try {
            String script = scriptInput.getText();
            if (script == null || script.isBlank()) {
                return script;
            }
            int size = sourceController.tableData.size();
            List<Data2DColumn> columns = sourceController.data2D.getColumns();
            if (size == 0 || tableRowNumber < 0 || tableRowNumber >= size
                    || columns == null || columns.isEmpty()) {
                return null;
            }
            List<String> tableRow = sourceController.tableData.get(tableRowNumber);
            List<String> names = sourceController.data2D.columnNames();
            for (int i = 0; i < names.size(); i++) {
                script = replaceAll(script, "#{" + names.get(i) + "}", tableRow.get(i + 1));
            }
            script = replaceAll(script, "#{" + message("DataRowNumber") + "}", tableRow.get(0) + "");
            script = replaceAll(script, "#{" + message("TableRowNumber") + "}", (tableRowNumber + 1) + "");
            return script;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public boolean calculate(int tableRowNumber) {
        return calculate(makeScript(tableRowNumber));
    }

    public boolean calculate(String script) {
        try {
            scriptResult = "";
            if (script == null || script.isBlank()) {
                return true;
            }
            Object o = webEngine.executeScript(script);
            if (o != null) {
                scriptResult = o.toString();
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            scriptResult = e.toString();
            return false;
        }
    }

}
