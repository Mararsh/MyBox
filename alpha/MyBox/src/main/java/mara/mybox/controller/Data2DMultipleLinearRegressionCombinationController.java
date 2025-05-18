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
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.NumberTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-8-19
 * @License Apache License Version 2.0
 */
public class Data2DMultipleLinearRegressionCombinationController extends BaseData2DRegressionController {

    protected ObservableList<List<String>> results;
    protected Map<String, List<String>> namesMap;
    protected OLSLinearRegression regression;
    protected List<String> names;

    @FXML
    protected ControlData2DMultipleLinearRegressionTable tableController;

    public Data2DMultipleLinearRegressionCombinationController() {
        baseTitle = message("MultipleLinearRegressionCombination");
        TipsLabelKey = "MultipleLinearRegressionCombinationTips";
        defaultScale = 8;
    }

    @Override
    public void initOptions() {
        try {
            super.initOptions();

            tableController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        try {
            if (!super.checkOptions()) {
                return false;
            }
            invalidAs = InvalidAs.Empty;

            dataColsIndices = new ArrayList<>();
            if (otherColsIndices == null || otherColsIndices.isEmpty()) {
                otherColsIndices = data2D.columnIndices();
            }
            dataColsIndices.addAll(otherColsIndices);
            if (checkedColsIndices == null || checkedColsIndices.isEmpty()) {
                checkedColsIndices = data2D.columnIndices();
            }
            dataColsIndices.addAll(checkedColsIndices);

            names = new ArrayList<>();
            for (int col : dataColsIndices) {
                names.add(data2D.columnName(col));
            }
            regression = null;
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        tableController.clear();
        namesMap = new HashMap<>();
        taskSuccessed = false;
        task = new FxSingletonTask<Void>(this) {

            List<List<String>> data;
            int n, xLen, yLen;

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(this);
                    data = filteredData(dataColsIndices, false);
                    if (data == null || data.isEmpty()) {
                        error = message("NoData");
                        return false;
                    }
                    n = data.size();
                    xLen = checkedColsIndices.size();
                    yLen = otherColsIndices.size();
                    List<Integer> xList = new ArrayList<>();
                    for (int i = yLen; i < dataColsIndices.size(); i++) {
                        xList.add(i);
                    }
                    for (int yIndex = 0; yIndex < yLen; yIndex++) {
                        for (int i = 0; i < xLen; i++) {
                            for (int j = i + 1; j <= xLen; j++) {
                                if (task == null || isCancelled()) {
                                    return false;
                                }
                                List<Integer> xIndices = xList.subList(i, j);
                                regress(yIndex, xIndices);
                            }
                        }
                    }
                    taskSuccessed = true;
                    return taskSuccessed;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            protected void regress(int yIndex, List<Integer> cIndices) {
                try {
                    String yName = names.get(yIndex);
                    List<String> xnames = new ArrayList<>();
                    List<Integer> xIndices = new ArrayList<>();
                    for (int i : cIndices) {
                        String name = names.get(i);
                        if (name.equals(yName)) {
                            continue;
                        }
                        xnames.add(name);
                        xIndices.add(i);
                    }
                    int k = xIndices.size();
                    if (k == 0) {
                        return;
                    }
                    String[] sy = new String[n];
                    String[][] sx = new String[n][k];
                    for (int r = 0; r < n; r++) {
                        List<String> row = data.get(r);
                        sy[r] = row.get(yIndex);
                        for (int c = 0; c < k; c++) {
                            if (task == null || isCancelled()) {
                                return;
                            }
                            sx[r][c] = row.get(xIndices.get(c));
                        }
                    }
                    regression = new OLSLinearRegression(interceptCheck.isSelected())
                            .setTask(this).setScale(scale)
                            .setInvalidAs(invalidAs)
                            .setyName(yName).setxNames(xnames);
                    regression.calculate(sy, sx);
                    List<String> row = new ArrayList<>();
                    String namesString = xnames.toString();
                    namesMap.put(namesString, xnames);
                    row.add(yName);
                    row.add(namesString);
                    row.add(NumberTools.format(regression.getAdjustedRSqure(), scale));
                    row.add(NumberTools.format(regression.getrSqure(), scale));
                    row.add(Arrays.toString(regression.getCoefficients()));
                    row.add(NumberTools.format(regression.getIntercept(), scale));

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (task == null || isCancelled()) {
                                return;
                            }
                            tableController.addRow(row);
                        }
                    });

                } catch (Exception e) {
                    error = e.toString();
                }
            }

            @Override
            protected void whenSucceeded() {
                tableController.afterRegression();
                rightPane.setDisable(false);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                closeTask(ok);
            }

        };
        start(task, false);
    }

    @FXML
    @Override
    public void dataAction() {
        tableController.dataAction();
    }

    @FXML
    @Override
    public void viewAction() {
        tableController.editAction();
    }


    /*
        static
     */
    public static Data2DMultipleLinearRegressionCombinationController open(BaseData2DLoadController tableController) {
        try {
            Data2DMultipleLinearRegressionCombinationController controller = (Data2DMultipleLinearRegressionCombinationController) WindowTools.referredStage(
                    tableController, Fxmls.Data2DMultipleLinearRegressionCombinationFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
