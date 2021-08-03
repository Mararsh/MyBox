package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-7-3
 * @License Apache License Version 2.0
 */
public class TextInMyBoxClipboardController extends BaseController {

    @FXML
    protected ControlTextClipboard clipboardController;

    public TextInMyBoxClipboardController() {
        baseTitle = Languages.message("TextInMyBoxClipboard");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            clipboardController.setParameters(null);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            return clipboardController.keyEventsFilter(event);
        } else {
            return true;
        }
    }

    /*
        static methods
     */
    public static TextInMyBoxClipboardController oneOpen() {
        TextInMyBoxClipboardController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object == null) {
                continue;
            }
            if (object instanceof TextInMyBoxClipboardController) {
                controller = (TextInMyBoxClipboardController) object;
                controller.toFront();
                break;
            }
        }
        if (controller == null) {
            controller = (TextInMyBoxClipboardController) WindowTools.openStage(Fxmls.TextInMyBoxClipboardFxml);
        }
        return controller;
    }

}
