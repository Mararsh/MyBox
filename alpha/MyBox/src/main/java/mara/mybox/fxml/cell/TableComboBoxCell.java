package mara.mybox.fxml.cell;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

/**
 * @Author Mara
 * @CreateDate 2021-10-26
 * @License Apache License Version 2.0
 */
public class TableComboBoxCell<S, T> extends ComboBoxTableCell<S, T> {

    protected int maxVisibleCount;

    public TableComboBoxCell(ObservableList<T> items, int maxCount) {
        super(items);
        this.maxVisibleCount = maxCount;
        if (maxVisibleCount <= 0) {
            maxVisibleCount = 10;
        }

        this.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                TableView<S> table = getTableView();
                if (table != null && table.getItems() != null) {
                    int index = rowIndex();
                    if (index < table.getItems().size()) {
                        table.edit(index, getTableColumn());
                    }
                }
            }
        });
    }

    @Override
    public void startEdit() {
        super.startEdit();
        Node g = getGraphic();
        if (g != null && g instanceof ComboBox) {
            ComboBox cb = (ComboBox) g;
            cb.setVisibleRowCount(maxVisibleCount);
        }
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

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> create(List<T> items, int maxVisibleCount) {
        return new Callback<TableColumn<S, T>, TableCell<S, T>>() {
            @Override
            public TableCell<S, T> call(TableColumn<S, T> param) {
                ObservableList<T> olist = FXCollections.observableArrayList();
                for (T item : items) {
                    olist.add(item);
                }
                return new TableComboBoxCell<>(olist, maxVisibleCount);
            }
        };
    }

}
