package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.controller.BaseData2DTableController;
import mara.mybox.db.data.Data2DColumn;

/**
 * @Author Mara
 * @CreateDate 2022-9-26
 * @License Apache License Version 2.0
 */
public class TableDataDisplayCell extends TableDataCell {

    public TableDataDisplayCell(BaseData2DTableController dataTable, Data2DColumn dataColumn) {
        super(dataTable, dataColumn);
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

    public static Callback<TableColumn, TableCell> create(BaseData2DTableController dataTable, Data2DColumn dataColumn) {
        return new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableDataDisplayCell(dataTable, dataColumn);
            }
        };
    }

}
