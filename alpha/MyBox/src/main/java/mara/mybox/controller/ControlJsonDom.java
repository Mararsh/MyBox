package mara.mybox.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import mara.mybox.data.JsonDomNode;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-4-30
 * @License Apache License Version 2.0
 */
public class ControlJsonDom extends BaseTreeViewController<JsonDomNode> {

    public final static ObjectMapper jsonMapper = new ObjectMapper();

    protected JsonEditorController jsonEditor;
    protected TreeItem<JsonDomNode> currentItem;

    @FXML
    protected TreeTableColumn<JsonDomNode, String> typeColumn;
    @FXML
    protected TextField nameInput;
    @FXML
    protected TextArea textArea;
    @FXML
    protected VBox editBox;

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
    @Override
    public void itemClicked(MouseEvent event, TreeItem<JsonDomNode> item) {
        editNode(item);
    }

    public TreeItem<JsonDomNode> makeTree(String json) {
        try {
            if (json == null) {
                clearTree();
                return null;
            }
            return loadTree(jsonMapper.readTree(json));
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<JsonDomNode> loadTree(JsonNode node) {
        try {
            clearTree();
            TreeItem<JsonDomNode> root = makeTreeItem("JSON", node);
            treeView.setRoot(root);
            return root;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<JsonDomNode> makeTreeItem(String key, JsonNode node) {
        try {
            if (node == null) {
                return null;
            }
            JsonDomNode jsonDomNode = new JsonDomNode(key, node);
            TreeItem<JsonDomNode> item = new TreeItem(jsonDomNode);
            item.setExpanded(true);
            if (jsonDomNode.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
                if (fields != null) {
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        addTreeItem(item, -1, field.getKey(), field.getValue());
                    }
                }

            } else if (jsonDomNode.isArray()) {
                Iterator<JsonNode> fields = node.elements();
                if (fields != null) {
                    int count = 1;
                    while (fields.hasNext()) {
                        addTreeItem(item, -1, count++ + "", fields.next());
                    }
                }
            }

            return item;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<JsonDomNode> addTreeItem(TreeItem<JsonDomNode> parent, int index, String key, JsonNode node) {
        try {
            if (parent == null || node == null) {
                return null;
            }
            TreeItem<JsonDomNode> item = makeTreeItem(key, node);
            if (item == null) {
                return null;
            }
            ObservableList<TreeItem<JsonDomNode>> parentChildren = parent.getChildren();
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

    public TreeItem<JsonDomNode> updateTreeItem(TreeItem<JsonDomNode> item) {
        try {
//            return updateTreeItem(item, item.getValue().getElement());
            return null;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<JsonDomNode> updateTreeItem(TreeItem<JsonDomNode> item, String key, JsonNode node) {
        try {
            if (item == null || node == null) {
                return null;
            }
            TreeItem<JsonDomNode> parent = item.getParent();
            if (parent == null) {
                return loadTree(node);
            }
            int index = parent.getChildren().indexOf(item);
            if (index < 0) {
                return null;
            }
            TreeItem<JsonDomNode> updated = makeTreeItem(key, node);
            if (updated == null) {
                return null;
            }
            parent.getChildren().set(index, updated);
            focusItem(updated);
            return updated;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        values
     */
    @Override
    public boolean validNode(JsonDomNode node) {
        return node != null && node.getNode() != null;
    }

    @Override
    public String title(JsonDomNode node) {
        return node == null ? null : node.getTitle();
    }

    @Override
    public String value(JsonDomNode node) {
        return node == null ? null : node.getValue();
    }

    public String jsonString() {
        try {
            return jsonMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(treeView.getRoot().getValue().getNode());
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        actions
     */
    @FXML
    @Override
    public void refreshAction() {
        updateTreeItem(treeView.getRoot());
    }

    /*
        edit
     */
    public void editNode(TreeItem<JsonDomNode> item) {
        currentItem = item;
        if (currentItem == null) {
            clearNode();
            return;
        }
        JsonDomNode node = currentItem.getValue();
        nameInput.setText(node.getTitle());
        textArea.setText(node.getValue());
        textArea.setDisable(!node.isValue());
        editBox.setDisable(currentItem.getParent() == null);
    }

    @FXML
    public void recoverNode() {
        editNode(currentItem);
    }

    @FXML
    public void okNode() {
        if (currentItem == null) {
            return;
        }
        TreeItem<JsonDomNode> parentItem = currentItem.getParent();
        JsonDomNode currentNode = currentItem.getValue();
        String name = nameInput.getText();
        if (name == null) {
            popError(message("InvalidParameter") + ": " + message("Name"));
            return;
        }
        if (parentItem == null || currentNode == null) {
            return;
        }
        ObjectNode parentNode = (ObjectNode) parentItem.getValue().getNode();
        if (currentNode.isValue()) {
            parentNode.put(name, textArea.getText());
        } else {
            parentNode.replace(name, currentNode.getNode());
        }

        updateTreeItem(currentItem, name, parentNode.findValue(name));
        editNode(currentItem);
        jsonEditor.domChanged(true);
        jsonEditor.popInformation(message("UpdateSuccessfully"));
    }

    protected void clearNode() {
        currentItem = null;
        nameInput.clear();
        textArea.clear();
        editBox.setDisable(true);
    }

}
