package mara.mybox.controller;

import java.sql.Connection;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.db.data.InfoNode;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-9-24
 * @License Apache License Version 2.0
 */
public abstract class BaseInfoTreeHandleController extends BaseChildController {

    protected InfoTreeManageController manager;
    protected InfoNode sourceNode;

    @FXML
    protected ControlInfoTreeHandler handlerController;

    public void setParameters(InfoTreeManageController manager) {
        this.manager = manager;
        handlerController.setParameters(this);
    }

    public boolean managerRunning() {
        return manager != null
                && manager.getMyStage() != null
                && manager.getMyStage().isShowing();
    }

    public boolean isSourceNode(InfoNode node) {
        return InfoNode.equal(node, sourceNode);
    }

    public boolean matchManagerSelected(InfoNode node) {
        List<InfoNode> nodes = manager.selectedItems();
        if (nodes == null || nodes.isEmpty()) {
            return false;
        }
        for (InfoNode source : nodes) {
            if (InfoNode.equal(node, source)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkOptions(SingletonTask<Void> task, Connection conn,
            List<InfoNode> sourceNodes, InfoNode targetNode) {
        if (sourceNodes == null || sourceNodes.isEmpty()) {
            displayError(message("NoData"));
            return false;
        }
        if (targetNode == null) {
            displayError(message("SelectNodeMoveInto"));
            return false;
        }
        for (InfoNode source : sourceNodes) {
            if (manager.tableTreeNode.equalOrDescendant(task, conn, targetNode, source)) {
                displayError(message("TreeTargetComments"));
                return false;
            }
        }
        return true;
    }

    public InfoNode selected() {
        return handlerController.selectedNode;
    }

}
