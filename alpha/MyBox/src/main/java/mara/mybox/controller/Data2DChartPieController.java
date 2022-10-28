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

            int valueCol = data2D.colOrder(selectedValue);
            if (valueCol < 0) {
                outOptionsError(message("SelectToHandle") + ": " + message("ValueColumn"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            if (!dataColsIndices.contains(valueCol)) {
                dataColsIndices.add(valueCol);
            }

            pieMaker.init(message("PieChart"))
                    .setDefaultChartTitle(selectedCategory + " - " + selectedValue)
                    .setChartTitle(pieMaker.getDefaultChartTitle())
                    .setDefaultCategoryLabel(selectedCategory)
                    .setCategoryLabel(selectedCategory)
                    .setDefaultValueLabel(selectedValue)
                    .setValueLabel(selectedValue)
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
        try {
            if (outputData == null || outputData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            int categoryIndex = -1;
            int valueIndex = -1;
            for (int i = 0; i < outputColumns.size(); i++) {
                String name = outputColumns.get(i).getColumnName();
                if (name.equals(selectedCategory)) {
                    categoryIndex = i;
                }
                if (name.equals(selectedValue)) {
                    valueIndex = i;
                }
            }
            chartController.writeChart(outputColumns, outputData, categoryIndex, valueIndex);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
