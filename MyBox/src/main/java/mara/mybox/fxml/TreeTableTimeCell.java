package mara.mybox.fxml;

import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.text.Text;
import javafx.util.Callback;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:24:30
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TreeTableTimeCell<T, Long> extends TreeTableCell<T, Long>
        implements Callback<TreeTableColumn<T, Long>, TreeTableCell<T, Long>> {

    @Override
    public TreeTableCell<T, Long> call(TreeTableColumn<T, Long> param) {
        TreeTableCell<T, Long> cell = new TreeTableCell<T, Long>() {
            private final Text text = new Text();

            @Override
            protected void updateItem(final Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || (long) item <= 0) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                text.setText(DateTools.datetimeToString((long) item));
                setGraphic(text);
            }
        };
        return cell;
    }
}
