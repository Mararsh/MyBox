package mara.mybox.controller;

import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableRowIndexCell;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-7-15
 * @License Apache License Version 2.0
 */
public class ControlLines extends BaseTableViewController<List<DoublePoint>> {

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
                        return new SimpleStringProperty(DoublePoint.imageCoordinatesToText(points, " "));
                    } catch (Exception e) {
                        return null;
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadList(List<List<DoublePoint>> list) {
        isSettingValues = true;
        if (list == null || list.isEmpty()) {
            tableData.clear();
        } else {
            tableData.setAll(DoublePoint.scaleLists(list, UserConfig.imageScale()));
        }
        isSettingValues = false;
    }

    @FXML
    @Override
    public void addAction() {
        add(-1);
    }

    @FXML
    public void insertAction() {
        int index = selectedIndix();
        if (index < 0) {
            popError(message("SelectToHandle"));
            return;
        }
        add(index);
    }

    public void add(int index) {
        LineInputController inputController = LineInputController.open(this, message("Add"), null);
        inputController.getNotify().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                List<DoublePoint> line = inputController.picked;
                if (line == null || line.isEmpty()) {
                    popError(message("InvalidValue"));
                    return;
                }
                if (index < 0) {
                    tableData.add(line);
                } else {
                    tableData.add(index, line);
                }
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
            List<DoublePoint> line = tableData.get(index);
            LineInputController inputController = LineInputController.open(this,
                    message("Line") + " " + (index + 1), line);
            inputController.getNotify().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    List<DoublePoint> line = inputController.picked;
                    if (line == null || line.isEmpty()) {
                        popError(message("InvalidValue"));
                        return;
                    }
                    inputController.close();
                    tableData.set(index, line);
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
