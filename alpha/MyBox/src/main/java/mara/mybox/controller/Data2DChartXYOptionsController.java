package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.BoxWhiskerChart;
import mara.mybox.fxml.chart.ResidualChart;
import mara.mybox.fxml.chart.SimpleRegressionChart;
import mara.mybox.fxml.chart.XYChartMaker;
import mara.mybox.fxml.chart.XYChartOptions;
import mara.mybox.fxml.chart.XYChartOptions.ChartCoordinate;
import mara.mybox.fxml.chart.XYChartOptions.LabelLocation;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-5-11
 * @License Apache License Version 2.0
 */
public class Data2DChartXYOptionsController extends BaseData2DChartFxOptionsController {

    protected ControlData2DChartXY xyChartController;
    protected XYChartMaker chartMaker;

    @FXML
    protected Tab categoryTab, valueTab;
    @FXML
    protected VBox plotBox, xyPlotBox, bubbleBox, categoryBox, categoryNumbersBox;
    @FXML
    protected HBox barGapBox, categoryGapBox, lineWidthBox, symbolSizeBox;
    @FXML
    protected ToggleGroup titleSideGroup, labelLocaionGroup, sortGroup,
            categorySideGroup, categoryCoordinateGroup, categoryValuesGroup,
            numberCoordinateGroup, numberSideGroup, sizeCoordinateGroup;
    @FXML
    protected CheckBox xyReverseCheck, hlinesCheck, vlinesCheck, hZeroCheck, vZeroCheck,
            categoryTickCheck, categoryMarkCheck, numberMarkCheck, numberTickCheck;
    @FXML
    protected RadioButton cartesianRadio, logarithmicERadio, logarithmic10Radio, squareRootRadio,
            categoryStringRadio, categoryNumberRadio, labelLocaionAboveRadio,
            categoryCartesianRadio, categorySquareRootRadio, categoryLogarithmicERadio, categoryLogarithmic10Radio,
            sizeCartesianRadio, sizeSquareRootRadio, sizeLogarithmicERadio, sizeLogarithmic10Radio,
            sortVertivalRadio, sortHorizontalRadio;
    @FXML
    protected ComboBox<String> labelFontSizeSelector, lineWidthSelector, symbolSizeSelector, tickFontSizeSelector,
            categoryFontSizeSelector, categoryTickRotationSelector, barGapSelector, categoryGapSelector,
            numberFontSizeSelector, numberTickRotationSelector;
    @FXML
    protected TextField categoryInput, valueInput;
    @FXML
    protected TextArea bubbleStyleInput;

    public Data2DChartXYOptionsController() {
    }

    public void setParameters(ControlData2DChartXY xyChartController) {
        try {
            this.xyChartController = xyChartController;
            this.chartMaker = xyChartController.chartMaker;

            chartController = xyChartController;
            options = chartMaker;
            chartName = options.getChartName();
            titleLabel.setText(chartName);

            isSettingValues = true;
            initDataTab();
            initPlotTab();
            initCategoryTab();
            initNumberTab();
            Chart chart = chartMaker.getChart();
            if (chart instanceof BubbleChart
                    || chart instanceof SimpleRegressionChart
                    || chart instanceof ResidualChart) {
                categoryStringRadio.setDisable(true);
                categoryNumberRadio.setSelected(true);
            } else {
                categoryStringRadio.setDisable(false);

                if (!(chart instanceof LineChart) && !(chart instanceof ScatterChart)) {
                    categoryStringRadio.setSelected(true);
                    categoryBox.getChildren().remove(categoryNumbersBox);
                }
            }

            if (!(chart instanceof BubbleChart)) {
                plotBox.getChildren().remove(bubbleBox);
            }

            if (chart instanceof BarChart || chart instanceof StackedBarChart) {
                if (pointRadio.isSelected()) {
                    noRadio.setSelected(true);
                }
                pointRadio.setDisable(true);
            } else {
                pointRadio.setDisable(false);
                categoryBox.getChildren().removeAll(barGapBox, categoryGapBox);

            }

            if (chart instanceof LineChart
                    || chart instanceof AreaChart
                    || chart instanceof StackedAreaChart
                    || chart instanceof BoxWhiskerChart) {
            } else {
                xyPlotBox.getChildren().removeAll(lineWidthBox);

                if (!(chart instanceof ScatterChart)) {
                    xyPlotBox.getChildren().removeAll(symbolSizeBox);
                }

            }

            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }


    /*
        data
     */
    @Override
    public void initDataTab() {
        try {
            super.initDataTab();

            int labelFontSize = chartMaker.getLabelFontSize();
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
                            chartMaker.setLabelFontSize(v);
                            labelFontSizeSelector.getEditor().setStyle(null);
                            chartController.redraw();
                        } else {
                            labelFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        labelFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            NodeTools.setRadioSelected(labelLocaionGroup, message(chartMaker.getLabelLocation().name()));
            labelLocaionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues || newValue == null) {
                        return;
                    }
                    String value = ((RadioButton) newValue).getText();
                    LabelLocation labelLocation = LabelLocation.Center;
                    if (message("Above").equals(value)) {
                        labelLocation = LabelLocation.Above;
                    } else if (message("Below").equals(value)) {
                        labelLocation = LabelLocation.Below;
                    }
                    chartMaker.setLabelLocation(labelLocation);
                    chartController.redraw();
                }
            });

