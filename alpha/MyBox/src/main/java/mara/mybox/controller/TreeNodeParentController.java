package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import mara.mybox.db.data.TreeNode;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-14
 * @License Apache License Version 2.0
 */
public class TreeNodeParentController extends TreeNodesController {

    protected TreeNodeEditor nodeController;

    public TreeNodeParentController() {
        baseTitle = message("Owner");
    }

    public void setParamters(TreeNodeEditor nodeController) {
        this.nodeController = nodeController;
        this.treeController = nodeController.treeController;
        setCaller(treeController.nodesController);
    }

    @FXML
    @Override
    public void okAction() {
        if (treeController == null || !treeController.getMyStage().isShowing()) {
            close();
            return;
        }
        TreeItem<TreeNode> targetItem = treeView.getSelectionModel().getSelectedItem();
        if (targetItem == null) {
            alertError(message("SelectNodeAsParent"));
            return;
        }
        nodeController.setParentNode(targetItem.getValue());
        close();
    }

    /*
        static methods
     */
    public static TreeNodeParentController open(TreeNodeEditor nodeController) {
        TreeNodeParentController controller = (TreeNodeParentController) WindowTools.openChildStage(
                nodeController.getMyWindow(), Fxmls.TreeNodeParentFxml, false);
        controller.setParamters(nodeController);
        controller.requestMouse();
        return controller;
    }

}
