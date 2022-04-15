package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2022-4-15
 * @License Apache License Version 2.0
 */
public class Data2DFrequencyController extends Data2DHandleController {

    protected int orderCol;
    protected List<Integer> colsIndices;
    protected List<String> colsNames;
    protected String orderName;
    protected ChangeListener<Boolean> tableStatusListener;

    @FXML
    protected CheckBox caseInsensitiveCheck;
    @FXML
    protected Label memoryNoticeLabel;

    @Override
    public void setParameters(ControlData2DEditTable tableController) {
        try {
            super.setParameters(tableController);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        boolean ok = super.checkOptions();
        targetController.setNotInTable(sourceController.allPages());
        if (!data2D.isTable() && sourceController.allPages()) {
            if (!thisPane.getChildren().contains(memoryNoticeLabel)) {
                thisPane.getChildren().add(3, memoryNoticeLabel);
            }
        } else {
            if (thisPane.getChildren().contains(memoryNoticeLabel)) {
                thisPane.getChildren().remove(memoryNoticeLabel);
            }
        }
        return ok;
    }

    /*
        static
     */
    public static Data2DFrequencyController open(ControlData2DEditTable tableController) {
        try {
            Data2DFrequencyController controller = (Data2DFrequencyController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DFrequencyFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
