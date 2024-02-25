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

    protected Data2DManufactureController dataController;
    protected int index;

    @FXML
    protected ControlData2DRowEdit rowEditController;
    @FXML
    protected Label nameLabel;

    public Data2DRowEditController() {
        baseTitle = message("EditSelectedRow");
    }

    public void setParameters(Data2DManufactureController controller, int index) {
        try {
            this.dataController = controller;
            this.index = index;

            rowEditController.setParameters(dataController, index);

            nameLabel.setText(message("Data") + ": " + dataController.data2D.displayName());

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

            dataController.tableData.set(index, row);
            dataController.tableView.scrollTo(index - 3);
            dataController.popSuccessful();

            close();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        rowEditController.setParameters(dataController, index);
    }

    /*
        static
     */
    public static Data2DRowEditController open(Data2DManufactureController tableViewController, int index) {
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
