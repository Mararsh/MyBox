package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.db.table.TableNodeDataColumn;
import mara.mybox.db.table.TableNodeHtml;
import mara.mybox.db.table.TableNodeImageScope;
import mara.mybox.db.table.TableNodeJEXL;
import mara.mybox.db.table.TableNodeJShell;
import mara.mybox.db.table.TableNodeJavaScript;
import mara.mybox.db.table.TableNodeMathFunction;
import mara.mybox.db.table.TableNodeRowExpression;
import mara.mybox.db.table.TableNodeSQL;
import mara.mybox.db.table.TableNodeText;
import mara.mybox.db.table.TableNodeWebFavorite;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import static mara.mybox.fxml.style.NodeStyleTools.attributeTextStyle;
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
public class DataTreeController extends BaseDataTreeController {

    @Override
    public void whenTreeEmpty() {
        if (AppVariables.isTesting) {
            return;
        }
        File file = nodeTable.exampleFile();
        if (file != null && PopTools.askSure(getTitle(), message("ImportExamples") + ": " + baseTitle)) {
            importExamples(null);
        }
    }

    /*
        operations
     */
    protected void renameNode(DataNode node) {
        if (node == null || isRoot(node)) {
            popError(message("SelectToHandle"));
            return;
        }
        String chainName = chainName(node);
        String name = PopTools.askValue(getBaseTitle(), chainName,
                message("ChangeNodeTitle"), title(node) + "m");
        if (name == null || name.isBlank()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private DataNode stonedNode;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    stonedNode = nodeTable.query(conn, node.getNodeid());
                    if (stonedNode == null) {
                        return false;
                    }
                    stonedNode.setUpdateTime(new Date());
                    stonedNode.setTitle(name);
                    stonedNode = nodeTable.updateData(conn, stonedNode);
                    if (stonedNode == null) {
                        return false;
                    }
                    conn.commit();
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                stonedNode.setHierarchyNumber(node.getHierarchyNumber());
                viewNode(stonedNode);
                popSuccessful();
            }
        };
        start(task);
    }

    protected void copyNodes(DataNode node) {
        DataTreeCopyController.open(this, node);
    }

    protected void moveNodes(DataNode node) {
        DataTreeMoveController.open(this, node);
    }

    protected void reorderNode(DataNode node) {
        if (node == null || isRoot(node)) {
            popError(message("SelectToHandle"));
            return;
        }
        float fvalue;
        try {
            String value = PopTools.askValue(getBaseTitle(),
                    chainName(node) + "\n" + message("NodeOrderComments"),
                    message("ChangeNodeOrder"),
                    node.getOrderNumber() + "");
            fvalue = Float.parseFloat(value);
        } catch (Exception e) {
            popError(message("InvalidValue"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private DataNode stonedNode;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    stonedNode = nodeTable.query(conn, node.getNodeid());
                    if (stonedNode == null) {
                        return false;
                    }
                    stonedNode.setUpdateTime(new Date());
                    stonedNode.setOrderNumber(fvalue);
                    stonedNode = nodeTable.updateData(conn, stonedNode);
                    return stonedNode != null;
                } catch (Exception e) {
                    error = e.toString();
                    MyBoxLog.error(e);
                    return false;
                }

            }

            @Override
            protected void whenSucceeded() {
                refreshNode(parentNode(stonedNode));
                reloadView(stonedNode);
                popSuccessful();
            }
        };
        start(task);
    }

    protected void trimDescendantsOrders(DataNode node, boolean allDescendants) {
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
                    error = e.toString();
//                    return false;
                }
                return count >= 0;
            }

            @Override
            protected void whenSucceeded() {
                if (count > 0) {
                    refreshNode(node);
                }
                popSuccessful();
            }

        };
        start(task);
    }

    public void deleteNodes(DataNode node) {
        DataTreeDeleteController.open(this, node);
    }

    protected void deleteDescendants(DataNode node) {
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
            String chainName = chainName(node);
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
                return nodeTable.deleteDecentants(node.getNodeid()) >= 0;
            }

            @Override
            protected void whenSucceeded() {
                refreshNode(node);
                popSuccessful();
            }

        };
        start(task);
    }

    protected void deleteNodeAndDescendants(DataNode node) {
        if (node == null) {
            popError(message("SelectToHandle"));
            return;
        }
        boolean isRoot = isRoot(node);
        if (!isLeaf(node)) {
            if (isRoot) {
                if (!PopTools.askSure(getTitle(), message("DeleteNodeAndDescendants"), message("SureDeleteAll"))) {
                    return;
                }
            } else {
                String chainName = chainName(node);
                if (!PopTools.askSure(getTitle(), chainName, message("DeleteNodeAndDescendants"))) {
                    return;
                }
            }
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                return nodeTable.deleteNode(node.getNodeid()) >= 0;
            }

            @Override
            protected void whenSucceeded() {
                if (isRoot) {
                    loadTree();
                } else {
                    refreshNode(node);
                }
                popSuccessful();
            }

        };
        start(task);
    }

    protected DataTreeImportController importExamples(DataNode node) {
        DataTreeImportController importController
                = (DataTreeImportController) childStage(Fxmls.DataTreeImportFxml);
        importController.importExamples(this, node);
        return importController;
    }

    protected DataTreeExportController exportNode(DataNode node) {
        return DataTreeExportController.open(this, node);
    }

    protected void importNode(DataNode node) {
        DataTreeImportController importController
                = (DataTreeImportController) childStage(Fxmls.DataTreeImportFxml);
        importController.setParamters(this, node);
    }

    protected void manufactureData() {
        Data2DManufactureController.openDef(nodeTable.dataTable());
    }

    /*
        events
     */
    @Override
    public void doubleClicked(Event event, DataNode node) {
        clicked(event, UserConfig.getString(baseName + "WhenDoubleClickNode", "PopNode"), node);
    }

    @Override
    public void rightClicked(Event event, DataNode node) {
        clicked(event, UserConfig.getString(baseName + "WhenRightClickNode", "PopMenu"), node);
    }

    public void clicked(Event event, String clickAction, DataNode node) {
        if (clickAction == null) {
            return;
        }
        switch (clickAction) {
            case "PopMenu":
                showPopMenu(event, node);
                break;
            case "EditNode":
                editNode(node);
                break;
            case "PopNode":
                popNode(node);
                break;
            case "ExecuteNode":
                executeNode(node);
                break;
            case "UnfoldNode":
                unfoldNode(node);
                break;
            default:
                break;
        }
    }

    /*
        menu
     */
    @Override
    public List<MenuItem> popMenu(Event event, DataNode inNode) {
        DataNode node = inNode != null ? inNode : rootNode;
        if (node == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(StringTools.menuPrefix(label(node)));
        menu.setStyle(attributeTextStyle());
        items.add(menu);
        items.add(new SeparatorMenuItem());

        Menu dataMenu = new Menu(message("Data"), StyleTools.getIconImageView("iconData.png"));
        dataMenu.getItems().addAll(dataMenuItems(event, node, false));
        items.add(dataMenu);

        Menu treeMenu = new Menu(message("View"), StyleTools.getIconImageView("iconView.png"));
        treeMenu.getItems().addAll(viewMenuItems(event, node, false));
        items.add(treeMenu);

        items.add(new SeparatorMenuItem());

        items.addAll(operationsMenuItems(event, node, false));

        return items;
    }

    @Override
    public List<MenuItem> dataMenuItems(Event fevent) {
        return dataMenuItems(fevent, null, true);
    }

    public List<MenuItem> dataMenuItems(Event fevent, DataNode inNode, boolean withTitle) {
        DataNode node = inNode != null ? inNode : selectedNode();
        if (node == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;
        if (withTitle) {
            menu = new MenuItem(StringTools.menuPrefix(label(node)));
            menu.setStyle(attributeTextStyle());
            items.add(menu);
            items.add(new SeparatorMenuItem());
        }

        menu = new MenuItem(message("Tags"), StyleTools.getIconImageView("iconTag.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            DataTreeTagsController.manage(this);
        });
        items.add(menu);

        menu = new MenuItem(message("Examples"), StyleTools.getIconImageView("iconExamples.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            importExamples(node);
        });
        items.add(menu);

        menu = new MenuItem(message("Export"), StyleTools.getIconImageView("iconExport.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            exportNode(node);
        });
        items.add(menu);

        menu = new MenuItem(message("Import"), StyleTools.getIconImageView("iconImport.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            importNode(node);
        });
        items.add(menu);

        menu = new MenuItem(message("DataManufacture"), StyleTools.getIconImageView("iconDatabase.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            manufactureData();
        });
        items.add(menu);

        return items;
    }

    @Override
    public List<MenuItem> operationsMenuItems(Event fevent) {
        return operationsMenuItems(fevent, null, true);
    }

    public List<MenuItem> operationsMenuItems(Event fevent, DataNode inNode, boolean withTitle) {
        DataNode node = inNode != null ? inNode : selectedNode();
        if (node == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;
        if (withTitle) {
            menu = new MenuItem(StringTools.menuPrefix(label(node)));
            menu.setStyle(attributeTextStyle());
            items.add(menu);
            items.add(new SeparatorMenuItem());
        }

        items.addAll(updateMenuItems(fevent, node));

        items.add(new SeparatorMenuItem());

        items.add(doubleClickMenu(fevent, node));
        items.add(rightClickMenu(fevent, node));

        return items;
    }

    public List<MenuItem> updateMenuItems(Event fevent, DataNode inNode) {
        DataNode node = inNode != null ? inNode : selectedNode();
        if (node == null) {
            return null;
        }
        boolean isRoot = isRoot(node);
        boolean isLeaf = isLeaf(node);

        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(message("EditNode"), StyleTools.getIconImageView("iconEdit.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            editNode(node);
        });
        items.add(menu);

        if (nodeTable.isNodeExecutable()) {
            menu = new MenuItem(message("ExecuteNode"), StyleTools.getIconImageView("iconGo.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                executeNode(node);
            });
            items.add(menu);
        }

        menu = new MenuItem(message("AddChildNode"), StyleTools.getIconImageView("iconAdd.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            addChild(node);
        });
        items.add(menu);

        if (!isRoot) {
            menu = new MenuItem(message("ChangeNodeTitle"), StyleTools.getIconImageView("iconInput.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                renameNode(node);
            });
            items.add(menu);

        }

        MenuItem orderMenuItem = new MenuItem(message("ChangeNodeOrder"), StyleTools.getIconImageView("iconClean.png"));
        orderMenuItem.setOnAction((ActionEvent menuItemEvent) -> {
            reorderNode(node);
        });

        if (isLeaf) {
            if (!isRoot) {
                items.add(orderMenuItem);
            }
        } else {
            Menu orderMenu = new Menu(message("OrderNumber"), StyleTools.getIconImageView("iconClean.png"));

            if (!isRoot) {
                orderMenu.getItems().add(orderMenuItem);
            }

            menu = new MenuItem(message("TrimDescendantsOrders"), StyleTools.getIconImageView("iconClean.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                trimDescendantsOrders(node, true);
            });
            orderMenu.getItems().add(menu);

            menu = new MenuItem(message("TrimChildrenOrders"), StyleTools.getIconImageView("iconClean.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                trimDescendantsOrders(node, false);
            });
            orderMenu.getItems().add(menu);

            items.add(orderMenu);
        }

        menu = new MenuItem(message("CopyNodes"), StyleTools.getIconImageView("iconCopy.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            copyNodes(node);
        });
        items.add(menu);

        menu = new MenuItem(message("MoveNodes"), StyleTools.getIconImageView("iconMove.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            moveNodes(node);
        });
        items.add(menu);

        Menu deleteMenu = new Menu(message("Delete"), StyleTools.getIconImageView("iconDelete.png"));

        if (isLeaf) {
            menu = new MenuItem(message("DeleteNode"), StyleTools.getIconImageView("iconDelete.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                deleteNodeAndDescendants(node);
            });
            deleteMenu.getItems().add(menu);

        } else {
            menu = new MenuItem(message("DeleteNodeAndDescendants"), StyleTools.getIconImageView("iconDelete.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                deleteNodeAndDescendants(node);
            });
            deleteMenu.getItems().add(menu);

            menu = new MenuItem(message("DeleteDescendants"), StyleTools.getIconImageView("iconDelete.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                deleteDescendants(node);
            });
            deleteMenu.getItems().add(menu);
        }

        menu = new MenuItem(message("DeleteNodes"), StyleTools.getIconImageView("iconDelete.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            deleteNodes(node);
        });
        deleteMenu.getItems().add(menu);

        items.add(deleteMenu);

        return items;
    }

    public Menu doubleClickMenu(Event fevent, DataNode inNode) {
        Menu clickMenu = new Menu(message("WhenDoubleClickNode"), StyleTools.getIconImageView("iconSelectAll.png"));
        clickMenu(fevent, inNode, clickMenu, "WhenDoubleClickNode", "EditNode");
        return clickMenu;
    }

    public Menu rightClickMenu(Event fevent, DataNode inNode) {
        Menu clickMenu = new Menu(message("WhenRightClickNode"), StyleTools.getIconImageView("iconSelectNone.png"));
        clickMenu(fevent, inNode, clickMenu, "WhenRightClickNode", "PopMenu");
        return clickMenu;
    }

    public Menu clickMenu(Event fevent, DataNode inNode, Menu menu, String key, String defaultAction) {
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

        RadioMenuItem unfoldNodeMenu = new RadioMenuItem(message("Unfold"), StyleTools.getIconImageView("iconPlus.png"));
        unfoldNodeMenu.setSelected("UnfoldNode".equals(currentClick));
        unfoldNodeMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + key, "UnfoldNode");
            }
        });
        unfoldNodeMenu.setToggleGroup(clickGroup);

        RadioMenuItem popNodeMenu = new RadioMenuItem(message("PopNode"), StyleTools.getIconImageView("iconPop.png"));
        popNodeMenu.setSelected("PopNode".equals(currentClick));
        popNodeMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + key, "PopNode");
            }
        });
        popNodeMenu.setToggleGroup(clickGroup);

        menu.getItems().addAll(editNodeMenu, unfoldNodeMenu, popNodeMenu);

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
            controller.initDataTree(table, null);
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

    public static DataTreeController rowExpression(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeRowExpression());
    }

    public static DataTreeController dataColumn(BaseController pController, boolean replaceScene) {
        return open(pController, replaceScene, new TableNodeDataColumn());
    }

}
