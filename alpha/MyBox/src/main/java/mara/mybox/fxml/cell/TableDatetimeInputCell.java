package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BaseInputController;
import mara.mybox.controller.DatetimeInputController;
import mara.mybox.db.data.ColumnDefinition.ColumnType;

/**
 * @Author Mara
 * @CreateDate 2022-10-2
 * @License Apache License Version 2.0
 */
public class TableDatetimeInputCell<S> extends TableStringInputCell<S> {

    protected ColumnType timeType;

    public TableDatetimeInputCell(BaseController parent, String comments, ColumnType timeType) {
        super(parent, comments);
        this.timeType = timeType;
    }

    @Override
    public BaseInputController open() {
        return DatetimeInputController.open(parent, name(), getCellValue(), timeType);
    }

    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>>
            create(BaseController parent, String comments, ColumnType timeType) {
        return new Callback<TableColumn<S, String>, TableCell<S, String>>() {
            @Override
            public TableCell<S, String> call(TableColumn<S, String> param) {
                return new TableDatetimeInputCell<>(parent, comments, timeType);
            }
        };
    }

}
