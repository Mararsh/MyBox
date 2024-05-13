package mara.mybox.data2d.modify;

import java.sql.Connection;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.DerbyBase;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataTableClear extends DataTableModify {

    public DataTableClear(DataTable data) {
        setSourceData(data);
        sourceTable = data;
    }

    @Override
    public boolean go() {
        try (Connection dconn = DerbyBase.getConnection()) {
            conn = dconn;
            String sql = "DELETE FROM " + sourceTable.getSheet();
            showInfo(sql);
            tableData2D = sourceTable.getTableData2D();
            tableData2D.setTableName(sourceTable.getSheet());
            handledCount = tableData2D.clearData(conn);
            if (handledCount < 0) {
                return false;
            }
            return updateTable();
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

}
