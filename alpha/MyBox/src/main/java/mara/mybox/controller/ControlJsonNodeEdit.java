package mara.mybox.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import mara.mybox.data.JsonTreeNode;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-5-16
 * @License Apache License Version 2.0
 */
public class ControlJsonNodeEdit extends ControlJsonNodeBase {

    public void setParameters(ControlJsonTree treeController) {
        this.treeController = treeController;
    }

    @Override
    public void checkValue() {
        if (nullRadio.isSelected() || trueRadio.isSelected() || falseRadio.isSelected()) {
            textArea.clear();
            textArea.setDisable(true);

        } else {
            JsonTreeNode currentTreeNode = treeItem != null ? treeItem.getValue() : null;
            String value = currentTreeNode != null ? currentTreeNode.getValue() : null;

            if (stringRadio.isSelected() || numberRadio.isSelected() || jsonRadio.isSelected()) {
                textArea.setText(value);

            } else if (objectRadio.isSelected()) {
                if (currentTreeNode != null && currentTreeNode.isObject()) {
                    textArea.setText(value);
                } else {
                    textArea.setText("{ }");
                }

            } else if (arrayRadio.isSelected()) {
                if (currentTreeNode != null && currentTreeNode.isArray()) {
                    textArea.setText(value);
                } else {
                    textArea.setText("[ ]");
                }

            }

            textArea.setDisable(false);
        }
    }

    public void editNode(TreeItem<JsonTreeNode> item) {
        treeItem = item;
        if (treeItem == null) {
            clearNode();
            return;
        }
        JsonTreeNode currentTreeNode = treeItem.getValue();
        if (currentTreeNode == null) {
            clearNode();
            return;
        }
        thisPane.setDisable(false);
        infoLabel.setText(treeController.makeHierarchyNumber(item));
        TreeItem<JsonTreeNode> parentItem = item.getParent();
        JsonTreeNode parentNode = parentItem == null ? null : parentItem.getValue();
        nameInput.setText(currentTreeNode.getTitle());
        nameInput.setDisable(parentNode == null || !parentNode.isObject());
        nameLabel.setText(parentNode != null && parentNode.isArray() ? message("Index") : message("Name"));
        switch (currentTreeNode.getType()) {
            case String:
                stringRadio.setSelected(true);
                break;
            case Number:
                numberRadio.setSelected(true);
                break;
            case Boolean:
                if ("true".equals(currentTreeNode.getValue())) {
                    trueRadio.setSelected(true);
                } else {
                    falseRadio.setSelected(true);
                }
                break;
            case Null:
                nullRadio.setSelected(true);
                break;
            case Object:
                objectRadio.setSelected(true);
                break;
            case Array:
                arrayRadio.setSelected(true);
                break;
        }
        checkValue();
    }

    @FXML
    public void okNode() {
        try {
            if (treeItem == null) {
                return;
            }
            String newName = nameInput.getText();
            if (newName == null || newName.isBlank()) {
                popError(message("InvalidParameter") + ": " + message("Name"));
                return;
            }
            JsonTreeNode currentTreeNode = treeItem.getValue();
            if (currentTreeNode == null) {
                return;
            }
            JsonNode newNode = pickValue();
            if (newNode == null) {
                return;
            }
            TreeItem<JsonTreeNode> parentItem = treeItem.getParent();
            if (parentItem == null) {
                treeController.loadTree(newNode);
                return;
            }
            JsonNode parentNode = parentItem.getValue().getJsonNode();

            if (parentNode.isArray()) {
                int index = Integer.parseInt(newName) - 1;
                ArrayNode arrayNode = (ArrayNode) parentNode;
                arrayNode.set(index, newNode);
                parentItem.getValue().setJsonNode(arrayNode);

            } else {
                String currentName = currentTreeNode.getTitle();
                ObjectNode parentObjectNode = (ObjectNode) parentNode;
                Iterator<Map.Entry<String, JsonNode>> fields = parentObjectNode.fields();
                Map<String, JsonNode> newFields = new LinkedHashMap<>();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String fieldName = field.getKey();
                    JsonNode fieldValue = field.getValue();
                    if (currentName.equals(fieldName)) {
                        newFields.put(newName, newNode);
                    } else {
                        newFields.put(fieldName, fieldValue);
                    }
                }
                parentObjectNode.removeAll();
                parentObjectNode.setAll(newFields);
                parentItem.getValue().setJsonNode(parentObjectNode);
            }

            clearNode();
            treeController.updateTreeItem(parentItem);
            treeController.jsonEditor.domChanged(true);
            treeController.jsonEditor.popInformation(message("UpdateSuccessfully"));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void recoverNode() {
        editNode(treeItem);
    }

    @Override
    public void clearNode() {
        super.clearNode();
        thisPane.setDisable(true);
    }

}
