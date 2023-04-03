package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.controller.ControlData2DLoad;
import mara.mybox.db.data.Data2DColumn;

/**
 * @Author Mara
 * @CreateDate 2022-9-26
 * @License Apache License Version 2.0
 */
public class TableDataDisplayCell extends TableDataCell {

    public TableDataDisplayCell(ControlData2DLoad dataControl, Data2DColumn dataColumn) {
        super(dataControl, dataColumn);
    }

    @Override
    public void startEdit() {
    }

    @Override
    public void commitEdit(String inValue) {
    }

    @Override
    public boolean commit(String value) {
        return true;
    }

    public static Callback<TableColumn, TableCell> create(ControlData2DLoad dataControl, Data2DColumn dataColumn) {
        return new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableDataDisplayCell(dataControl, dataColumn);
            }
        };
    }

}
