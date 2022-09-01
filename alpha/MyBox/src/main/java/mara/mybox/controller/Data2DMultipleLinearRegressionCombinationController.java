package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import mara.mybox.calculation.OLSLinearRegression;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-8-19
 * @License Apache License Version 2.0
 */
public class Data2DMultipleLinearRegressionCombinationController extends Data2DMultipleLinearRegressionController {

    protected ObservableList<List<String>> results;
    protected Map<String, List<String>> namesMap;

    @FXML
    protected ControlData2DMultipleLinearRegressionTable resultsController;

    public Data2DMultipleLinearRegressionCombinationController() {
        baseTitle = message("MultipleLinearRegressionCombination");
        TipsLabelKey = "MultipleLinearRegressionTips";
        defaultScale = 8;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            resultsController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        resultsController.clear();
        namesMap = new HashMap<>();
        task = new SingletonTask<Void>(this) {

            List<List<String>> data;

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(task, filterController.filter);
                    if (isAllPages()) {
                        data = data2D.allRows(dataColsIndices, false);
                    } else {
                        data = filtered(dataColsIndices, false);
                    }
                    data2D.stopFilter();
                    if (data == null || data.isEmpty()) {
                        error = message("NoData");
                        return false;
                    }
                    int size = xNames.size();
                    for (int i = 0; i < size; i++) {
                        for (int j = i + 1; j <= size; j++) {
                            List<String> x = xNames.subList(i, j);
                            regress(x);
                        }
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            protected void regress(List<String> x) {
                try {
                    regression = new OLSLinearRegression(interceptCheck.isSelected())
                            .setTask(task).setScale(scale)
                            .setInvalidAs(invalidAs)
                            .setyName(yName).setxNames(x);
                    regression.calculate(data);

                    List<String> row = new ArrayList<>();
                    String namesString = x.toString();
                    namesMap.put(namesString, x);
                    row.add(namesString);
                    row.add(Arrays.toString(regression.getCoefficients()));
                    row.add(DoubleTools.format(regression.getrSqure(), scale));
                    row.add(DoubleTools.format(regression.getAdjustedRSqure(), scale));
                    row.add(DoubleTools.format(regression.getIntercept(), scale));

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            resultsController.addRow(row);
                        }
                    });

                } catch (Exception e) {
                    error = e.toString();
                }
            }

            @Override
            protected void whenSucceeded() {
                resultsController.afterRegression();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                task = null;
            }

        };
        start(task);

    }

    @FXML
    @Override
    public void dataAction() {
        resultsController.dataAction();
    }

    @FXML
    @Override
    public void viewAction() {
        resultsController.editAction();
    }


    /*
        static
     */
    public static Data2DMultipleLinearRegressionCombinationController open(ControlData2DEditTable tableController) {
        try {
            Data2DMultipleLinearRegressionCombinationController controller = (Data2DMultipleLinearRegressionCombinationController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DMultipleLinearRegressionCombinationFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
