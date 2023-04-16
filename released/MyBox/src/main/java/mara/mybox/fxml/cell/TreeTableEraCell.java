package mara.mybox.fxml.cell;

import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:24:30
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TreeTableEraCell<T> extends TreeTableCell<T, Long>
        implements Callback<TreeTableColumn<T, Long>, TreeTableCell<T, Long>> {

    @Override
    public TreeTableCell<T, Long> call(TreeTableColumn<T, Long> param) {
        TreeTableCell<T, Long> cell = new TreeTableCell<T, Long>() {

            @Override
            protected void updateItem(final Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == AppValues.InvalidLong) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(DateTools.textEra(item));
                setGraphic(null);
            }
        };
        return cell;
    }
}
