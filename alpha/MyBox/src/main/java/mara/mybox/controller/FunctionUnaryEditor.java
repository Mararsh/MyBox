package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-9-8
 * @License Apache License Version 2.0
 */
public class FunctionUnaryEditor extends TreeNodeEditor {

    protected FunctionUnaryController functionController;
    protected String outputs = "";

    @FXML
    protected ControlJavaScriptRefer referController;

    public FunctionUnaryEditor() {
        defaultExt = "txt";
    }

    protected void setParameters(FunctionUnaryController functionController) {
        try {
            this.functionController = functionController;
            referController.placeholdersList.getItems().add("#{x}");
            referController.scriptInput = (TextArea) valueInput;

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    protected void showEditorPane() {
    }

    @FXML
    protected void popScriptExamples(MouseEvent event) {
        if (UserConfig.getBoolean(interfaceName + "ScriptExamplesPopWhenMouseHovering", true)) {
            scriptExamples(event);
        }
    }

    @FXML
    protected void showScriptExamples(ActionEvent event) {
        scriptExamples(event);
    }

    protected void scriptExamples(Event event) {
        try {
            MenuController controller = PopTools.popJavaScriptExamples(this, event, valueInput, interfaceName);

            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    "Math.PI", "Math.E", "Math.random()", "Math.abs(#{x})",
                    "Math.pow(#{x},2)", "Math.pow(#{x},3)", "Math.sqrt(#{x})", "Math.pow(#{x},-3)",
                    "Math.pow(3, #{x})", "Math.exp(#{x})",
                    "Math.log(#{x})", "Math.log2(#{x})", "Math.log10(#{x})",
                    "Math.sin(#{x})", "Math.cos(#{x})", "Math.tan(#{x})"
            ), true, controller.nodesBox.getChildren().size() - 3);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void popScriptHistories(MouseEvent mouseEvent) {
        if (UserConfig.getBoolean(interfaceName + "ScriptHistoriesPopWhenMouseHovering", true)) {
            PopTools.popStringValues(this, valueInput, mouseEvent, interfaceName + "ScriptHistories", false, true);
        }
    }

    @FXML
    protected void showScriptHistories(ActionEvent event) {
        PopTools.popStringValues(this, valueInput, event, interfaceName + "ScriptHistories", false, true);
    }

    @FXML
    public void clearDomain() {
        moreInput.clear();
    }

    @FXML
    protected void popDomainExamples(MouseEvent event) {
        if (UserConfig.getBoolean(interfaceName + "DomainExamplesPopWhenMouseHovering", true)) {
            domainExamples(event);
        }
    }

    @FXML
    protected void showDomainExamples(ActionEvent event) {
        domainExamples(event);
    }

    protected void domainExamples(Event event) {
        try {
            MenuController controller = MenuController.open(this, moreInput, event);
            controller.setTitleLabel(message("Syntax"));

            List<Node> topButtons = new ArrayList<>();
            Button newLineButton = new Button();
            newLineButton.setGraphic(StyleTools.getIconImage("iconTurnOver.png"));
            NodeStyleTools.setTooltip(newLineButton, new Tooltip(message("Newline")));
            newLineButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    moreInput.replaceText(moreInput.getSelection(), "\n");
                    moreInput.requestFocus();
                }
            });
            topButtons.add(newLineButton);

            Button clearInputButton = new Button();
            clearInputButton.setGraphic(StyleTools.getIconImage("iconClear.png"));
            NodeStyleTools.setTooltip(clearInputButton, new Tooltip(message("ClearInputArea")));
            clearInputButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    moreInput.clear();
                }
            });
            topButtons.add(clearInputButton);

            CheckBox popCheck = new CheckBox();
            popCheck.setGraphic(StyleTools.getIconImage("iconPop.png"));
            NodeStyleTools.setTooltip(popCheck, new Tooltip(message("PopWhenMouseHovering")));
            popCheck.setSelected(UserConfig.getBoolean(interfaceName + "DomainExamplesPopWhenMouseHovering", true));
            popCheck.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(interfaceName + "DomainExamplesPopWhenMouseHovering", popCheck.isSelected());
                }
            });
            topButtons.add(popCheck);

            controller.addFlowPane(topButtons);
            controller.addNode(new Separator());

            PopTools.addButtonsPane(controller, moreInput, Arrays.asList(
                    "functionDomain.set(\"Math\", Math.class);\n",
                    "functionDomain.set(\"BigDecimal\", new java.math.BigDecimal(10));\n",
                    "functionDomain.set(\"df\", \"#,###\");\n"
                    + "functionDomain.set(\"DecimalFormat\", new java.text.DecimalFormat(df));\n",
                    "functionDomain.set(\"StringTools\", mara.mybox.tools.StringTools.class);\n",
                    "functionDomain.set(\"DoubleTools\", mara.mybox.tools.DoubleTools.class);\n",
                    "functionDomain.set(\"DateTools\", mara.mybox.tools.DateTools.class);\n",
                    "functionDomain.set(\"x\", 5);\n",
                    "functionDomain.set(\"x\", 5);\n",
                    "functionDomain.set(\"s\", \"hello\");\n"
            ));

            Hyperlink elink = new Hyperlink("JEXL Overview");
            elink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openLink("https://commons.apache.org/proper/commons-function/index.html");
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
    protected void popDomainHistories(MouseEvent mouseEvent) {
        if (UserConfig.getBoolean(interfaceName + "DomainHistoriesPopWhenMouseHovering", true)) {
            PopTools.popStringValues(this, moreInput, mouseEvent, interfaceName + "DomainHistories", false, true);
        }
    }

    @FXML
    protected void showDomainHistories(ActionEvent event) {
        PopTools.popStringValues(this, moreInput, event, interfaceName + "DomainHistories", false, true);
    }

}
