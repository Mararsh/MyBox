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
            this.imageViewerController = imageViewerController;
            super.setParameters(imageViewerController);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void selectAllAction() {
        imageViewerController.selectAllAction();
    }

    @FXML
    @Override
    public void cropAction() {
        imageViewerController.cropAction();
    }

    @FXML
    public void turnOver() {
        imageViewerController.turnOver();
    }

    @FXML
    public void rotateRight() {
        imageViewerController.rotateRight();
    }

    @FXML
    public void rotateLeft() {
        imageViewerController.rotateLeft();
    }

    @FXML
    @Override
    public void recoverAction() {
        imageViewerController.recoverAction();
    }

    @FXML
    @Override
    public void saveAction() {
        imageViewerController.saveAction();
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        imageViewerController.loadContentInSystemClipboard();
    }

    @FXML
    @Override
    public void previousAction() {
        imageViewerController.previousAction();
    }

    @FXML
    @Override
    public void nextAction() {
        imageViewerController.nextAction();
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
