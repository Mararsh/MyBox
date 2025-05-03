package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.DataNode;
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
public class DataSelectSQLController extends BaseDataSelectController {

    protected Data2DTableQueryController queryController;

    public void setParameters(Data2DTableQueryController parent) {
        try {
            if (parent == null) {
                close();
                return;
            }
            queryController = parent;
            initDataTree(queryController.nodeTable, null);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        DataNode node = treeController.selectedValue();
        if (node == null) {
            popError(message("SelectToHandle"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private DataNode savedNode;

            @Override
            protected boolean handle() {
                savedNode = nodeTable.query(node);
                return savedNode != null;
            }

            @Override
            protected void whenSucceeded() {
                if (WindowTools.isRunning(queryController)) {
                    queryController.load(savedNode.getStringValue("statement"));
                }
                close();
            }
        };
        start(task);
    }

    /*
        static methods
     */
    public static DataSelectSQLController open(Data2DTableQueryController parent) {
        DataSelectSQLController controller
                = (DataSelectSQLController) WindowTools.childStage(parent, Fxmls.DataSelectSQLFxml);
        controller.setParameters(parent);
        controller.requestMouse();
        return controller;
    }

}
