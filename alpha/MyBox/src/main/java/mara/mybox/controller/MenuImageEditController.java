package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-8-11
 * @License Apache License Version 2.0
 */
public class MenuImageEditController extends MenuImageViewController {

    protected ImageEditorController editor;

    public void setParameters(ImageEditorController controller, double x, double y) {
        try {
            editor = controller;
            super.setParameters(controller, x, y);

            undoButton.disableProperty().bind(editor.undoButton.disableProperty());
            redoButton.disableProperty().bind(editor.redoButton.disableProperty());
            recoverButton.disableProperty().bind(editor.recoverButton.disableProperty());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void undoAction() {
        editor.undoAction();
    }

    @FXML
    @Override
    public void redoAction() {
        editor.redoAction();
    }

    @FXML
    public void showHistories() {
        editor.showHistories();
    }


    /*
        static methods
     */
    public static MenuImageEditController editMenu(ImageEditorController editor, double x, double y) {
        try {
            try {
                if (editor == null) {
                    return null;
                }
                List<Window> windows = new ArrayList<>();
                windows.addAll(Window.getWindows());
                for (Window window : windows) {
                    Object object = window.getUserData();
                    if (object != null && object instanceof MenuImageEditController) {
                        try {
                            MenuImageEditController controller = (MenuImageEditController) object;
                            if (controller.editor.equals(editor)) {
                                controller.close();
                            }
                        } catch (Exception e) {
                        }
                    }
                }
                MenuImageEditController controller = (MenuImageEditController) WindowTools.openChildStage(
                        editor.getMyWindow(), Fxmls.MenuImageEditFxml, false);
                controller.setParameters(editor, x, y);
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
