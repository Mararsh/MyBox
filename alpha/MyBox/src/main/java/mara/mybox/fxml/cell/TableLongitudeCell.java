package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * @Author Mara
 * @CreateDate 2020-2-22
 * @License Apache License Version 2.0
 */
public class TableLongitudeCell<T> extends TableCell<T, Double>
        implements Callback<TableColumn<T, Double>, TableCell<T, Double>> {

    @Override
    public TableCell<T, Double> call(TableColumn<T, Double> param) {

        TableCell<T, Double> cell = new TableCell<T, Double>() {
            @Override
            public void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setTextFill(null);
                    return;
                }
                if (item >= -180 && item <= 180) {
                    setText(item + "");
                } else {
                    setText(null);
                }
                setGraphic(null);
            }
        };
        return cell;
    }
}
