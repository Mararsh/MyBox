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
import mara.mybox.db.data.InfoNode;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import static mara.mybox.db.data.InfoNode.TitleSeparater;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class ControlInfoTreeManage extends BaseInfoTreeController {

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

    public String chainName(Connection conn, InfoNode node) {
        if (node == null) {
            return null;
        }
        String chainName = "";
        List<InfoNode> ancestor = ancestor(conn, node);
        if (ancestor != null) {
            for (InfoNode a : ancestor) {
                chainName += a.getTitle() + TitleSeparater;
            }
        }
        chainName += node.getTitle();
        return chainName;
    }

    @Override
    public void itemClicked(MouseEvent event, TreeItem<InfoNode> item) {
        if (item == null) {
            return;
        }
        String clickAction = UserConfig.getString(baseName + "TreeWhenClickNode", "PopMenu");
        if (clickAction == null) {
            return;
        }
        switch (clickAction) {
            case "PopMenu":
                showItemMenu(item);
                break;
            case "Edit":
                editNode(item);
                break;
            case "Paste":
                pasteNode(item);
                break;
            case "View":
                viewNode(item);
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
    public void doubleClicked(MouseEvent event, TreeItem<InfoNode> item) {
        if (nodeExecutable) {
            executeNode(item);
        } else {
            viewNode(item);
        }
    }

    @Override
    public List<MenuItem> functionItems(TreeItem<InfoNode> treeItem) {
        boolean isRoot = treeItem == null || isRoot(treeItem.getValue());

        List<MenuItem> items = new ArrayList<>();

        Menu clickMenu = new Menu(message("WhenClickNode"), StyleTools.getIconImageView("iconSelect.png"));
        ToggleGroup clickGroup = new ToggleGroup();
        String currentClick = UserConfig.getString(baseName + "TreeWhenClickNode", "PopMenu");

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

        RadioMenuItem editNodeMenu = new RadioMenuItem(message("EditNode"), StyleTools.getIconImageView("iconEdit.png"));
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

        RadioMenuItem viewNodeMenu = new RadioMenuItem(message("ViewNode"), StyleTools.getIconImageView("iconView.png"));
        viewNodeMenu.setSelected("View".equals(currentClick));
        viewNodeMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "TreeWhenClickNode", "View");
            }
        });
        viewNodeMenu.setToggleGroup(clickGroup);

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

        viewMenu.getItems().addAll(foldItems(treeItem));

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

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("AddNode"), StyleTools.getIconImageView("iconAdd.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            addChild(treeItem);
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

        menu = new MenuItem(message("MoveNodes"), StyleTools.getIconImageView("iconMove.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            moveNode(treeItem);
        });
        menu.setDisable(isRoot);
        menu.setDisable(treeItem == null);
        items.add(menu);

        items.add(new SeparatorMenuItem());

        if (nodeExecutable) {
            menu = new MenuItem(message("Execute"), StyleTools.getIconImageView("iconGo.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                executeNode(treeItem);
            });
            menu.setDisable(treeItem == null);
            items.add(menu);
        }

        menu = new MenuItem(message("EditNode"), StyleTools.getIconImageView("iconEdit.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            editNode(treeItem);
        });
        menu.setDisable(treeItem == null);
        items.add(menu);

        menu = new MenuItem(message("PasteNodeValueToCurrentEdit"), StyleTools.getIconImageView("iconPaste.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                pasteNode(treeItem);
            }
        });
        items.add(menu);

        menu = new MenuItem(message("ViewNode"), StyleTools.getIconImageView("iconPop.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            viewNode(treeItem);
        });
        menu.setDisable(treeItem == null);
        items.add(menu);

        menu = new MenuItem(message("CopyValue"), StyleTools.getIconImageView("iconCopySystem.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            TextClipboardTools.copyToSystemClipboard(this, treeItem.getValue().getInfo());
        });
        menu.setDisable(treeItem == null);
        items.add(menu);

        menu = new MenuItem(message("CopyTitle"), StyleTools.getIconImageView("iconCopySystem.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            TextClipboardTools.copyToSystemClipboard(this, treeItem.getValue().getTitle());
        });
        menu.setDisable(treeItem == null);
        items.add(menu);

        return items;
    }

    @Override
    public List<MenuItem> operationsItems(TreeItem<InfoNode> item) {
        List<MenuItem> items = new ArrayList<>();
        items.addAll(viewItems(item));

        items.add(new SeparatorMenuItem());

        MenuItem menu = new MenuItem(message("TreeView"), StyleTools.getIconImageView("iconHtml.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            infoTree();
        });
        items.add(menu);

        menu = new MenuItem(message("Examples"), StyleTools.getIconImageView("iconExamples.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            importExamples();
        });
        items.add(menu);

        return items;
    }

    @Override
    public void nodeAdded(InfoNode parent, InfoNode newNode) {
        manageController.nodeAdded(parent, newNode);
    }

    protected void deleteNode(TreeItem<InfoNode> targetItem) {
        if (targetItem == null) {
            popError(message("SelectToHandle"));
            return;
        }
        InfoNode node = targetItem.getValue();
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
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private TreeItem<InfoNode> rootItem;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    if (isRoot) {
                        tableTreeNode.deleteChildren(conn, node.getNodeid());
                        InfoNode rootNode = root(conn);
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
                    setRoot(rootItem);
                } else {
                    targetItem.getChildren().clear();
                    if (targetItem.getParent() != null) {
                        targetItem.getParent().getChildren().remove(targetItem);
                    }
                }
                popSuccessful();
                manageController.nodeDeleted(node);
            }

        };
        start(task, treeView);
    }

    protected void renameNode(TreeItem<InfoNode> item) {
        if (item == null) {
            popError(message("SelectToHandle"));
            return;
        }
        InfoNode nodeValue = item.getValue();
        if (nodeValue == null || isRoot(nodeValue)) {
            popError(message("SelectToHandle"));
            return;
        }
        String chainName = chainName(item);
        String name = PopTools.askValue(getBaseTitle(), chainName, message("RenameNode"), nodeValue.getTitle() + "m");
        if (name == null || name.isBlank()) {
            return;
        }
        if (name.contains(TitleSeparater)) {
            popError(message("NodeNameNotInclude") + " \"" + TitleSeparater + "\"");
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private InfoNode updatedNode;

            @Override
            protected boolean handle() {
                nodeValue.setTitle(name);
                updatedNode = tableTreeNode.updateData(nodeValue);
                return updatedNode != null;
            }

            @Override
            protected void whenSucceeded() {
                item.setValue(updatedNode);
                treeView.refresh();
                manageController.nodeRenamed(updatedNode);
                popSuccessful();
            }
        };
        start(task, treeView);
    }

    protected void copyNode(TreeItem<InfoNode> item) {
        if (item == null || isRoot(item.getValue())) {
            return;
        }
        String chainName = chainName(item);
        TreeNodeCopyController controller
                = (TreeNodeCopyController) WindowTools.openChildStage(getMyWindow(), Fxmls.TreeNodeCopyFxml);
        controller.setCaller(this, item.getValue(), chainName);
    }

    protected void moveNode(TreeItem<InfoNode> item) {
        if (item == null || isRoot(item.getValue())) {
            return;
        }
        String chainName = chainName(item);
        TreeNodeMoveController controller = (TreeNodeMoveController) WindowTools.openChildStage(getMyWindow(), Fxmls.TreeNodeMoveFxml);
        controller.setCaller(this, item.getValue(), chainName);
    }

    protected void nodeMoved(InfoNode parent, InfoNode node) {
        manageController.nodeMoved(parent, node);
    }

    protected void editNode(TreeItem<InfoNode> item) {
        if (item == null) {
            return;
        }
        manageController.editNode(item.getValue());
    }

    protected void pasteNode(TreeItem<InfoNode> item) {
        if (item == null) {
            return;
        }
        manageController.pasteNode(item.getValue());
    }

    protected void executeNode(TreeItem<InfoNode> item) {
        if (item == null) {
            return;
        }
        manageController.executeNode(item.getValue());
    }

    protected void exportNode(TreeItem<InfoNode> item) {
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

    protected void listChildren(TreeItem<InfoNode> item) {
        if (item == null) {
            return;
        }
        manageController.loadChildren(item.getValue());
    }

    protected void listDescentants(TreeItem<InfoNode> item) {
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
