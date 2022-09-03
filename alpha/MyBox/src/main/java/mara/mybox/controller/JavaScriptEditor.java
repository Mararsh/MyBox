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
 * @CreateDate 2022-3-20
 * @License Apache License Version 2.0
 */
public class JavaScriptEditor extends TreeNodeEditor {

    protected JavaScriptController jsController;

    @FXML
    protected Button clearCodesButton;

    public JavaScriptEditor() {
        defaultExt = "js";
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

    protected void setParameters(JavaScriptController jsController) {
        this.jsController = jsController;
    }

    @FXML
    @Override
    public void startAction() {
        jsController.runScirpt(getScript());
    }

    public String getScript() {
        return valueInput.getText();
    }

    @FXML
    protected void popExamplesMenu(MouseEvent event) {
        if (UserConfig.getBoolean("JavaScriptExamplesPopWhenMouseHovering", true)) {
            examplesMenu(event);
        }
    }

    @FXML
    protected void showExamplesMenu(ActionEvent event) {
        examplesMenu(event);
    }

    protected void examplesMenu(Event event) {
        try {
            MenuController controller = MenuController.open(this, valueInput, event);

            controller.setTitleLabel(message("Examples"));

            List<Node> topButtons = new ArrayList<>();
            Button newLineButton = new Button();
            newLineButton.setGraphic(StyleTools.getIconImage("iconTurnOver.png"));
            NodeStyleTools.setTooltip(newLineButton, new Tooltip(message("Newline")));
            newLineButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    valueInput.replaceText(valueInput.getSelection(), "\n");
                    valueInput.requestFocus();
                }
            });
            topButtons.add(newLineButton);

            Button clearInputButton = new Button();
            clearInputButton.setGraphic(StyleTools.getIconImage("iconClear.png"));
            NodeStyleTools.setTooltip(clearInputButton, new Tooltip(message("ClearInputArea")));
            clearInputButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    valueInput.clear();
                }
            });
            topButtons.add(clearInputButton);

            CheckBox popCheck = new CheckBox();
            popCheck.setGraphic(StyleTools.getIconImage("iconPop.png"));
            NodeStyleTools.setTooltip(popCheck, new Tooltip(message("PopWhenMouseHovering")));
            popCheck.setSelected(UserConfig.getBoolean("JavaScriptExamplesPopWhenMouseHovering", true));
            popCheck.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("JavaScriptExamplesPopWhenMouseHovering", popCheck.isSelected());
                }
            });
            topButtons.add(popCheck);

            controller.addFlowPane(topButtons);
            controller.addNode(new Separator());

            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    " var ", " = ", ";", " += ", " -= ", " *= ", " /= ", " %= ",
                    " + ", " - ", " * ", " / ", " % ", "++ ", "-- ",
                    " , ", "( )", " { } ", "[ ]", "\" \"", "' '", ".", " this"
            ));
            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    " == ", " === ", " != ", " !== ", " true ", " false ", " null ", " undefined ",
                    " >= ", " > ", " <= ", " < ", " && ", " || ", " ! "
            ));
            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    "'ab'.length", "'hello'.indexOf('el')", "'hello'.startsWith('h')", "'hello'.endsWith('h')",
                    "'hello'.search(/L/i)", "'hello'.replace(/L/i,'ab')", "'hello'.replace(/l/ig,'a')"
            ));
            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    "Math.PI", "Math.E", "Math.abs(-5.47)", "Math.random()",
                    "Math.trunc(3.51)", "Math.round(3.51)", "Math.ceil(3.15)", "Math.floor(3.51)",
                    "Math.pow(3, 4)", "Math.sqrt(9)", "Math.exp(2)", "Math.log(5)", "Math.log2(5)", "Math.log10(5)",
                    "Math.min(1,2,-3)", "Math.max(1,2,-3)", "Math.sin(Math.PI/2)", "Math.cos(0)", "Math.tan(2)"
            ));
            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    "var array = [ 'A', 'B', 'C', 'D' ];\narray[3]",
                    "var object = { name1:'value1', name2:'value2', name3:'value3'}; \nobject.name2"
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
    protected void popHistories(MouseEvent mouseEvent) {
        if (UserConfig.getBoolean("JavaScriptHistoriesPopWhenMouseHovering", true)) {
            PopTools.popStringValues(this, valueInput, mouseEvent, "JavaScriptHistories", false, true);
        }
    }

    @FXML
    protected void showHistories(ActionEvent event) {
        PopTools.popStringValues(this, valueInput, event, "JavaScriptHistories", false, true);
    }

    @Override
    public void cleanPane() {
        try {
            cancelAction();
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
