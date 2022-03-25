package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.table.TableTreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.IconTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-3-29
 * @License Apache License Version 2.0
 */
public class WebFavoriteAddController extends TreeNodesController {

    protected String title, address;

    public WebFavoriteAddController() {
        baseTitle = message("AddAsFavorite");
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            super.setManager(null, false);
            treeController = null;
            tableTreeNode = new TableTreeNode();
            category = TreeNode.WebFavorite;

            loadTree();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setValues(String title, String address) {
        this.title = title;
        this.address = address;
    }

    @FXML
    @Override
    public void okAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            TreeItem<TreeNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                alertError(Languages.message("SelectNodeAddInto"));
                return;
            }
            TreeNode node = selectedItem.getValue();
            task = new SingletonTask<Void>(this) {

                private TreeNode data;

                @Override
                protected boolean handle() {
                    try {
                        data = new TreeNode();
                        data.setTitle(title);
                        data.setValue(address);
                        File icon = IconTools.readIcon(address, true);
                        if (icon != null) {
                            data.setMore(icon.getAbsolutePath());
                        }
                        data.setParentid(node.getNodeid());
                        data = tableTreeNode.insertData(data);
                        return data != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    WebFavoritesController.oneOpen(node);
                    closeStage();
                }
            };
            start(task);
        }
    }

}
