package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.DataInternalTable;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.BaseNodeTable;
import static mara.mybox.db.table.BaseNodeTable.RootID;
import mara.mybox.db.table.BaseTableTools;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.db.table.TableNodeWebFavorite;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2025-4-26
 * @License Apache License Version 2.0
 */
public class BaseDataTreeController extends BaseFileController {

    protected BaseNodeTable nodeTable;
    protected TableDataTag tagTable;
    protected TableDataNodeTag nodeTagsTable;
    protected String dataName;
    protected DataNode rootNode, currentNode;

    @FXML
    protected ToggleGroup formatGroup;
    @FXML
    protected RadioButton treeRadio, tableRaido;
    @FXML
    protected VBox dataBox, treeBox, tableBox;
    @FXML
    protected ControlTreeView treeController;
    @FXML
    protected ControlTreeTable tableController;
    @FXML
    protected ControlWebView viewController;

    public void initDataTree(BaseNodeTable table, DataNode node) {
        try {
            if (table == null) {
                return;
            }

            nodeTable = table;
            tagTable = new TableDataTag(nodeTable);
            nodeTagsTable = new TableDataNodeTag(nodeTable);
            dataName = nodeTable.getTableName();
            baseName = baseName + "_" + dataName;
            baseTitle = nodeTable.getTreeName();
            setTitle(baseTitle);

            if (viewController != null) {
                viewController.setParent(this);
                viewController.initStyle = HtmlStyles.styleValue("Table");
            }

            currentNode = node;

            formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    loadTree();
                }
            });
            loadTree();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadTree(DataNode node) {
        currentNode = node;
        loadTree();
    }

    public void loadTree() {
        try {
            dataBox.getChildren().clear();
            if (treeRadio.isSelected()) {
                dataBox.getChildren().add(treeBox);
                treeController.setParameters(this);
            } else {
                dataBox.getChildren().add(tableBox);
                tableController.setParameters(this);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        values
     */
    public String title(DataNode node) {
        return node == null ? null : node.getTitle();
    }

    public String value(DataNode node) {
        return nodeTable.valuesString(node);
    }

    public boolean validNode(DataNode node) {
        return node != null;
    }

    public String label(DataNode node) {
        if (node == null) {
            return "";
        }
        return node.getHierarchyNumber() + " " + title(node);
    }

    public String chainName(DataNode node) {
        if (node == null) {
            return null;
        }
        String chainName = "";  // #######
//        List<TreeItem<DataNode>> ancestors = ancestorItems(item);
//        if (ancestors != null) {
//            for (TreeItem<DataNode> a : ancestors) {
//                chainName += title(a.getValue()) + TitleSeparater;
//            }
//        }
//        chainName += title(item.getValue());
        return chainName;
    }

    public void setHierarchyNumber(DataNode node, String hierarchyNumber) {
        if (node != null) {
            node.setHierarchyNumber(hierarchyNumber);
        }
    }

    public boolean equalNode(DataNode node1, DataNode node2) {
        if (node1 == null || node2 == null) {
            return false;
        }
        return node1.getNodeid() == node2.getNodeid();
    }

    public boolean isRoot(DataNode node) {
        if (node == null) {
            return false;
        }
        return node.getNodeid() == RootID;
    }

    public boolean isLeaf(DataNode node) {
        if (node == null) {
            return false;
        }
        return false;
    }

    public DataNode selectedNode() {
        return rootNode;
    }

    public DataNode parentNode(DataNode node) {
        return node.getParentNode();
    }

    /*
        events
     */
    public void itemClicked(Event event, DataNode node) {
        if (node == null) {
            return;
        }
        loadCurrent(node);
//        clicked(event, UserConfig.getString(baseName + "WhenDoubleClickNode", "PopNode"), node);
    }

    public void doubleClicked(Event event, DataNode node) {
        if (node == null) {
            return;
        }
        popNode(node);
//        clicked(event, UserConfig.getString(baseName + "WhenDoubleClickNode", "PopMenu"), node);
    }

    public void rightClicked(Event event, DataNode node) {
        if (node == null) {
            return;
        }
        popNode(node);
//        clicked(event, UserConfig.getString(baseName + "WhenRightClickNode", "PopMenu"), node);
    }

    public void clicked(Event event, String clickAction, DataNode node) {
        if (node == null || clickAction == null) {
            return;
        }
        switch (clickAction) {
            case "PopMenu":
                popMenu(event, node);
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
            default:
                break;
        }
    }


    /*
        operations
     */
    protected void nullCurrent() {
        currentNode = null;
        if (editButton != null) {
            editButton.setVisible(false);
        }
        if (goButton != null) {
            goButton.setVisible(false);
        }
        if (viewController != null) {
            viewController.loadContent("");
        }
    }

    public void loadCurrent(DataNode node) {
        nullCurrent();
        if (viewController == null || node == null) {
            return;
        }
        viewController.popInformation(message("Loading") + ": " + node.shortDescription());
        FxTask loadTask = new FxSingletonTask<Void>(this) {
            private String html;
            private DataNode stonedNode;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    stonedNode = nodeTable.query(conn, node.getNodeid());
                    if (stonedNode == null) {
                        return false;
                    }
                    html = nodeTable.valuesHtml(this, conn, controller, stonedNode,
                            node.getHierarchyNumber(), 4);
                    return html != null && !html.isBlank();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                viewController.loadContent(html);
                currentNode = stonedNode;
                if (editButton != null) {
                    editButton.setVisible(true);
                }
                if (goButton != null) {
                    goButton.setVisible(nodeTable.isNodeExecutable(stonedNode));
                }
            }

            @Override
            protected void whenFailed() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                viewController.closePopup();
            }

        };
        start(loadTask, rightPane);
    }

    public void addChild(DataNode node) {
        if (node == null) {
            popError(message("SelectToHandle"));
            return;
        }
        DataTreeNodeEditorController.addNode(this, node);
    }

    public void editNode(DataNode node) {
        if (node == null) {
            popError(message("SelectToHandle"));
            return;
        }
        DataTreeNodeEditorController.editNode(this, node);
    }

    public void executeNode(DataNode node) {
        if (node == null) {
            popError(message("SelectToHandle"));
            return;
        }
        if (nodeTable instanceof TableNodeWebFavorite) {
            FxTask exTask = new FxTask<Void>(this) {
                private String address;

                @Override
                protected boolean handle() {
                    try (Connection conn = DerbyBase.getConnection()) {
                        DataNode savedNode = nodeTable.query(conn, node.getNodeid());
                        if (savedNode == null) {
                            return false;
                        }
                        address = savedNode.getStringValue("address");
                        return address != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    WebBrowserController.openAddress(address, true);
                }
            };
            start(exTask, false);
        } else {
            DataTreeNodeEditorController.executeNode(this, node);
        }
    }

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
                reloadCurrent(stonedNode);
                popSuccessful();
            }
        };
        start(task);
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
                reorderChildlren(parentNode(stonedNode));
                loadCurrent(stonedNode);
                popSuccessful();
            }
        };
        start(task);
    }

    public void reorderChildlren(DataNode node) {
        if (node == null) {
            popError(message("SelectToHandle"));
            return;
        }
        // ###########
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
//                    error = e.toString();
//                    return false;
                }
                return count >= 0;
            }

            @Override
            protected void whenSucceeded() {
                if (count > 0) {
                    reloadCurrent(node);
                }
                popSuccessful();
            }

        };
        start(task);
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
                try (Connection conn = DerbyBase.getConnection()) {
                    return nodeTable.deleteDecentants(conn, node.getNodeid()) >= 0;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                reloadCurrent();
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
                    loadTree();
                } else {
                    reloadCurrent(node);
                }
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

    public void deleteNodes(DataNode node) {
        DataTreeDeleteController.open(this, node);
    }

    public void popNode(DataNode node) {
        if (node == null) {
            return;
        }
        FxTask popTask = new FxSingletonTask<Void>(this) {
            private String html;
            private DataNode savedNode;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    savedNode = nodeTable.query(conn, node.getNodeid());
                    if (savedNode == null) {
                        return false;
                    }
                    html = nodeTable.valuesHtml(this, conn, controller, savedNode,
                            node.getHierarchyNumber(), 4);
                    return html != null && !html.isBlank();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                HtmlTableController.open(null, html);
            }

        };
        start(popTask, false);
    }

    public void refreshNode(DataNode node) {
        if (node == null) {
            return;
        }
    }

    protected void reloadCurrent() {
        loadCurrent(currentNode);
    }

    protected void reloadCurrent(DataNode node) {
        if (currentNode != null && currentNode.equals(node)) {
            loadCurrent(node);
        }
    }

    public boolean focusNode(DataNode node) {
        return true;
    }

    public void nodeSaved(DataNode parent, DataNode node) {
        try {
            reloadCurrent(node);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void importNode(DataNode node) {
        DataTreeImportController importController
                = (DataTreeImportController) childStage(Fxmls.DataTreeImportFxml);
        importController.setParamters(this, node);
    }

    protected DataTreeImportController importExamples(DataNode node) {
        DataTreeImportController importController
                = (DataTreeImportController) childStage(Fxmls.DataTreeImportFxml);
        importController.importExamples(this, node);
        return importController;
    }

    protected DataTreeExportController exportNode(DataNode node) {
        DataTreeExportController exportController
                = (DataTreeExportController) childStage(Fxmls.DataTreeExportFxml);
        exportController.setParamters(this, node);
        return exportController;
    }

    protected void manufactureData() {
        String tname = nodeTable.getTableName();
        DataTable dataTable = BaseTableTools.isInternalTable(tname)
                ? new DataInternalTable() : new DataTable();
        dataTable.setDataName(nodeTable.getTreeName()).setSheet(tname);
        Data2DManufactureController.openDef(dataTable);
    }

    public void locate() {
        DataTreeLocateController.open(this);
    }

    /*
        action
     */
    @FXML
    public void clearTree() {

    }

    @FXML
    @Override
    public void refreshAction() {
        loadTree();
    }

    @FXML
    public void queryAction() {

    }

    @FXML
    @Override
    public boolean popAction() {
        popNode(selectedNode());
        return true;
    }

    @FXML
    public void editAction() {
        if (currentNode == null) {
            DataNode node = selectedNode();
            if (node == null) {
                return;
            }
            currentNode = node;
        }
        DataTreeNodeEditorController.editNode(this, currentNode);
    }

    @FXML
    @Override
    public void goAction() {
        if (currentNode == null) {
            DataNode node = selectedNode();
            if (node == null) {
                return;
            }
            currentNode = node;
        }
        executeNode(currentNode);
    }

    @FXML
    public void aboutTreeInformation() {
        openHtml(HelpTools.aboutTreeInformation());
    }

    @FXML
    @Override
    public void cancelAction() {
        closeStage();
    }

    @FXML
    public void manageAction() {
        DataTreeController.open(null, false, nodeTable);
        setIconified(true);
    }

    /*
        menu
     */
    @Override
    public List<MenuItem> viewMenuItems(Event fevent) {
        return viewMenuItems(fevent, null);
    }

    public List<MenuItem> viewMenuItems(Event fevent, DataNode inNode) {
        DataNode node = inNode != null ? inNode : selectedNode();
        if (node == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(message("PopNode"), StyleTools.getIconImageView("iconPop.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            popNode(node);
        });
        items.add(menu);

        menu = new MenuItem(message("CopyTitle"), StyleTools.getIconImageView("iconCopySystem.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            TextClipboardTools.copyToSystemClipboard(this, title(node));
        });
        items.add(menu);

        menu = new MenuItem(message("CopyValue"), StyleTools.getIconImageView("iconCopySystem.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            TextClipboardTools.copyToSystemClipboard(this, value(node));
        });
        items.add(menu);

        menu = new MenuItem(message("Locate"), StyleTools.getIconImageView("iconTarget.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            locate();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

//        if (!item.isLeaf()) {
//            items.addAll(foldMenuItems(item));
//
//            items.add(new SeparatorMenuItem());
//        }
        menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            refreshAction();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("AboutTreeInformation"), StyleTools.getIconImageView("iconClaw.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            openHtml(HelpTools.aboutTreeInformation());
        });
        items.add(menu);

        return items;
    }

    @Override
    public List<MenuItem> dataMenuItems(Event fevent) {
        return dataMenuItems(fevent, null);
    }

    public List<MenuItem> dataMenuItems(Event fevent, DataNode inNode) {
        DataNode node = inNode != null ? inNode : selectedNode();
        if (node == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(StringTools.menuPrefix(label(node)));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

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
        return operationsMenuItems(fevent, null);
    }

    public List<MenuItem> operationsMenuItems(Event fevent, DataNode inNode) {
        DataNode node = inNode != null ? inNode : selectedNode();
        if (node == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();

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

    public void popMenu(Event event, DataNode node) {
        if (node == null) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(StringTools.menuPrefix(label(node)));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        Menu dataMenu = new Menu(message("Data"), StyleTools.getIconImageView("iconData.png"));
        dataMenu.getItems().addAll(dataMenuItems(event, node));
        items.add(dataMenu);

        Menu treeMenu = new Menu(message("View"), StyleTools.getIconImageView("iconView.png"));
        treeMenu.getItems().addAll(viewMenuItems(event, node));
        items.add(treeMenu);

        items.add(new SeparatorMenuItem());

        items.addAll(operationsMenuItems(event, node));

        popNodeMenu(treeController.treeView, items);
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

}
