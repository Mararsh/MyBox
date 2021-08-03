package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.data.Era;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2020-7-7
 * @License Apache License Version 2.0
 */
public class TableEraCell<T> extends TableCell<T, Era>
        implements Callback<TableColumn<T, Era>, TableCell<T, Era>> {

    @Override
    public TableCell<T, Era> call(TableColumn<T, Era> param) {

        TableCell<T, Era> cell = new TableCell<T, Era>() {
            @Override
            public void updateItem(Era item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(DateTools.textEra(item));
                setGraphic(null);
            }
        };
        return cell;
    }
}
