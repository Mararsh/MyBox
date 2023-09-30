package mara.mybox.fxml.cell;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.ColorPalettePopupController;
import mara.mybox.db.table.TableColor;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;

/**
 * Reference:
 * https://stackoverflow.com/questions/24694616/how-to-enable-commit-on-focuslost-for-tableview-treetableview
 * By Ogmios
 *
 * @Author Mara
 * @CreateDate 2020-12-03
 * @License Apache License Version 2.0
 */
public class TableColorEditCell<S> extends TableCell<S, Color> {

    protected BaseController parent;
    protected TableColor tableColor;
    protected Rectangle rectangle;
    protected String msgPrefix;

    public TableColorEditCell(BaseController parent, TableColor tableColor) {
        this.parent = parent;
        this.tableColor = tableColor;
        if (this.tableColor == null) {
            this.tableColor = new TableColor();
        }
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        rectangle = new Rectangle(30, 20);
        rectangle.setStroke(Color.BLACK);
        rectangle.setStrokeWidth(1);
        msgPrefix = message("ClickColorToPalette");
        this.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                setColor();
            }
        });
    }

    @Override
    protected void updateItem(Color item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
            return;
        }
        if (item == null) {
            rectangle.setFill(Color.WHITE);
            NodeStyleTools.setTooltip(rectangle, msgPrefix);
        } else {
            rectangle.setFill((Paint) item);
            NodeStyleTools.setTooltip(rectangle, msgPrefix + "\n---------\n"
                    + FxColorTools.colorNameDisplay(tableColor, item));
        }
        setGraphic(rectangle);
    }

    public void setColor() {
        Node g = getGraphic();
        if (g == null || !(g instanceof Rectangle)) {
            return;
        }
        ColorPalettePopupController controller = ColorPalettePopupController.open(parent, rectangle);
        controller.getSetNotify().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                if (controller == null) {
                    return;
                }
                colorChanged(getIndex(), (Color) rectangle.getFill());
                controller.close();
            }
        });
    }

    public void colorChanged(int index, Color color) {
    }

}
