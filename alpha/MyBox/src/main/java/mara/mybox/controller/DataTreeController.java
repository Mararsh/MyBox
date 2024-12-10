package mara.mybox.controller;

import java.io.File;
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
import mara.mybox.data2d.DataInternalTable;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.db.table.TableNodeData2DDefinition;
import mara.mybox.db.table.TableNodeHtml;
import mara.mybox.db.table.TableNodeImageScope;
import mara.mybox.db.table.TableNodeJEXL;
import mara.mybox.db.table.TableNodeJShell;
import mara.mybox.db.table.TableNodeJavaScript;
import mara.mybox.db.table.TableNodeMathFunction;
import mara.mybox.db.table.TableNodeRowFilter;
import mara.mybox.db.table.TableNodeSQL;
import mara.mybox.db.table.TableNodeText;
import mara.mybox.db.table.TableNodeWebFavorite;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2024-8-8
 * @License Apache License Version 2.0
 */
public class DataTreeController extends BaseDataTreeViewController {

    /*
        tree
     */
    @Override
    public void whenTreeEmpty() {
        File file = nodeTable.exampleFile();
        if (file != null) {
            if (AppVariables.isTesting
                    || PopTools.askSure(getTitle(), message("ImportExamples") + ": " + baseTitle)) {
                importExamples(null);
            }
        }
    }

    @Override
    public void doubleClicked(MouseEvent event, TreeItem<DataNode> item) {
        clicked(UserConfig.getString(baseName + "WhenDoubleClickNode", "PopNode"), item);
    }

    @Override
    public void rightClicked(MouseEvent event, TreeItem<DataNode> item) {
        clicked(UserConfig.getString(baseName + "WhenRightClickNode", "PopMenu"), item);
    }

    public void clicked(String clickAction, TreeItem<DataNode> item) {
        if (item == null || clickAction == null) {
            return;
        }
        switch (clickAction) {
            case "PopMenu":
                showPopMenu(item);
                break;
            case "EditNode":
                editNode(item.getValue());
                break;
            case "PopNode":
                popNode(item.getValue());
                break;
            case "ExecuteNode":
                executeNode(item.getValue());
                break;
            default:
                break;
        }
    }

    /*
        menu
     */
    @Override
    public List<MenuItem> operationsMenuItems(TreeItem<DataNode> treeItem) {
        List<MenuItem> items = new ArrayList<>();

        items.addAll(updateMenuItems(treeItem));

        items.add(new SeparatorMenuItem());

        items.add(doubleClickMenu(treeItem));
        items.add(rightClickMenu(treeItem));

        return items;
    }

    public List<MenuItem> updateMenuItems(TreeItem<DataNode> treeItem) {
        if (treeItem == null) {
            return null;
        }
        boolean isRoot = isRoot(treeItem.getValue());
        boolean isLeaf = treeItem.isLeaf();

        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(message("EditNode"), StyleTools.getIconImageView("iconEdit.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            editNode(treeItem.getValue());
        });
        items.add(menu);

        if (nodeTable.isNodeExecutable()) {
            menu = new MenuItem(message("ExecuteNode"), StyleTools.getIconImageView("iconGo.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                executeNode(treeItem.getValue());
            });
            items.add(menu);
        }

        menu = new MenuItem(message("AddChildNode"), StyleTools.getIconImageView("iconAdd.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            addChild(treeItem.getValue());
        });
        items.add(menu);

        if (!isRoot) {
            menu = new MenuItem(message("ChangeNodeTitle"), StyleTools.getIconImageView("iconInput.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                renameNode(treeItem);
            });
            items.add(menu);

        }

        if (!isRoot) {
            menu = new MenuItem(message("ChangeNodeOrder"), StyleTools.getIconImageView("iconInput.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                reorderNode(treeItem);
            });

        }

        MenuItem orderMenuItem = new MenuItem(message("ChangeNodeOrder"), StyleTools.getIconImageView("iconInput.png"));
        orderMenuItem.setOnAction((ActionEvent menuItemEvent) -> {
            reorderNode(treeItem);
        });

        if (isLeaf) {
            if (!isRoot) {
                items.add(orderMenuItem);
            }
        } else {
            Menu orderMenu = new Menu(message("OrderNumber"));

            if (!isRoot) {
                orderMenu.getItems().add(orderMenuItem);
            }

            menu = new MenuItem(message("TrimDescendantsOrders"), StyleTools.getIconImageView("iconClean.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                trimDescendantsOrders(treeItem, true);
            });
            orderMenu.getItems().add(menu);

            menu = new MenuItem(message("TrimChildrenOrders"), StyleTools.getIconImageView("iconClean.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                trimDescendantsOrders(treeItem, false);
            });
            orderMenu.getItems().add(menu);
        }

        menu = new MenuItem(message("CopyNodes"), StyleTools.getIconImageView("iconCopy.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            copyNodes(treeItem);
        });
        items.add(menu);

        menu = new MenuItem(message("MoveNodes"), StyleTools.getIconImageView("iconMove.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            moveNodes(treeItem);
        });
        items.add(menu);

        Menu deleteMenu = new Menu(message("Delete"));

        if (!isRoot) {
            menu = new MenuItem(message("DeleteNodeAndDescendants"), StyleTools.getIconImageView("iconDelete.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                deleteNodeAndDescendants(treeItem);
            });
            deleteMenu.getItems().add(menu);
        }

        if (!isLeaf) {
            menu = new MenuItem(message("DeleteDescendants"), StyleTools.getIconImageView("iconDelete.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                deleteDescendants(treeItem);
            });
            deleteMenu.getItems().add(menu);
        }

        menu = new MenuItem(message("DeleteNodes"), StyleTools.getIconImageView("iconDelete.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            deleteNodes(treeItem);
        });
        deleteMenu.getItems().add(menu);

        items.add(deleteMenu);

        return items;
    }

