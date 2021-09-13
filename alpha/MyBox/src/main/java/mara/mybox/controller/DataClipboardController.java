package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-3-11
 * @License Apache License Version 2.0
 */
public class DataClipboardController extends BaseDataTableController<DataDefinition> {

    @FXML
    protected ControlDataClipboard clipboardController;

    public DataClipboardController() {
        baseTitle = message("DataClipboard");
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            return clipboardController.keyEventsFilter(event);
        }
        return true;
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

    public static DataClipboardController open(String[][] data, List<ColumnDefinition> columns) {
        DataClipboardController controller = oneOpen();
        controller.clipboardController.load(data, columns);
        return controller;
    }

}
