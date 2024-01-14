package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import mara.mybox.db.data.InfoNode;
import mara.mybox.db.table.TableTreeNode;
import mara.mybox.db.table.TableTreeNodeTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.IconTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-3-29
 * @License Apache License Version 2.0
 */
public class WebFavoriteAddController extends ControlInfoTreeList {

    protected String title, address;

    @FXML
    protected TextField titleInput, addressInput;

    public WebFavoriteAddController() {
        baseTitle = message("AddAsFavorite");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            tableTreeNode = new TableTreeNode();
            tableTreeNodeTag = new TableTreeNodeTag();
            category = InfoNode.WebFavorite;

            loadTree();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setValues(String title, String address) {
        titleInput.setText(title);
        addressInput.setText(address);
    }

    @FXML
    @Override
    public void okAction() {
        if (task != null) {
            task.cancel();
        }
        address = addressInput.getText();
        if (address == null || address.isBlank()) {
            popError(message("InvalidParameters"));
            return;
        }
        title = titleInput.getText();
        if (title == null || title.isBlank()) {
            title = address;
        }
        TreeItem<InfoNode> selectedItem = selected();
        if (selectedItem == null) {
            alertError(message("SelectNodeAddInto"));
            return;
        }
        task = new FxSingletonTask<Void>(this) {

            private InfoNode newNode;

            @Override
            protected boolean handle() {
                try {
                    File icon = IconTools.readIcon(this, address, true);
                    String info;
                    if (address != null && !address.isBlank()) {
                        info = address.trim() + "\n";
                    } else {
                        info = "";
                    }
                    if (icon != null && icon.exists()) {
                        info += InfoNode.ValueSeparater + "\n" + icon.getAbsolutePath();
                    }
                    if (info.isBlank()) {
                        error = message("NoData");
                        return false;
                    }
                    newNode = InfoNode.create()
                            .setParentid(selectedItem.getValue().getNodeid())
                            .setCategory(category)
                            .setTitle(title)
                            .setInfo(info);
                    newNode = tableTreeNode.insertData(newNode);
                    return newNode != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                WebFavoritesController c = WebFavoritesController.oneOpen();
                if (!c.treeController.focusNode(newNode)) {
                    c.treeController.loadTree(newNode);
                }
                closeStage();
            }
        };
        start(task);
    }

    /*
        static methods
     */
    public static WebFavoriteAddController open(String title, String address) {
        WebFavoriteAddController controller = (WebFavoriteAddController) WindowTools.openStage(Fxmls.WebFavoriteAddFxml);
        if (controller != null) {
            controller.setValues(title, address);
            controller.requestMouse();
        }
        return controller;
    }

}
