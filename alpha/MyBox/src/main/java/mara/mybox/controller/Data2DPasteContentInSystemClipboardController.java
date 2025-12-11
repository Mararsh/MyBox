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
public class Data2DPasteContentInSystemClipboardController extends BaseData2DPasteController {

    @FXML
    protected ControlData2DSystemClipboard boardController;

    public Data2DPasteContentInSystemClipboardController() {
        baseTitle = message("PasteContentInSystemClipboard");
    }

    public void setParameters(Data2DManufactureController target, String text) {
        try {
            this.parentController = target;

            setParameters(target);

            boardController.loadNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    loadDef(boardController.textData);
                }
            });
            boardController.load(text);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean handleKeyEvent(KeyEvent event) {
        if (boardController.handleKeyEvent(event)) {
            return true;
        }
        return super.handleKeyEvent(event);
    }

    /*
        static
     */
    public static Data2DPasteContentInSystemClipboardController open(Data2DManufactureController parent, String text) {
        try {
            Data2DPasteContentInSystemClipboardController controller = (Data2DPasteContentInSystemClipboardController) WindowTools.referredTopStage(
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
