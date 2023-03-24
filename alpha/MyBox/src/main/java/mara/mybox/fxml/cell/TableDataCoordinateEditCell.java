package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.controller.ControlData2DLoad;
import mara.mybox.controller.Data2DCoordinatePickerController;
import mara.mybox.db.data.Data2DColumn;

/**
 * @Author Mara
 * @CreateDate 2022-10-8
 * @License Apache License Version 2.0
 */
public class TableDataCoordinateEditCell extends TableDataEditCell {

    public TableDataCoordinateEditCell(ControlData2DLoad dataControl, Data2DColumn dataColumn) {
        super(dataControl, dataColumn);
    }

    @Override
    public void startEditDo() {
        Data2DCoordinatePickerController.open(dataControl, editingRow);
    }

    public static Callback<TableColumn, TableCell> create(ControlData2DLoad dataControl, Data2DColumn dataColumn) {
        return new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableDataCoordinateEditCell(dataControl, dataColumn);
            }
        };
    }

}
