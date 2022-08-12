package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.stage.Window;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-12-15
 * @License Apache License Version 2.0
 */
public class MatricesManageController extends BaseData2DController {

    public MatricesManageController() {
        baseTitle = Languages.message("MatricesManage");
        type = Data2DDefinition.Type.Matrix;
    }


    /*
        static
     */
    public static MatricesManageController oneOpen() {
        MatricesManageController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof MatricesManageController) {
                try {
                    controller = (MatricesManageController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (MatricesManageController) WindowTools.openStage(Fxmls.MatricesManageFxml);
        }
        controller.requestMouse();
        return controller;
    }

    public static MatricesManageController open(Data2DDefinition def) {
        MatricesManageController controller = oneOpen();
        controller.loadDef(def);
        return controller;
    }

    public static MatricesManageController open(String name, List<Data2DColumn> cols, List<List<String>> data) {
        MatricesManageController controller = oneOpen();
        controller.dataController.loadTmpData(name, cols, data);
        return controller;
    }

    public static MatricesManageController loadCSV(DataFileCSV csvData) {
        MatricesManageController controller = oneOpen();
        controller.loadCSVData(csvData);
        return controller;
    }

    public static MatricesManageController loadTable(DataTable dataTable) {
        MatricesManageController controller = oneOpen();
        controller.loadTableData(dataTable);
        controller.requestMouse();
        return controller;
    }

}
