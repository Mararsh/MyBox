package mara.mybox.fxml.cell;

import java.util.Date;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2020-7-7
 * @License Apache License Version 2.0
 */
public class TableDateCell<T> extends TableCell<T, Date>
        implements Callback<TableColumn<T, Date>, TableCell<T, Date>> {

    @Override
    public TableCell<T, Date> call(TableColumn<T, Date> param) {

        TableCell<T, Date> cell = new TableCell<T, Date>() {
            @Override
            public void updateItem(Date item, boolean empty) {
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
