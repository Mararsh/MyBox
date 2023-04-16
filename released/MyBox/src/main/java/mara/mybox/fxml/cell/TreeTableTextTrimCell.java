package mara.mybox.fxml.cell;

import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import mara.mybox.tools.StringTools;

/**
 * @Author Mara
 * @CreateDate 2023-4-9
 * @License Apache License Version 2.0
 */
public class TreeTableTextTrimCell<T> extends TreeTableCell<T, String>
        implements Callback<TreeTableColumn<T, String>, TreeTableCell<T, String>> {

    @Override
    public TreeTableCell<T, String> call(TreeTableColumn<T, String> param) {
        TreeTableCell<T, String> cell = new TreeTableCell<T, String>() {

            @Override
            protected void updateItem(final String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(StringTools.abbreviate(item, 60));
                setGraphic(null);
            }
        };
        return cell;
    }
}
