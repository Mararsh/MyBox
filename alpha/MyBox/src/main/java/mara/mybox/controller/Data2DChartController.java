package mara.mybox.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.ChartTools;
import mara.mybox.fxml.chart.LabeledBarChart;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public class Data2DChartController extends Data2DHandleController {

    protected String selectedCategory;
    protected ChartTools.LabelType labelType;
    protected ChartTools.ChartCoordinate chartCoordinate;
    protected int labelFontSize;
    protected Side legendSide;
    protected LabeledBarChart barChart;

    @FXML
    protected RadioButton barChartRadio, stackedBarChartRadio, lineChartRadio, scatterChartRadio,
            pieRadio, bubbleChartRadio, areaChartRadio, stackedAreaChartRadio,
            cartesianRadio, logarithmicERadio, logarithmic10Radio, squareRootRadio;
    @FXML
    protected ComboBox<String> categorySelector;
    @FXML
    protected VBox chartBox;
    @FXML
    protected CheckBox categoryAxisCheck, hlinesCheck, vlinesCheck, xyReverseCheck;
    @FXML
    protected ToggleGroup chartGroup, labelGroup, legendGroup, numberCoordinateGroup;
    @FXML
    protected ComboBox<String> labelSizeSelector;

    @Override
    public void initControls() {
        try {
            super.initControls();

            categorySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkOptions();
                }
            });

            chartCoordinate = ChartTools.ChartCoordinate.Cartesian;
            numberCoordinateGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        if (logarithmicERadio.isSelected()) {
                            chartCoordinate = ChartTools.ChartCoordinate.LogarithmicE;
                        } else if (logarithmic10Radio.isSelected()) {
                            chartCoordinate = ChartTools.ChartCoordinate.Logarithmic10;
                        } else if (squareRootRadio.isSelected()) {
                            chartCoordinate = ChartTools.ChartCoordinate.SquareRoot;
                        } else {
                            chartCoordinate = ChartTools.ChartCoordinate.Cartesian;
                        }
                        barChart.setChartCoordinate(chartCoordinate);
                        makeChart();
                        okAction();
                    });

            labelType = ChartTools.LabelType.NameAndValue;
            labelGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                if (isSettingValues || newValue == null) {
                    return;
                }
                String value = ((RadioButton) newValue).getText();
                if (message("NameAndValue").equals(value)) {
                    labelType = ChartTools.LabelType.NameAndValue;
                } else if (message("Value").equals(value)) {
                    labelType = ChartTools.LabelType.Value;
                } else if (message("Name").equals(value)) {
                    labelType = ChartTools.LabelType.Name;
                } else if (message("NotDisplay").equals(value)) {
                    labelType = ChartTools.LabelType.NotDisplay;
                } else if (message("Pop").equals(value)) {
                    labelType = ChartTools.LabelType.Pop;
                } else {
                    labelType = ChartTools.LabelType.NameAndValue;
                }
                barChart.setLabelType(labelType);
                makeChart();
                okAction();
            });

            labelFontSize = UserConfig.getInt(baseName + "ChartTextSize", 12);
            if (labelFontSize < 0) {
                labelFontSize = 12;
            }
            labelSizeSelector.getItems().addAll(Arrays.asList(
                    "12", "14", "10", "15", "16", "18", "9", "8", "18", "20", "24"
            ));
            labelSizeSelector.getSelectionModel().select(labelFontSize + "");
            labelSizeSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v > 0) {
                        labelFontSize = v;
                        labelSizeSelector.getEditor().setStyle(null);
                        UserConfig.setInt(baseName + "ChartTextSize", labelFontSize);
                        barChart.setTextSize(labelFontSize);
                        okAction();
                    } else {
                        labelSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                } catch (Exception e) {
                    labelSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                }
            });

            legendSide = Side.TOP;
            legendGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        String value = ((RadioButton) newValue).getText();
                        if (message("NotDisplay").equals(value)) {
                            legendSide = null;
                        } else if (message("Left").equals(value)) {
                            legendSide = Side.LEFT;
                        } else if (message("Top").equals(value)) {
                            legendSide = Side.TOP;
                        } else if (message("Bottom").equals(value)) {
                            legendSide = Side.BOTTOM;
                        } else {
                            legendSide = Side.RIGHT;
                        }
                        if (legendSide == null) {
                            barChart.setLegendVisible(false);
                        } else {
                            barChart.setLegendVisible(true);
                            barChart.setLegendSide(legendSide);
                        }
                    });

            xyReverseCheck.setSelected(UserConfig.getBoolean(baseName + "YX", false));
            xyReverseCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "YX", xyReverseCheck.isSelected());
                makeChart();
                okAction();
            });

            categoryAxisCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayCategoryAxis", true));
            categoryAxisCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayCategoryAxis", categoryAxisCheck.isSelected());
                barChart.displayCategoryAxis(categoryAxisCheck.isSelected());
            });

            hlinesCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayHlines", false));
            hlinesCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayHlines", hlinesCheck.isSelected());
                barChart.setHorizontalGridLinesVisible(hlinesCheck.isSelected());
            });

            vlinesCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayVlines", false));
            vlinesCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayVlines", vlinesCheck.isSelected());
                barChart.setVerticalGridLinesVisible(vlinesCheck.isSelected());
            });

            chartGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        makeChart();
                        okAction();
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setParameters(ControlData2DEditTable tableController) {
        try {
            super.setParameters(tableController);

            tableController.statusNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    refreshXY();
                }
            });

            refreshXY();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void refreshXY() {
        try {
            categorySelector.getItems().clear();

            List<String> names = tableController.data2D.columnNames();
            if (names == null || names.isEmpty()) {
                return;
            }
            selectedCategory = categorySelector.getSelectionModel().getSelectedItem();
            categorySelector.getItems().setAll(names);
            if (selectedCategory != null && names.contains(selectedCategory)) {
                categorySelector.setValue(selectedCategory);
            } else {
                categorySelector.getSelectionModel().select(0);
            }
            okAction();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        boolean ok = super.checkOptions();
        selectedCategory = categorySelector.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            infoLabel.setText(message("SelectToHandle"));
            okButton.setDisable(true);
            return false;
        }
        return ok;
    }

    public void makeChart() {
        try {
            chartBox.getChildren().clear();
            if (xyReverseCheck.isSelected()) {
                barChart = LabeledBarChart.yx(categoryAxisCheck.isSelected(), chartCoordinate);
            } else {
                barChart = LabeledBarChart.xy(categoryAxisCheck.isSelected(), chartCoordinate);
            }
            barChart.setIntValue(false).setLabelType(labelType).setTextSize(labelFontSize);
            barChart.setAlternativeRowFillVisible(false);
            barChart.setAlternativeColumnFillVisible(false);
            barChart.setBarGap(0.0);
            barChart.setCategoryGap(0.0);
            barChart.setAnimated(false);
            barChart.getXAxis().setAnimated(false);
            barChart.getYAxis().setAnimated(false);
            barChart.getXAxis().setTickLabelRotation(90);
            barChart.setVerticalGridLinesVisible(vlinesCheck.isSelected());
            barChart.setHorizontalGridLinesVisible(hlinesCheck.isSelected());
            barChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(barChart, Priority.ALWAYS);
            HBox.setHgrow(barChart, Priority.ALWAYS);
            if (legendSide == null) {
                barChart.setLegendVisible(false);
            } else {
                barChart.setLegendVisible(true);
                barChart.setLegendSide(legendSide);
            }
            chartBox.getChildren().add(barChart);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public synchronized void handleRowsTask() {
        try {
            makeChart();
            List<Integer> cols = tableController.checkedColsIndices();
            List<List<String>> selectedRows = tableController.selectedRows(all());
            int categoryCol = data2D.colOrder(selectedCategory);
            if (cols == null || selectedRows == null || categoryCol < 0) {
                return;
            }
            Map<String, String> palette = new HashMap();
            Random random = new Random();
            for (int i = 0; i < cols.size(); i++) {
                int colIndex = cols.get(i);
                Data2DColumn column = data2D.column(colIndex);
                String colName = column.getName();
                XYChart.Series series = new XYChart.Series();
                series.setName(colName);
                Color color = column.getColor();
                if (color == null) {
                    color = FxColorTools.randomColor(random);
                }
                palette.put(colName, FxColorTools.color2rgb(color));
                for (List<String> rowData : selectedRows) {
                    String categoryValue = rowData.get(categoryCol + 1);
                    double numberValue = data2D.doubleValue(rowData.get(colIndex + 1));
                    double coordinateValue = ChartTools.coordinateValue(chartCoordinate, numberValue);
                    XYChart.Data item = barChart.isXY()
                            ? new XYChart.Data(categoryValue, coordinateValue)
                            : new XYChart.Data(coordinateValue, categoryValue);
                    series.getData().add(item);
                }
                barChart.getData().add(i, series);
            }
            ChartTools.setBarChartColors(barChart, palette, legendSide != null);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    /*
        static
     */
    public static Data2DChartController open(ControlData2DEditTable tableController) {
        try {
            Data2DChartController controller = (Data2DChartController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DChartFxml, false);
            controller.setParameters(tableController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
