package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.data.DataFileCSV;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-12-28
 * @License Apache License Version 2.0
 */
public class Data2DNormalizeController extends Data2DHandleController {

    @FXML
    protected ControlData2DNormalize normalizeController;

    @Override
    public boolean checkOptions() {
        boolean ok = super.checkOptions();
        targetController.setNotInTable(sourceController.allPages());
        if (sourceController.allPages()) {
            normalizeController.columnsRadio.fire();
            normalizeController.rowsRadio.setDisable(true);
            normalizeController.matrixRadio.setDisable(true);
        } else {
            normalizeController.rowsRadio.setDisable(false);
            normalizeController.matrixRadio.setDisable(false);
        }
        return ok;
    }

    @Override
    public boolean handleRows() {
        try {
            List<Integer> checkedRowsIndices = sourceController.checkedRowsIndices();
            List<Integer> checkedColsIndices = sourceController.checkedColsIndices();
            if (checkedRowsIndices == null || checkedRowsIndices.isEmpty()
                    || checkedColsIndices == null || checkedColsIndices.isEmpty()) {
                return false;
            }
            int rowsNumber = checkedRowsIndices.size();
            int colsNumber = checkedColsIndices.size();
            double[][] matrix = new double[rowsNumber][colsNumber];
            for (int r = 0; r < rowsNumber; r++) {
                int row = checkedRowsIndices.get(r);
                List<String> tableRow = editController.tableData.get(row);
                for (int c = 0; c < colsNumber; c++) {
                    int col = checkedColsIndices.get(c);
                    matrix[r][c] = data2D.doubleValue(tableRow.get(col + 1));
                }
            }
            matrix = normalizeController.calculate(matrix);
            if (matrix == null) {
                return false;
            }
            handledData = new ArrayList<>();
            if (showColNames()) {
                List<String> names = sourceController.checkedColsNames();
                if (showRowNumber()) {
                    names.add(0, message("SourceRowNumber"));
                }
                handledData.add(names);
            }
            int scale = data2D.getScale();
            for (int r = 0; r < rowsNumber; r++) {
                List<String> row = new ArrayList<>();
                if (showRowNumber()) {
                    row.add(checkedRowsIndices.get(r) + "");
                }
                for (int c = 0; c < colsNumber; c++) {
                    row.add(DoubleTools.format(matrix[r][c], scale));
                }
                handledData.add(row);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
            return false;
        }
    }

    @Override
    public DataFileCSV generatedFile() {
        if (normalizeController.minmaxRadio.isSelected()) {
            return data2D.normalizeMinMax(sourceController.checkedColsIndices,
                    normalizeController.from, normalizeController.to,
                    rowNumberCheck.isSelected(), colNameCheck.isSelected());

        } else if (normalizeController.l1Radio.isSelected()) {
            return data2D.normalizeSum(sourceController.checkedColsIndices,
                    rowNumberCheck.isSelected(), colNameCheck.isSelected());

        } else if (normalizeController.l2Radio.isSelected()) {
            return data2D.normalizeZscore(sourceController.checkedColsIndices,
                    rowNumberCheck.isSelected(), colNameCheck.isSelected());
        }
        return null;
    }

    /*
        static
     */
    public static Data2DNormalizeController open(ControlData2DEditTable tableController) {
        try {
            Data2DNormalizeController controller = (Data2DNormalizeController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DNormalizeFxml, false);
            controller.setParameters(tableController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
