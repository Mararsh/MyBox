package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import mara.mybox.controller.ControlData2DLoad;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;

/**
 * @Author Mara
 * @CreateDate 2022-10-1
 * @License Apache License Version 2.0
 */
public class TableDataBooleanDisplayCell extends TableDataCell {

    protected ImageView imageview;

    public TableDataBooleanDisplayCell(ControlData2DLoad dataControl, Data2DColumn dataColumn) {
        super(dataControl, dataColumn);
        imageview = StyleTools.getIconImage("iconYes.png");
        imageview.setPreserveRatio(true);
    }

    @Override
    public void displayData(String item) {
        setText(null);
        setGraphic(StringTools.isTrue(item) ? imageview : null);
    }

    public static Callback<TableColumn, TableCell>
            create(ControlData2DLoad dataControl, Data2DColumn dataColumn) {
        return new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableDataBooleanDisplayCell(dataControl, dataColumn);
            }
        };
    }
}
