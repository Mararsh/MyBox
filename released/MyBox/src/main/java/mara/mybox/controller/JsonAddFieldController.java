package mara.mybox.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
public class JsonAddFieldController extends ControlJsonNodeBase {

    public void setParameters(ControlJsonTree treeController, TreeItem<JsonTreeNode> item) {
        try {
            this.treeController = treeController;
            this.treeItem = item;

            setTitle(treeController.getMyStage().getTitle());

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
            if (treeNode == null || !treeNode.isObject()) {
                close();
                return;
            }
            String newName = nameInput.getText();
            if (newName == null || newName.isBlank()) {
                popError(message("InvalidParameter") + ": " + message("Name"));
                return;
            }
            JsonNode newNode = pickValue();
            if (newNode == null) {
                popError(message("InvalidParameter") + ": " + message("Value"));
                return;
            }
            ObjectNode objectNode = (ObjectNode) treeNode.getJsonNode();
            objectNode.set(newName, newNode);
            treeItem.getValue().setJsonNode(objectNode);

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

    /*
        static methods
     */
    public static JsonAddFieldController open(ControlJsonTree treeController, TreeItem<JsonTreeNode> item) {
        JsonAddFieldController controller = (JsonAddFieldController) WindowTools.openChildStage(
                treeController.getMyWindow(), Fxmls.JsonAddFieldFxml);
        if (controller != null) {
            controller.setParameters(treeController, item);
            controller.requestMouse();
        }
        return controller;
    }

}
