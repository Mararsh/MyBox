package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara_
 * @CreateDate 2021-7-4
 * @License Apache License Version 2.0
 */
public class TextClipboardPopController extends MenuTextBaseController {

    protected String lastSystemClip;

    @FXML
    protected ControlTextClipboard clipboardController;

    public TextClipboardPopController() {
        baseTitle = Languages.message("MyBoxClipboard");
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            return clipboardController.keyEventsFilter(event);
        } else {
            return true;
        }
    }

    @Override
    public void setParameters(BaseController parent, Node node, double x, double y) {
        try {
            super.setParameters(parent, node, x, y);
            if (textInput != null) {
                if (!textInput.isDisable() && textInput.isEditable()) {
                    bottomLabel.setText("");
                }
                clipboardController.setParameters(textInput);
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    /*
        static methods
     */
    public static TextClipboardPopController open(BaseController parent, Node node, double x, double y) {
        try {
            if (parent == null || node == null) {
                return null;
            }
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                Object object = window.getUserData();
                if (object != null && object instanceof TextClipboardPopController) {
                    ((TextClipboardPopController) object).close();
                }
            }
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
