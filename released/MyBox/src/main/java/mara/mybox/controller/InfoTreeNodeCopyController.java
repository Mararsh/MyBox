package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.InfoNode;
import mara.mybox.fxml.SingletonCurrentTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-27
 * @License Apache License Version 2.0
 */
public class InfoTreeNodeCopyController extends BaseInfoTreeHandleController {

    @FXML
    protected Label sourceLabel;
    @FXML
    protected RadioButton nodeAndDescendantsRadio, descendantsRadio, nodeRadio;

    public InfoTreeNodeCopyController() {
        baseTitle = message("CopyNode");
    }

    public void setParameters(InfoTreeManageController manager, InfoNode sourceNode, String name) {
        this.sourceNode = sourceNode;
        sourceLabel.setText(message("NodeCopyed") + ": " + name);
        setParameters(manager);
    }

    @FXML
    @Override
    public void okAction() {
        if (sourceNode == null || sourceNode.isRoot()) {
            alertError(message("SelectToHandle"));
            return;
        }
        List<InfoNode> sourceNodes = new ArrayList<>();
        sourceNodes.add(sourceNode);
        InfoNode targetNode = handlerController.selectedNode;
        if (targetNode == null) {
            popError(message("SelectNodeMoveInto"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    if (!checkOptions(task, conn, sourceNodes, targetNode)) {
                        return false;
                    }
                    if (nodeAndDescendantsRadio.isSelected()) {
                        ok = manager.treeController.copyNodeAndDescendants(conn, sourceNode, targetNode);
                    } else if (descendantsRadio.isSelected()) {
                        ok = manager.treeController.copyDescendants(conn, sourceNode, targetNode);
                    } else {
                        ok = manager.treeController.copyNode(conn, sourceNode, targetNode) != null;
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return ok;
            }

            @Override
            protected void whenSucceeded() {
                if (managerRunning()) {
                    manager.treeController.loadTree(targetNode);
                    manager.nodeAdded(targetNode, sourceNode);
                    manager.popSuccessful();
                }
                closeStage();
            }
        };
        start(task);
    }

}
