package mara.mybox.controller;

import java.util.ArrayList;
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
import mara.mybox.fxml.FxmlWindow;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2021-4-30
 * @License Apache License Version 2.0
 */
public class WebFavoritesCopyController extends BaseTreeNodeSelector {

    protected WebFavoritesController favoriteController;
    protected TableWebFavorite tableFavoriteAddress;

    public WebFavoritesCopyController() {
        baseTitle = message("CopyFavorites");
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
                alertError(message("SelectNodeCopyInto"));
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
            task = new SingletonTask<Void>() {

                private int count;

                @Override
                protected boolean handle() {
                    try {
                        long owner = targetNode.getNodeid();
                        List<WebFavorite> newAddresses = new ArrayList<>();
                        for (WebFavorite address : addresses) {
                            WebFavorite newAddress = new WebFavorite(owner, address.getTitle(), address.getAddress(), address.getIcon());
                            newAddresses.add(newAddress);
                        }
                        count = tableFavoriteAddress.insertList(newAddresses);
                        return count > 0;
                    } catch (Exception e) {
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    favoriteController.treeController.nodeChanged(targetNode);
                    favoriteController.treeController.loadTree(targetNode);
                    favoriteController.popInformation(message("Copied") + ": " + count);
                    closeStage();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    /*
        static methods
     */
    public static WebFavoritesCopyController oneOpen(WebFavoritesController favoriteController) {
        WebFavoritesCopyController controller = null;
        Stage stage = FxmlWindow.findStage(message("CopyFavorites"));
        if (stage != null && stage.getUserData() != null) {
            try {
                controller = (WebFavoritesCopyController) stage.getUserData();
            } catch (Exception e) {
            }
        }
        if (controller == null) {
            controller = (WebFavoritesCopyController) FxmlWindow.openStage(CommonValues.WebFavoritesCopyFxml);
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
