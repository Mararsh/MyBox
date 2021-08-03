package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-02-08
 * @License Apache License Version 2.0
 */
public class TableMessageCell<T> extends TableCell<T, String>
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
                setText(Languages.message(item));
                setGraphic(null);
            }
        };
        return cell;
    }
}
