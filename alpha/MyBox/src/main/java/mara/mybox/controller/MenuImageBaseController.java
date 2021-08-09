package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-8-9
 * @License Apache License Version 2.0
 */
public class MenuImageBaseController extends MenuController {

    protected BaseImageController imageController;

    public void setParameters(BaseImageController imageController) {
        try {
            if (imageController == null) {
                return;
            }
            this.imageController = imageController;
            parentController = imageController;
            baseName = imageController.baseName;
            setControlsStyle();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        imageController.popFunctionsMenu(mouseEvent);
    }

    @FXML
    public void zoomOut() {
        imageController.zoomOut();
    }

    @FXML
    public void zoomIn() {
        imageController.zoomIn();
    }

    @FXML
    public void paneSize() {
        imageController.paneSize();
    }

    @FXML
    public void loadedSize() {
        imageController.loadedSize();
    }

    @FXML
    @Override
    public void copyToSystemClipboard() {
        imageController.copyToSystemClipboard();
    }

    @FXML
    @Override
    public void systemClipBoard() {
        imageController.systemClipBoard();
    }

    @FXML
    @Override
    public void myBoxClipBoard() {
        imageController.myBoxClipBoard();
    }

    @FXML
    @Override
    public void popAction() {
        imageController.popAction();
    }

    @FXML
    public void manufactureAction() {
        imageController.manufactureAction();
    }

    @FXML
    @Override
    public void saveAsAction() {
        imageController.saveAsAction();
    }

    @FXML
    @Override
    public void infoAction() {
        imageController.infoAction();
    }

    @FXML
    public void metaAction() {
        imageController.metaAction();
    }

    @FXML
    public void settings() {
        imageController.settings();
    }

    /*
        static methods
     */
    public static MenuImageBaseController open(BaseImageController imageController, double x, double y) {
        try {
            if (imageController == null) {
                return null;
            }
            Popup popup = PopTools.popWindow(imageController, Fxmls.MenuImageBaseFxml, imageController.imageView, x, y);
            if (popup == null) {
                return null;
            }
            Object object = popup.getUserData();
            if (object == null && !(object instanceof MenuController)) {
                return null;
            }
            MenuImageBaseController controller = (MenuImageBaseController) object;
            controller.setParameters(imageController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
