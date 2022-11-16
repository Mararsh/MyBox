package mara.mybox.data2d.reader;

import java.util.List;
import mara.mybox.calculation.SimpleLinearRegression;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DSimpleLinearRegression extends Data2DOperator {

    protected SimpleLinearRegression simpleRegression;
    protected double x, y;

    public static Data2DSimpleLinearRegression create(Data2D_Edit data) {
        Data2DSimpleLinearRegression op = new Data2DSimpleLinearRegression();
        return op.setData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        return cols != null && !cols.isEmpty() && simpleRegression != null;
    }

    @Override
    public void handleRow() {
        try {
            x = DoubleTools.toDouble(sourceRow.get(cols.get(0)), invalidAs);
            y = DoubleTools.toDouble(sourceRow.get(cols.get(1)), invalidAs);
            List<String> row = simpleRegression.addData(rowIndex, x, y);
            if (csvPrinter != null) {
                csvPrinter.printRecord(row);
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
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
