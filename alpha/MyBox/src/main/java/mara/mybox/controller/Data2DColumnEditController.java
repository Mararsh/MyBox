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
    protected Label nameLabel;

    public Data2DColumnEditController() {
        baseTitle = message("Column");
    }

    public void setParameters(BaseData2DColumnsController columnsController, int index) {
        try {
            this.columnsController = columnsController;
            this.index = index;

            columnEditController.setParameters(columnsController, index);
            nameLabel.setText((columnsController.data2D == null ? ""
                    : (message("Data") + ": " + columnsController.data2D.displayName() + " "))
                    + message("Column") + ": " + (index + 1));
            selectButton.setDisable(columnEditController.isTableExistedColumn);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void load(Data2DColumn column) {
        columnEditController.loadColumn(column);
        selectButton.setDisable(columnEditController.isTableExistedColumn);
    }

    @FXML
    @Override
    public void okAction() {
        try {
            Data2DColumn column = columnEditController.pickValues(true);
            if (column == null) {
                return;
            }
            columnsController.tableData.set(index, column);
            columnsController.tableView.scrollTo(index - 3);
            columnsController.popSuccessful();
            if (closeAfterCheck.isSelected()) {
                close();
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        columnEditController.loadColumn(index);
    }

    @FXML
    @Override
    public void selectAction() {
        DataSelectDataColumnController.edit(this);
    }

    @FXML
    @Override
    public void saveAction() {
        Data2DColumn column = columnEditController.pickValues(true);
        if (column == null) {
            return;
        }
        ControlDataDataColumn.loadColumn(this, column);
    }


    /*
        static
     */
    public static Data2DColumnEditController open(BaseData2DColumnsController columnsController, int index) {
        try {
            Data2DColumnEditController controller = (Data2DColumnEditController) WindowTools.childStage(
                    columnsController, Fxmls.Data2DColumnEditFxml);
            controller.setParameters(columnsController, index);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
