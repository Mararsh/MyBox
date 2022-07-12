package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TreeItem;
import javafx.stage.Window;
import mara.mybox.db.DerbyBase;
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

    @FXML
    protected RadioButton nodeAndDescendantsRadio, descendantsRadio, nodeRadio;

    public TreeNodesCopyController() {
        baseTitle = message("Copy");
    }

    public void setParamters(TreeManageController treeController) {
        this.manageController = treeController;
        setCaller(treeController.nodesController);
    }

    @FXML
    @Override
    public void okAction() {
        if (manageController == null || manageController.getMyStage() == null || !manageController.getMyStage().isShowing()) {
            return;
        }
        List<TreeNode> nodes = manageController.tableView.getSelectionModel().getSelectedItems();
        if (nodes == null || nodes.isEmpty()) {
            alertError(message("NoData"));
            manageController.getMyStage().requestFocus();
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
        if (equal(targetNode, manageController.loadedParent)) {
            alertError(message("TargetShouldDifferentWithSource"));
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        for (TreeNode sourceNode : nodes) {
                            if (nodeAndDescendantsRadio.isSelected()) {
                                ok = copyNodeAndDescendants(conn, sourceNode, targetNode);
                            } else if (descendantsRadio.isSelected()) {
                                ok = copyDescendants(conn, sourceNode, targetNode);
                            } else {
                                ok = copyNode(conn, sourceNode, targetNode) != null;
                            }
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    manageController.popSuccessful();
                    manageController.nodesCopied(targetNode);
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
                    controller.requestMouse();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (TreeNodesCopyController) WindowTools.openChildStage(treeController.getMyWindow(), Fxmls.TreeNodesCopyFxml);
        }
        if (controller != null) {
            controller.setParamters(treeController);
            controller.requestMouse();
        }
        return controller;
    }

}
