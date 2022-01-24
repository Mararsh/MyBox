package mara.mybox.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.SquareRootCoordinate;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.ChartTools;
import mara.mybox.fxml.chart.LabeledBarChart;
import mara.mybox.fxml.chart.Logarithmic10Coordinate;
import mara.mybox.fxml.chart.LogarithmicECoordinate;
import mara.mybox.tools.StringTools;
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
    protected int fontSize, categoryTickRotation, numberTickRotation;
    protected double barGap, categoryGap;
    protected Side titleSide, legendSide, categorySide, numberSide;
    protected LabeledBarChart barChart;
    protected LineChart lineChart;
    protected ScatterChart​ scatterChart​;
    protected PieChart pieChart;
    protected Chart chart;
    protected XYChart xyChart;
    protected CategoryAxis categoryAxis;
    protected NumberAxis numberAxis;

    @FXML
    protected RadioButton barChartRadio, stackedBarChartRadio, lineChartRadio, scatterChartRadio,
            pieRadio, bubbleChartRadio, areaChartRadio, stackedAreaChartRadio,
            cartesianRadio, logarithmicERadio, logarithmic10Radio, squareRootRadio;
    @FXML
    protected ComboBox<String> categoryColumnSelector, barGapSelector, categoryGapSelector,
            categoryTickRotationSelector, numberTickRotationSelector;
    @FXML
    protected VBox chartBox, barOptionsBox;
    @FXML
    protected TextField titleInput, categoryLabel, numberLabel;
    @FXML
    protected CheckBox categoryTickCheck, numberTickCheck, categoryMarkCheck, numberMarkCheck,
            hlinesCheck, vlinesCheck, xyReverseCheck,
            hZeroCheck, vZeroCheck, animatedCheck, categoryAxisAnimatedCheck, numberAxisAnimatedCheck;
    @FXML
    protected ToggleGroup chartGroup, titleSideGroup, labelGroup, legendGroup, numberCoordinateGroup,
            categorySideGroup, numberSideGroup;
    @FXML
    protected ComboBox<String> fontSizeSelector;

    @Override
    public void initControls() {
        try {
            super.initControls();

            chartGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        okAction();
                    });

            initDataTab();
            initPlotTab();
            initCategoryTab();
            initNumberTab();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initDataTab() {
        try {
            categoryColumnSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkOptions();
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initPlotTab() {
        try {
            titleInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (chart != null) {
                        chart.setTitle(titleInput.getText());
                    }
                }
            });

            xyReverseCheck.setSelected(UserConfig.getBoolean(baseName + "YX", false));
            xyReverseCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "YX", xyReverseCheck.isSelected());
                okAction();
            });

            hlinesCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayHlines", false));
            hlinesCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayHlines", hlinesCheck.isSelected());
                if (xyChart != null) {
                    xyChart.setHorizontalGridLinesVisible(hlinesCheck.isSelected());
                }
            });

            vlinesCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayVlines", false));
            vlinesCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayVlines", vlinesCheck.isSelected());
                if (xyChart != null) {
                    xyChart.setVerticalGridLinesVisible(vlinesCheck.isSelected());
                }
            });

            hZeroCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayHZero", true));
            hZeroCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayHZero", hZeroCheck.isSelected());
                if (xyChart != null) {
                    xyChart.setHorizontalZeroLineVisible(hZeroCheck.isSelected());
                }
            });

            vZeroCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayVZero", true));
            vZeroCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayVZero", vZeroCheck.isSelected());
                if (xyChart != null) {
                    xyChart.setVerticalZeroLineVisible(vZeroCheck.isSelected());
                }
            });

            fontSize = UserConfig.getInt(baseName + "ChartTextSize", 12);
            if (fontSize < 0) {
                fontSize = 12;
            }
            fontSizeSelector.getItems().addAll(Arrays.asList(
                    "12", "14", "10", "8", "15", "16", "18", "9", "6", "4", "20", "24"
            ));
            fontSizeSelector.getSelectionModel().select(fontSize + "");
            fontSizeSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v > 0) {
                        fontSize = v;
                        fontSizeSelector.getEditor().setStyle(null);
                        UserConfig.setInt(baseName + "ChartTextSize", fontSize);
                        if (chart != null) {
                            chart.setStyle(chart.getStyle() + "; -fx-font-size: " + fontSize + "px; ");
                            chart.requestLayout();
                        }
                    } else {
                        fontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                } catch (Exception e) {
                    fontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                }
            });

            titleSide = Side.TOP;
            titleSideGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues || newValue == null) {
                        return;
                    }
                    String value = ((RadioButton) newValue).getText();
                    if (message("Top").equals(value)) {
                        titleSide = Side.TOP;
                    } else if (message("Bottom").equals(value)) {
                        titleSide = Side.BOTTOM;
                    } else if (message("Left").equals(value)) {
                        titleSide = Side.LEFT;
                    } else if (message("Right").equals(value)) {
                        titleSide = Side.RIGHT;
                    }
                    if (chart != null) {
                        chart.setTitleSide(titleSide);
                        chart.requestLayout();
                    }
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
                        if (chart != null) {
                            if (legendSide == null) {
                                chart.setLegendVisible(false);
                            } else {
                                chart.setLegendVisible(true);
                                chart.setLegendSide(legendSide);
                            }
                            chart.requestLayout();
                        }
                    });

            animatedCheck.setSelected(UserConfig.getBoolean(baseName + "Animated", false));
            animatedCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "Animated", animatedCheck.isSelected());
                if (chart != null) {
                    chart.setAnimated(animatedCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initCategoryTab() {
        try {
            categoryLabel.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (categoryAxis != null) {
                        categoryAxis.setLabel(categoryLabel.getText());
                        chart.requestLayout();
                    }
                }
            });

            categoryTickCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayCategoryTick", true));
            categoryTickCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayCategoryTick", categoryTickCheck.isSelected());
                if (categoryAxis != null) {
                    categoryAxis.setTickLabelsVisible(categoryTickCheck.isSelected());
                    chart.requestLayout();
                }
            });

            categoryMarkCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayCategoryMark", true));
            categoryMarkCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayCategoryMark", categoryMarkCheck.isSelected());
                if (categoryAxis != null) {
                    categoryAxis.setTickMarkVisible(categoryMarkCheck.isSelected());
                    chart.requestLayout();
                }
            });

            categoryTickRotation = UserConfig.getInt(baseName + "CategoryTickRotation", 90);
            if (categoryTickRotation < 0) {
                categoryTickRotation = 90;
            }
            categoryTickRotationSelector.getItems().addAll(Arrays.asList(
                    "90", "45", "0", "30", "15", "60", "135", "120", "105", "150"
            ));
            categoryTickRotationSelector.getSelectionModel().select(categoryTickRotation + "");
            categoryTickRotationSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> v, String ov, String nv) -> {
                        try {
                            int d = Integer.valueOf(nv);
                            categoryTickRotation = d;
                            categoryTickRotationSelector.getEditor().setStyle(null);
                            UserConfig.setInt(baseName + "CategoryTickRotation", categoryTickRotation);
                            if (categoryAxis != null) {
                                categoryAxis.setTickLabelRotation(categoryTickRotation);
                                chart.requestLayout();
                            }
                        } catch (Exception e) {
                            categoryTickRotationSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            categorySide = Side.BOTTOM;
            categorySideGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues || newValue == null) {
                        return;
                    }
                    String value = ((RadioButton) newValue).getText();
                    if (message("Top").equals(value)) {
                        categorySide = Side.TOP;
                    } else if (message("Bottom").equals(value)) {
                        categorySide = Side.BOTTOM;
                    } else if (message("Left").equals(value)) {
                        categorySide = Side.LEFT;
                    } else if (message("Right").equals(value)) {
                        categorySide = Side.RIGHT;
                    }
                    if (categoryAxis != null) {
                        categoryAxis.setSide(categorySide);
                        chart.requestLayout();
                    }
                }
            });

            barGap = UserConfig.getDouble(baseName + "ChartBarGap", 1.0d);
            if (barGap < 0) {
                barGap = 1.0d;
            }
            barGapSelector.getItems().addAll(Arrays.asList(
                    "1", "0", "0.5", "2", "4", "1.5", "5", "8", "10", "20", "30", "40", "50"
            ));
            barGapSelector.getSelectionModel().select(barGap + "");
            barGapSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> v, String ov, String nv) -> {
                        try {
                            double d = Double.valueOf(nv);
                            if (d >= 0) {
                                barGap = d;
                                barGapSelector.getEditor().setStyle(null);
                                UserConfig.setDouble(baseName + "ChartBarGap", barGap);
                                if (barChart != null) {
                                    barChart.setBarGap(barGap);
                                    barChart.requestLayout();
                                }
                            } else {
                                barGapSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            barGapSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            categoryGap = UserConfig.getDouble(baseName + "ChartCategoryGap", 1.0d);
            if (categoryGap < 0) {
                categoryGap = 1.0d;
            }
            categoryGapSelector.getItems().addAll(Arrays.asList(
                    "1", "0", "0.5", "2", "4", "1.5", "5", "8", "10", "20", "30", "40", "50"
            ));
            categoryGapSelector.getSelectionModel().select(categoryGap + "");
            categoryGapSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> v, String ov, String nv) -> {
                        try {
                            double d = Double.valueOf(nv);
                            if (d >= 0) {
                                categoryGap = d;
                                categoryGapSelector.getEditor().setStyle(null);
                                UserConfig.setDouble(baseName + "ChartCategoryGap", categoryGap);
                                if (barChart != null) {
                                    barChart.setCategoryGap(categoryGap);
                                    barChart.requestLayout();
                                }
                            } else {
                                categoryGapSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            categoryGapSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            categoryAxisAnimatedCheck.setSelected(UserConfig.getBoolean(baseName + "CategoryAxisAnimated", false));
            categoryAxisAnimatedCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "CategoryAxisAnimated", categoryAxisAnimatedCheck.isSelected());
                if (categoryAxis != null) {
                    categoryAxis.setAnimated(categoryAxisAnimatedCheck.isSelected());
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initNumberTab() {
        try {
            numberLabel.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (numberAxis != null) {
                        numberAxis.setLabel(numberLabel.getText());
                        chart.requestLayout();
                    }
                }
            });

            numberTickCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayNumberAxis", true));
            numberTickCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayNumberAxis", numberTickCheck.isSelected());
                if (numberAxis != null) {
                    numberAxis.setTickLabelsVisible(numberTickCheck.isSelected());
                    chart.requestLayout();
                }
            });

            numberMarkCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayNumberMark", true));
            numberMarkCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayNumberMark", numberMarkCheck.isSelected());
                if (numberAxis != null) {
                    numberAxis.setTickMarkVisible(numberMarkCheck.isSelected());
                    chart.requestLayout();
                }
            });

            numberTickRotation = UserConfig.getInt(baseName + "NumberTickRotation", 0);
            if (numberTickRotation < 0) {
                numberTickRotation = 0;
            }
            numberTickRotationSelector.getItems().addAll(Arrays.asList(
                    "0", "90", "45", "30", "15", "60", "135", "120", "105", "150"
            ));
            numberTickRotationSelector.getSelectionModel().select(numberTickRotation + "");
            numberTickRotationSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> v, String ov, String nv) -> {
                        try {
                            int d = Integer.valueOf(nv);
                            numberTickRotation = d;
                            numberTickRotationSelector.getEditor().setStyle(null);
                            UserConfig.setInt(baseName + "NumberTickRotation", numberTickRotation);
                            if (numberAxis != null) {
                                numberAxis.setTickLabelRotation(numberTickRotation);
                                chart.requestLayout();
                            }
                        } catch (Exception e) {
                            numberTickRotationSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            numberSide = Side.LEFT;
            numberSideGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues || newValue == null) {
                        return;
                    }
                    String value = ((RadioButton) newValue).getText();
                    if (message("Top").equals(value)) {
                        numberSide = Side.TOP;
                    } else if (message("Bottom").equals(value)) {
                        numberSide = Side.BOTTOM;
                    } else if (message("Left").equals(value)) {
                        numberSide = Side.LEFT;
                    } else if (message("Right").equals(value)) {
                        numberSide = Side.RIGHT;
                    }
                    if (numberAxis != null) {
                        numberAxis.setSide(numberSide);
                        chart.requestLayout();
                    }
                }
            });

            labelType = ChartTools.LabelType.NameAndValue;
            labelGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
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
                        okAction();
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
                        okAction();
                    });

            numberAxisAnimatedCheck.setSelected(UserConfig.getBoolean(baseName + "NumberAxisAnimated", false));
            numberAxisAnimatedCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "NumberAxisAnimated", numberAxisAnimatedCheck.isSelected());
                if (numberAxis != null) {
                    numberAxis.setAnimated(numberAxisAnimatedCheck.isSelected());
                }
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
//            titleInput.clear();
//            categoryLabel.clear();
//            numberLabel.clear();
            categoryColumnSelector.getItems().clear();

            List<String> names = tableController.data2D.columnNames();
            if (names == null || names.isEmpty()) {
                return;
            }
            selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
            categoryColumnSelector.getItems().setAll(names);
            if (selectedCategory != null && names.contains(selectedCategory)) {
                categoryColumnSelector.setValue(selectedCategory);
            } else {
                categoryColumnSelector.getSelectionModel().select(0);
            }
            okAction();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        boolean ok = super.checkOptions();
        selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            infoLabel.setText(message("SelectToHandle"));
            okButton.setDisable(true);
            return false;
        }
        return ok;
    }

    public void makeChart() {
        try {
            chart = null;
            xyChart = null;
            barChart = null;
            lineChart = null;
            pieChart = null;
            categoryAxis = null;
            numberAxis = null;
            barOptionsBox.setVisible(barChartRadio.isSelected());
            chartBox.getChildren().clear();
            if (pieRadio.isSelected()) {
                makePieChart();
            } else {
                categoryAxis = new CategoryAxis();
                categoryAxis.setLabel(categoryLabel.getText());
                categoryAxis.setSide(categorySide);
                categoryAxis.setTickLabelsVisible(categoryTickCheck.isSelected());
                categoryAxis.setGapStartAndEnd(true);
                categoryAxis.setTickLabelRotation(categoryTickRotation);
                categoryAxis.setAnimated(categoryAxisAnimatedCheck.isSelected());
                if (xyReverseCheck.isSelected()) {
                    categoryAxis.setEndMargin(100);
                } else {
                    categoryAxis.setEndMargin(20);
                }

                numberAxis = new NumberAxis();
                numberAxis.setLabel(numberLabel.getText());
                numberAxis.setSide(numberSide);
                numberAxis.setTickLabelsVisible(numberTickCheck.isSelected());
                numberAxis.setTickLabelRotation(numberTickRotation);
                numberAxis.setAnimated(numberAxisAnimatedCheck.isSelected());
                switch (chartCoordinate) {
                    case LogarithmicE:
                        numberAxis.setTickLabelFormatter(new LogarithmicECoordinate());
                        break;
                    case Logarithmic10:
                        numberAxis.setTickLabelFormatter(new Logarithmic10Coordinate());
                        break;
                    case SquareRoot:
                        numberAxis.setTickLabelFormatter(new SquareRootCoordinate());
                        break;
                }

                if (barChartRadio.isSelected()) {
                    makeBarChart();
                } else if (lineChartRadio.isSelected()) {
                    makeLineChart();
                } else if (scatterChartRadio.isSelected()) {
                    makeScatterChart​();
                } else {
                    return;
                }

                xyChart.setAlternativeRowFillVisible(false);
                xyChart.setAlternativeColumnFillVisible(false);
                xyChart.setVerticalGridLinesVisible(vlinesCheck.isSelected());
                xyChart.setHorizontalGridLinesVisible(hlinesCheck.isSelected());
                xyChart.setVerticalZeroLineVisible(vZeroCheck.isSelected());
                xyChart.setHorizontalZeroLineVisible(hZeroCheck.isSelected());

                if (legendSide == null) {
                    xyChart.setLegendVisible(false);
                } else {
                    xyChart.setLegendVisible(true);
                    xyChart.setLegendSide(legendSide);
                }

//                xyChart.getData().addListener(new ListChangeListener<XYChart.Series<Axis, Axis>>() {
//                    @Override
//                    public void onChanged(ListChangeListener.Change<? extends XYChart.Series<Axis, Axis>> change) {
//                        while (change.next()) {
//                            if (!change.wasPermutated() && !change.wasUpdated()) {
//                                for (XYChart.Series<Axis, Axis> series : change.getAddedSubList()) {
//
//                                }
//                            }
//                        }
//                    }
//                });
                chart = xyChart;

            }
            if (chart != null) {
                chart.setStyle(chart.getStyle() + "; -fx-font-size: " + fontSize + "px; ");
                chart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                VBox.setVgrow(chart, Priority.ALWAYS);
                HBox.setHgrow(chart, Priority.ALWAYS);
                chart.setAnimated(animatedCheck.isSelected());
                chart.setTitle(titleInput.getText());
                chart.setTitleSide(titleSide);
                chartBox.getChildren().add(chart);
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void makeBarChart() {
        try {
            if (xyReverseCheck.isSelected()) {
                barChart = new LabeledBarChart(numberAxis, categoryAxis);
            } else {
                barChart = new LabeledBarChart(categoryAxis, numberAxis);
            }
            barChart.setIntValue(false).setLabelType(labelType)
                    .setTextSize(fontSize).setChartCoordinate(chartCoordinate);
            barChart.setBarGap(barGap);
            barChart.setCategoryGap(categoryGap);
            xyChart = barChart;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void makeLineChart() {
        try {
            if (xyReverseCheck.isSelected()) {
                lineChart = new LineChart(numberAxis, categoryAxis);
            } else {
                lineChart = new LineChart(categoryAxis, numberAxis);
            }
            xyChart = lineChart;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void makeScatterChart​() {
        try {
            if (xyReverseCheck.isSelected()) {
                scatterChart​ = new ScatterChart​(numberAxis, categoryAxis);
            } else {
                scatterChart​ = new ScatterChart​(categoryAxis, numberAxis);
            }
            xyChart = scatterChart​;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void makePieChart() {
        try {

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
                String rgb = FxColorTools.color2rgb(color);
                palette.put(colName, rgb);
                for (List<String> rowData : selectedRows) {
                    String categoryValue = rowData.get(categoryCol + 1);
                    double numberValue = data2D.doubleValue(rowData.get(colIndex + 1));
                    double coordinateValue = ChartTools.coordinateValue(chartCoordinate, numberValue);
                    XYChart.Data data = xyReverseCheck.isSelected()
                            ? new XYChart.Data(coordinateValue, categoryValue)
                            : new XYChart.Data(categoryValue, coordinateValue);
                    setLabel(data, categoryValue, numberValue, color);
                    series.getData().add(data);
                }
                xyChart.getData().add(i, series);
            }
            if (barChart != null) {
                ChartTools.setBarChartColors(barChart, palette, legendSide != null);
            } else if (lineChart != null) {
                ChartTools.setLineChartColors(lineChart, palette, legendSide != null);
            } else if (scatterChart​ != null) {
                ChartTools.setLegend(scatterChart​, palette, legendSide != null);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void setLabel(XYChart.Data data, String name, double value, Color color) {
        String label;
        if (null == labelType || barChart != null) {
            return;
        } else {
            switch (labelType) {
                case NotDisplay:
                    return;
                case Name:
                    label = name;
                    break;
                case Value:
                    label = StringTools.format(value);
                    break;
                default:
                    label = name + ": " + StringTools.format(value);
                    break;
            }
        }
        Label labelNode = new Label();
        if (labelType == ChartTools.LabelType.Pop) {
            labelNode.setText("");
            NodeStyleTools.setTooltip(labelNode, label);
        } else {
            labelNode.setText(label);
        }
        Circle circle = new Circle(0, 0, 5, color);
        StackPane pane = new StackPane();
        pane.setStyle("-fx-background-color: transparent;  -fx-font-size: " + fontSize + "px;");
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(circle, labelNode);
        pane.getChildren().addAll(box);
        data.setNode(pane);
    }

    @FXML
    public void refreshAction() {
        okAction();
    }

    @FXML
    public void snapAction() {;
        ImageViewerController.load(NodeTools.snap(chartBox));
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
