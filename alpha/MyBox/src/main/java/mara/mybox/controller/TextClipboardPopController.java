package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
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
    protected HBox buttonsBox;

    public TextClipboardPopController() {
        baseTitle = Languages.message("MyBoxClipboard");
    }

    @Override
    public void textChanged(String nv) {
        super.textChanged(nv);
        pasteButton.setDisable(!inputEditable || copyToSystemClipboardButton.isDisable());
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
            pasteButton.setDisable(true);
            if (inputEditable) {
                NodeStyleTools.setTooltip(tipsView, new Tooltip(message("TextClipboardUseComments")
                        + "\n\n" + message("TextInMyBoxClipboardTips")));
            } else {
                buttonsBox.getChildren().remove(pasteButton);
                NodeStyleTools.setTooltip(tipsView, new Tooltip(message("TextInMyBoxClipboardTips")));
            }

            baseStyle = thisPane.getStyle();
            if (baseStyle == null) {
                baseStyle = "";
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            PopTools.setWindowStyle(thisPane, baseName, baseStyle);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void itemDoubleClicked() {
        pasteAction();
    }

    @FXML
    @Override
    public void pasteAction() {
        if (textInput == null || !inputEditable) {
            inputEditable = false;
            buttonsBox.getChildren().remove(pasteButton);
            return;
        }
        String s = textArea.getSelectedText();
        if (s == null || s.isEmpty()) {
            s = textArea.getText();
        }
        if (s == null || s.isEmpty()) {
            popError(Languages.message("SelectToHandle"));
            return;
        }
        textInput.insertText(textInput.getAnchor(), s);
    }

    @FXML
    protected void showStyles(Event event) {
        PopTools.popWindowStyles(this, baseStyle, event);
    }

    @FXML
    protected void popStyles(Event event) {
        if (UserConfig.getBoolean("WindowStylesPopWhenMouseHovering", false)) {
            showStyles(event);
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
            MyBoxLog.error(e);
        }
    }

    public static TextClipboardPopController open(BaseController parent, Node node, double x, double y) {
        try {
            if (parent == null || node == null) {
                return null;
            }
            closeAll();
            TextClipboardPopController controller
                    = (TextClipboardPopController) WindowTools.branchStage(parent, Fxmls.TextClipboardPopFxml);
            controller.setParameters(parent, node, x, y);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static TextClipboardPopController open(BaseController parent, Node node) {
        if (parent == null || node == null) {
            return null;
        }
        Bounds bounds = node.localToScreen(node.getBoundsInLocal());
        return open(parent, node, bounds.getMinX() + 80, bounds.getMinY() + 80);
    }

}
