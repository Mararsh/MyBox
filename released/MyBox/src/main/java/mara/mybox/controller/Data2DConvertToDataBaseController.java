package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-2-13
 * @License Apache License Version 2.0
 */
public class Data2DConvertToDataBaseController extends BaseData2DHandleController {

    @FXML
    protected Tab logsTab, attributesTab;
    @FXML
    protected CheckBox importCheck;
    @FXML
    protected VBox dataVBox, filterVBox, attributesBox, optionsBox;
    @FXML
    protected ControlNewDataTable attributesController;
    @FXML
    protected Data2DConvertToDataBaseTask taskController;

    public Data2DConvertToDataBaseController() {
        baseTitle = message("ConvertToDatabaseTable");
        TipsLabelKey = message("SqlIdentifierComments");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            importCheck.setSelected(UserConfig.getBoolean(baseName + "Import", true));
            importCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Import", importCheck.isSelected());
                }
            });

            okButton = startButton;

            taskController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void sourceChanged() {
        if (tableController == null) {
            return;
        }
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
    public boolean initData() {
        return super.initData() && taskController.checkAttributes();
    }

    @FXML
    @Override
    public void startAction() {
        okAction();
    }

    @Override
    protected void startOperation() {
        taskController.startAction();
    }

    /*
        static
     */
    public static Data2DConvertToDataBaseController open(ControlData2DLoad tableController) {
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
