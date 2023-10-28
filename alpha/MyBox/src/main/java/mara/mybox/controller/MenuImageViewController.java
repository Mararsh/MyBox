package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-8-4
 * @License Apache License Version 2.0
 */
public class MenuImageViewController extends MenuImageBaseController {

    protected ImageView imageView;
    protected ImageViewerController imageViewerController;

    public void setParameters(ImageViewerController imageViewerController, double x, double y) {
        try {
            this.imageViewerController = imageViewerController;
            super.setParameters(imageViewerController, x, y);

        } catch (Exception e) {
            MyBoxLog.error(e);
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
    public void renameAction() {
        imageViewerController.renameAction();
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

    @Override
    public void cleanPane() {
        try {
            imageViewerController = null;
            imageView = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }


    /*
        static methods
     */
    public static MenuImageViewController imageViewMenu(ImageViewerController imageViewerController, double x, double y) {
        try {
            try {
                if (imageViewerController == null) {
                    return null;
                }
                List<Window> windows = new ArrayList<>();
                windows.addAll(Window.getWindows());
                for (Window window : windows) {
                    Object object = window.getUserData();
                    if (object != null && object instanceof MenuImageViewController) {
                        try {
                            MenuImageViewController controller = (MenuImageViewController) object;
                            if (controller.imageViewerController.equals(imageViewerController)) {
                                controller.close();
                            }
                        } catch (Exception e) {
                        }
                    }
                }
                MenuImageViewController controller = (MenuImageViewController) WindowTools.openChildStage(
                        imageViewerController.getMyWindow(), Fxmls.MenuImageViewFxml, false);
                controller.setParameters(imageViewerController, x, y);
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
