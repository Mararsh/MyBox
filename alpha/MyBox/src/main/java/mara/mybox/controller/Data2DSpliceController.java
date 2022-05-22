package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.stage.Window;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-5-21
 * @License Apache License Version 2.0
 */
public class Data2DSpliceController extends BaseData2DController {

    @FXML
    protected ControlData2DLoad dataAController, dataBController;

    public Data2DSpliceController() {
        baseTitle = message("SpliceData");
        TipsLabelKey = "DataManageTips";
    }

    @Override
    public void setDataType(Data2D.Type type) {
        try {
            this.type = type;
            dataAController.setData(Data2D.create(type));
            dataBController.setData(Data2D.create(type));

            tableData2DDefinition = dataAController.tableData2DDefinition;
            data2D = dataAController.data2D;

            checkButtons();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        static
     */
    public static Data2DSpliceController oneOpen() {
        Data2DSpliceController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof Data2DSpliceController) {
                try {
                    controller = (Data2DSpliceController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (Data2DSpliceController) WindowTools.openStage(Fxmls.Data2DManageFxml);
        }
        controller.requestMouse();
        return controller;
    }

    public static Data2DSpliceController open(Data2DDefinition def) {
        Data2DSpliceController controller = oneOpen();
        controller.loadDef(def);
        return controller;
    }

    public static void updateList() {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof Data2DSpliceController) {
                try {
                    Data2DSpliceController controller = (Data2DSpliceController) object;
                    controller.refreshAction();
                    break;
                } catch (Exception e) {
                }
            }
        }
    }

}
