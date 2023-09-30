package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-4
 * @License Apache License Version 2.0
 */
public class Data2DColumnEditController extends BaseChildController {

    protected BaseData2DColumnsController columnsController;
    protected int index;

    @FXML
    protected ControlData2DColumnEdit columnEditController;
    @FXML
    protected Label titleLabel;

    public void setParameters(BaseData2DColumnsController columnsController, int index) {
        try {
            this.columnsController = columnsController;
            this.index = index;

            columnEditController.setParameters(columnsController, index);
            String t = columnsController.data2D == null ? "" : (columnsController.data2D.displayName() + "\n");
            titleLabel.setText(t + message("Column") + " " + (index + 1));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            Data2DColumn column = columnEditController.pickValues();
            if (column == null) {
                return;
            }
            columnsController.tableData.set(index, column);
            columnsController.tableView.scrollTo(index - 3);
            popSuccessful();
            close();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static
     */
    public static Data2DColumnEditController open(BaseData2DColumnsController columnsController, int index) {
        try {
            Data2DColumnEditController controller = (Data2DColumnEditController) WindowTools.openChildStage(
                    columnsController.getMyWindow(), Fxmls.Data2DColumnEditFxml, true);
            controller.setParameters(columnsController, index);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
