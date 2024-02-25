package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.stage.Window;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-3-11
 * @License Apache License Version 2.0
 */
public class DataInMyBoxClipboardController extends BaseData2DListController {

    public DataInMyBoxClipboardController() {
        baseTitle = message("DataInMyBoxClipboard");
    }

    @Override
    public void setConditions() {
        queryConditions = " data_type = " + Data2D.type(Data2DDefinition.DataType.MyBoxClipboard);
    }

    @Override
    protected int deleteData(FxTask currentTask, List<Data2DDefinition> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        List<Data2DDefinition> handled = new ArrayList<>();
        for (Data2DDefinition d : data) {
            if (currentTask == null || !currentTask.isWorking()) {
                break;
            }
            FileDeleteTools.delete(null, d.getFile());
            handled.add(d);
        }
        if (handled.isEmpty()) {
            return 0;
        } else {
            return tableData2DDefinition.deleteData(handled);
        }
    }

    @Override
    protected void afterDeletion() {
        refreshAction();
        File file = viewController.data2D.getFile();
        if (file != null && !file.exists()) {
            viewController.loadNull();
        }
    }

    @Override
    protected void afterClear() {
        super.afterClear();
        FileDeleteTools.clearDir(null, new File(AppPaths.getDataClipboardPath()));
        viewController.loadNull();
    }

    @FXML
    public void openAction() {
        try {
            browseURI(new File(AppPaths.getDataClipboardPath() + File.separator).toURI());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
        c.viewController.loadDef(clip);
        return c;
    }

    public static DataInMyBoxClipboardController loadCSV(DataFileCSV csvData) {
        DataInMyBoxClipboardController c = oneOpen();
        c.viewController.loadCSVData(csvData);
        return c;
    }

    public static DataInMyBoxClipboardController loadTable(DataTable dataTable) {
        DataInMyBoxClipboardController c = oneOpen();
        c.viewController.loadTableData(dataTable);
        return c;
    }

}
