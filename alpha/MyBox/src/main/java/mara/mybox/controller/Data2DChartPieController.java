package mara.mybox.controller;

import java.util.ArrayList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.PieChartMaker;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public class Data2DChartPieController extends BaseData2DChartController {

    protected PieChartMaker pieMaker;
    protected int categoryIndex, valueIndex;

    @FXML
    protected ControlData2DChartPie chartController;

    public Data2DChartPieController() {
        baseTitle = message("PieChart");
        TipsLabelKey = "DataChartPieTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            pieMaker = chartController.pieMaker;
            chartController.redrawNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    drawChart();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean initData() {
        try {
            if (!super.initData()) {
                return false;
            }
            dataColsIndices = new ArrayList<>();
            int categoryCol = data2D.colOrder(selectedCategory);
            if (categoryCol < 0) {
                outOptionsError(message("SelectToHandle") + ": " + message("CategoryColumn"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            dataColsIndices.add(categoryCol);
            categoryIndex = showRowNumber() ? 1 : 0;

            int valueCol = data2D.colOrder(selectedValue);
            if (valueCol < 0) {
                outOptionsError(message("SelectToHandle") + ": " + message("ValueColumn"));
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
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public String chartTitle() {
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
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public void drawChart() {
        drawPieChart();
    }

    public void drawPieChart() {
        chartController.writeChart(outputColumns, outputData, categoryIndex, valueIndex);
    }

    /*
        static
     */
    public static Data2DChartPieController open(ControlData2DLoad tableController) {
        try {
            Data2DChartPieController controller = (Data2DChartPieController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DChartPieFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
