package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import mara.mybox.db.data.TreeNode;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

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
        category = Languages.message("WebFavorites");
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
                = (WebFavoriteNodeCopyController) WindowTools.openStage(Fxmls.WebFavoriteNodeCopyFxml);
        controller.tableWebFavorite = favoritesController.tableWebFavorite;
        controller.setCaller(this, selectedItem.getValue(), chainName, onlyContents);
    }

    @FXML
    @Override
    protected void exportNode() {
        WebFavoritesExportController exportController
                = (WebFavoritesExportController) WindowTools.openStage(Fxmls.WebFavoritesExportFxml);
        exportController.setController(favoritesController);
    }

    @FXML
    protected void importExamples() {
        WebFavoritesImportController controller = (WebFavoritesImportController) WindowTools.openStage(Fxmls.WebFavoritesImportFxml);
        controller.importExamples(favoritesController);
    }

    @FXML
    protected void importFiles() {
        WebFavoritesImportController controller = (WebFavoritesImportController) WindowTools.openStage(Fxmls.WebFavoritesImportFxml);
        controller.favoritesController = favoritesController;
    }

}
