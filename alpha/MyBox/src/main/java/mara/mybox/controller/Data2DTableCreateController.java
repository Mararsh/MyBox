package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2022-2-24
 * @License Apache License Version 2.0
 */
public class Data2DTableCreateController extends BaseData2DTaskController {

    @FXML
    protected Tab attributesTab;
    @FXML
    protected VBox attributesBox, optionsBox;
    @FXML
    protected ControlNewDataTable attributesController;

    @Override
    public void setParameters(BaseData2DLoadController controller) {
        try {
            super.setParameters(controller);

            attributesController.setParameters(this, data2D);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void sourceChanged() {
        try {
            super.sourceChanged();

            if (data2D == null || data2D.getColumns() == null) {
                attributesController.setColumns(null);
            } else {
                List<Integer> indices = new ArrayList<>();
                for (int i = 0; i < data2D.getColumns().size(); i++) {
                    indices.add(i);
                }
                attributesController.setColumns(indices);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        try (Connection conn = DerbyBase.getConnection()) {
            boolean ok = attributesController.checkOptions(conn, false);
            if (!ok) {
                tabPane.getSelectionModel().select(attributesTab);
            }
            return ok;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void beforeTask() {
        super.beforeTask();
        attributesBox.setDisable(true);
        optionsBox.setDisable(true);

    }

    @Override
    protected void startOperation() {
        defaultStartTask();
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        try (Connection conn = DerbyBase.getConnection()) {
            if (!attributesController.createTable(currentTask, conn)) {
                return false;
            }
            if (data2D.isMutiplePages()) {
                attributesController.importAllData(currentTask, conn, invalidAs);
            } else {
                attributesController.importData(conn, null, invalidAs);
            }
            return true;
        } catch (Exception e) {
            updateLogs(e.toString());
            return false;
        }
    }

    @Override
    public void afterSuccess() {
        try {
            SoundTools.miao3();
            if (dataController != null) {
                dataController.setData(attributesController.dataTable);
                dataController.dataSaved();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void afterTask() {
        try {
            super.afterTask();
            attributesBox.setDisable(false);
            optionsBox.setDisable(false);
            startButton.setDisable(false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static
     */
    public static Data2DTableCreateController open(Data2DManufactureController tableController) {
        try {
            Data2DTableCreateController controller = (Data2DTableCreateController) WindowTools.childStage(
                    tableController, Fxmls.Data2DTableCreateFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
