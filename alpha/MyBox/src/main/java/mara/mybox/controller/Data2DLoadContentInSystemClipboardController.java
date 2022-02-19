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
public class Data2DLoadContentInSystemClipboardController extends BaseChildController {

    protected ControlData2DLoad loadController;

    @FXML
    protected ControlData2DInput inputController;

    public Data2DLoadContentInSystemClipboardController() {
        baseTitle = message("LoadContentInSystemClipboard");
    }

    public void setParameters(ControlData2DLoad parent, String text) {
        try {
            loadController = parent;
            inputController.load(text);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (inputController.data == null || inputController.data.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        loadController.loadTmpData(inputController.dataFileText.getColumns(), inputController.data);
        close();
    }

    /*
        static
     */
    public static Data2DLoadContentInSystemClipboardController open(ControlData2DLoad parent, String text) {
        try {
            Data2DLoadContentInSystemClipboardController controller = (Data2DLoadContentInSystemClipboardController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.Data2DLoadContentInSystemClipboardFxml, true);
            controller.setParameters(parent, text);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
