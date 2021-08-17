package mara.mybox.fxml.cell;

import javafx.scene.control.ListCell;
import mara.mybox.db.data.GeographyCode;

/**
 * @Author Mara
 * @CreateDate 2020-3-21
 * @License Apache License Version 2.0
 */
public class ListGeographyCodeCell extends ListCell<GeographyCode> {

    @Override
    protected void updateItem(GeographyCode item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            setText(null);
        } else {
            setText(item.getName());
        }
    }

}
