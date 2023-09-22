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
public class TreeNodeParentController extends BaseController {

    protected TreeManageController manageController;
    protected ControlInfoNodeAttributes nodeController;

    @FXML
    protected ControlInfoNodeSelector selectorController;

    public TreeNodeParentController() {
        baseTitle = message("Owner");
    }

    public void setParamters(ControlInfoNodeAttributes nodeController) {
        this.nodeController = nodeController;
        this.manageController = nodeController.manager;
        selectorController.nodesController.setCaller(manageController.nodesController);
    }

    @FXML
    @Override
    public void okAction() {
        if (manageController == null || !manageController.getMyStage().isShowing()) {
            close();
            return;
        }
        TreeItem<InfoNode> targetItem = selectorController.nodesController.selected();
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
    public static TreeNodeParentController open(ControlInfoNodeAttributes nodeController) {
        TreeNodeParentController controller = (TreeNodeParentController) WindowTools.openChildStage(
                nodeController.getMyWindow(), Fxmls.TreeNodeParentFxml, false);
        controller.setParamters(nodeController);
        controller.requestMouse();
        return controller;
    }

}
