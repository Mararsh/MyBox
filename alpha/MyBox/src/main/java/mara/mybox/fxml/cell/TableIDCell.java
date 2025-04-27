package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * @Author Mara
 * @CreateDate 2025-4-25
 * @License Apache License Version 2.0
 */
public class TableIDCell<T> extends TableCell<T, Long>
        implements Callback<TableColumn<T, Long>, TableCell<T, Long>> {

    @Override
    public TableCell<T, Long> call(TableColumn<T, Long> param) {
        TableCell<T, Long> cell = new TableCell<T, Long>() {

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
