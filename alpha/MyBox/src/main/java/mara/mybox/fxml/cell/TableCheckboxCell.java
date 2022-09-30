package mara.mybox.fxml.cell;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.Callback;

/**
 * @Author Mara
 * @CreateDate 2021-11-13
 * @License Apache License Version 2.0
 */
public class TableCheckboxCell<S, T> extends CheckBoxTableCell<S, T> {

    protected SimpleBooleanProperty checked;
    protected ChangeListener<Boolean> checkedListener;

    public TableCheckboxCell() {
        checked = new SimpleBooleanProperty(false);
        checkedListener = new ChangeListener<Boolean>() {
            @Override
            public synchronized void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                int rowIndex = rowIndex();
                if (rowIndex < 0) {
                    return;
                }
                setCellValue(rowIndex, newValue);
            }
        };
        checked.addListener(checkedListener);

        setSelectedStateCallback(new Callback<Integer, ObservableValue<Boolean>>() {
            @Override
            public synchronized ObservableValue<Boolean> call(Integer index) {
                int rowIndex = rowIndex();
                if (rowIndex < 0) {
                    return null;
                }
                checked.set(getCellValue(rowIndex));
                return checked;
            }
        });
    }

    protected boolean getCellValue(int rowIndex) {
        return false;
    }

    protected void setCellValue(int rowIndex, boolean value) {
    }

    public int rowIndex() {
        try {
            TableRow row = getTableRow();
            if (row == null) {
                return -1;
            }
            int index = row.getIndex();
            if (index >= 0 && index < getTableView().getItems().size()) {
                return index;
            } else {
                return -2;
            }
        } catch (Exception e) {
            return -3;
        }
    }

    public SimpleBooleanProperty getSelected() {
        return checked;
    }

    public void setSelected(SimpleBooleanProperty selected) {
        this.checked = selected;
    }

}
