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
        task = new SingletonTask<Void>(this) {

            private List<Data2DColumn> cols;
            private List<List<String>> data;

            @Override
            protected boolean handle() {
                try {
                    cols = tableController.data2D.getColumns();
                    if (tableController.data2D.isMutiplePages()) {
                        tableController.data2D.setTask(task);
                        DataFileCSV targetData = ((DataFileCSV) (tableController.data2D)).savePageAs();
                        tableController.data2D.setTask(null);
                        targetData.setTask(task);
                        data = targetData.allRows(false);
                        targetData.setTask(null);
                    } else {
                        data = tableController.data2D.tableRowsWithoutNumber();
                    }
                    return data != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }

            }

            @Override
            protected void whenSucceeded() {
                closeStage();
                String target = "csv";
                BaseData2DController controller = null;
                if (csvRadio.isSelected()) {
                    controller = DataFileCSVController.open(cols, data);
                } else if (excelRadio.isSelected()) {
                    controller = DataFileExcelController.open(cols, data);
                    target = "excel";
                } else if (textsRadio.isSelected()) {
                    controller = DataFileTextController.open(cols, data);
                    target = "texts";
                } else if (matrixRadio.isSelected()) {
                    controller = MatricesManageController.open(cols, data);
                    target = "matrix";
                } else if (tableRadio.isSelected()) {
                    controller = DataTablesController.open(cols, data);
                    target = "table";
                } else if (myBoxClipboardRadio.isSelected()) {
                    tableController.copyToMyBoxClipboard2(cols, data);
                    target = "myBoxClipboard";
                }
                UserConfig.setString(baseName + "DataTarget", target);
                if (controller != null) {
                    controller.saveAction();
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
