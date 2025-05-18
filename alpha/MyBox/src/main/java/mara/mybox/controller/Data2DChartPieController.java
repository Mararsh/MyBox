package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.PieChartMaker;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public class Data2DChartPieController extends BaseData2DChartController {

    protected PieChartMaker pieMaker;
    protected int categoryIndex, valueIndex, percentageIndex;

    @FXML
    protected ControlData2DChartPie chartController;

    public Data2DChartPieController() {
        baseTitle = message("PieChart");
        TipsLabelKey = "DataChartPieTips";
    }

    @Override
    public void initOptions() {
        try {
            super.initOptions();

            pieMaker = chartController.pieMaker;
            chartController.redrawNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    drawChart();
                }
            });

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
                popError(message("SelectToHandle") + ": " + message("CategoryColumn"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            dataColsIndices.add(categoryCol);
            categoryIndex = showRowNumber() ? 1 : 0;

            int valueCol = data2D.colOrder(selectedValue);
            if (valueCol < 0) {
                popError(message("SelectToHandle") + ": " + message("ValueColumn"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            if (categoryCol != valueCol) {
                valueIndex = categoryIndex + 1;
                dataColsIndices.add(valueCol);
            } else {
                valueIndex = categoryIndex;
            }

            outputColumns = data2D.makeColumns(dataColsIndices, showRowNumber());

            return initChart();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public String baseChartTitle() {
        return selectedCategory + " - " + selectedValue;
    }

    public boolean initChart() {
        try {
            pieMaker.init(message("PieChart"))
                    .setDefaultChartTitle(chartTitle())
                    .setDefaultCategoryLabel(selectedCategory)
                    .setDefaultValueLabel(selectedValue)
                    .setInvalidAs(invalidAs);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void readData() {
        super.readData();
        percentageIndex = outputColumns.size();
        outputColumns.add(new Data2DColumn(message("Percentage"), ColumnDefinition.ColumnType.Double));
    }

    @Override
    public void drawChart() {
        drawPieChart();
    }

    public void drawPieChart() {
        try {
            chartData = chartMax();
            if (chartData == null || chartData.isEmpty()) {
                return;
            }
            double sum = 0, value;
            for (List<String> data : chartData) {
                try {
                    sum += Double.parseDouble(data.get(valueIndex));
                } catch (Exception e) {
                }
            }
            List<List<String>> pdata = new ArrayList<>();
            for (List<String> row : chartData) {
                try {
                    value = Double.parseDouble(row.get(valueIndex));
                    row.add(DoubleTools.percentage(value, sum, scale));
                    pdata.add(row);
                } catch (Exception e) {
                }
            }
            chartData = pdata;
            chartController.writeChart(outputColumns, chartData, categoryIndex, valueIndex, percentageIndex);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
        }
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
    public static Data2DChartPieController open(BaseData2DLoadController tableController) {
        try {
            Data2DChartPieController controller = (Data2DChartPieController) WindowTools.referredStage(
                    tableController, Fxmls.Data2DChartPieFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
