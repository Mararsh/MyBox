package mara.mybox.fxml.cell;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * @Author Mara
 * @CreateDate 2021-10-26
 * @License Apache License Version 2.0
 */
public class TableComboBoxCell<S, T> extends ComboBoxTableCell<S, T> {

    public TableComboBoxCell(StringConverter<T> converter, ObservableList<T> items) {
        super(converter, items);
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

    public int rowIndex() {
        TableRow row = getTableRow();
        return row == null ? -1 : row.getIndex();
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(final T... items) {
        return forTableColumn(null, items);
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(
            final StringConverter<T> converter, final T... items) {
        return forTableColumn(converter, FXCollections.observableArrayList(items));
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(final ObservableList<T> items) {
        return forTableColumn(null, items);
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(
            final StringConverter<T> converter, final ObservableList<T> items) {
        return list -> new TableComboBoxCell<S, T>(converter, items);
    }

}
