package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.InfoNode;
import mara.mybox.fxml.FxSingletonTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-30
 * @License Apache License Version 2.0
 */
public class InfoTreeNodeMoveController extends BaseInfoTreeHandleController {

    @FXML
    protected Label sourceLabel;

    public InfoTreeNodeMoveController() {
        baseTitle = message("MoveNode");
    }

    public void setParameters(InfoTreeManageController manager, InfoNode sourceNode, String name) {
        this.sourceNode = sourceNode;
        sourceLabel.setText(message("NodeMoved") + ": " + name);
        setParameters(manager);
    }

    @FXML
    @Override
    public void okAction() {
        if (sourceNode == null || sourceNode.isRoot()) {
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
        task = new FxSingletonTask<Void>(this) {

            InfoNode updatedNode;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    if (!checkOptions(task, conn, sourceNodes, targetNode)) {
                        return false;
                    }
                    sourceNode.setParentid(targetNode.getNodeid());
                    updatedNode = manager.tableTreeNode.updateData(conn, sourceNode);
                    return updatedNode != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (managerRunning()) {
                    manager.treeController.loadTree(targetNode);
                    manager.nodeMoved(targetNode, updatedNode);
                    manager.popSuccessful();
                }
                closeStage();

            }
        };
        start(task);
    }

}
