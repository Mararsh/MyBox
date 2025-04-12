package mara.mybox.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import mara.mybox.data2d.DataClipboard;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFileExcel;
import mara.mybox.data2d.DataFileText;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2025-4-11
 * @License Apache License Version 2.0
 */
public class Data2DCreateController extends Data2DAttributesController {

    @FXML
    protected ControlData2DNew attributesController;

    @Override
    public void initValues() {
        try {
            super.initValues();

            tableData2DDefinition = new TableData2DDefinition();
            tableData2DColumn = new TableData2DColumn();
            tableData = FXCollections.observableArrayList();

            dataNameInput = attributesController.nameInput;
            descInput = attributesController.descInput;
            scaleSelector = attributesController.scaleSelector;
            randomSelector = attributesController.randomSelector;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean isInvalid() {
        return dataController == null
                || !dataController.isShowing();
    }

    @Override
    protected void setParameters(Data2DManufactureController controller) {
        try {
            dataController = controller;
            if (isInvalid()) {
                close();
                return;
            }

            attributesController.setParameters(this);

            loadValues();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void updateData() {
        try {
            TargetType type = attributesController.format;
            switch (type) {
                case CSV:
                    data2D = new DataFileCSV();
                    break;
                case Excel:
                    data2D = new DataFileExcel();
                    break;
                case Text:
                    data2D = new DataFileText();
                    break;
                case Matrix:
                    data2D = new DataMatrix();
                    break;
                case MyBoxClipboard:
                    data2D = new DataClipboard();
                    break;
                case DatabaseTable:
                    data2D = new DataTable();
                    break;
                default:
                    data2D = new DataFileCSV();
            }
            columnsController.setParameters(this);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static
     */
    public static Data2DCreateController open(Data2DManufactureController tableController) {
        try {
            Data2DCreateController controller = (Data2DCreateController) WindowTools.childStage(
                    tableController, Fxmls.Data2DCreateFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
