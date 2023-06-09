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

    protected ControlData2DLoad tableController;

    @FXML
    protected ControlData2DTarget targetController;

    public DataManufactureSaveController() {
        baseTitle = message("DataManufacture");
    }

    public void setParameters(ControlData2DLoad tableController) {
        try {
            this.tableController = tableController;

            targetController.setParameters(this, tableController);

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
                    String name = targetController.name();
                    if (tableController.data2D.isMutiplePages()) {
                        if (tableController.data2D.isTableChanged()) {
                            tableController.data2D.startTask(this, null);
                            csvFile = ((DataFileCSV) (tableController.data2D)).savePageAs(name);
                            tableController.data2D.stopTask();
                        } else {
                            csvFile = tableController.data2D.copy(name,
                                    tableController.data2D.columnIndices(), false, true, false);
                        }
                    } else {
                        csvFile = DataFileCSV.save(name, task, ",", tableController.data2D.getColumns(),
                                tableController.data2D.tableRows(false));
                    }
                    csvFile.setDataName(name);
                    return csvFile != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }

            }

            @Override
            protected void whenSucceeded() {
                closeStage();
                DataFileCSV.openCSV(tableController, csvFile, targetController.target);
                if (csvFile.isTableChanged()) {
                    tableController.loadDef(csvFile);
                }
            }

            @Override
            protected void finalAction() {
                tableController.data2D.stopTask();
                task = null;
            }
        };
        start(task);
    }

    /*
        static
     */
    public static DataManufactureSaveController open(ControlData2DLoad tableController) {
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
