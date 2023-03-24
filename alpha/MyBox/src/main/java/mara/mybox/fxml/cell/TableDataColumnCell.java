package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.controller.ControlData2DColumns;
import mara.mybox.controller.Data2DColumnEditController;

/**
 * @Author Mara
 * @CreateDate 2022-10-4
 * @License Apache License Version 2.0
 */
public class TableDataColumnCell<S, T> extends TableAutoCommitCell<S, T> {

    protected ControlData2DColumns columnsControl;

    public TableDataColumnCell(ControlData2DColumns columnsControl) {
        super(null);
        this.columnsControl = columnsControl;
    }

    @Override
    public void startEditDo() {
        Data2DColumnEditController.open(columnsControl, editingRow);
    }

    @Override
    public void commitEdit(T value) {
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> create(ControlData2DColumns columnsControl) {
        return new Callback<TableColumn<S, T>, TableCell<S, T>>() {
            @Override
            public TableCell<S, T> call(TableColumn<S, T> param) {
                return new TableDataColumnCell(columnsControl);
            }
        };
    }

}
