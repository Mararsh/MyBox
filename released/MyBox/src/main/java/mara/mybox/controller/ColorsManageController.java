package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-1-7
 * @License Apache License Version 2.0
 */
public class ColorsManageController extends BaseController {

    @FXML
    protected ControlColors colorsController;

    public ColorsManageController() {
        baseTitle = Languages.message("ManageColors");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            colorsController.setParameters(this);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            return colorsController.keyEventsFilter(event);
        } else {
            return true;
        }
    }

    /*
        static methods
     */
    public static ColorsManageController oneOpen() {
        ColorsManageController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof ColorsManageController) {
                try {
                    controller = (ColorsManageController) object;
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (ColorsManageController) WindowTools.openStage(Fxmls.ColorsManageFxml);
        }
        return controller;
    }

    public static ColorsManageController addColors(List<Color> colors) {
        if (colors == null || colors.isEmpty()) {
            return null;
        }
        ColorsManageController manageController = oneOpen();
        if (manageController == null) {
            return null;
        }
        ColorCopyController addController = (ColorCopyController) WindowTools.openStage(Fxmls.ColorCopyFxml);
        addController.setValues(manageController, colors);
        addController.toFront();
        return manageController;
    }

    public static ColorsManageController addColor(Color color) {
        if (color == null) {
            return null;
        }
        List<Color> colors = new ArrayList<>();
        colors.add(color);
        return addColors(colors);
    }

}
