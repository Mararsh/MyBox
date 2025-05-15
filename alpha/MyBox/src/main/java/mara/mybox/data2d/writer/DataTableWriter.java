package mara.mybox.data2d.writer;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.tools.Data2DTableTools;
import mara.mybox.db.Database;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataTableWriter extends Data2DWriter {

    protected DataTable targetTable;
    protected String targetTableName, targetTableDesciption;
    protected List<String> keys;
    protected String idName;
    protected boolean dropExisted;
    protected TableData2D tableData2D;
    protected PreparedStatement insert;

    @Override
    public boolean openWriter() {
        try {
            if (!super.openWriter()) {
                return false;
            }
            conn = conn();
            if (conn == null) {
                return false;
            }
            if (targetTable == null) {
                targetTable = Data2DTableTools.createTable(task(), conn, targetTableName,
                        columns, keys, targetTableDesciption, idName, dropExisted);
                if (targetTable == null) {
                    return false;
                }
            } else {
                columns = targetTable.getColumns();
            }
            tableData2D = targetTable.getTableData2D();
            conn.setAutoCommit(false);
            targetRowIndex = 0;
            String sql = tableData2D.insertStatement();
            showInfo(sql);
            insert = conn.prepareStatement(sql);
            targetData = targetTable;
            validateValue = true;
            showInfo(message("Writing") + " " + targetTable.getName());
            status = Status.Openned;
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    @Override
    public void writeRow(List<String> inRow) {
        try {
            printRow = null;
            if (inRow == null || inRow.isEmpty() || conn == null || targetTable == null) {
                return;
            }
            printRow = new ArrayList<>();
            Data2DRow data2DRow = targetTable.makeRow(inRow, this);
            if (data2DRow == null || data2DRow.isEmpty()) {
                return;
            }
            if (tableData2D.setInsertStatement(conn, insert, data2DRow)) {
                insert.addBatch();
                if (++targetRowIndex % Database.BatchSize == 0) {
                    insert.executeBatch();
                    conn.commit();
                    showInfo(message("Inserted") + ": " + targetRowIndex);
                }
            }
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public void closeWriter() {
        try {
            if (conn == null || targetTable == null || insert == null) {
                showInfo(message("Failed"));
                status = Status.Failed;
                return;
            }
            if (isFailed()) {
                insert.close();
                showInfo(message("Failed"));
                status = Status.Failed;
                return;
            }
            insert.executeBatch();
            conn.commit();
            insert.close();
            targetTable.setRowsNumber(targetRowIndex);
            Data2D.saveAttributes(conn, targetTable, targetTable.getColumns());
            targetData = targetTable;
            showInfo(message("Generated") + ": " + targetTable.getSheet() + "  "
                    + message("RowsNumber") + ": " + targetRowIndex);
            status = targetRowIndex == 0 ? Status.NoData : Status.Created;
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    /*
        get/set
     */
    public DataTable getTargetTable() {
        return targetTable;
    }

    public DataTableWriter setTargetTable(DataTable targetTable) {
        this.targetTable = targetTable;
        return this;
    }

    public String getTargetTableName() {
        return targetTableName;
    }

    public DataTableWriter setTargetTableName(String targetTableName) {
        this.targetTableName = targetTableName;
        return this;
    }

    public String getTargetTableDesciption() {
        return targetTableDesciption;
    }

    public DataTableWriter setTargetTableDesciption(String targetTableDesciption) {
        this.targetTableDesciption = targetTableDesciption;
        return this;
    }

    public List<String> getKeys() {
        return keys;
    }

    public DataTableWriter setKeys(List<String> keys) {
        this.keys = keys;
        return this;
    }

    public String getIdName() {
        return idName;
    }

    public DataTableWriter setIdName(String idName) {
        this.idName = idName;
        return this;
    }

    public boolean isDropExisted() {
        return dropExisted;
    }

    public DataTableWriter setDropExisted(boolean dropExisted) {
        this.dropExisted = dropExisted;
        return this;
    }

    public PreparedStatement getInsert() {
        return insert;
    }

    public DataTableWriter setInsert(PreparedStatement insert) {
        this.insert = insert;
        return this;
    }

}
