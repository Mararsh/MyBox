package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-5
 * @License Apache License Version 2.0
 */
public class Data2DSaveAsController extends BaseChildController {

    protected BaseData2DLoadController tableController;

    @FXML
    protected ControlData2DTarget targetController;

    public Data2DSaveAsController() {
        baseTitle = message("DataManufacture");
    }

    public void setParameters(BaseData2DLoadController tableController) {
        try {
            this.tableController = tableController;

            targetController.setParameters(this, tableController);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        Data2D data2D = tableController.data2D;
        if (data2D == null || !data2D.isValid()) {
            close();
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private DataFileCSV dataFileCSV;

            @Override
            protected boolean handle() {
                try {
                    String name = targetController.name();
                    File file = targetController.file();
                    if (data2D.isMutiplePages()) {
                        if (data2D.isTableChanged()) {
                            data2D.startTask(this, null);
                            dataFileCSV = ((DataFileCSV) (data2D)).savePageAs(name);
                            data2D.stopTask();
                        } else {
                            dataFileCSV = data2D.copy(task, file, name,
                                    data2D.columnIndices(), false, true, false);
                        }
                    } else {
                        dataFileCSV = DataFileCSV.save(task, file, name,
                                ",", data2D.getColumns(), data2D.tableRows(false));
                    }
                    dataFileCSV.setDataName(name);
                    return dataFileCSV != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }

            }

            @Override
            protected void whenSucceeded() {
                closeStage();
                DataFileCSV.createData(tableController, dataFileCSV,
                        targetController.target,
                        targetController.name(),
                        targetController.file());
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
            }
        };
        start(task);
    }

    /*
        static
     */
    public static Data2DSaveAsController open(BaseData2DLoadController tableController) {
        try {
            Data2DSaveAsController controller = (Data2DSaveAsController) WindowTools.childStage(
                    tableController, Fxmls.Data2DSaveAsFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
