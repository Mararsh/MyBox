package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.controller.BaseData2DLoadController;
import mara.mybox.controller.BaseData2DTableController;
import mara.mybox.controller.Data2DCoordinatePickerController;
import mara.mybox.db.data.Data2DColumn;

/**
 * @Author Mara
 * @CreateDate 2022-10-8
 * @License Apache License Version 2.0
 */
public class TableDataCoordinateEditCell extends TableDataEditCell {

    public TableDataCoordinateEditCell(BaseData2DTableController dataTable, Data2DColumn dataColumn) {
        super(dataTable, dataColumn);
    }

    @Override
    public void editCell() {
        Data2DCoordinatePickerController.open(dataTable, editingRow);
    }

    public static Callback<TableColumn, TableCell> create(BaseData2DLoadController dataControl, Data2DColumn dataColumn) {
        return new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableDataCoordinateEditCell(dataControl, dataColumn);
            }
        };
    }

}
