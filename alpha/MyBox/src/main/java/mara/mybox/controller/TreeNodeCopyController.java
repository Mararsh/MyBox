package mara.mybox.controller;

import java.sql.Connection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TreeItem;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.InfoNode;
import mara.mybox.fxml.SingletonCurrentTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-27
 * @License Apache License Version 2.0
 */
public class TreeNodeCopyController extends ControlInfoTreeSelector {

    protected InfoNode sourceNode;

    @FXML
    protected Label sourceLabel;
    @FXML
    protected RadioButton nodeAndDescendantsRadio, descendantsRadio, nodeRadio;

    public TreeNodeCopyController() {
        baseTitle = message("CopyNode");
    }

    public void setCaller(BaseInfoTreeController nodesController, InfoNode sourceNode, String name) {
        this.sourceNode = sourceNode;
        sourceLabel.setText(message("NodeCopyed") + ":\n" + name);
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
            alertError(message("SelectToHandle"));
            return;
        }
        TreeItem<InfoNode> targetItem = selected();
        if (targetItem == null) {
            alertError(message("SelectNodeCopyInto"));
            return;
        }
        InfoNode targetNode = targetItem.getValue();
        if (targetNode == null) {
            alertError(message("SelectNodeCopyInto"));
            return;
        }
        if (equalOrDescendant(targetItem, find(sourceNode))) {
            alertError(message("TreeTargetComments"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    if (nodeAndDescendantsRadio.isSelected()) {
                        ok = copyNodeAndDescendants(conn, sourceNode, targetNode);
                    } else if (descendantsRadio.isSelected()) {
                        ok = copyDescendants(conn, sourceNode, targetNode);
                    } else {
                        ok = copyNode(conn, sourceNode, targetNode) != null;
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
