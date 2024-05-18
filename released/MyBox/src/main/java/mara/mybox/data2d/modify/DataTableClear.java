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
        setSourceTable(data);
    }

    @Override
    public boolean go() {
        try (Connection dconn = DerbyBase.getConnection()) {
            conn = dconn;
            String sql = "DELETE FROM " + tableName;
            showInfo(sql);
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
