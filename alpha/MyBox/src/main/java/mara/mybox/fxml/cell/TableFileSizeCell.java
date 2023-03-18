package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Text;
import javafx.util.Callback;
import mara.mybox.tools.FileTools;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:24:30
 * @License Apache License Version 2.0
 */
public class TableFileSizeCell<T> extends TableCell<T, Long>
        implements Callback<TableColumn<T, Long>, TableCell<T, Long>> {

    @Override
    public TableCell<T, Long> call(TableColumn<T, Long> param) {
        TableCell<T, Long> cell = new TableCell<T, Long>() {
            private final Text text = new Text();

            @Override
            protected void updateItem(final Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item <= 0) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                text.setText(FileTools.showFileSize(item));
                setGraphic(text);
            }
        };
        return cell;
    }
}
