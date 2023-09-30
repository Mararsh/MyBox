package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.controller.BaseData2DColumnsController;
import mara.mybox.controller.Data2DColumnEditController;

/**
 * @Author Mara
 * @CreateDate 2022-10-4
 * @License Apache License Version 2.0
 */
public class TableDataColumnCell<S, T> extends TableAutoCommitCell<S, T> {

    protected BaseData2DColumnsController columnsControl;

    public TableDataColumnCell(BaseData2DColumnsController columnsControl) {
        super(null);
        this.columnsControl = columnsControl;
    }

    @Override
    public void editCell() {
        Data2DColumnEditController.open(columnsControl, editingRow);
    }

    @Override
    public boolean setCellValue(T value) {
        return true;
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> create(BaseData2DColumnsController columnsControl) {
        return new Callback<TableColumn<S, T>, TableCell<S, T>>() {
            @Override
            public TableCell<S, T> call(TableColumn<S, T> param) {
                return new TableDataColumnCell(columnsControl);
            }
        };
    }

}
