package mara.mybox.fxml.cell;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import mara.mybox.controller.ColorPalettePopupController;
import mara.mybox.controller.ControlData2DLoad;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableColor;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-10-8
 * @License Apache License Version 2.0
 */
public class TableDataColorEditCell extends TableDataEditCell {

    protected TableColor tableColor;
    protected Rectangle rectangle;
    protected String msgPrefix;

    public TableDataColorEditCell(ControlData2DLoad dataControl, Data2DColumn dataColumn, TableColor tableColor) {
        super(dataControl, dataColumn);
        this.tableColor = tableColor;
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        rectangle = new Rectangle(30, 20);
        rectangle.setStroke(Color.BLACK);
        rectangle.setStrokeWidth(1);
        msgPrefix = message("ClickColorToPalette");
    }

    @Override
    public void editCell() {
        Node g = getGraphic();
        if (g == null || !(g instanceof Rectangle)) {
            return;
        }
        ColorPalettePopupController inputController = ColorPalettePopupController.open(dataControl, rectangle);
        inputController.getSetNotify().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                Color color = (Color) rectangle.getFill();
                setCellValue(color.toString());
                inputController.closeStage();
            }
        });
    }

    @Override
    public void displayData(String item) {
        Color color;
        try {
            color = Color.web(item);
        } catch (Exception e) {
            color = Color.WHITE;
        }
        if (tableColor == null) {
            tableColor = new TableColor();
        }
        rectangle.setFill(color);
        NodeStyleTools.setTooltip(rectangle, msgPrefix + "\n---------\n"
                + FxColorTools.colorNameDisplay(tableColor, color));
        setGraphic(rectangle);
    }

    public static Callback<TableColumn, TableCell> create(ControlData2DLoad dataControl,
            Data2DColumn dataColumn, TableColor tableColor) {
        return new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableDataColorEditCell(dataControl, dataColumn, tableColor);
            }
        };
    }

}
