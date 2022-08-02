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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.db.table.TableTreeNode;
import mara.mybox.db.table.TableTreeNodeTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ExpressionCalculator;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-6-4
 * @License Apache License Version 2.0
 */
public class ControlData2DRowExpression extends TreeNodesController {

    protected Data2D data2D;
    public ExpressionCalculator calculator;

    @FXML
    protected TextArea scriptInput;
    @FXML
    protected ListView placeholdersList;

    public ControlData2DRowExpression() {
        baseTitle = "JavaScript";
        category = TreeNode.JavaScript;
        TipsLabelKey = "RowExpressionTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initCalculator();

            tableTreeNode = new TableTreeNode();
            tableTreeNodeTag = new TableTreeNodeTag();
            loadTree(null);

            placeholdersList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    if (newV == null || newV.isBlank()) {
                        return;
                    }
                    scriptInput.replaceText(scriptInput.getSelection(), newV);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initCalculator() {
        calculator = new ExpressionCalculator();
    }

    public void setData2D(Data2D data2D) {
        try {
            this.data2D = data2D;
            placeholdersList.getItems().clear();
            if (data2D == null || !data2D.isValid()) {
                return;
            }
            List<String> colnames = data2D.columnNames();
            for (int i = 0; i < colnames.size(); i++) {
                placeholdersList.getItems().add("#{" + colnames.get(i) + "}");
            }
            placeholdersList.getItems().add("#{" + message("TableRowNumber") + "}");
            placeholdersList.getItems().add("#{" + message("DataRowNumber") + "}");
            for (int i = 0; i < colnames.size(); i++) {
                placeholdersList.getItems().add("#{" + colnames.get(i) + "-" + message("Mean") + "}");
            }
            for (int i = 0; i < colnames.size(); i++) {
                placeholdersList.getItems().add("#{" + colnames.get(i) + "-" + message("Median") + "}");
            }
            for (int i = 0; i < colnames.size(); i++) {
                placeholdersList.getItems().add("#{" + colnames.get(i) + "-" + message("Mode") + "}");
            }
            for (int i = 0; i < colnames.size(); i++) {
                placeholdersList.getItems().add("#{" + colnames.get(i) + "-" + message("MinimumQ0") + "}");
            }
            for (int i = 0; i < colnames.size(); i++) {
                placeholdersList.getItems().add("#{" + colnames.get(i) + "-" + message("LowerQuartile") + "}");
            }
            for (int i = 0; i < colnames.size(); i++) {
                placeholdersList.getItems().add("#{" + colnames.get(i) + "-" + message("UpperQuartile") + "}");
            }
            for (int i = 0; i < colnames.size(); i++) {
                placeholdersList.getItems().add("#{" + colnames.get(i) + "-" + message("MaximumQ4") + "}");
            }
            for (int i = 0; i < colnames.size(); i++) {
                placeholdersList.getItems().add("#{" + colnames.get(i) + "-" + message("LowerExtremeOutlierLine") + "}");
            }
            for (int i = 0; i < colnames.size(); i++) {
                placeholdersList.getItems().add("#{" + colnames.get(i) + "-" + message("LowerMildOutlierLine") + "}");
            }
            for (int i = 0; i < colnames.size(); i++) {
                placeholdersList.getItems().add("#{" + colnames.get(i) + "-" + message("UpperMildOutlierLine") + "}");
            }
            for (int i = 0; i < colnames.size(); i++) {
                placeholdersList.getItems().add("#{" + colnames.get(i) + "-" + message("UpperExtremeOutlierLine") + "}");
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        tree
     */
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
        if (item == null) {
            return;
        }
        TreeNode node = item.getValue();
        if (node == null || node.getValue() == null) {
            return;
        }
        scriptInput.replaceText(scriptInput.getSelection(), node.getValue());
    }

    @FXML
    public void editAction() {
        JavaScriptController.open(scriptInput.getText());
    }

    @FXML
    public void dataAction() {
        JavaScriptController.open("");
    }

    /*
        script
     */
    @FXML
    public void clearScript() {
        scriptInput.clear();
    }

    @FXML
    protected void popScriptExamples(MouseEvent mouseEvent) {
        if (UserConfig.getBoolean(interfaceName + "ExamplesPopWhenMouseHovering", true)) {
            scriptExamples(mouseEvent);
        }
    }

    @FXML
    protected void showScriptExamples(ActionEvent event) {
        scriptExamples(event);
    }

    protected void scriptExamples(Event event) {
        try {
            MenuController controller = MenuController.open(this, scriptInput, event);
            controller.setTitleLabel(message("Examples"));

            List<Node> topButtons = new ArrayList<>();
            Button newLineButton = new Button();
            newLineButton.setGraphic(StyleTools.getIconImage("iconTurnOver.png"));
            NodeStyleTools.setTooltip(newLineButton, new Tooltip(message("Newline")));
            newLineButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    scriptInput.replaceText(scriptInput.getSelection(), "\n");
                    scriptInput.requestFocus();
                }
            });
            topButtons.add(newLineButton);

            Button clearInputButton = new Button();
            clearInputButton.setGraphic(StyleTools.getIconImage("iconClear.png"));
            NodeStyleTools.setTooltip(clearInputButton, new Tooltip(message("ClearInputArea")));
            clearInputButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    scriptInput.clear();
                }
            });
            topButtons.add(clearInputButton);

            CheckBox popCheck = new CheckBox();
            popCheck.setGraphic(StyleTools.getIconImage("iconPop.png"));
            NodeStyleTools.setTooltip(popCheck, new Tooltip(message("PopWhenMouseHovering")));
            popCheck.setSelected(UserConfig.getBoolean(interfaceName + "ExamplesPopWhenMouseHovering", true));
            popCheck.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(interfaceName + "ExamplesPopWhenMouseHovering", popCheck.isSelected());
                }
            });
            topButtons.add(popCheck);

            controller.addFlowPane(topButtons);

            scriptExampleButtons(controller);

            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    " '' == ", " == ", " '' != ", " != ",
                    " === ", " !== ", " true ", " false ", " null ", " undefined ",
                    " >= ", " > ", " <= ", " < ", " && ", " || ", " ! "
            ));
            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    "''", " var ", " = ", ";", " += ", " -= ", " *= ", " /= ", " %= ",
                    " + ", " - ", " * ", " / ", " % ", "++ ", "-- ",
                    " , ", "( )", " { } ", "[ ]", "\" \"", ".", " this"
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

    protected void scriptExampleButtons(MenuController controller) {
        try {
            if (data2D == null || !data2D.isValid()) {
                return;
            }
            String col1 = data2D.columnNames().get(0);
            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    "#{" + message("DataRowNumber") + "} % 2 == 0",
                    "#{" + message("DataRowNumber") + "} % 2 == 1",
                    "#{" + message("DataRowNumber") + "} >= 9",
                    "#{" + message("TableRowNumber") + "} % 2 == 0",
                    "#{" + message("TableRowNumber") + "} % 2 == 1",
                    "#{" + message("TableRowNumber") + "} == 1",
                    "#{" + col1 + "} == 0",
                    "Math.abs(#{" + col1 + "}) >= 0",
                    "#{" + col1 + "} < 0 || #{" + col1 + "} > 100 ",
                    "#{" + col1 + "} != 6"
            ));

            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    "'#{" + col1 + "}'.search(/Hello/ig) >= 0",
                    "'#{" + col1 + "}'.length > 0",
                    "'#{" + col1 + "}'.indexOf('Hello') == 3",
                    "'#{" + col1 + "}'.startsWith('Hello')",
                    "'#{" + col1 + "}'.endsWith('Hello')",
                    "var array = [ 'A', 'B', 'C', 'D' ];\n"
                    + "array.includes('#{" + col1 + "}')"
            ));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void popScriptHistories(MouseEvent event) {
        if (UserConfig.getBoolean(interfaceName + "HistoriesPopWhenMouseHovering", true)) {
            PopTools.popStringValues(this, scriptInput, event, interfaceName + "Histories", false, true);
        }
    }

    @FXML
    protected void showScriptHistories(ActionEvent event) {
        PopTools.popStringValues(this, scriptInput, event, interfaceName + "Histories", false, true);
    }

    public boolean checkExpression(boolean allPages) {
        error = null;
        if (data2D == null || !data2D.hasData()) {
            error = message("InvalidData");
            return false;
        }
        String script = scriptInput.getText();
        if (script == null || script.isBlank()) {
            return true;
        }
        if (calculator.validateExpression(data2D, script, allPages)) {
            TableStringValues.add(interfaceName + "Histories", script.trim());
            return true;
        } else {
            error = calculator.getError();
            return false;
        }
    }

}
