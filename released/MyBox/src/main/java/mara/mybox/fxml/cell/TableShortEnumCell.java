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
import mara.mybox.controller.BaseData2DTableController;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-26
 * @License Apache License Version 2.0
 */
public class TableShortEnumCell extends ComboBoxTableCell<List<String>, String> {

    protected BaseData2DTableController dataTable;
    protected Data2DColumn dataColumn;
    protected int maxVisibleCount;

    public TableShortEnumCell(BaseData2DTableController dataTable, Data2DColumn dataColumn,
            ObservableList<String> items, int maxCount, boolean editable) {
        super(items);
        setComboBoxEditable(editable);
        maxVisibleCount = maxCount;
        if (maxVisibleCount <= 0) {
            maxVisibleCount = 10;
        }
        this.dataTable = dataTable;
        this.dataColumn = dataColumn;

        setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                TableView<List<String>> table = getTableView();
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
            cb.setValue(getItem());
            if (isComboBoxEditable()) {
                NodeStyleTools.setTooltip(cb.getEditor(), message("EditCellComments"));
            }
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

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setStyle(null);
        if (empty) {
            setText(null);
            setGraphic(null);
            return;
        }
        try {
            setStyle(dataTable.getData2D().cellStyle(dataTable.getStyleFilter(),
                    rowIndex(), dataColumn.getColumnName()));
        } catch (Exception e) {
        }
//        setText(item);
    }

    public static Callback<TableColumn, TableCell> create(
            BaseData2DTableController dataTable, Data2DColumn dataColumn, List<String> items,
            int maxVisibleCount, boolean editable) {
        return new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                ObservableList<String> olist = FXCollections.observableArrayList();
                for (String item : items) {
                    olist.add(item);
                }
                return new TableShortEnumCell(dataTable, dataColumn, olist, maxVisibleCount, editable);
            }
        };
    }

}
