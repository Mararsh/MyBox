package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.IntPoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableRowIndexCell;
import static mara.mybox.value.Languages.message;

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
            yColumn.setCellValueFactory(new PropertyValueFactory<>("y"));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadText(String values) {
        loadList(DoublePoint.parseImageCoordinates(values));
    }

    public void loadList(List<DoublePoint> list) {
        isSettingValues = true;
        if (list == null || list.isEmpty()) {
            tableData.clear();
        } else {
            tableData.setAll(DoublePoint.scaleImageCoordinates(list));
        }
        isSettingValues = false;
    }

    public void loadIntList(List<IntPoint> list) {
        if (list == null || list.isEmpty()) {
            tableData.clear();
            return;
        }
        List<DoublePoint> dlist = new ArrayList<>();
        for (IntPoint p : list) {
            dlist.add(new DoublePoint(p.getX(), p.getY()));
        }
        isSettingValues = true;
        tableData.setAll(dlist);
        isSettingValues = false;
    }

    public void addPoint(DoublePoint point) {
        if (point == null) {
            return;
        }
        addPoint(point.getX(), point.getY());
    }

    public void addPoint(double x, double y) {
        tableData.add(point(x, y));
    }

    public DoublePoint point(double x, double y) {
        return DoublePoint.imageCoordinate(x, y);
    }

    public void setPoint(int index, double x, double y) {
        if (index < 0 || index >= tableData.size()) {
            return;
        }
        tableData.set(index, point(x, y));
    }

    public void deletePoint(int index) {
        if (index < 0 || index >= tableData.size()) {
            return;
        }
        tableData.remove(index);
    }

    public void clear() {
        tableData.clear();
    }

    @FXML
    @Override
    public void addAction() {
        PointInputController inputController = PointInputController.open(this, message("Add"), null);
        inputController.getNotify().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                tableData.add(inputController.picked);
                inputController.close();
            }
        });
    }

    @FXML
    @Override
    public void editAction() {
        try {
            int index = selectedIndix();
            if (index < 0) {
                popError(message("SelectToHandle"));
                return;
            }
            DoublePoint point = tableData.get(index);
            PointInputController inputController = PointInputController.open(this, message("Point") + " " + (index + 1), point);
            inputController.getNotify().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    tableData.set(index, inputController.picked);
                    inputController.close();
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void insertAction() {
        int index = selectedIndix();
        if (index < 0) {
            popError(message("SelectToHandle"));
            return;
        }

    }

}