//            sortGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
//                @Override
//                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
//                    if (isSettingValues || newValue == null) {
//                        return;
//                    }
//                    if (sortVertivalRadio.isSelected()) {
//                        chartMaker.setSort(XYChartOptions.Sort.Y);
//                    } else if (sortHorizontalRadio.isSelected()) {
//                        chartMaker.setSort(XYChartOptions.Sort.X);
//                    } else {
//                        chartMaker.setSort(XYChartOptions.Sort.None);
//                    }
//                    chartController.redraw();
//                }
//            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        plot
     */
    @Override
    public void initPlotTab() {
        try {
            super.initPlotTab();

            NodeTools.setRadioSelected(titleSideGroup, message(options.getTitleSide().name()));
            titleSideGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues || newValue == null) {
                        return;
                    }
                    String value = ((RadioButton) newValue).getText();
                    options.setTitleSide(Side.LEFT);
                    if (message("Top").equals(value)) {
                        options.setTitleSide(Side.TOP);
                    } else if (message("Bottom").equals(value)) {
                        options.setTitleSide(Side.BOTTOM);
                    } else if (message("Left").equals(value)) {
                        options.setTitleSide(Side.LEFT);
                    } else if (message("Right").equals(value)) {
                        options.setTitleSide(Side.RIGHT);
                    }
                }
            });

            lineWidthSelector.getItems().addAll(Arrays.asList(
                    "4", "1", "2", "3", "5", "6", "7", "8", "9", "10"
            ));
            lineWidthSelector.getSelectionModel().select(chartMaker.getLineWidth() + "");
            lineWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            chartMaker.setLineWidth(v);
                            lineWidthSelector.getEditor().setStyle(null);
                            chartController.redraw();
                        } else {
                            lineWidthSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        lineWidthSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            symbolSizeSelector.getItems().addAll(Arrays.asList(
                    "10", "8", "6", "4", "2", "1", "20", "16", "18", "30"
            ));
            symbolSizeSelector.getSelectionModel().select(chartMaker.getSymbolSize() + "");
            symbolSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            chartMaker.setSymbolSize(v);
                            symbolSizeSelector.getEditor().setStyle(null);
                            chartController.redraw();
                        } else {
                            symbolSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        symbolSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            int tickFontSize = options.getTickFontSize();
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
                            options.setTickFontSize(v);
                            tickFontSizeSelector.getEditor().setStyle(null);
                        } else {
                            tickFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        tickFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            xyReverseCheck.setSelected(!chartMaker.isIsXY());
            xyReverseCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                checkXYReverse();
                chartMaker.setIsXY(!xyReverseCheck.isSelected());
                chartController.redraw();
            });
            checkXYReverse();

            hlinesCheck.setSelected(chartMaker.isDisplayHlines());
            hlinesCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                chartMaker.setDisplayHlines(hlinesCheck.isSelected());
            });

            vlinesCheck.setSelected(chartMaker.isDisplayVlines());
            vlinesCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                chartMaker.setDisplayVlines(vlinesCheck.isSelected());
            });

            hZeroCheck.setSelected(chartMaker.isDisplayHZero());
            hZeroCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                chartMaker.setDisplayHZero(hZeroCheck.isSelected());
            });

            vZeroCheck.setSelected(chartMaker.isDisplayVZero());
            vZeroCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                chartMaker.setDisplayVZero(vZeroCheck.isSelected());
            });

            NodeTools.setRadioSelected(sizeCoordinateGroup, message(chartMaker.getSizeCoordinate().name()));
            sizeCoordinateGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        ChartCoordinate sizeCoordinate = ChartCoordinate.Cartesian;
                        if (sizeLogarithmicERadio.isSelected()) {
                            sizeCoordinate = ChartCoordinate.LogarithmicE;
                        } else if (sizeLogarithmic10Radio.isSelected()) {
                            sizeCoordinate = ChartCoordinate.Logarithmic10;
                        } else if (sizeSquareRootRadio.isSelected()) {
                            sizeCoordinate = ChartCoordinate.SquareRoot;
                        }
                        chartMaker.setSizeCoordinate(sizeCoordinate);
                        chartController.redraw();
                    });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void checkXYReverse() {
        try {
            if (xyReverseCheck.isSelected()) {
                categoryTab.setText(message("CategoryAxis") + "(" + message("Vertical") + ")");
                valueTab.setText(message("ValueAxis") + "(" + message("Horizontal") + ")");
            } else {
                categoryTab.setText(message("CategoryAxis") + "(" + message("Horizontal") + ")");
                valueTab.setText(message("ValueAxis") + "(" + message("Vertical") + ")");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void defaultBubbleStyle() {
        bubbleStyleInput.setText(XYChartOptions.DefaultBubbleStyle);
    }

    @FXML
    public void applyBubbleStyle() {
        chartMaker.setBubbleStyle(StringTools.replaceLineBreak(bubbleStyleInput.getText()));
    }

    @FXML
    public void cssGuide() {
        openLink(HelpTools.javaFxCssLink());
    }

    /*
        category
     */
    public void initCategoryTab() {
        try {
            categoryInput.setText(chartMaker.getCategoryLabel());

            categoryTickCheck.setSelected(chartMaker.isDisplayCategoryTick());
            categoryTickCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                chartMaker.setDisplayCategoryTick(categoryTickCheck.isSelected());
            });

            categoryMarkCheck.setSelected(chartMaker.isDisplayCategoryMark());
            categoryMarkCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                chartMaker.setDisplayCategoryMark(categoryMarkCheck.isSelected());
            });

            int categoryTickRotation = chartMaker.getCategoryTickRotation();
            categoryTickRotationSelector.getItems().addAll(Arrays.asList(
                    "90", "45", "0", "30", "15", "60", "135", "120", "105", "150"
            ));
            categoryTickRotationSelector.getSelectionModel().select(categoryTickRotation + "");
            categoryTickRotationSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> v, String ov, String nv) -> {
                        try {
                            chartMaker.setCategoryTickRotation(Integer.parseInt(nv));
                            categoryTickRotationSelector.getEditor().setStyle(null);
                        } catch (Exception e) {
                            categoryTickRotationSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            int categoryFontSize = chartMaker.getCategoryFontSize();
            categoryFontSizeSelector.getItems().addAll(Arrays.asList(
                    "12", "14", "10", "8", "15", "16", "18", "9", "6", "4", "20", "24"
            ));
            categoryFontSizeSelector.getSelectionModel().select(categoryFontSize + "");
            categoryFontSizeSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            chartMaker.setCategoryFontSize(Integer.parseInt(newValue));
                            categoryFontSizeSelector.getEditor().setStyle(null);
                        } catch (Exception e) {
                            categoryFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            NodeTools.setRadioSelected(categorySideGroup, message(chartMaker.getCategorySide().name()));
            categorySideGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues || newValue == null) {
                        return;
                    }
                    String value = ((RadioButton) newValue).getText();
                    Side categorySide = Side.BOTTOM;
                    if (message("Top").equals(value)) {
                        categorySide = Side.TOP;
                    } else if (message("Left").equals(value)) {
                        categorySide = Side.LEFT;
                    } else if (message("Right").equals(value)) {
                        categorySide = Side.RIGHT;
                    }
                    chartMaker.setCategorySide(categorySide);
                }
            });

            if (chartMaker.isCategoryIsNumbers()) {
                categoryNumberRadio.setSelected(true);
            } else {
                categoryStringRadio.setSelected(true);
            }
            categoryValuesGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    chartMaker.setCategoryIsNumbers(categoryNumberRadio.isSelected());
                    UserConfig.setBoolean(chartName + "CategoryIsNumbers", categoryNumberRadio.isSelected());
                    chartController.redraw();
                }
            });

            NodeTools.setRadioSelected(categoryCoordinateGroup, message(chartMaker.getCategoryCoordinate().name()));
            categoryCoordinateGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        ChartCoordinate categoryCoordinate = ChartCoordinate.Cartesian;
                        if (categoryLogarithmicERadio.isSelected()) {
                            categoryCoordinate = ChartCoordinate.LogarithmicE;
                        } else if (categoryLogarithmic10Radio.isSelected()) {
                            categoryCoordinate = ChartCoordinate.Logarithmic10;
                        } else if (categorySquareRootRadio.isSelected()) {
                            categoryCoordinate = ChartCoordinate.SquareRoot;
                        }
                        chartMaker.setCategoryCoordinate(categoryCoordinate);
                        chartController.redraw();
                    });

            double barGap = chartMaker.getBarGap();
            barGapSelector.getItems().addAll(Arrays.asList(
                    "1", "0", "0.5", "2", "4", "1.5", "5", "8", "10", "20", "30", "40", "50"
            ));
            barGapSelector.getSelectionModel().select(barGap + "");
            barGapSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> v, String ov, String nv) -> {
                        try {
                            double d = Double.parseDouble(nv);
                            if (d >= 0) {
                                chartMaker.setBarGap(d);
                                barGapSelector.getEditor().setStyle(null);
                                chartController.redraw();
                            } else {
                                barGapSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            barGapSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            double categoryGap = chartMaker.getCategoryGap();
            categoryGapSelector.getItems().addAll(Arrays.asList(
                    "1", "0", "0.5", "2", "4", "5", "8", "20", "10", "30", "1.5", "40", "50"
            ));
            categoryGapSelector.getSelectionModel().select(categoryGap + "");
            categoryGapSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> v, String ov, String nv) -> {
                        try {
                            double d = Double.parseDouble(nv);
                            if (d >= 0) {
                                chartMaker.setCategoryGap(d);
                                categoryGapSelector.getEditor().setStyle(null);
                                chartController.redraw();
                            } else {
                                categoryGapSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            categoryGapSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            bubbleStyleInput.setText(chartMaker.getBubbleStyle());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void defaultCategoryLabel() {
        categoryInput.setText(chartMaker.getDefaultCategoryLabel());
    }

    @FXML
    public void goCategoryLabel() {
        chartMaker.setCategoryLabel(categoryInput.getText());
    }

    /*
        number
     */
    public void initNumberTab() {
        try {
            valueInput.setText(chartMaker.getValueLabel());

            numberTickCheck.setSelected(chartMaker.isDisplayNumberTick());
            numberTickCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                chartMaker.setDisplayNumberTick(numberTickCheck.isSelected());
            });

            numberMarkCheck.setSelected(chartMaker.isDisplayNumberMark());
            numberMarkCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                chartMaker.setDisplayNumberMark(numberMarkCheck.isSelected());
            });

            int numberTickRotation = chartMaker.getNumberTickRotation();
            numberTickRotationSelector.getItems().addAll(Arrays.asList(
                    "0", "90", "45", "30", "15", "60", "135", "120", "105", "150"
            ));
            numberTickRotationSelector.getSelectionModel().select(numberTickRotation + "");
            numberTickRotationSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> v, String ov, String nv) -> {
                try {
                    int d = Integer.parseInt(nv);
                    chartMaker.setNumberTickRotation(Integer.parseInt(nv));
                    numberTickRotationSelector.getEditor().setStyle(null);
                } catch (Exception e) {
                    numberTickRotationSelector.getEditor().setStyle(UserConfig.badStyle());
                }
            });

            int numberFontSize = chartMaker.getNumberFontSize();
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
                        chartMaker.setNumberFontSize(v);
                        numberFontSizeSelector.getEditor().setStyle(null);
                    } else {
                        numberFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                } catch (Exception e) {
                    numberFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                }
            });

            NodeTools.setRadioSelected(numberSideGroup, message(chartMaker.getNumberSide().name()));
            numberSideGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues || newValue == null) {
                        return;
                    }
                    String value = ((RadioButton) newValue).getText();
                    Side numberSide = Side.LEFT;
                    if (message("Top").equals(value)) {
                        numberSide = Side.TOP;
                    } else if (message("Bottom").equals(value)) {
                        numberSide = Side.BOTTOM;
                    } else if (message("Right").equals(value)) {
                        numberSide = Side.RIGHT;
                    }
                    chartMaker.setNumberSide(numberSide);
                }
            });

            NodeTools.setRadioSelected(numberCoordinateGroup, message(chartMaker.getNumberCoordinate().name()));
            numberCoordinateGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        ChartCoordinate numberCoordinate = ChartCoordinate.Cartesian;
                        if (logarithmicERadio.isSelected()) {
                            numberCoordinate = ChartCoordinate.LogarithmicE;
                        } else if (logarithmic10Radio.isSelected()) {
                            numberCoordinate = ChartCoordinate.Logarithmic10;
                        } else if (squareRootRadio.isSelected()) {
                            numberCoordinate = ChartCoordinate.SquareRoot;
                        }
                        chartMaker.setNumberCoordinate(numberCoordinate);
                        chartController.redraw();
                    });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void defaultValueLabel() {
        valueInput.setText(chartMaker.getDefaultValueLabel());
    }

    @FXML
    public void goValueLabel() {
        chartMaker.setValueLabel(valueInput.getText());
    }

    /*
        static methods
     */
    public static Data2DChartXYOptionsController open(ControlData2DChartXY chartController) {
        try {
            if (chartController == null) {
                return null;
            }
            Data2DChartXYOptionsController controller = (Data2DChartXYOptionsController) WindowTools.referredTopStage(
                    chartController, Fxmls.Data2DChartXYOptionsFxml);
            controller.setParameters(chartController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
