package mara.mybox.fxml.cell;

import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import mara.mybox.tools.FileTools;

/**
 * @Author Mara
 * @CreateDate 2019-11-12
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TreeTableFileSizeCell<T> extends TreeTableCell<T, Long>
        implements Callback<TreeTableColumn<T, Long>, TreeTableCell<T, Long>> {

    @Override
    public TreeTableCell<T, Long> call(TreeTableColumn<T, Long> param) {
        TreeTableCell<T, Long> cell = new TreeTableCell<T, Long>() {
            @Override
            protected void updateItem(final Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item <= 0) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(FileTools.showFileSize(item));
                setGraphic(null);
            }
        };
        return cell;
    }
}
