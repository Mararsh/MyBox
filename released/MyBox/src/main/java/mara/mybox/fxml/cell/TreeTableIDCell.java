package mara.mybox.fxml.cell;

import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

/**
 * @Author Mara
 * @CreateDate 2023-4-9
 * @License Apache License Version 2.0
 */
public class TreeTableIDCell<T> extends TreeTableCell<T, Long>
        implements Callback<TreeTableColumn<T, Long>, TreeTableCell<T, Long>> {

    @Override
    public TreeTableCell<T, Long> call(TreeTableColumn<T, Long> param) {
        TreeTableCell<T, Long> cell = new TreeTableCell<T, Long>() {

            @Override
            protected void updateItem(final Long item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(null);
                if (empty || item == null || item < 0) {
                    setText(null);
                    return;
                }
                setText(item + "");
            }
        };
        return cell;
    }
}
