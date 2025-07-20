package mara.mybox.fxml.cell;

import javafx.scene.control.ListCell;
import mara.mybox.data.ImageItem;

/**
 * @Author Mara
 * @CreateDate 2020-1-5
 * @License Apache License Version 2.0
 */
public class ListImageItemCell extends ListCell<ImageItem> {

    protected int height = 60;

    public ListImageItemCell() {
    }

    public ListImageItemCell(int imageSize) {
        this.height = imageSize;
    }

    @Override
    public void updateItem(ImageItem item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);
        if (empty || item == null) {
            setGraphic(null);
            return;
        }
        setGraphic(item.makeNode(height, true));
    }

}
