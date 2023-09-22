package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.stage.Window;
import mara.mybox.db.data.InfoNode;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-30
 * @License Apache License Version 2.0
 */
public class TreeNodesMoveController extends ControlInfoTreeSelector {

    protected TreeManageController manager;

    public TreeNodesMoveController() {
        baseTitle = message("Move");
    }

    public void setParameters(TreeManageController manager) {
        this.manager = manager;
        setCaller(manager.treeController);
    }

    @Override
    public boolean isSourceNode(InfoNode node) {
        List<InfoNode> nodes = manager.selectedItems();
        if (nodes == null || nodes.isEmpty()) {
            return false;
        }
        for (InfoNode sourceNode : nodes) {
            if (equalNode(node, sourceNode)) {
                return true;
            }
        }
        return false;
    }

    @FXML
    @Override
    public synchronized void okAction() {
        if (manager == null || manager.getMyStage() == null
                || !manager.getMyStage().isShowing()) {
            return;
        }
        List<InfoNode> nodes = manager.selectedItems();
        if (nodes == null || nodes.isEmpty()) {
            alertError(message("NoData"));
            manager.getMyStage().requestFocus();
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
        for (InfoNode sourceNode : nodes) {
            if (equalOrDescendant(targetItem, find(sourceNode))) {
                alertError(message("TreeTargetComments"));
                return;
            }
        }
        long parentid = targetNode.getNodeid();
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private int count;

            @Override
            protected boolean handle() {
                try {
                    for (InfoNode node : nodes) {
                        node.setParentid(parentid);
                    }
                    count = tableTreeNode.updateList(nodes);
                    return count > 0;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                manager.popInformation(message("Moved") + ": " + count);
                manager.nodesMoved(targetNode, nodes);
                closeStage();
            }
        };
        start(task);
    }

    /*
        static methods
     */
    public static TreeNodesMoveController oneOpen(TreeManageController manager) {
        TreeNodesMoveController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof TreeNodesMoveController) {
                try {
                    controller = (TreeNodesMoveController) object;
                    controller.requestMouse();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (TreeNodesMoveController) WindowTools.openChildStage(manager.getMyWindow(), Fxmls.TreeNodesMoveFxml);
        }
        if (controller != null) {
            controller.setParameters(manager);
            controller.requestMouse();
        }
        return controller;
    }

}
