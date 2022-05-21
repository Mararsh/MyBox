package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-4-5
 * @License Apache License Version 2.0
 */
public class DataManufactureSaveController extends BaseChildController {

    protected ControlData2DEditTable tableController;

    @FXML
    protected ToggleGroup targetGroup;
    @FXML
    protected RadioButton csvRadio, excelRadio, textsRadio, matrixRadio, tableRadio, myBoxClipboardRadio;

    public DataManufactureSaveController() {
        baseTitle = message("DataManufacture");
    }

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            this.tableController = tableController;

            String target = UserConfig.getString(baseName + "DataTarget", "csv");
            if (target == null) {
                target = "csv";
            }
            switch (target) {
                case "csv":
                    csvRadio.fire();
                    break;
                case "excel":
                    excelRadio.fire();
                    break;
                case "texts":
                    textsRadio.fire();
                    break;
                case "matrix":
                    matrixRadio.fire();
                    break;
                case "myBoxClipboard":
                    myBoxClipboardRadio.fire();
                    break;
                case "table":
                    tableRadio.fire();
                    break;
                default:
                    csvRadio.fire();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        String target = "csv";
        if (excelRadio.isSelected()) {
            target = "excel";
        } else if (textsRadio.isSelected()) {
            target = "texts";
        } else if (matrixRadio.isSelected()) {
            target = "matrix";
        } else if (tableRadio.isSelected()) {
            target = "table";
        } else if (myBoxClipboardRadio.isSelected()) {
            target = "myBoxClipboard";
        }
        UserConfig.setString(baseName + "DataTarget", target);
        if (tableController.data2D.isMutiplePages()) {
            saveFile();
        } else {
            saveRows();
        }
    }

    public void saveRows() {
        try {
            List<Data2DColumn> cols = tableController.data2D.getColumns();
            List<List<String>> data = tableController.data2D.tableRowsWithoutNumber();
            BaseData2DController controller = null;
            if (csvRadio.isSelected()) {
                controller = DataFileCSVController.open(cols, data);
            } else if (excelRadio.isSelected()) {
                controller = DataFileExcelController.open(cols, data);
            } else if (textsRadio.isSelected()) {
                controller = DataFileTextController.open(cols, data);
            } else if (matrixRadio.isSelected()) {
                controller = MatricesManageController.open(cols, data);
            } else if (tableRadio.isSelected()) {
                controller = DataTablesController.open(cols, data);
            } else if (myBoxClipboardRadio.isSelected()) {
                tableController.copyToMyBoxClipboard2(cols, data);
            }
            if (controller != null) {
                controller.saveAction();
                closeStage();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void saveFile() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            private DataFileCSV csvFile;

            @Override
            protected boolean handle() {
                try {
                    if (tableController.data2D.isTableChanged()) {
                        tableController.data2D.setTask(task);
                        csvFile = ((DataFileCSV) (tableController.data2D)).savePageAs();
                        tableController.data2D.setTask(null);
                    } else {
                        csvFile = (DataFileCSV) tableController.data2D;
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
                if (csvRadio.isSelected()) {
                    DataFileCSVController.loadData(csvFile);
                } else if (excelRadio.isSelected()) {
                    DataFileExcelController.loadData(csvFile);
                } else if (textsRadio.isSelected()) {
                    DataFileTextController.loadData(csvFile);
                } else if (matrixRadio.isSelected()) {
                    MatricesManageController.loadData(csvFile);
                } else if (tableRadio.isSelected()) {
                    DataTablesController.loadData(csvFile);
                } else if (myBoxClipboardRadio.isSelected()) {
                    DataInMyBoxClipboardController.loadData(csvFile);
                }
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
