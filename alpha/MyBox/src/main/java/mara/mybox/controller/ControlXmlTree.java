package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.XmlTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @Author Mara
 * @CreateDate 2023-5-27
 * @License Apache License Version 2.0
 */
public class ControlXmlTree extends BaseTreeViewController<XmlTreeNode> {

    protected XmlEditorController xmlEditor;
    protected Document doc;

    @FXML
    protected ControlXmlNodeEdit nodeController;

    @Override
    public void initControls() {
        try {
            super.initControls();

            nodeController.setParameters(this);

            clearTree();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        tree
     */
    public TreeItem<XmlTreeNode> makeTree(String xml) {
        try {
            if (xml == null) {
                clearTree();
                return null;
            }
            doc = XmlTreeNode.doc(xml);
            return loadTree(doc);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<XmlTreeNode> loadTree(Node doc) {
        try {
            clearTree();
            TreeItem<XmlTreeNode> xml = makeTreeItem(new XmlTreeNode("XML", doc));
            treeView.setRoot(xml);
            return xml;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<XmlTreeNode> makeTreeItem(XmlTreeNode xmlTreeNode) {
        try {
            if (xmlTreeNode == null) {
                return null;
            }
            TreeItem<XmlTreeNode> item = new TreeItem(xmlTreeNode);
            item.setExpanded(true);
            Node node = xmlTreeNode.getNode();
            NodeList children = node.getChildNodes();
            if (children != null) {
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        addTreeItem(item, -1, new XmlTreeNode(child.getNodeName(), child));
                    }
                }
            }
            return item;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<XmlTreeNode> addTreeItem(TreeItem<XmlTreeNode> parent, int index, XmlTreeNode xmlTreeNode) {
        try {
            if (parent == null || xmlTreeNode == null) {
                return null;
            }
            TreeItem<XmlTreeNode> item = makeTreeItem(xmlTreeNode);
            if (item == null) {
                return null;
            }
            ObservableList<TreeItem<XmlTreeNode>> parentChildren = parent.getChildren();
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

    public TreeItem<XmlTreeNode> updateTreeItem(TreeItem<XmlTreeNode> item) {
        try {
            if (item == null) {
                return null;
            }
            TreeItem<XmlTreeNode> parentItem = item.getParent();
            if (parentItem == null) {
                return loadTree(item.getValue().getNode());
            }
            int index = parentItem.getChildren().indexOf(item);
            if (index < 0) {
                return null;
            }
            parentItem.getChildren().set(index, item);
            focusItem(item);
            return item;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public void itemClicked(MouseEvent event, TreeItem<XmlTreeNode> item) {
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
    public boolean validNode(XmlTreeNode node) {
        return node != null && node.getNode() != null;
    }

    @Override
    public String title(XmlTreeNode node) {
        return node == null ? null : node.getTitle();
    }

    @Override
    public String value(XmlTreeNode node) {
        return node == null ? null : node.getValue();
    }

    @Override
    public String copyTitleMessage() {
        return message("CopyName");
    }

    public String toText() {
        return XmlTools.toText(doc, UserConfig.getBoolean("XmlTransformerIndent", false));
    }

    /*
        actions
     */
    @Override
    public List<MenuItem> functionItems(TreeItem<XmlTreeNode> treeItem) {
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

        menu = new MenuItem(message("AddNode"), StyleTools.getIconImageView("iconAdd.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
//                JsonAddField.open(this, treeItem);
        });
        items.add(menu);

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

        return items;
    }

    public void deleteNode(TreeItem<XmlTreeNode> treeItem) {
        try {
            if (treeItem == null) {
                return;
            }
            TreeItem<XmlTreeNode> parentItem = treeItem.getParent();
            if (parentItem == null) {
                if (PopTools.askSure(getTitle(), message("SureClear"))) {
                    clearTree();
                }
                return;
            }

            String itemName = treeItem.getValue().getTitle();
//            JsonNode parentNode = parentItem.getValue().getJsonNode();
//
//            if (parentNode.isArray()) {
//                int index = Integer.parseInt(itemName) - 1;
//                ArrayNode arrayNode = (ArrayNode) parentNode;
//                arrayNode.remove(index);
//                parentItem.getValue().setJsonNode(arrayNode);
//
//            } else if (parentNode.isObject()) {
//                ObjectNode objectNode = (ObjectNode) parentNode;
//                objectNode.remove(itemName);
//                parentItem.getValue().setJsonNode(objectNode);
//            }

//            updateTreeItem(parentItem);
            xmlEditor.domChanged(true);
            xmlEditor.popInformation(message("DeletedSuccessfully"));

            nodeController.clearNode();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void duplicate(TreeItem<XmlTreeNode> treeItem, boolean afterNode) {
        try {
            if (treeItem == null) {
                return;
            }
            TreeItem<XmlTreeNode> parentItem = treeItem.getParent();
            if (parentItem == null) {
                return;
            }
            String itemName = treeItem.getValue().getTitle();
//            JsonNode parentNode = parentItem.getValue().getJsonNode();
//            JsonNode newNode = treeItem.getValue().getJsonNode().deepCopy();
//
//            if (parentNode.isArray()) {
//                ArrayNode arrayNode = (ArrayNode) parentNode;
//                if (afterNode) {
//                    arrayNode.insert(Integer.parseInt(itemName), newNode);
//                } else {
//                    arrayNode.add(newNode);
//                }
//                parentItem.getValue().setJsonNode(arrayNode);
//
//            } else if (parentNode.isObject()) {
//                ObjectNode objectNode = (ObjectNode) parentNode;
//                Iterator<Map.Entry<String, JsonNode>> fields = parentNode.fields();
//                List<String> names = new ArrayList<>();
//                while (fields.hasNext()) {
//                    names.add(fields.next().getKey());
//                }
//                String newName = itemName + "_Copy";
//                while (names.contains(newName)) {
//                    newName = itemName + "_Copy" + new Date().getTime();
//                }
//                if (afterNode) {
//                    fields = parentNode.fields();
//                    Map<String, JsonNode> newFields = new LinkedHashMap<>();
//                    while (fields.hasNext()) {
//                        Map.Entry<String, JsonNode> field = fields.next();
//                        String fieldName = field.getKey();
//                        JsonNode fieldValue = field.getValue();
//                        newFields.put(fieldName, fieldValue);
//                        if (itemName.equals(fieldName)) {
//                            newFields.put(newName, newNode);
//                        }
//                    }
//                    newFields.put(newName, newNode);
//                    objectNode.removeAll();
//                    objectNode.setAll(newFields);
//                } else {
//                    objectNode.set(newName, newNode);
//                }
//                parentItem.getValue().setJsonNode(objectNode);
//            }

//            updateTreeItem(parentItem);
            xmlEditor.domChanged(true);
            xmlEditor.popInformation(message("DeletedSuccessfully"));

            nodeController.clearNode();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        try {
            TreeItem<XmlTreeNode> root = treeView.getRoot();
            if (root == null || root.isLeaf()) {
                return;
            }
//            updateTreeItem(root.getChildren().get(0));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
