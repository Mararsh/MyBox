package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-16
 * @License Apache License Version 2.0
 */
public class Data2DColumns extends BaseChildController {

    protected ControlData2DLoad dataController;
    protected TableData2DDefinition tableData2DDefinition;

    @FXML
    protected ControlData2DColumns columnsController;
    @FXML
    protected Label nameLabel;

    public Data2DColumns() {
        baseTitle = message("Columns");
    }

    protected void setParameters(ControlData2DLoad controller) {
        try {
            dataController = controller;

            loadValues();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void loadValues() {
        try {
            if (dataController == null || !dataController.isShowing()) {
                close();
            }
            nameLabel.setText(message("Data") + ": " + dataController.data2D.displayName());

            columnsController.setParameters(dataController);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        columnsController.okAction();
        if (closeAfterCheck.isSelected()) {
            close();
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        loadValues();
    }

    /*
        static
     */
    public static Data2DColumns open(ControlData2DLoad tableController) {
        try {
            Data2DColumns controller = (Data2DColumns) WindowTools.branchStage(
                    tableController, Fxmls.Data2DColumnsFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
