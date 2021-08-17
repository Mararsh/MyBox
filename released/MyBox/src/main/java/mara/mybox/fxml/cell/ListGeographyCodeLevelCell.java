package mara.mybox.fxml.cell;

import javafx.scene.control.ListCell;
import mara.mybox.db.data.GeographyCodeLevel;

/**
 * @Author Mara
 * @CreateDate 2020-3-21
 * @License Apache License Version 2.0
 */
public class ListGeographyCodeLevelCell extends ListCell<GeographyCodeLevel> {

    @Override
    protected void updateItem(GeographyCodeLevel item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            setText(null);
        } else {
            setText(item.getName());
        }
    }

}
