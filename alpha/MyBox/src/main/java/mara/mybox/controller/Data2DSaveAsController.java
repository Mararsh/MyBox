package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import mara.mybox.data2d.operate.Data2DSaveAs;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-5
 * @License Apache License Version 2.0
 */
public class Data2DSaveAsController extends BaseTaskController {

    protected Data2DWriter writer;
    protected InvalidAs invalidAs;

    @FXML
    protected ControlData2DTarget targetController;

    public Data2DSaveAsController() {
        baseTitle = message("SaveAs");
    }

    public void setParameters(BaseData2DLoadController controller) {
        try {
            targetController.setParameters(this, controller);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseData2DLoadController controller, TargetType targetType) {
        try {
            targetController.setParameters(this, controller);
            targetController.setTarget(targetType);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        try {
            writer = targetController.pickWriter();
            if (writer == null) {
                return false;
            }
            invalidAs = targetController.invalidAs();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        try {
            targetController.data2D.startTask(currentTask, null);
            Data2DSaveAs operate = Data2DSaveAs.writeTo(targetController.data2D, writer);
            if (operate == null) {
                return false;
            }
            operate.setController(this)
                    .setTask(currentTask)
                    .start();
            return !operate.isFailed();
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    @Override
    public void afterSuccess() {
        writer.showResult();
    }

    @Override
    public void afterTask(boolean ok) {
        if (targetController.data2D != null) {
            targetController.data2D.stopTask();
        }
        if (taskSuccessed) {
            targetController.tableController.popInformation(message("Done"));
            close();
//            if (targetController.tableController instanceof Data2DManufactureController) {
//                Data2DManufactureController c = (Data2DManufactureController) targetController.tableController;
//                if (c.askedTmp) {
//                    c.close();
//                }
//            }
        } else {
            popError(message("Failed"));
        }
    }

    /*
        static
     */
    public static Data2DSaveAsController open(BaseData2DLoadController tableController) {
        try {
            Data2DSaveAsController controller
                    = (Data2DSaveAsController) WindowTools.openStage(Fxmls.Data2DSaveAsFxml);
            controller.setParameters(tableController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static Data2DSaveAsController open(BaseData2DLoadController tableController, TargetType targetType) {
        try {
            Data2DSaveAsController controller
                    = (Data2DSaveAsController) WindowTools.openStage(Fxmls.Data2DSaveAsFxml);
            controller.setParameters(tableController, targetType);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
