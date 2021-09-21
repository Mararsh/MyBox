package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-7-22
 * @License Apache License Version 2.0
 */
public class ColorsPopController extends ColorsManageController {

    public ColorsPopController() {
        baseTitle = Languages.message("PickingColorsNow");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            colorsController.paletteTabPane.getSelectionModel().select(colorsController.colorsTab);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setStageStatus() {
        setAsPopup(baseName);
    }

    public void setParameters(BaseController parent) {
        try {
            this.parentController = parent;

            popInformation(Languages.message("PickingColorsNow"));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void cleanPane() {
        try {
            if (parentController != null && (parentController instanceof BaseImageController)) {
                BaseImageController c = (BaseImageController) parentController;
                if (c.pickColorCheck != null) {
                    c.pickColorCheck.setSelected(false);
                }
                c.isPickingColor = false;
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static methods
     */
    public static ColorsPopController oneOpen(BaseController parent) {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof ColorsPopController) {
                ((ColorsPopController) object).close();
            }
        }
        ColorsPopController controller = (ColorsPopController) WindowTools.openChildStage(parent.getMyStage(), Fxmls.ColorsPopFxml, false);
        controller.setParameters(parent);
        return controller;
    }

}
