package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.stage.Window;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-29
 * @License Apache License Version 2.0
 */
public class WebFavoritesController extends InfoTreeManageController {

    @FXML
    protected WebFavoriteEditor editorController;

    public WebFavoritesController() {
        baseTitle = message("WebFavorites");
        category = InfoNode.WebFavorite;
        nameMsg = message("Title");
        valueMsg = message("Address");
    }

    @Override
    public void initControls() {
        try {
            editor = editorController;
            super.initControls();

            goButton.disableProperty().bind(Bindings.isEmpty(editor.attributesController.nameInput.textProperty()));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void goAction() {
        String address = editor.valueInput.getText();
        if (address == null || address.isBlank()) {
            popError(message("InvalidData") + ": " + message("Address"));
            return;
        }
        WebBrowserController.openAddress(address, true);
    }

    /*
        static methods
     */
    public static WebFavoritesController open() {
        WebFavoritesController controller = (WebFavoritesController) WindowTools.openStage(Fxmls.WebFavoritesFxml);
        if (controller != null) {
            controller.requestMouse();
        }
        return controller;
    }

    public static WebFavoritesController oneOpen() {
        WebFavoritesController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof WebFavoritesController) {
                try {
                    controller = (WebFavoritesController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (WebFavoritesController) WindowTools.openStage(Fxmls.WebFavoritesFxml);
        }
        controller.requestMouse();
        return controller;
    }

}
