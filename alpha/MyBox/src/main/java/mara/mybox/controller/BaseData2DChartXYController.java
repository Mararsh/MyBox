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
import javafx.scene.paint.Color;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.chart.ChartTools;
import mara.mybox.fxml.chart.ChartTools.LabelLocation;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DChartXYController extends BaseData2DFxChartController {

    protected ChartTools.ChartCoordinate nCoordinate, cCoordinate, xCoordinate, yCoordinate, sCoordinate;
    protected int categoryFontSize, categoryMargin, categoryTickRotation,
            numberFontSize, numberTickRotation;
    protected Side categorySide, numberSide;
    protected LabelLocation labelLocation;
    protected XYChart xyChart;
    protected Axis xAxis, yAxis, categoryAxis;
    protected CategoryAxis stringAxis;
    protected NumberAxis numberAxisY, numberAxisX;
    protected Map<String, String> palette;

    @FXML
    protected Tab categoryTab, valueTab;
    @FXML
    protected RadioButton cartesianRadio, logarithmicERadio, logarithmic10Radio, squareRootRadio,
            categoryCartesianRadio, categorySquareRootRadio, categoryLogarithmicERadio, categoryLogarithmic10Radio,
            sizeCartesianRadio, sizeSquareRootRadio, sizeLogarithmicERadio, sizeLogarithmic10Radio;
    @FXML
    protected ComboBox<String> tickFontSizeSelector, labelFontSizeSelector,
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
                                okAction();
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
                                okAction();
                            }
                        } else {
                            labelFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        labelFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            labelLocation = LabelLocation.Above;
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
                    okAction();
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
                    okAction();
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
                        okAction();
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
                        okAction();
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

    public void checkXYReverse() {
        try {
            if (isXY()) {
                categoryTab.setText(message("CategoryAxis") + "(X)");
                valueTab.setText(message("ValueAxis") + "(Y)");
            } else {
                categoryTab.setText(message("CategoryAxis") + "(Y)");
                valueTab.setText(message("ValueAxis") + "(X)");
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
                numberAxisX.setLabel(categoryName());
                numberAxisX.setSide(categorySide);
                numberAxisX.setTickLabelsVisible(categoryTickCheck.isSelected());
                numberAxisX.setTickMarkVisible(numberMarkCheck.isSelected());
                numberAxisX.setTickLabelRotation(categoryTickRotation);
                numberAxisX.setAnimated(categoryAxisAnimatedCheck.isSelected());
                ChartTools.setChartCoordinate(numberAxisX, cCoordinate);
                categoryAxis = numberAxisX;
            } else {
                stringAxis = new CategoryAxis();
                stringAxis.setLabel(categoryLabel.getText());
                stringAxis.setSide(categorySide);
                stringAxis.setTickLabelsVisible(categoryTickCheck.isSelected());
                stringAxis.setTickMarkVisible(categoryMarkCheck.isSelected());
                stringAxis.setTickLabelRotation(categoryTickRotation);
                stringAxis.setGapStartAndEnd(true);
                stringAxis.setAnimated(categoryAxisAnimatedCheck.isSelected());
                if (isXY()) {
                    stringAxis.setEndMargin(100);
                } else {
                    stringAxis.setEndMargin(20);
                }
                categoryAxis = stringAxis;
            }

            numberAxisY = new NumberAxis();
            numberAxisY.setLabel(numberLabel.getText());
            numberAxisY.setSide(numberSide);
            numberAxisY.setTickLabelsVisible(numberTickCheck.isSelected());
            numberAxisY.setTickMarkVisible(numberMarkCheck.isSelected());
            numberAxisY.setTickLabelRotation(numberTickRotation);
            numberAxisY.setAnimated(numberAxisAnimatedCheck.isSelected());
            ChartTools.setChartCoordinate(numberAxisY, nCoordinate);

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

            chart = xyChart;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void makeFinalChart() {
        try {
            if (chart == null) {
                return;
            }
            if (categoryAxis != null) {
                categoryAxis.setStyle("-fx-font-size: " + categoryFontSize + "px;");
            }
            if (numberAxisY != null) {
                numberAxisY.setStyle("-fx-font-size: " + numberFontSize + "px;");
            }
            super.makeFinalChart();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void writeXYChart() {
        try {
            palette = new HashMap();
            Random random = new Random();
            XYChart.Data xyData;
            for (int i = 0; i < checkedColsIndices.size(); i++) {
                int colIndex = checkedColsIndices.get(i);
                Data2DColumn column = data2D.column(colIndex);
                String colName = column.getColumnName();
                XYChart.Series series = new XYChart.Series();
                series.setName(colName);

                Color color = column.getColor();
                if (color == null) {
                    color = FxColorTools.randomColor(random);
                }
                String rgb = FxColorTools.color2rgb(color);
                palette.put(colName, rgb);
                double categoryValue, categoryCoordinateValue, numberValue, numberCoordinateValue;
                for (List<String> rowData : outputData) {
                    String category = rowData.get(0);
                    numberValue = data2D.doubleValue(rowData.get(i + 1));
                    numberCoordinateValue = ChartTools.coordinateValue(nCoordinate, numberValue);
                    categoryValue = data2D.doubleValue(category);
                    categoryCoordinateValue = ChartTools.coordinateValue(cCoordinate, categoryValue);
                    if (isXY()) {
                        if (xyChart.getXAxis() instanceof NumberAxis) {
                            xyData = new XYChart.Data(categoryCoordinateValue, numberCoordinateValue);
                        } else {
                            xyData = new XYChart.Data(category, numberCoordinateValue);
                        }
                    } else {
                        if (xyChart.getYAxis() instanceof NumberAxis) {
                            xyData = new XYChart.Data(numberCoordinateValue, categoryCoordinateValue);
                        } else {
                            xyData = new XYChart.Data(numberCoordinateValue, category);
                        }
                    }
                    series.getData().add(xyData);
                }

                xyChart.getData().add(i, series);

            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void defaultCategoryLabel() {
        categoryLabel.setText(categoryName());
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
        numberLabel.setText(valuesNames());
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
