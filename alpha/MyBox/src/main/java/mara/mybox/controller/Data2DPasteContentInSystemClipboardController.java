package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-11-27
 * @License Apache License Version 2.0
 */
public class Data2DPasteContentInSystemClipboardController extends BaseBranchController {

    @FXML
    protected ControlData2DSystemClipboard boardController;
    @FXML
    protected BaseData2DRowsColumnsController sourceController;
    @FXML
    protected ControlData2DPaste pasteController;

    public Data2DPasteContentInSystemClipboardController() {
        baseTitle = message("PasteContentInSystemClipboard");
    }

    public void setParameters(Data2DManufactureController target, String text) {
        try {
            this.parentController = target;

            sourceController.setParameters(this);

            boardController.loadNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    sourceController.loadDef(boardController.textData);
                }
            });
            boardController.load(text);

            pasteController.setParameters(sourceController, target);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void dataManufacture() {
        boardController.editAction();
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (pasteController.keyEventsFilter(event)) {
            return true;
        }
        if (boardController.keyEventsFilter(event)) {
            return true;
        }
        if (sourceController.keyEventsFilter(event)) {
            return true;
        }
        return super.keyEventsFilter(event);
    }

    /*
        static
     */
    public static Data2DPasteContentInSystemClipboardController open(Data2DManufactureController parent, String text) {
        try {
            Data2DPasteContentInSystemClipboardController controller = (Data2DPasteContentInSystemClipboardController) WindowTools.branchStage(
                    parent, Fxmls.Data2DPasteContentInSystemClipboardFxml);
            controller.setParameters(parent, text);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
