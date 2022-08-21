package mara.mybox.data2d.reader;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.Data2D_Edit;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DRowExpression extends Data2DOperator {

    protected String script, name;
    protected boolean errorContinue;

    public static Data2DRowExpression create(Data2D_Edit data) {
        Data2DRowExpression op = new Data2DRowExpression();
        return op.setData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        return cols != null && !cols.isEmpty() && csvPrinter != null
                && script != null && name != null;
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
            if (data2D.calculateDataRowExpression(script, sourceRow, rowIndex)) {
                row.add(data2D.expressionResult());
            } else {
                if (errorContinue) {
                    row.add(null);
                } else {
                    reader.readerStopped = true;
                    return;
                }
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
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

    public Data2DRowExpression setErrorContinue(boolean errorContinue) {
        this.errorContinue = errorContinue;
        return this;
    }

}
