package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import mara.mybox.controller.BaseData2DTableController;
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

    public TableDataBooleanDisplayCell(BaseData2DTableController dataTable, Data2DColumn dataColumn) {
        super(dataTable, dataColumn);
        imageview = StyleTools.getIconImageView("iconYes.png");
        imageview.setPreserveRatio(true);
    }

    @Override
    public void displayData(String item) {
        setText(null);
        setGraphic(StringTools.isTrue(item) ? imageview : null);
    }

    public static Callback<TableColumn, TableCell>
            create(BaseData2DTableController dataTable, Data2DColumn dataColumn) {
        return new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableDataBooleanDisplayCell(dataTable, dataColumn);
            }
        };
    }
}
