package mara.mybox.fxml.cell;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

/**
 * @Author Mara
 * @CreateDate 2023-4-9
 * @License Apache License Version 2.0
 */
public class TreeTableHierachyCell<T> extends TreeTableCell<T, String>
        implements Callback<TreeTableColumn<T, String>, TreeTableCell<T, String>> {

    @Override
    public TreeTableCell<T, String> call(TreeTableColumn<T, String> param) {
        TreeTableCell<T, String> cell = new TreeTableCell<T, String>() {

            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(null);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(serialNumber(getTreeTableView().getTreeItem(getIndex())));
            }

            public String serialNumber(TreeItem item) {
                if (item == null) {
                    return "";
                }
                TreeItem parent = item.getParent();
                if (parent == null) {
                    return "";
                }
                String p = serialNumber(parent);
                return (p == null || p.isBlank() ? "" : p + ".") + (parent.getChildren().indexOf(item) + 1);
            }
        };
        return cell;
    }
}
