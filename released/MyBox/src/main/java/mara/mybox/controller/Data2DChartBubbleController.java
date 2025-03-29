package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.ChartOptions.ChartType;
import mara.mybox.fxml.chart.XYChartMaker;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public class Data2DChartBubbleController extends BaseData2DChartController {

    protected XYChartMaker chartMaker;
    protected Data2DColumn categoryColumn;
    protected int categoryIndex;
    protected List<Integer> valueIndices;

    @FXML
    protected CheckBox xyReverseCheck;
    @FXML
    protected VBox columnsBox, columnCheckBoxsBox;
    @FXML
    protected FlowPane categoryColumnsPane;
    @FXML
    protected ControlData2DChartXY chartController;

    public Data2DChartBubbleController() {
        baseTitle = message("BubbleChart");
        TipsLabelKey = "DataChartXYTips";
    }

    @Override
    public void initOptions() {
        try {
            super.initOptions();

            chartMaker = chartController.chartMaker;
            chartController.redrawNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    drawChart();
                }
            });

            if (xyReverseCheck != null) {
                xyReverseCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        initChart();
                        drawChart();
                    }
                });
            }

            initChart();
            drawXYChart();

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
            dataColsIndices = new ArrayList<>();

            int categoryCol = data2D.colOrder(selectedCategory);
            if (categoryCol < 0) {
                popError(message("SelectToHandle") + ": " + message("Column"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            categoryColumn = data2D.column(categoryCol);
            dataColsIndices.add(categoryCol);

            categoryIndex = showRowNumber() ? 1 : 0;

            valueIndices = new ArrayList<>();
            int valueCol = data2D.colOrder(selectedValue);
            if (valueCol < 0) {
                popError(message("SelectToHandle") + ": " + message("Column"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            int pos = dataColsIndices.indexOf(valueCol);
            if (pos >= 0) {
                valueIndices.add(pos + categoryIndex);
            } else {
                valueIndices.add(dataColsIndices.size() + categoryIndex);
                dataColsIndices.add(valueCol);
            }

            for (int col : checkedColsIndices) {
                pos = dataColsIndices.indexOf(col);
                if (pos >= 0) {
                    valueIndices.add(pos + categoryIndex);
                } else {
                    valueIndices.add(dataColsIndices.size() + categoryIndex);
                    dataColsIndices.add(col);
                }
            }

            return initChart();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public String baseChartTitle() {
        String title = selectedCategory
                + " - " + selectedValue
                + " - " + checkedColsNames;
        return title;
    }

    public boolean initChart() {
        if (categoryColumn != null) {
            return initChart(categoryColumn.isDBNumberType());
        } else {
            return false;
        }
    }

    public boolean initChart(boolean categoryIsNumbers) {
        try {
            String chartName = message("BubbleChart");
            UserConfig.setBoolean(chartName + "CategoryIsNumbers", categoryIsNumbers);
            chartMaker.init(ChartType.Bubble, chartName)
                    .setDefaultChartTitle(chartTitle())
                    .setDefaultCategoryLabel(selectedCategory)
                    .setInvalidAs(invalidAs);
            chartMaker.setIsXY(!xyReverseCheck.isSelected());
            chartMaker.setDefaultValueLabel(selectedValue);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void drawChart() {
        drawXYChart();
    }

    public void drawXYChart() {
        chartData = chartMax();
        if (chartData == null || chartData.isEmpty()) {
            return;
        }
        chartController.writeXYChart(outputColumns, chartData, categoryIndex, valueIndices);
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (super.keyEventsFilter(event)) {
            return true;
        }
        return chartController.keyEventsFilter(event);
    }

    /*
        static
     */
    public static Data2DChartBubbleController open(BaseData2DLoadController tableController) {
        try {
            Data2DChartBubbleController controller = (Data2DChartBubbleController) WindowTools.operationStage(
                    tableController, Fxmls.Data2DChartBubbleFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
