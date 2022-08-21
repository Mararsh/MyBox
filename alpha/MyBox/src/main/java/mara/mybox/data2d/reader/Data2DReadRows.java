package mara.mybox.data2d.reader;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.Data2D_Edit;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DReadRows extends Data2DOperator {

    protected List<List<String>> rows;

    public static Data2DReadRows create(Data2D_Edit data) {
        Data2DReadRows op = new Data2DReadRows();
        return op.setData(data) ? op : null;
    }

    @Override
    public void handleRow() {
        try {
            List<String> row = new ArrayList<>();
            row.addAll(sourceRow);
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
