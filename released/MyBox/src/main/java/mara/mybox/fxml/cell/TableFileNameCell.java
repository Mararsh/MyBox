package mara.mybox.fxml.cell;

import java.io.File;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * @Author Mara
 * @CreateDate 2021-11-28
 * @License Apache License Version 2.0
 */
public class TableFileNameCell<T> extends TableCell<T, File>
        implements Callback<TableColumn<T, File>, TableCell<T, File>> {

    @Override
    public TableCell<T, File> call(TableColumn<T, File> param) {
        TableCell<T, File> cell = new TableCell<T, File>() {

            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(null);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item.getName());
            }
        };
        return cell;
    }
}
