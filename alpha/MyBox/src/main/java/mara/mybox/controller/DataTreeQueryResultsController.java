package mara.mybox.controller;

import java.util.List;
import javafx.event.Event;
import javafx.fxml.FXML;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.TmpTable;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-5-14
 * @License Apache License Version 2.0
 */
public class DataTreeQueryResultsController extends BaseData2DLoadController {

    protected DataTreeQueryController queryController;
    protected BaseNodeTable nodeTable;
    protected String dataName;
    protected DataTable treeTable;
    protected TmpTable results;
    protected DataNode viewNode;

    @FXML
    protected ControlDataTreeNodeView viewController;

    @Override
    public void initValues() {
        try {
            super.initValues();

            rightPaneControl = viewController.rightPaneControl;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(DataTreeQueryController parent, TmpTable data) {
        try {
            if (parent == null || data == null) {
                close();
                return;
            }
            results = data;
            queryController = parent;
            nodeTable = queryController.nodeTable;
            dataName = nodeTable.getDataName();
            baseName = baseName + "_" + dataName;

            baseTitle = nodeTable.getTreeName() + " - " + message("QueryResults");
            setTitle(baseTitle);

            parent.setIconified(true);

            viewController.setQuery(this);

            loadDef(results);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        tableView.getColumns().remove(1);
    }

    @Override
    public void clicked(Event event) {
        List<String> row = selectedItem();
        if (row == null) {
            return;
        }
        viewController.loadNode(Long.parseLong(row.get(2)));
    }

    @Override
    public void cleanPane() {
        try {
            if (WindowTools.isRunning(queryController)) {
                queryController.setIconified(false);
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }


    /*
        static
     */
    public static DataTreeQueryResultsController open(DataTreeQueryController parent, TmpTable data) {
        try {
            DataTreeQueryResultsController controller = (DataTreeQueryResultsController) WindowTools
                    .openStage(Fxmls.DataTreeQueryResultsFxml);
            controller.setParameters(parent, data);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
