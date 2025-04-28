package mara.mybox.controller;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
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
import mara.mybox.fxml.cell.TableTextTrimCell;

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
    protected TableColumn<DataNode, Integer> dataRowColumn;
    @FXML
    protected TableColumn<DataNode, String> hierarchyColumn, titleColumn;
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

            hierarchyColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            titleColumn.setCellFactory(new TableTextTrimCell());

            idColumn.setCellValueFactory(new PropertyValueFactory<>("nodeid"));
            idColumn.setCellFactory(new TableIDCell());

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
                updateNamesPane();
                loadTableData();
            }

        };
        start(task);
    }

    public void updateNamesPane() {
        try {
            namesPane.getChildren().clear();
            if (nodeTable == null || dataController.currentNode == null) {
                return;
            }
            List<DataNode> ancestors = dataController.currentNode.getAncestors();
            if (ancestors == null) {
                return;
            }
            for (DataNode node : ancestors) {
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
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
            return nodeTable.query(conn, sql);
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
            tableData.clear();
            namesPane.getChildren().clear();
            paginationController.reset();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
