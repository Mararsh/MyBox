package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.TmpTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
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
    protected TmpTable results;

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
                showLogs(message("Filter") + ": " + filter.getSourceScript());
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
            treeTable.startTask(currentTask, filter);
            List<Integer> cols = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                cols.add(i);
            }
            results = new TmpTable()
                    .setSourceData(treeTable)
                    .setTargetName(dataName + "_" + message("QueryResults"))
                    .setSourcePickIndice(cols)
                    .setImportData(true);
            results.setTask(currentTask);
            if (results.createTable()) {
                showLogs("Done");
                return true;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        results = null;
        return false;
    }

    @Override
    public void afterTask(boolean ok) {
        treeTable.stopFilter();
        if (results != null) {
            if (results.getRowsNumber() > 0) {
                DataTreeQueryResultsController.open(this, results);
            } else {
                alertInformation(message("ResultIsEmpty"));
            }
        }
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
