package mara.mybox.fxml.cell;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.TextInputController;

/**
 * @Author Mara
 * @CreateDate 2022-9-22
 * @License Apache License Version 2.0
 */
public class TableTextAreaEditCell<S> extends TableStringEditCell<S> {

    protected BaseController parent;
    protected ChangeListener<Boolean> getListener;
    protected String comments;

    public TableTextAreaEditCell(BaseController parent) {
        this(parent, null);
    }

    public TableTextAreaEditCell(BaseController parent, String comments) {
        super();
        this.parent = parent;
        this.comments = comments;
    }

    @Override
    public void startEdit() {
        int row = rowIndex();
        if (row < 0) {
            return;
        }
        TextInputController inputController = TextInputController.open(parent, name(), getCellValue());
        inputController.setCommentsLabel(comments);
        getListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                String value = inputController.getText();
                inputController.getNotify().removeListener(getListener);
                setCellValue(value);
                inputController.closeStage();
            }
        };
        inputController.getNotify().addListener(getListener);
    }

    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>>
            forStringColumn(BaseController parent, String comments) {
        return new Callback<TableColumn<S, String>, TableCell<S, String>>() {
            @Override
            public TableCell<S, String> call(TableColumn<S, String> param) {
                return new TableTextAreaEditCell<>(parent, comments);
            }
        };
    }

}
