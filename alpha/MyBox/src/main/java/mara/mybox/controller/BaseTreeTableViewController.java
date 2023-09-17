package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public abstract class BaseTreeTableViewController<NodeP> extends BaseController {

    protected final SimpleBooleanProperty loadedNotify;
    protected NodeP focusNode;

    @FXML
    protected TreeTableView<NodeP> treeView;
    @FXML
    protected TreeTableColumn<NodeP, String> hierarchyColumn, titleColumn, valueColumn;
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
                            setText(hierarchyNumber(getTreeTableView().getTreeItem(getIndex())));
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
                            setText(StringTools.abbreviate(item, 60));
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

            valueColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("value"));
            valueColumn.setCellFactory(new TreeTableTextTrimCell());

            treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (popMenu != null && popMenu.isShowing()) {
                        popMenu.hide();
                    }
                    TreeItem<NodeP> item = selected();
                    if (event.getButton() == MouseButton.SECONDARY) {
                        showItemMenu(item);
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
    public void setRoot(TreeItem<NodeP> root) {
        treeView.setRoot(root);
        if (root != null) {
            root.setExpanded(true);
            if (focusNode != null) {
                focusNode(focusNode);
            }
        }
        focusNode = null;
        loadedNotify.set(!loadedNotify.get());
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

    public void addNewNode(TreeItem<NodeP> parent, NodeP node, boolean select) {
        if (parent == null || node == null) {
            return;
        }
        TreeItem<NodeP> child = new TreeItem(node);
        parent.getChildren().add(child);
        child.setExpanded(false);
        if (select) {
            focusItem(child);
            itemClicked(null, child);
        }
    }

    public void focusItem(TreeItem<NodeP> nodeitem) {
        if (treeView == null || nodeitem == null) {
            return;
        }
        isSettingValues = true;
        treeView.getSelectionModel().select(nodeitem);
        isSettingValues = false;
        int index = treeView.getRow(nodeitem);
        treeView.scrollTo(Math.max(0, index - 5));
    }

    public void focusNode(NodeP node) {
        if (treeView == null || node == null) {
            return;
        }
        if (treeView.getRoot() != null) {
            focusItem(find(node));
        }
        focusNode = null;
    }

    public void focusNodeAfterLoaded(NodeP node) {
        try {
            if (treeView.getRoot() != null) {
                focusNode(node);
            } else {
                focusNode = node;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void nodeAdded(NodeP parent, NodeP newNode) {
    }


    /*
        values
     */
    public TreeItem<NodeP> selected() {
        TreeItem<NodeP> selecteItem = treeView.getSelectionModel().getSelectedItem();
        return validItem(selecteItem);
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

    public boolean equalItem(TreeItem<NodeP> item1, TreeItem<NodeP> item2) {
        if (item1 == null || item2 == null) {
            return false;
        }
        String s1 = hierarchyNumber(item1);
        String s2 = hierarchyNumber(item2);
        return s1.equals(s2);
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

    public boolean equalOrDescendant(TreeItem<NodeP> item1, TreeItem<NodeP> item2) {
        if (item1 == null || item2 == null) {
            return false;
        }
        if (equalItem(item1, item2)) {
            return true;
        }
        return equalOrDescendant(item1.getParent(), item2);
    }

    public String hierarchyNumber(TreeItem<NodeP> item) {
        if (item == null) {
            return "";
        }
        String h = "";
        TreeItem<NodeP> parent = item.getParent();
        TreeItem<NodeP> citem = item;
        while (parent != null) {
            int index = parent.getChildren().indexOf(citem);
            if (index < 0) {
                return "";
            }
            h = "." + (index + 1) + h;
            citem = parent;
            parent = parent.getParent();
        }
        if (h.startsWith(".")) {
            h = h.substring(1, h.length());
        }
        return h;
    }

    public String label(TreeItem<NodeP> item) {
        if (item == null) {
            return "";
        }
        return hierarchyNumber(item) + " " + title(item.getValue());
    }

    public TreeItem<NodeP> find(NodeP node) {
        if (treeView == null || node == null) {
            return null;
        }
        return find(treeView.getRoot(), node);
    }

    public TreeItem<NodeP> find(TreeItem<NodeP> item, NodeP node) {
        if (item == null || node == null) {
            return null;
        }
        if (equalNode(node, item.getValue())) {
            return item;
        }
        List<TreeItem<NodeP>> children = item.getChildren();
        if (children == null) {
            return null;
        }
        for (TreeItem<NodeP> child : children) {
            TreeItem<NodeP> find = find(child, node);
            if (find != null) {
                return find;
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

    /*
        actions
     */
    @FXML
    public void popFunctionsMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "TreeFunctionsPopWhenMouseHovering", true)) {
            showFunctionsMenu(event);
        }
    }

    @FXML
    public void showFunctionsMenu(Event event) {
        TreeItem<NodeP> treeItem = selected();
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

        items.addAll(functionItems(item));

        items.add(new SeparatorMenuItem());
        return items;
    }

    public List<MenuItem> functionItems(TreeItem<NodeP> item) {
        return viewItems(item);
    }

    public List<MenuItem> foldItems(TreeItem<NodeP> item) {
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

    public List<MenuItem> viewItems(TreeItem<NodeP> item) {
        List<MenuItem> items = foldItems(item);

        MenuItem menu = new MenuItem(message("ViewNode"), StyleTools.getIconImageView("iconPop.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            viewNode(item);
        });
        menu.setDisable(item == null);
        items.add(menu);

        menu = new MenuItem(copyValueMessage(), StyleTools.getIconImageView("iconCopySystem.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            TextClipboardTools.copyToSystemClipboard(this, value(item.getValue()));
        });
        menu.setDisable(item == null);
        items.add(menu);

        menu = new MenuItem(copyTitleMessage(), StyleTools.getIconImageView("iconCopySystem.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            TextClipboardTools.copyToSystemClipboard(this, title(item.getValue()));
        });
        menu.setDisable(item == null);
        items.add(menu);

        menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            refreshAction();
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
        TextPopController.loadText(this, s);
    }

    @FXML
    public void foldNode() {
        fold(selected(), false);
    }

    @FXML
    public void foldNodeAndDecendants() {
        fold(selected(), true);
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
        unfold(selected(), false);
    }

    @FXML
    public void unfoldNodeAndDecendants() {
        unfold(selected(), true);
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
    public void popOperationsMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "TreeOperationsPopWhenMouseHovering", true)) {
            showOperationsMenu(event);
        }
    }

    @FXML
    public void showOperationsMenu(Event event) {
        TreeItem<NodeP> item = selected();
        if (item == null) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(StringTools.menuPrefix(label(item)));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        items.addAll(operationsItems(item));

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

    public List<MenuItem> operationsItems(TreeItem<NodeP> item) {
        return viewItems(item);
    }

    @FXML
    public void clearTree() {
        setRoot(null);
    }

}
