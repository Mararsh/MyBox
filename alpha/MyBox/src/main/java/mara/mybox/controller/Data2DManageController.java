package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.stage.Window;
import mara.mybox.data2d.Data2D;
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
public class Data2DManageController extends BaseData2DListController {

    public Data2DManageController() {
        baseTitle = message("ManageData");
        TipsLabelKey = "DataManageTips";
    }

    @Override
    public void setConditions() {
        try {
            queryConditions = " data_type != " + Data2D.type(Data2DDefinition.DataType.InternalTable);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static
     */
    public static Data2DManageController oneOpen() {
        Data2DManageController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof Data2DManageController) {
                try {
                    controller = (Data2DManageController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (Data2DManageController) WindowTools.openStage(Fxmls.Data2DManageFxml);
        }
        controller.requestMouse();
        return controller;
    }

    public static Data2DManageController open(Data2DDefinition def) {
        Data2DManageController controller = oneOpen();
        controller.loadDef(def);
        return controller;
    }

    public static void updateList() {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof Data2DManageController) {
                try {
                    Data2DManageController controller = (Data2DManageController) object;
                    controller.refreshAction();
                    break;
                } catch (Exception e) {
                }
            }
        }
    }

}
