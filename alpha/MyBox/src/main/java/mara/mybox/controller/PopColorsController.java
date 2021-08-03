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
public class PopColorsController extends ColorsManageController {

    public PopColorsController() {
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
    public void setStageStatus(String prefix, int minSize) {
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
    public boolean checkBeforeNextAction() {
        if (parentController != null && (parentController instanceof BaseImageController)) {
            BaseImageController c = (BaseImageController) parentController;
            if (c.pickColorCheck != null && c.pickColorCheck.isSelected()) {
                c.pickColorCheck.setSelected(false);
            }
        }
        return true;
    }

    /*
        static methods
     */
    public static PopColorsController oneOpen(BaseController parent) {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof PopColorsController) {
                ((PopColorsController) object).close();
            }
        }
        PopColorsController controller = (PopColorsController) WindowTools.openChildStage(parent.getMyStage(), Fxmls.PopColorsFxml, false);
        controller.setParameters(parent);
        return controller;
    }

}
