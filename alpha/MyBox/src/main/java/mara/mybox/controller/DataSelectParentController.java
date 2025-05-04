package mara.mybox.controller;

import java.sql.Connection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
public class DataSelectParentController extends BaseDataSelectController {

    protected DataTreeNodeEditorController editor;

    @FXML
    protected Label nodeLabel;

    public void setParameters(DataTreeNodeEditorController controller,
            DataNode node, DataNode parentNode) {
        try {
            if (controller == null) {
                close();
                return;
            }
            editor = controller;
            sourceNode = node;
            if (sourceNode != null) {
                nodeLabel.setText(message("SourceNode") + ": " + sourceNode.shortDescription());
            }

            initDataTree(editor.nodeTable, parentNode);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public String initTitle() {
        return nodeTable.getTreeName() + " - " + message("SelectParentNode");
    }

    @FXML
    @Override
    public void okAction() {
        DataNode targetNode = selectedNode();
        if (targetNode == null) {
            popError(message("SelectToHandle"));
            return;
        }
        if (sourceNode == null) {
            editor.setParentNode(targetNode);
            close();
            return;
        }

        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    if (nodeTable.equalOrDescendant(this, conn, targetNode, sourceNode)) {
                        error = message("TreeTargetComments");
                        return false;
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                editor.setParentNode(targetNode);
                close();
            }
        };
        start(task);
    }

    /*
        static methods
     */
    public static DataSelectParentController open(DataTreeNodeEditorController parent,
            DataNode sourceNode, DataNode parentNode) {
        DataSelectParentController controller = (DataSelectParentController) WindowTools
                .childStage(parent, Fxmls.DataSelectParentFxml);
        controller.setParameters(parent, sourceNode, parentNode);
        controller.requestMouse();
        return controller;
    }

}
