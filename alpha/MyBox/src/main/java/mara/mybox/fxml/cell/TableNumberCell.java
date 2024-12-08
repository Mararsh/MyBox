package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Text;
import javafx.util.Callback;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:24:30
 * @License Apache License Version 2.0
 */
public class TableNumberCell<T> extends TableCell<T, Long>
        implements Callback<TableColumn<T, Long>, TableCell<T, Long>> {

    private boolean notPermitNegative = false;

    public TableNumberCell(boolean notPermitNegative) {
        this.notPermitNegative = notPermitNegative;
    }

    @Override
    public TableCell<T, Long> call(TableColumn<T, Long> param) {
        TableCell<T, Long> cell = new TableCell<T, Long>() {
            private final Text text = new Text();

            @Override
            protected void updateItem(final Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null
                        || item == AppValues.InvalidLong
                        || (notPermitNegative && item < 0)) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                text.setText(StringTools.format(item));
                setGraphic(text);
            }
        };
        return cell;
    }
}
