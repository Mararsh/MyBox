package mara.mybox.controller;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import javafx.fxml.FXML;
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

    public void setParameters(BaseDataTreeController parent, DataNode node) {
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
        List<Long> sourceIDs = sourceController.selectedIDs();
        if (sourceIDs == null || sourceIDs.isEmpty()) {
            popError(message("SelectSourceNodes"));
            return;
        }
        DataNode targetNode = targetController.selectedNode();
        if (targetNode == null) {
            popError(message("SelectNodeCopyInto"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private int count;

            @Override
            protected boolean handle() {
                count = 0;
                try (Connection conn = DerbyBase.getConnection()) {
                    if (!targetController.equalOrDescendant(this, conn, targetNode, sourceIDs)) {
                        error = message("TreeTargetComments");
                        return false;
                    }
                    long targetid = targetNode.getNodeid();
                    for (long sourceID : sourceIDs) {
                        DataNode nodeValues = nodeTable.query(conn, sourceID);
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
                if (dataRunning()) {
                    dataController.loadTree(targetNode);
                    dataController.popInformation(message("Moved") + ": " + count);
                }
                if (closeAfterCheck.isSelected()) {
                    close();
                } else {
                    sourceController.loadTree(targetNode);
                    targetController.loadTree(targetNode);
                }
            }
        };
        start(task);
    }

    /*
        static methods
     */
    public static DataTreeMoveController open(BaseDataTreeController parent, DataNode node) {
        DataTreeMoveController controller
                = (DataTreeMoveController) WindowTools.childStage(parent, Fxmls.DataTreeMoveFxml);
        controller.setParameters(parent, node);
        controller.requestMouse();
        return controller;
    }

}
