package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * @Author Mara
 * @CreateDate 2023-7-8
 * @License Apache License Version 2.0
 */
public class TableRowIndexCell<T> extends TableCell<T, Object>
        implements Callback<TableColumn<T, Object>, TableCell<T, Object>> {

    @Override
    public TableCell<T, Object> call(TableColumn<T, Object> param) {
        TableCell<T, Object> cell = new TableCell<T, Object>() {
            @Override
            public void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(null);
                if (empty || item == null) {
                    return;
                }
                try {
                    int v = getTableRow().getIndex();
                    if (v >= 0) {
                        setText((v + 1) + "");
                    }
                } catch (Exception e) {
                }
            }
        };
        return cell;
    }
}
