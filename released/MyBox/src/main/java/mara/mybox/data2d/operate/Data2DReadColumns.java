package mara.mybox.data2d.operate;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.Data2D_Edit;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DReadColumns extends Data2DOperate {

    protected List<List<String>> rows;

    public static Data2DReadColumns create(Data2D_Edit data) {
        Data2DReadColumns op = new Data2DReadColumns();
        return op.setSourceData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        if (!super.checkParameters() || cols == null || cols.isEmpty()) {
            return false;
        }
        rows = new ArrayList<>();
        return true;
    }

    @Override
    public boolean handleRow() {
        try {
            if (sourceRow == null) {
                return false;
            }
            List<String> row = new ArrayList<>();
            for (int col : cols) {
                if (col >= 0 && col < sourceRow.size()) {
                    row.add(sourceRow.get(col));
                } else {
                    row.add(null);
                }
            }
            if (row.isEmpty()) {
                return false;
            }
            if (includeRowNumber) {
                row.add(0, sourceRowIndex + "");
            }
            rows.add(row);
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    /*
        get
     */
    public List<List<String>> getRows() {
        return rows;
    }

}
