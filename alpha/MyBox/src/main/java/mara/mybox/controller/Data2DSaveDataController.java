package mara.mybox.controller;

import java.io.File;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.reader.Data2DExport;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2022-4-5
 * @License Apache License Version 2.0
 */
public class Data2DSaveDataController extends BaseData2DSaveAsController {

    public void setParameters(DataFileCSV csvData, TargetType inFormat,
            String inTargetName, File inTargetFile) {
        try {
            if (csvData == null || !csvData.isValid() || inTargetFile == null || inFormat == null) {
                close();
                return;
            }
            data2D = csvData;
            format = inFormat;
            targetName = inTargetName;
            targetFile = inTargetFile;
            export = Data2DExport.create(data2D);
            startAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void beforeTask() {
        super.beforeTask();
        checkTargets();
        if (format != TargetType.DatabaseTable) {
            export.initParameters(format);
            export.setDataName(targetName);
            export.setTargetFile(targetFile);
            export.setColumns(data2D.getColumns());
            export.setNames(data2D.columnNames());
            export.setSkip(true);
        }
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        try {
            if (format == TargetType.DatabaseTable) {
                dataTable = data2D.toTable(currentTask, targetName);
                return dataTable != null;
            } else {
                data2D.startTask(currentTask, null);
                data2D.export(export, data2D.columnIndices());
                data2D.stopTask();
                return !export.failed();
            }
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    /*
        static
     */
    public static Data2DSaveDataController createData(DataFileCSV sourceData,
            TargetType format, String targetName, File targetFile) {
        try {
            Data2DSaveDataController controller
                    = (Data2DSaveDataController) WindowTools.openStage(Fxmls.Data2DSaveDataFxml);
            controller.setParameters(sourceData, format, targetName, targetFile);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
