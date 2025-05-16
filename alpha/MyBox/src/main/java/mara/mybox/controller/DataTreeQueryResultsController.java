package mara.mybox.controller;

import java.util.List;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.TmpTable;
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

    protected BaseDataTreeController dataController;
    protected BaseNodeTable nodeTable;
    protected String dataName;
    protected DataTable treeTable;
    protected TmpTable results;

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

    public void setParameters(BaseController parent, BaseDataTreeController controller, TmpTable data) {
        try {
            if (parent == null || controller == null || data == null) {
                close();
                return;
            }
            parentController = parent;
            dataController = controller;
            results = data;
            nodeTable = dataController.nodeTable;
            dataName = nodeTable.getDataName();
            baseName = baseName + "_" + dataName;

            baseTitle = nodeTable.getTreeName() + " - " + message("QueryResults");
            setTitle(baseTitle);

            parentController.setIconified(true);
            viewController.setParameters(this, nodeTable);

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
    public boolean keyEventsFilter(KeyEvent event) {
        if (super.keyEventsFilter(event)) {
            return true;
        }
        if (viewController != null) {
            if (viewController.keyEventsFilter(event)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void cleanPane() {
        try {
            if (WindowTools.isRunning(parentController)) {
                parentController.setIconified(false);
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }


    /*
        static
     */
    public static DataTreeQueryResultsController open(BaseController parent,
            BaseDataTreeController tree, TmpTable data) {
        try {
            DataTreeQueryResultsController controller = (DataTreeQueryResultsController) WindowTools
                    .openStage(Fxmls.DataTreeQueryResultsFxml);
            controller.setParameters(parent, tree, data);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
