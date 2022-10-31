package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.data2d.Data2D_Operations.ObjectType;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.DataTableGroupStatistic;
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

            statistic = new DataTableGroupStatistic().setMix(false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public String chartTitle() {
        if (group == null) {
            return null;
        }
        return group.getIdColName() + groupid + " - " + group.parameterValue(groupid - 1) + "\n"
                + super.chartTitle();
    }

    @Override
    public void drawChart() {
        drawFrame();
    }

    @Override
    protected boolean handleGroups() {
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
    protected boolean makeFrameData() {
        try {

            return outputData != null && !outputData.isEmpty();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void drawFrame() {
//        if (outputData == null) {
//            return;
//        }
//        chartMaker.setDefaultChartTitle(chartTitle());
//        super.drawChartBoxWhisker();
    }


    /*
        static
     */
    public static Data2DChartGroupBoxWhiskerController open(ControlData2DLoad tableController) {
        try {
            Data2DChartGroupBoxWhiskerController controller = (Data2DChartGroupBoxWhiskerController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DChartGroupBoxWhiskerFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
