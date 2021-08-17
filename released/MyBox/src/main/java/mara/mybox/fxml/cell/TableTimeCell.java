package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppValues;


/**
 * @Author Mara
 * @CreateDate 2020-7-7
 * @License Apache License Version 2.0
 */
public class TableTimeCell<T> extends TableCell<T, Long>
        implements Callback<TableColumn<T, Long>, TableCell<T, Long>> {

    @Override
    public TableCell<T, Long> call(TableColumn<T, Long> param) {

        TableCell<T, Long> cell = new TableCell<T, Long>() {
            @Override
            public void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == AppValues.InvalidLong) {
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
