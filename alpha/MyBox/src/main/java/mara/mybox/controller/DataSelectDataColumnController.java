package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.TableNodeDataColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2024-12-5
 * @License Apache License Version 2.0
 */
public class DataSelectDataColumnController extends BaseDataSelectController {

    protected ControlData2DColumns columnsController;
    protected Data2DColumnEditController editController;

    @Override
    public void initControls() {
        try {
            super.initControls();

            initDataTree(new TableNodeDataColumn(), null);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(ControlData2DColumns controller) {
        try {
            if (controller == null) {
                close();
                return;
            }
            columnsController = controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setEditor(Data2DColumnEditController controller) {
        try {
            if (controller == null) {
                close();
                return;
            }
            editController = controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        DataNode node = selectedNode();
        if (node == null) {
            popError(message("SelectToHandle"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private Data2DColumn column;

            @Override
            protected boolean handle() {
                column = TableNodeDataColumn.toColumn(nodeTable.query(node));
                return column != null;
            }

            @Override
            protected void whenSucceeded() {
                if (columnsController != null) {
                    columnsController.addColumn(column);

                } else if (editController != null) {
                    editController.load(column);
                }
                close();
            }
        };
        start(task);
    }

    /*
        static methods
     */
    public static DataSelectDataColumnController open(ControlData2DColumns parent) {
        DataSelectDataColumnController controller
                = (DataSelectDataColumnController) WindowTools.childStage(parent, Fxmls.DataSelectDataColumnFxml);
        controller.setParameters(parent);
        controller.requestMouse();
        return controller;
    }

    public static DataSelectDataColumnController edit(Data2DColumnEditController parent) {
        DataSelectDataColumnController controller
                = (DataSelectDataColumnController) WindowTools.childStage(parent, Fxmls.DataSelectDataColumnFxml);
        controller.setEditor(parent);
        controller.requestMouse();
        return controller;
    }

}
