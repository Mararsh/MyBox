package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import mara.mybox.db.data.InfoNode;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-14
 * @License Apache License Version 2.0
 */
public class TreeNodeParentController extends ControlInfoTreeSelector {

    protected TreeManageController manageController;
    protected ControlTreeNodeAttributes nodeController;

    public TreeNodeParentController() {
        baseTitle = message("Owner");
    }

    public void setParamters(ControlTreeNodeAttributes nodeController) {
        this.nodeController = nodeController;
        this.manageController = nodeController.treeController;
        setCaller(manageController.nodesController);
    }

    @FXML
    @Override
    public void okAction() {
        if (manageController == null || !manageController.getMyStage().isShowing()) {
            close();
            return;
        }
        TreeItem<InfoNode> targetItem = selected();
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
    public static TreeNodeParentController open(ControlTreeNodeAttributes nodeController) {
        TreeNodeParentController controller = (TreeNodeParentController) WindowTools.openChildStage(
                nodeController.getMyWindow(), Fxmls.TreeNodeParentFxml, false);
        controller.setParamters(nodeController);
        controller.requestMouse();
        return controller;
    }

}
