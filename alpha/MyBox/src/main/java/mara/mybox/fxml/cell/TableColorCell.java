package mara.mybox.fxml.cell;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;

/**
 * @Author Mara
 * @CreateDate 2020-1-8
 * @License Apache License Version 2.0
 */
public class TableColorCell<T> extends TableCell<T, Color>
        implements Callback<TableColumn<T, Color>, TableCell<T, Color>> {

    @Override
    public TableCell<T, Color> call(TableColumn<T, Color> param) {
        final Rectangle rectangle;
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        rectangle = new Rectangle(30, 20);

        TableCell<T, Color> cell = new TableCell<T, Color>() {

            @Override
            protected void updateItem(Color item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    rectangle.setFill((Paint) item);
                    setGraphic(rectangle);
                }
            }
        };
        return cell;
    }
}
