package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.data.Era;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-7-22
 * @License Apache License Version 2.0
 */
public class TableTimeFormatCell<T> extends TableCell<T, Era.Format>
        implements Callback<TableColumn<T, Era.Format>, TableCell<T, Era.Format>> {

    @Override
    public TableCell<T, Era.Format> call(TableColumn<T, Era.Format> param) {

        TableCell<T, Era.Format> cell = new TableCell<T, Era.Format>() {
            @Override
            public void updateItem(Era.Format item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(Languages.message(item.name()));
                setGraphic(null);
            }
        };
        return cell;
    }
}
