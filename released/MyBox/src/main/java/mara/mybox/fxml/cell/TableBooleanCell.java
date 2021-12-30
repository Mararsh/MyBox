package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import mara.mybox.fxml.StyleTools;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:17:47
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableBooleanCell<T> extends TableCell<T, Boolean>
        implements Callback<TableColumn<T, Boolean>, TableCell<T, Boolean>> {

    @Override
    public TableCell<T, Boolean> call(TableColumn<T, Boolean> param) {
        final ImageView imageview = StyleTools.getIconImage("iconYes.png");
        imageview.setPreserveRatio(true);
        TableCell<T, Boolean> cell = new TableCell<T, Boolean>() {
            @Override
            public void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || !item) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setGraphic(imageview);
                setText(null);
            }
        };
        return cell;
    }
}
