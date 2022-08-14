package mara.mybox.controller;

import java.util.ArrayList;
import javafx.fxml.FXML;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
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

            chartController.dataController = this;
            pieMaker = chartController.pieMaker;

            noColumnSelection(true);

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
            outputColumns = new ArrayList<>();
            outputColumns.add(new Data2DColumn(message("RowNumber"), ColumnDefinition.ColumnType.String));
            int categoryCol = data2D.colOrder(selectedCategory);
            if (categoryCol < 0) {
                outError(message("SelectToHandle") + ": " + message("CategoryColumn"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            dataColsIndices.add(categoryCol);
            outputColumns.add(data2D.column(categoryCol));
            int valueCol = data2D.colOrder(selectedValue);
            if (valueCol < 0) {
                outError(message("SelectToHandle") + ": " + message("ValueColumn"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            dataColsIndices.add(valueCol);
            outputColumns.add(data2D.column(valueCol));

            pieMaker.init(message("PieChart"))
                    .setDefaultChartTitle(selectedCategory + " - " + selectedValue)
                    .setDefaultCategoryLabel(selectedCategory)
                    .setDefaultValueLabel(selectedValue);

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public void drawChart() {
        try {
            if (outputData == null || outputData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            chartController.writeChart(outputColumns, outputData);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static
     */
    public static Data2DChartPieController open(ControlData2DEditTable tableController) {
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
