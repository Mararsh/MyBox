package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.ChartTools;
import mara.mybox.fxml.chart.ChartTools.LabelType;
import mara.mybox.fxml.chart.LabeledAreaChart;
import mara.mybox.fxml.chart.LabeledBarChart;
import mara.mybox.fxml.chart.LabeledBubbleChart;
import mara.mybox.fxml.chart.LabeledLineChart;
import mara.mybox.fxml.chart.LabeledScatterChart;
import mara.mybox.fxml.chart.LabeledStackedAreaChart;
import mara.mybox.fxml.chart.LabeledStackedBarChart;
import mara.mybox.tools.DoubleTools;
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

    protected String selectedCategory, selectedValue;
    protected LabelType labelType;
    protected ChartTools.ChartCoordinate chartCoordinate;
    protected int plotFontSize, tickFontSize, lineWidth, categoryFontSize, categoryMargin,
            categoryTickRotation, numberFontSize, numberTickRotation;
    protected double barGap, categoryGap;
    protected Side titleSide, legendSide, categorySide, numberSide;
    protected LabeledBarChart barChart;
    protected LabeledStackedBarChart stackedBarChart;
    protected LabeledLineChart lineChart;
    protected LabeledScatterChart​ scatterChart​;
    protected LabeledAreaChart areaChart;
    protected LabeledStackedAreaChart stackedAreaChart;
    protected LabeledBubbleChart bubbleChart;
    protected PieChart pieChart;
    protected Chart chart;
    protected XYChart xyChart;
    protected CategoryAxis categoryAxis;
    protected NumberAxis numberAxis;
    protected List<Integer> checkedColsIndices;
    protected Map<String, String> palette;
    protected List<String> paletteList;

    @FXML
    protected Tab categoryTab, valueTab;
    @FXML
    protected RadioButton barChartRadio, stackedBarChartRadio, lineChartRadio, scatterChartRadio,
            pieRadio, bubbleChartRadio, areaChartRadio, stackedAreaChartRadio,
            cartesianRadio, logarithmicERadio, logarithmic10Radio, squareRootRadio;
    @FXML
    protected ComboBox<String> categoryColumnSelector, valueColumnSelector,
            plotFontSizeSelector, lineWdithSelector, tickFontSizeSelector, categoryMarginSelector,
            barGapSelector, categoryGapSelector, categoryFontSizeSelector, categoryTickRotationSelector,
            numberFontSizeSelector, numberTickRotationSelector;
    @FXML
    protected VBox columnsBox, chartBox, xyPlotBox;
    @FXML
    protected HBox barGapBox, categoryGapBox, lineWidthBox, bubbleBox;
    @FXML
    protected FlowPane valueColumnsPane, valueColumnPane, categoryColumnsPane;
    @FXML
    protected TextField titleInput, categoryLabel, numberLabel, bubbleStyleInput;
    @FXML
    protected CheckBox categoryTickCheck, numberTickCheck, categoryMarkCheck, numberMarkCheck,
            hlinesCheck, vlinesCheck, xyReverseCheck, autoTitleCheck, clockwiseCheck,
            hZeroCheck, vZeroCheck, animatedCheck, categoryAxisAnimatedCheck, numberAxisAnimatedCheck;
    @FXML
    protected ToggleGroup chartGroup, titleSideGroup, labelGroup, legendGroup, numberCoordinateGroup,
            categorySideGroup, numberSideGroup;
    @FXML
    protected Label columnsLabel;

    @Override
    public void initControls() {
        try {
            super.initControls();

            initChartBox();
            initDataTab();
            initPlotTab();
            initCategoryTab();
            initNumberTab();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initChartBox() {
        try {
            chartGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        checkChartType();
                        okAction();
                    });
            checkChartType();

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

            valueColumnSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
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

            labelType = LabelType.NameAndValue;
            labelGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        String value = ((RadioButton) newValue).getText();
                        if (message("NameAndValue").equals(value)) {
                            labelType = LabelType.NameAndValue;
                        } else if (message("Value").equals(value)) {
                            labelType = LabelType.Value;
                        } else if (message("Name").equals(value)) {
                            labelType = LabelType.Name;
                        } else if (message("NotDisplay").equals(value)) {
                            labelType = LabelType.NotDisplay;
                        } else if (message("Pop").equals(value)) {
                            labelType = LabelType.Pop;
                        } else if (message("Point").equals(value)) {
                            labelType = LabelType.Point;
                        } else {
                            labelType = LabelType.NameAndValue;
                        }
                        okAction();
                    });

            xyReverseCheck.setSelected(UserConfig.getBoolean(baseName + "YX", false));
            xyReverseCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                checkXYReverse();
                UserConfig.setBoolean(baseName + "YX", xyReverseCheck.isSelected());
                okAction();
            });
            checkXYReverse();

            autoTitleCheck.setSelected(UserConfig.getBoolean(baseName + "AutoTitle", true));
            autoTitleCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "AutoTitle", autoTitleCheck.isSelected());
                checkAutoTitle();
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

            plotFontSize = UserConfig.getInt(baseName + "PlotFontSize", 12);
            if (plotFontSize < 0) {
                plotFontSize = 12;
            }
            plotFontSizeSelector.getItems().addAll(Arrays.asList(
                    "12", "14", "10", "8", "15", "16", "18", "9", "6", "4", "20", "24"
            ));
            plotFontSizeSelector.getSelectionModel().select(plotFontSize + "");
            plotFontSizeSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                plotFontSize = v;
                                plotFontSizeSelector.getEditor().setStyle(null);
                                UserConfig.setInt(baseName + "PlotFontSize", plotFontSize);
                                if (chart != null) {
                                    okAction();
                                }
                            } else {
                                plotFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            plotFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            tickFontSize = UserConfig.getInt(baseName + "TickFontSize", 12);
            if (tickFontSize < 0) {
                tickFontSize = 12;
            }
            tickFontSizeSelector.getItems().addAll(Arrays.asList(
                    "12", "14", "10", "8", "15", "16", "18", "9", "6", "4", "20", "24"
            ));
            tickFontSizeSelector.getSelectionModel().select(tickFontSize + "");
            tickFontSizeSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                tickFontSize = v;
                                tickFontSizeSelector.getEditor().setStyle(null);
                                UserConfig.setInt(baseName + "TickFontSize", tickFontSize);
                                if (chart != null) {
                                    okAction();
                                }
                            } else {
                                tickFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            tickFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            lineWidth = UserConfig.getInt(baseName + "LineWidth", 4);
            if (lineWidth < 0) {
                lineWidth = 1;
            }
            lineWdithSelector.getItems().addAll(Arrays.asList(
                    "4", "1", "2", "3", "5", "6", "7", "8", "9", "10"
            ));
            lineWdithSelector.getSelectionModel().select(lineWidth + "");
            lineWdithSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v >= 0) {
                                lineWidth = v;
                                lineWdithSelector.getEditor().setStyle(null);
                                UserConfig.setInt(baseName + "LineWidth", lineWidth);
                                if (lineChart != null) {
                                    ChartTools.setLineChartColors(lineChart, lineWidth, palette, legendSide != null);
                                }
                            } else {
                                lineWdithSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            lineWdithSelector.getEditor().setStyle(UserConfig.badStyle());
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

            clockwiseCheck.setSelected(UserConfig.getBoolean(baseName + "Clockwise", false));
            clockwiseCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "Clockwise", clockwiseCheck.isSelected());
                if (pieChart != null) {
                    pieChart.setClockwise(clockwiseCheck.isSelected());
                }
            });

            bubbleStyleInput.setText(UserConfig.getString(baseName + "BubbleStyle", ChartTools.DefaultBubbleStyle));

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

            categoryFontSize = UserConfig.getInt(baseName + "CategoryFontSize", 12);
            if (categoryFontSize < 0) {
                categoryFontSize = 12;
            }
            categoryFontSizeSelector.getItems().addAll(Arrays.asList(
                    "12", "14", "10", "8", "15", "16", "18", "9", "6", "4", "20", "24"
            ));
            categoryFontSizeSelector.getSelectionModel().select(categoryFontSize + "");
            categoryFontSizeSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                categoryFontSize = v;
                                categoryFontSizeSelector.getEditor().setStyle(null);
                                UserConfig.setInt(baseName + "CategoryFontSize", categoryFontSize);
                                if (categoryAxis != null) {
                                    categoryAxis.setStyle("-fx-font-size: " + categoryFontSize + "px;");
                                }
                            } else {
                                categoryFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            categoryFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            categoryMargin = UserConfig.getInt(baseName + "CategoryMargin", 0);
            categoryMarginSelector.getItems().addAll(Arrays.asList(
                    "12", "14", "10", "8", "15", "16", "18", "9", "6", "4", "20", "24"
            ));
            categoryMarginSelector.getSelectionModel().select(categoryMargin + "");
            categoryMarginSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            categoryMargin = Integer.parseInt(newValue);
                            categoryMarginSelector.getEditor().setStyle(null);
                            UserConfig.setInt(baseName + "CategoryMargin", categoryMargin);
                            if (categoryAxis != null) {
                                categoryAxis.setStartMargin(categoryMargin);
                                categoryAxis.setEndMargin(categoryMargin);
                            }
                        } catch (Exception e) {
                            categoryMarginSelector.getEditor().setStyle(UserConfig.badStyle());
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

            categoryGap = UserConfig.getDouble(baseName + "ChartCategoryGap", 20d);
            if (categoryGap < 0) {
                categoryGap = 1.0d;
            }
            categoryGapSelector.getItems().addAll(Arrays.asList(
                    "20", "10", "30", "5", "8", "1", "0", "0.5", "2", "4", "1.5", "40", "50"
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
                                } else if (stackedBarChart != null) {
                                    stackedBarChart.setCategoryGap(categoryGap);
                                    stackedBarChart.requestLayout();
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

            numberFontSize = UserConfig.getInt(baseName + "NumberAxisFontSize", 12);
            if (numberFontSize < 0) {
                numberFontSize = 12;
            }
            numberFontSizeSelector.getItems().addAll(Arrays.asList(
                    "12", "14", "10", "8", "15", "16", "18", "9", "6", "4", "20", "24"
            ));
            numberFontSizeSelector.getSelectionModel().select(numberFontSize + "");
            numberFontSizeSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                numberFontSize = v;
                                numberFontSizeSelector.getEditor().setStyle(null);
                                UserConfig.setInt(baseName + "NumberAxisFontSize", numberFontSize);
                                if (numberAxis != null) {
                                    numberAxis.setStyle("-fx-font-size: " + numberFontSize + "px;");
                                }
                            } else {
                                numberFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            numberFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
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

    public void checkChartType() {
        try {
            columnsBox.getChildren().clear();
            boolean isPie = pieRadio.isSelected();
            if (isPie) {
                columnsBox.getChildren().addAll(categoryColumnsPane, valueColumnPane);

            } else if (bubbleChartRadio.isSelected()) {
                columnsLabel.setText(message("SizeColumns"));
                columnsBox.getChildren().addAll(categoryColumnsPane, valueColumnPane, valueColumnsPane);

            } else {
                columnsLabel.setText(message("ValueColumns"));
                columnsBox.getChildren().addAll(categoryColumnsPane, valueColumnsPane);

            }
            categoryTab.setDisable(isPie);
            valueTab.setDisable(isPie);
            xyPlotBox.setDisable(isPie);
            clockwiseCheck.setDisable(!isPie);

            barGapBox.setDisable(!barChartRadio.isSelected() && !stackedBarChartRadio.isSelected());
            categoryGapBox.setDisable(!barChartRadio.isSelected());

            bubbleBox.setDisable(!bubbleChartRadio.isSelected());

            checkAutoTitle();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkXYReverse() {
        try {
            if (xyReverseCheck.isSelected()) {
                categoryTab.setText(message("CategoryAxis") + "(Y)");
                valueTab.setText(message("ValueAxis") + "(X)");
            } else {
                categoryTab.setText(message("CategoryAxis") + "(X)");
                valueTab.setText(message("ValueAxis") + "(Y)");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void checkAutoTitle() {
        try {
            if (autoTitleCheck.isSelected()) {
                defaultTitle();
                defaultCategoryLabel();
                defaultValueLabel();
            }
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
                    refreshSelectors();
                }
            });

            refreshSelectors();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void refreshSelectors() {
        try {
            categoryColumnSelector.getItems().clear();
            valueColumnSelector.getItems().clear();

            List<String> names = tableController.data2D.columnNames();
            if (names == null || names.isEmpty()) {
                return;
            }
            isSettingValues = true;
            selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
            categoryColumnSelector.getItems().setAll(names);
            if (selectedCategory != null && names.contains(selectedCategory)) {
                categoryColumnSelector.setValue(selectedCategory);
            } else {
                categoryColumnSelector.getSelectionModel().select(0);
            }

            selectedValue = valueColumnSelector.getSelectionModel().getSelectedItem();
            valueColumnSelector.getItems().setAll(names);
            if (selectedValue != null && names.contains(selectedValue)) {
                valueColumnSelector.setValue(selectedValue);
            } else {
                valueColumnSelector.getSelectionModel().select(names.size() > 1 ? 1 : 0);
            }

            isSettingValues = false;
            okAction();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public String title() {
        if (bubbleChartRadio.isSelected()) {
            return categoryName() + " - " + valueName() + " : " + valuesNames();
        } else if (pieRadio.isSelected()) {
            return categoryName() + " - " + valueName();
        } else {
            return categoryName() + " - " + valuesNames();
        }
    }

    public String categoryName() {
        return categoryColumnSelector.getSelectionModel().getSelectedItem();
    }

    public String valueName() {
        return valueColumnSelector.getSelectionModel().getSelectedItem();
    }

    public String valuesNames() {
        return tableController.checkedColsNames().toString();
    }

    @Override
    public boolean checkOptions() {
        if (isSettingValues) {
            return true;
        }
        checkAutoTitle();
        boolean ok = super.checkOptions();
        selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            infoLabel.setText(message("SelectToHandle"));
            okButton.setDisable(true);
            return false;
        }
        if (pieRadio.isSelected() || bubbleChartRadio.isSelected()) {
            selectedValue = valueColumnSelector.getSelectionModel().getSelectedItem();
            if (selectedValue == null) {
                infoLabel.setText(message("SelectToHandle"));
                okButton.setDisable(true);
                return false;
            }
        }
        return ok;
    }

    public void makeChart() {
        try {
            chart = null;
            xyChart = null;
            barChart = null;
            stackedBarChart = null;
            lineChart = null;
            areaChart = null;
            stackedAreaChart = null;
            bubbleChart = null;
            scatterChart​ = null;
            pieChart = null;
            categoryAxis = null;
            numberAxis = null;
            palette = null;
            paletteList = null;

            chartBox.getChildren().clear();
            if (pieRadio.isSelected()) {
                makePieChart();
            } else {
                categoryAxis = new CategoryAxis();
                categoryAxis.setLabel(categoryLabel.getText());
                categoryAxis.setSide(categorySide);
                categoryAxis.setTickLabelsVisible(categoryTickCheck.isSelected());
                categoryAxis.setTickMarkVisible(categoryMarkCheck.isSelected());
                categoryAxis.setTickLabelRotation(categoryTickRotation);
                categoryAxis.setGapStartAndEnd(true);
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
                numberAxis.setTickMarkVisible(numberMarkCheck.isSelected());
                numberAxis.setTickLabelRotation(numberTickRotation);
                numberAxis.setAnimated(numberAxisAnimatedCheck.isSelected());

                if (barChartRadio.isSelected()) {
                    makeBarChart();
                } else if (stackedBarChartRadio.isSelected()) {
                    makeStackedBarChart();
                } else if (lineChartRadio.isSelected()) {
                    makeLineChart();
                } else if (scatterChartRadio.isSelected()) {
                    makeScatterChart​();
                } else if (areaChartRadio.isSelected()) {
                    makeAreaChart();
                } else if (stackedAreaChartRadio.isSelected()) {
                    makeStackedAreaChart();
                } else if (bubbleChartRadio.isSelected()) {
                    makeBubbleChart();
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

                chart = xyChart;

            }
            if (chart != null) {
                chart.setStyle("-fx-font-size: " + plotFontSize + "px; -fx-tick-label-font-size: " + tickFontSize + "px; ");
                if (categoryAxis != null) {
                    categoryAxis.setStyle("-fx-font-size: " + categoryFontSize + "px;");
                }
                if (numberAxis != null) {
                    numberAxis.setStyle("-fx-font-size: " + numberFontSize + "px;");
                }
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
                    .setTextSize(tickFontSize).setChartCoordinate(chartCoordinate);
            barChart.setBarGap(barGap);
            barChart.setCategoryGap(categoryGap);
            xyChart = barChart;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void makeStackedBarChart() {
        try {
            if (xyReverseCheck.isSelected()) {
                stackedBarChart = new LabeledStackedBarChart(numberAxis, categoryAxis);
            } else {
                stackedBarChart = new LabeledStackedBarChart(categoryAxis, numberAxis);
            }
            stackedBarChart.setIntValue(false).setLabelType(labelType)
                    .setTextSize(tickFontSize).setChartCoordinate(chartCoordinate);
            stackedBarChart.setCategoryGap(categoryGap);
            xyChart = stackedBarChart;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void makeLineChart() {
        try {
            if (xyReverseCheck.isSelected()) {
                lineChart = new LabeledLineChart(numberAxis, categoryAxis);
            } else {
                lineChart = new LabeledLineChart(categoryAxis, numberAxis);
            }
            lineChart.setIntValue(false).setLabelType(labelType)
                    .setTextSize(tickFontSize).setChartCoordinate(chartCoordinate);
            xyChart = lineChart;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void makeScatterChart​() {
        try {
            if (xyReverseCheck.isSelected()) {
                scatterChart = new LabeledScatterChart​(numberAxis, categoryAxis);
            } else {
                scatterChart = new LabeledScatterChart​(categoryAxis, numberAxis);
            }
            scatterChart.setIntValue(false).setLabelType(labelType)
                    .setTextSize(tickFontSize).setChartCoordinate(chartCoordinate);
            xyChart = scatterChart;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void makeAreaChart() {
        try {
            if (xyReverseCheck.isSelected()) {
                areaChart = new LabeledAreaChart(numberAxis, categoryAxis);
            } else {
                areaChart = new LabeledAreaChart(categoryAxis, numberAxis);
            }
            areaChart.setIntValue(false).setLabelType(labelType)
                    .setTextSize(tickFontSize).setChartCoordinate(chartCoordinate);
            xyChart = areaChart;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void makeStackedAreaChart() {
        try {
            if (xyReverseCheck.isSelected()) {
                stackedAreaChart = new LabeledStackedAreaChart(numberAxis, categoryAxis);
            } else {
                stackedAreaChart = new LabeledStackedAreaChart(categoryAxis, numberAxis);
            }
            stackedAreaChart.setIntValue(false).setLabelType(labelType)
                    .setTextSize(tickFontSize).setChartCoordinate(chartCoordinate);
            xyChart = stackedAreaChart;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void makeBubbleChart() {
        try {
            NumberAxis numberAxisX = new NumberAxis();
            numberAxisX.setLabel(categoryLabel.getText());
            numberAxisX.setSide(categorySide);
            numberAxisX.setTickLabelsVisible(categoryTickCheck.isSelected());
            numberAxisX.setTickLabelRotation(categoryTickRotation);
            numberAxisX.setAnimated(categoryAxisAnimatedCheck.isSelected());
            ChartTools.setChartCoordinate(numberAxisX, chartCoordinate);
            bubbleChart = new LabeledBubbleChart(numberAxisX, numberAxis);
            bubbleChart.setIntValue(false).setLabelType(labelType)
                    .setTextSize(tickFontSize).setChartCoordinate(chartCoordinate);
            xyChart = bubbleChart;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void makePieChart() {
        try {
            pieChart = new PieChart();
            pieChart.setClockwise(clockwiseCheck.isSelected());
            pieChart.setLabelLineLength(0d);
            chart = pieChart;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public synchronized void okAction() {
        if (!checkOptions()) {
            return;
        }
        List<Integer> colsIndices = new ArrayList<>();
        int categoryCol = data2D.colOrder(selectedCategory);
        if (categoryCol < 0) {
            popError(message("SelectToHandle"));
            return;
        }
        colsIndices.add(categoryCol);
        if (pieRadio.isSelected() || bubbleChartRadio.isSelected()) {
            int valueCol = data2D.colOrder(selectedValue);
            if (valueCol < 0) {
                popError(message("SelectToHandle"));
                return;
            }
            colsIndices.add(valueCol);
        }
        if (!pieRadio.isSelected()) {
            checkedColsIndices = tableController.checkedColsIndices();
            if (checkedColsIndices == null || checkedColsIndices.isEmpty()) {
                popError(message("SelectToHandle"));
                return;
            }
            colsIndices.addAll(checkedColsIndices);
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(task);
                    if (allPages()) {
                        handledData = data2D.allRows(colsIndices, false);
                    } else {
                        handledData = tableController.selectedData(tableController.checkedRowsIndices(all()),
                                colsIndices, false);
                    }
                    return handledData != null && !handledData.isEmpty();
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                drawChart();
            }

        };
        start(task);
    }

    public synchronized void drawChart() {
        try {
            if (checkedColsIndices == null || handledData == null || handledData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            makeChart();
            if (pieRadio.isSelected()) {
                drawPieChart();
            } else if (bubbleChartRadio.isSelected()) {
                drawBubbleChart();
            } else {
                drawXYChart();
            }
            setChartStyle();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public synchronized void drawXYChart() {
        try {
            palette = new HashMap();
            Random random = new Random();
            XYChart.Data xyData;
            for (int i = 0; i < checkedColsIndices.size(); i++) {
                int colIndex = checkedColsIndices.get(i);
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
                for (List<String> rowData : handledData) {
                    String category = rowData.get(0);
                    double numberValue = data2D.doubleValue(rowData.get(i + 1));
                    double numberCoordinateValue = ChartTools.coordinateValue(chartCoordinate, numberValue);
                    if (bubbleChartRadio.isSelected()) {
                        double categoryValue = data2D.doubleValue(category);
                        double categoryCoordinateValue = ChartTools.coordinateValue(chartCoordinate, categoryValue);

                        xyData = xyReverseCheck.isSelected()
                                ? new XYChart.Data(numberCoordinateValue, categoryCoordinateValue)
                                : new XYChart.Data(categoryCoordinateValue, numberCoordinateValue);
                    } else {
                        xyData = xyReverseCheck.isSelected()
                                ? new XYChart.Data(numberCoordinateValue, category)
                                : new XYChart.Data(category, numberCoordinateValue);
                    }
                    series.getData().add(xyData);
                }
                xyChart.getData().add(i, series);
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public synchronized void drawBubbleChart() {
        try {
            XYChart.Data xyData;
            palette = new HashMap();
            Random random = new Random();
            List<XYChart.Series> seriesList = new ArrayList<>();
            int sizeNum = checkedColsIndices.size();
            for (int i = 0; i < sizeNum; i++) {
                int colIndex = checkedColsIndices.get(i);
                Data2DColumn column = data2D.column(colIndex);
                String colName = column.getName();
                XYChart.Series series = new XYChart.Series();
                series.setName(colName);
                seriesList.add(series);

                Color color = column.getColor();
                if (color == null) {
                    color = FxColorTools.randomColor(random);
                }
                String rgb = FxColorTools.color2rgb(color);
                palette.put(colName, rgb);
            }
            for (List<String> rowData : handledData) {
                double categoryValue = data2D.doubleValue(rowData.get(0));
                double categoryCoordinateValue = ChartTools.coordinateValue(chartCoordinate, categoryValue);
                double numberValue = data2D.doubleValue(rowData.get(1));
                double numberCoordinateValue = ChartTools.coordinateValue(chartCoordinate, numberValue);
                for (int i = 0; i < sizeNum; i++) {
                    double sizeValue = data2D.doubleValue(rowData.get(i + 2));
                    double sizeCoordinateValue = ChartTools.coordinateValue(chartCoordinate, sizeValue);
                    xyData = xyReverseCheck.isSelected()
                            ? new XYChart.Data(numberCoordinateValue, categoryCoordinateValue)
                            : new XYChart.Data(categoryCoordinateValue, numberCoordinateValue);
                    xyData.setExtraValue(sizeCoordinateValue);
                    seriesList.get(i).getData().add(xyData);
                }
            }
            bubbleChart.getData().addAll(seriesList);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public synchronized void drawPieChart() {
        try {
            Random random = new Random();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            pieChart.setData(pieData);
            String label;
            paletteList = new ArrayList();
            double total = 0;
            for (List<String> rowData : handledData) {
                double d = data2D.doubleValue(rowData.get(1));
                total += d;
            }
            if (total == 0) {
                total = Double.MIN_VALUE;
            }
            for (List<String> rowData : handledData) {
                String name = rowData.get(0);
                double d = data2D.doubleValue(rowData.get(1));
                double percent = DoubleTools.scale(d * 100 / total, 1);
                String labelValue = StringTools.format(d);
                switch (labelType) {
                    case Name:
                        label = name;
                        break;
                    case Value:
                        label = percent + "% " + labelValue;
                        break;
                    case NameAndValue:
                        label = name + " " + percent + "% " + labelValue;
                        break;
                    case NotDisplay:
                    case Point:
                    case Pop:
                    default:
                        label = "";
                        break;
                }
                PieChart.Data item = new PieChart.Data(label, d);
                pieData.add(item);
                if (labelType == LabelType.Pop) {
                    NodeStyleTools.setTooltip(item.getNode(), name + " " + percent + "% " + labelValue);
                }
                paletteList.add(FxColorTools.randomRGB(random));
            }

            pieChart.setLabelsVisible(labelType == LabelType.Name
                    || labelType == LabelType.Value || labelType == LabelType.NameAndValue);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public synchronized void setChartStyle() {
        if (chart == null) {
            return;
        }
        if (barChart != null) {
            ChartTools.setBarChartColors(barChart, palette, legendSide != null);
        } else if (stackedBarChart != null) {
            ChartTools.setBarChartColors(stackedBarChart, palette, legendSide != null);
        } else if (lineChart != null) {
            ChartTools.setLineChartColors(lineChart, lineWidth, palette, legendSide != null);
        } else if (areaChart != null) {
            ChartTools.setAreaChartColors(areaChart, lineWidth, palette, legendSide != null);
        } else if (stackedAreaChart != null) {
            ChartTools.setAreaChartColors(stackedAreaChart, lineWidth, palette, legendSide != null);
        } else if (scatterChart != null) {
            ChartTools.setScatterChart​Colors(scatterChart, palette, legendSide != null);
        } else if (pieChart != null) {
            ChartTools.setPieColors(pieChart, paletteList, legendSide != null);
        } else if (bubbleChart != null) {
            ChartTools.setBubbleChart​Colors(bubbleChart, bubbleStyleInput.getText(), palette, legendSide != null);
        }
        if (chart != null) {
            chart.requestLayout();
        }
    }

    @FXML
    public void refreshAction() {
        okAction();
    }

    @FXML
    public void snapAction() {
        ImageViewerController.load(NodeTools.snap(chartBox));
    }

    @FXML
    public void defaultTitle() {
        titleInput.setText(title());
    }

    @FXML
    public void defaultCategoryLabel() {
        categoryLabel.setText(categoryName());
    }

    @FXML
    public void defaultValueLabel() {
        numberLabel.setText(valuesNames());
    }

    @FXML
    public void defaultBubbleStyle() {
        bubbleStyleInput.setText(ChartTools.DefaultBubbleStyle);
    }

    @FXML
    public void applyBubbleStyle() {
        UserConfig.setString(baseName + "BubbleStyle", bubbleStyleInput.getText());
        if (bubbleChart != null) {
            ChartTools.setBubbleChart​Colors(bubbleChart, bubbleStyleInput.getText(), palette, legendSide != null);
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
