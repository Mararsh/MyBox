package mara.mybox.data2d.operate;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DReadRows extends Data2DOperate {

    protected List<List<String>> rows;
    protected long maxCount = -1;

    public static Data2DReadRows create(Data2D_Edit data) {
        Data2DReadRows op = new Data2DReadRows();
        return op.setSourceData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        if (!super.checkParameters()) {
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
            row.addAll(sourceRow);
            if (includeRowNumber) {
                row.add(0, sourceRowIndex + "");
            }
            rows.add(row);
            if (maxCount > 0 && handledCount >= maxCount) {
                stopped = true;
            }
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

    /*
        get
     */
    public List<List<String>> getRows() {
        return rows;
    }

    public Data2DReadRows setMaxCount(long maxCount) {
        this.maxCount = maxCount;
        return this;
    }

}
