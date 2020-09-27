package mara.mybox.fxml;

import javafx.scene.control.ListCell;
import mara.mybox.data.Dataset;

/**
 * @Author Mara
 * @CreateDate 2020-9-13
 * @License Apache License Version 2.0
 */
public class ListDatasetCell extends ListCell<Dataset> {

    @Override
    protected void updateItem(Dataset item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && item != null) {
            setText(item.getDataSet());
            setGraphic(null);
        } else {
            setGraphic(null);
            setText(null);
        }
    }
}
