package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2022-2-24
 * @License Apache License Version 2.0
 */
public class Data2DTableCreateController extends BaseTaskController {

    protected ControlData2DEditTable tableController;
    protected ChangeListener<Boolean> columnStatusListener;

    @FXML
    protected Tab attributesTab;
    @FXML
    protected VBox attributesBox;
    @FXML
    protected ControlNewDataTable attributesController;

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            this.tableController = tableController;
            attributesController.setParameters(this, tableController);

            columnStatusListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    refreshControls();
                }
            };
            tableController.columnChangedNotify.addListener(columnStatusListener);
            refreshControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public synchronized void refreshControls() {
        try {
            getMyStage().setTitle(tableController.getTitle());

            if (tableController.data2D.getColumns() == null) {
                attributesController.setColumns(null);
            } else {
                List<Integer> indices = new ArrayList<>();
                for (int i = 0; i < tableController.data2D.getColumns().size(); i++) {
                    indices.add(i);
                }
                attributesController.setColumns(indices);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        try ( Connection conn = DerbyBase.getConnection()) {
            boolean ok = attributesController.checkOptions(conn);
            if (!ok) {
                tabPane.getSelectionModel().select(attributesTab);
            }
            return ok;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public void beforeTask() {
        attributesBox.setDisable(true);
    }

    @Override
    public boolean doTask() {
        try ( Connection conn = DerbyBase.getConnection()) {
            if (!attributesController.createTable(conn)) {
                return false;
            }
            if (tableController.data2D.isMutiplePages()) {
                attributesController.importAllData(conn);
            } else {
                attributesController.importData(conn, null);
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
            tableController.dataController.setData(attributesController.dataTable);
            tableController.dataSaved();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterTask() {
        try {
            attributesBox.setDisable(successed);
            startButton.setDisable(successed);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void cleanPane() {
        try {
            tableController.columnChangedNotify.removeListener(columnStatusListener);
            columnStatusListener = null;
            tableController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static
     */
    public static Data2DTableCreateController open(ControlData2DEditTable tableController) {
        try {
            Data2DTableCreateController controller = (Data2DTableCreateController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DTableCreateFxml, true);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
