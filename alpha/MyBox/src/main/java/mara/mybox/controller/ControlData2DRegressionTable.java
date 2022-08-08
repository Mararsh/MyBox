package mara.mybox.controller;

import java.util.Comparator;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.StringTools;

/**
 * @Author Mara
 * @CreateDate 2022-4-21
 * @License Apache License Version 2.0
 */
public class ControlData2DRegressionTable extends BaseTableViewController<List<String>> {

    @FXML
    protected TableColumn<List<String>, String> yColumn, xColumn, slopeColumn, interceptColumn,
            rsquareColumn, rColumn, modelColumn;

    @Override
    protected void initColumns() {
        try {
            initColumn(yColumn, 0, false);
            initColumn(xColumn, 1, false);
            initColumn(slopeColumn, 2, true);
            initColumn(interceptColumn, 3, true);
            initColumn(rsquareColumn, 4, true);
            initColumn(rColumn, 5, true);
            initColumn(modelColumn, 6, false);

            tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initColumn(TableColumn column, int index, boolean isDouble) {
        try {
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<List<String>, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<List<String>, String> param) {
                    try {
                        List<String> row = (List<String>) param.getValue();
                        String value = row.get(index);
                        if (value == null) {
                            return null;
                        }
                        return new SimpleStringProperty(value);
                    } catch (Exception e) {
                        return null;
                    }
                }
            });

            column.setComparator(new Comparator<String>() {
                @Override
                public int compare(String v1, String v2) {
                    if (isDouble) {
                        return DoubleTools.compare(v1, v2, true);
                    } else {
                        return StringTools.compare(v1, v2);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void addRow(List<String> row) {
        tableData.add(row);
    }

    public void sortR() {
        tableView.getSortOrder().clear();
        tableView.getSortOrder().add(rColumn);
    }

    public List<String> selected() {
        return tableView.getSelectionModel().getSelectedItem();
    }

}
