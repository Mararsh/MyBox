package mara.mybox.data2d.modify;

import java.sql.PreparedStatement;
import mara.mybox.data2d.DataMatrix;

/**
 * @Author Mara
 * @CreateDate 2022-8-16
 * @License Apache License Version 2.0
 */
public abstract class DataMatrixModify extends Data2DModify {

    protected DataMatrix sourceMatrix;
    protected PreparedStatement update;

    public boolean setSourceMatrix(DataMatrix data) {
        if (!setSourceData(data)) {
            return false;
        }
        sourceMatrix = data;
        return true;
    }

    public boolean updateMatrix() {
        try {
            if (stopped || sourceMatrix == null || conn == null) {
                return false;
            }
//            String sql = "SELECT count(*) FROM " + tableName;
//            showInfo(sql);
//            try (ResultSet query = conn.prepareStatement(sql).executeQuery()) {
//                if (query.next()) {
//                    rowsNumber = query.getLong(1);
//                }
//            }
//            sourceData.setRowsNumber(rowsNumber);
//            if (stopped) {
//                return false;
//            }
//            sourceData.saveAttributes(conn);
//            showInfo(message("DataTable") + ": " + tableName + "  "
//                    + message("RowsNumber") + ": " + rowsNumber);
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    @Override
    public boolean end() {
        return true;
    }

}
