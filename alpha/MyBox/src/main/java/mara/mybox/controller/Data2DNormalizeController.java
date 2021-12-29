package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
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
    public void handleFileTask() {
        popError(message("NotSupport"));
    }

    @Override
    public boolean handleRows() {
        try {
            List<Integer> checkedRowsIndices = tableController.checkedRowsIndices(false);
            List<Integer> checkedColsIndices = tableController.checkedColsIndices();
            if (checkedRowsIndices == null || checkedRowsIndices.isEmpty()
                    || checkedColsIndices == null || checkedColsIndices.isEmpty()) {
                return false;
            }
            int rowsNumber = checkedRowsIndices.size();
            int colsNumber = checkedColsIndices.size();
            double[][] matrix = new double[rowsNumber][colsNumber];
            for (int r = 0; r < rowsNumber; r++) {
                int row = checkedRowsIndices.get(r);
                List<String> vs = new ArrayList<>();
                List<String> tableRow = tableController.tableData.get(row);
                for (int c = 0; c < colsNumber; c++) {
                    int col = checkedColsIndices.get(c);
                    matrix[r][c] = data2D.doubleValue(tableRow.get(col + 1));
                    vs.add(tableRow.get(col));
                }
            }
            matrix = normalizeController.calculate(matrix);
            if (matrix == null) {
                return false;
            }
            handledData = new ArrayList<>();
            if (showRowNumber()) {
                List<String> names = tableController.checkedColsNames();
                names.add(0, message("SourceRowNumber"));
                handledData.add(names);
            }
            int scale = data2D.getScale();
            for (int r = 0; r < rowsNumber; r++) {
                List<String> row = new ArrayList<>();
                if (showColNames()) {
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
