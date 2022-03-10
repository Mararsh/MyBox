package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
 * @CreateDate 2021-4-30
 * @License Apache License Version 2.0
 */
public class TreeNodeMoveController extends TreeNodesController {

    protected TreeNode sourceNode;

    @FXML
    protected Label sourceLabel;

    public TreeNodeMoveController() {
        baseTitle = message("MoveNode");
    }

    public void setCaller(TreeNodesController nodesController, TreeNode sourceNode, String name) {
        this.sourceNode = sourceNode;
        sourceLabel.setText(message("NodeMoved") + ":\n" + name);
        ignoreNode = sourceNode;
        setCaller(nodesController);
    }

    @Override
    public TreeNode getIgnoreNode() {
        return sourceNode;
    }

    @FXML
    @Override
    public void okAction() {
        if (sourceNode == null || sourceNode.isRoot()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            TreeItem<TreeNode> targetItem = treeView.getSelectionModel().getSelectedItem();
            if (targetItem == null) {
                alertError(message("SelectNodeMoveInto"));
                return;
            }
            TreeNode targetNode = targetItem.getValue();
            if (targetNode == null) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    sourceNode.setParent(targetNode.getNodeid());
                    tableTree.updateData(sourceNode);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (caller == null || !caller.getMyStage().isShowing()) {
                        caller = oneOpen();
                    } else {
                        caller.nodeChanged(sourceNode);
                        caller.nodeChanged(targetNode);
                    }
                    caller.loadTree(targetNode);
                    caller.popSuccessful();
                    closeStage();

                }
            };
            start(task);
        }
    }

    /*
        static methods
     */
    public static TreeNodeMoveController oneOpen(TreeNodesController nodesController) {
        TreeNodeMoveController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof TreeNodeMoveController) {
                try {
                    controller = (TreeNodeMoveController) object;
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (TreeNodeMoveController) WindowTools.openStage(Fxmls.TreeNodeMoveFxml);
        }
        if (controller != null) {
            controller.setCaller(nodesController);
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
