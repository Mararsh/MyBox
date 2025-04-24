package mara.mybox.controller;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
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
public class DataTreeMoveController extends BaseDataTreeHandleController {

    @FXML
    protected ControlDataTreeSource sourceController;
    @FXML
    protected ControlDataTreeTarget targetController;

    public void setParameters(ControlTreeView parent, DataNode node) {
        try {
            super.setParameters(parent);

            sourceController.setParameters(parent, node);
            targetController.setParameters(parent);

            baseTitle = nodeTable.getTreeName() + " - " + message("MoveNodes");
            setTitle(baseTitle);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        List<DataNode> sourceNodes = sourceController.selectedNodes();
        if (sourceNodes == null || sourceNodes.isEmpty()) {
            popError(message("SelectSourceNodes"));
            return;
        }
        TreeItem<DataNode> targetItem = targetController.selectedItem();
        if (targetItem == null) {
            popError(message("SelectNodeCopyInto"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private int count;
            private DataNode targetNode;

            @Override
            protected boolean handle() {
                count = 0;
                targetNode = targetItem.getValue();
                try (Connection conn = DerbyBase.getConnection()) {
                    if (!targetController.equalOrDescendant(this, conn, targetNode, sourceNodes)) {
                        error = message("TreeTargetComments");
                        return false;
                    }
                    long targetid = targetNode.getNodeid();
                    for (DataNode sourceNode : sourceNodes) {
                        DataNode nodeValues = nodeTable.query(conn, sourceNode.getNodeid());
                        nodeValues.setParentid(targetid).setUpdateTime(new Date());
                        if (nodeTable.updateData(conn, nodeValues) == null) {
                            return false;
                        }
                        count++;
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return count >= 0;
            }

            @Override
            protected void whenSucceeded() {
                popInformation(message("Moved") + ": " + count);
                sourceController.loadTree(targetNode);
                targetController.loadTree(targetNode);
                if (treeRunning()) {
                    treeController.loadTree(targetNode);
                    treeController.reloadCurrent();
                }
            }
        };
        start(task);
    }

    /*
        static methods
     */
    public static DataTreeMoveController open(ControlTreeView parent, DataNode node) {
        DataTreeMoveController controller
                = (DataTreeMoveController) WindowTools.childStage(parent, Fxmls.DataTreeMoveFxml);
        controller.setParameters(parent, node);
        controller.requestMouse();
        return controller;
    }

}
