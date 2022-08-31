package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

    protected ControlData2DEditTable tableController;
    protected ChangeListener<Boolean> tableStatusListener;

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
            this.tableController = tableController;
            nodeController.setParameters(tableController);
            getMyStage().setTitle(tableController.getTitle());

            tableStatusListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    nodeController.setDataTable(tableController.data2D);
                }
            };
            tableController.statusNotify.addListener(tableStatusListener);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void cleanPane() {
        try {
            tableController.statusNotify.removeListener(tableStatusListener);
            tableStatusListener = null;
            tableController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
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
