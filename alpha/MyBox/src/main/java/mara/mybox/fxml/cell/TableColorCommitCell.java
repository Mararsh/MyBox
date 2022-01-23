package mara.mybox.fxml.cell;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.ColorPalettePopupController;
import mara.mybox.db.table.TableColor;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.NodeStyleTools;
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
public class TableColorCommitCell<S> extends TableCell<S, Color> {

    protected BaseController parent;
    protected TableColor tableColor;
    protected Rectangle rectangle;
    protected String msgPrefix;

    public TableColorCommitCell(BaseController parent, TableColor tableColor) {
        this.parent = parent;
        this.tableColor = tableColor;
        if (this.tableColor == null) {
            this.tableColor = new TableColor();
        }
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        rectangle = new Rectangle(30, 20);
        rectangle.setStroke(Color.BLACK);
        rectangle.setStrokeWidth(1);
        msgPrefix = message("ClickToEdit");
        this.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                TableView<S> table = getTableView();
                if (table != null) {
                    table.edit(rowIndex(), getTableColumn());
                }
            }
        });
    }

    public int rowIndex() {
        TableRow row = getTableRow();
        return row == null ? -1 : row.getIndex();
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

    @Override
    public void startEdit() {
        super.startEdit();

        Node g = getGraphic();
        if (g == null || !(g instanceof Rectangle)) {
            return;
        }
        setColor((Rectangle) g);
    }

    public void setColor(Rectangle rect) {
        ColorPalettePopupController controller = ColorPalettePopupController.open(parent, rect);
        controller.getSetNotify().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                commitEdit((Color) rect.getFill());
                controller.close();
            }
        });
    }

    public static <S> Callback<TableColumn<S, Color>, TableCell<S, Color>>
            create(BaseController parent, TableColor tableColor) {
        return new Callback<TableColumn<S, Color>, TableCell<S, Color>>() {
            @Override
            public TableCell<S, Color> call(TableColumn<S, Color> param) {
                return new TableColorCommitCell<>(parent, tableColor);
            }
        };
    }

}
