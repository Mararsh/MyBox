package mara.mybox.data2d.reader;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.Data2D_Edit;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DCopy extends Data2DOperator {

    public static Data2DCopy create(Data2D_Edit data) {
        Data2DCopy op = new Data2DCopy();
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
            if (row.isEmpty()) {
                return;
            }
            if (includeRowNumber) {
                row.add(0, rowIndex + "");
            }
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

}
