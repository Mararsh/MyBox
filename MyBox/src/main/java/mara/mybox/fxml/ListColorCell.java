package mara.mybox.fxml;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:30:16
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ListColorCell extends ListCell<Color> {

    private final Rectangle rectangle;

    public ListColorCell() {
        setContentDisplay(ContentDisplay.LEFT);
        rectangle = new Rectangle(30, 20);
    }

    @Override
    protected void updateItem(Color item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            setText(null);
        } else {
            rectangle.setFill(item);
            setGraphic(rectangle);
            setText(item.toString());
        }
    }

}
