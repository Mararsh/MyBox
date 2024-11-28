package mara.mybox.controller;

import java.sql.Connection;
import javafx.fxml.FXML;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-14
 * @License Apache License Version 2.0
 */
public class DataTreeParentController extends DataTreeNodeSelectController {

    @Override
    public void initMore() {
        baseTitle = nodeTable.getTreeName() + " - " + message("SelectParentNode");
        setTitle(baseTitle);
    }

    @FXML
    @Override
    public void okAction() {
        if (!parentRunning()) {
            close();
            return;
        }
        DataNode targetNode = selectedValue();
        if (targetNode == null) {
            popError(message("SelectNodeComments"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    return checkOptions(this, conn, targetNode);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
//                treeController.nodeController.setParentNode(targetNode);
                close();
            }
        };
        start(task);

    }

    /*
        static methods
     */
    public static DataTreeParentController open(BaseController parent, BaseNodeTable table, DataNode node) {
        DataTreeParentController controller = (DataTreeParentController) WindowTools.childStage(
                parent, Fxmls.DataTreeParentFxml);
        controller.setParameters(parent, table, node);
        controller.requestMouse();
        return controller;
    }

}
