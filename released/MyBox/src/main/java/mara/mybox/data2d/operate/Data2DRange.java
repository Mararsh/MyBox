package mara.mybox.data2d.operate;

import java.util.ArrayList;
import mara.mybox.data2d.Data2D_Edit;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DRange extends Data2DOperate {

    protected long start, end;  // 1-based, include end

    public static Data2DRange create(Data2D_Edit data) {
        Data2DRange op = new Data2DRange();
        return op.setSourceData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        return super.checkParameters() && cols != null && !cols.isEmpty();
    }

    @Override
    public boolean handleRow() {
        try {
            targetRow = null;
            if (sourceRow == null) {
                return false;
            }
            targetRow = new ArrayList<>();
            for (int col : cols) {
                if (col >= 0 && col < sourceRow.size()) {
                    targetRow.add(sourceRow.get(col));
                } else {
                    targetRow.add(null);
                }
            }
            if (targetRow.isEmpty() || sourceRowIndex < start) {
                return false;
            }
            if (sourceRowIndex > end) {
                stop();
                return false;
            }
            if (includeRowNumber) {
                targetRow.add(0, sourceRowIndex + "");
            }
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    /*
        set
     */
    public Data2DRange setStart(long start) {
        this.start = start;
        return this;
    }

    public Data2DRange setEnd(long end) {
        this.end = end;
        return this;
    }

}
