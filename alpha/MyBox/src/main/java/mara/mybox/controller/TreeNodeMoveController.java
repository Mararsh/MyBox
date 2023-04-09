package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import mara.mybox.db.data.TreeNode;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-30
 * @License Apache License Version 2.0
 */
public class TreeNodeMoveController extends ControlTreeInfoSelect {

    protected ControlTreeInfoManage manager;
    protected TreeNode sourceNode;

    @FXML
    protected Label sourceLabel;

    public TreeNodeMoveController() {
        baseTitle = message("MoveNode");
    }

    public void setCaller(ControlTreeInfoManage nodesController, TreeNode sourceNode, String name) {
        manager = nodesController;
        this.sourceNode = sourceNode;
        sourceLabel.setText(message("NodeMoved") + ":\n" + name);
        ignoreNode = sourceNode;
        setCaller(nodesController);
    }

    @Override
    public TreeNode getIgnoreNode() {
        return sourceNode;
    }

    @FXML
    @Override
    public void okAction() {
        if (sourceNode == null || sourceNode.isRoot()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            TreeItem<TreeNode> targetItem = selected();
            if (targetItem == null) {
                alertError(message("SelectNodeMoveInto"));
                return;
            }
            TreeNode targetNode = targetItem.getValue();
            if (targetNode == null) {
                return;
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

}
