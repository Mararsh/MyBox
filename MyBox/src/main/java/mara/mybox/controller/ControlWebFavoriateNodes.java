package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import mara.mybox.db.data.TreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2021-4-30
 * @License Apache License Version 2.0
 */
public class ControlWebFavoriateNodes extends BaseTreeNodeSelector {

    protected WebFavoritesController favoritesController;

    public ControlWebFavoriateNodes() {
    }

    public void setParent(WebFavoritesController parent, boolean manageMode) {
        super.setParent(parent, manageMode);
        favoritesController = parent;
        tableTree = parent.tableTree;
        category = message("WebFavorites");
        baseTitle = category;
    }

    @Override
    protected void copyNode(Boolean onlyContents) {
        TreeItem<TreeNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || isRoot(selectedItem.getValue())) {
            return;
        }
        String chainName = chainName(selectedItem);
        WebFavoriteNodeCopyController controller
                = (WebFavoriteNodeCopyController) FxmlStage.openStage(CommonValues.WebFavoriteNodeCopyFxml);
        controller.tableWebFavorite = favoritesController.tableWebFavorite;
        controller.setCaller(this, selectedItem.getValue(), chainName, onlyContents);
    }

    @FXML
    @Override
    protected void exportNode() {
        WebFavoritesExportController exportController
                = (WebFavoritesExportController) FxmlStage.openStage(CommonValues.WebFavoritesExportFxml);
        exportController.setController(favoritesController);
    }

    @FXML
    @Override
    protected void popImportMenu(MouseEvent mouseEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu = new MenuItem(message("ImportFiles"));
            menu.setOnAction((ActionEvent event) -> {
                WebFavoritesImportController controller = (WebFavoritesImportController) FxmlStage.openStage(CommonValues.WebFavoritesImportFxml);
                controller.favoritesController = favoritesController;
            });
            items.add(menu);

            menu = new MenuItem(message("ImportExamples"));
            menu.setOnAction((ActionEvent event) -> {
                importExamples();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                if (popMenu != null && popMenu.isShowing()) {
                    popMenu.hide();
                }
                popMenu = null;
            });
            items.add(menu);

            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);
            popMenu.getItems().addAll(items);
            FxmlControl.locateCenter((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void importExamples() {
        WebFavoritesImportController controller = (WebFavoritesImportController) FxmlStage.openStage(CommonValues.WebFavoritesImportFxml);
        controller.importExamples(favoritesController);
    }

}
