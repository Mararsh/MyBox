package mara.mybox.fxml;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:17:47
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableDoubleCell<T, C> extends TableCell<T, C>
        implements Callback<TableColumn<T, C>, TableCell<T, C>> {

    @Override
    public TableCell<T, C> call(TableColumn<T, C> param) {

        TableCell<T, C> cell = new TableCell<T, C>() {
            @Override
            public void updateItem(C item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(null);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                Double v = (Double) item;
                if (v > Double.MIN_VALUE) {
                    setText(v + "");
                } else {
                    setText(null);
                }
            }
        };
        return cell;
    }
}
