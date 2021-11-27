package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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

    protected ControlData2D sourceController;
    protected ControlData2D targetController;

    @FXML
    protected Label targetLabel;
    @FXML
    protected Button pasteDataButton;

    public void setParameters(ControlData2D parent) {
        try {
            this.parentController = parent;
            targetController = parent;

            sourceController = clipboardController.dataController;

            updateTitle();
            targetController.statusNotify.addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        updateTitle();
                    });

            clipboardController.dataController.statusNotify.addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        pasteDataButton.setDisable(!sourceController.data2D.hasData()
                                || (sourceController.data2D.isMutiplePages() && sourceController.data2D.isTableChanged()));
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
        if (!clipboardController.dataController.data2D.hasData()) {
            popError(message("NoData"));
            return;
        }
//        if (sourceController.data2D.isMutiplePages() && !sourceController.needSave()) {
//            return;
//        }
//        SheetPasteController controller = (SheetPasteController) openChildStage(Fxmls.SheetPasteFxml, false);
//        controller.setParameters(clipboardController.sheetController, targetController);
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
                if (object != null && object instanceof DataClipboardPopController) {
                    ((DataClipboardPopController) object).close();
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static DataClipboardPopController open(ControlData2D parent) {
        try {
            if (parent == null) {
                return null;
            }
            closeAll();
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
