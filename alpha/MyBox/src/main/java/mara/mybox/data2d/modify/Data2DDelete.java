package mara.mybox.data2d.modify;

import java.util.List;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.data2d.operate.Data2DOperate;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-8-16
 * @License Apache License Version 2.0
 */
public class Data2DDelete extends Data2DOperate {

    public static Data2DDelete create(Data2D_Edit data) {
        if (data == null) {
            return null;
        }
        Data2DDelete operate = new Data2DDelete();
        if (!operate.setSourceData(data)) {
            return null;
        }
        operate.addWriter(data.selfWriter());
        return operate;
    }

    @Override
    public void handleRow(List<String> row, long index) {
        try {
            sourceRow = row;
            sourceRowIndex = index;
            targetRow = null;
            passFilter = sourceData.filterDataRow(sourceRow, sourceRowIndex);
            reachMax = sourceData.filterReachMaxPassed();
            if (sourceData.error != null) {
                if (errorContinue) {
                    showError(sourceData.error);
                    return;
                } else {
                    failStop(sourceData.error);
                    return;
                }
            }
            deleteRow(passFilter && !reachMax);
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
