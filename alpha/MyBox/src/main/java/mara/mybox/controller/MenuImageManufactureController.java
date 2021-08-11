package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.stage.Popup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-8-11
 * @License Apache License Version 2.0
 */
public class MenuImageManufactureController extends MenuImageViewController {

    protected ImageManufactureController manufactureController;

    public void setParameters(ImageManufactureController manufactureController) {
        try {
            this.manufactureController = manufactureController;
            super.setParameters(manufactureController);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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
                Popup popup = PopTools.popWindow(manufactureController, Fxmls.MenuImageManufactureFxml, manufactureController.imageView, x, y);
                if (popup == null) {
                    return null;
                }
                Object object = popup.getUserData();
                if (object == null && !(object instanceof MenuController)) {
                    return null;
                }
                MenuImageManufactureController controller = (MenuImageManufactureController) object;
                controller.setParameters(manufactureController);
                return controller;
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
                return null;
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
