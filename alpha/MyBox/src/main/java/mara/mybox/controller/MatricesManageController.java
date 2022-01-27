package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import mara.mybox.data.Data2D;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-12-15
 * @License Apache License Version 2.0
 */
public class MatricesManageController extends BaseController {

    @FXML
    protected ControlMatrixTable listController;
    @FXML
    protected ControlData2D dataController;
    @FXML
    protected Label matrixLabel;

    public MatricesManageController() {
        baseTitle = Languages.message("MatricesManage");
        TipsLabelKey = "Data2DTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            dataController.setDataType(this, Data2D.Type.Matrix);

            listController.matrixLabel = matrixLabel;
            listController.setParameters(dataController);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            dataController.savedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    listController.refreshAction();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void createAction() {
        dataController.create();
    }

    @FXML
    @Override
    public void recoverAction() {
        dataController.recoverMatrix();
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        dataController.loadContentInSystemClipboard();
    }

    @FXML
    @Override
    public void saveAction() {
        dataController.save();
    }

    public void loadDef(Data2DDefinition def) {
        dataController.loadDef(def);
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            return dataController.keyEventsFilter(event);
        }
        return true;
    }

    @Override
    public void myBoxClipBoard() {
        dataController.myBoxClipBoard();
    }

    /*
        static
     */
    public static MatricesManageController oneOpen() {
        MatricesManageController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof MatricesManageController) {
                try {
                    controller = (MatricesManageController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (MatricesManageController) WindowTools.openStage(Fxmls.MatricesManageFxml);
        }
        controller.requestMouse();
        return controller;
    }

    public static MatricesManageController open(Data2DDefinition def) {
        MatricesManageController controller = oneOpen();
        controller.loadDef(def);
        return controller;
    }

}
