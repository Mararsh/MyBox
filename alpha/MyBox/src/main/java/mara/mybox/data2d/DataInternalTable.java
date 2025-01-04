package mara.mybox.data2d;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.BaseTable;
import static mara.mybox.db.table.BaseTableTools.internalTables;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-2-12
 * @License Apache License Version 2.0
 */
public class DataInternalTable extends DataTable {

    public DataInternalTable() {
        dataType = DataType.InternalTable;
    }

    @Override
    public int type() {
        return type(DataType.InternalTable);
    }

    @Override
    public boolean readColumns(Connection conn) {
        try {
            columns = null;
            if (dataID < 0 || sheet == null) {
                return false;
            }
            BaseTable internalTable = internalTables().get(sheet.toUpperCase());
            if (internalTable == null) {
                dataType = DataType.DatabaseTable;
                return super.readColumns(conn);
            }
            List<ColumnDefinition> dbColumns = internalTable.getColumns();
            if (dbColumns == null) {
                return false;
            }
            columns = new ArrayList<>();
            colsNumber = dbColumns.size();
            for (int i = 0; i < colsNumber; i++) {
                ColumnDefinition dbColumn = dbColumns.get(i);
                Data2DColumn dataColumn = new Data2DColumn();
                dataColumn.cloneFrom(dbColumn);
                String columnName = DerbyBase.fixedIdentifier(dbColumn.getColumnName());
                dataColumn.setColumnName(columnName);
                dataColumn.setData2DDefinition(this)
                        .setDataID(dataID).setColumnID(-1).setIndex(i);
                if (savedColumns != null) {
                    for (Data2DColumn savedColumn : savedColumns) {
                        if (columnName.equalsIgnoreCase(savedColumn.getColumnName())) {
                            dataColumn.setColumnID(savedColumn.getColumnID());
                            break;
                        }
                    }
                }
                columns.add(dataColumn);
            }
            tableData2DColumn.save(conn, dataID, columns);
            tableData2DDefinition.updateData(conn, this);
            tableData2D.readDefinitionFromDB(conn, sheet);
            tableData2D.setColumns(dbColumns);
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return false;
        }
    }

}
