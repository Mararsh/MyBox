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
import mara.mybox.fxml.cell.TreeTableHierachyCell;
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
public abstract class BaseTreeViewController<NodeP> extends BaseController {

    protected final SimpleBooleanProperty loadedNotify;
    protected NodeP selectNodeWhenLoaded;

    @FXML
    protected TreeTableView<NodeP> treeView;
    @FXML
    protected TreeTableColumn<NodeP, String> hierarchyColumn, titleColumn, valueColumn;

    public BaseTreeViewController() {
        loadedNotify = new SimpleBooleanProperty(false);
    }

    /*
        abstract
     */
    public abstract void unfold(TreeItem<NodeP> item, boolean descendants);

    public abstract String title(NodeP node);

    public abstract String value(NodeP node);

    public abstract boolean equal(NodeP node1, NodeP node2);

    public abstract void addChild(TreeItem<NodeP> targetItem);

    /*
        init
     */
    @Override
    public void initControls() {
        try {
            super.initControls();

            initTree();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initTree() {
        try {
            hierarchyColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("title"));
            hierarchyColumn.setCellFactory(new TreeTableHierachyCell());

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
                        doubleClicked(item);
                    } else {
                        itemSelected(item);
                    }
                }
            });

            loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    treeLoaded();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        tree
     */
    public void loadTree() {
    }

    public void treeLoaded() {
        if (selectNodeWhenLoaded != null) {
            selectNode(selectNodeWhenLoaded);
        }
    }

    public boolean nodeLoaded(TreeItem<NodeP> item) {
        try {
            return title(item.getChildren().get(0).getValue()) != null;
        } catch (Exception e) {
            return true;
        }
    }

    public void itemSelected(TreeItem<NodeP> item) {
    }

    public void doubleClicked(TreeItem<NodeP> item) {
    }

    public void addNewNode(TreeItem<NodeP> parent, NodeP node, boolean select) {
        if (parent == null || node == null) {
            return;
        }
        TreeItem<NodeP> child = new TreeItem(node);
        parent.getChildren().add(child);
        child.setExpanded(false);
        if (select) {
            selectItem(child);
            itemSelected(child);
        }
    }

    public void selectItem(TreeItem<NodeP> nodeitem) {
        if (treeView == null || nodeitem == null) {
            return;
        }
        isSettingValues = true;
        treeView.getSelectionModel().select(nodeitem);
        isSettingValues = false;
        treeView.scrollTo(treeView.getRow(nodeitem));
    }

    public void selectNode(NodeP node) {
        if (treeView == null || node == null) {
            return;
        }
        if (treeView.getRoot() != null) {
            selectItem(find(node));
        }
        selectNodeWhenLoaded = null;
    }

    public void selectNodeWhenLoaded(NodeP node) {
        try {
            if (treeView.getRoot() != null) {
                selectNode(node);
            } else {
                selectNodeWhenLoaded = node;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        values
     */
    public TreeItem<NodeP> selected() {
        if (treeView == null) {
            return null;
        }
        TreeItem<NodeP> selecteItem = treeView.getSelectionModel().getSelectedItem();
        if (selecteItem == null) {
            selecteItem = treeView.getRoot();
        }
        return selecteItem;
    }

    public boolean isRoot(NodeP node) {
        if (treeView.getRoot() == null || node == null) {
            return false;
        }
        return equal(treeView.getRoot().getValue(), node);
    }

    public boolean isSourceNode(NodeP node) {
        return false;
    }

    public boolean equalOrDescendant(TreeItem<NodeP> item1, TreeItem<NodeP> item2) {
        if (item1 == null || item2 == null) {
            return false;
        }
        NodeP node1 = item1.getValue();
        NodeP node2 = item2.getValue();
        if (node1 == null || node2 == null) {
            return false;
        }
        if (equal(node1, node2)) {
            return true;
        }
        return equalOrDescendant(item1.getParent(), item2);
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
        if (equal(node, item.getValue())) {
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
        List<MenuItem> items = makeFunctionsMenu(treeItem);

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean(baseName + "TreeFunctionsPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean(baseName + "TreeFunctionsPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);
        popEventMenu(event, items);
    }

    public void showItemMenu(TreeItem<NodeP> item) {
        popNodeMenu(treeView, makeFunctionsMenu(item));
    }

    public List<MenuItem> makeFunctionsMenu(TreeItem<NodeP> item) {
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu = new MenuItem(StringTools.menuSuffix(title(item.getValue())));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());
        items.addAll(functionItems(item));
        items.add(new SeparatorMenuItem());
        return items;
    }

    public List<MenuItem> functionItems(TreeItem<NodeP> item) {
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

        menu = new MenuItem(message("CopyValue"), StyleTools.getIconImageView("iconCopySystem.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            TextClipboardTools.copyToSystemClipboard(this, value(item.getValue()));
        });
        menu.setDisable(item == null);
        items.add(menu);

        menu = new MenuItem(message("CopyTitle"), StyleTools.getIconImageView("iconCopySystem.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            TextClipboardTools.copyToSystemClipboard(this, title(item.getValue()));
        });
        menu.setDisable(item == null);
        items.add(menu);

        menu = new MenuItem(message("AddNode"), StyleTools.getIconImageView("iconAdd.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            addChild(item);
        });
        items.add(menu);

        menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            refreshAction();
        });
        items.add(menu);

        List<MenuItem> more = moreMenuItems(item);
        if (more != null) {
            items.addAll(more);
        }

        return items;
    }

    public List<MenuItem> moreMenuItems(TreeItem<NodeP> item) {
        return null;
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
        if (descendants) {
            List<TreeItem<NodeP>> children = item.getChildren();
            if (children != null) {
                for (TreeItem<NodeP> child : children) {
                    fold(child, true);
                    child.setExpanded(false);
                }
            }
        }
        item.setExpanded(false);
    }

    @FXML
    public void unfoldNode() {
        unfold(selected(), false);
    }

    @FXML
    public void unfoldNodeAndDecendants() {
        unfold(selected(), true);
    }

    public void nodeAdded(NodeP parent, NodeP newNode) {
    }

    @FXML
    @Override
    public void refreshAction() {
        loadTree();
    }

}
