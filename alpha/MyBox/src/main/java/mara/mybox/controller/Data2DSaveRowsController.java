package mara.mybox.controller;

import java.util.List;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import mara.mybox.data2d.operate.Data2DExport;
import mara.mybox.data2d.writer.Data2DWriter;
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

    public void setParameters(Data2DWriter writer,
            List<Data2DColumn> outputColumns, List<List<String>> outputData) {
        try {
            if (outputColumns == null || outputColumns.isEmpty()
                    || outputData == null || outputData.isEmpty()
                    || writer == null) {
                close();
                return;
            }
            format = writer.getFormat();
            targetName = writer.getDataName();
            dataColumns = outputColumns;
            dataRows = outputData;
            checkParameters();
            if (format != TargetType.DatabaseTable) {
                export = new Data2DExport();
                export.initParameters();
                export.setDataName(targetName);
                export.addWriter(writer);
                export.setColumns(dataColumns);
                export.setTargetFile(writer.getTargetFile());
            }
            startAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
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
    public static Data2DSaveRowsController createData(Data2DWriter writer,
            List<Data2DColumn> outputColumns, List<List<String>> outputData) {
        try {
            Data2DSaveRowsController controller
                    = (Data2DSaveRowsController) WindowTools.openStage(Fxmls.Data2DSaveRowsFxml);
            controller.setParameters(writer, outputColumns, outputData);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
