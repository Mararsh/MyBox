package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.TableNodeRowExpression;
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
public class DataSelectRowExpressionController extends BaseDataSelectController {

    protected ControlDataRowExpression expController;

    public void setParameters(ControlDataRowExpression parent) {
        try {
            if (parent == null) {
                close();
                return;
            }
            expController = parent;

            initDataTree(new TableNodeRowExpression(), null);

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
                expController.edit(savedNode.getStringValue("script"));
                close();
            }
        };
        start(task);
    }

    /*
        static methods
     */
    public static DataSelectRowExpressionController open(ControlDataRowExpression parent) {
        DataSelectRowExpressionController controller = (DataSelectRowExpressionController) WindowTools.
                childStage(parent, Fxmls.DataSelectRowExpressionFxml);
        controller.setParameters(parent);
        controller.requestMouse();
        return controller;
    }

}
