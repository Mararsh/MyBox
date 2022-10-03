package mara.mybox.fxml.cell;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.controller.ControlData2DLoad;
import mara.mybox.controller.TextInputController;
import mara.mybox.db.data.Data2DColumn;

/**
 * @Author Mara
 * @CreateDate 2022-9-26
 * @License Apache License Version 2.0
 */
public class TableDataCell extends TableStringEditCell<List<String>> {

    protected ControlData2DLoad dataControl;
    protected Data2DColumn dataColumn;
    protected ChangeListener<Boolean> getListener;
    protected boolean forEdit;
    protected final int trucSize = 200;

    public TableDataCell(ControlData2DLoad dataControl, Data2DColumn dataColumn, boolean forEdit) {
        super();
        this.dataControl = dataControl;
        this.dataColumn = dataColumn;
        this.forEdit = isEdit;
    }

    @Override
    public void startEdit() {
        if (!forEdit) {
            return;
        }
        String s = getItem();
        if (dataColumn.isTextType() && s != null && s.contains("\n")) {
            TextInputController inputController = TextInputController.open(dataControl, name(), s);
            getListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    String value = inputController.getInputString();
                    inputController.getNotify().removeListener(getListener);
                    setCellValue(value);
                    inputController.closeStage();
                }
            };
            inputController.getNotify().addListener(getListener);

        } else {
            super.startEdit();
        }
    }

    @Override
    public void commitEdit(String value) {
        if (!forEdit) {
            return;
        }
        super.commitEdit(value);
    }

    @Override
    public void commit(String value, boolean valid, boolean changed) {
        if (!forEdit) {
            return;
        }
        super.commit(value, valid, changed);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setStyle(null);
        try {
            setStyle(dataControl.getData2D().cellStyle(dataControl.getStyleFilter(),
                    rowIndex(), dataColumn.getColumnName()));
        } catch (Exception e) {
        }
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }
        setText(dataColumn.display(item, trucSize));
    }

    @Override
    public boolean valid(String value) {
        return dataColumn.validValue(value);
    }

    public static Callback<TableColumn, TableCell> create(ControlData2DLoad dataControl,
            Data2DColumn dataColumn, boolean forEdit) {
        return new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableDataCell(dataControl, dataColumn, forEdit);
            }
        };
    }

}
