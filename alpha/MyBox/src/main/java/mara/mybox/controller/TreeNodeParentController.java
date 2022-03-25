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

    public TreeNodeParentController() {
        baseTitle = message("Owner");
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
        TreeItem<TreeNode> targetItem = treeView.getSelectionModel().getSelectedItem();
        if (targetItem == null) {
            alertError(message("SelectNodeAsParent"));
            return;
        }
        treeController.parentNode = targetItem.getValue();
        treeController.nodeController.updateParentNode();
        close();
    }

    /*
        static methods
     */
    public static TreeNodeParentController open(TreeManageController treeController) {
        TreeNodeParentController controller = (TreeNodeParentController) WindowTools.openChildStage(
                treeController.getMyWindow(), Fxmls.TreeNodeParentFxml, false);
        controller.setParamters(treeController);
        controller.requestMouse();
        return controller;
    }

}
