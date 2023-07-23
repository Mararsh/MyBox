package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-8-11
 * @License Apache License Version 2.0
 */
public class MenuImageManufactureController extends MenuImageViewController {

    protected ImageManufactureController manufactureController;

    public void setParameters(ImageManufactureController manufactureController, double x, double y) {
        try {
            this.manufactureController = manufactureController;
            super.setParameters(manufactureController, x, y);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void updateImage() {
        super.updateImage();
        cropButton.setDisable(false);
    }

    @FXML
    @Override
    public void undoAction() {
        manufactureController.undoAction();
    }

    @FXML
    @Override
    public void redoAction() {
        manufactureController.redoAction();
    }


    /*
        static methods
     */
    public static MenuImageManufactureController open(ImageManufactureController manufactureController, double x, double y) {
        try {
            try {
                if (manufactureController == null) {
                    return null;
                }
                List<Window> windows = new ArrayList<>();
                windows.addAll(Window.getWindows());
                for (Window window : windows) {
                    Object object = window.getUserData();
                    if (object != null && object instanceof MenuImageManufactureController) {
                        try {
                            MenuImageManufactureController controller = (MenuImageManufactureController) object;
                            if (controller.manufactureController.equals(manufactureController)) {
                                controller.close();
                            }
                        } catch (Exception e) {
                        }
                    }
                }
                MenuImageManufactureController controller = (MenuImageManufactureController) WindowTools.openChildStage(
                        manufactureController.getMyWindow(), Fxmls.MenuImageManufactureFxml, false);
                controller.setParameters(manufactureController, x, y);
                return controller;
            } catch (Exception e) {
                MyBoxLog.error(e);
                return null;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
