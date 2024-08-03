package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.InfoNode;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-14
 * @License Apache License Version 2.0
 */
public class DataTreeNodeParentController extends BaseInfoTreeHandleController {

    protected ControlInfoNodeAttributes attributesController;

    public DataTreeNodeParentController() {
        baseTitle = message("SelectNodeAsParent");
    }

    public void setParameters(ControlInfoNodeAttributes nodeController) {
        attributesController = nodeController;
        manager = nodeController.manager;
        setParameters(manager);
    }

    @FXML
    @Override
    public void okAction() {
        if (manager == null || !manager.getMyStage().isShowing()) {
            close();
            return;
        }
        InfoNode targetNode = handlerController.selectedNode;
        if (targetNode == null) {
            popError(message("SelectNodeMoveInto"));
            return;
        }
        attributesController.setParentNode(targetNode);
        close();
    }

    /*
        static methods
     */
    public static DataTreeNodeParentController open(ControlInfoNodeAttributes nodeController) {
        DataTreeNodeParentController controller = (DataTreeNodeParentController) WindowTools.childStage(
                nodeController, Fxmls.InfoTreeNodeParentFxml);
        controller.setParameters(nodeController);
        controller.requestMouse();
        return controller;
    }

}
