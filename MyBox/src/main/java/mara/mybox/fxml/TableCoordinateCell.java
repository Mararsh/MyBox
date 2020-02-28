package mara.mybox.fxml;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * @Author Mara
 * @CreateDate 2020-2-22
 * @License Apache License Version 2.0
 */
public class TableCoordinateCell<T, C> extends TableCell<T, C>
        implements Callback<TableColumn<T, C>, TableCell<T, C>> {

    @Override
    public TableCell<T, C> call(TableColumn<T, C> param) {

        TableCell<T, C> cell = new TableCell<T, C>() {
            @Override
            public void updateItem(C item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setTextFill(null);
                    return;
                }
                Double v = (Double) item;
                if (v >= -180) {
                    setText(v + "");
                } else {
                    setText(null);
                }
                setGraphic(null);
            }
        };
        return cell;
    }
}
