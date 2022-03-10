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
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public class TreeLeavesCopyController extends TreeNodesController {

    public TreeLeavesCopyController() {
        baseTitle = message("Copy");
    }

    public void setParamters(TreeManageController treeController) {
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
                alertError(message("SelectNodeCopyInto"));
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
            task = new SingletonTask<Void>(this) {

                private int count;

                @Override
                protected boolean handle() {
                    try {
                        long parentid = targetNode.getNodeid();
                        List<TreeLeaf> newLeaves = new ArrayList<>();
                        for (TreeLeaf leaf : leaves) {
                            TreeLeaf newLeaf = new TreeLeaf(parentid, leaf.getName(), leaf.getValue(), leaf.getMore());
                            newLeaves.add(newLeaf);
                        }
                        count = tableTreeLeaf.insertList(newLeaves);
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
                    treeController.popInformation(message("Copied") + ": " + count);
                    closeStage();
                }
            };
            start(task);
        }
    }

    /*
        static methods
     */
    public static TreeLeavesCopyController oneOpen(TreeManageController treeController) {
        TreeLeavesCopyController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof TreeLeavesCopyController) {
                try {
                    controller = (TreeLeavesCopyController) object;
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (TreeLeavesCopyController) WindowTools.openStage(Fxmls.TreeLeavesCopyFxml);
        }
        if (controller != null) {
            controller.setParamters(treeController);
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
