package mara.mybox.data2d.modify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.table.TableMatrixCell;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-3-13
 * @License Apache License Version 2.0
 */
public class DataMatrixClear extends DataMatrixModify {

    public DataMatrixClear(DataMatrix data) {
        setSourceMatrix(data);
    }

    @Override
    public boolean go() {
        try (Connection dconn = DerbyBase.getConnection();
                PreparedStatement clear = dconn.prepareStatement(TableMatrixCell.ClearData)) {
            showInfo(TableMatrixCell.ClearData + "\ndata ID: " + dataID);
            clear.setLong(1, dataID);
            handledCount = clear.executeUpdate();
            if (handledCount >= 0) {
                conn.commit();
                sourceData.setRowsNumber(0);
                sourceData.saveAttributes(dconn);
                showInfo(message("Cleared") + ": " + handledCount);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

}
