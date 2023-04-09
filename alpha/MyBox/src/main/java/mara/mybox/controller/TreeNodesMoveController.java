package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.stage.Window;
import mara.mybox.db.data.TreeNode;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-30
 * @License Apache License Version 2.0
 */
public class TreeNodesMoveController extends ControlTreeInfoSelect {

    protected TreeManageController manageController;

    public TreeNodesMoveController() {
        baseTitle = message("Move");
    }

    public void setParameters(TreeManageController manageController) {
        this.manageController = manageController;
        setCaller(manageController.nodesController);
    }

    @FXML
    @Override
    public void okAction() {
        if (manageController == null || manageController.getMyStage() == null
                || !manageController.getMyStage().isShowing()) {
            return;
        }
        synchronized (this) {
            List<TreeNode> nodes = manageController.selectedItems();
            if (nodes == null || nodes.isEmpty()) {
                alertError(message("NoData"));
                manageController.getMyStage().requestFocus();
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
            if (equal(targetNode, manageController.loadedParent)) {
                alertError(message("TargetShouldDifferentWithSource"));
                return;
            }
            long parentid = targetNode.getNodeid();
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>(this) {

                private int count;

                @Override
                protected boolean handle() {
                    try {
                        for (TreeNode node : nodes) {
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
                    manageController.popInformation(message("Moved") + ": " + count);
                    manageController.nodesMoved(targetNode, nodes);
                    closeStage();
                }
            };
            start(task);
        }
    }

    /*
        static methods
     */
    public static TreeNodesMoveController oneOpen(TreeManageController treeController) {
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
            controller = (TreeNodesMoveController) WindowTools.openChildStage(treeController.getMyWindow(), Fxmls.TreeNodesMoveFxml);
        }
        if (controller != null) {
            controller.setParameters(treeController);
            controller.requestMouse();
        }
        return controller;
    }

}
