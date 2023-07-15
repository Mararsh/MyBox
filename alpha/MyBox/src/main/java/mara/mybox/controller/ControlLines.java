package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.fxml.cell.TableRowIndexCell;

/**
 * @Author Mara
 * @CreateDate 2023-7-15
 * @License Apache License Version 2.0
 */
public class ControlLines extends BaseTableViewController<List<DoublePoint>> {

    public final int Scale = 2;

    @FXML
    protected TableColumn<List<DoublePoint>, String> indexColumn, pointsColumn;

    @Override
    public void initControls() {
        try {
            super.initControls();

            indexColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<List<DoublePoint>, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<List<DoublePoint>, String> param) {
                    return new SimpleStringProperty("x");
                }
            });
            indexColumn.setCellFactory(new TableRowIndexCell());

            pointsColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<List<DoublePoint>, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<List<DoublePoint>, String> param) {
                    try {
                        List<DoublePoint> points = param.getValue();
                        if (points == null) {
                            return null;
                        }
                        return new SimpleStringProperty(DoublePoint.toText(points, Scale));
                    } catch (Exception e) {
                        return null;
                    }
                }
            });
            pointsColumn.setCellFactory(TableAutoCommitCell.forStringColumn());
            pointsColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<List<DoublePoint>, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<List<DoublePoint>, String> e) {
                    if (e == null) {
                        return;
                    }
                    int row = e.getTablePosition().getRow();
                    if (row < 0) {
                        return;
                    }
                    List<DoublePoint> points = DoublePoint.parseList(e.getNewValue());
                    if (points == null || points.isEmpty()) {
                        return;
                    }
                    tableData.set(row, points);
                }
            });
            pointsColumn.setEditable(true);
            pointsColumn.getStyleClass().add("editable-column");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadList(List<List<DoublePoint>> list) {
        if (list == null || list.isEmpty()) {
            tableData.clear();
        } else {
            tableData.setAll(DoublePoint.scaleLists(list, Scale));
        }
    }

    @FXML
    @Override
    public void addAction() {
        List<DoublePoint> line = new ArrayList<>();
        line.add(new DoublePoint(0, 0));
        tableData.add(line);
    }

}
