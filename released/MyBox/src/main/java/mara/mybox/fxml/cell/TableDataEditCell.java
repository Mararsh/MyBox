package mara.mybox.fxml.cell;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.controller.BaseData2DTableController;
import mara.mybox.controller.TextInputController;
import mara.mybox.db.data.Data2DColumn;

/**
 * @Author Mara
 * @CreateDate 2022-9-26
 * @License Apache License Version 2.0
 */
public class TableDataEditCell extends TableDataCell {

    public TableDataEditCell(BaseData2DTableController dataTable, Data2DColumn dataColumn) {
        super(dataTable, dataColumn);
    }

    @Override
    public void editCell() {
        String s = getItem();
        if (supportMultipleLine && s != null && s.contains("\n")) {
            TextInputController inputController = TextInputController.open(dataTable, name(), s);
            inputController.getNotify().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    String value = inputController.getInputString();
                    setCellValue(value);
                    inputController.close();
                }
            });

        } else {
            super.editCell();
        }
    }

    public static Callback<TableColumn, TableCell> create(BaseData2DTableController dataTable, Data2DColumn dataColumn) {
        return new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableDataEditCell(dataTable, dataColumn);
            }
        };
    }

}
