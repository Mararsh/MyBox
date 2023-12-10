package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.stage.Window;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.InfoNode;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public class InfoTreeNodesCopyController extends BaseInfoTreeHandleController {

    @FXML
    protected RadioButton nodeAndDescendantsRadio, descendantsRadio, nodeRadio;

    public InfoTreeNodesCopyController() {
        baseTitle = message("Copy");
    }

    @Override
    public boolean isSourceNode(InfoNode node) {
        return matchManagerSelected(node);
    }

    @FXML
    @Override
    public void okAction() {
        if (!managerRunning()) {
            return;
        }
        List<InfoNode> sourceNodes = manager.selectedItems();
        InfoNode targetNode = handlerController.selectedNode;
        if (targetNode == null) {
            popError(message("SelectNodeMoveInto"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    if (!checkOptions(task, conn, sourceNodes, targetNode)) {
                        return false;
                    }
                    for (InfoNode sourceNode : sourceNodes) {
                        if (nodeAndDescendantsRadio.isSelected()) {
                            ok = manager.treeController.copyNodeAndDescendants(conn, sourceNode, targetNode);
                        } else if (descendantsRadio.isSelected()) {
                            ok = manager.treeController.copyDescendants(conn, sourceNode, targetNode);
                        } else {
                            ok = manager.treeController.copyNode(conn, sourceNode, targetNode) != null;
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
                if (managerRunning()) {
                    manager.popSuccessful();
                    manager.nodesCopied(targetNode);
                }
                closeStage();
            }
        };
        start(task);
    }

    /*
        static methods
     */
    public static InfoTreeNodesCopyController oneOpen(InfoTreeManageController manager) {
        InfoTreeNodesCopyController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof InfoTreeNodesCopyController) {
                try {
                    controller = (InfoTreeNodesCopyController) object;
                    controller.requestMouse();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (InfoTreeNodesCopyController) WindowTools.openChildStage(manager.getMyWindow(), Fxmls.InfoTreeNodesCopyFxml);
        }
        if (controller != null) {
            controller.setParameters(manager);
            controller.requestMouse();
        }
        return controller;
    }

}
