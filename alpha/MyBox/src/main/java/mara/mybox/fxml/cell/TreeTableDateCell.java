package mara.mybox.fxml.cell;

import java.util.Date;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2023-4-9
 * @License Apache License Version 2.0
 */
public class TreeTableDateCell<T> extends TreeTableCell<T, Date>
        implements Callback<TreeTableColumn<T, Date>, TreeTableCell<T, Date>> {

    @Override
    public TreeTableCell<T, Date> call(TreeTableColumn<T, Date> param) {
        TreeTableCell<T, Date> cell = new TreeTableCell<T, Date>() {

            @Override
            protected void updateItem(final Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
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
