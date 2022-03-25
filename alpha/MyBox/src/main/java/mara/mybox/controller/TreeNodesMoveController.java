package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
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
public class TreeNodesMoveController extends TreeNodesController {

    public TreeNodesMoveController() {
        baseTitle = message("Move");
    }

    public void setParameters(TreeManageController treeController) {
        this.treeController = treeController;
        setCaller(treeController.nodesController);
    }

    @FXML
    @Override
    public void okAction() {
        if (treeController == null || !treeController.getMyStage().isShowing()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            List<TreeNode> nodes = treeController.tableView.getSelectionModel().getSelectedItems();
            if (nodes == null || nodes.isEmpty()) {
                alertError(message("NoData"));
                treeController.getMyStage().requestFocus();
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
            if (equal(targetNode, treeController.nodesController.selectedNode)) {
                alertError(message("TargetShouldDifferentWithSource"));
                return;
            }
            long parentid = targetNode.getNodeid();
            task = new SingletonTask<Void>(this) {

                private int count;
                private boolean updateAddress = false;

                @Override
                protected boolean handle() {
                    try {
                        long currentid = -1;
                        if (treeController.currentNode != null) {
                            currentid = treeController.currentNode.getNodeid();
                        }
                        for (TreeNode node : nodes) {
                            node.setParentid(parentid);
                            if (node.getNodeid() == currentid) {
                                updateAddress = true;
                            }
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
                    treeController.nodesController.nodeChanged(targetNode);
                    treeController.nodesController.loadTree(targetNode);
                    treeController.popInformation(message("Moved") + ": " + count);
                    if (updateAddress) {
                        treeController.currentNode.setParentid(parentid);
                        treeController.parentNode = targetNode;
                        treeController.nodeController.updateParentNode();
                    }
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
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (TreeNodesMoveController) WindowTools.openStage(Fxmls.TreeNodesMoveFxml);
        }
        if (controller != null) {
            controller.setParameters(treeController);
            Stage cstage = controller.getMyStage();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        cstage.requestFocus();
                        cstage.toFront();
                    });
                }
            }, 500);
        }
        return controller;
    }

}
