package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-3-11
 * @License Apache License Version 2.0
 */
public class DataClipboardController extends BaseController {

    @FXML
    protected ControlDataClipboard clipboardController;

    public DataClipboardController() {
        baseTitle = message("DataClipboard");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            clipboardController.loadTableData();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void createAction() {
        clipboardController.createAction();
    }

    @FXML
    public void refreshAction() {
        clipboardController.refreshAction();
    }

    public void load(Data2DDefinition clip) {
        clipboardController.dataController.loadDef(clip);
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            return clipboardController.keyEventsFilter(event);
        }
        return true;
    }

    @Override
    public void myBoxClipBoard() {
        clipboardController.myBoxClipBoard();
    }


    /*
        static
     */
    public static DataClipboardController oneOpen() {
        DataClipboardController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof DataClipboardController) {
                try {
                    controller = (DataClipboardController) object;
                    controller.toFront();
                    controller.refreshAction();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (DataClipboardController) WindowTools.openStage(Fxmls.DataClipboardFxml);
        }
        return controller;
    }

    public static void update() {
        Platform.runLater(() -> {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                Object object = window.getUserData();
                if (object == null) {
                    continue;
                }
                if (object instanceof DataClipboardController) {
                    ((DataClipboardController) object).refreshAction();
                }
                if (object instanceof DataClipboardPopController) {
                    ((DataClipboardPopController) object).refreshAction();
                }
            }
        });
    }

    public static DataClipboardController open(Data2DDefinition clip) {
        DataClipboardController c = oneOpen();
        c.load(clip);
        return c;
    }

}
