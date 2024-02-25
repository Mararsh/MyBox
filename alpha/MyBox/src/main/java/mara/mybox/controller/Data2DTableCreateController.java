package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
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
public class Data2DTableCreateController extends BaseTaskController {

    protected Data2DManufactureController editController;
    protected InvalidAs invalidAs = InvalidAs.Blank;

    @FXML
    protected Tab attributesTab;
    @FXML
    protected VBox attributesBox, optionsBox;
    @FXML
    protected ControlNewDataTable attributesController;
    @FXML
    protected ToggleGroup objectGroup;
    @FXML
    protected RadioButton zeroNonnumericRadio, blankNonnumericRadio;

    public void setParameters(Data2DManufactureController controller) {
        try {
            editController = controller;
            attributesController.setParameters(this, editController.data2D);

            refreshControls();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void refreshControls() {
        try {
            if (editController == null) {
                return;
            }
            getMyStage().setTitle(editController.getTitle());

            if (editController.data2D.getColumns() == null) {
                attributesController.setColumns(null);
            } else {
                List<Integer> indices = new ArrayList<>();
                for (int i = 0; i < editController.data2D.getColumns().size(); i++) {
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

        if (zeroNonnumericRadio != null && zeroNonnumericRadio.isSelected()) {
            invalidAs = InvalidAs.Zero;
        } else {
            invalidAs = InvalidAs.Blank;
        }
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        try (Connection conn = DerbyBase.getConnection()) {
            if (!attributesController.createTable(currentTask, conn)) {
                return false;
            }
            if (editController.data2D.isMutiplePages()) {
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
            if (editController != null) {
                editController.setData(attributesController.dataTable);
                editController.dataSaved();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void afterTask() {
        try {
            super.afterTask();
            attributesBox.setDisable(successed);
            optionsBox.setDisable(successed);
            startButton.setDisable(successed);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void cleanPane() {
        try {
            if (editController != null) {
                editController = null;
            }

        } catch (Exception e) {
        }
        super.cleanPane();
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
