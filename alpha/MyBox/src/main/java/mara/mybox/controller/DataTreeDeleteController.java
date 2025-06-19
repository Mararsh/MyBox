package mara.mybox.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-14
 * @License Apache License Version 2.0
 */
public class DataTreeDeleteController extends BaseDataTreeController {

    protected BaseDataTreeController dataController;

    public void setParameters(BaseDataTreeController parent, DataNode node) {
        try {
            if (parent == null) {
                close();
                return;
            }
            dataController = parent;
            selectionType = DataNode.SelectionType.Multiple;

            initDataTree(dataController.nodeTable, node);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public String initTitle() {
        return nodeTable.getTreeName() + " - " + message("DeleteNodes");
    }

    @FXML
    @Override
    public void okAction() {
        List<Long> selectedIDs = selectedIDs();
        if (selectedIDs == null || selectedIDs.isEmpty()) {
            popError(message("SelectNodes"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private int count = 0;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection();
                        PreparedStatement delete = conn.prepareStatement(
                                "DELETE FROM " + nodeTable.getTableName() + " WHERE nodeid=?")) {
                    conn.setAutoCommit(false);
                    for (long nodeid : selectedIDs) {
                        delete.setLong(1, nodeid);
                        delete.addBatch();
                        if (count > 0 && (count % Database.BatchSize == 0)) {
                            count += nodeTable.executeBatch(conn, delete);
                        }
                    }
                    count += nodeTable.executeBatch(conn, delete);
                    return count > 0;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                if (count > 0) {
                    loadTree();
                    if (WindowTools.isRunning(dataController)) {
                        dataController.loadTree();
                    }
                }
            }

        };
        start(task);

    }

    /*
        static methods
     */
    public static DataTreeDeleteController open(BaseDataTreeController parent, DataNode node) {
        DataTreeDeleteController controller
                = (DataTreeDeleteController) WindowTools.childStage(parent, Fxmls.DataTreeDeleteFxml);
        controller.setParameters(parent, node);
        controller.requestMouse();
        return controller;
    }

}
