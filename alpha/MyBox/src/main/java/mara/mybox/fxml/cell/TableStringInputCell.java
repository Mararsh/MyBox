package mara.mybox.fxml.cell;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BaseInputController;

/**
 * @Author Mara
 * @CreateDate 2022-9-22
 * @License Apache License Version 2.0
 */
public class TableStringInputCell<S> extends TableStringEditCell<S> {

    protected BaseController parent;
    protected ChangeListener<Boolean> getListener;
    protected String comments;

    public TableStringInputCell(BaseController parent) {
        this(parent, null);
    }

    public TableStringInputCell(BaseController parent, String comments) {
        super();
        this.parent = parent;
        this.comments = comments;
    }

    public BaseInputController open() {
        return null;
    }

    @Override
    public void startEdit() {
        int row = rowIndex();
        if (row < 0) {
            return;
        }
        BaseInputController inputController = open();
        inputController.setCommentsLabel(comments);
        getListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                inputController.getNotify().removeListener(getListener);
                setCellValue(inputController.getInputString());
                inputController.closeStage();
            }
        };
        inputController.getNotify().addListener(getListener);
    }

}
