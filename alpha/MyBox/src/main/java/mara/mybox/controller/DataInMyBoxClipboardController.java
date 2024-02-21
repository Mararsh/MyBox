package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.stage.Window;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-3-11
 * @License Apache License Version 2.0
 */
public class DataInMyBoxClipboardController extends BaseData2DController {

    public DataInMyBoxClipboardController() {
        baseTitle = message("DataInMyBoxClipboard");
        dataType = Data2DDefinition.Type.MyBoxClipboard;
    }

    /*
        static
     */
    public static DataInMyBoxClipboardController oneOpen() {
        DataInMyBoxClipboardController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof DataInMyBoxClipboardController
                    && !(object instanceof Data2DPasteContentInMyBoxClipboardController)) {
                try {
                    controller = (DataInMyBoxClipboardController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (DataInMyBoxClipboardController) WindowTools.openStage(Fxmls.DataInMyBoxClipboardFxml);
        }
        controller.requestMouse();
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
                if (object instanceof DataInMyBoxClipboardController) {
                    ((DataInMyBoxClipboardController) object).refreshAction();
                }
                if (object instanceof Data2DPasteContentInMyBoxClipboardController) {
                    ((Data2DPasteContentInMyBoxClipboardController) object).refreshAction();
                }
            }
        });
    }

    public static DataInMyBoxClipboardController open(Data2DDefinition clip) {
        DataInMyBoxClipboardController c = oneOpen();
        c.loadDef(clip);
        return c;
    }

    public static DataInMyBoxClipboardController loadCSV(DataFileCSV csvData) {
        DataInMyBoxClipboardController c = oneOpen();
        c.loadCSVData(csvData);
        return c;
    }

    public static DataInMyBoxClipboardController loadTable(DataTable dataTable) {
        DataInMyBoxClipboardController c = oneOpen();
        c.loadTableData(dataTable);
        return c;
    }

}
