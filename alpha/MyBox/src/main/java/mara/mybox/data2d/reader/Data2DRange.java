package mara.mybox.data2d.reader;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DRange extends Data2DOperator {

    protected long start, end;  // 1-based, include end

    public static Data2DRange create(Data2D_Edit data) {
        Data2DRange op = new Data2DRange();
        return op.setData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        return cols != null && !cols.isEmpty() && csvPrinter != null;
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
            if (row.isEmpty() || rowIndex < start) {
                return;
            }
            if (rowIndex > end) {
                reader.readerStopped = true;
                return;
            }
            if (includeRowNumber) {
                row.add(0, rowIndex + "");
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
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
