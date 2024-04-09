package mara.mybox.controller;

import java.sql.Connection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-2-13
 * @License Apache License Version 2.0
 */
public class Data2DConvertToDataBaseController extends Data2DTableCreateController {

    @FXML
    protected CheckBox importCheck;
    @FXML
    protected VBox dataVBox, filterVBox;

    public Data2DConvertToDataBaseController() {
        baseTitle = message("ConvertToDatabaseTable");
        TipsLabelKey = message("SqlIdentifierComments");
    }

    @Override
    public void setParameters(BaseData2DLoadController controller) {
        try {
            super.setParameters(controller);

            importCheck.setSelected(UserConfig.getBoolean(baseName + "Import", true));
            importCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Import", importCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void sourceChanged() {
        super.sourceChanged();
        attributesController.setData(data2D);
        attributesController.setColumns(checkedColsIndices);
    }

    @Override
    public void columnSelected() {
        if (isSettingValues) {
            return;
        }
        checkColumns();
        attributesController.setColumns(checkedColsIndices);
    }

    @Override
    public void beforeTask() {
        try {
            super.beforeTask();

            dataVBox.setDisable(true);
            filterVBox.setDisable(true);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        try (Connection conn = DerbyBase.getConnection()) {
            attributesController.columnIndices = checkedColsIndices;
            if (!attributesController.createTable(currentTask, conn)) {
                return false;
            }
            if (importCheck.isSelected()) {
                attributesController.data2D.startTask(currentTask, filterController.filter);
                attributesController.task = currentTask;
                if (isAllPages() && data2D.isMutiplePages()) {
                    attributesController.importAllData(currentTask, conn, invalidAs);
                } else {
                    attributesController.importData(conn, sourceController.filteredRowsIndices(), invalidAs);
                }
                attributesController.data2D.stopFilter();
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
            Data2DManufactureController.openDef(attributesController.dataTable);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void afterTask() {
        try {
            super.afterTask();
            dataVBox.setDisable(false);
            filterVBox.setDisable(false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static
     */
    public static Data2DConvertToDataBaseController open(BaseData2DLoadController tableController) {
        try {
            Data2DConvertToDataBaseController controller = (Data2DConvertToDataBaseController) WindowTools.branchStage(
                    tableController, Fxmls.Data2DConvertToDatabaseFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
