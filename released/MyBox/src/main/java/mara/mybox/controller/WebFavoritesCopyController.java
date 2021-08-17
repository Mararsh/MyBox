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
public class WebFavoritesCopyController extends BaseTreeNodeSelector {

    protected WebFavoritesController favoriteController;
    protected TableWebFavorite tableFavoriteAddress;

    public WebFavoritesCopyController() {
        baseTitle = Languages.message("CopyFavorites");
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
                alertError(Languages.message("SelectNodeCopyInto"));
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
                    favoriteController.popInformation(Languages.message("Copied") + ": " + count);
                    closeStage();
                }
            };
            handling(task);
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
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof WebFavoritesCopyController) {
                try {
                    controller = (WebFavoritesCopyController) object;
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (WebFavoritesCopyController) WindowTools.openStage(Fxmls.WebFavoritesCopyFxml);
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
