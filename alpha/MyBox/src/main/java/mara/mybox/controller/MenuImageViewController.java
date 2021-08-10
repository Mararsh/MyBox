package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.stage.Popup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-8-4
 * @License Apache License Version 2.0
 */
public class MenuImageViewController extends MenuImageBaseController {

    protected ImageView imageView;
    protected ImageViewerController imageViewerController;

    public void setParameters(ImageViewerController imageViewerController) {
        try {
            super.setParameters(imageViewerController);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void selectAllAction() {
        imageController.selectAllAction();
    }

    @FXML
    @Override
    public void cropAction() {
        imageController.cropAction();
    }

    @FXML
    public void turnOver() {
        imageController.turnOver();
    }

    @FXML
    public void rotateRight() {
        imageController.rotateRight();
    }

    @FXML
    public void rotateLeft() {
        imageController.rotateLeft();
    }

    @FXML
    @Override
    public void recoverAction() {
        imageController.recoverAction();
    }

    @FXML
    @Override
    public void saveAction() {
        imageController.saveAction();
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        imageController.loadContentInSystemClipboard();
    }

    @FXML
    @Override
    public void previousAction() {
        imageController.previousAction();
    }

    @FXML
    @Override
    public void nextAction() {
        imageController.nextAction();
    }


    /*
        static methods
     */
    public static MenuImageViewController open(ImageViewerController imageViewerController, double x, double y) {
        try {
            try {
                if (imageViewerController == null) {
                    return null;
                }
                Popup popup = PopTools.popWindow(imageViewerController, Fxmls.MenuImageViewFxml, imageViewerController.imageView, x, y);
                if (popup == null) {
                    return null;
                }
                Object object = popup.getUserData();
                if (object == null && !(object instanceof MenuController)) {
                    return null;
                }
                MenuImageViewController controller = (MenuImageViewController) object;
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
