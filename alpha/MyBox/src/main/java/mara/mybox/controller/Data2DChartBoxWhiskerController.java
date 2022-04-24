package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-23
 * @License Apache License Version 2.0
 */
public class Data2DChartBoxWhiskerController extends BaseData2DHandleController {

    @FXML
    protected CheckBox clockwiseCheck;

    public Data2DChartBoxWhiskerController() {
        baseTitle = message("BoxWhiskerChart");
        TipsLabelKey = "DataChartPieTips";
    }

    @Override
    public void setParameters(ControlData2DEditTable tableController) {
        try {
            super.setParameters(tableController);

            setSourceLabel(message("PieChartLabel"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean handleRows() {
        try {
            outputData = sourceController.selectedData(showRowNumber());
            if (outputData == null) {
                return false;
            }
//            JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(baseTitle, interfaceName, baseTitle, dataset, isPop);
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return false;
        }
    }


    /*
        static
     */
    public static Data2DChartBoxWhiskerController open(ControlData2DEditTable tableController) {
        try {
            Data2DChartBoxWhiskerController controller = (Data2DChartBoxWhiskerController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DChartBoxWhiskerFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
