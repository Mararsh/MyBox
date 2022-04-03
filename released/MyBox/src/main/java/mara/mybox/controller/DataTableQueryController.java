package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.TreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-1
 * @License Apache License Version 2.0
 */
public class DataTableQueryController extends TreeManageController {

    @FXML
    protected DataTableQueryEditor nodeController;

    public DataTableQueryController() {
        baseTitle = message("DataQuery");
        category = TreeNode.SQL;
        nameMsg = message("Title");
        valueMsg = "SQL";
    }

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            nodeController.setParameters(tableController);
            getMyStage().setTitle(tableController.getTitle());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void itemClicked() {
    }

    @Override
    public void itemDoubleClicked() {
        editAction();
    }


    /*
        static
     */
    public static DataTableQueryController open(ControlData2DEditTable tableController) {
        try {
            DataTableQueryController controller = (DataTableQueryController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.DataTableQueryFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
