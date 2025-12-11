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
    protected String info;
    protected DataTable treeTable;
    protected TmpTable results;

    @FXML
    protected ControlDataTreeNodeView viewController;

    @Override
    public void initValues() {
        try {
            super.initValues();

            leftPaneControl = viewController.leftPaneControl;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseController parent,
            BaseDataTreeController controller, String conditions, TmpTable data) {
        try {
            if (parent == null || controller == null || data == null) {
                close();
                return;
            }
            parentController = parent;
            dataController = controller;
            results = data;
            nodeTable = dataController.nodeTable;
            baseName = baseName + "_" + nodeTable.getDataName();
            info = conditions;

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

    @FXML
    public void dataAction(Event event) {
        if (results != null) {
            Data2DManufactureController.openDef(results);
        }
    }

    @FXML
    public void infoAction(Event event) {
        if (info != null) {
            TextPopController.loadText(info);
        }
    }

    @Override
    public boolean handleKeyEvent(KeyEvent event) {
        if (super.handleKeyEvent(event)) {
            return true;
        }
        if (viewController != null) {
            if (viewController.handleKeyEvent(event)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean needStageVisitHistory() {
        return false;
    }

    @Override
    public void cleanPane() {
        try {
            if (WindowTools.isRunning(parentController)) {
                parentController.setIconified(false);
                parentController = null;
            }
            dataController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }


    /*
        static
     */
    public static DataTreeQueryResultsController open(BaseController parent,
            BaseDataTreeController tree, String conditions, TmpTable data) {
        try {
            DataTreeQueryResultsController controller = (DataTreeQueryResultsController) WindowTools
                    .forkStage(parent, Fxmls.DataTreeQueryResultsFxml);
            controller.setParameters(parent, tree, conditions, data);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
