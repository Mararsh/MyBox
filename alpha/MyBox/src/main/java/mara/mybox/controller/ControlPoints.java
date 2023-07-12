package mara.mybox.controller;

import java.util.List;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.fxml.cell.TableRowIndexCell;

/**
 * @Author Mara
 * @CreateDate 2023-7-7
 * @License Apache License Version 2.0
 */
public class ControlPoints extends BaseTableViewController<DoublePoint> {

    @FXML
    protected TableColumn<DoublePoint, Double> indexColumn, xColumn, yColumn;

    @Override
    public void initControls() {
        try {
            super.initControls();

            indexColumn.setCellValueFactory(new PropertyValueFactory<>("x"));
            indexColumn.setCellFactory(new TableRowIndexCell());

            xColumn.setCellValueFactory(new PropertyValueFactory<>("x"));
            xColumn.setCellFactory(TableAutoCommitCell.forDoubleColumn());
            xColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<DoublePoint, Double>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<DoublePoint, Double> e) {
                    if (e == null) {
                        return;
                    }
                    int row = e.getTablePosition().getRow();
                    if (row < 0) {
                        return;
                    }
                    DoublePoint point = tableData.get(row);
                    point.setX(e.getNewValue());
                }
            });
            xColumn.getStyleClass().add("editable-column");

            yColumn.setCellValueFactory(new PropertyValueFactory<>("y"));
            yColumn.setCellFactory(TableAutoCommitCell.forDoubleColumn());
            yColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<DoublePoint, Double>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<DoublePoint, Double> e) {
                    if (e == null) {
                        return;
                    }
                    int row = e.getTablePosition().getRow();
                    if (row < 0) {
                        return;
                    }
                    DoublePoint point = tableData.get(row);
                    point.setY(e.getNewValue());
                }
            });
            yColumn.getStyleClass().add("editable-column");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadText(String values) {
        List<DoublePoint> list = DoublePoint.parseList(values, DoublePoint.Separator);
        if (list == null || list.isEmpty()) {
            tableData.clear();
        } else {
            tableData.setAll(list);
        }
    }

    public void loadList(List<DoublePoint> list) {
        if (list == null || list.isEmpty()) {
            tableData.clear();
        } else {
            tableData.setAll(list);
        }
    }

    public String toText() {
        return DoublePoint.toText(tableData, 2);
    }

    @FXML
    @Override
    public void addAction() {
        tableData.add(new DoublePoint(0, 0));
    }

}
