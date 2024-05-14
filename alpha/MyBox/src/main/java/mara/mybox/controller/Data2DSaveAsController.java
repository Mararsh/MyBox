package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import mara.mybox.data2d.operate.Data2DSaveAs;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import static mara.mybox.db.data.Data2DDefinition.DataType.CSV;
import static mara.mybox.db.data.Data2DDefinition.DataType.DatabaseTable;
import static mara.mybox.db.data.Data2DDefinition.DataType.Excel;
import static mara.mybox.db.data.Data2DDefinition.DataType.Matrix;
import static mara.mybox.db.data.Data2DDefinition.DataType.MyBoxClipboard;
import static mara.mybox.db.data.Data2DDefinition.DataType.Texts;
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
    protected boolean saveTmp;

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

    public void saveTmp(BaseData2DLoadController controller) {
        try {
            targetController.setParameters(this, controller);
            TargetType ttype;
            switch (targetController.data2D.getType()) {
                case CSV:
                    ttype = TargetType.CSV;
                    break;
                case Excel:
                    ttype = TargetType.Excel;
                    break;
                case Texts:
                    ttype = TargetType.Text;
                    break;
                case MyBoxClipboard:
                    ttype = TargetType.MyBoxClipboard;
                    break;
                case Matrix:
                    ttype = TargetType.Matrix;
                    break;
                case DatabaseTable:
                    ttype = TargetType.DatabaseTable;
                    break;
                default:
                    return;
            }
            targetController.setTarget(ttype);
            saveTmp = true;
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
            showLogs(writer.getClass().getName());
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
        if (saveTmp) {
            targetController.tableController.loadDef(writer.getTargetData(), false);
        } else {
            writer.showResult();
        }
    }

    @Override
    public void afterTask(boolean ok) {
        if (targetController.data2D != null) {
            targetController.data2D.stopTask();
        }
        if (taskSuccessed) {
            targetController.tableController.popInformation(message("Done"));
            close();

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

    public static Data2DSaveAsController save(BaseData2DLoadController tableController) {
        try {
            Data2DSaveAsController controller
                    = (Data2DSaveAsController) WindowTools.openStage(Fxmls.Data2DSaveAsFxml);
            controller.saveTmp(tableController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
