package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2022-9-1
 * @License Apache License Version 2.0
 */
public class Data2DAddRowsController extends TableAddRowsController {

    protected ControlData2DEditTable dataEditController;

    @FXML
    protected ControlData2DRowEdit rowEditController;

    public void setParameters(ControlData2DEditTable dataEditController) {
        try {
            super.setParameters(dataEditController);
            this.dataEditController = dataEditController;

            rowEditController.setParameters(dataEditController);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void addRow(int index, int number) {
        try {
            List<String> row = rowEditController.pickValues();
            if (row == null) {
                return;
            }
            List<List<String>> list = new ArrayList<>();
            for (int i = 0; i < number; i++) {
                list.add(row);
            }
            tableViewController.addRows(index, list);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }


    /*
        static
     */
    public static Data2DAddRowsController open(ControlData2DEditTable tableViewController) {
        try {
            Data2DAddRowsController controller = (Data2DAddRowsController) WindowTools.openChildStage(
                    tableViewController.getMyWindow(), Fxmls.Data2DAddRowsFxml, false);
            controller.setParameters(tableViewController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
