package mara.mybox.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.TmpTable;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.BaseNodeTable;
import static mara.mybox.db.table.BaseNodeTable.NodeFields;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-5-16
 * @License Apache License Version 2.0
 */
public class DataTreeQueryDescendantsController extends BaseTaskController {

    protected BaseDataTreeController dataController;
    protected BaseNodeTable nodeTable;
    protected String dataName, chainName, querySQL;
    protected DataTable treeTable;
    protected TmpTable results;
    protected DataNode node;
    protected PreparedStatement insert;
    protected TableData2D tableData2D;
    protected boolean all;

    @FXML
    protected Tab nodeTab;
    @FXML
    protected Label nameLabel;

    public void setParameters(BaseDataTreeController parent, DataNode inNode, boolean onlyChildren) {
        try {
            if (parent == null || inNode == null) {
                close();
                return;
            }
            dataController = parent;
            parentController = parent;
            nodeTable = dataController.nodeTable;
            dataName = nodeTable.getDataName();
            baseName = baseName + "_" + dataName;
            node = inNode;
            all = !onlyChildren;

            String name = all ? message("QueryDescendants") : message("QueryChildren");
            nodeTab.setText(name);

            baseTitle = nodeTable.getTreeName() + " - " + name;
            setTitle(baseTitle);

            startAction();

        } catch (Exception e) {
            showLogs(e.toString());
        }
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        try (Connection conn = DerbyBase.getConnection()) {
            treeTable = nodeTable.recordTable(conn);
            if (treeTable == null) {
                dataController.popError(message("InvalidParameters"));
                close();
                return false;
            }
            DataNode savedNode = nodeTable.readChain(currentTask, conn, node.getNodeid());
            if (savedNode == null) {
                dataController.popError(message("InvalidParameters"));
                close();
                return false;
            }
            chainName = savedNode.getChainName();
            showLogs(message("Node") + ":\n" + chainName);
            Platform.runLater(() -> {
                nameLabel.setText(chainName);
            });
            List<Integer> cols = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                cols.add(i);
            }
            results = new TmpTable()
                    .setSourceData(treeTable)
                    .setTargetName(dataName + "_" + message("QueryResults"))
                    .setSourcePickIndice(cols)
                    .setImportData(false);
            results.setTask(currentTask);
            if (!results.createTable(conn)) {
                showLogs("Failed");
                return false;
            }
            tableData2D = results.getTableData2D();
            querySQL = "SELECT " + NodeFields + " FROM "
                    + nodeTable.getTableName()
                    + " WHERE parentid=? AND parentid<>nodeid  ORDER BY "
                    + nodeTable.getOrderColumns();
            insert = conn.prepareStatement(tableData2D.insertStatement());
            conn.setAutoCommit(false);
            long count = writeDescedents(currentTask, conn, savedNode.getNodeid(), 0, all);
            if (count > 0) {
                insert.executeBatch();
            }
            conn.commit();
            insert.close();
            if (count < 0) {
                results = null;
                return false;
            }
            results.setRowsNumber(count);
            if (count > 0) {
                Data2D.saveAttributes(conn, results, results.getColumns());
            }
            showLogs(message("Generated") + ": " + results.getSheet() + "  "
                    + message("RowsNumber") + ": " + count);
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            results = null;
            return false;
        }
    }

    public long writeDescedents(FxTask currentTask, Connection conn,
            long nodeid, long inCount, boolean all) {
        if (inCount < 0) {
            return inCount;
        }
        if (nodeid < 0 || (currentTask != null && !currentTask.isWorking())) {
            return -inCount;
        }
        long count = inCount;
        try (PreparedStatement query = conn.prepareStatement(querySQL)) {
            query.setLong(1, nodeid);
            try (ResultSet queryResults = query.executeQuery()) {
                while (queryResults != null && queryResults.next()) {
                    if (currentTask != null && !currentTask.isWorking()) {
                        return -count;
                    }
                    long childid = queryResults.getLong("nodeid");
                    Data2DRow data2DRow = tableData2D.newRow();
                    data2DRow.setValue(results.columnName(1), childid + "");
                    data2DRow.setValue(results.columnName(2), queryResults.getString("title"));
                    data2DRow.setValue(results.columnName(3), queryResults.getObject("order_number") + "");
                    data2DRow.setValue(results.columnName(4), queryResults.getObject("update_time") + "");
                    data2DRow.setValue(results.columnName(5), queryResults.getLong("parentid") + "");
                    if (tableData2D.setInsertStatement(conn, insert, data2DRow)) {
                        insert.addBatch();
                        if (++count % Database.BatchSize == 0) {
                            insert.executeBatch();
                            conn.commit();
                            showLogs(message("Inserted") + ": " + count);
                        }
                    }
                    if (all) {
                        count = writeDescedents(currentTask, conn, childid, count, all);
                    }
                    if (count < 0) {
                        return count;
                    }
                }
            } catch (Exception e) {
                showLogs(e.toString());
                return -count;
            }
        } catch (Exception e) {
            showLogs(e.toString());
            return -count;
        }
        return count;
    }

    @Override
    public void afterTask(boolean ok) {
        if (results != null) {
            if (results.getRowsNumber() > 0) {
                DataTreeQueryResultsController.open(this, dataController,
                        message("QueryDescendants") + "\n"
                        + message("DataTree") + ": " + nodeTable.getDataName() + "\n"
                        + message("Node") + ": " + chainName,
                        results);
            } else {
                alertInformation(message("ResultIsEmpty"));
            }
        }
    }

    /*
        static
     */
    public static DataTreeQueryDescendantsController open(BaseDataTreeController parent,
            DataNode inNode, boolean onlyChildren) {
        try {
            DataTreeQueryDescendantsController controller = (DataTreeQueryDescendantsController) WindowTools
                    .openStage(Fxmls.DataTreeQueryDescendantsFxml);
            controller.setParameters(parent, inNode, onlyChildren);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
