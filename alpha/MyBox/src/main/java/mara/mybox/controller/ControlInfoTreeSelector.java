package mara.mybox.controller;

import java.sql.Connection;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class ControlInfoTreeSelector extends BaseInfoTreeController {

    protected BaseInfoTreeController caller;

    public void setCaller(BaseInfoTreeController caller) {
        if (caller == null) {
            return;
        }
        this.parentController = caller;
        this.baseName = caller.baseName;
        this.caller = caller;
        tableTreeNode = caller.tableTreeNode;
        tableTreeNodeTag = caller.tableTreeNodeTag;
        category = caller.category;
        cloneTree(caller.treeView);
    }

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
            case "View":
                viewNode(item);
                break;
            case "OK":
                okAction();
                break;
            default:
                break;
        }
    }

    @Override
    public void nodeAdded(InfoNode parent, InfoNode newNode) {
        caller.addNewNode(caller.find(parent), newNode, true);
    }

    @Override
    public List<MenuItem> functionMenuItems(TreeItem<InfoNode> treeItem) {
        List<MenuItem> items = viewMenuItems(treeItem);

        items.add(new SeparatorMenuItem());

        items.add(doubleClickMenu(treeItem));

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

        RadioMenuItem nothingMenu = new RadioMenuItem(message("DoNothing"));
        nothingMenu.setSelected(currentClick == null || "DoNothing".equals(currentClick));
        nothingMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "TreeSelectorWhenDoubleClickNode", "DoNothing");
            }
        });
        nothingMenu.setToggleGroup(clickGroup);

        clickMenu.getItems().addAll(viewNodeMenu, okMenu, nothingMenu);

        return clickMenu;
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

}
