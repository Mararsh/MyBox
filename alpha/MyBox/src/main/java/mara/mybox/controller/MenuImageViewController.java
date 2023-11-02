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
    protected BaseImageController viewerController;

    public void setParameters(BaseImageController controller, double x, double y) {
        try {
            viewerController = controller;
            super.setParameters(viewerController, x, y);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void selectAllAction() {
        viewerController.selectAllAction();
    }

    @FXML
    @Override
    public void cropAction() {
        viewerController.cropAction();
    }

    @FXML
    public void turnOver() {
        viewerController.turnOver();
    }

    @FXML
    public void rotateRight() {
        viewerController.rotateRight();
    }

    @FXML
    public void rotateLeft() {
        viewerController.rotateLeft();
    }

    @FXML
    @Override
    public void recoverAction() {
        viewerController.recoverAction();
    }

    @FXML
    @Override
    public void saveAction() {
        viewerController.saveAction();
    }

    @FXML
    public void renameAction() {
        viewerController.renameAction();
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        viewerController.loadContentInSystemClipboard();
    }

    @FXML
    @Override
    public void previousAction() {
        viewerController.previousAction();
    }

    @FXML
    @Override
    public void nextAction() {
        viewerController.nextAction();
    }

    @Override
    public void cleanPane() {
        try {
            viewerController = null;
            imageView = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }


    /*
        static methods
     */
    public static MenuImageViewController imageViewMenu(BaseImageController controller, double x, double y) {
        try {
            try {
                if (controller == null) {
                    return null;
                }
                List<Window> windows = new ArrayList<>();
                windows.addAll(Window.getWindows());
                for (Window window : windows) {
                    Object object = window.getUserData();
                    if (object != null && object instanceof MenuImageViewController) {
                        try {
                            MenuImageViewController menu = (MenuImageViewController) object;
                            if (menu.viewerController.equals(controller)) {
                                menu.close();
                            }
                        } catch (Exception e) {
                        }
                    }
                }
                MenuImageViewController menu = (MenuImageViewController) WindowTools.openChildStage(controller.getMyWindow(), Fxmls.MenuImageViewFxml, false);
                menu.setParameters(controller, x, y);
                return menu;
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
