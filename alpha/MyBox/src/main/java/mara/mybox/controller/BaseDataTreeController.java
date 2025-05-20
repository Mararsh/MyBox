package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.DataNode.SelectionType;
import mara.mybox.db.table.BaseNodeTable;
import static mara.mybox.db.table.BaseNodeTable.RootID;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.TextClipboardTools;
import static mara.mybox.fxml.style.NodeStyleTools.attributeTextStyle;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
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
    protected DataNode rootNode, currentNode, sourceNode;
    protected SelectionType selectionType = SelectionType.None;
    protected final SimpleBooleanProperty loadedNotify;
    public boolean checkEmptyTree = true, testing = false;

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
    protected ControlDataTreeNodeView viewController;

    public BaseDataTreeController() {
        loadedNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            if (viewController != null) {
                leftPaneControl = viewController.leftPaneControl;
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initDataTree(BaseNodeTable table, DataNode node, boolean checkEmpty) {
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

            checkEmptyTree = checkEmpty;

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
                viewController.setParameters(this, nodeTable);
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

    public void initDataTree(BaseNodeTable table, DataNode node) {
        initDataTree(table, node, true);
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
            if (isSettingValues) {
                return;
            }
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

    public void whenTreeEmpty() {
    }

    public void notifyLoaded() {
        loadedNotify.set(!loadedNotify.get());
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
    public void viewNode(DataNode node) {
        if (viewController == null || node == null) {
            return;
        }
        viewController.loadNode(node.getNodeid());
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
        nodeTable.executeNode(this, node);
    }

    public void popNode(DataNode node) {
        nodeTable.popNode(this, node);
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

    public void importedNode(DataNode node) {
        refreshNode(node);
    }

    protected void reloadView(DataNode node) {
        if (viewController == null || node == null) {
            return;
        }
        if (viewController.viewNode != null
                && viewController.viewNode.equals(node)) {
            viewNode(node);
        }
    }

    public void locateNode(DataNode node) {
        if (node == null) {
            return;
        }
        DataTreeController.open(nodeTable, node);
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

    public void query() {
        DataTreeQueryController.open(this);
    }

    public void queryDescendants(DataNode node) {
        if (node == null) {
            return;
        }
        DataTreeQueryDescendantsController.open(this, node);
    }

    /*
        action
     */
    @FXML
    @Override
    public void refreshAction() {
        loadTree();
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

        menu = new MenuItem(message("Query") + " ...", StyleTools.getIconImageView("iconQuery.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            query();
        });
        items.add(menu);

        menu = new MenuItem(message("QueryDescendants"), StyleTools.getIconImageView("iconQuery.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            queryDescendants(node);
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

    /*
        events
     */
    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (super.keyEventsFilter(event)) {
            return true;
        }
        if (viewController != null) {
            if (viewController.keyEventsFilter(event)) {
                return true;
            }
        }
        return false;
    }

}
