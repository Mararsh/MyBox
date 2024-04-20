package mara.mybox.fxml.cell;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.controller.BaseData2DTableController;
import mara.mybox.controller.DateInputController;
import mara.mybox.db.data.Data2DColumn;

/**
 * @Author Mara
 * @CreateDate 2022-10-2
 * @License Apache License Version 2.0
 */
public class TableDataDateEditCell extends TableDataEditCell {

    public TableDataDateEditCell(BaseData2DTableController dataControl, Data2DColumn dataColumn) {
        super(dataControl, dataColumn);
    }

    @Override
    public void editCell() {
        DateInputController inputController
                = DateInputController.open(dataTable, name(), getCellValue(), dataColumn.getType());
        inputController.getNotify().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                String value = inputController.getInputString();
                setCellValue(value);
                inputController.close();
            }
        });
    }

    public static Callback<TableColumn, TableCell> create(BaseData2DTableController dataControl, Data2DColumn dataColumn) {
        return new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableDataDateEditCell(dataControl, dataColumn);
            }
        };
    }

}
