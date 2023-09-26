package mara.mybox.fxml.cell;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BaseInputController;
import mara.mybox.controller.TextInputController;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-9-22
 * @License Apache License Version 2.0
 */
public class TableTextAreaEditCell<S> extends TableAutoCommitCell<S, String> {

    protected BaseController parent;
    protected String comments;

    public TableTextAreaEditCell(BaseController parent, String comments) {
        super(new DefaultStringConverter());
        this.parent = parent;
        this.comments = comments;
    }

    protected String getCellValue() {
        return getItem();
    }

    @Override
    public boolean setCellValue(String inValue) {
        try {
            String value = inValue;
            if (value != null) {
                value = value.replaceAll("\\\\n", "\n");
            }
            return super.setCellValue(value);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @Override
    public void editCell() {
        BaseInputController inputController = TextInputController.open(parent, name(), getCellValue());
        inputController.setCommentsLabel(comments);
        inputController.getNotify().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                setCellValue(inputController.getInputString());
                inputController.close();
            }
        });
    }

    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>>
            create(BaseController parent, String comments) {
        return new Callback<TableColumn<S, String>, TableCell<S, String>>() {
            @Override
            public TableCell<S, String> call(TableColumn<S, String> param) {
                return new TableTextAreaEditCell<>(parent, comments);
            }
        };
    }

}
