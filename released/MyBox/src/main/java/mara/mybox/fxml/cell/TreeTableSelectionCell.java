package mara.mybox.fxml.cell;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.util.Callback;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-10-26
 * @License Apache License Version 2.0
 */
public class TreeTableSelectionCell<S, T> extends CheckBoxTreeTableCell<S, T> {

    protected TreeTableView treeView;
    protected SimpleBooleanProperty checked;
    protected boolean selectingRow, checkingBox;
    protected ChangeListener<Boolean> checkedListener;
    protected ListChangeListener selectedListener;

    public TreeTableSelectionCell(TreeTableView tView) {
        treeView = tView;
        checked = new SimpleBooleanProperty(false);
        selectingRow = checkingBox = false;
        getStyleClass().add("row-number");

//        treeView.getSelectionModel().getSelectedIndices().addListener(selectedListener);
//        checked.addListener(checkedListener);
        setSelectedStateCallback(new Callback<Integer, ObservableValue<Boolean>>() {
            @Override
            public synchronized ObservableValue<Boolean> call(Integer index) {
                MyBoxLog.console(index);
                if (index < 0) {
                    return null;
                }
                checkingBox = true;
                checked.set(isChecked());
                checkingBox = false;
                return checked;
            }
        });

        selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                MyBoxLog.console(nv + "  " + getTableRow().getIndex());
            }
        });

    }

    public boolean isChecked() {
        return true;
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }
    }

    public static <S, T> Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> create(TreeTableView treeView) {
        return new Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>>() {
            @Override
            public TreeTableCell<S, T> call(TreeTableColumn<S, T> param) {
                return new TreeTableSelectionCell<>(treeView);
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
