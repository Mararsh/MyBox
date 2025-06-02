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
public class DataTreeQueryByConditionsController extends ControlData2DRowFilter {

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
            nodeTable = dataController.nodeTable;
            dataName = nodeTable.getDataName();
            baseName = baseName + "_" + dataName;

            baseTitle = nodeTable.getTreeName() + " - " + message("QueryByConditions");
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
            List<Integer> cols = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                cols.add(i);
            }
            results = new TmpTable()
                    .setSourceData(treeTable)
                    .setTargetName(dataName + "_" + message("QueryResults"))
                    .setSourcePickIndice(cols)
                    .setImportData(true);
            treeTable.startTask(currentTask, filter);
            if (results.createTable()) {
                showLogs("Done");
                return true;
            }
            showLogs(message("Failed"));
        } catch (Exception e) {
            showLogs(e.toString());
        }
        results = null;
        return false;
    }

    @Override
    public void afterTask(boolean ok) {
        treeTable.stopFilter();
        if (results != null) {
            if (results.getRowsNumber() > 0) {
                DataTreeQueryResultsController.open(this, dataController,
                        message("QueryDescendants") + "\n"
                        + message("DataTree") + ": " + nodeTable.getDataName() + "\n"
                        + message("Condition") + ": \n" + filter.toString(),
                        results);
            } else {
                alertInformation(message("ResultIsEmpty"));
            }
        }
    }

    /*
        static
     */
    public static DataTreeQueryByConditionsController open(BaseDataTreeController parent) {
        try {
            DataTreeQueryByConditionsController controller = (DataTreeQueryByConditionsController) WindowTools
                    .referredStage(parent, Fxmls.DataTreeQueryByConditionsFxml);
            controller.setParameters(parent);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
