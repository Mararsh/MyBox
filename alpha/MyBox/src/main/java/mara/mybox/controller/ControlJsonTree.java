package mara.mybox.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.JsonTreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-4-30
 * @License Apache License Version 2.0
 */
public class ControlJsonTree extends BaseTreeTableViewController<JsonTreeNode> {

    protected JsonEditorController jsonEditor;

    @FXML
    protected TreeTableColumn<JsonTreeNode, String> typeColumn;
    @FXML
    protected ControlJsonNodeEdit nodeController;

    @Override
    public void initControls() {
        try {
            super.initControls();

            typeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("typename"));

            nodeController.setParameters(this);

            clearTree();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        tree
     */
    public void makeTree(String json) {
        try {
            if (json == null) {
                clearTree();
                return;
            }
            loadTree(JsonTreeNode.parseByJackson(json));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadTree(JsonNode node) {
        if (task != null && !task.isQuit()) {
            return;
        }
        if (node == null) {
            clearTree();
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {

            TreeItem<JsonTreeNode> root;

            @Override
            protected boolean handle() {
                root = makeTreeItem(new JsonTreeNode("JSON", node));
                return true;
            }

            @Override
            protected void whenSucceeded() {
                setRoot(root);
                if (error != null) {
                    popError(error);
                }
            }
        };
        start(task);
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
                        addTreeItem(item, -1, new JsonTreeNode(count++ + "", fields.next()));
                    }
                }
            }

            return item;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
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
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    public void updateTreeItem(TreeItem<JsonTreeNode> item) {
        updateTreeItem(item, item.getValue());
    }

    public void updateTreeItem(TreeItem<JsonTreeNode> item, JsonTreeNode jsonTreeNode) {
        try {
            if (item == null || jsonTreeNode == null) {
                return;
            }
            TreeItem<JsonTreeNode> parentItem = item.getParent();
            if (parentItem == null) {
                loadTree(jsonTreeNode.getJsonNode());
                return;
            }
            int index = parentItem.getChildren().indexOf(item);
            if (index < 0) {
                return;
            }
            TreeItem<JsonTreeNode> updated = makeTreeItem(jsonTreeNode);
            if (updated == null) {
                return;
            }
            parentItem.getChildren().set(index, updated);
            focusItem(updated);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void itemClicked(MouseEvent event, TreeItem<JsonTreeNode> item) {
        nodeController.editNode(item);
    }

    @FXML
    @Override
    public void clearTree() {
        super.clearTree();
        nodeController.clearNode();
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
    @Override
    public List<MenuItem> functionItems(TreeItem<JsonTreeNode> treeItem) {
        if (treeItem == null) {
            return null;
        }
        JsonTreeNode jsonTreeNode = treeItem.getValue();
        List<MenuItem> items = new ArrayList<>();

        Menu viewMenu = new Menu(message("View"), StyleTools.getIconImageView("iconView.png"));
        items.add(viewMenu);

        viewMenu.getItems().addAll(foldItems(treeItem));

        MenuItem menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            refreshAction();
        });
        viewMenu.getItems().add(menu);

        items.add(new SeparatorMenuItem());

        if (jsonTreeNode.isObject()) {
            menu = new MenuItem(message("AddField"), StyleTools.getIconImageView("iconAdd.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                JsonAddFieldController.open(this, treeItem);
            });
            items.add(menu);

        } else if (jsonTreeNode.isArray()) {
            menu = new MenuItem(message("AddElement"), StyleTools.getIconImageView("iconAdd.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                JsonAddElementController.open(this, treeItem);
            });
            items.add(menu);

        }

        menu = new MenuItem(message("DeleteNode"), StyleTools.getIconImageView("iconDelete.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            deleteNode(treeItem);
        });
        items.add(menu);

        menu = new MenuItem(message("DuplicateAfterNode"), StyleTools.getIconImageView("iconCopy.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            duplicate(treeItem, true);
        });
        menu.setDisable(treeItem.getParent() == null);
        items.add(menu);

        menu = new MenuItem(message("DuplicateToParentEnd"), StyleTools.getIconImageView("iconCopy.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            duplicate(treeItem, false);
        });
        menu.setDisable(treeItem.getParent() == null);
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(copyValueMessage(), StyleTools.getIconImageView("iconCopySystem.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            TextClipboardTools.copyToSystemClipboard(this, value(treeItem.getValue()));
        });
        items.add(menu);

        menu = new MenuItem(copyTitleMessage(), StyleTools.getIconImageView("iconCopySystem.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            TextClipboardTools.copyToSystemClipboard(this, title(treeItem.getValue()));
        });
        items.add(menu);

        if (jsonEditor != null && jsonEditor.sourceFile != null && jsonEditor.sourceFile.exists()) {
            items.add(new SeparatorMenuItem());
            menu = new MenuItem(message("Recover"), StyleTools.getIconImageView("iconRecover.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                recoverAction();
            });
            items.add(menu);
        }

        return items;
    }

    public void deleteNode(TreeItem<JsonTreeNode> treeItem) {
        try {
            if (treeItem == null) {
                return;
            }
            TreeItem<JsonTreeNode> parentItem = treeItem.getParent();
            if (parentItem == null) {
                if (PopTools.askSure(getTitle(), message("SureClear"))) {
                    clearTree();
                }
                return;
            }

            String itemName = treeItem.getValue().getTitle();
            JsonNode parentNode = parentItem.getValue().getJsonNode();

            if (parentNode.isArray()) {
                int index = Integer.parseInt(itemName) - 1;
                ArrayNode arrayNode = (ArrayNode) parentNode;
                arrayNode.remove(index);
                parentItem.getValue().setJsonNode(arrayNode);

            } else if (parentNode.isObject()) {
                ObjectNode objectNode = (ObjectNode) parentNode;
                objectNode.remove(itemName);
                parentItem.getValue().setJsonNode(objectNode);
            }

            updateTreeItem(parentItem);

            jsonEditor.domChanged(true);
            jsonEditor.popInformation(message("DeletedSuccessfully"));

            nodeController.clearNode();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void duplicate(TreeItem<JsonTreeNode> treeItem, boolean afterNode) {
        try {
            if (treeItem == null) {
                return;
            }
            TreeItem<JsonTreeNode> parentItem = treeItem.getParent();
            if (parentItem == null) {
                return;
            }
            String itemName = treeItem.getValue().getTitle();
            JsonNode parentNode = parentItem.getValue().getJsonNode();
            JsonNode newNode = treeItem.getValue().getJsonNode().deepCopy();

            if (parentNode.isArray()) {
                ArrayNode arrayNode = (ArrayNode) parentNode;
                if (afterNode) {
                    arrayNode.insert(Integer.parseInt(itemName), newNode);
                } else {
                    arrayNode.add(newNode);
                }
                parentItem.getValue().setJsonNode(arrayNode);

            } else if (parentNode.isObject()) {
                ObjectNode objectNode = (ObjectNode) parentNode;
                Iterator<Map.Entry<String, JsonNode>> fields = parentNode.fields();
                List<String> names = new ArrayList<>();
                while (fields.hasNext()) {
                    names.add(fields.next().getKey());
                }
                String newName = itemName + "_Copy";
                while (names.contains(newName)) {
                    newName = itemName + "_Copy" + new Date().getTime();
                }
                if (afterNode) {
                    fields = parentNode.fields();
                    Map<String, JsonNode> newFields = new LinkedHashMap<>();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        String fieldName = field.getKey();
                        JsonNode fieldValue = field.getValue();
                        newFields.put(fieldName, fieldValue);
                        if (itemName.equals(fieldName)) {
                            newFields.put(newName, newNode);
                        }
                    }
                    newFields.put(newName, newNode);
                    objectNode.removeAll();
                    objectNode.setAll(newFields);
                } else {
                    objectNode.set(newName, newNode);
                }
                parentItem.getValue().setJsonNode(objectNode);
            }

            updateTreeItem(parentItem);

            jsonEditor.domChanged(true);
            jsonEditor.popInformation(message("CopySuccessfully"));

            nodeController.clearNode();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

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

    @FXML
    @Override
    public void recoverAction() {
        if (jsonEditor != null && jsonEditor.sourceFile != null && jsonEditor.sourceFile.exists()) {
            jsonEditor.fileChanged = false;
            jsonEditor.sourceFileChanged(jsonEditor.sourceFile);
        }
    }

    @FXML
    protected void popHelps(Event event) {
        if (UserConfig.getBoolean("JsonHelpsPopWhenMouseHovering", false)) {
            showHelps(event);
        }
    }

    @FXML
    protected void showHelps(Event event) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menuItem = new MenuItem(message("JsonTutorial") + " - " + message("English"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.jsonEnLink(), true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("JsonTutorial") + " - " + message("Chinese"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.jsonZhLink(), true);
                }
            });
            items.add(menuItem);

            items.add(new SeparatorMenuItem());

            menuItem = new MenuItem(message("JsonSpecification"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.jsonSpecification(), true);
                }
            });
            items.add(menuItem);

            items.add(new SeparatorMenuItem());

            CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            hoverMenu.setSelected(UserConfig.getBoolean("JsonHelpsPopWhenMouseHovering", false));
            hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("JsonHelpsPopWhenMouseHovering", hoverMenu.isSelected());
                }
            });
            items.add(hoverMenu);

            popEventMenu(event, items);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
