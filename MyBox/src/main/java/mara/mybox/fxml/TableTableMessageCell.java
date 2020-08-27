package mara.mybox.fxml;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2020-07-22
 * @License Apache License Version 2.0
 */
public class TableTableMessageCell<T> extends TableCell<T, String>
        implements Callback<TableColumn<T, String>, TableCell<T, String>> {

    @Override
    public TableCell<T, String> call(TableColumn<T, String> param) {

        TableCell<T, String> cell = new TableCell<T, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(AppVariables.tableMessage(item));
                setGraphic(null);
            }
        };
        return cell;
    }
}
