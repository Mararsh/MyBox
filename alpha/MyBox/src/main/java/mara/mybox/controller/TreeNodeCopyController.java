package mara.mybox.controller;

import java.sql.Connection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.TreeNode;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-27
 * @License Apache License Version 2.0
 */
public class TreeNodeCopyController extends TreeNodesController {

    protected TreeNode sourceNode;
    protected boolean onlyContents;

    @FXML
    protected Label sourceLabel;

    public TreeNodeCopyController() {
        baseTitle = message("CopyNode");
    }

    public void setCaller(TreeNodesController nodesController, TreeNode sourceNode, String name, boolean onlyContents) {
        this.sourceNode = sourceNode;
        this.onlyContents = onlyContents;
        sourceLabel.setText(message("NodeCopyed") + ":\n" + name);
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
            alertError(message("SelectToHandle"));
            return;
        }
        TreeItem<TreeNode> targetItem = treeView.getSelectionModel().getSelectedItem();
        if (targetItem == null) {
            alertError(message("SelectNodeCopyInto"));
            return;
        }
        TreeNode targetNode = targetItem.getValue();
        if (targetNode == null) {
            alertError(message("SelectNodeCopyInto"));
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        if (!onlyContents) {
                            ok = copyNode(conn, sourceNode, targetNode);
                        } else {
                            ok = copyChildren(conn, sourceNode, targetNode);
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return ok;
                }

                @Override
                protected void whenSucceeded() {
                    if (caller != null && caller.getMyStage() != null && caller.getMyStage().isShowing()) {
                        caller.loadTree(targetNode);
                        caller.nodeAdded(targetNode, sourceNode);
                        caller.popSuccessful();
                    }
                    closeStage();
                }
            };
            start(task);
        }
    }

}
