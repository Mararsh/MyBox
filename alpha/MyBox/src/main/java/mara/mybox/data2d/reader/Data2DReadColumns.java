package mara.mybox.data2d.reader;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.Data2D_Edit;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DReadColumns extends Data2DOperator {

    protected List<List<String>> rows;

    public static Data2DReadColumns create(Data2D_Edit data) {
        Data2DReadColumns op = new Data2DReadColumns();
        return op.setData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        if (cols == null || cols.isEmpty()) {
            return false;
        }
        rows = new ArrayList<>();
        return true;
    }

    @Override
    public void handleRow() {
        try {
            List<String> row = new ArrayList<>();
            for (int col : cols) {
                if (col >= 0 && col < sourceRow.size()) {
                    row.add(sourceRow.get(col));
                } else {
                    row.add(null);
                }
            }
            if (row.isEmpty()) {
                return;
            }
            if (includeRowNumber) {
                row.add(0, rowIndex + "");
            }
            rows.add(row);
        } catch (Exception e) {
        }
    }

    /*
        get
     */
    public List<List<String>> getRows() {
        return rows;
    }

}
