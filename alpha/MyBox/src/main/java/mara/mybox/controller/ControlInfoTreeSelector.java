package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.InfoNode;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class ControlInfoTreeSelector extends BaseInfoTreeViewController {

    protected ControlInfoNodeSelector selector;
    protected BaseInfoTreeViewController caller;

    @FXML
    protected CheckBox nodesListCheck;

    public void setCaller(BaseInfoTreeViewController caller) {
        if (caller == null) {
            return;
        }
        this.parentController = caller;
        this.baseName = caller.baseName + "_" + baseName;
        this.caller = caller;
        tableTreeNode = caller.tableTreeNode;
        tableTreeNodeTag = caller.tableTreeNodeTag;
        category = caller.category;
        cloneTree(caller.treeView);
    }

    /*
        data
     */
    public void cloneTree(TreeTableView<InfoNode> sourceTreeView) {
        if (sourceTreeView == null) {
            return;
        }
        TreeItem<InfoNode> sourceRoot = sourceTreeView.getRoot();
        if (sourceRoot == null) {
            return;
        }
        TreeItem<InfoNode> targetRoot = new TreeItem(sourceRoot.getValue());
        cloneNode(sourceRoot, targetRoot);
        setRoot(targetRoot);
    }

    public void cloneNode(TreeItem<InfoNode> sourceNode, TreeItem<InfoNode> targetNode) {
        if (sourceNode == null || targetNode == null) {
            return;
        }
        List<TreeItem<InfoNode>> sourceChildren = sourceNode.getChildren();
        if (sourceChildren == null) {
            return;
        }
        for (TreeItem<InfoNode> sourceChild : sourceChildren) {
            TreeItem<InfoNode> targetChild = new TreeItem<>(sourceChild.getValue());
            targetNode.getChildren().add(targetChild);
            targetChild.setExpanded(sourceChild.isExpanded());
            cloneNode(sourceChild, targetChild);
        }
    }

    public InfoNode copyNode(Connection conn, InfoNode sourceNode, InfoNode targetNode) {
        if (conn == null || sourceNode == null || targetNode == null) {
            if (task != null) {
                task.setError(message("InvalidData"));
            }
            return null;
        }
        try {
            InfoNode newNode = sourceNode.copyIn(targetNode);
            newNode = tableTreeNode.insertData(conn, newNode);
            if (newNode == null) {
                return null;
            }
            conn.commit();
            return newNode;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
    }

    public boolean copyNodeAndDescendants(Connection conn, InfoNode sourceNode, InfoNode targetNode) {
        return copyDescendants(conn, sourceNode, copyNode(conn, sourceNode, targetNode));
    }

    public boolean copyDescendants(Connection conn, InfoNode sourceNode, InfoNode targetNode) {
        if (conn == null || sourceNode == null || targetNode == null) {
            if (task != null) {
                task.setError(message("InvalidData"));
            }
            return false;
        }
        try {
            long sourceid = sourceNode.getNodeid();
            long targetid = targetNode.getNodeid();
            List<InfoNode> children = tableTreeNode.children(conn, sourceid);
            if (children != null && !children.isEmpty()) {
                conn.setAutoCommit(true);
                for (InfoNode child : children) {
                    InfoNode newNode = InfoNode.create()
                            .setParentid(targetid)
                            .setCategory(category)
                            .setTitle(child.getTitle())
                            .setInfo(child.getInfo());
                    tableTreeNode.insertData(conn, newNode);
                    copyDescendants(conn, child, newNode);
                }
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    /*
        action
     */
    @Override
    protected void viewNode(TreeItem<InfoNode> item) {
        selector.viewNode(item.getValue());
    }

    @Override
    public void nodeAdded(InfoNode parent, InfoNode newNode) {
        caller.addNewNode(caller.find(parent), newNode, true);
    }

    @Override
    public void doubleClicked(MouseEvent event, TreeItem<InfoNode> item) {
        if (item == null) {
            return;
        }
        String clickAction = UserConfig.getString(baseName + "TreeSelectorWhenDoubleClickNode", "View");
        if (clickAction == null) {
            return;
        }
        switch (clickAction) {
            case "PopMenu":
                showPopMenu(item);
                break;
            case "View":
                viewNode(item);
                break;
            case "OK":
                selector.okAction();
                break;
            case "LoadChildren":
                listChildren(item);
                break;
            case "LoadDescendants":
                listDescentants(item);
                break;
            default:
                break;
        }
    }

    @Override
    public List<MenuItem> viewMenuItems(TreeItem<InfoNode> item) {
        if (item == null) {
            return null;
        }
        List<MenuItem> items = super.viewMenuItems(item);

        items.add(new SeparatorMenuItem());

        items.add(doubleClickMenu(item));

        return items;
    }

    public Menu doubleClickMenu(TreeItem<InfoNode> treeItem) {
        Menu clickMenu = new Menu(message("WhenDoubleClickNode"), StyleTools.getIconImageView("iconSelectAll.png"));

        ToggleGroup clickGroup = new ToggleGroup();
        String currentClick = UserConfig.getString(baseName + "TreeSelectorWhenDoubleClickNode", "View");

        RadioMenuItem viewNodeMenu = new RadioMenuItem(message("ViewNode"), StyleTools.getIconImageView("iconView.png"));
        viewNodeMenu.setSelected("View".equals(currentClick));
        viewNodeMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "TreeSelectorWhenDoubleClickNode", "View");
            }
        });
        viewNodeMenu.setToggleGroup(clickGroup);

        RadioMenuItem okMenu = new RadioMenuItem(message("OK"), StyleTools.getIconImageView("iconOK.png"));
        okMenu.setSelected("OK".equals(currentClick));
        okMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "TreeSelectorWhenDoubleClickNode", "OK");
            }
        });
        okMenu.setToggleGroup(clickGroup);

        RadioMenuItem loadChildrenMenu = new RadioMenuItem(message("LoadChildren"), StyleTools.getIconImageView("iconList.png"));
        loadChildrenMenu.setSelected("LoadChildren".equals(currentClick));
        loadChildrenMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "TreeSelectorWhenDoubleClickNode", "LoadChildren");
            }
        });
        loadChildrenMenu.setToggleGroup(clickGroup);

        RadioMenuItem loadDescendantsMenu = new RadioMenuItem(message("LoadDescendants"), StyleTools.getIconImageView("iconList.png"));
        loadDescendantsMenu.setSelected("LoadDescendants".equals(currentClick));
        loadDescendantsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "TreeSelectorWhenDoubleClickNode", "LoadDescendants");
            }
        });
        loadDescendantsMenu.setToggleGroup(clickGroup);

        RadioMenuItem nothingMenu = new RadioMenuItem(message("DoNothing"));
        nothingMenu.setSelected(currentClick == null || "DoNothing".equals(currentClick));
        nothingMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "TreeSelectorWhenDoubleClickNode", "DoNothing");
            }
        });
        nothingMenu.setToggleGroup(clickGroup);

        RadioMenuItem clickPopMenu = new RadioMenuItem(message("PopMenu"), StyleTools.getIconImageView("iconMenu.png"));
        clickPopMenu.setSelected("PopMenu".equals(currentClick));
        clickPopMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "TreeSelectorWhenDoubleClickNode", "PopMenu");
            }
        });
        clickPopMenu.setToggleGroup(clickGroup);

        clickMenu.getItems().addAll(viewNodeMenu, okMenu, loadChildrenMenu, loadDescendantsMenu,
                clickPopMenu, nothingMenu);

        return clickMenu;
    }

    @FXML
    public void showPopMenu(TreeItem<InfoNode> item) {
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

        menu = new MenuItem(message("AddNode"), StyleTools.getIconImageView("iconAdd.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            addChild(item);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        items.add(doubleClickMenu(item));

        popNodeMenu(treeView, items);
    }

}
