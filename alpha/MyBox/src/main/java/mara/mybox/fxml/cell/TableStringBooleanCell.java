package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.fxml.style.StyleTools;

/**
 * @Author Mara
 * @CreateDate 2022-10-1
 * @License Apache License Version 2.0
 */
public class TableStringBooleanCell<T> extends TableCell<T, String>
        implements Callback<TableColumn<T, String>, TableCell<T, String>> {

    @Override
    public TableCell<T, String> call(TableColumn<T, String> param) {
        final ImageView imageview = StyleTools.getIconImage("iconYes.png");
        imageview.setPreserveRatio(true);
        TableCell<T, String> cell = new TableCell<T, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                boolean bValue = ColumnDefinition.string2Boolean(item);
                if (empty || !bValue) {
                    setGraphic(null);
                    return;
                }
                setGraphic(imageview);
            }
        };
        return cell;
    }
}
