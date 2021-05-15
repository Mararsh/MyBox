package mara.mybox.controller;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.db.data.WebFavorite;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.table.TableWebFavorite;
import mara.mybox.fxml.FxmlStage;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2021-4-30
 * @License Apache License Version 2.0
 */
public class WebFavoritesMoveController extends BaseTreeNodeSelector {

    protected WebFavoritesController favoriteController;
    protected TableWebFavorite tableFavoriteAddress;

    public WebFavoritesMoveController() {
        baseTitle = message("MoveFavorites");
    }

    public void setController(WebFavoritesController favoriteController) {
        this.favoriteController = favoriteController;
        tableFavoriteAddress = favoriteController.tableWebFavorite;
        setCaller(favoriteController.treeController);
    }

    @FXML
    @Override
    public void okAction() {
        if (favoriteController == null || !favoriteController.getMyStage().isShowing()) {
            favoriteController = WebFavoritesController.oneOpen();
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            List<WebFavorite> addresses = favoriteController.tableView.getSelectionModel().getSelectedItems();
            if (addresses == null || addresses.isEmpty()) {
                alertError(message("NoData"));
                favoriteController.getMyStage().requestFocus();
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
            if (equal(targetNode, favoriteController.treeController.selectedNode)) {
                alertError(message("TargetShouldDifferentWithSource"));
                return;
            }
            long owner = targetNode.getNodeid();
            task = new SingletonTask<Void>() {

                private int count;
                private boolean updateAddress = false;

                @Override
                protected boolean handle() {
                    try {
                        long currentid = -1;
                        if (favoriteController.currentAddress != null) {
                            currentid = favoriteController.currentAddress.getFaid();
                        }
                        for (WebFavorite address : addresses) {
                            address.setOwner(owner);
                            if (address.getFaid() == currentid) {
                                updateAddress = true;
                            }
                        }
                        count = tableFavoriteAddress.updateList(addresses);
                        return count > 0;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    favoriteController.treeController.nodeChanged(targetNode);
                    favoriteController.treeController.loadTree(targetNode);
                    favoriteController.popInformation(message("Moved") + ": " + count);
                    if (updateAddress) {
                        favoriteController.currentAddress.setOwner(owner);
                        favoriteController.nodeOfCurrentAddress = targetNode;
                        favoriteController.updateNodeOfCurrentAddress();
                    }
                    closeStage();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    /*
        static methods
     */
    public static WebFavoritesMoveController oneOpen(WebFavoritesController favoriteController) {
        WebFavoritesMoveController controller = null;
        Stage stage = FxmlStage.findStage(message("MoveFavorites"));
        if (stage != null && stage.getUserData() != null) {
            try {
                controller = (WebFavoritesMoveController) stage.getUserData();
            } catch (Exception e) {
            }
        }
        if (controller == null) {
            controller = (WebFavoritesMoveController) FxmlStage.openStage(CommonValues.WebFavoritesMoveFxml);
        }
        if (controller != null) {
            controller.setController(favoriteController);
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
