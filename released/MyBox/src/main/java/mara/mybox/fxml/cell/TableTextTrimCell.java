package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2023-9-18
 * @License Apache License Version 2.0
 */
public class TableTextTrimCell<T> extends TableCell<T, String>
        implements Callback<TableColumn<T, String>, TableCell<T, String>> {

    @Override
    public TableCell<T, String> call(TableColumn<T, String> param) {
        TableCell<T, String> cell = new TableCell<T, String>() {

            @Override
            protected void updateItem(final String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(StringTools.abbreviate(item, AppVariables.titleTrimSize));
                setGraphic(null);
            }
        };
        return cell;
    }
}
