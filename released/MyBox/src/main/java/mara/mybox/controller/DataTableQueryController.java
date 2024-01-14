package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-1
 * @License Apache License Version 2.0
 */
public class DataTableQueryController extends InfoTreeManageController {

    protected ControlData2DLoad tableController;
    protected ChangeListener<Boolean> tableStatusListener;

    @FXML
    protected DataTableQueryEditor editorController;

    public DataTableQueryController() {
        baseTitle = message("DataQuery");
        category = InfoNode.SQL;
        nameMsg = message("Title");
        valueMsg = "SQL";
    }

    @Override
    public void initControls() {
        try {
            editor = editorController;
            super.initControls();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(ControlData2DLoad tableController) {
        try {
            this.tableController = tableController;
            editorController.setParameters(tableController);
            getMyStage().setTitle(tableController.getTitle());

            tableStatusListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    editorController.setDataTable(tableController.data2D);
                }
            };
            tableController.statusNotify.addListener(tableStatusListener);

        } catch (Exception e) {
            MyBoxLog.error(e);
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
    public static DataTableQueryController open(ControlData2DLoad tableController) {
        try {
            DataTableQueryController controller = (DataTableQueryController) WindowTools.branchStage(
                    tableController, Fxmls.DataTableQueryFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
