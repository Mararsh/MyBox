package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
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
    protected ControlData2DSystemClipboard boardController;
    @FXML
    protected ControlData2DView resultsController;

    public DataInSystemClipboardController() {
        baseTitle = message("DataInSystemClipboard");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            boardController.loadNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    resultsController.loadDef(boardController.textData);
                }
            });
            boardController.loadContentInSystemClipboard();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean handleKeyEvent(KeyEvent event) {
        if (super.handleKeyEvent(event)) {
            return true;
        }
        return boardController.handleKeyEvent(event);
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
