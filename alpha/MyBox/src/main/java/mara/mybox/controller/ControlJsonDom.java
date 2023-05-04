package mara.mybox.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Iterator;
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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import mara.mybox.data.JsonDomNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TreeTableHierachyCell;
import mara.mybox.fxml.cell.TreeTableTextTrimCell;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-4-30
 * @License Apache License Version 2.0
 */
public class ControlJsonDom extends BaseController {

    public final static ObjectMapper jsonMapper = new ObjectMapper();

    protected JsonEditorController jsonEditor;
    protected TreeItem<JsonDomNode> currentItem;

    @FXML
    protected TreeTableView<JsonDomNode> domTree;
    @FXML
    protected TreeTableColumn<JsonDomNode, String> hierarchyColumn, nameColumn, typeColumn, valueColumn;
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

            hierarchyColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
            hierarchyColumn.setCellFactory(new TreeTableHierachyCell());
            nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
            nameColumn.setCellFactory(new TreeTableTextTrimCell());
            typeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("typename"));
            valueColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("text"));
            valueColumn.setCellFactory(new TreeTableTextTrimCell());

            domTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            domTree.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (popMenu != null && popMenu.isShowing()) {
                        popMenu.hide();
                    }
                    TreeItem<JsonDomNode> item = selected();
                    if (event.getButton() == MouseButton.SECONDARY) {
                        popNodeMenu(domTree, makeFunctionsMenu(item));
                    } else {
                        treeClicked(event, item);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void treeClicked(MouseEvent event, TreeItem<JsonDomNode> item) {
        editNode(item);
    }

    public TreeItem<JsonDomNode> makeTree(String json) {
        try {
            if (json == null) {
                clearDom();
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
            clearDom();
            TreeItem<JsonDomNode> root = makeTreeItem("JSON", node);
            domTree.setRoot(root);
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
            focus(updated);
            return updated;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public void focus(TreeItem<JsonDomNode> item) {
        if (item == null) {
            return;
        }
        try {
            domTree.getSelectionModel().select(item);
            int index = domTree.getRow(item);
            domTree.scrollTo(index > 3 ? index - 3 : index);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public TreeItem<JsonDomNode> selected() {
        TreeItem<JsonDomNode> item = domTree.getSelectionModel().getSelectedItem();
        return validItem(item);
    }

    public TreeItem<JsonDomNode> validItem(TreeItem<JsonDomNode> item) {
        TreeItem<JsonDomNode> validItem = item;
        if (validItem == null) {
            validItem = domTree.getRoot();
        }
        if (validItem == null) {
            return null;
        }
        JsonDomNode node = validItem.getValue();
//        if (node == null || node.getElement() == null) {
//            return null;
//        }
        return validItem;
    }

    public String tag(TreeItem<JsonDomNode> item) {
        try {
            return item.getValue().getName();
        } catch (Exception e) {
            return null;
        }
    }

    public String hierarchyNumber(TreeItem<JsonDomNode> item) {
        if (item == null) {
            return "";
        }
        TreeItem<JsonDomNode> parent = item.getParent();
        if (parent == null) {
            return "";
        }
        String p = hierarchyNumber(parent);
        return (p == null || p.isBlank() ? "" : p + ".") + (parent.getChildren().indexOf(item) + 1);
    }

    public String label(TreeItem<JsonDomNode> item) {
        if (item == null) {
            return "";
        }
        return hierarchyNumber(item) + " " + tag(item);
    }

    public boolean isSameLocation(TreeItem<JsonDomNode> item1, TreeItem<JsonDomNode> item2) {
        if (item1 == null || item2 == null) {
            return false;
        }
        String s1 = hierarchyNumber(item1);
        String s2 = hierarchyNumber(item2);
        return s1.equals(s2);
    }

    // true when item1 is a descendant of item2
    public boolean isSameOrDescendantLocation(TreeItem<JsonDomNode> item1, TreeItem<JsonDomNode> item2) {
        try {
            if (item1 == null || item2 == null) {
                return false;
            }
            String s1 = hierarchyNumber(item1);
            String s2 = hierarchyNumber(item2);
            return s1.startsWith(s2);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public TreeItem<JsonDomNode> find(String number) {
        return find(domTree.getRoot(), number);
    }

    public TreeItem<JsonDomNode> find(TreeItem<JsonDomNode> parent, String number) {
        try {
            if (parent == null || number == null || number.isBlank()) {
                return parent;
            }
            String[] numbers = number.split("\\.", -1);
            if (numbers == null || numbers.length == 0) {
                return null;
            }
            int index;
            TreeItem<JsonDomNode> item = parent;
            for (String n : numbers) {
                index = Integer.parseInt(n);
                List<TreeItem<JsonDomNode>> children = item.getChildren();
                if (index < 1 || index > children.size()) {
                    return null;
                }
                item = children.get(index - 1);
            }
            return item;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    public void foldAction() {
        setExpanded(selected(), false);
    }

    @FXML
    public void unfoldAction() {
        setExpanded(selected(), true);
    }

    @FXML
    @Override
    public void refreshAction() {
        updateTreeItem(domTree.getRoot());
    }

    public void setExpanded(TreeItem<JsonDomNode> item, boolean unfold) {
        TreeItem<JsonDomNode> validItem = validItem(item);
        if (validItem == null) {
            return;
        }
        validItem.setExpanded(unfold);
        List<TreeItem<JsonDomNode>> children = validItem.getChildren();
        if (children == null || children.isEmpty()) {
            return;
        }
        for (TreeItem child : children) {
            setExpanded(child, unfold);
        }
    }

    @FXML
    public void popFunctionsMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "DomFunctionsPopWhenMouseHovering", true)) {
            showFunctionsMenu(event);
        }
    }

    @FXML
    public void showFunctionsMenu(Event event) {
        List<MenuItem> items = makeFunctionsMenu(selected());

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean(baseName + "DomFunctionsPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                UserConfig.setBoolean(baseName + "DomFunctionsPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        if (event == null) {
            popNodeMenu(domTree, items);
        } else {
            popEventMenu(event, items);
        }
    }

    public List<MenuItem> makeFunctionsMenu(TreeItem<JsonDomNode> inItem) {
        TreeItem<JsonDomNode> item = validItem(inItem);
        if (item == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();
        MenuItem menuItem = new MenuItem(StringTools.menuPrefix(label(item)));
        menuItem.setStyle("-fx-text-fill: #2e598a;");
        items.add(menuItem);
        items.add(new SeparatorMenuItem());

        menuItem = new MenuItem(message("Unfold"), StyleTools.getIconImageView("iconPlus.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            setExpanded(item, true);
        });
        items.add(menuItem);

        menuItem = new MenuItem(message("Fold"), StyleTools.getIconImageView("iconMinus.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            setExpanded(item, false);
        });
        items.add(menuItem);

        menuItem = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            updateTreeItem(item);
        });
        items.add(menuItem);

        items.add(new SeparatorMenuItem());

        items.addAll(viewMenu(item));

        List<MenuItem> more = moreMenu(item);
        if (more != null) {
            items.addAll(more);

        }
        return items;
    }

    public List<MenuItem> viewMenu(TreeItem<JsonDomNode> item) {
        List<MenuItem> items = new ArrayList<>();
        if (item == null) {
            return items;
        }
        Menu viewMenu = new Menu(message("View"), StyleTools.getIconImageView("iconView.png"));
        items.add(viewMenu);

        MenuItem menuItem = new MenuItem(message("InnerHtml"), StyleTools.getIconImageView("iconMeta.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            popCodes(item, false);
        });
        viewMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("OuterHtml"), StyleTools.getIconImageView("iconMeta.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            popCodes(item, true);
        });
        viewMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("WholeHtml"), StyleTools.getIconImageView("iconMeta.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            popCodes(null, true);
        });
        viewMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("Value"), StyleTools.getIconImageView("iconTxt.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
//            popText(item.getValue().getValue());
        });
        viewMenu.getItems().add(menuItem);

        items.add(new SeparatorMenuItem());

        return items;
    }

    public List<MenuItem> moreMenu(TreeItem<JsonDomNode> inItem) {
        return null;
    }

    public void popCodes(TreeItem<JsonDomNode> item, boolean outer) {
        TreeItem<JsonDomNode> validItem = validItem(item);
        if (validItem == null) {
            return;
        }
        JsonDomNode node = validItem.getValue();
//        popText(outer ? node.getOuterHtml() : node.getInnerHtml());
    }

    public void popText(String text) {
        if (text == null || text.isBlank()) {
            popInformation(message("NullOrBlank"));
            return;
        }
        TextPopController.loadText(this, text);
    }

    @FXML
    protected void clearDom() {
        domTree.setRoot(null);
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
        nameInput.setText(node.getName());
        textArea.setText(node.getText());
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
