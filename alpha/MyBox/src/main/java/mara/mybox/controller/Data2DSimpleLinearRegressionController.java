package mara.mybox.controller;

import java.util.ArrayList;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.ChartTools;
import mara.mybox.fxml.chart.LabeledScatterChart;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-21
 * @License Apache License Version 2.0
 */
public class Data2DSimpleLinearRegressionController extends BaseData2DChartXYController {

    protected LabeledScatterChart​ scatterChart​;

    public Data2DSimpleLinearRegressionController() {
        baseTitle = message("LinearRegression");
        TipsLabelKey = "DataChartTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void checkChartType() {
        try {
            setSourceLabel(message("LinerRegressionSourceLabel"));
            checkAutoTitle();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public String title() {
        String prefix = categoryName() + " - ";
        return prefix + valuesNames();
    }

    @Override
    public String categoryName() {
        if (categoryColumnSelector != null) {
            return categoryColumnSelector.getSelectionModel().getSelectedItem();
        } else {
            return null;
        }
    }

    @Override
    public String valueName() {
        if (categoryColumnSelector != null) {
            return valueColumnSelector.getSelectionModel().getSelectedItem();
        } else {
            return null;
        }
    }

    @Override
    public boolean isCategoryNumbers() {
        return true;
    }

    @Override
    public boolean checkOptions() {
        if (isSettingValues) {
            return true;
        }
        boolean ok = super.checkOptions();
        if (categoryColumnSelector != null) {
            selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
            if (selectedCategory == null) {
                infoLabel.setText(message("SelectToHandle"));
                okButton.setDisable(true);
                return false;
            }
        }
        return ok;
    }

    @Override
    public boolean initData() {
        colsIndices = new ArrayList<>();
        int categoryCol = data2D.colOrder(selectedCategory);
        if (categoryCol < 0) {
            popError(message("SelectToHandle"));
            return false;
        }
        colsIndices.add(categoryCol);
        checkedColsIndices = sourceController.checkedColsIndices();
        if (checkedColsIndices == null || checkedColsIndices.isEmpty()) {
            popError(message("SelectToHandle"));
            return false;
        }
        colsIndices.addAll(checkedColsIndices);
        return true;
    }

    @Override
    public void clearChart() {
        super.clearChart();
        scatterChart​ = null;
    }

    @Override
    public void makeChart() {
        try {
            makeAxis();

            scatterChart = new LabeledScatterChart​(xAxis, yAxis);
            xyChart = scatterChart;
            scatterChart.setChartController(this);

            makeXYChart();
            makeFinalChart();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void writeChartData() {
        writeXYChart();
    }

    @Override
    public void setChartStyle() {
        if (scatterChart == null) {
            return;
        }
        ChartTools.setScatterChart​Colors(scatterChart, palette, legendSide != null);
        scatterChart.requestLayout();
    }

    /*
        static
     */
    public static Data2DSimpleLinearRegressionController open(ControlData2DEditTable tableController) {
        try {
            Data2DSimpleLinearRegressionController controller = (Data2DSimpleLinearRegressionController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DSimpleLinearRegressionFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
