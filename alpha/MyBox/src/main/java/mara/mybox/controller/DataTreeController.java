package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
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
