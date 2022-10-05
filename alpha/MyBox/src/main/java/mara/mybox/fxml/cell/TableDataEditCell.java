package mara.mybox.fxml.cell;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.controller.ControlData2DLoad;
import mara.mybox.controller.TextInputController;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-9-26
 * @License Apache License Version 2.0
 */
public class TableDataEditCell extends TableDataCell {

    protected ChangeListener<Boolean> getListener;

    public TableDataEditCell(ControlData2DLoad dataControl, Data2DColumn dataColumn) {
        super(dataControl, dataColumn);
    }

    @Override
    public void startEdit() {
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
    public void commitEdit(String inValue) {
        try {
            clearEditor();
            setCellValue(inValue);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public static Callback<TableColumn, TableCell> create(ControlData2DLoad dataControl, Data2DColumn dataColumn) {
        return new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableDataEditCell(dataControl, dataColumn);
            }
        };
    }

}
