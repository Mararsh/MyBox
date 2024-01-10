package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.stage.Window;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.InfoNode;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-30
 * @License Apache License Version 2.0
 */
public class InfoTreeNodesMoveController extends BaseInfoTreeHandleController {

    public InfoTreeNodesMoveController() {
        baseTitle = message("MoveNode");
    }

    @Override
    public boolean isSourceNode(InfoNode node) {
        return matchManagerSelected(node);
    }

    @FXML
    @Override
    public synchronized void okAction() {
        if (!managerRunning()) {
            return;
        }
        List<InfoNode> sourceNodes = manager.tableController.selectedItems();
        InfoNode targetNode = handlerController.selectedNode;
        if (targetNode == null) {
            popError(message("SelectNodeMoveInto"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private int count;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    if (!checkOptions(this, conn, sourceNodes, targetNode)) {
                        return false;
                    }
                    long parentid = targetNode.getNodeid();
                    for (InfoNode node : sourceNodes) {
                        node.setParentid(parentid);
                    }
                    count = manager.tableTreeNode.updateList(conn, sourceNodes);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return count > 0;
            }

            @Override
            protected void whenSucceeded() {
                if (managerRunning()) {
                    manager.popInformation(message("Moved") + ": " + count);
                    manager.nodesMoved(targetNode, sourceNodes);
                }
                closeStage();
            }
        };
        start(task);
    }

    /*
        static methods
     */
    public static InfoTreeNodesMoveController oneOpen(InfoTreeManageController manager) {
        InfoTreeNodesMoveController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof InfoTreeNodesMoveController) {
                try {
                    controller = (InfoTreeNodesMoveController) object;
                    controller.requestMouse();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (InfoTreeNodesMoveController) WindowTools.childStage(
                    manager, Fxmls.InfoTreeNodesMoveFxml);
        }
        if (controller != null) {
            controller.setParameters(manager);
            controller.requestMouse();
        }
        return controller;
    }

}
