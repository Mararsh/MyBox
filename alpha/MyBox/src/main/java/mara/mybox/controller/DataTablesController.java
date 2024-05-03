package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.stage.Window;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.TmpTable;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-12-16
 * @License Apache License Version 2.0
 */
public class DataTablesController extends BaseData2DListController {

    public DataTablesController() {
        baseTitle = message("DatabaseTable");
        TipsLabelKey = "DataTableTips";
    }

    @Override
    public void setConditions() {
        try {
            queryConditions = " data_type  = " + Data2D.type(Data2DDefinition.DataType.DatabaseTable)
                    + " AND NOT( sheet like '" + TmpTable.TmpTablePrefix + "%' "
                    + " OR sheet like '" + TmpTable.TmpTablePrefix.toLowerCase() + "%' )";

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void createAction() {
        Data2DManufactureController controller = Data2DManufactureController.open();
        controller.createData(Data2DDefinition.DataType.DatabaseTable);
    }

    /*
        static
     */
    public static DataTablesController oneOpen() {
        DataTablesController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof DataTablesController) {
                try {
                    controller = (DataTablesController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (DataTablesController) WindowTools.openStage(Fxmls.DataTablesFxml);
        }
        controller.requestMouse();
        return controller;
    }

    public static DataTablesController open(Data2DDefinition def) {
        DataTablesController controller = oneOpen();
        controller.loadDef(def);
        return controller;
    }

    public static void updateList() {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof DataTablesController) {
                try {
                    DataTablesController controller = (DataTablesController) object;
                    controller.refreshAction();
                    break;
                } catch (Exception e) {
                }
            }
        }
    }

}
