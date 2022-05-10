package mara.mybox.controller;

import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.chart.ChartTools;
import mara.mybox.fxml.chart.ChartTools.LabelLocation;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DChartXYController extends BaseData2DChartFxController {

    protected ChartTools.ChartCoordinate nCoordinate, cCoordinate, xCoordinate, yCoordinate, sCoordinate;
    protected int categoryFontSize, categoryMargin, categoryTickRotation,
            numberFontSize, numberTickRotation;
    protected Side categorySide, numberSide;
    protected LabelLocation labelLocation;
    protected XYChart xyChart;
    protected Axis xAxis, yAxis, categoryAxis;
    protected CategoryAxis stringAxis;
    protected NumberAxis numberAxisY, numberAxisX;
    protected int lineWidth;

    @FXML
    protected Tab categoryTab, valueTab;
    @FXML
    protected RadioButton cartesianRadio, logarithmicERadio, logarithmic10Radio, squareRootRadio,
            categoryCartesianRadio, categorySquareRootRadio, categoryLogarithmicERadio, categoryLogarithmic10Radio,
            sizeCartesianRadio, sizeSquareRootRadio, sizeLogarithmicERadio, sizeLogarithmic10Radio;
    @FXML
    protected ComboBox<String> lineWdithSelector, tickFontSizeSelector, labelFontSizeSelector,
            categoryFontSizeSelector, categoryTickRotationSelector, categoryMarginSelector,
            numberFontSizeSelector, numberTickRotationSelector;
    @FXML
    protected FlowPane valueColumnPane, categoryColumnsPane, categoryCoordinatePane;
    @FXML
    protected TextField categoryLabel, numberLabel;
    @FXML
    protected CheckBox xyReverseCheck,
            categoryTickCheck, numberTickCheck, categoryMarkCheck, numberMarkCheck,
            hlinesCheck, vlinesCheck, hZeroCheck, vZeroCheck,
            categoryAxisAnimatedCheck, numberAxisAnimatedCheck, altColumnsFillCheck, altRowsFillCheck;
    @FXML
    protected ToggleGroup labelLocaionGroup, categorySideGroup, categoryCoordinateGroup,
            numberCoordinateGroup, numberSideGroup;

    @Override
    public void initControls() {
        try {
            super.initControls();

            initCategoryTab();
            initNumberTab();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initPlotTab() {
        try {
            super.initPlotTab();

            lineWidth = UserConfig.getInt(baseName + "LineWidth", 4);
            if (lineWidth < 0) {
                lineWidth = 1;
            }
            if (lineWdithSelector != null) {
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
                                    lineWidthChanged();

                                } else {
                                    lineWdithSelector.getEditor().setStyle(UserConfig.badStyle());
                                }
                            } catch (Exception e) {
                                lineWdithSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        });
            }

            tickFontSizeSelector.getItems().addAll(Arrays.asList(
                    "12", "14", "10", "8", "15", "16", "18", "9", "6", "4", "20", "24"
            ));
            tickFontSizeSelector.getSelectionModel().select(tickFontSize + "");
            tickFontSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            tickFontSize = v;
                            tickFontSizeSelector.getEditor().setStyle(null);
                            UserConfig.setInt(baseName + "TickFontSize", tickFontSize);
                            if (chart != null) {
                                redrawChart();
                            }
                        } else {
                            tickFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        tickFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            labelFontSizeSelector.getItems().addAll(Arrays.asList(
                    "12", "14", "10", "8", "15", "16", "18", "9", "6", "4", "20", "24"
            ));
            labelFontSizeSelector.getSelectionModel().select(labelFontSize + "");
            labelFontSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            labelFontSize = v;
                            labelFontSizeSelector.getEditor().setStyle(null);
                            UserConfig.setInt(baseName + "LabelFontSize", labelFontSize);
                            if (chart != null) {
                                redrawChart();
                            }
                        } else {
                            labelFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        labelFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            labelLocation = LabelLocation.Below;
            labelLocaionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues || newValue == null) {
                        return;
                    }
                    String value = ((RadioButton) newValue).getText();
                    if (message("Above").equals(value)) {
                        labelLocation = LabelLocation.Above;
                    } else if (message("Below").equals(value)) {
                        labelLocation = LabelLocation.Below;
                    } else if (message("Center").equals(value)) {
                        labelLocation = LabelLocation.Center;
                    }
                    redrawChart();
                }
            });

            if (xyReverseCheck != null) {
                xyReverseCheck.setSelected(UserConfig.getBoolean(baseName + "YX", false));
                xyReverseCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                    if (isSettingValues) {
                        return;
                    }
                    checkXYReverse();
                    UserConfig.setBoolean(baseName + "YX", xyReverseCheck.isSelected());
                    redrawChart();
                });
            }
            checkXYReverse();

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

            altColumnsFillCheck.setSelected(UserConfig.getBoolean(baseName + "AltColumnsFill", false));
            altColumnsFillCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "AltColumnsFill", altColumnsFillCheck.isSelected());
                if (xyChart != null) {
                    xyChart.setAlternativeColumnFillVisible(altColumnsFillCheck.isSelected());
                }
            });

            altRowsFillCheck.setSelected(UserConfig.getBoolean(baseName + "AltRowsFill", true));
            altRowsFillCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "AltRowsFill", altRowsFillCheck.isSelected());
                if (xyChart != null) {
                    xyChart.setAlternativeRowFillVisible(altRowsFillCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initCategoryTab() {
        try {
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
                            if (stringAxis != null) {
                                stringAxis.setStartMargin(categoryMargin);
                                stringAxis.setEndMargin(categoryMargin);
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

            cCoordinate = ChartTools.ChartCoordinate.Cartesian;
            categoryCoordinateGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        if (categoryLogarithmicERadio.isSelected()) {
                            cCoordinate = ChartTools.ChartCoordinate.LogarithmicE;
                        } else if (categoryLogarithmic10Radio.isSelected()) {
                            cCoordinate = ChartTools.ChartCoordinate.Logarithmic10;
                        } else if (categorySquareRootRadio.isSelected()) {
                            cCoordinate = ChartTools.ChartCoordinate.SquareRoot;
                        } else {
                            cCoordinate = ChartTools.ChartCoordinate.Cartesian;
                        }
                        redrawChart();
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initNumberTab() {
        try {
            numberTickCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayNumberAxis", true));
            numberTickCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayNumberAxis", numberTickCheck.isSelected());
                if (numberAxisY != null) {
                    numberAxisY.setTickLabelsVisible(numberTickCheck.isSelected());
                    chart.requestLayout();
                }
            });

            numberMarkCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayNumberMark", true));
            numberMarkCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayNumberMark", numberMarkCheck.isSelected());
                if (numberAxisY != null) {
                    numberAxisY.setTickMarkVisible(numberMarkCheck.isSelected());
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
            numberTickRotationSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> v, String ov, String nv) -> {
                try {
                    int d = Integer.valueOf(nv);
                    numberTickRotation = d;
                    numberTickRotationSelector.getEditor().setStyle(null);
                    UserConfig.setInt(baseName + "NumberTickRotation", numberTickRotation);
                    if (numberAxisY != null) {
                        numberAxisY.setTickLabelRotation(numberTickRotation);
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
            numberFontSizeSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v > 0) {
                        numberFontSize = v;
                        numberFontSizeSelector.getEditor().setStyle(null);
                        UserConfig.setInt(baseName + "NumberAxisFontSize", numberFontSize);
                        if (numberAxisY != null) {
                            numberAxisY.setStyle("-fx-font-size: " + numberFontSize + "px;");
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
                    if (numberAxisY != null) {
                        numberAxisY.setSide(numberSide);
                        chart.requestLayout();
                    }
                }
            });

            nCoordinate = ChartTools.ChartCoordinate.Cartesian;
            numberCoordinateGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        if (logarithmicERadio.isSelected()) {
                            nCoordinate = ChartTools.ChartCoordinate.LogarithmicE;
                        } else if (logarithmic10Radio.isSelected()) {
                            nCoordinate = ChartTools.ChartCoordinate.Logarithmic10;
                        } else if (squareRootRadio.isSelected()) {
                            nCoordinate = ChartTools.ChartCoordinate.SquareRoot;
                        } else {
                            nCoordinate = ChartTools.ChartCoordinate.Cartesian;
                        }
                        redrawChart();
                    });

            numberAxisAnimatedCheck.setSelected(UserConfig.getBoolean(baseName + "NumberAxisAnimated", false));
            numberAxisAnimatedCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "NumberAxisAnimated", numberAxisAnimatedCheck.isSelected());
                if (numberAxisY != null) {
                    numberAxisY.setAnimated(numberAxisAnimatedCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void lineWidthChanged() {
    }

    public void checkXYReverse() {
        try {
            if (isXY()) {
                categoryTab.setText(message("CategoryAxis") + "(" + message("HorizontalAxis") + ")");
                valueTab.setText(message("ValueAxis") + "(" + message("VerticalAxis") + ")");
            } else {
                categoryTab.setText(message("CategoryAxis") + "(" + message("VerticalAxis") + ")");
                valueTab.setText(message("ValueAxis") + "(" + message("HorizontalAxis") + ")");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
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

    public boolean isXY() {
        return xyReverseCheck == null || !xyReverseCheck.isSelected();
    }

    @Override
    public void clearChart() {
        super.clearChart();
        xyChart = null;
        xAxis = null;
        yAxis = null;
        categoryAxis = null;
        numberAxisY = null;
        numberAxisX = null;
        xCoordinate = null;
        yCoordinate = null;
        sCoordinate = null;
        palette = null;
    }

    public void makeAxis() {
        try {
            if (isCategoryNumbers()) {
                numberAxisX = new NumberAxis();
                categoryAxis = numberAxisX;
            } else {
                stringAxis = new CategoryAxis();
                categoryAxis = stringAxis;
            }
            initCategoryAxis(categoryAxis);

            numberAxisY = new NumberAxis();
            initValueAxis(numberAxisY);

            if (isXY()) {
                xAxis = categoryAxis;
                yAxis = numberAxisY;
                xCoordinate = cCoordinate;
                yCoordinate = nCoordinate;
            } else {
                xAxis = numberAxisY;
                yAxis = categoryAxis;
                xCoordinate = nCoordinate;
                yCoordinate = cCoordinate;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void makeXYChart() {
        try {
            initXYChart(xyChart);
            chart = xyChart;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void initCategoryAxis(Axis axis) {
        try {
            if (axis instanceof NumberAxis) {
                ChartTools.setChartCoordinate((NumberAxis) axis, cCoordinate);
            } else {
                CategoryAxis axisC = (CategoryAxis) axis;
                axisC.setGapStartAndEnd(true);
                if (isXY()) {
                    axisC.setEndMargin(100);
                } else {
                    axisC.setEndMargin(20);
                }
            }
            axis.setLabel(categoryLabel.getText());
            axis.setSide(categorySide);
            axis.setTickLabelsVisible(categoryTickCheck.isSelected());
            axis.setTickMarkVisible(categoryMarkCheck.isSelected());
            axis.setTickLabelRotation(categoryTickRotation);
            axis.setAnimated(categoryAxisAnimatedCheck.isSelected());
            axis.setStyle("-fx-font-size: " + categoryFontSize + "px;");

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void initValueAxis(NumberAxis axis) {
        try {
            axis.setLabel(numberLabel.getText());
            axis.setSide(numberSide);
            axis.setTickLabelsVisible(numberTickCheck.isSelected());
            axis.setTickMarkVisible(numberMarkCheck.isSelected());
            axis.setTickLabelRotation(numberTickRotation);
            axis.setAnimated(numberAxisAnimatedCheck.isSelected());
            ChartTools.setChartCoordinate(axis, nCoordinate);

            axis.setStyle("-fx-font-size: " + numberFontSize + "px;");
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void initXYChart(XYChart xyChart) {
        try {
            xyChart.setAlternativeRowFillVisible(altRowsFillCheck.isSelected());
            xyChart.setAlternativeColumnFillVisible(altColumnsFillCheck.isSelected());
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
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void writeXYChart(List<Data2DColumn> columns, List<List<String>> data, boolean rowNumber) {
        writeXYChart(xyChart, columns, data, null, rowNumber);
    }

    // The first column is and the second columns is "Category"
    public void writeXYChart(XYChart targetChart, List<Data2DColumn> columns, List<List<String>> data,
            List<Integer> colIndics, boolean rowNumber) {
        try {
            if (columns == null || data == null) {
                return;
            }
            targetChart.getData().clear();
            XYChart.Data xyData;
            int index = 0, startIndex = rowNumber ? 1 : 0;
            for (int col = 1 + startIndex; col < columns.size(); col++) {
                if (colIndics != null && !colIndics.contains(col)) {
                    continue;
                }
                Data2DColumn column = columns.get(col);
                String colName = column.getColumnName();
                XYChart.Series series = new XYChart.Series();
                series.setName(colName);

                double categoryValue, categoryCoordinateValue, numberValue, numberCoordinateValue;
                for (List<String> rowData : data) {
                    String category = rowData.get(startIndex);
                    numberValue = data2D.doubleValue(rowData.get(col));
                    numberCoordinateValue = ChartTools.coordinateValue(nCoordinate, numberValue);
                    categoryValue = data2D.doubleValue(category);
                    categoryCoordinateValue = ChartTools.coordinateValue(cCoordinate, categoryValue);
                    if (isXY()) {
                        if (targetChart.getXAxis() instanceof NumberAxis) {
                            xyData = new XYChart.Data(categoryCoordinateValue, numberCoordinateValue);
                        } else {
                            xyData = new XYChart.Data(category, numberCoordinateValue);
                        }
                    } else {
                        if (targetChart.getYAxis() instanceof NumberAxis) {
                            xyData = new XYChart.Data(numberCoordinateValue, categoryCoordinateValue);
                        } else {
                            xyData = new XYChart.Data(numberCoordinateValue, category);
                        }
                    }
                    series.getData().add(xyData);
                }

                targetChart.getData().add(index++, series);
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void defaultCategoryLabel() {
        categoryLabel.setText(message("Category") + ": " + categoryName());
    }

    @FXML
    public void goCategoryLabel() {
        if (numberAxisX != null) {
            numberAxisX.setLabel(categoryLabel.getText());
        } else if (categoryAxis != null) {
            categoryAxis.setLabel(categoryLabel.getText());
        }
    }

    @FXML
    public void defaultValueLabel() {
        String v = valuesNames();
        if (v != null) {
            numberLabel.setText(message("Values") + ": " + v);
        } else {
            numberLabel.setText(message("Value") + ": " + valueName());
        }
    }

    @FXML
    public void goValueLabel() {
        if (numberAxisY != null) {
            numberAxisY.setLabel(numberLabel.getText());
        }
    }


    /*
        get/set
     */
    public ChartTools.ChartCoordinate getxCoordinate() {
        return xCoordinate;
    }

    public ChartTools.ChartCoordinate getyCoordinate() {
        return yCoordinate;
    }

    public ChartTools.ChartCoordinate getsCoordinate() {
        return sCoordinate;
    }

    public XYChart getXyChart() {
        return xyChart;
    }

    public LabelLocation getLabelLocation() {
        return labelLocation;
    }

}
