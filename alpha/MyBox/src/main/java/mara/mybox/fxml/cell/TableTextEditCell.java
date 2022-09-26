package mara.mybox.fxml.cell;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.converter.DefaultStringConverter;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.TextInputController;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-26
 * @License Apache License Version 2.0
 */
public class TableTextEditCell<S> extends TableAutoCommitCell<S, String> {

    protected BaseController parent;
    protected boolean isText;
    protected ChangeListener<Boolean> getListener;

    public TableTextEditCell(BaseController parent, boolean isText) {
        super(new DefaultStringConverter());
        this.parent = parent;
        this.isText = isText;
    }

    protected String name() {
        return message("TableRowNumber") + " " + (rowIndex() + 1) + "\n"
                + getTableColumn().getText();
    }

    @Override
    public void startEdit() {
        String s = getItem();
        if (isText && s != null && s.contains("\n")) {
            TextInputController inputController = TextInputController.open(parent, name(), s);
            getListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    String value = inputController.getText();
                    inputController.getNotify().removeListener(getListener);
                    commit(value, valid(value), !s.equals(value));
                    inputController.closeStage();
                }
            };
            inputController.getNotify().addListener(getListener);

        } else {
            super.startEdit();
        }
    }

    @Override
    public void commitEdit(String inValue) {
        try {
            clearEditor();
            String value = inValue == null ? null : inValue.replaceAll("\\\\n", "\n");
            commit(value, valid(value), changed(value));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

}
