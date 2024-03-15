package mara.mybox.data2d.modify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import mara.mybox.data.SetValue;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataTableSetValue extends Data2DSetValue {

    protected DataTable sourceTable;
    protected TableData2D tableData2D;
    protected int columnsNumber;
    protected Data2DRow sourceTableRow;
    protected Connection conn;
    protected PreparedStatement update;
    protected List<Data2DColumn> columns;

    public DataTableSetValue(DataTable data, SetValue setValue) {
        setSourceData(data);
        sourceTable = data;
    }

    @Override
    public boolean checkParameters() {
        return super.checkParameters() && !initParameters(setValue);
    }

    @Override
    public boolean go() {
        handledCount = 0;
        tableData2D = sourceTable.getTableData2D();
        tableData2D.setTableName(sourceTable.getSheet());
        String sql = "SELECT * FROM " + sourceTable.getSheet();
        showInfo(sql);
        columns = sourceTable.getColumns();
        columnsNumber = columns.size();
        try (Connection dconn = DerbyBase.getConnection();
                PreparedStatement statement = dconn.prepareStatement(sql);
                ResultSet results = statement.executeQuery();
                PreparedStatement dUpdate = conn.prepareStatement(tableData2D.updateStatement())) {
            conn = dconn;
            conn.setAutoCommit(false);
            update = dUpdate;
            while (results.next() && !stopped && !reachMax) {
                sourceTableRow = tableData2D.readData(results);
                sourceRow = sourceTableRow.toStrings(columns);
                sourceRowIndex++;
                handleRow(sourceRow, sourceRowIndex);
            }
            update.executeBatch();
            conn.commit();
            showInfo(message("Updated") + ": " + handledCount);
            conn.close();
            conn = null;
            return true;
        } catch (Exception e) {
            failStop(e.toString());
            return false;
        }
    }

    @Override
    public void writeRow() {
        try {
            if (stopped || targetRow == null || targetRow.isEmpty()) {
                return;
            }
            for (int i = 0; i < columnsNumber; ++i) {
                Data2DColumn column = columns.get(i);
                String name = column.getColumnName();
                sourceTableRow.setColumnValue(name, column.fromString(targetRow.get(i), ColumnDefinition.InvalidAs.Blank));
            }
            if (tableData2D.setUpdateStatement(conn, update, sourceTableRow)) {
                update.addBatch();
                if (++handledCount % Database.BatchSize == 0) {
                    update.executeBatch();
                    conn.commit();
                    showInfo(message("Updated") + ": " + handledCount);
                }
            }
        } catch (Exception e) {
            showError(e.toString());
        }
    }

}
