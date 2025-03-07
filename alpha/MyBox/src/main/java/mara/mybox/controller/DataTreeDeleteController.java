package mara.mybox.controller;

import java.sql.Connection;
import java.util.List;
import javafx.fxml.FXML;
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
public class DataTreeDeleteController extends BaseDataTreeViewController {

    protected BaseDataTreeViewController treeController;

    public void setParameters(DataTreeController parent, DataNode node) {
        try {
            if (parent == null) {
                close();
                return;
            }
            treeController = parent;
            nodeTable = treeController.nodeTable;
            dataName = nodeTable.getDataName();
            baseName = baseName + "_" + dataName;

            baseTitle = nodeTable.getTreeName() + " - " + message("DeleteNodes");
            setTitle(baseTitle);

            loadTree(node);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        List<DataNode> nodes = selectedNodes();
        if (nodes == null || nodes.isEmpty()) {
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
                try (Connection conn = DerbyBase.getConnection()) {
                    count = nodeTable.deleteData(conn, nodes);
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
                    if (WindowTools.isRunning(treeController)) {
                        treeController.loadTree();
                        treeController.reloadCurrent();
                    }
                }
            }

        };
        start(task);

    }

    /*
        static methods
     */
    public static DataTreeDeleteController open(DataTreeController parent, DataNode node) {
        DataTreeDeleteController controller
                = (DataTreeDeleteController) WindowTools.childStage(parent, Fxmls.DataTreeDeleteFxml);
        controller.setParameters(parent, node);
        controller.requestMouse();
        return controller;
    }

}
