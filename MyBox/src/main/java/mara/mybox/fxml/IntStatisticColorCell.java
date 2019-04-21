package mara.mybox.fxml;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import mara.mybox.data.IntStatistic;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:24:30
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class IntStatisticColorCell extends TableCell<IntStatistic, Integer> {

    private final Rectangle rectangle;
    private Color color;

    public IntStatisticColorCell() {
        setContentDisplay(ContentDisplay.LEFT);
        rectangle = new Rectangle(30, 20);
    }

    @Override
    protected void updateItem(final Integer item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || item < 0 || empty) {
            setGraphic(null);
        } else {
            IntStatistic row = getTableView().getItems().get(getTableRow().getIndex());

            switch (row.getName()) {
                case "Red":
                    color = new Color(item / 255.0, 0, 0, 1);
                    break;
                case "Green":
                    color = new Color(0, item / 255.0, 0, 1);
                    break;
                case "Blue":
                    color = new Color(0, 0, item / 255.0, 1);
                    break;
                case "Alpha":
                    color = new Color(1, 1, 1, item / 255.0);
                    break;
                case "Grey":
                case "Gray":
                    double c = item / 255.0;
                    color = new Color(c, c, c, 1);
                    break;
                case "Hue":
                    color = Color.hsb(item, 1, 1);
                    break;
                case "Saturation":
                    color = Color.hsb(66, item / 100.0, 1);
                    break;
                case "Brightness":
                    color = Color.hsb(66, 1, item / 100.0);
                    break;
                default:
                    color = null;
                    break;
            }
            if (color != null) {
                rectangle.setFill(color);
                setGraphic(rectangle);
            }
            setText(item + "");

        }
    }
}
