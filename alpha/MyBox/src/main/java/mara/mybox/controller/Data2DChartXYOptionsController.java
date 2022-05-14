package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.XYChartOptions;
import mara.mybox.fxml.chart.XYChartOptions.ChartCoordinate;
import mara.mybox.fxml.chart.XYChartOptions.LabelLocation;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-5-11
 * @License Apache License Version 2.0
 */
public class Data2DChartXYOptionsController extends Data2DChartFxOptionsController {

    protected ControlData2DChartXY xyChartController;
    protected XYChartOptions xyOptions;
    protected XYChart xyChart;
    protected String chartName;

    @FXML
    protected Tab categoryTab, valueTab;
    @FXML
    protected VBox plotBox, xyPlotBox, bubbleBox, categoryBox, categoryNumbersBox;
    @FXML
    protected HBox barGapBox;
    @FXML
    protected ToggleGroup labelLocaionGroup,
            categorySideGroup, categoryCoordinateGroup, categoryValuesGroup,
            numberCoordinateGroup, numberSideGroup, sizeCoordinateGroup;
    @FXML
    protected CheckBox xyReverseCheck, hlinesCheck, vlinesCheck, hZeroCheck, vZeroCheck,
            altColumnsFillCheck, altRowsFillCheck,
            categoryTickCheck, categoryMarkCheck, numberMarkCheck, numberTickCheck;
    @FXML
    protected RadioButton cartesianRadio, logarithmicERadio, logarithmic10Radio, squareRootRadio,
            categoryStringRadio, categoryNumberRadio, labelLocaionAboveRadio,
            categoryCartesianRadio, categorySquareRootRadio, categoryLogarithmicERadio, categoryLogarithmic10Radio,
            sizeCartesianRadio, sizeSquareRootRadio, sizeLogarithmicERadio, sizeLogarithmic10Radio;
    @FXML
    protected ComboBox<String> lineWdithSelector, tickFontSizeSelector,
            categoryFontSizeSelector, categoryTickRotationSelector, categoryMarginSelector, barGapSelector, categoryGapSelector,
            numberFontSizeSelector, numberTickRotationSelector;
    @FXML
    protected TextField categoryInput, valueInput, bubbleStyleInput;

    public Data2DChartXYOptionsController() {
    }

