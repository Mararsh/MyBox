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
import javafx.stage.Window;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.data.WebFavorite;
import mara.mybox.db.table.TableWebFavorite;
import mara.mybox.fxml.WindowTools;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-4-30
 * @License Apache License Version 2.0
 */
public class WebFavoritesMoveController extends BaseTreeNodeSelector {

    protected WebFavoritesController favoriteController;
    protected TableWebFavorite tableFavoriteAddress;

    public WebFavoritesMoveController() {
        baseTitle = Languages.message("MoveFavorites");
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
                alertError(Languages.message("NoData"));
                favoriteController.getMyStage().requestFocus();
                return;
            }
            TreeItem<TreeNode> targetItem = treeView.getSelectionModel().getSelectedItem();
            if (targetItem == null) {
                alertError(Languages.message("SelectNodeMoveInto"));
                return;
            }
            TreeNode targetNode = targetItem.getValue();
            if (targetNode == null) {
                return;
            }
            if (equal(targetNode, favoriteController.treeController.selectedNode)) {
                alertError(Languages.message("TargetShouldDifferentWithSource"));
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
                    favoriteController.popInformation(Languages.message("Moved") + ": " + count);
                    if (updateAddress) {
                        favoriteController.currentAddress.setOwner(owner);
                        favoriteController.nodeOfCurrentAddress = targetNode;
                        favoriteController.updateNodeOfCurrentAddress();
                    }
                    closeStage();
                }
            };
            start(task);
        }
    }

    /*
        static methods
     */
    public static WebFavoritesMoveController oneOpen(WebFavoritesController favoriteController) {
        WebFavoritesMoveController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof WebFavoritesMoveController) {
                try {
                    controller = (WebFavoritesMoveController) object;
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (WebFavoritesMoveController) WindowTools.openStage(Fxmls.WebFavoritesMoveFxml);
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
