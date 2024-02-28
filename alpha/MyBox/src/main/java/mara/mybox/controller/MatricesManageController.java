package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.stage.Window;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-12-15
 * @License Apache License Version 2.0
 */
public class MatricesManageController extends BaseData2DListController {

    public MatricesManageController() {
        baseTitle = Languages.message("MatricesManage");
    }

    @Override
    public void setConditions() {
        try {
            queryConditions = " data_type = " + Data2D.type(Data2DDefinition.DataType.Matrix);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
        controller.viewController.loadData(name, cols, data);
        return controller;
    }

    public static MatricesManageController createMatrix(DataFileCSV csvData) {
        MatricesManageController controller = oneOpen();
        controller.viewController.createData(csvData, Data2DDefinition.DataType.Matrix, null, null);
        return controller;
    }

}
