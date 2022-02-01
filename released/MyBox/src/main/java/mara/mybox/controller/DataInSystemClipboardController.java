package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-12-25
 * @License Apache License Version 2.0
 */
public class DataInSystemClipboardController extends BaseController {

    @FXML
    protected ControlData2DInput inputController;

    public DataInSystemClipboardController() {
        baseTitle = message("DataInSystemClipboard");
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        inputController.loadContentInSystemClipboard();
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            return inputController.keyEventsFilter(event);
        }
        return true;
    }

    /*
        static
     */
    public static DataInSystemClipboardController oneOpen() {
        DataInSystemClipboardController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof DataInSystemClipboardController) {
                try {
                    controller = (DataInSystemClipboardController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (DataInSystemClipboardController) WindowTools.openStage(Fxmls.DataInSystemClipboardFxml);
        }
        controller.requestMouse();
        return controller;
    }

}
