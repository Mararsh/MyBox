package mara.mybox.data2d.operate;

import mara.mybox.calculation.SimpleLinearRegression;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DSimpleLinearRegression extends Data2DOperate {

    protected SimpleLinearRegression simpleRegression;
    protected double x, y;

    public static Data2DSimpleLinearRegression create(Data2D_Edit data) {
        Data2DSimpleLinearRegression op = new Data2DSimpleLinearRegression();
        return op.setSourceData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        return super.checkParameters()
                && cols != null && !cols.isEmpty()
                && simpleRegression != null;
    }

    @Override
    public boolean handleRow() {
        try {
            targetRow = null;
            if (sourceRow == null) {
                return false;
            }
            x = DoubleTools.toDouble(sourceRow.get(cols.get(0)), invalidAs);
            y = DoubleTools.toDouble(sourceRow.get(cols.get(1)), invalidAs);
            targetRow = simpleRegression.addData(sourceRowIndex, x, y);
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
        set
     */
    public Data2DSimpleLinearRegression setSimpleRegression(SimpleLinearRegression simpleRegression) {
        this.simpleRegression = simpleRegression;
        return this;
    }

}
