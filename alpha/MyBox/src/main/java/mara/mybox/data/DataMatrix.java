package mara.mybox.data;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class DataMatrix extends Data2D {

    public DataMatrix() {
        type = Type.Matrix;
    }

    public int type() {
        return type(Type.Matrix);
    }

    public boolean isTmpData() {
        return file == null || file.getAbsolutePath().startsWith(AppVariables.MyBoxTempPath.getAbsolutePath());
    }

    public boolean isSquare() {
        return rowsNumber == colsNumber;
    }

    public double[][] matrixDouble() {
        double[][] matrix = new double[(int) rowsNumber][(int) colsNumber];
        for (int j = 0; j < rowsNumber; j++) {
            for (int i = 0; i < colsNumber; i++) {
//                double d = cellDouble(j, i);
//                if (d != AppValues.InvalidDouble) {
//                    matrix[j][i] = d;
//                }
            }
        }
        return matrix;
    }

    @Override
    public Data2DDefinition queryDefinition(Connection conn) {
        return tableData2DDefinition.queryFile(conn, type, file);
    }

    @Override
    public void applyOptions() {
        try {
            if (options == null) {
                return;
            }
            if (options.containsKey("hasHeader")) {
                hasHeader = (boolean) (options.get("hasHeader"));
            }
        } catch (Exception e) {
        }
    }

    @Override
    public List<String> readColumns() {
        checkAttributes();
        List<String> names = new ArrayList<>();
        for (int i = 1; i <= colsNumber; i++) {
            names.add(colPrefix() + i);
        }
        return names;
    }

    @Override
    public long readTotal() {
        dataSize = rowsNumber;
        return dataSize;
    }

    @Override
    public List<List<String>> readPageData() {
        if (startRowOfCurrentPage < 0) {
            startRowOfCurrentPage = 0;
        }
        endRowOfCurrentPage = startRowOfCurrentPage;
        List<List<String>> rows = new ArrayList<>();
        try {

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
            return null;
        }
        endRowOfCurrentPage = startRowOfCurrentPage + rows.size();
        return rows;
    }

    @Override
    public boolean savePageData(Data2D targetData) {
        if (targetData == null || !(targetData instanceof DataMatrix)) {
            return false;
        }

        return true;
    }

    @Override
    public File tmpFile(List<String> cols, List<List<String>> data) {
        try {
            if (cols == null || cols.isEmpty()) {
                if (data == null || data.isEmpty()) {
                    return null;
                }
            }
            File tmpFile = TmpFileTools.getTempFile(".txt");
            return tmpFile;
        } catch (Exception e) {
            MyBoxLog.console(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
    }

}
