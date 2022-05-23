package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-5
 * @License Apache License Version 2.0
 */
public class DataManufactureSaveController extends BaseChildController {

    protected ControlData2DEditTable tableController;

    @FXML
    protected ControlData2DTarget targetController;

    public DataManufactureSaveController() {
        baseTitle = message("DataManufacture");
    }

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            this.tableController = tableController;

            targetController.setParameters(this, null);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            private DataFileCSV csvFile;

            @Override
            protected boolean handle() {
                try {
                    if (tableController.data2D.isMutiplePages()) {
                        if (tableController.data2D.isTableChanged()) {
                            tableController.data2D.setTask(task);
                            csvFile = ((DataFileCSV) (tableController.data2D)).savePageAs();
                            tableController.data2D.setTask(null);
                        } else {
                            csvFile = (DataFileCSV) tableController.data2D;
                        }
                    } else {
                        csvFile = DataFileCSV.save(task, tableController.data2D.getColumns(),
                                tableController.data2D.tableRowsWithoutNumber());
                    }
                    return csvFile != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }

            }

            @Override
            protected void whenSucceeded() {
                closeStage();
                DataFileCSV.open(tableController, csvFile, targetController.target);
                if (csvFile.isTableChanged()) {
                    tableController.loadDef(csvFile);
                }
            }

            @Override
            protected void finalAction() {
                tableController.data2D.setTask(null);
                task = null;
            }
        };
        start(task);
    }

    /*
        static
     */
    public static DataManufactureSaveController open(ControlData2DEditTable tableController) {
        try {
            DataManufactureSaveController controller = (DataManufactureSaveController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.DataManufactureSaveFxml, true);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
