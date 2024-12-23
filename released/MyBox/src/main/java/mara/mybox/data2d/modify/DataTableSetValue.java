package mara.mybox.data2d.modify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import mara.mybox.data.SetValue;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.tools.Data2DRowTools;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataTableSetValue extends DataTableModify {

    public DataTableSetValue(DataTable data, SetValue value) {
        if (!setSourceTable(data)) {
            return;
        }
        initSetValue(value);
        sourceTable = data;
    }

    @Override
    public boolean go() {
        handledCount = 0;
        String sql = "SELECT * FROM " + tableName;
        showInfo(sql);
        try (Connection dconn = DerbyBase.getConnection();
                PreparedStatement statement = dconn.prepareStatement(sql);
                ResultSet results = statement.executeQuery();
                PreparedStatement dUpdate = dconn.prepareStatement(tableData2D.updateStatement())) {
            conn = dconn;
            conn.setAutoCommit(false);
            update = dUpdate;
            while (results.next() && !stopped && !reachMaxFiltered) {
                sourceTableRow = tableData2D.readData(results);
                sourceRow = Data2DRowTools.toStrings(sourceTableRow, columns);
                sourceRowIndex++;
                setValue(sourceRow, sourceRowIndex);
            }
            if (!stopped) {
                update.executeBatch();
                conn.commit();
                updateTable();
            }
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
            rowChanged = false;
            for (int i = 0; i < columnsNumber; ++i) {
                column = columns.get(i);
                columnName = column.getColumnName();
                currentValue = targetRow.get(i);
                newValue = setValue.makeValue(sourceData, column,
                        currentValue, sourceRow, sourceRowIndex,
                        setValueIndex, setValueDigit, random);
                valueInvalid = setValue.valueInvalid;
                if (!valueInvalid && validateColumn) {
                    if (!column.validValue(newValue)) {
                        valueInvalid = true;
                    }
                }
                if (valueInvalid) {
                    if (skipInvalid) {
                        continue;
                    } else if (rejectInvalid) {
                        failStop(message("InvalidData") + ". "
                                + message("Column") + ":" + columnName + "  "
                                + message("Value") + ": " + newValue);
                        return;
                    }
                }
                if ((currentValue == null && newValue == null)
                        || (currentValue != null && currentValue.equals(newValue))) {
                    continue;
                }
                sourceTableRow.setValue(columnName, column.fromString(newValue));
                rowChanged = true;
            }
            if (rowChanged) {
                setValueIndex++;
                if (tableData2D.setUpdateStatement(conn, update, sourceTableRow)) {
                    update.addBatch();
                    if (++handledCount % Database.BatchSize == 0) {
                        update.executeBatch();
                        conn.commit();
                        showInfo(message("Updated") + ": " + handledCount);
                    }
                }
            }
        } catch (Exception e) {
            showError(e.toString());
        }
    }

}
