package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import mara.mybox.data2d.Data2D_Operations.ObjectType;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.reader.DataTableGroupStatistic;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-10-31
 * @License Apache License Version 2.0
 */
public class Data2DChartGroupBoxWhiskerController extends Data2DChartBoxWhiskerController {

    protected DataTableGroupStatistic statistic;
    protected DataTable statisticData;

    @FXML
    protected ControlData2DResults statisticDataController;

    public Data2DChartGroupBoxWhiskerController() {
        baseTitle = message("GroupData") + " - " + message("BoxWhiskerChart");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            objectType = ObjectType.Columns;

            statistic = new DataTableGroupStatistic().setCountChart(false);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void drawChart() {
        drawFrame();
    }

    @Override
    protected boolean initGroups() {
        try {
            if (group == null || framesNumber <= 0) {
                return false;
            }
            statistic.setGroups(group)
                    .setCalculation(calculation)
                    .setCalNames(checkedColsNames)
                    .setTask(task);
            if (!statistic.run()) {
                return false;
            }
            statisticData = statistic.getStatisticData();
            return statisticData != null;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    protected void loadChartData() {
        statisticDataController.loadData(statisticData.cloneAll());
        super.loadChartData();
    }

    @Override
    protected boolean makeFrameData() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DerbyBase.getConnection();
            }
            outputData = statistic.groupData(conn, groupid);
            groupParameters = group.parameterValue(conn, groupid);
            return outputData != null && !outputData.isEmpty();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void drawFrame() {
        if (outputData == null) {
            return;
        }
        outputColumns = statisticData.getColumns().subList(2, statisticData.getColumns().size());
        chartMaker.setDefaultChartTitle(chartTitle());
        super.drawChartBoxWhisker();
    }

    @Override
    public Node snapNode() {
        return chartController.chartPane;
    }

    /*
        static
     */
    public static Data2DChartGroupBoxWhiskerController open(ControlData2DLoad tableController) {
        try {
            Data2DChartGroupBoxWhiskerController controller = (Data2DChartGroupBoxWhiskerController) WindowTools.branchStage(
                    tableController, Fxmls.Data2DChartGroupBoxWhiskerFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
