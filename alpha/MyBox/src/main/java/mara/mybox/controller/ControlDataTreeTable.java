package mara.mybox.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.util.Callback;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.cell.TableIDCell;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-4-25
 * @License Apache License Version 2.0
 */
public class ControlDataTreeTable extends BaseTablePagesController<DataNode> {

    protected BaseDataTreeController dataController;
    protected BaseNodeTable nodeTable;
    protected TableDataTag tagTable;
    protected TableDataNodeTag nodeTagsTable;
    protected String dataName;
    protected ControlWebView viewController;

    @FXML
    protected TableColumn<DataNode, String> hierarchyColumn, titleColumn;
    @FXML
    protected TableColumn<DataNode, Long> idColumn, childrenColumn;
    @FXML
    protected TableColumn<DataNode, Float> orderColumn;
    @FXML
    protected TableColumn<DataNode, Date> timeColumn;
    @FXML
    protected FlowPane namesPane;

    @Override
    public void initColumns() {
        try {

            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            titleColumn.setCellFactory(new Callback<TableColumn<DataNode, String>, TableCell<DataNode, String>>() {

                @Override
                public TableCell<DataNode, String> call(TableColumn<DataNode, String> param) {
                    try {
                        final Hyperlink link = new Hyperlink();
                        NodeStyleTools.setTooltip(link, new Tooltip(message("View")));

                        TableCell<DataNode, String> cell = new TableCell<DataNode, String>() {

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                setText(null);
                                if (empty || item == null) {
                                    setGraphic(null);
                                    return;
                                }
                                link.setText(StringTools.abbreviate(item, AppVariables.titleTrimSize));
                                link.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        dataController.viewNode(getTableRow().getItem());
                                    }
                                });
                                setGraphic(link);
                                if (isSourceNode(getTableRow().getItem())) {
                                    setStyle(NodeStyleTools.darkRedTextStyle());
                                } else {
                                    setStyle(null);
                                }
                            }

                        };

                        return cell;
                    } catch (Exception e) {
                        return null;
                    }
                }
            });

            hierarchyColumn.setCellValueFactory(new PropertyValueFactory<>("hierarchyNumber"));
            hierarchyColumn.setCellFactory(new Callback<TableColumn<DataNode, String>, TableCell<DataNode, String>>() {

                @Override
                public TableCell<DataNode, String> call(TableColumn<DataNode, String> param) {
                    try {
                        final Hyperlink link = new Hyperlink();
                        NodeStyleTools.setTooltip(link, new Tooltip(message("Unfold")));

                        TableCell<DataNode, String> cell = new TableCell<DataNode, String>() {

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                setText(null);
                                setGraphic(null);
                                if (empty || item == null) {
                                    return;
                                }
                                DataNode node = getTableRow().getItem();
                                if (node == null) {
                                    return;
                                }
                                if (node.getChildrenSize() > 0) {
                                    link.setText(item);
                                    link.setOnAction(new EventHandler<ActionEvent>() {
                                        @Override
                                        public void handle(ActionEvent event) {
                                            loadNode(node);
                                        }
                                    });
                                    setGraphic(link);
                                } else {
                                    setText(item);
                                }

                            }

                        };

                        return cell;
                    } catch (Exception e) {
                        return null;
                    }
                }
            });

            idColumn.setCellValueFactory(new PropertyValueFactory<>("nodeid"));
            idColumn.setCellFactory(new TableIDCell());

            childrenColumn.setCellValueFactory(new PropertyValueFactory<>("childrenSize"));
            childrenColumn.setCellFactory(new TableIDCell());

            orderColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));

            timeColumn.setCellValueFactory(new PropertyValueFactory<>("updateTime"));
            timeColumn.setCellFactory(new TableDateCell());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseDataTreeController controller) {
        try {
            dataController = controller;
            nodeTable = dataController.nodeTable;
            tagTable = dataController.tagTable;
            nodeTagsTable = dataController.nodeTagsTable;
            dataName = dataController.dataName;
            baseName = dataController.baseName;
            viewController = dataController.viewController;

            refreshStyle();

            loadNode(dataController.currentNode);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean isSourceNode(DataNode node) {
        return dataController != null && dataController.isSourceNode(node);
    }

    public void loadNode(DataNode node) {
        loadNode(node, true);
    }

    public void loadNode(DataNode node, boolean refreshChildren) {
        if (task != null) {
            task.cancel();
        }
        if (refreshChildren) {
            resetTable();
        }
        task = new FxSingletonTask<Void>(this) {

            private DataNode currentNode;

            @Override
            protected boolean handle() {
                currentNode = null;
                if (nodeTable == null) {
                    return true;
                }
                try (Connection conn = DerbyBase.getConnection()) {
                    currentNode = nodeTable.readChain(this, conn, node);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return currentNode != null;
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                if (currentNode != null) {
                    dataController.currentNode = currentNode;
                    if (currentNode.isRoot()) {
                        dataController.rootNode = currentNode.cloneAll();
                    }
                    if (refreshChildren) {
                        loadTableData();
                    } else {
                        writeNamesPane();
                    }
                }
            }

        };
        start(task);
    }

    @Override
    public List<DataNode> readPageData(FxTask currentTask, Connection conn) {
        if (nodeTable == null || dataController.currentNode == null) {
            return null;
        }
        List<DataNode> nodes = new ArrayList<>();
        String sql = "SELECT * FROM " + nodeTable.tableName
                + " WHERE parentid=? AND parentid<>nodeid "
                + " ORDER BY " + nodeTable.orderColumns
                + " OFFSET " + pagination.startRowOfCurrentPage
                + " ROWS FETCH NEXT " + pagination.pageSize + " ROWS ONLY";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, dataController.currentNode.getNodeid());
            try (ResultSet results = statement.executeQuery()) {
                String prefix = dataController.currentNode.getHierarchyNumber();
                if (prefix == null || prefix.isBlank()) {
                    prefix = "";
                } else {
                    prefix += ".";
                }
                long index = pagination.startRowOfCurrentPage;
                while (results != null && results.next()) {
                    if (currentTask == null || currentTask.isCancelled()) {
                        return null;
                    }
                    DataNode childNode = nodeTable.readData(results);
                    childNode.setIndex(index);
                    childNode.setHierarchyNumber(prefix + (++index));
                    childNode.setChildrenSize(nodeTable.childrenSize(conn, childNode.getNodeid()));
                    nodes.add(childNode);
                }
            } catch (Exception e) {
                if (currentTask != null) {
                    currentTask.setError(e.toString());
                } else {
                    MyBoxLog.error(e);
                }
            }
        } catch (Exception e) {
            if (currentTask != null) {
                currentTask.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
        }
        return nodes;
    }

    @Override
    public long readDataSize(FxTask currentTask, Connection conn) {
        if (nodeTable == null || dataController.currentNode == null) {
            return -1;
        }
        long size = nodeTable.childrenSize(conn, dataController.currentNode.getNodeid());
        dataSizeLoaded = true;
        return size;
    }

    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        writeNamesPane();
        dataController.viewNode(dataController.currentNode);
    }

    public void writeNamesPane() {
        try {
            namesPane.getChildren().clear();
            if (nodeTable == null || dataController.currentNode == null) {
                return;
            }
            for (DataNode node : dataController.currentNode.getChainNodes()) {
                Hyperlink viewLink = new Hyperlink(node.getTitle());
                NodeStyleTools.setTooltip(viewLink, new Tooltip(message("View")));
                viewLink.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        dataController.viewNode(node);
                    }
                });
                namesPane.getChildren().add(viewLink);

                Hyperlink unfoldLink = new Hyperlink(">");
                NodeStyleTools.setTooltip(unfoldLink, new Tooltip(message("Unfold")));
                unfoldLink.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        loadNode(node);
                    }
                });
                namesPane.getChildren().add(unfoldLink);

            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected long clearData(FxTask currentTask) {
        try {
            if (nodeTable == null || dataController.currentNode == null) {
                return -1;
            }
            return nodeTable.deleteDecentants(dataController.currentNode.getNodeid());
        } catch (Exception e) {
            if (currentTask != null) {
                currentTask.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return -2;
        }
    }

    public void resetTable() {
        try {
            isSettingValues = true;
            tableData.clear();
            isSettingValues = false;
            namesPane.getChildren().clear();
            paginationController.reset();
            tableChanged(false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public DataNode selectedNode() {
        DataNode node = tableView.getSelectionModel().getSelectedItem();
        return node != null ? node : dataController.currentNode;
    }

    @Override
    public void clicked(Event event) {
        dataController.leftClicked(event, selectedNode());
    }

    @Override
    public void doubleClicked(Event event) {
        dataController.doubleClicked(event, selectedNode());
    }

    @Override
    public void rightClicked(Event event) {
        dataController.rightClicked(event, selectedNode());
    }

    public boolean refreshNode(DataNode node) {
        if (nodeTable == null
                || dataController.currentNode == null
                || !dataController.currentNode.equals(node)) {
            return false;
        }
        loadNode(dataController.currentNode);
        return true;
    }

    public void nodeSaved(DataNode parent, DataNode node) {
        try {
            if (nodeTable == null || dataController.currentNode == null) {
                return;
            }

            for (DataNode anode : dataController.currentNode.getChainNodes()) {
                if (anode.equals(node)) {
                    loadNode(dataController.currentNode, false);
                    return;
                }
            }

            for (int i = 0; i < tableData.size(); i++) {
                DataNode tnode = tableData.get(i);
                if (tnode.equals(node)) {
                    loadTableData();
                    return;
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
