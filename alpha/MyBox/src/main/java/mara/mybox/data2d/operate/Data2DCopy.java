package mara.mybox.data2d.operate;

import java.util.ArrayList;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Edit;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DCopy extends Data2DOperate {

    private String value;
    private int rowSize;

    public static Data2DCopy create(Data2D_Edit data) {
        Data2DCopy op = new Data2DCopy();
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
            rowSize = sourceRow.size();
            for (int col : cols) {
                if (col >= 0 && col < rowSize) {
                    value = sourceRow.get(col);
                } else {
                    value = null;
                }
                targetRow.add(value);
            }
            if (targetRow.isEmpty()) {
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

    @Override
    public void handleTargetData(Data2D targetData) {
        copySourceAttriutes(targetData);
    }

}
