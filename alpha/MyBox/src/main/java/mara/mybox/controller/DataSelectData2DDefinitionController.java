package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.tools.Data2DDefinitionTools;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.TableNodeData2DDefinition;
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
public class DataSelectData2DDefinitionController extends BaseDataSelectController {

    protected BaseData2DColumnsController columnsController;

    public void setParameters(BaseData2DColumnsController controller) {
        try {
            if (controller == null) {
                close();
                return;
            }
            columnsController = controller;
            nodeTable = new TableNodeData2DDefinition();
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

            private DataFileCSV def;

            @Override
            protected boolean handle() {
                def = Data2DDefinitionTools.fromDataNode(nodeTable.query(node));
                return def != null;
            }

            @Override
            protected void whenSucceeded() {
                columnsController.addColumns(def);
                close();
            }
        };
        start(task);
    }

    /*
        static methods
     */
    public static DataSelectData2DDefinitionController open(BaseData2DColumnsController parent) {
        DataSelectData2DDefinitionController controller
                = (DataSelectData2DDefinitionController) WindowTools.childStage(parent, Fxmls.DataSelectData2DDefinitionFxml);
        controller.setParameters(parent);
        controller.requestMouse();
        return controller;
    }

}
