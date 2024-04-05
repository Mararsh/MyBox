package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Toggle;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-13
 * @License Apache License Version 2.0
 */
public class Data2DChartXYZController extends BaseData2DTaskController {

    protected int seriesSize;
    protected File chartFile;

    @FXML
    protected ControlChartXYZ chartController;
    @FXML
    protected VBox zBox, columnsBox;
    @FXML
    protected FlowPane zColumnPane, zlabelPane;
    @FXML
    protected ComboBox<String> xSelector, ySelector, zSelector;
    @FXML
    protected CheckBox xCategoryCheck, yCategoryCheck, zCategoryCheck;

    public Data2DChartXYZController() {
        baseTitle = message("XYZChart");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            chartController.typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    typeChanged();
                }
            });
            typeChanged();

            xSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    adjustColumnsPane();
                }
            });
            ySelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    adjustColumnsPane();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void typeChanged() {
        zBox.getChildren().clear();
        if (chartController.scatterRadio.isSelected()) {
            if (!zlabelPane.getChildren().contains(zCategoryCheck)) {
                zlabelPane.getChildren().add(zCategoryCheck);
            }
            zBox.getChildren().add(columnsBox);
        } else {
            if (!zColumnPane.getChildren().contains(zCategoryCheck)) {
                zColumnPane.getChildren().add(zCategoryCheck);
            }
            zBox.getChildren().add(zColumnPane);
        }
    }

    @Override
    public void sourceChanged() {
        try {
            super.sourceChanged();
            isSettingValues = true;
            xSelector.getItems().clear();
            ySelector.getItems().clear();
            zSelector.getItems().clear();
            isSettingValues = false;
            List<String> names = data2D.columnNames();
            if (names == null || names.isEmpty()) {
                return;
            }
            isSettingValues = true;
            String xCol = xSelector.getSelectionModel().getSelectedItem();
            xSelector.getItems().setAll(names);
            if (xCol != null && names.contains(xCol)) {
                xSelector.setValue(xCol);
            } else {
                xSelector.getSelectionModel().select(0);
            }
            String yCol = ySelector.getSelectionModel().getSelectedItem();
            ySelector.getItems().setAll(names);
            if (yCol != null && names.contains(yCol)) {
                ySelector.setValue(yCol);
            } else {
                ySelector.getSelectionModel().select(names.size() > 1 ? 1 : 0);
            }
            isSettingValues = false;
            adjustColumnsPane();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void adjustColumnsPane() {
        try {
            if (isSettingValues) {
                return;
            }
            isSettingValues = true;
            columnsPane.getChildren().clear();
            isSettingValues = false;
            List<String> names = data2D.columnNames();
            if (names == null) {
                return;
            }
            String xName = xSelector.getValue();
            String yName = ySelector.getValue();
            names.remove(xName);
            names.remove(yName);
            isSettingValues = true;
            for (String name : names) {
                columnsPane.getChildren().add(new CheckBox(name));
            }
            String zCol = zSelector.getSelectionModel().getSelectedItem();
            zSelector.getItems().setAll(names);
            if (zCol != null && names.contains(zCol)) {
                zSelector.setValue(zCol);
            } else {
                zSelector.getSelectionModel().select(0);
            }
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        try {
            if (!super.checkOptions()) {
                return false;
            }
            if (!chartController.checkParameters()) {
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            invalidAs = InvalidAs.Skip;
            dataColsIndices = new ArrayList<>();
            outputColumns = new ArrayList<>();
            String xName = xSelector.getSelectionModel().getSelectedItem();
            int xCol = data2D.colOrder(xName);
            if (xCol < 0) {
                popError(message("SelectToHandle") + ": " + message("AxisX"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            outputColumns.add(data2D.column(xCol));
            dataColsIndices.add(xCol);

            String yName = ySelector.getSelectionModel().getSelectedItem();
            int yCol = data2D.colOrder(yName);
            if (yCol < 0) {
                popError(message("SelectToHandle") + ": " + message("AxisY"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            outputColumns.add(data2D.column(yCol));
            dataColsIndices.add(yCol);

            if (chartController.scatterRadio.isSelected()) {
                if (checkedColsIndices == null || checkedColsIndices.isEmpty()) {
                    popError(message("SelectToHandle") + ": " + message("AxisZ"));
                    tabPane.getSelectionModel().select(optionsTab);
                    return false;
                }
                dataColsIndices.addAll(checkedColsIndices);
                outputColumns.addAll(checkedColumns);
                seriesSize = checkedColsIndices.size();
            } else {
                String zName = zSelector.getSelectionModel().getSelectedItem();
                int zCol = data2D.colOrder(zName);
                if (zCol < 0) {
                    popError(message("SelectToHandle") + ": " + message("AxisZ"));
                    tabPane.getSelectionModel().select(optionsTab);
                    return false;
                }
                outputColumns.add(data2D.column(zCol));
                dataColsIndices.add(zCol);
                seriesSize = 1;
            }
            if (otherColsIndices != null) {
                for (int c : otherColsIndices) {
                    if (!dataColsIndices.contains(c)) {
                        dataColsIndices.add(c);
                        outputColumns.add(data2D.column(c));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(this);
                    chartFile = null;
                    outputData = filteredData(dataColsIndices, false);
                    if (outputData == null || outputData.isEmpty()) {
                        error = message("NoData");
                        return false;
                    }
                    chartFile = chartController.makeChart(outputColumns, outputData,
                            seriesSize, data2D.dataName(), scale,
                            xCategoryCheck.isSelected(), yCategoryCheck.isSelected(), zCategoryCheck.isSelected());
                    return chartFile != null && chartFile.exists();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                closeTask();
                if (!ok) {
                    return;
                }
                browse(chartFile);
                browse(chartFile.getParentFile());
                if (closeAfterCheck.isSelected()) {
                    close();
                }
            }

        };
        start(task, false);
    }

    /*
        static
     */
    public static Data2DChartXYZController open(BaseData2DLoadController tableController) {
        try {
            Data2DChartXYZController controller = (Data2DChartXYZController) WindowTools.branchStage(
                    tableController, Fxmls.Data2DChartXYZFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
