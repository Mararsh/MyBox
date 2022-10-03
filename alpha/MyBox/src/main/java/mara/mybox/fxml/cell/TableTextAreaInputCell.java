package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BaseInputController;
import mara.mybox.controller.TextInputController;

/**
 * @Author Mara
 * @CreateDate 2022-9-22
 * @License Apache License Version 2.0
 */
public class TableTextAreaInputCell<S> extends TableStringInputCell<S> {

    public TableTextAreaInputCell(BaseController parent, String comments) {
        super(parent, comments);
    }

    @Override
    public BaseInputController open() {
        return TextInputController.open(parent, name(), getCellValue());
    }

    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>>
            create(BaseController parent, String comments) {
        return new Callback<TableColumn<S, String>, TableCell<S, String>>() {
            @Override
            public TableCell<S, String> call(TableColumn<S, String> param) {
                return new TableTextAreaInputCell<>(parent, comments);
            }
        };
    }

}
