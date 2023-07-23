package mara.mybox.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import mara.mybox.data.JsonTreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-5-20
 * @License Apache License Version 2.0
 */
public class JsonAddElementController extends ControlJsonNodeBase {

    public void setParameters(ControlJsonTree treeController, TreeItem<JsonTreeNode> item) {
        try {
            this.treeController = treeController;
            this.treeItem = item;
            checkValue();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (treeItem == null) {
                close();
                return;
            }
            JsonTreeNode treeNode = treeItem.getValue();
            if (treeNode == null || !treeNode.isArray()) {
                close();
                return;
            }
            JsonNode newNode = pickValue();
            if (newNode == null) {
                popError(message("InvalidParameter") + ": " + message("Value"));
                return;
            }
            ArrayNode arrayNode = (ArrayNode) treeNode.getJsonNode();
            arrayNode.add(newNode);
            treeItem.getValue().setJsonNode(arrayNode);

            treeController.updateTreeItem(treeItem);
            treeController.jsonEditor.domChanged(true);
            treeController.jsonEditor.popInformation(message("CreatedSuccessfully"));

            close();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    @Override
    public boolean keyESC() {
        close();
        return false;
    }

    @Override
    public boolean keyF6() {
        close();
        return false;
    }

    /*
        static methods
     */
    public static JsonAddElementController open(ControlJsonTree treeController, TreeItem<JsonTreeNode> item) {
        JsonAddElementController controller = (JsonAddElementController) WindowTools.openChildStage(
                treeController.getMyWindow(), Fxmls.JsonAddElementFxml);
        if (controller != null) {
            controller.setParameters(treeController, item);
            controller.requestMouse();
        }
        return controller;
    }

}
