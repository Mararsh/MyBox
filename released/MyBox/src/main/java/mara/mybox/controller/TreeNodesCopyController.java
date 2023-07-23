package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TreeItem;
import javafx.stage.Window;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.InfoNode;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public class TreeNodesCopyController extends ControlInfoTreeSelector {

    protected TreeManageController manageController;

    @FXML
    protected RadioButton nodeAndDescendantsRadio, descendantsRadio, nodeRadio;

    public TreeNodesCopyController() {
        baseTitle = message("Copy");
    }

    public void setParamters(TreeManageController manageController) {
        this.manageController = manageController;
        setCaller(manageController.nodesController);
    }

    @Override
    public boolean isSourceNode(InfoNode node) {
        List<InfoNode> nodes = manageController.selectedItems();
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
    public void okAction() {
        if (manageController == null || manageController.getMyStage() == null
                || !manageController.getMyStage().isShowing()) {
            return;
        }
        List<InfoNode> nodes = manageController.selectedItems();
        if (nodes == null || nodes.isEmpty()) {
            alertError(message("NoData"));
            manageController.getMyStage().requestFocus();
            return;
        }
        TreeItem<InfoNode> targetItem = selected();
        if (targetItem == null) {
            alertError(message("SelectNodeCopyInto"));
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
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    for (InfoNode sourceNode : nodes) {
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
