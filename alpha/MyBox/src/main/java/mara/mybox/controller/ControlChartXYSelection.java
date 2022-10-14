package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.chart.ChartOptions.ChartType;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public class ControlChartXYSelection extends BaseController {

    protected ChartType chartType;
    protected String chartName;
    protected SimpleBooleanProperty typeNodify;

    @FXML
    protected ToggleGroup chartGroup;
    @FXML
    protected RadioButton barChartRadio, stackedBarChartRadio, lineChartRadio, scatterChartRadio,
            bubbleChartRadio, areaChartRadio, stackedAreaChartRadio;

    @Override
    public void initControls() {
        try {
            super.initControls();
            typeNodify = new SimpleBooleanProperty();

            checkType();
            chartGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        checkType();
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public ChartType checkType() {
        try {
            if (barChartRadio.isSelected()) {
                chartType = ChartType.Bar;
                chartName = message("BarChart");
            } else if (stackedBarChartRadio.isSelected()) {
                chartType = ChartType.StackedBar;
                chartName = message("StackedBarChart");
            } else if (lineChartRadio.isSelected()) {
                chartType = ChartType.Line;
                chartName = message("LineChart");
            } else if (scatterChartRadio.isSelected()) {
                chartType = ChartType.Scatter;
                chartName = message("ScatterChart");
            } else if (areaChartRadio.isSelected()) {
                chartType = ChartType.Area;
                chartName = message("AreaChart");
            } else if (stackedAreaChartRadio.isSelected()) {
                chartType = ChartType.StackedArea;
                chartName = message("StackedAreaChart");
            } else if (bubbleChartRadio.isSelected()) {
                chartType = ChartType.Bubble;
                chartName = message("BubbleChart");
            }

            typeNodify.set(!typeNodify.get());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return chartType;
    }

    public boolean isBubbleChart() {
        return bubbleChartRadio.isSelected();
    }

}
