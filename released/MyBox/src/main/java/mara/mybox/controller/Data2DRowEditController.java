package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-4
 * @License Apache License Version 2.0
 */
public class Data2DRowEditController extends BaseChildController {

    protected ControlData2DEditTable dataEditController;
    protected int index;

    @FXML
    protected ControlData2DRowEdit rowEditController;
    @FXML
    protected Label titleLabel;

    public void setParameters(ControlData2DEditTable dataEditController, int index) {
        try {
            this.dataEditController = dataEditController;
            this.index = index;

            rowEditController.setParameters(dataEditController, index);
            titleLabel.setText(dataEditController.data2D.displayName() + "\n"
                    + message("TableRowNumber") + " " + (index + 1));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            List<String> row = rowEditController.pickValues(false);
            if (row == null) {
                return;
            }

            dataEditController.tableData.set(index, row);
            dataEditController.tableView.scrollTo(index - 3);
            dataEditController.popSuccessful();

            close();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static
     */
    public static Data2DRowEditController open(ControlData2DEditTable tableViewController, int index) {
        try {
            Data2DRowEditController controller = (Data2DRowEditController) WindowTools.branchStage(
                    tableViewController, Fxmls.Data2DRowEditFxml);
            controller.setParameters(tableViewController, index);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
