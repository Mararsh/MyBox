package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.db.table.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-23
 * @License Apache License Version 2.0
 */
public class BaseSheetController_Calculation extends BaseSheetController_Size {

    // 1-based, include
    public void sum(List<Integer> calculationColumns, List<Integer> displayColumns, int intFrom, int inTo) {
        try {
            if (calculationColumns == null || calculationColumns.isEmpty()
                    || sheet == null || intFrom > inTo) {
                popError(message("InvalidParameters"));
                return;
            }
            int from = Math.min(sheet.length, Math.max(1, intFrom));
            int to = Math.min(sheet.length, Math.max(1, inTo));
            if (from > to) {
                popError(message("InvalidParameters"));
                return;
            }
            MyBoxLog.console(from + " " + to);
            int calSize = calculationColumns.size();
            int displaySize = displayColumns.size();
            List<ColumnDefinition> dataColumns = new ArrayList<>();
            dataColumns.add(new ColumnDefinition(message("Calculation"), ColumnType.String));
            for (int c : calculationColumns) {
                ColumnDefinition def = columns.get(c);
                if (!def.isNumberType()) {
                    popError(message("InvalidParameters"));
                    return;
                }
                dataColumns.add(new ColumnDefinition(def.getName(), ColumnType.Double));
            }
            for (int c : displayColumns) {
                dataColumns.add(columns.get(c));
            }

            String[][] data = new String[to - from + 2][calSize + displaySize + 1];
            data[0][0] = message("Total");
            for (int c = 0; c < calSize; ++c) {
                double sum = 0;
                int colIndex = calculationColumns.get(c);
                for (int r = from - 1; r <= to - 1; ++r) {
                    try {
                        sum += Double.valueOf(sheet[r][colIndex]);
                    } catch (Exception e) {
                    }
                }
                data[0][c + 1] = DoubleTools.format(sum, 2);
            }
            for (int c = 0; c < displaySize; ++c) {
                data[0][c + calSize + 1] = "";
            }
            for (int r = 1; r <= to - from + 1; ++r) {
                data[r][0] = null;
            }
            for (int c = 0; c < calSize; ++c) {
                int colIndex = calculationColumns.get(c);
                for (int r = 1; r <= to - from + 1; ++r) {
                    data[r][c + 1] = sheet[r + from - 2][colIndex];
                }
            }
            for (int c = 0; c < displaySize; ++c) {
                int colIndex = displayColumns.get(c);
                for (int r = 1; r <= to - from + 1; ++r) {
                    data[r][c + calSize + 1] = sheet[r + from - 2][colIndex];
                }
            }
            DataClipboardController.open(data, dataColumns);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
