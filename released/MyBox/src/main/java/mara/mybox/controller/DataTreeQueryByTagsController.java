package mara.mybox.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.TmpTable;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.data.DataTag;
import mara.mybox.db.table.BaseNodeTable;
import static mara.mybox.db.table.BaseNodeTable.NodeFields;
import mara.mybox.db.table.TableData2D;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-5-7
 * @License Apache License Version 2.0
 */
public class DataTreeQueryByTagsController extends BaseTaskController {

    protected BaseDataTreeController dataController;
    protected BaseNodeTable nodeTable;
    protected TableDataTag tagTable;
    protected TableDataNodeTag nodeTagsTable;
    protected String dataName, chainName, querySQL;
    protected DataTable treeTable;
    protected TmpTable results;
    protected TableData2D tableData2D;
    protected PreparedStatement insert;
    protected long count;

    @FXML
    protected ControlDataNodeTags tagsController;

    public void setParameters(BaseDataTreeController parent) {
        try {
            if (parent == null) {
                close();
                return;
            }
            dataController = parent;
            nodeTable = dataController.nodeTable;
            tagTable = dataController.tagTable;
            nodeTagsTable = dataController.nodeTagsTable;
            dataName = nodeTable.getDataName();
            baseName = baseName + "_" + dataName;

            baseTitle = nodeTable.getTreeName() + " - " + message("QueryByTags");
            setTitle(baseTitle);

            tagsController.setParameters(dataController, nodeTable, tagTable, nodeTagsTable);

            tagsController.loadTags(null);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        try {
            if (tagsController.selected == null || tagsController.selected.isEmpty()) {
                popError(message("SelectToHandle"));
                return false;
            }
            querySQL = "SELECT " + NodeFields + " FROM "
                    + nodeTable.getTableName() + ","
                    + nodeTagsTable.getTableName()
                    + " WHERE ttagid=? AND tnodeid=nodeid ORDER BY "
                    + nodeTable.getOrderColumns();
            showLogs(querySQL);
            count = 0;
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        try (Connection conn = DerbyBase.getConnection();
                PreparedStatement query = conn.prepareStatement(querySQL)) {
            treeTable = nodeTable.recordTable(conn);
            if (treeTable == null) {
                dataController.popError(message("InvalidParameters"));
                close();
                return false;
            }
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
            insert = conn.prepareStatement(tableData2D.insertStatement());
            conn.setAutoCommit(false);
            count = 0;
            for (DataTag tag : tagsController.selected) {
                if (currentTask != null && !currentTask.isWorking()) {
                    return false;
                }
                showLogs(message("Tag") + ":" + tag.getTag());
                query.setLong(1, tag.getTagid());
                queryNodes(currentTask, conn, query);
            }
            if (count > 0) {
                insert.executeBatch();
                conn.commit();
            }
            insert.close();
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

    public boolean queryNodes(FxTask currentTask, Connection conn, PreparedStatement query) {
        if (query == null || (currentTask != null && !currentTask.isWorking())) {
            return false;
        }
        try (ResultSet queryResults = query.executeQuery()) {
            while (queryResults != null && queryResults.next()) {
                if (currentTask != null && !currentTask.isWorking()) {
                    return false;
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
            }
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    @Override
    public void afterTask(boolean ok) {
        treeTable.stopFilter();
        if (results != null) {
            if (results.getRowsNumber() > 0) {
                String info = message("QueryByTags") + "\n"
                        + message("DataTree") + ": " + nodeTable.getDataName() + "\n"
                        + message("Tags") + ": \n";
                for (DataTag tag : tagsController.selected) {
                    info += tag.getTag() + "  ";
                }
                DataTreeQueryResultsController.open(this, dataController, info, results);
            } else {
                alertInformation(message("ResultIsEmpty"));
            }
        }
    }

    /*
        static
     */
    public static DataTreeQueryByTagsController open(BaseDataTreeController parent) {
        try {
            DataTreeQueryByTagsController controller = (DataTreeQueryByTagsController) WindowTools
                    .referredStage(parent, Fxmls.DataTreeQueryByTagsFxml);
            controller.setParameters(parent);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
