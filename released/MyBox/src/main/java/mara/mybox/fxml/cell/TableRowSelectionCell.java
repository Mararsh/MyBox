package mara.mybox.fxml.cell;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.Callback;

/**
 * @Author Mara
 * @CreateDate 2021-10-26
 * @License Apache License Version 2.0
 */
public class TableRowSelectionCell<S, T> extends CheckBoxTableCell<S, T> {

    protected TableView tableView;
    protected SimpleBooleanProperty checked;
    protected boolean selectingRow, checkingBox;
    protected ChangeListener<Boolean> checkedListener;
    protected ListChangeListener selectedListener;

    public TableRowSelectionCell(TableView tableView) {
        this.tableView = tableView;
        checked = new SimpleBooleanProperty(false);
        selectingRow = checkingBox = false;
        getStyleClass().add("row-number");

        initListeners();

        setSelectedStateCallback(new Callback<Integer, ObservableValue<Boolean>>() {
            @Override
            public synchronized ObservableValue<Boolean> call(Integer index) {
                if (checked != null) {
                    checked.removeListener(checkedListener);
                    tableView.getSelectionModel().getSelectedIndices().removeListener(selectedListener);
                    checked = null;
                }
                int row = rowIndex();
                if (row < 0) {
                    return null;
                }
                setText("" + (row + 1));
                checked = new SimpleBooleanProperty(tableView.getSelectionModel().isSelected(row));
                checked.addListener(checkedListener);
                tableView.getSelectionModel().getSelectedIndices().addListener(selectedListener);
                return checked;
            }
        });

    }

    private void initListeners() {
        checkedListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                int row = rowIndex();
                if (checkingBox || row < 0) {
                    return;
                }
                selectingRow = true;
                if (newValue) {
                    tableView.getSelectionModel().select(row);
                } else {
                    tableView.getSelectionModel().clearSelection(row);
                }
                selectingRow = false;
            }
        };

        selectedListener = new ListChangeListener<Integer>() {

            @Override
            public void onChanged(ListChangeListener.Change c) {
                int row = rowIndex();
                if (checked == null || selectingRow || row < 0) {
                    return;
                }
                checkingBox = true;
                checked.set(tableView.getSelectionModel().isSelected(row));
                checkingBox = false;
            }
        };
    }

    public int rowIndex() {
        TableRow row = getTableRow();
        return row == null ? -1 : row.getIndex();
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> create(TableView tableView) {
        return new Callback<TableColumn<S, T>, TableCell<S, T>>() {
            @Override
            public TableCell<S, T> call(TableColumn<S, T> param) {
                return new TableRowSelectionCell<>(tableView);
            }
        };
    }

    public SimpleBooleanProperty getSelected() {
        return checked;
    }

    public void setSelected(SimpleBooleanProperty selected) {
        this.checked = selected;
    }

}
