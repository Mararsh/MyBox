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
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public class TreeNodesCopyController extends TreeNodesController {

    public TreeNodesCopyController() {
        baseTitle = message("Copy");
    }

    public void setParamters(TreeManageController treeController) {
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
            List<TreeNode> leaves = treeController.tableView.getSelectionModel().getSelectedItems();
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
            if (equal(targetNode, treeController.loadedParent)) {
                alertError(message("TargetShouldDifferentWithSource"));
                return;
            }
            task = new SingletonTask<Void>(this) {

                private List<TreeNode> newNodes;
                private int count;

                @Override
                protected boolean handle() {
                    try {
                        long parentid = targetNode.getNodeid();
                        newNodes = new ArrayList<>();
                        for (TreeNode node : leaves) {
                            TreeNode newNode = TreeNode.create().setParentid(parentid).setCategory(category)
                                    .setTitle(node.getTitle()).setValue(node.getValue()).setMore(node.getMore());
                            newNodes.add(newNode);
                        }
                        count = tableTreeNode.insertList(newNodes);
                        return count > 0;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    treeController.popInformation(message("Copied") + ": " + count);
                    treeController.nodesCopied(targetNode, newNodes);
                    closeStage();
                }
            };
            start(task);
        }
    }

    /*
        static methods
     */
    public static TreeNodesCopyController oneOpen(TreeManageController treeController) {
        TreeNodesCopyController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof TreeNodesCopyController) {
                try {
                    controller = (TreeNodesCopyController) object;
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (TreeNodesCopyController) WindowTools.openStage(Fxmls.TreeNodesCopyFxml);
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
