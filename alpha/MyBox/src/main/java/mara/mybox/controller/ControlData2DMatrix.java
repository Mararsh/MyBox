package mara.mybox.controller;

import java.util.List;
import mara.mybox.data2d.Data2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2DMatrix extends ControlData2DSource {

    @Override
    public void initControls() {
        try {
            super.initControls();

            createData(Data2D.DataType.CSV);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public double[][] pickMatrix(FxTask task) {
        List<List<String>> data = selectedData(task, checkedColsIndices, false);
        if (data == null || data.isEmpty()) {
            return null;
        }
        int rowsNumber = data.size();
        int colsNumber = data.get(0).size();
        if (rowsNumber <= 0 || colsNumber <= 0) {
            return null;
        }
        double[][] matrix = new double[(int) rowsNumber][(int) colsNumber];
        for (int r = 0; r < rowsNumber; r++) {
            List<String> row = data.get(r);
            for (int c = 0; c < row.size(); c++) {
                try {
                    matrix[r][c] = Double.parseDouble(row.get(c).replaceAll(",", ""));
                } catch (Exception e) {
                }
            }
        }
        return matrix;
    }

    protected boolean isSquare(double[][] data) {
        if (data == null || data.length == 0) {
            return false;
        }
        return data.length == data[0].length;
    }

}