    @Override
    public List<MenuItem> dataMenuItems(TreeItem<DataNode> item) {
        if (item == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(message("Tags"), StyleTools.getIconImageView("iconTag.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            DataTreeTagsController.manage(this);
        });
        items.add(menu);

        menu = new MenuItem(message("Examples"), StyleTools.getIconImageView("iconExamples.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            importExamples(item);
        });
        items.add(menu);

        menu = new MenuItem(message("Export"), StyleTools.getIconImageView("iconExport.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            exportNode(item);
        });
        items.add(menu);

        menu = new MenuItem(message("Import"), StyleTools.getIconImageView("iconImport.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            importAction(item);
        });
        items.add(menu);

        menu = new MenuItem(message("DataManufacture"), StyleTools.getIconImageView("iconDatabase.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            manufactureData();
        });
        items.add(menu);

        return items;
    }

    public Menu doubleClickMenu(TreeItem<DataNode> treeItem) {
        Menu clickMenu = new Menu(message("WhenDoubleClickNode"), StyleTools.getIconImageView("iconSelectAll.png"));
        clickMenu(treeItem, clickMenu, "WhenDoubleClickNode", "EditNode");
        return clickMenu;
    }

    public Menu rightClickMenu(TreeItem<DataNode> treeItem) {
        Menu clickMenu = new Menu(message("WhenRightClickNode"), StyleTools.getIconImageView("iconSelectNone.png"));
        clickMenu(treeItem, clickMenu, "WhenRightClickNode", "PopMenu");
        return clickMenu;
    }

    public Menu clickMenu(TreeItem<DataNode> treeItem, Menu menu, String key, String defaultAction) {
        ToggleGroup clickGroup = new ToggleGroup();
        String currentClick = UserConfig.getString(baseName + key, defaultAction);

        RadioMenuItem editNodeMenu = new RadioMenuItem(message("EditNode"), StyleTools.getIconImageView("iconEdit.png"));
        editNodeMenu.setSelected("EditNode".equals(currentClick));
        editNodeMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + key, "EditNode");
            }
        });
        editNodeMenu.setToggleGroup(clickGroup);

        RadioMenuItem popNodeMenu = new RadioMenuItem(message("PopNode"), StyleTools.getIconImageView("iconPop.png"));
        popNodeMenu.setSelected("PopNode".equals(currentClick));
        popNodeMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + key, "PopNode");
            }
        });
        popNodeMenu.setToggleGroup(clickGroup);

        menu.getItems().addAll(editNodeMenu, popNodeMenu);

        if (nodeTable.isNodeExecutable()) {
            RadioMenuItem executeNodeMenu = new RadioMenuItem(message("ExecuteNode"), StyleTools.getIconImageView("iconGo.png"));
            executeNodeMenu.setSelected("ExecuteNode".equals(currentClick));
            executeNodeMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString(baseName + key, "ExecuteNode");
                }
            });
            executeNodeMenu.setToggleGroup(clickGroup);
            menu.getItems().add(executeNodeMenu);
        }

        RadioMenuItem nothingMenu = new RadioMenuItem(message("DoNothing"));
        nothingMenu.setSelected(currentClick == null || "DoNothing".equals(currentClick));
        nothingMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + key, "DoNothing");
            }
        });
        nothingMenu.setToggleGroup(clickGroup);

        RadioMenuItem clickPopMenu = new RadioMenuItem(message("ContextMenu"), StyleTools.getIconImageView("iconMenu.png"));
        clickPopMenu.setSelected("PopMenu".equals(currentClick));
        clickPopMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + key, "PopMenu");
            }
        });
        clickPopMenu.setToggleGroup(clickGroup);

        menu.getItems().addAll(clickPopMenu, nothingMenu);

        return menu;
    }

    @FXML
    public void showPopMenu(TreeItem<DataNode> item) {
        if (item == null) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(StringTools.menuPrefix(label(item)));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        Menu dataMenu = new Menu(message("Data"), StyleTools.getIconImageView("iconData.png"));
        dataMenu.getItems().addAll(dataMenuItems(item));
        items.add(dataMenu);

        Menu treeMenu = new Menu(message("View"), StyleTools.getIconImageView("iconView.png"));
        treeMenu.getItems().addAll(viewMenuItems(item));
        items.add(treeMenu);

        items.add(new SeparatorMenuItem());

        items.addAll(operationsMenuItems(item));

        popNodeMenu(treeView, items);
    }

    protected void deleteNodeAndDescendants(TreeItem<DataNode> item) {
        if (item == null) {
            popError(message("SelectToHandle"));
            return;
        }
        DataNode node = item.getValue();
        if (node == null) {
            popError(message("SelectToHandle"));
            return;
        }
        boolean isRoot = isRoot(node);
        if (isRoot) {
            if (!PopTools.askSure(getTitle(), message("DeleteNodeAndDescendants"), message("SureDeleteAll"))) {
                return;
            }
        } else {
            String chainName = chainName(item);
            if (!PopTools.askSure(getTitle(), chainName, message("DeleteNodeAndDescendants"))) {
                return;
            }
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    return nodeTable.deleteNode(conn, node.getNodeid()) >= 0;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (isRoot) {
                    loadTree(null);
                } else {
                    item.getParent().getChildren().remove(item);
                    reloadCurrent();
                }
                popSuccessful();
            }

        };
        start(task, treeView);
    }

    protected void deleteDescendants(TreeItem<DataNode> item) {
        if (item == null) {
            popError(message("SelectToHandle"));
            return;
        }
        DataNode node = item.getValue();
        if (node == null) {
            popError(message("SelectToHandle"));
            return;
        }
        boolean isRoot = isRoot(node);
        if (isRoot) {
            if (!PopTools.askSure(getTitle(), message("DeleteDescendants"), message("SureDeleteAll"))) {
                return;
            }
        } else {
            String chainName = chainName(item);
            if (!PopTools.askSure(getTitle(), chainName, message("DeleteDescendants"))) {
                return;
            }
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    return nodeTable.deleteDecentants(conn, node.getNodeid()) >= 0;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                item.getChildren().clear();
                reloadCurrent();
                popSuccessful();
            }

        };
        start(task, treeView);
    }

    protected void trimDescendantsOrders(TreeItem<DataNode> item, boolean allDescendants) {
        if (item == null) {
            popError(message("SelectToHandle"));
            return;
        }
        DataNode node = item.getValue();
        if (node == null) {
            popError(message("SelectToHandle"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private int count;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    count = nodeTable.trimDescedentsOrders(this, conn, node, allDescendants, 0);
                } catch (Exception e) {
//                    error = e.toString();
//                    return false;
                }
                return count >= 0;
            }

            @Override
            protected void whenSucceeded() {
                if (count > 0) {
                    refreshItem(item);
                    reloadCurrent();
                }
                popSuccessful();
            }

        };
        start(task, treeView);
    }

    public void deleteNodes(TreeItem<DataNode> item) {
        DataTreeDeleteController.open(this, item != null ? item.getValue() : null);
    }

    protected void renameNode(TreeItem<DataNode> item) {
        if (item == null) {
            popError(message("SelectToHandle"));
            return;
        }
        DataNode nodeValue = item.getValue();
        if (nodeValue == null || isRoot(nodeValue)) {
            popError(message("SelectToHandle"));
            return;
        }
        String chainName = chainName(item);
        String name = PopTools.askValue(getBaseTitle(), chainName,
                message("ChangeNodeTitle"), nodeValue.getTitle() + "m");
        if (name == null || name.isBlank()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private DataNode updatedNode;

            @Override
            protected boolean handle() {
                nodeValue.setTitle(name);
                updatedNode = nodeTable.updateData(nodeValue);
                return updatedNode != null;
            }

            @Override
            protected void whenSucceeded() {
                item.setValue(updatedNode);
                treeView.refresh();
                checkCurrent(updatedNode);
                popSuccessful();
            }
        };
        start(task, treeView);
    }

    protected void reorderNode(TreeItem<DataNode> item) {
        if (item == null) {
            popError(message("SelectToHandle"));
            return;
        }
        DataNode nodeValue = item.getValue();
        if (nodeValue == null || isRoot(nodeValue)) {
            popError(message("SelectToHandle"));
            return;
        }
        float fvalue;
        try {
            String value = PopTools.askValue(getBaseTitle(),
                    chainName(item) + "\n" + message("NodeOrderComments"),
                    message("ChangeNodeOrder"),
                    nodeValue.getOrderNumber() + "");
            fvalue = Float.parseFloat(value);
        } catch (Exception e) {
            popError(message("InvalidValue"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private DataNode updatedNode;

            @Override
            protected boolean handle() {
                nodeValue.setOrderNumber(fvalue);
                updatedNode = nodeTable.updateData(nodeValue);
                return updatedNode != null;
            }

            @Override
            protected void whenSucceeded() {
                reorderChildlren(item.getParent());
                checkCurrent(updatedNode);
                popSuccessful();
            }
        };
        start(task, treeView);
    }

    protected void copyNodes(TreeItem<DataNode> item) {
        DataTreeCopyController.open(this, item != null ? item.getValue() : null);
    }

    protected void moveNodes(TreeItem<DataNode> item) {
        DataTreeMoveController.open(this, item != null ? item.getValue() : null);
    }

    protected void exportNode(TreeItem<DataNode> item) {
        DataTreeExportController exportController
                = (DataTreeExportController) openStage(Fxmls.DataTreeExportFxml);
        exportController.setParamters(this, item);
    }

    /*
        action
     */
    @FXML
    protected void importAction(TreeItem<DataNode> item) {
        DataTreeImportController importController
                = (DataTreeImportController) openStage(Fxmls.DataTreeImportFxml);
        importController.setParamters(this, item);
    }

    @FXML
    protected void importExamples(TreeItem<DataNode> item) {
        DataTreeImportController importController
                = (DataTreeImportController) openStage(Fxmls.DataTreeImportFxml);
        importController.importExamples(this, item);
    }

    protected void manufactureData() {
        String tname = nodeTable.getTableName();
        DataTable dataTable = DataInternalTable.isInternalTable(tname)
                ? new DataInternalTable() : new DataTable();
        dataTable.setDataName(nodeTable.getTreeName()).setSheet(tname);
        Data2DManufactureController.openDef(dataTable);
    }

    @FXML
    @Override
    public void cancelAction() {

    }

    @FXML
    protected void moveAction() {
//        InfoTreeNodesMoveController.oneOpen(this);
    }

    /*
        static methods
     */
    public static DataTreeController open(BaseController pController, boolean replaceScene, BaseNodeTable table) {
        try {
            if (table == null) {
                return null;
            }
            DataTreeController controller;
            if ((replaceScene || AppVariables.closeCurrentWhenOpenTool) && pController != null) {
                controller = (DataTreeController) pController.loadScene(Fxmls.DataTreeFxml);
            } else {
                controller = (DataTreeController) WindowTools.openStage(Fxmls.DataTreeFxml);
            }
            controller.requestMouse();
            controller.initDataTree(table);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DataTreeController open(BaseNodeTable table, DataNode node) {
        try {
            DataTreeController controller = (DataTreeController) WindowTools.openStage(Fxmls.DataTreeFxml);
            controller.initDataTree(table, node);
            controller.setAlwaysOnTop();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DataTreeController textTree(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeText());
    }

    public static DataTreeController htmlTree(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeHtml());
    }

    public static DataTreeController webFavorite(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeWebFavorite());
    }

    public static DataTreeController sql(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeSQL());
    }

    public static DataTreeController mathFunction(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeMathFunction());
    }

    public static DataTreeController imageScope(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeImageScope());
    }

    public static DataTreeController jShell(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeJShell());
    }

    public static DataTreeController jexl(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeJEXL());
    }

    public static DataTreeController javascript(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeJavaScript());
    }

    public static DataTreeController rowFilter(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeRowFilter());
    }

    public static DataTreeController data2DDefinition(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeData2DDefinition());
    }

}
