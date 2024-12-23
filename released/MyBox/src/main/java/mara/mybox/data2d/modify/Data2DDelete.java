package mara.mybox.data2d.modify;

import java.util.List;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-8-16
 * @License Apache License Version 2.0
 */
public class Data2DDelete extends Data2DModify {

    public static Data2DDelete create(Data2D_Edit data) {
        if (data == null) {
            return null;
        }
        Data2DDelete operate = new Data2DDelete();
        if (!operate.setSourceData(data)) {
            return null;
        }
        operate.initWriter();
        return operate;
    }

    @Override
    public void handleRow(List<String> row, long index) {
        try {
            sourceRow = row;
            sourceRowIndex = index;
            targetRow = null;
            rowPassFilter = sourceData.filterDataRow(sourceRow, sourceRowIndex);
            reachMaxFiltered = sourceData.filterReachMaxPassed();
            deleteRow(rowPassFilter && !reachMaxFiltered);
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    public void deleteRow(boolean handle) {
        if (handle) {
            handledCount++;
        } else {
            targetRow = sourceRow;
            writeRow();
        }
    }

}
