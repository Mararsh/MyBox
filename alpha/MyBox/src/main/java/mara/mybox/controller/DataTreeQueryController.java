package mara.mybox.controller;

import java.sql.Connection;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
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
        task = new FxSingletonTask<Void>(this) {

            protected DataTable dataTable;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    dataTable = nodeTable.recordTable(conn);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
                return dataTable != null;
            }

            @Override
            protected void whenSucceeded() {
                updateData(dataTable);
            }

        };
        start(task);
    }

    /*
        static
     */
    public static DataTreeQueryController open(BaseDataTreeController parent) {
        try {
            DataTreeQueryController controller = (DataTreeQueryController) WindowTools.childStage(
                    parent, Fxmls.DataTreeQueryFxml);
            controller.setParameters(parent);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
