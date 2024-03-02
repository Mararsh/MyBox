package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import mara.mybox.data2d.reader.Data2DExport;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2022-4-5
 * @License Apache License Version 2.0
 */
public class Data2DSaveRowsController extends BaseData2DSaveAsController {

    protected List<List<String>> dataRows;
    protected List<Data2DColumn> dataColumns;

    public void setParameters(TargetType inFormat,
            List<Data2DColumn> outputColumns, List<List<String>> outputData, String inTargetName) {
        try {
            if (outputColumns == null || outputColumns.isEmpty()
                    || outputData == null || outputData.isEmpty()
                    || inFormat == null) {
                close();
                return;
            }
            format = inFormat;
            targetName = inTargetName;
            dataColumns = outputColumns;
            dataRows = outputData;
            checkTargets();
            export = new Data2DExport();
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
            export.setColumns(dataColumns);
            List<String> names = new ArrayList<>();
            for (Data2DColumn c : dataColumns) {
                names.add(c.getColumnName());
            }
            export.setNames(names);
            export.setSkip(true);
        }
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        try {
            if (format == TargetType.DatabaseTable) {
                dataTable = Data2D.createTable(currentTask,
                        dataColumns, dataRows, targetName, invalidAs);
                return dataTable != null;
            } else {
                export.openWriters();
                for (List<String> row : dataRows) {
                    if (currentTask == null || !currentTask.isWorking()) {
                        break;
                    }
                    export.writeRow(row);
                }
                export.closeWriters();
                return true;
            }
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    @Override
    public void afterTask() {
        super.afterTask();
        if (created) {
            close();
        }
    }

    /*
        static
     */
    public static Data2DSaveRowsController createData(TargetType format,
            List<Data2DColumn> outputColumns, List<List<String>> outputData, String targetName) {
        try {
            Data2DSaveRowsController controller
                    = (Data2DSaveRowsController) WindowTools.openStage(Fxmls.Data2DSaveRowsFxml);
            controller.setParameters(format, outputColumns, outputData, targetName);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
