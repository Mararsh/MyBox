package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-13
 * @License Apache License Version 2.0
 */
public class DataClipboardPopController extends DataClipboardController {

    protected ControlSheet targetController;

    @FXML
    protected Label targetLabel;

    public void setParameters(ControlSheet parent) {
        try {
            this.parentController = parent;
            targetController = parent;

            updateTitle();
            targetController.sheetChangedNotify.addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        updateTitle();
                    });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void updateTitle() {
        targetLabel.setText(message("Target") + ": " + targetController.getTitle());
    }

    @FXML
    @Override
    public void pasteAction() {
        DataPasteController controller = (DataPasteController) openChildStage(Fxmls.DataPasteFxml, false);
        controller.setParameters(clipboardController.sheetController, targetController);
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
    public static DataClipboardPopController open(ControlSheet parent) {
        try {
            if (parent == null) {
                return null;
            }
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                Object object = window.getUserData();
                if (object != null && object instanceof DataClipboardPopController) {
                    ((DataClipboardPopController) object).close();
                }
            }
            DataClipboardPopController controller
                    = (DataClipboardPopController) WindowTools.openChildStage(parent.getMyStage(), Fxmls.DataClipboardPopFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
