package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.DataInternalTable;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.DataNode.SelectionType;
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
import static mara.mybox.fxml.style.NodeStyleTools.attributeTextStyle;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

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
    protected DataNode rootNode, currentNode, viewNode, sourceNode;
    protected SelectionType selectionType = SelectionType.None;

    @FXML
    protected ToggleGroup formatGroup;
    @FXML
    protected RadioButton treeRadio, tableRadio;
    @FXML
    protected VBox dataBox, treeBox, tableBox;
    @FXML
    protected ControlDataTreeView treeController;
    @FXML
    protected ControlDataTreeTable tableController;
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
            baseTitle = initTitle();
            setTitle(baseTitle);

            treeController.setParameters(this);
            tableController.setParameters(this);

            if (selectionType == SelectionType.Multiple) {
                treeController.treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                treeController.treeView.setEditable(true);

                tableController.tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                tableController.tableView.setEditable(true);

            } else if (selectionType == SelectionType.Single) {
                treeController.treeView.getColumns().remove(treeController.selectColumn);
                treeController.treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                treeController.treeView.setEditable(false);

                tableController.tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                tableController.tableView.setEditable(true);

            } else {
                treeController.treeView.getColumns().remove(treeController.selectColumn);
                treeController.treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                treeController.treeView.setEditable(false);

                tableController.tableView.getColumns().remove(tableController.rowsSelectionColumn);
                tableController.tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                tableController.tableView.setEditable(false);
            }

            if (viewController != null) {
                viewController.setParent(this);
                viewController.initStyle = HtmlStyles.styleValue("Table");
            }

            formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    loadTree();
                }
            });

            setFormat(node);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setFormat(DataNode node) {
        task = new FxSingletonTask<Void>(this) {

            private long size = -1;

            @Override
            protected boolean handle() {
                if (nodeTable == null) {
                    return true;
                }
                try (Connection conn = DerbyBase.getConnection()) {
                    size = nodeTable.childrenSize(conn, RootID);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return size >= 0;
            }

            @Override
            protected void whenSucceeded() {
                isSettingValues = true;
                if (size > 100) {
                    tableRadio.setSelected(true);
                }
                isSettingValues = false;
                loadTree(node);
            }

        };
        start(task, thisPane);
    }

    public String initTitle() {
        return nodeTable.getTreeName();
    }

    public void loadTree(DataNode node) {
        currentNode = node;
        loadTree();
    }

    public void loadTree() {
        try {
            dataBox.getChildren().clear();
            treeController.resetTree();
            tableController.resetTable();
            if (treeRadio.isSelected()) {
                dataBox.getChildren().add(treeBox);
                treeController.loadTree(currentNode);
            } else {
                dataBox.getChildren().add(tableBox);
                tableController.loadNode(currentNode);
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
        String s = node.getHierarchyNumber();
        return (s != null ? s + " " : "") + title(node);
    }

    public String chainName(DataNode node) {
        if (node == null) {
            return null;
        }
        return node.getChainName();
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
        return node == null ? false : node.getChildrenSize() == 0;
    }

    public DataNode selectedNode() {
        DataNode node;
        if (treeRadio.isSelected()) {
            node = treeController.selectedNode();
        } else {
            node = tableController.selectedNode();
        }
        return node != null ? node
                : (currentNode != null ? currentNode : rootNode);
    }

    public List<DataNode> selectedNodes() {
        if (treeRadio.isSelected()) {
            return treeController.selectedNodes();
        } else {
            return tableController.selectedItems();
        }
    }

    public DataNode parentNode(DataNode node) {
        return node.getParentNode();
    }

    public boolean isSourceNode(DataNode node) {
        return equalNode(node, sourceNode);
    }

    public boolean equalOrDescendant(FxTask<Void> currentTask, Connection conn,
            DataNode targetNode, List<DataNode> sourceNodes) {
        if (sourceNodes == null || sourceNodes.isEmpty()) {
            displayError(message("SelectSourceNodes"));
            return false;
        }
        if (targetNode == null) {
            displayError(message("SelectTargetNode"));
            return false;
        }
        for (DataNode source : sourceNodes) {
            if (nodeTable.equalOrDescendant(currentTask, conn, targetNode, source)) {
                displayError(message("TreeTargetComments"));
                return false;
            }
        }
        return true;
    }

    /*
        events
     */
    public void leftClicked(Event event, DataNode node) {
        viewNode(node != null ? node : rootNode);
    }

    public void doubleClicked(Event event, DataNode node) {
        popNode(node);
    }

    public void rightClicked(Event event, DataNode node) {
        showPopMenu(event, node);
    }

    /*
        operations
     */
    protected void nullView() {
        viewNode = null;
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

    public void viewNode(DataNode node) {
        nullView();
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
                viewNode = stonedNode;
                if (editButton != null) {
                    editButton.setVisible(true);
                }
                if (goButton != null) {
                    goButton.setVisible(nodeTable.isNodeExecutable(viewNode));
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

    public void showNode(DataNode node) {
        if (viewController == null) {
            popNode(node);
        } else {
            viewNode(node);
        }
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
                viewNode(stonedNode);
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

    public void unfoldNode(DataNode node) {
        if (node == null) {
            popError(message("SelectToHandle"));
            return;
        }
        if (isLeaf(node)) {
            return;
        }
        if (treeRadio.isSelected()) {
            treeController.unfoldNode(node);
        } else {
            tableController.loadNode(node);
        }
    }

    public void refreshNode(DataNode node) {
        if (node == null) {
            return;
        }
        if (treeRadio.isSelected()) {
            treeController.refreshNode(node);
        } else {
            tableController.refreshNode(node);
        }
        reloadView(node);
    }

    protected void reloadView(DataNode node) {
        if (viewNode != null && viewNode.equals(node)) {
            viewNode(node);
        }
    }

    public boolean focusNode(DataNode node) {
        return true;
    }

    public void nodeSaved(DataNode parent, DataNode node) {
        try {
            if (treeRadio.isSelected()) {
                treeController.nodeSaved(parent, node);
            } else {
                tableController.nodeSaved(parent, node);
            }
            reloadView(node);
            popSaved();
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
        if (viewNode == null) {
            DataNode node = selectedNode();
            if (node == null) {
                return;
            }
            viewNode = node;
        }
        DataTreeNodeEditorController.editNode(this, viewNode);
    }

    @FXML
    @Override
    public void goAction() {
        if (viewNode == null) {
            DataNode node = selectedNode();
            if (node == null) {
                return;
            }
            viewNode = node;
        }
        executeNode(viewNode);
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
        return viewMenuItems(fevent, null, true);
    }

    public List<MenuItem> viewMenuItems(Event fevent, DataNode inNode, boolean withTitle) {
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

        menu = new MenuItem(message("PopNode"), StyleTools.getIconImageView("iconPop.png"));
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

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("Locate"), StyleTools.getIconImageView("iconTarget.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            locate();
        });
        items.add(menu);

        if (!isLeaf(node)) {
            if (treeRadio.isSelected()) {
                items.addAll(treeController.foldMenuItems());

            } else {
                menu = new MenuItem(message("Unfold"), StyleTools.getIconImageView("iconPlus.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    tableController.loadNode(node);
                });
                items.add(menu);
            }

        }
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

    public void showPopMenu(Event event, DataNode node) {
        List<MenuItem> items = popMenu(event, node);
        if (items == null) {
            return;
        }
        popEventMenu(event, items);
    }

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

        items.addAll(viewMenuItems(event, node, false));

        return items;
    }

}
