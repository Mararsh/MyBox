package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
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
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2025-4-25
 * @License Apache License Version 2.0
 */
public class ControlTreeTable extends BaseTablePagesController<DataNode> {

    protected BaseDataTreeController dataController;
    protected BaseNodeTable nodeTable;
    protected TableDataTag tagTable;
    protected TableDataNodeTag nodeTagsTable;
    protected String dataName;
    protected ControlWebView viewController;

    @FXML
    protected TableColumn<DataNode, String> rowColumn, hierarchyColumn, titleColumn;
    @FXML
    protected TableColumn<DataNode, Long> idColumn;
    @FXML
    protected TableColumn<DataNode, Float> orderColumn;
    @FXML
    protected TableColumn<DataNode, Date> timeColumn;
    @FXML
    protected FlowPane namesPane;

    @Override
    public void initColumns() {
        try {
            rowColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            rowColumn.setCellFactory(new Callback<TableColumn<DataNode, String>, TableCell<DataNode, String>>() {

                @Override
                public TableCell<DataNode, String> call(TableColumn<DataNode, String> param) {
                    try {
                        final Button rowButton = new Button("");
                        rowButton.setGraphic(StyleTools.getIconImageView("iconOperation.png"));

                        TableCell<DataNode, String> cell = new TableCell<DataNode, String>() {

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                setText(null);
                                if (empty || item == null) {
                                    setGraphic(null);
                                    return;
                                }
                                rowButton.setOnMouseEntered(new EventHandler<MouseEvent>() {
                                    @Override
                                    public void handle(MouseEvent event) {
                                        if (UserConfig.getBoolean("TreeTableRowMenuPopWhenMouseHovering", true)) {
                                            showRowMenu(event, getTableRow().getItem());
                                        }
                                    }
                                });
                                rowButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                    @Override
                                    public void handle(MouseEvent event) {
                                        showRowMenu(event, getTableRow().getItem());
                                    }
                                });
                                setGraphic(rowButton);
                            }

                        };

                        return cell;
                    } catch (Exception e) {
                        return null;
                    }
                }
            });

            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            titleColumn.setCellFactory(new Callback<TableColumn<DataNode, String>, TableCell<DataNode, String>>() {

                @Override
                public TableCell<DataNode, String> call(TableColumn<DataNode, String> param) {
                    try {
                        final Hyperlink link = new Hyperlink();

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
                                        loadChildren(getTableRow().getItem());
                                    }
                                });
                                setGraphic(link);
                            }

                        };

                        return cell;
                    } catch (Exception e) {
                        return null;
                    }
                }
            });

            hierarchyColumn.setCellValueFactory(new PropertyValueFactory<>("hierarchyNumber"));

            idColumn.setCellValueFactory(new PropertyValueFactory<>("nodeid"));
            idColumn.setCellFactory(new TableIDCell());

            orderColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));

            timeColumn.setCellValueFactory(new PropertyValueFactory<>("updateTime"));
            timeColumn.setCellFactory(new TableDateCell());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void showRowMenu(Event fevent, DataNode rowNode) {
        try {
            List<MenuItem> items = dataController.popMenu(fevent, rowNode);
            if (items == null || items.isEmpty()) {
                return;
            }

            items.add(new SeparatorMenuItem());

            CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            popItem.setSelected(UserConfig.getBoolean("TreeTableRowMenuPopWhenMouseHovering", true));
            popItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("TreeTableRowMenuPopWhenMouseHovering", popItem.isSelected());
                }
            });
            items.add(popItem);

            popEventMenu(fevent, items);
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

            loadChildren(dataController.currentNode);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadChildren(DataNode node) {
        if (task != null) {
            task.cancel();
        }
        resetTable();
        task = new FxSingletonTask<Void>(this) {

            private DataNode currentNode;

            @Override
            protected boolean handle() {
                currentNode = null;
                if (nodeTable == null) {
                    return true;
                }
                try (Connection conn = DerbyBase.getConnection()) {
                    currentNode = nodeTable.readAncestors(this, conn, node);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return currentNode != null;
            }

            @Override
            protected void whenSucceeded() {
                dataController.currentNode = currentNode;
                if (currentNode.isRoot()) {
                    dataController.rootNode = currentNode.cloneAll();
                }
                loadTableData();
            }

        };
        start(task);
    }

    public String queryCondition() {
        queryConditions = null;
        if (nodeTable != null && dataController.currentNode != null) {
            queryConditions = " parentid=" + dataController.currentNode.getNodeid()
                    + " AND parentid<>nodeid ";
        }
        return queryConditions;
    }

    @Override
    public List<DataNode> readPageData(FxTask currentTask, Connection conn) {
        try {
            if (nodeTable == null || dataController.currentNode == null) {
                return null;
            }
            String sql = "SELECT * FROM " + nodeTable.tableName
                    + " WHERE " + queryCondition()
                    + " ORDER BY " + nodeTable.orderColumns
                    + " OFFSET " + pagination.startRowOfCurrentPage
                    + " ROWS FETCH NEXT " + pagination.pageSize + " ROWS ONLY";
            List<DataNode> nodes = nodeTable.query(conn, sql);
            if (nodes != null) {
                String prefix = nodeTable.makeHierarchyNumber(conn, dataController.currentNode);
                if (prefix == null || prefix.isBlank()) {
                    prefix = "";
                } else {
                    prefix += ".";
                }
                long index = pagination.startRowOfCurrentPage;
                for (DataNode node : nodes) {
                    node.setHierarchyNumber(prefix + (++index));
                }
            }
            return nodes;
        } catch (Exception e) {
            if (currentTask != null) {
                currentTask.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    @Override
    public long readDataSize(FxTask currentTask, Connection conn) {
        try {
            if (nodeTable == null || dataController.currentNode == null) {
                return 0;
            }
            long size = nodeTable.conditionSize(conn, queryCondition());
            dataSizeLoaded = true;
            return size;
        } catch (Exception e) {
            if (currentTask != null) {
                currentTask.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return 0;
        }
    }

    @Override
    public void postLoadedTableData() {
        try {
            super.postLoadedTableData();
            namesPane.getChildren().clear();
            if (nodeTable == null || dataController.currentNode == null) {
                return;
            }
            List<DataNode> nodes = new ArrayList<>();
            nodes.add(dataController.currentNode);
            List<DataNode> ancestors = dataController.currentNode.getAncestors();
            if (ancestors != null) {
                nodes.addAll(ancestors);
            }
            for (DataNode node : nodes) {
                Hyperlink link = new Hyperlink(node.getTitle());
                link.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        loadChildren(node);
                    }
                });
                if (!namesPane.getChildren().isEmpty()) {
                    namesPane.getChildren().add(0, new Label(">"));
                }
                namesPane.getChildren().add(0, link);
            }
            dataController.loadCurrent(dataController.currentNode);
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
            return nodeTable.deleteCondition(queryCondition());
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
        return tableView.getSelectionModel().getSelectedItem();
    }

    @Override
    public void itemClicked(Event event) {
        dataController.leftClicked(event, selectedNode());
    }

    @Override
    public void itemDoubleClicked(Event event) {
        dataController.doubleClicked(event, selectedNode());
    }

}
