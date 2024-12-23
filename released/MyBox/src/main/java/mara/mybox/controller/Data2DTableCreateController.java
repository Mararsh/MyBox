package mara.mybox.controller;

import java.sql.Connection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-24
 * @License Apache License Version 2.0
 */
public class Data2DTableCreateController extends BaseChildController {

    protected Data2DManufactureController editor;
    protected Data2D data2D;
    protected InvalidAs invalidAs;
    protected ChangeListener<Boolean> tableLoadListener;

    @FXML
    protected Tab attributesTab;
    @FXML
    protected VBox attributesBox, optionsBox;
    @FXML
    protected ControlNewDataTable attributesController;
    @FXML
    protected RadioButton skipInvalidRadio, zeroInvalidRadio;

    public Data2DTableCreateController() {
        baseTitle = message("DataTableCreate");
    }

    public void setParameters(Data2DManufactureController controller) {
        try {
            editor = controller;

            attributesController.setParameters(this, data2D);

            tableLoadListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    sourceChanged();
                }
            };
            editor.loadedNotify.addListener(tableLoadListener);

            sourceChanged();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void sourceChanged() {
        try {
            data2D = editor.data2D.cloneAll().setController(this);

            attributesController.setData(data2D);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        if (isSettingValues) {
            return true;
        }
        if (data2D == null || !data2D.isValidDefinition()) {
            popError(message("NoData"));
            return false;
        }
        if (zeroInvalidRadio.isSelected()) {
            invalidAs = InvalidAs.Zero;
        } else {
            invalidAs = InvalidAs.Skip;
        }
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
            if (editor != null) {
                editor.popInformation(message("Saved"));
                editor.setData(attributesController.dataTable);
                editor.notifySaved();
                editor.loadPage(true);
                close();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void cleanPane() {
        try {
            if (editor != null) {
                editor.loadedNotify.removeListener(tableLoadListener);
                tableLoadListener = null;
            }
            editor = null;
            data2D = null;
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
