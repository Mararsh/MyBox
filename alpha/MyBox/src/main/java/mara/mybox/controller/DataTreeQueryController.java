package mara.mybox.controller;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.operate.Data2DCopy;
import mara.mybox.data2d.operate.Data2DOperate;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-5-7
 * @License Apache License Version 2.0
 */
public class DataTreeQueryController extends ControlData2DRowFilter {

    protected BaseDataTreeController dataController;
    protected String dataName, chainName;
    protected DataTable treeTable;
    protected DataFileCSV results;

    public void setParameters(BaseDataTreeController parent) {
        try {
            if (parent == null) {
                close();
                return;
            }
            dataController = parent;
            parentController = parent;
            nodeTable = dataController.nodeTable;
            dataName = nodeTable.getDataName();
            baseName = baseName + "_" + dataName;

            baseTitle = nodeTable.getTreeName() + " - " + message("Query");
            setTitle(baseTitle);

            loadColumns();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadColumns() {
        if (nodeTable == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        treeTable = null;
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    treeTable = nodeTable.recordTable(conn);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
                return treeTable != null;
            }

            @Override
            protected void whenSucceeded() {
                updateData(treeTable);
            }

        };
        start(task);
    }

    @Override
    public boolean checkOptions() {
        try {
            if (treeTable == null || pickFilter(true) == null) {
                if (error != null && !error.isBlank()) {
                    alertError(error);
                } else {
                    popError(message("InvalidParameters"));
                }
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        try {
            targetFile = FileTmpTools.getTempFile(".csv");
            results = new DataFileCSV();
            results.setCharset(Charset.forName("utf-8"))
                    .setDelimiter(",")
                    .setHasHeader(true)
                    .setDataName(dataName + "_query")
                    .setFile(targetFile);
            Data2DWriter writer = results.selfWriter();
            writer.setPrintFile(targetFile)
                    .setRecordTargetFile(false)
                    .setRecordTargetData(false)
                    .setInvalidAs(InvalidAs.Skip);
            List<Integer> cols = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                cols.add(i);
            }
            Data2DOperate operate = Data2DCopy.create(treeTable)
                    .setIncludeRowNumber(false)
                    .setCols(cols)
                    .setInvalidAs(InvalidAs.Skip)
                    .addWriter(writer)
                    .setTask(currentTask).start();
            return !operate.isFailed() && writer.isCompleted();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @FXML
    @Override
    public void handleTargetFiles() {
        if (targetFile == null || !targetFile.exists()) {
            popError(message("Failed"));
            return;
        }
        browse(targetFile);
    }

    /*
        static
     */
    public static DataTreeQueryController open(BaseDataTreeController parent) {
        try {
            DataTreeQueryController controller = (DataTreeQueryController) WindowTools
                    .operationStage(parent, Fxmls.DataTreeQueryFxml);
            controller.setParameters(parent);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
