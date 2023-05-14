package mara.mybox.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import mara.mybox.data.JsonTreeNode;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-4-30
 * @License Apache License Version 2.0
 */
public class ControlJsonTree extends BaseTreeViewController<JsonTreeNode> {

    protected JsonEditorController jsonEditor;
    protected TreeItem<JsonTreeNode> currentItem;

    @FXML
    protected TreeTableColumn<JsonTreeNode, String> typeColumn;
    @FXML
    protected TextField nameInput;
    @FXML
    protected ToggleGroup valueGroup;
    @FXML
    protected RadioButton stringRadio, numberRadio, booleanRadio, nullRadio;
    @FXML
    protected TextArea textArea;
    @FXML
    protected VBox editBox, valueBox;

    @Override
    public void initControls() {
        try {
            super.initControls();

            typeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("typename"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        tree
     */
    public TreeItem<JsonTreeNode> makeTree(String json) {
        try {
            if (json == null) {
                clearTree();
                return null;
            }
            return loadTree(JsonTreeNode.parseByJackson(json));
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<JsonTreeNode> loadTree(JsonNode node) {
        try {
            clearTree();
            TreeItem<JsonTreeNode> json = makeTreeItem(new JsonTreeNode("JSON", node));
            treeView.setRoot(json);
            return json;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<JsonTreeNode> makeTreeItem(JsonTreeNode jsonTreeNode) {
        try {
            if (jsonTreeNode == null) {
                return null;
            }
            TreeItem<JsonTreeNode> item = new TreeItem(jsonTreeNode);
            item.setExpanded(true);
            if (jsonTreeNode.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> fields = jsonTreeNode.getJsonNode().fields();
                if (fields != null) {
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        addTreeItem(item, -1, new JsonTreeNode(field.getKey(), field.getValue()));
                    }
                }

            } else if (jsonTreeNode.isArray()) {
                Iterator<JsonNode> fields = jsonTreeNode.getJsonNode().elements();
                if (fields != null) {
                    int count = 1;
                    while (fields.hasNext()) {
                        addTreeItem(item, -1,
                                new JsonTreeNode(count++ + "", fields.next()).setIsArrayElement(true));
                    }
                }
            }

            return item;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<JsonTreeNode> addTreeItem(TreeItem<JsonTreeNode> parent, int index, JsonTreeNode jsonTreeNode) {
        try {
            if (parent == null || jsonTreeNode == null) {
                return null;
            }
            TreeItem<JsonTreeNode> item = makeTreeItem(jsonTreeNode);
            if (item == null) {
                return null;
            }
            ObservableList<TreeItem<JsonTreeNode>> parentChildren = parent.getChildren();
            if (index >= 0 && index < parentChildren.size() - 1) {
                parentChildren.add(index, item);
            } else {
                parentChildren.add(item);
            }
            return item;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<JsonTreeNode> updateTreeItem(TreeItem<JsonTreeNode> item) {
        try {
            return updateTreeItem(item, item.getValue());
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<JsonTreeNode> updateTreeItem(TreeItem<JsonTreeNode> item, JsonTreeNode jsonTreeNode) {
        try {
            if (item == null || jsonTreeNode == null) {
                return null;
            }
            TreeItem<JsonTreeNode> parentItem = item.getParent();
            if (parentItem == null) {
                return null;
            }
            int index = parentItem.getChildren().indexOf(item);
            if (index < 0) {
                return null;
            }
            TreeItem<JsonTreeNode> updated = makeTreeItem(jsonTreeNode);
            if (updated == null) {
                return null;
            }
            parentItem.getChildren().set(index, updated);
            focusItem(updated);
            return updated;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public void itemClicked(MouseEvent event, TreeItem<JsonTreeNode> item) {
        editNode(item);
    }

    /*
        values
     */
    @Override
    public boolean validNode(JsonTreeNode node) {
        return node != null && node.getJsonNode() != null;
    }

    @Override
    public String title(JsonTreeNode node) {
        return node == null ? null : node.getTitle();
    }

    @Override
    public String value(JsonTreeNode node) {
        return node == null ? null : node.getValue();
    }

    @Override
    public String copyTitleMessage() {
        return message("CopyName");
    }

    public String jsonFormatString() {
        try {
            return treeView.getRoot().getValue().formatByJackson();
        } catch (Exception e) {
            return null;
        }
    }


    /*
        actions
     */
    @FXML
    @Override
    public void refreshAction() {
        try {
            TreeItem<JsonTreeNode> root = treeView.getRoot();
            if (root == null || root.isLeaf()) {
                return;
            }
            updateTreeItem(root.getChildren().get(0));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        edit
     */
    public void editNode(TreeItem<JsonTreeNode> item) {
        currentItem = item;
        if (currentItem == null) {
            clearNode();
            return;
        }
        JsonTreeNode currentTreeNode = currentItem.getValue();
        if (currentTreeNode == null) {
            clearNode();
            return;
        }
        TreeItem<JsonTreeNode> parentItem = item.getParent();
        JsonTreeNode parentNode = parentItem == null ? null : parentItem.getValue();
        nameInput.setText(currentTreeNode.getTitle());
        nameInput.setDisable(parentNode == null || !parentNode.isObject());
        textArea.setText(currentTreeNode.getValue());
        valueBox.setVisible(true);
        switch (currentTreeNode.getType()) {
            case String:
                stringRadio.setSelected(true);
                break;
            case Number:
                numberRadio.setSelected(true);
                break;
            case Boolean:
                booleanRadio.setSelected(true);
                break;
            case Null:
                nullRadio.setSelected(true);
                break;
            default:
                valueBox.setVisible(false);
        }
        editBox.setDisable(nameInput.isDisable() && !valueBox.isVisible());
    }

    @FXML
    public void recoverNode() {
        editNode(currentItem);
    }

    @FXML
    public void okNode() {
        try {
            if (currentItem == null) {
                return;
            }
            String newName = nameInput.getText();
            if (newName == null) {
                popError(message("InvalidParameter") + ": " + message("Name"));
                return;
            }
            JsonTreeNode currentTreeNode = currentItem.getValue();
            if (currentTreeNode == null) {
                return;
            }
            TreeItem<JsonTreeNode> parentItem = currentItem.getParent();
            ObjectNode parentObjectNode = parentItem == null ? null
                    : (ObjectNode) parentItem.getValue().getJsonNode();
            String currentName = currentTreeNode.getTitle();

            String newValue = textArea.getText();
            if (nameInput.isEditable()) {
                if (parentObjectNode == null) {
                    return;
                }
                if (textArea.isEditable()) {
                    if (!newName.equals(currentName)) {
                        parentObjectNode.remove(currentName);
                    }
                    parentObjectNode.put(newValue, newValue);
                } else {
                    parentObjectNode.put(currentName, newValue);
                }
                updateTreeItem(parentItem);
            } else {
                if (!textArea.isEditable()) {
                    return;
                }
                updateTreeItem(parentItem);
                ObjectNode currentObjectNode = (ObjectNode) currentTreeNode.getJsonNode();
//                currentObjectNode.
//                        updateTreeItem(currentItem);
            }

            editNode(currentItem);
            jsonEditor.domChanged(true);
            jsonEditor.popInformation(message("UpdateSuccessfully"));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void clearNode() {
        currentItem = null;
        nameInput.clear();
        textArea.clear();
        editBox.setDisable(true);
    }

}
