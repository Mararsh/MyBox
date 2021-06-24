package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.stage.Modality;
import mara.mybox.db.data.TreeNode;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-30
 * @License Apache License Version 2.0
 */
public class TreeNodeMoveController extends BaseTreeNodeSelector {

    protected TreeNode sourceNode;

    @FXML
    protected Label sourceLabel;

    public TreeNodeMoveController() {
        baseTitle = message("MoveNode");
    }

    public void setCaller(BaseTreeNodeSelector treeController, TreeNode sourceNode, String name) {
        this.sourceNode = sourceNode;
        sourceLabel.setText(message("NodeMoved") + ":\n" + name);
        ignoreNode = sourceNode;
        setCaller(treeController);
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
            TreeItem<TreeNode> targetItem = treeView.getSelectionModel().getSelectedItem();
            if (targetItem == null) {
                alertError(message("SelectNodeMoveInto"));
                return;
            }
            TreeNode targetNode = targetItem.getValue();
            if (targetNode == null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    sourceNode.setParent(targetNode.getNodeid());
                    tableTree.updateData(sourceNode);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (treeController == null || !treeController.getMyStage().isShowing()) {
                        treeController = oneOpen();
                    } else {
                        treeController.nodeChanged(sourceNode);
                        treeController.nodeChanged(targetNode);
                    }
                    treeController.loadTree(targetNode);
                    treeController.popSuccessful();
                    closeStage();

                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

}
