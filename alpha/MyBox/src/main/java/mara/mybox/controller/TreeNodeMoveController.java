package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import mara.mybox.db.data.InfoNode;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-30
 * @License Apache License Version 2.0
 */
public class TreeNodeMoveController extends ControlInfoTreeSelector {

    protected ControlInfoTreeManage manager;
    protected InfoNode sourceNode;

    @FXML
    protected Label sourceLabel;

    public TreeNodeMoveController() {
        baseTitle = message("MoveNode");
    }

    public void setCaller(ControlInfoTreeManage nodesController, InfoNode sourceNode, String name) {
        manager = nodesController;
        this.sourceNode = sourceNode;
        sourceLabel.setText(message("NodeMoved") + ":\n" + name);
        setCaller(nodesController);
    }

    @Override
    public boolean isSourceNode(InfoNode node) {
        return equalNode(node, sourceNode);
    }

    @FXML
    @Override
    public void okAction() {
        if (sourceNode == null || sourceNode.isRoot()) {
            return;
        }
        TreeItem<InfoNode> targetItem = selected();
        if (targetItem == null) {
            alertError(message("SelectNodeMoveInto"));
            return;
        }
        InfoNode targetNode = targetItem.getValue();
        if (targetNode == null) {
            return;
        }
        if (equalOrDescendant(targetItem, find(sourceNode))) {
            alertError(message("TreeTargetComments"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                sourceNode.setParentid(targetNode.getNodeid());
                tableTreeNode.updateData(sourceNode);
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (caller != null && caller.getMyStage() != null && caller.getMyStage().isShowing()) {
                    caller.loadTree(targetNode);
                    manager.nodeMoved(targetNode, sourceNode);
                    caller.popSuccessful();
                }
                closeStage();

            }
        };
        start(task);
    }

}
