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
 * @CreateDate 2022-2-19
 * @License Apache License Version 2.0
 *
 */
public class Data2DColumnCreateController extends BaseChildController {

    protected ControlData2DColumns columnsController;

    @FXML
    protected ControlData2DColumnEdit columnEditController;
    @FXML
    protected Label buttomLabel;

    public Data2DColumnCreateController() {
        baseTitle = message("NewColumn");
        TipsLabelKey = message("SqlIdentifierComments");
    }

    protected void setParameters(ControlData2DColumns columnsController) {
        try {
            this.columnsController = columnsController;
            buttomLabel.setVisible(columnsController.data2D.isTable() && columnsController.data2D.getSheet() != null);

            columnEditController.setParameters(columnsController);

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
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
            columnsController.addRow(column);
            popSuccessful();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static
     */
    public static Data2DColumnCreateController open(ControlData2DColumns columnsController) {
        try {
            Data2DColumnCreateController controller = (Data2DColumnCreateController) WindowTools.openChildStage(
                    columnsController.getMyWindow(), Fxmls.Data2DColumnCreateFxml, false);
            controller.setParameters(columnsController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
