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

    protected TreeManageController manager;

    @FXML
    protected RadioButton nodeAndDescendantsRadio, descendantsRadio, nodeRadio;

    public TreeNodesCopyController() {
        baseTitle = message("Copy");
    }

    public void setParamters(TreeManageController manager) {
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
    public void okAction() {
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
                manager.popSuccessful();
                manager.nodesCopied(targetNode);
                closeStage();
            }
        };
        start(task);
    }

    /*
        static methods
     */
    public static TreeNodesCopyController oneOpen(TreeManageController manager) {
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
            controller = (TreeNodesCopyController) WindowTools.openChildStage(manager.getMyWindow(), Fxmls.TreeNodesCopyFxml);
        }
        if (controller != null) {
            controller.setParamters(manager);
            controller.requestMouse();
        }
        return controller;
    }

}
