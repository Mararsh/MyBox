package mara.mybox.controller;

import java.sql.Connection;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-14
 * @License Apache License Version 2.0
 */
public class DataTreeCopyController extends DataTreeNodeSelectController {

    protected DataTreeController treeController;

    @FXML
    protected RadioButton nodeAndDescendantsRadio, descendantsRadio, nodeRadio;

    public void setParameters(DataTreeController parent, DataNode node) {
        try {
            treeController = parent;

            setParameters(parent, treeController.nodeTable, node);

            baseTitle = nodeTable.getTreeName() + " - " + message("SelectNodeCopyInto");
            setTitle(baseTitle);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (!parentRunning() || sourceNode == null) {
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

            private int count;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    if (!checkOptions(this, conn, targetNode)) {
                        return false;
                    }
                    if (nodeAndDescendantsRadio.isSelected()) {
                        count = nodeTable.copyNodeAndDescendants(this, conn, sourceNode, targetNode);
                    } else if (descendantsRadio.isSelected()) {
                        count = nodeTable.copyDescendants(this, conn, sourceNode, targetNode, 0);
                    } else {
                        count = nodeTable.copyNode(conn, sourceNode, targetNode) != null ? 1 : 0;
                    }
                    return count > 0;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (parentRunning()) {
                    treeController.popSuccessful();
                    treeController.refreshNode(targetNode);
                }
                closeStage();
            }
        };
        start(task);

    }

    /*
        static methods
     */
    public static DataTreeCopyController open(DataTreeController parent, DataNode node) {
        DataTreeCopyController controller = (DataTreeCopyController) WindowTools.childStage(
                parent, Fxmls.DataTreeCopyFxml);
        controller.setParameters(parent, node);
        controller.requestMouse();
        return controller;
    }

}
