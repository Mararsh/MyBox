package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-11-27
 * @License Apache License Version 2.0
 */
public class Data2DPasteContentInSystemClipboardController extends BaseChildController {

    @FXML
    protected ControlData2DSystemClipboard boardController;
    @FXML
    protected BaseData2DSourceController sourceController;
    @FXML
    protected ControlData2DPaste pasteController;

    public Data2DPasteContentInSystemClipboardController() {
        baseTitle = message("PasteContentInSystemClipboard");
    }

    public void setParameters(ControlData2DLoad target, String text) {
        try {
            this.parentController = target;

            sourceController.setParameters(this);

            boardController.loadNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    sourceController.loadData(boardController.textData);
                }
            });
            boardController.load(text);

            pasteController.setParameters(sourceController, target);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void editAction() {
        boardController.editAction();
    }

    /*
        static
     */
    public static Data2DPasteContentInSystemClipboardController open(ControlData2DLoad parent, String text) {
        try {
            Data2DPasteContentInSystemClipboardController controller = (Data2DPasteContentInSystemClipboardController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.Data2DPasteContentInSystemClipboardFxml, false);
            controller.setParameters(parent, text);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
