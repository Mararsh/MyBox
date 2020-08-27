package mara.mybox.fxml;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.tools.DoubleTools;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:17:47
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TablePercentageCell<T> extends TableCell<T, Double>
        implements Callback<TableColumn<T, Double>, TableCell<T, Double>> {

    @Override
    public TableCell<T, Double> call(TableColumn<T, Double> param) {

        TableCell<T, Double> cell = new TableCell<T, Double>() {
            @Override
            public void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(null);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                if (item != Double.MAX_VALUE) {
                    setText(DoubleTools.scale(item * 100.0d, 2) + "");
                } else {
                    setText(null);
                }
            }
        };
        return cell;
    }
}
