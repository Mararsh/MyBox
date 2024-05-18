package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-13
 * @License Apache License Version 2.0
 */
public class Data2DPasteContentInMyBoxClipboardController extends BaseData2DPasteController {

    public Data2DPasteContentInMyBoxClipboardController() {
        baseTitle = message("PasteContentInMyBoxClipboard");
    }

    /*
        static methods
     */
    public static Data2DPasteContentInMyBoxClipboardController open(Data2DManufactureController target) {
        try {
            if (target == null) {
                return null;
            }
            Data2DPasteContentInMyBoxClipboardController controller
                    = (Data2DPasteContentInMyBoxClipboardController) WindowTools.branchStage(
                            target, Fxmls.Data2DPasteContentInMyBoxClipboardFxml);
            controller.setParameters(target);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
