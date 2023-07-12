package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-21
 * @License Apache License Version 2.0
 */
public class ControlData2DMultipleLinearRegressionTable extends ControlData2DSimpleLinearRegressionTable {

    @Override
    public List<Data2DColumn> createColumns() {
        try {
            List<Data2DColumn> cols = new ArrayList<>();
            cols.add(new Data2DColumn(message("DependentVariable"), ColumnType.String, 100));
            cols.add(new Data2DColumn(message("IndependentVariable"), ColumnType.String, 200));
            cols.add(new Data2DColumn(message("AdjustedRSquared"), ColumnType.Double, 80));
            cols.add(new Data2DColumn(message("CoefficientOfDetermination"), ColumnType.Double, 80));
            cols.add(new Data2DColumn(message("Coefficients"), ColumnType.Double, 80));
            cols.add(new Data2DColumn(message("Intercept"), ColumnType.Double, 100));

            return cols;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    @Override
    public void editAction() {
        if (regressController == null) {
            return;
        }
        List<String> selected = selected();
        if (selected == null) {
            Data2DMultipleLinearRegressionController.open(regressController.tableController);
        } else {
            try {
                Data2DMultipleLinearRegressionController controller = (Data2DMultipleLinearRegressionController) WindowTools.openChildStage(
                        regressController.parentController.getMyWindow(), Fxmls.Data2DMultipleLinearRegressionFxml, false);
                controller.categoryColumnSelector.setValue(selected.get(1));
                List<Integer> cols = new ArrayList<>();
                List<String> names = ((Data2DMultipleLinearRegressionCombinationController) regressController).namesMap.get(selected.get(2));
                for (String name : names) {
                    cols.add(regressController.data2D.colOrder(name));
                }
                controller.checkedColsIndices = cols;
                controller.interceptCheck.setSelected(regressController.interceptCheck.isSelected());
                controller.cloneOptions(regressController);
                controller.setParameters(regressController.tableController);
                controller.okAction();
                controller.requestMouse();
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
        }
    }

}
