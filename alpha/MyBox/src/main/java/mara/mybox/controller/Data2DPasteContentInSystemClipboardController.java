package mara.mybox.controller;

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
    protected ControlData2DInput inputController;
    @FXML
    protected ControlData2DPaste pasteController;

    public Data2DPasteContentInSystemClipboardController() {
        baseTitle = message("PasteContentInSystemClipboard");
    }

    public void setParameters(ControlData2DLoad target, String text) {
        try {
            this.parentController = target;
            pasteController.setParameters(inputController.sourceController, target);
            inputController.load(text);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
