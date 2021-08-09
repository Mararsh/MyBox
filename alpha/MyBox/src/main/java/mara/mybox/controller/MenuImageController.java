package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-8-4
 * @License Apache License Version 2.0
 */
public class MenuImageController extends MenuController {

    protected ImageView imageView;
    protected ImageViewerController imageViewerController;

    public void setParameters(ImageViewerController imageViewerController) {
        try {
            if (imageViewerController == null) {
                return;
            }
            imageView = imageViewerController.imageView;
            if (imageView == null) {
                return;
            }
            parentController = imageViewerController;
            baseName = imageViewerController.baseName;
            setControlsStyle();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        imageViewerController.popFunctionsMenu(mouseEvent);
    }

    /*
        static methods
     */
    public static MenuImageController open(ImageViewerController imageViewerController, double x, double y) {
        try {
            try {
                if (imageViewerController == null) {
                    return null;
                }
                Popup popup = PopTools.popWindow(imageViewerController, Fxmls.MenuFxml, imageViewerController.imageView, x, y);
                if (popup == null) {
                    return null;
                }
                Object object = popup.getUserData();
                if (object == null && !(object instanceof MenuController)) {
                    return null;
                }
                MenuImageController controller = (MenuImageController) object;
                controller.setParameters(imageViewerController);
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
