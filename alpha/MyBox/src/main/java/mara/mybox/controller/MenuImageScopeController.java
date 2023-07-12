package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-8-11
 * @License Apache License Version 2.0
 */
public class MenuImageScopeController extends MenuImageViewController {

    protected ImageManufactureScopeController scopeController;

    public void setParameters(ImageManufactureScopeController scopeController, double x, double y) {
        try {
            this.scopeController = scopeController;
            super.setParameters(scopeController, x, y);

            pickColorCheck.setDisable(!scopeController.setBox.isVisible()
                    || !scopeController.tabPane.getTabs().contains(scopeController.colorsTab));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static methods
     */
    public static MenuImageScopeController open(ImageManufactureScopeController scopeController, double x, double y) {
        try {
            try {
                if (scopeController == null) {
                    return null;
                }
                List<Window> windows = new ArrayList<>();
                windows.addAll(Window.getWindows());
                for (Window window : windows) {
                    Object object = window.getUserData();
                    if (object != null && object instanceof MenuImageScopeController) {
                        try {
                            MenuImageScopeController controller = (MenuImageScopeController) object;
                            if (controller.scopeController.equals(scopeController)) {
                                controller.close();
                            }
                        } catch (Exception e) {
                        }
                    }
                }
                MenuImageScopeController controller = (MenuImageScopeController) WindowTools.openChildStage(
                        scopeController.getMyWindow(), Fxmls.MenuImageScopeFxml, false);
                controller.setParameters(scopeController, x, y);
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
