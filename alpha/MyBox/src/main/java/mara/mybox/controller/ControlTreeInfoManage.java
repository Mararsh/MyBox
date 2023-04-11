package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.TreeNode;
import static mara.mybox.db.data.TreeNode.NodeSeparater;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class ControlTreeInfoManage extends BaseTreeInfoController {

    protected TreeManageController manageController;

    public void setParameters(TreeManageController parent) {
        this.parentController = parent;
        this.baseName = parent.baseName;
        manageController = parent;
        tableTreeNode = parent.tableTreeNode;
        tableTreeNodeTag = parent.tableTreeNodeTag;
        category = manageController.category;
        baseTitle = category;
        nodeExecutable = manageController != null
                && (manageController.startButton != null || manageController.goButton != null
                || (manageController.nodeController != null
                && (manageController.nodeController.startButton != null
                || manageController.nodeController.goButton != null)));
    }

    public String chainName(Connection conn, TreeNode node) {
        if (node == null) {
            return null;
        }
        String chainName = "";
        List<TreeNode> ancestor = ancestor(conn, node);
        if (ancestor != null) {
            for (TreeNode a : ancestor) {
                chainName += a.getTitle() + NodeSeparater;
            }
        }
        chainName += node.getTitle();
        return chainName;
    }

    @Override
    public void itemSelected(TreeItem<TreeNode> item) {
        if (item == null) {
            return;
        }
        String clickAction = UserConfig.getString(baseName + "TreeWhenClickNode", "DoNothing");
        switch (clickAction) {
            case "PopMenu":
                popFunctionsMenu(null, item);
                break;
            case "Edit":
                editNode(item);
                break;
            case "Paste":
                pasteNode(item);
                break;
            case "Execute":
                executeNode(item);
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
    protected void doubleClicked(TreeItem<TreeNode> item) {
        editNode(item);
    }

    @FXML
    public void popViewMenu(MouseEvent event) {
        if (isSettingValues || getMyWindow() == null) {
            return;
        }
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

        menu = new MenuItem(message("TreeView"), StyleTools.getIconImageView("iconHtml.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            infoTree();
        });
        items.add(menu);

        menu = new MenuItem(message("Examples"), StyleTools.getIconImageView("iconExamples.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            importExamples();
        });
        items.add(menu);

        menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            refreshAction();
        });
        items.add(menu);

        popMenu(infoTree, items);
    }

    @Override
    protected List<MenuItem> makeFunctionsMenu(TreeItem<TreeNode> treeItem) {
        boolean isRoot = treeItem == null || isRoot(treeItem.getValue());

        List<MenuItem> items = new ArrayList<>();

        Menu clickMenu = new Menu(message("WhenClickNode"), StyleTools.getIconImageView("iconSelect.png"));
        ToggleGroup clickGroup = new ToggleGroup();
        String currentClick = UserConfig.getString(baseName + "TreeWhenClickNode", "DoNothing");

        RadioMenuItem nothingMenu = new RadioMenuItem(message("DoNothing"));
        nothingMenu.setSelected(currentClick == null || "DoNothing".equals(currentClick));
        nothingMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "TreeWhenClickNode", "DoNothing");
            }
        });
        nothingMenu.setToggleGroup(clickGroup);

        RadioMenuItem clickPopMenu = new RadioMenuItem(message("PopMenu"), StyleTools.getIconImageView("iconMenu.png"));
        clickPopMenu.setSelected("PopMenu".equals(currentClick));
        clickPopMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "TreeWhenClickNode", "PopMenu");
            }
        });
        clickPopMenu.setToggleGroup(clickGroup);

        RadioMenuItem editNodeMenu = new RadioMenuItem(message("Edit"), StyleTools.getIconImageView("iconEdit.png"));
        editNodeMenu.setSelected("Edit".equals(currentClick));
        editNodeMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "TreeWhenClickNode", "Edit");
            }
        });
        editNodeMenu.setToggleGroup(clickGroup);

        RadioMenuItem pasteNodeMenu = new RadioMenuItem(message("PasteNodeValueToCurrentEdit"), StyleTools.getIconImageView("iconPaste.png"));
        pasteNodeMenu.setSelected("Paste".equals(currentClick));
        pasteNodeMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "TreeWhenClickNode", "Paste");
            }
        });
        pasteNodeMenu.setToggleGroup(clickGroup);

        clickMenu.getItems().addAll(nothingMenu, clickPopMenu, editNodeMenu, pasteNodeMenu);

        if (nodeExecutable) {
            RadioMenuItem executeNodeMenu = new RadioMenuItem(message("Execute"), StyleTools.getIconImageView("iconGo.png"));
            executeNodeMenu.setSelected("Execute".equals(currentClick));
            executeNodeMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString(baseName + "TreeWhenClickNode", "Execute");
                }
            });
            executeNodeMenu.setToggleGroup(clickGroup);
            clickMenu.getItems().add(executeNodeMenu);
        }

        RadioMenuItem loadChildrenMenu = new RadioMenuItem(message("LoadChildren"), StyleTools.getIconImageView("iconList.png"));
        loadChildrenMenu.setSelected("LoadChildren".equals(currentClick));
        loadChildrenMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "TreeWhenClickNode", "LoadChildren");
            }
        });
        loadChildrenMenu.setToggleGroup(clickGroup);

        RadioMenuItem loadDescendantsMenu = new RadioMenuItem(message("LoadDescendants"), StyleTools.getIconImageView("iconList.png"));
        loadDescendantsMenu.setSelected("LoadDescendants".equals(currentClick));
        loadDescendantsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "TreeWhenClickNode", "LoadDescendants");
            }
        });
        loadDescendantsMenu.setToggleGroup(clickGroup);

        clickMenu.getItems().addAll(loadChildrenMenu, loadDescendantsMenu);

        items.add(clickMenu);

        items.add(new SeparatorMenuItem());

        Menu dataMenu = new Menu(message("Data"), StyleTools.getIconImageView("iconData.png"));
        items.add(dataMenu);

        MenuItem menu = new MenuItem(message("TreeView"), StyleTools.getIconImageView("iconHtml.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            infoTree();
        });
        dataMenu.getItems().add(menu);

        menu = new MenuItem(message("Examples"), StyleTools.getIconImageView("iconExamples.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            importExamples();
        });
        dataMenu.getItems().add(menu);

        menu = new MenuItem(message("Export"), StyleTools.getIconImageView("iconExport.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            exportNode(treeItem);
        });
        dataMenu.getItems().add(menu);

        menu = new MenuItem(message("Import"), StyleTools.getIconImageView("iconImport.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            importAction();
        });
        dataMenu.getItems().add(menu);

        Menu viewMenu = new Menu(message("View"), StyleTools.getIconImageView("iconView.png"));
        items.add(viewMenu);

        menu = new MenuItem(message("UnfoldNode"), StyleTools.getIconImageView("iconPlus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            unfoldNode();
        });
        viewMenu.getItems().add(menu);

        menu = new MenuItem(message("UnfoldNodeAndDescendants"), StyleTools.getIconImageView("iconPlus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            unfoldNodeAndDecendants();
        });
        viewMenu.getItems().add(menu);

        menu = new MenuItem(message("FoldNode"), StyleTools.getIconImageView("iconMinus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            foldNode();
        });
        viewMenu.getItems().add(menu);

        menu = new MenuItem(message("FoldNodeAndDescendants"), StyleTools.getIconImageView("iconMinus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            foldNodeAndDecendants();
        });
        viewMenu.getItems().add(menu);

        menu = new MenuItem(message("LoadChildren"), StyleTools.getIconImageView("iconList.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                listChildren(treeItem);
            }
        });
        viewMenu.getItems().add(menu);

        menu = new MenuItem(message("LoadDescendants"), StyleTools.getIconImageView("iconList.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                listDescentants(treeItem);
            }
        });
        viewMenu.getItems().add(menu);

        menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            refreshAction();
        });
        viewMenu.getItems().add(menu);

        menu = new MenuItem(message("EditNode"), StyleTools.getIconImageView("iconEdit.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            editNode(treeItem);
        });
        items.add(menu);

        menu = new MenuItem(message("PasteNodeValueToCurrentEdit"), StyleTools.getIconImageView("iconPaste.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                pasteNode(treeItem);
            }
        });
        items.add(menu);

        menu = new MenuItem(message("AddNode"), StyleTools.getIconImageView("iconAdd.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            addNode(treeItem);
        });
        items.add(menu);

        menu = new MenuItem(message("DeleteNode"), StyleTools.getIconImageView("iconDelete.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            deleteNode(treeItem);
        });
        items.add(menu);

        menu = new MenuItem(message("RenameNode"), StyleTools.getIconImageView("iconInput.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            renameNode(treeItem);
        });
        menu.setDisable(isRoot);
        items.add(menu);

        menu = new MenuItem(message("CopyNodes"), StyleTools.getIconImageView("iconCopy.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            copyNode(treeItem);
        });
        menu.setDisable(isRoot);
        items.add(menu);

        menu = new MenuItem(message("MoveNodes"), StyleTools.getIconImageView("iconRef.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            moveNode(treeItem);
        });
        menu.setDisable(isRoot);
        items.add(menu);

        return items;
    }

    @Override
    protected void nodeAdded(TreeNode parent, TreeNode newNode) {
        manageController.nodeAdded(parent, newNode);
    }

    protected void deleteNode(TreeItem<TreeNode> targetItem) {
        if (targetItem == null) {
            popError(message("SelectToHandle"));
            return;
        }
        TreeNode node = targetItem.getValue();
        if (node == null) {
            popError(message("SelectToHandle"));
            return;
        }
        boolean isRoot = isRoot(node);
        if (isRoot) {
            if (!PopTools.askSure(getTitle(), message("Delete"), message("SureDeleteAll"))) {
                return;
            }
        } else {
            String chainName = chainName(targetItem);
            if (!PopTools.askSure(getTitle(), chainName, message("Delete"))) {
                return;
            }
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>(this) {

                private TreeItem<TreeNode> rootItem;

                @Override
                protected boolean handle() {
                    try (Connection conn = DerbyBase.getConnection()) {
                        if (isRoot) {
                            tableTreeNode.deleteChildren(conn, node.getNodeid());
                            TreeNode rootNode = root(conn);
                            rootItem = new TreeItem(rootNode);
                        } else {
                            tableTreeNode.deleteData(conn, node);
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (isRoot) {
                        infoTree.setRoot(rootItem);
                        rootItem.setExpanded(true);
                    } else {
                        targetItem.getChildren().clear();
                        if (targetItem.getParent() != null) {
                            targetItem.getParent().getChildren().remove(targetItem);
                        }
                    }
                    popSuccessful();
                }

            };
            start(task, infoTree);
        }
    }

    protected void nodeDeleted(TreeNode node) {
        manageController.nodeDeleted(node);
    }

    protected void renameNode(TreeItem<TreeNode> item) {
        if (item == null) {
            popError(message("SelectToHandle"));
            return;
        }
        TreeNode nodeValue = item.getValue();
        if (nodeValue == null || isRoot(nodeValue)) {
            popError(message("SelectToHandle"));
            return;
        }
        String chainName = chainName(item);
        String name = PopTools.askValue(getBaseTitle(), chainName, message("RenameNode"), nodeValue.getTitle() + "m");
        if (name == null || name.isBlank()) {
            return;
        }
        if (name.contains(NodeSeparater)) {
            popError(message("NodeNameNotInclude") + " \"" + NodeSeparater + "\"");
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>(this) {
                private TreeNode updatedNode;

                @Override
                protected boolean handle() {
                    nodeValue.setTitle(name);
                    updatedNode = tableTreeNode.updateData(nodeValue);
                    return updatedNode != null;
                }

                @Override
                protected void whenSucceeded() {
                    item.setValue(updatedNode);
                    infoTree.refresh();
                    manageController.nodeRenamed(updatedNode);
                    popSuccessful();
                }
            };
            start(task, infoTree);
        }
    }

    protected void copyNode(TreeItem<TreeNode> item) {
        if (item == null || isRoot(item.getValue())) {
            return;
        }
        String chainName = chainName(item);
        TreeNodeCopyController controller
                = (TreeNodeCopyController) WindowTools.openChildStage(getMyWindow(), Fxmls.TreeNodeCopyFxml);
        controller.setCaller(this, item.getValue(), chainName);
    }

    protected void moveNode(TreeItem<TreeNode> item) {
        if (item == null || isRoot(item.getValue())) {
            return;
        }
        String chainName = chainName(item);
        TreeNodeMoveController controller = (TreeNodeMoveController) WindowTools.openChildStage(getMyWindow(), Fxmls.TreeNodeMoveFxml);
        controller.setCaller(this, item.getValue(), chainName);
    }

    protected void nodeMoved(TreeNode parent, TreeNode node) {
        manageController.nodeMoved(parent, node);
    }

    protected void editNode(TreeItem<TreeNode> item) {
        if (item == null) {
            return;
        }
        manageController.editNode(item.getValue());
    }

    protected void pasteNode(TreeItem<TreeNode> item) {
        if (item == null) {
            return;
        }
        manageController.pasteNode(item.getValue());
    }

    protected void executeNode(TreeItem<TreeNode> item) {
        if (item == null) {
            return;
        }
        manageController.executeNode(item.getValue());
    }

    protected void exportNode(TreeItem<TreeNode> item) {
        TreeNodeExportController exportController
                = (TreeNodeExportController) WindowTools.openChildStage(getMyWindow(), Fxmls.TreeNodeExportFxml);
        exportController.setParamters(manageController, item);
    }

    @FXML
    protected void importAction() {
        TreeNodeImportController controller = (TreeNodeImportController) WindowTools.openChildStage(getMyWindow(), Fxmls.TreeNodeImportFxml);
        controller.setCaller(this);
    }

    @Override
    protected void afterImport() {
        manageController.tagsController.refreshAction();
        manageController.refreshTimes();
    }

    protected void listChildren(TreeItem<TreeNode> item) {
        if (item == null) {
            return;
        }
        manageController.loadChildren(item.getValue());
    }

    protected void listDescentants(TreeItem<TreeNode> item) {
        if (item == null) {
            return;
        }
        manageController.loadDescendants(item.getValue());
    }

    @FXML
    @Override
    public void cancelAction() {

    }

}
