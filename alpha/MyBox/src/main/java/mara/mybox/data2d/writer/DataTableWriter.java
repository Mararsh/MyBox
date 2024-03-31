package mara.mybox.data2d.writer;

import java.sql.PreparedStatement;
import java.util.List;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.tools.Data2DTableTools;
import mara.mybox.db.Database;
import mara.mybox.db.data.Data2DColumn;
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
    protected List<Data2DColumn> referColumns;
    protected List<String> keys;
    protected String idName;
    protected boolean dropExisted;
    protected long dwCount;
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
                        referColumns, keys, targetTableDesciption, idName, dropExisted);
                if (targetTable == null) {
                    return false;
                }
            }
            tableData2D = targetTable.getTableData2D();
            columns = targetTable.getColumns();
            columns = columns.subList(1, columns.size());
            conn.setAutoCommit(false);
            dwCount = 0;
            String sql = tableData2D.insertStatement();
            showInfo(sql);
            insert = conn.prepareStatement(sql);
            targetData = targetTable;
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    @Override
    public void printTargetRow() {
        try {
            if (targetRow == null || conn == null || targetTable == null) {
                return;
            }
            Data2DRow data2DRow = targetTable.makeRow(targetRow, invalidAs());
            if (data2DRow == null || data2DRow.isNoColumn()) {
                return;
            }
            if (tableData2D.setInsertStatement(conn, insert, data2DRow)) {
                insert.addBatch();
                if (++dwCount % Database.BatchSize == 0) {
                    insert.executeBatch();
                    conn.commit();
                    showInfo(message("Inserted") + ": " + dwCount);
                }
            }
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public void closeWriter() {
        try {
            created = false;
            if (conn == null || targetTable == null) {
                return;
            }
            insert.executeBatch();
            conn.commit();
            insert.close();
            targetTable.setRowsNumber(dwCount);
            Data2D.saveAttributes(conn, targetTable, targetTable.getColumns());
            targetData = targetTable;
            created = true;
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

    public List<Data2DColumn> getReferColumns() {
        return referColumns;
    }

    public DataTableWriter setReferColumns(List<Data2DColumn> referColumns) {
        this.referColumns = referColumns;
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

    public long getDwCount() {
        return dwCount;
    }

    public DataTableWriter setDwCount(long dwCount) {
        this.dwCount = dwCount;
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
