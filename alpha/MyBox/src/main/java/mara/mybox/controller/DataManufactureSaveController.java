package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
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
        try {
            String target = "csv";
            BaseData2DController c = null;
            List<Data2DColumn> cols = tableController.data2D.getColumns();
            List<List<String>> data = tableController.data2D.tableRowsWithoutNumber();
            if (csvRadio.isSelected()) {
                c = DataFileCSVController.open(cols, data);
            } else if (excelRadio.isSelected()) {
                c = DataFileExcelController.open(cols, data);
                target = "excel";
            } else if (textsRadio.isSelected()) {
                c = DataFileTextController.open(cols, data);
                target = "texts";
            } else if (matrixRadio.isSelected()) {
                c = MatricesManageController.open(cols, data);
                target = "matrix";
            } else if (tableRadio.isSelected()) {
                c = DataTablesController.open(cols, data);
                target = "table";
            } else if (myBoxClipboardRadio.isSelected()) {
                tableController.copyToMyBoxClipboard2(cols, data);
                target = "myBoxClipboard";
            }
            UserConfig.setString(baseName + "DataTarget", target);
            closeStage();
            if (c != null) {
                c.saveAction();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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
