package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-1
 * @License Apache License Version 2.0
 */
public class Data2DAddRowsController extends TableAddRowsController {

    protected BaseData2DLoadController dataController;

    @FXML
    protected ControlData2DRowEdit rowEditController;
    @FXML
    protected Label nameLabel;

    public Data2DAddRowsController() {
        baseTitle = message("AddRows");
    }

    public void setParameters(BaseData2DLoadController controller) {
        try {
            super.setParameters(controller);
            this.dataController = controller;

            rowEditController.setParameters(controller);

            nameLabel.setText(message("Data") + ": " + dataController.data2D.displayName());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public int addRow(int index, int number) {
        try {
            List<String> values = rowEditController.pickValues(false);
            if (values == null) {
                return -1;
            }
            List<List<String>> list = new ArrayList<>();
            for (int i = 0; i < number; i++) {
                List<String> row = new ArrayList<>();
                row.addAll(values);
                list.add(row);
            }
            return tableViewController.addRows(index, list);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -2;
        }
    }


    /*
        static
     */
    public static Data2DAddRowsController open(BaseData2DLoadController tableViewController) {
        try {
            Data2DAddRowsController controller = (Data2DAddRowsController) WindowTools.branchStage(
                    tableViewController, Fxmls.Data2DAddRowsFxml);
            controller.setParameters(tableViewController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
