package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.cell.TreeTableTextTrimCell;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public abstract class BaseTreeTableViewController<NodeP> extends BaseController {

    protected final SimpleBooleanProperty loadedNotify;
    protected List<TreeItem<NodeP>> selectedItems;

    @FXML
    protected TreeTableView<NodeP> treeView;
    @FXML
    protected TreeTableColumn<NodeP, String> hierarchyColumn, titleColumn, valueColumn;
    @FXML
    protected TreeTableColumn<NodeP, Boolean> selectColumn;
    @FXML
    protected Label treeLabel;

    public BaseTreeTableViewController() {
        loadedNotify = new SimpleBooleanProperty(false);
    }

    /*
        abstract
     */
    public abstract String title(NodeP node);

    public abstract String value(NodeP node);

    /*
        init
     */
    @Override
    public void initControls() {
        try {
            super.initControls();

            initTree();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initTree() {
        try {
            hierarchyColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("title"));
            hierarchyColumn.setCellFactory(new Callback<TreeTableColumn<NodeP, String>, TreeTableCell<NodeP, String>>() {
                @Override
                public TreeTableCell<NodeP, String> call(TreeTableColumn<NodeP, String> param) {

                    TreeTableCell<NodeP, String> cell = new TreeTableCell<NodeP, String>() {
                        @Override
                        public void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            setGraphic(null);
                            if (empty || item == null) {
                                setText(null);
                                return;
                            }
                            TreeItem<NodeP> treeItem = getTreeTableView().getTreeItem(getIndex());
                            String hierarchyNumber = makeHierarchyNumber(treeItem);
                            setText(hierarchyNumber);
//                            setHierarchyNumber(treeItem.getValue(), hierarchyNumber);
                        }
                    };
                    return cell;
                }
            });

            titleColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("title"));
            titleColumn.setCellFactory(new Callback<TreeTableColumn<NodeP, String>, TreeTableCell<NodeP, String>>() {
                @Override
                public TreeTableCell<NodeP, String> call(TreeTableColumn<NodeP, String> param) {

                    TreeTableCell<NodeP, String> cell = new TreeTableCell<NodeP, String>() {
                        @Override
                        public void updateItem(String item, boolean empty) {
                            if (empty || item == null) {
                                setText(null);
                                setGraphic(null);
                                return;
                            }
                            setText(StringTools.abbreviate(item, AppVariables.titleTrimSize));
                            setGraphic(null);
                            if (isSourceNode(getTableRow().getItem())) {
                                setStyle(NodeStyleTools.darkRedTextStyle());
                            } else {
                                setStyle(null);
                            }
                        }
                    };
                    return cell;
                }
            });

            if (valueColumn != null) {
                valueColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("value"));
                valueColumn.setCellFactory(new TreeTableTextTrimCell());
            }

            if (selectColumn != null) {
                selectColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<NodeP, Boolean>, ObservableValue<Boolean>>() {
                    @Override
                    public ObservableValue<Boolean> call(TreeTableColumn.CellDataFeatures<NodeP, Boolean> param) {
                        if (param.getValue() != null) {
                            return getSelectedProperty(param.getValue().getValue());
                        }
                        return null;
                    }
                });
                selectColumn.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(selectColumn));
            }

            treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (popMenu != null && popMenu.isShowing()) {
                        popMenu.hide();
                    }
                    TreeItem<NodeP> item = selectedItem();
                    if (event.getButton() == MouseButton.SECONDARY) {
                        rightClicked(event, item);
                    } else if (event.getClickCount() > 1) {
                        doubleClicked(event, item);
                    } else {
                        itemClicked(event, item);
                    }
                }
            });

            if (treeLabel != null) {
                treeView.expandedItemCountProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue v, Number ov, Number nv) {
                        treeLabel.setText(message("ExpandedItemCount") + ": " + treeView.getExpandedItemCount());
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        tree
     */
    public TreeItem<NodeP> getRootItem() {
        return treeView.getRoot();
    }

    public void setRoot(TreeItem<NodeP> root) {
        treeView.setRoot(root);
        treeView.refresh();
        if (root != null) {
            root.setExpanded(true);
        }
        loadedNotify.set(!loadedNotify.get());
    }

    public NodeP getRootNode() {
        TreeItem<NodeP> root = getRootItem();
        return root != null ? root.getValue() : null;
    }

    public boolean isLoaded(TreeItem<NodeP> item) {
        try {
            return title(item.getChildren().get(0).getValue()) != null;
        } catch (Exception e) {
            return true;
        }
    }

    public void itemClicked(MouseEvent event, TreeItem<NodeP> item) {
    }

    public void doubleClicked(MouseEvent event, TreeItem<NodeP> item) {
    }

    public void rightClicked(MouseEvent event, TreeItem<NodeP> item) {
        showItemMenu(item);
    }

    public void focusItem(TreeItem<NodeP> nodeitem) {
        if (treeView == null || nodeitem == null) {
            return;
        }
        unfoldItemAncestors(nodeitem);
        moveToItem(nodeitem);
    }

    public void moveToItem(TreeItem<NodeP> nodeitem) {
        if (treeView == null || nodeitem == null) {
            return;
        }
        isSettingValues = true;
        treeView.getSelectionModel().select(nodeitem);
        isSettingValues = false;
        int index = treeView.getRow(nodeitem);
        treeView.scrollTo(Math.max(0, index - 5));
    }

    public boolean focusNode(NodeP node) {
        if (treeView == null || node == null) {
            return false;
        }
        boolean found = false;
        if (treeView.getRoot() != null) {
            TreeItem<NodeP> item = find(node);
            if (item != null) {
                found = true;
                focusItem(item);
            }
        }
        return found;
    }

    public void unfoldItemAncestors(TreeItem<NodeP> nodeitem) {
        if (treeView == null || nodeitem == null) {
            return;
        }
        TreeItem<NodeP> parent = nodeitem.getParent();
        if (parent == null) {
            return;
        }
        parent.setExpanded(true);
        unfoldItemAncestors(parent);
    }

    /*
        values
     */
    public BooleanProperty getSelectedProperty(NodeP node) {
        return null;
    }

    public TreeItem<NodeP> selectedItem() {
        TreeItem<NodeP> selecteItem = treeView.getSelectionModel().getSelectedItem();
        return validItem(selecteItem);
    }

    public NodeP selectedValue() {
        TreeItem<NodeP> selecteItem = selectedItem();
        return selecteItem != null ? selecteItem.getValue() : null;
    }

    public TreeItem<NodeP> validItem(TreeItem<NodeP> item) {
        TreeItem<NodeP> validItem = item;
        if (validItem == null) {
            validItem = treeView.getRoot();
        }
        if (validItem == null || !validNode(validItem.getValue())) {
            return null;
        }
        return validItem;
    }

    public boolean validNode(NodeP node) {
        return node != null;
    }

    public boolean equalNode(NodeP node1, NodeP node2) {
        if (node1 == null || node2 == null) {
            return false;
        }
        return node1.equals(node2);
    }

    public boolean isSourceNode(NodeP node) {
        return false;
    }

    public void setHierarchyNumber(NodeP node, String hierarchyNumber) {
    }

    public String makeHierarchyNumber(TreeItem<NodeP> item) {
        if (item == null) {
            return "";
        }
        String h = "";
        TreeItem<NodeP> parent = item.getParent();
        TreeItem<NodeP> child = item;
        while (parent != null) {
            int index = parent.getChildren().indexOf(child);
            if (index < 0) {
                return "";
            }
            h = "." + (index + 1) + h;
            child = parent;
            parent = parent.getParent();
        }
        if (h.startsWith(".")) {
            h = h.substring(1, h.length());
        }
        setHierarchyNumber(item.getValue(), h);
        return h;
    }

    public String label(TreeItem<NodeP> item) {
        if (item == null) {
            return "";
        }
        return makeHierarchyNumber(item) + " " + title(item.getValue());
    }

    public TreeItem<NodeP> find(NodeP node) {
        if (treeView == null || node == null) {
            return null;
        }
        return findDescendant(treeView.getRoot(), node);
    }

    public TreeItem<NodeP> findDescendant(TreeItem<NodeP> fromItem, NodeP node) {
        if (fromItem == null || node == null) {
            return null;
        }
        if (equalNode(node, fromItem.getValue())) {
            return fromItem;
        }
        List<TreeItem<NodeP>> children = fromItem.getChildren();
        if (children == null) {
            return null;
        }
        for (TreeItem<NodeP> child : children) {
            TreeItem<NodeP> find = findDescendant(child, node);
            if (find != null) {
                return find;
            }
        }
        return null;
    }

    public TreeItem<NodeP> findChild(TreeItem<NodeP> parentItem, NodeP node) {
        if (parentItem == null || node == null) {
            return null;
        }
        if (equalNode(node, parentItem.getValue())) {
            return parentItem;
        }
        List<TreeItem<NodeP>> children = parentItem.getChildren();
        if (children == null) {
            return null;
        }
        for (TreeItem<NodeP> child : children) {
            if (equalNode(node, child.getValue())) {
                return child;
            }
        }
        return null;
    }

    public TreeItem<NodeP> findSequenceNumber(String sequenceNumber) {
        return findSequenceNumber(treeView.getRoot(), sequenceNumber);
    }

    public TreeItem<NodeP> findSequenceNumber(TreeItem<NodeP> parent, String sequenceNumber) {
        try {
            if (parent == null || sequenceNumber == null || sequenceNumber.isBlank()) {
                return parent;
            }
            String[] numbers = sequenceNumber.split("\\.", -1);
            if (numbers == null || numbers.length == 0) {
                return null;
            }
            int index;
            TreeItem<NodeP> item = parent;
            for (String n : numbers) {
                index = Integer.parseInt(n);
                List<TreeItem<NodeP>> children = item.getChildren();
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

    public String copyTitleMessage() {
        return message("CopyTitle");
    }

    public String copyValueMessage() {
        return message("CopyValue");
    }

    public List<NodeP> selectedNodes() {
        List<TreeItem<NodeP>> items = selectedItems();
        if (items == null) {
            return null;
        }
        List<NodeP> selectedNodes = new ArrayList<>();
        for (TreeItem<NodeP> item : items) {
            selectedNodes.add(item.getValue());
        }
        return selectedNodes;
    }

    public List<TreeItem<NodeP>> selectedItems() {
        selectedItems = new ArrayList<>();
        if (selectColumn == null) {
            TreeItem<NodeP> item = selectedItem();
            if (item != null) {
                selectedItems.add(item);
            }
            return selectedItems;
        }
        checkSelectedItems(treeView.getRoot());
        return selectedItems;
    }

    private void checkSelectedItems(TreeItem<NodeP> item) {
        try {
            if (item == null || selectColumn == null) {
                return;
            }
            NodeP node = item.getValue();
            if (node == null) {
                return;
            }
            BooleanProperty selectedProperty = getSelectedProperty(node);
            if (selectedProperty == null) {
                return;
            }
            if (selectedProperty.get()) {
                selectedItems.add(item);
            }
            ObservableList<TreeItem<NodeP>> children = item.getChildren();
            if (children == null) {
                return;
            }
            for (TreeItem<NodeP> child : children) {
                checkSelectedItems(child);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }


    /*
        actions
     */
    @FXML
    @Override
    public void popViewMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "TreeViewPopWhenMouseHovering", true)) {
            showViewMenu(event);
        }
    }

    @FXML
    @Override
    public void showViewMenu(Event event) {
        TreeItem<NodeP> item = selectedItem();
        if (item == null) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(StringTools.menuPrefix(label(item)));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        items.addAll(viewMenuItems(item));

        items.add(new SeparatorMenuItem());

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean(baseName + "TreeViewPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean(baseName + "TreeViewPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        if (event == null) {
            popNodeMenu(treeView, items);
        } else {
            popEventMenu(event, items);
        }
    }

    public List<MenuItem> viewMenuItems(TreeItem<NodeP> treeItem) {
        if (treeItem == null) {
            return null;
        }

        List<MenuItem> items = new ArrayList<>();
        if (!treeItem.isLeaf()) {
            items.addAll(foldMenuItems(treeItem));

            items.add(new SeparatorMenuItem());
        }

        NodeP node = treeItem.getValue();
        if (node == null) {
            return items;
        }
        MenuItem menu = new MenuItem(message("ViewNode"), StyleTools.getIconImageView("iconPop.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            viewNode(treeItem);
        });
        items.add(menu);
        items.add(new SeparatorMenuItem());

        menu = new MenuItem(copyValueMessage(), StyleTools.getIconImageView("iconCopySystem.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            TextClipboardTools.copyToSystemClipboard(this, value(node));
        });
        items.add(menu);

        menu = new MenuItem(copyTitleMessage(), StyleTools.getIconImageView("iconCopySystem.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            TextClipboardTools.copyToSystemClipboard(this, title(node));
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            refreshAction();
        });
        items.add(menu);

        return items;
    }

    @FXML
    @Override
    public void popFunctionsMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "TreeFunctionsPopWhenMouseHovering", true)) {
            showFunctionsMenu(event);
        }
    }

    @FXML
    @Override
    public void showFunctionsMenu(Event event) {
        TreeItem<NodeP> treeItem = selectedItem();
        if (treeItem == null) {
            return;
        }
        List<MenuItem> items = makeFunctionsMenu(treeItem);
        if (items == null) {
            return;
        }
        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean(baseName + "TreeFunctionsPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean(baseName + "TreeFunctionsPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);
        if (event == null) {
            popNodeMenu(treeView, items);
        } else {
            popEventMenu(event, items);
        }
    }

    public List<MenuItem> makeFunctionsMenu(TreeItem<NodeP> item) {
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu = new MenuItem(StringTools.menuPrefix(label(item)));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        items.addAll(functionMenuItems(item));

        items.add(new SeparatorMenuItem());
        return items;
    }

    public List<MenuItem> functionMenuItems(TreeItem<NodeP> item) {
        return viewMenuItems(item);
    }

    public List<MenuItem> foldMenuItems(TreeItem<NodeP> item) {
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(message("UnfoldNode"), StyleTools.getIconImageView("iconPlus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            unfoldNode();
        });
        items.add(menu);

        menu = new MenuItem(message("UnfoldNodeAndDescendants"), StyleTools.getIconImageView("iconPlus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            unfoldNodeAndDecendants();
        });
        items.add(menu);

        menu = new MenuItem(message("FoldNode"), StyleTools.getIconImageView("iconMinus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            foldNode();
        });
        items.add(menu);

        menu = new MenuItem(message("FoldNodeAndDescendants"), StyleTools.getIconImageView("iconMinus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            foldNodeAndDecendants();
        });
        items.add(menu);

        return items;
    }

    public void showItemMenu(TreeItem<NodeP> item) {
        popNodeMenu(treeView, makeFunctionsMenu(item));
    }

    protected void viewNode(TreeItem<NodeP> item) {
        if (item == null) {
            return;
        }
        String s = label(item);
        NodeP node = item.getValue();
        if (node != null) {
            s += "\n" + value(node);
        }
        TextPopController.loadText(s);
    }

    @FXML
    public void foldNode() {
        fold(selectedItem(), false);
    }

    @FXML
    public void foldNodeAndDecendants() {
        fold(selectedItem(), true);
    }

    public void fold(TreeItem<NodeP> item, boolean descendants) {
        if (item == null) {
            return;
        }
        item.setExpanded(false);
        if (descendants) {
            List<TreeItem<NodeP>> children = item.getChildren();
            if (children == null || children.isEmpty()) {
                return;
            }
            for (TreeItem<NodeP> child : children) {
                fold(child, true);
            }
        }
    }

    @FXML
    public void unfoldNode() {
        unfold(selectedItem(), false);
    }

    @FXML
    public void unfoldNodeAndDecendants() {
        unfold(selectedItem(), true);
    }

    public void unfold(TreeItem<NodeP> item, boolean descendants) {
        if (item == null) {
            return;
        }
        item.setExpanded(true);
        List<TreeItem<NodeP>> children = item.getChildren();
        if (children == null || children.isEmpty()) {
            return;
        }
        for (TreeItem<NodeP> child : children) {
            if (descendants) {
                unfold(child, true);
            } else {
                child.setExpanded(false);
            }
        }
    }

    @FXML
    @Override
    public void popOperationsMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "TreeOperationsPopWhenMouseHovering", true)) {
            showOperationsMenu(event);
        }
    }

    @FXML
    @Override
    public void showOperationsMenu(Event event) {
        TreeItem<NodeP> item = selectedItem();
        if (item == null) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(StringTools.menuPrefix(label(item)));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        items.addAll(operationsMenuItems(item));

        items.add(new SeparatorMenuItem());

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean(baseName + "TreeOperationsPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean(baseName + "TreeOperationsPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        if (event == null) {
            popNodeMenu(treeView, items);
        } else {
            popEventMenu(event, items);
        }
    }

    public List<MenuItem> operationsMenuItems(TreeItem<NodeP> item) {
        return functionMenuItems(item);
    }

    @FXML
    public void clearTree() {
        setRoot(null);
    }

}
