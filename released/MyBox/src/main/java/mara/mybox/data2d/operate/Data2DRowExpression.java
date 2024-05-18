package mara.mybox.data2d.operate;

import java.util.ArrayList;
import mara.mybox.data2d.Data2D_Edit;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DRowExpression extends Data2DOperate {

    protected String script, name;

    public static Data2DRowExpression create(Data2D_Edit data) {
        Data2DRowExpression op = new Data2DRowExpression();
        return op.setSourceData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        return super.checkParameters()
                && cols != null && !cols.isEmpty()
                && script != null && name != null;
    }

    @Override
    public boolean handleRow() {
        try {
            targetRow = null;
            if (sourceRow == null) {
                return false;
            }
            targetRow = new ArrayList<>();
            int rowSize = sourceRow.size();
            for (int col : cols) {
                if (col >= 0 && col < rowSize) {
                    targetRow.add(sourceRow.get(col));
                } else {
                    targetRow.add(null);
                }
            }
            if (targetRow.isEmpty()) {
                return false;
            }
            if (includeRowNumber) {
                targetRow.add(0, sourceRowIndex + "");
            }
            if (sourceData.calculateDataRowExpression(script, sourceRow, sourceRowIndex)) {
                targetRow.add(sourceData.expressionResult());
                return true;
            } else {
                stop();
                return false;
            }
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    /*
        set
     */
    public Data2DRowExpression setScript(String script) {
        this.script = script;
        return this;
    }

    public Data2DRowExpression setName(String name) {
        this.name = name;
        return this;
    }

}
