package mara.mybox.fxml;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.data.CoordinateSystem;

/**
 * @Author Mara
 * @CreateDate 2020-8-14
 * @License Apache License Version 2.0
 */
public class TableCoordinateSystemCell<T> extends TableCell<T, CoordinateSystem>
        implements Callback<TableColumn<T, CoordinateSystem>, TableCell<T, CoordinateSystem>> {

    @Override
    public TableCell<T, CoordinateSystem> call(TableColumn<T, CoordinateSystem> param) {

        TableCell<T, CoordinateSystem> cell = new TableCell<T, CoordinateSystem>() {
            @Override
            public void updateItem(CoordinateSystem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setTextFill(null);
                    return;
                }
                setText(item.name() + "");
                setGraphic(null);
            }
        };
        return cell;
    }
}
