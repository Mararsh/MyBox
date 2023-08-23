package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
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
public class Data2DPasteContentInMyBoxClipboardController extends DataInMyBoxClipboardController {

    @FXML
    protected ComboBox<String> rowSelector, colSelector;
    @FXML
    protected RadioButton replaceRadio, insertRadio, appendRadio;
    @FXML
    protected BaseData2DSourceController sourceController;
    @FXML
    protected ControlData2DPaste pasteController;

    public Data2DPasteContentInMyBoxClipboardController() {
        baseTitle = message("PasteContentInMyBoxClipboard");
    }

    @Override
    public void setStageStatus() {
        setAsPop(baseName);
    }

    @Override
    public void initValues() {
        try {
            loadController = sourceController;

            super.initValues();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(ControlData2DEditTable target) {
        try {
            sourceController.setParameters(this);
            pasteController.setParameters(sourceController, target);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean keyESC() {
        close();
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
                if (object != null && object instanceof Data2DPasteContentInMyBoxClipboardController) {
                    ((Data2DPasteContentInMyBoxClipboardController) object).close();
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static Data2DPasteContentInMyBoxClipboardController open(ControlData2DEditTable target) {
        try {
            if (target == null) {
                return null;
            }
            closeAll();
            Data2DPasteContentInMyBoxClipboardController controller
                    = (Data2DPasteContentInMyBoxClipboardController) WindowTools.openChildStage(target.getMyStage(),
                            Fxmls.Data2DPasteContentInMyBoxClipboardFxml, false);
            controller.setParameters(target);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
