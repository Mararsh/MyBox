package mara.mybox.data2d.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-9-12
 * @License Apache License Version 2.0
 */
public class Data2DColumnTools {

    public static List<String> toNames(List<Data2DColumn> cols) {
        try {
            if (cols == null) {
                return null;
            }
            List<String> names = new ArrayList<>();
            for (Data2DColumn c : cols) {
                names.add(c.getColumnName());
            }
            return names;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<Data2DColumn> toColumns(List<String> names) {
        try {
            if (names == null) {
                return null;
            }
            List<Data2DColumn> cols = new ArrayList<>();
            int index = -1;
            for (String c : names) {
                Data2DColumn col = new Data2DColumn(c, ColumnDefinition.ColumnType.String);
                col.setIndex(index--);
                cols.add(col);
            }
            return cols;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static StringTable validate(List<Data2DColumn> columns) {
        try {
            if (columns == null || columns.isEmpty()) {
                return null;
            }
            List<String> colsNames = new ArrayList<>();
            List<String> tNames = new ArrayList<>();
            tNames.addAll(Arrays.asList(message("ID"), message("Name"), message("Reason")));
            StringTable colsTable = new StringTable(tNames, message("InvalidColumns"));
            for (int c = 0; c < columns.size(); c++) {
                Data2DColumn column = columns.get(c);
                if (!column.valid()) {
                    List<String> row = new ArrayList<>();
                    row.addAll(Arrays.asList(c + 1 + "", column.getColumnName(), message("Invalid")));
                    colsTable.add(row);
                }
                if (colsNames.contains(column.getColumnName())) {
                    List<String> row = new ArrayList<>();
                    row.addAll(Arrays.asList(c + 1 + "", column.getColumnName(), message("Duplicated")));
                    colsTable.add(row);
                }
                colsNames.add(column.getColumnName());
            }
            return colsTable;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String columnInfo(Data2DColumn column) {
        try {
            if (column == null) {
                return null;
            }
            StringBuilder s = new StringBuilder();
            s.append("D2cid").append(": ").append(column.getD2cid()).append("\n");
            s.append("D2id").append(": ").append(column.getD2id()).append("\n");
            s.append(ColumnDefinition.info(column));
            return s.toString();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String dataInfo(Data2D data2D) {
        try {
            if (data2D == null) {
                return null;
            }
            StringBuilder s = new StringBuilder();
            for (Data2DColumn column : data2D.getColumns()) {
                s.append(Data2DColumnTools.columnInfo(column));
                s.append("----------------------------------");
            }
            return s.toString();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String majorAttributes(Data2DColumn column) {
        try {
            if (column == null) {
                return null;
            }
            return column.getIndex() + " " + column.getColumnName() + " " + column.getType();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
