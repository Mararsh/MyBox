package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.UserConfig;

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

            if (selectAreaCheck != null && cropButton != null) {
                selectAreaCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        cropButton.setDisable(!newValue);
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void updateImage() {
        super.updateImage();
        boolean selected = UserConfig.getBoolean(baseName + "SelectArea", false);
        if (cropButton != null) {
            cropButton.setDisable(!selected);
        }
        if (selectAllButton != null) {
            selectAllButton.setDisable(!selected);
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
    public static MenuImageViewController open(ImageViewerController imageViewerController, double x, double y) {
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
