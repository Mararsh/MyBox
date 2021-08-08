package mara.mybox.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara_
 * @CreateDate 2021-7-4
 * @License Apache License Version 2.0
 */
public class TextClipboardPopController extends TextInMyBoxClipboardController {

    protected Node node;
    protected String baseStyle;
    protected double initX, initY;
    protected String lastSystemClip;
    protected TextInputControl textInput;
    protected boolean inputEditable;

    @FXML
    protected Label titleLabel;
    @FXML
    protected Button useButton;
    @FXML
    protected HBox buttonsBox;

    public TextClipboardPopController() {
        baseTitle = Languages.message("MyBoxClipboard");
    }

    @Override
    public void setStageStatus(String prefix, int minSize) {
        setAsPopup(baseName);
    }

    @Override
    public void textChanged(String nv) {
        super.textChanged(nv);
        useButton.setDisable(!inputEditable || copyToSystemClipboardButton.isDisable());
    }

    public void setParameters(BaseController parent, Node node, double x, double y) {
        try {
            this.parentController = parent;
            initX = x;
            initY = y;
            this.node = node;
            if (node != null) {
                if (node instanceof TextInputControl) {
                    textInput = (TextInputControl) node;
                } else if (node instanceof ComboBox) {
                    ComboBox cb = (ComboBox) node;
                    if (cb.isEditable()) {
                        textInput = cb.getEditor();
                    }
                }
                titleLabel.setText(Languages.message("Target") + ": " + node.getId());
            }
            inputEditable = textInput != null && !textInput.isDisable() && textInput.isEditable();
            useButton.setDisable(true);
            if (inputEditable) {
                NodeStyleTools.setTooltip(tipsView, new Tooltip(message("TextClipboardUseComments")
                        + "\n\n" + message("TextInMyBoxClipboardTips")));
            } else {
                buttonsBox.getChildren().remove(useButton);
                NodeStyleTools.setTooltip(tipsView, new Tooltip(message("TextInMyBoxClipboardTips")));
            }

            baseStyle = thisPane.getStyle();
            if (baseStyle == null) {
                baseStyle = "";
            }
            String style = UserConfig.getString(baseName + "WindowStyle", "");
            setLabelsStyle(baseStyle + style);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void itemDoubleClicked() {
        useAction();
    }

    @FXML
    public void useAction() {
        if (textInput == null || !inputEditable) {
            inputEditable = false;
            buttonsBox.getChildren().remove(useButton);
            return;
        }
        String s = textArea.getSelectedText();
        if (s == null || s.isEmpty()) {
            s = textArea.getText();
        }
        if (s == null || s.isEmpty()) {
            popError(Languages.message("CopyNone"));
            return;
        }
        textInput.insertText(textInput.getAnchor(), s);
    }

    @FXML
    public void popStyles(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;
            Map<String, String> styles = new LinkedHashMap<>();
            styles.put("Default", "");
            styles.put("Transparent", "; -fx-text-fill: black; -fx-background-color: transparent;");
            styles.put("Console", "; -fx-text-fill: #CCFF99; -fx-background-color: black;");
            styles.put("Blackboard", "; -fx-text-fill: white; -fx-background-color: #336633;");
            styles.put("Ago", "; -fx-text-fill: white; -fx-background-color: darkblue;");
            styles.put("Book", "; -fx-text-fill: black; -fx-background-color: #F6F1EB;");
            for (String name : styles.keySet()) {
                menu = new MenuItem(Languages.message(name));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        String style = styles.get(name);
                        UserConfig.setString(baseName + "WindowStyle", style);
                        setLabelsStyle(baseStyle + style);
                    }
                });
                popMenu.getItems().add(menu);
            }

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateMouse(mouseEvent, popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setLabelsStyle(String style) {
        thisPane.setStyle(style);
        setLabelsStyle(thisPane, style);
    }

    public void setLabelsStyle(Node node, String style) {
        if (node instanceof Label) {
            node.setStyle(style);
        } else if (node instanceof Parent && !(node instanceof TableView)) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                setLabelsStyle(child, style);
            }
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        closeStage();
    }

    @Override
    public boolean keyESC() {
        closeStage();
        return false;
    }

    @Override
    public boolean keyF6() {
        closeStage();
        return false;
    }


    /*
        static methods
     */
    public static void closeAll() {
        try {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                Object object = window.getUserData();
                if (object != null && object instanceof TextClipboardPopController) {
                    ((TextClipboardPopController) object).close();
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static TextClipboardPopController open(BaseController parent, Node node, double x, double y) {
        try {
            if (parent == null || node == null) {
                return null;
            }
            closeAll();
            TextClipboardPopController controller
                    = (TextClipboardPopController) WindowTools.openChildStage(parent.getMyStage(), Fxmls.TextClipboardPopFxml, false);
            controller.setParameters(parent, node, x, y);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static TextClipboardPopController open(BaseController parent, Node node) {
        if (parent == null || node == null) {
            return null;
        }
        Bounds bounds = node.localToScreen(node.getBoundsInLocal());
        return open(parent, node, bounds.getMinX() + 40, bounds.getMinY() + 40);
    }

}
