package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.data.GeoCoordinateSystem;

/**
 * @Author Mara
 * @CreateDate 2020-8-14
 * @License Apache License Version 2.0
 */
public class TableCoordinateSystemCell<T> extends TableCell<T, GeoCoordinateSystem>
        implements Callback<TableColumn<T, GeoCoordinateSystem>, TableCell<T, GeoCoordinateSystem>> {

    @Override
    public TableCell<T, GeoCoordinateSystem> call(TableColumn<T, GeoCoordinateSystem> param) {

        TableCell<T, GeoCoordinateSystem> cell = new TableCell<T, GeoCoordinateSystem>() {
            @Override
            public void updateItem(GeoCoordinateSystem item, boolean empty) {
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
