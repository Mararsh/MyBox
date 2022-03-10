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
import mara.mybox.db.data.TreeLeaf;
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
public class TreeLeavesMoveController extends TreeNodesController {

    public TreeLeavesMoveController() {
        baseTitle = message("Move");
    }

    public void setParameters(TreeManageController treeController) {
        this.treeController = treeController;
        tableTreeLeaf = treeController.tableTreeLeaf;
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
            List<TreeLeaf> leaves = treeController.tableView.getSelectionModel().getSelectedItems();
            if (leaves == null || leaves.isEmpty()) {
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
                        if (treeController.currentLeaf != null) {
                            currentid = treeController.currentLeaf.getLeafid();
                        }
                        for (TreeLeaf leaf : leaves) {
                            leaf.setParentid(parentid);
                            if (leaf.getLeafid() == currentid) {
                                updateAddress = true;
                            }
                        }
                        count = tableTreeLeaf.updateList(leaves);
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
                        treeController.currentLeaf.setParentid(parentid);
                        treeController.nodeOfCurrentLeaf = targetNode;
                        treeController.updateNodeOfCurrentLeaf();
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
    public static TreeLeavesMoveController oneOpen(TreeManageController treeController) {
        TreeLeavesMoveController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof TreeLeavesMoveController) {
                try {
                    controller = (TreeLeavesMoveController) object;
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (TreeLeavesMoveController) WindowTools.openStage(Fxmls.TreeLeavesMoveFxml);
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
