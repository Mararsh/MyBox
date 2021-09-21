package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.stage.Modality;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.data.WebFavorite;
import mara.mybox.db.table.TableWebFavorite;
import mara.mybox.dev.MyBoxLog;

import mara.mybox.tools.IconTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-3-29
 * @License Apache License Version 2.0
 */
public class WebFavoriteAddController extends BaseTreeNodeSelector {

    protected TableWebFavorite tableFavoriteAddress;
    protected String title, address;

    public WebFavoriteAddController() {
        baseTitle = Languages.message("AddAsFavorite");
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            setParent(null, Languages.message("WebFavorites"));
            loadTree();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setValues(String title, String address) {
        this.title = title;
        this.address = address;
        tableFavoriteAddress = new TableWebFavorite();
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
            task = new SingletonTask<Void>() {

                private WebFavorite data;

                @Override
                protected boolean handle() {
                    try {
                        data = new WebFavorite();
                        data.setTitle(title);
                        data.setAddress(address);
                        File icon = IconTools.readIcon(address, true);
                        if (icon != null) {
                            data.setIcon(icon.getAbsolutePath());
                        }
                        data.setOwner(node.getNodeid());
                        data = tableFavoriteAddress.insertData(data);
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
