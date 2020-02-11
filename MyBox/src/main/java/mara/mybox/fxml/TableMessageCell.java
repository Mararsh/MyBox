package mara.mybox.fxml;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2020-02-08
 * @License Apache License Version 2.0
 */
public class TableMessageCell<T, P> extends TableCell<T, P>
        implements Callback<TableColumn<T, P>, TableCell<T, P>> {

    @Override
    public TableCell<T, P> call(TableColumn<T, P> param) {

        TableCell<T, P> cell = new TableCell<T, P>() {
            @Override
            public void updateItem(P item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(AppVariables.message((String) item));
                setGraphic(null);
            }
        };
        return cell;
    }
}
