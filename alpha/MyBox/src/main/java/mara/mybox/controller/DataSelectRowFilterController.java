package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.TableNodeRowFilter;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2024-12-5
 * @License Apache License Version 2.0
 */
public class DataSelectRowFilterController extends BaseDataSelectController {

    protected ControlData2DRowFilter filterController;

    public void setParameters(ControlData2DRowFilter parent) {
        try {
            if (parent == null) {
                close();
                return;
            }
            filterController = parent;
            nodeTable = new TableNodeRowFilter();
            dataName = nodeTable.getDataName();
            baseName = baseName + "_" + dataName;

            baseTitle = nodeTable.getTreeName() + " - " + message("SelectNode");
            setTitle(baseTitle);

            loadTree();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        DataNode node = selectedValue();
        if (node == null) {
            popError(message("SelectToHandle"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private DataNode savedNode;

            @Override
            protected boolean handle() {
                savedNode = nodeTable.query(node);
                return savedNode != null;
            }

            @Override
            protected void whenSucceeded() {
                filterController.load(savedNode.getStringValue("script"),
                        savedNode.getBooleanValue("match_true"),
                        savedNode.getLongValue("max_match"));
                close();
            }
        };
        start(task);
    }

    /*
        static methods
     */
    public static DataSelectRowFilterController open(ControlData2DRowFilter parent) {
        DataSelectRowFilterController controller = (DataSelectRowFilterController) WindowTools.childStage(parent, Fxmls.DataSelectRowFilterFxml);
        controller.setParameters(parent);
        controller.requestMouse();
        return controller;
    }

}
