package mara.mybox.controller;

import javafx.stage.Popup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-8-11
 * @License Apache License Version 2.0
 */
public class MenuImageScopeController extends MenuImageViewController {

    protected ImageManufactureScopeController scopeController;

    public void setParameters(ImageManufactureScopeController scopeController) {
        try {
            this.scopeController = scopeController;
            super.setParameters(scopeController);

            pickColorCheck.setDisable(!scopeController.setBox.isVisible()
                    || !scopeController.tabPane.getTabs().contains(scopeController.colorsTab));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
                Popup popup = PopTools.popWindow(scopeController, Fxmls.MenuImageScopeFxml, scopeController.scopeView, x, y);
                if (popup == null) {
                    return null;
                }
                Object object = popup.getUserData();
                if (object == null && !(object instanceof MenuController)) {
                    return null;
                }
                MenuImageScopeController controller = (MenuImageScopeController) object;
                controller.setParameters(scopeController);
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
