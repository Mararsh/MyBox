package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.reader.Data2DExport;
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
public class Data2DSaveAsController extends BaseDataConvertController {

    protected BaseData2DLoadController tableController;
    protected Data2DExport export;
    protected Data2D data2D;
    protected String format;
    protected DataFileCSV sourceData;

    @FXML
    protected ControlData2DTarget targetController;

    public Data2DSaveAsController() {
        baseTitle = message("SaveAs");
    }

    public void setParameters(BaseData2DLoadController controller) {
        try {
            if (controller == null || controller.getData2D() == null) {
                close();
                return;
            }
            tableController = controller;

            targetController.setParameters(this, tableController);

            initControls(baseName);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void saveData(DataFileCSV sourceData, String format, String targetName, File targetFile) {
        try {
            if (sourceData == null || format == null) {
                close();
                return;
            }
            data2D = sourceData;
            export = Data2DExport.create(data2D);
            export.initParameters(format);
            export.setDataName(targetName);
            export.setTargetFile(targetFile);
            startAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        if (sourceData != null) {
            return true;
        } else {
            return pickParameters();
        }
    }

    public boolean pickParameters() {
        try {
            if (tableController == null || !tableController.isShowing()) {
                close();
                return false;
            }
            data2D = tableController.data2D;
            if (data2D == null || !data2D.isValid()) {
                close();
                return false;
            }
            format = targetController.target;
            if (format == null) {
                return false;
            }
            export = Data2DExport.create(data2D);
            export.initParameters(format);
            if ("csv".equals(format) || "matrix".equals(format) || "table".equals(format)) {
                if (!pickCSV(export)) {
                    return false;
                }
            } else if ("text".equals(format)) {
                if (!pickText(export)) {
                    return false;
                }
            } else if ("excel".equals(format)) {
                if (!pickExcel(export)) {
                    return false;
                }
            } else if ("html".equals(format)) {
                if (!pickHtml(export)) {
                    return false;
                }
            } else if ("pdf".equals(format)) {
                if (!pickPDF(export)) {
                    return false;
                }
            }
            export.setDataName(targetController.name());
            export.setTargetFile(targetController.file());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        try {
            data2D.startTask(currentTask, null);
            data2D.stopTask();
            return data2D.export(export, data2D.columnIndices());
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    @Override
    public void afterSuccess() {
        if (export != null) {
            export.openResults(this);
            closeStage();
        }
    }

    @Override
    public void afterTask() {
        if (data2D != null) {
            data2D.stopTask();
        }
        export = null;
    }

    /*
        static
     */
    public static Data2DSaveAsController open(BaseData2DLoadController tableController) {
        try {
            Data2DSaveAsController controller = (Data2DSaveAsController) WindowTools.childStage(
                    tableController, Fxmls.Data2DSaveAsFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static Data2DSaveAsController createData(DataFileCSV sourceData,
            String format, String targetName, File targetFile) {
        try {
            Data2DSaveAsController controller = (Data2DSaveAsController) WindowTools.openStage(
                    Fxmls.Data2DSaveAsFxml);
            controller.saveData(sourceData, format, targetName, targetFile);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