    public void setParameters(ControlData2DChartXY xyChartController) {
        try {
            this.xyChartController = xyChartController;
            this.xyOptions = xyChartController.xyOptions;

            chartController = xyChartController;
            options = xyOptions;
            chart = xyOptions.getChart();
            xyChart = xyOptions.getXyChart();
            chartName = xyOptions.getChartName();
            titleLabel.setText(chartName);

            isSettingValues = true;
            initDataTab();
            initPlotTab();
            initCategoryTab();
            initNumberTab();
            if (chart instanceof BubbleChart) {
                categoryStringRadio.setDisable(true);
            } else {
                plotBox.getChildren().removeAll(bubbleBox);
                if (!(chart instanceof LineChart) && !(chart instanceof ScatterChart)) {
                    categoryStringRadio.fire();
                    categoryBox.getChildren().removeAll(categoryNumbersBox);
                }
            }

            if (chart instanceof BarChart) {
                labelLocaionAboveRadio.fire();
            } else {
                categoryBox.getChildren().removeAll(barGapBox);
            }
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }


    /*
        data
     */
    @Override
    public void initDataTab() {
        try {
            super.initDataTab();

            int labelFontSize = xyOptions.getLabelFontSize();
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
                            xyOptions.setLabelFontSize(v);
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

            MyBoxLog.console(xyOptions.getLabelLocation().name());
            NodeTools.setRadioSelected(labelLocaionGroup, message(xyOptions.getLabelLocation().name()));
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
                    xyOptions.setLabelLocation(labelLocation);
                    chartController.redraw();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        plot
     */
    @Override
    public void initPlotTab() {
        try {
            super.initPlotTab();

            int lineWidth = xyOptions.getLineWidth();
            lineWdithSelector.getItems().addAll(Arrays.asList(
                    "4", "1", "2", "3", "5", "6", "7", "8", "9", "10"
            ));
            lineWdithSelector.getSelectionModel().select(lineWidth + "");
            lineWdithSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v >= 0) {
                                xyOptions.setLineWidth(v);
                                lineWdithSelector.getEditor().setStyle(null);
                                chartController.redraw();
                            } else {
                                lineWdithSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            lineWdithSelector.getEditor().setStyle(UserConfig.badStyle());
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

            xyReverseCheck.setSelected(!xyOptions.isIsXY());
            xyReverseCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                checkXYReverse();
                xyOptions.setIsXY(!xyReverseCheck.isSelected());
                chartController.redraw();
            });
            checkXYReverse();

            hlinesCheck.setSelected(xyOptions.isDisplayHlines());
            hlinesCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                xyOptions.setDisplayHlines(hlinesCheck.isSelected());
            });

            vlinesCheck.setSelected(xyOptions.isDisplayVlines());
            vlinesCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                xyOptions.setDisplayVlines(vlinesCheck.isSelected());
            });

            hZeroCheck.setSelected(xyOptions.isDisplayHZero());
            hZeroCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                xyOptions.setDisplayHZero(hZeroCheck.isSelected());
            });

            vZeroCheck.setSelected(xyOptions.isDisplayVZero());
            vZeroCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                xyOptions.setDisplayVZero(vZeroCheck.isSelected());
            });

            altColumnsFillCheck.setSelected(xyOptions.isAltColumnsFill());
            altColumnsFillCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                xyOptions.setAltColumnsFill(altColumnsFillCheck.isSelected());
            });

            altRowsFillCheck.setSelected(xyOptions.isAltRowsFill());
            altRowsFillCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                xyOptions.setAltRowsFill(altRowsFillCheck.isSelected());
            });

            NodeTools.setRadioSelected(sizeCoordinateGroup, xyOptions.getSizeCoordinate().name());
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
                        xyOptions.setSizeCoordinate(sizeCoordinate);
                        chartController.redraw();
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
        xyOptions.setBubbleStyle(bubbleStyleInput.getText());
    }

    /*
        category
     */
    public void initCategoryTab() {
        try {
            categoryInput.setText(xyOptions.getCategoryLabel());

            categoryTickCheck.setSelected(xyOptions.isDisplayCategoryTick());
            categoryTickCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                xyOptions.setDisplayCategoryTick(categoryTickCheck.isSelected());
            });

            categoryMarkCheck.setSelected(xyOptions.isDisplayCategoryMark());
            categoryMarkCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                xyOptions.setDisplayCategoryMark(categoryMarkCheck.isSelected());
            });

            int categoryTickRotation = xyOptions.getCategoryTickRotation();
            categoryTickRotationSelector.getItems().addAll(Arrays.asList(
                    "90", "45", "0", "30", "15", "60", "135", "120", "105", "150"
            ));
            categoryTickRotationSelector.getSelectionModel().select(categoryTickRotation + "");
            categoryTickRotationSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> v, String ov, String nv) -> {
                        try {
                            xyOptions.setCategoryTickRotation(Integer.valueOf(nv));
                            categoryTickRotationSelector.getEditor().setStyle(null);
                        } catch (Exception e) {
                            categoryTickRotationSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            int categoryFontSize = xyOptions.getCategoryFontSize();
            categoryFontSizeSelector.getItems().addAll(Arrays.asList(
                    "12", "14", "10", "8", "15", "16", "18", "9", "6", "4", "20", "24"
            ));
            categoryFontSizeSelector.getSelectionModel().select(categoryFontSize + "");
            categoryFontSizeSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            xyOptions.setCategoryFontSize(Integer.parseInt(newValue));
                            categoryFontSizeSelector.getEditor().setStyle(null);
                        } catch (Exception e) {
                            categoryFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            int categoryMargin = xyOptions.getCategoryMargin();
            categoryMarginSelector.getItems().addAll(Arrays.asList(
                    "12", "14", "10", "8", "15", "16", "18", "9", "6", "4", "20", "24"
            ));
            categoryMarginSelector.getSelectionModel().select(categoryMargin + "");
            categoryMarginSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            xyOptions.setCategoryMargin(Integer.parseInt(newValue));
                            categoryMarginSelector.getEditor().setStyle(null);
                        } catch (Exception e) {
                            categoryMarginSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            NodeTools.setRadioSelected(categorySideGroup, xyOptions.getCategorySide().name());
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
                    xyOptions.setCategorySide(categorySide);
                }
            });

            if (xyOptions.isCategoryIsNumbers()) {
                categoryNumberRadio.fire();
            } else {
                categoryStringRadio.fire();
            }
            categoryValuesGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    xyOptions.setCategoryIsNumbers(categoryNumberRadio.isSelected());
                    chartController.redraw();
                }
            });

            NodeTools.setRadioSelected(categoryCoordinateGroup, xyOptions.getCategoryCoordinate().name());
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
                        xyOptions.setCategoryCoordinate(categoryCoordinate);
                        chartController.redraw();
                    });

            double barGap = xyOptions.getBarGap();
            barGapSelector.getItems().addAll(Arrays.asList(
                    "1", "0", "0.5", "2", "4", "1.5", "5", "8", "10", "20", "30", "40", "50"
            ));
            barGapSelector.getSelectionModel().select(barGap + "");
            barGapSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> v, String ov, String nv) -> {
                        try {
                            double d = Double.valueOf(nv);
                            if (d >= 0) {
                                xyOptions.setBarGap(d);
                                barGapSelector.getEditor().setStyle(null);
                                chartController.redraw();
                            } else {
                                barGapSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            barGapSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            double categoryGap = xyOptions.getCategoryGap();
            categoryGapSelector.getItems().addAll(Arrays.asList(
                    "20", "10", "30", "5", "8", "1", "0", "0.5", "2", "4", "1.5", "40", "50"
            ));
            categoryGapSelector.getSelectionModel().select(categoryGap + "");
            categoryGapSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> v, String ov, String nv) -> {
                        try {
                            double d = Double.valueOf(nv);
                            if (d >= 0) {
                                xyOptions.setCategoryGap(d);
                                categoryGapSelector.getEditor().setStyle(null);
                                chartController.redraw();
                            } else {
                                categoryGapSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            categoryGapSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void defaultCategoryLabel() {
        categoryInput.setText(xyOptions.getDefaultCategoryLabel());
    }

    @FXML
    public void goCategoryLabel() {
        xyOptions.setCategoryLabel(categoryInput.getText());
    }

    /*
        number
     */
    public void initNumberTab() {
        try {
            valueInput.setText(xyOptions.getValueLabel());

            numberTickCheck.setSelected(xyOptions.isDisplayNumberTick());
            numberTickCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                xyOptions.setDisplayNumberTick(numberTickCheck.isSelected());
            });

            numberMarkCheck.setSelected(xyOptions.isDisplayNumberMark());
            numberMarkCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                xyOptions.setDisplayNumberMark(numberMarkCheck.isSelected());
            });

            int numberTickRotation = xyOptions.getNumberTickRotation();
            numberTickRotationSelector.getItems().addAll(Arrays.asList(
                    "0", "90", "45", "30", "15", "60", "135", "120", "105", "150"
            ));
            numberTickRotationSelector.getSelectionModel().select(numberTickRotation + "");
            numberTickRotationSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> v, String ov, String nv) -> {
                try {
                    int d = Integer.valueOf(nv);
                    xyOptions.setNumberTickRotation(Integer.valueOf(nv));
                    numberTickRotationSelector.getEditor().setStyle(null);
                } catch (Exception e) {
                    numberTickRotationSelector.getEditor().setStyle(UserConfig.badStyle());
                }
            });

            int numberFontSize = xyOptions.getNumberFontSize();
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
                        xyOptions.setNumberFontSize(v);
                        numberFontSizeSelector.getEditor().setStyle(null);
                    } else {
                        numberFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                } catch (Exception e) {
                    numberFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                }
            });

            NodeTools.setRadioSelected(numberSideGroup, xyOptions.getNumberSide().name());
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
                    xyOptions.setNumberSide(numberSide);
                }
            });

            NodeTools.setRadioSelected(numberCoordinateGroup, xyOptions.getNumberCoordinate().name());
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
                        xyOptions.setNumberCoordinate(numberCoordinate);
                        chartController.redraw();
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void defaultValueLabel() {
        valueInput.setText(xyOptions.getDefaultValueLabel());
    }

    @FXML
    public void goValueLabel() {
        xyOptions.setValueLabel(valueInput.getText());
    }

    /*
        static methods
     */
    public static Data2DChartXYOptionsController open(ControlData2DChartXY chartController) {
        try {
            if (chartController == null) {
                return null;
            }
            Data2DChartXYOptionsController controller = (Data2DChartXYOptionsController) WindowTools.openChildStage(
                    chartController.getMyWindow(), Fxmls.Data2DChartXYOptionsFxml, false);
            controller.setParameters(chartController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
