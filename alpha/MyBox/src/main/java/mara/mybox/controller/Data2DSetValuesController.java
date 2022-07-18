package mara.mybox.controller;

import java.util.List;
import java.util.Random;
import javafx.fxml.FXML;
import mara.mybox.calculation.DescriptiveStatistic;
import mara.mybox.calculation.DoubleStatistic;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ExpressionCalculator;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public class Data2DSetValuesController extends BaseData2DHandleController {

    @FXML
    protected ControlData2DSetValue valueController;

    public Data2DSetValuesController() {
        baseTitle = message("SetValues");
    }

    @Override
    public void setParameters(ControlData2DEditTable tableController) {
        try {
            super.setParameters(tableController);

            idExclude(true);
            valueController.setParameter(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void sourceChanged() {
        super.sourceChanged();
        valueController.setData2D(data2D);
    }

    @Override
    public void refreshControls() {
        try {
            if (data2D == null) {
                return;
            }
            if (data2D.isMutiplePages()) {
                allPagesRadio.setDisable(false);
            } else {
                if (allPagesRadio.isSelected()) {
                    currentPageRadio.fire();
                }
                allPagesRadio.setDisable(true);
            }
            showPaginationPane(false);
            checkOptions();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        boolean ok = super.checkOptions();
        ok = valueController.checkSelection() && ok;
        okButton.setDisable(!ok);
        return ok;
    }

    @Override
    public void handleAllTask() {
        if (!tableController.checkBeforeNextAction()) {
            return;
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    if (!data2D.isTmpData() && tableController.dataController.backupController != null
                            && tableController.dataController.backupController.isBack()) {
                        tableController.dataController.backupController.addBackup(task, data2D.getFile());
                    }
                    data2D.startTask(task, rowFilterController.rowFilter);
                    ok = data2D.setValue(checkedColsIndices, valueController.value, valueController.errorContinueCheck.isSelected());
                    data2D.stopFilter();
                    return ok;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                tableController.dataController.goPage();
                tableController.requestMouse();
                tableController.popDone();
                tabPane.getSelectionModel().select(dataTab);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                task = null;
                valueController.expressionController.calculator.stop();
            }

        };
        start(task);
    }

    @Override
    public void handleRowsTask() {
        try {
            if (valueController.columnMeanRadio.isSelected()) {
                setRowsMean();
                return;
            }
            tableController.isSettingValues = true;
            boolean ok;
            if (valueController.gaussianDistributionRadio.isSelected()) {
                ok = gaussianDistribution();
            } else if (valueController.identifyRadio.isSelected()) {
                ok = identifyMatrix();
            } else if (valueController.upperTriangleRadio.isSelected()) {
                ok = upperTriangleMatrix();
            } else if (valueController.lowerTriangleRadio.isSelected()) {
                ok = lowerTriangleMatrix();
            } else {
                ok = setValue();
            }
            if (ok) {
                tableController.tableView.refresh();
                popDone();
            }
            tableController.isSettingValues = false;
            tableController.tableChanged(true);
            tableController.requestMouse();
            tabPane.getSelectionModel().select(dataTab);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
        }
    }

    public void setRowsMean() {
        if (!tableController.checkBeforeNextAction()) {
            return;
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(task, rowFilterController.rowFilter);
                    ok = data2D.setValue(checkedColsIndices, valueController.value, valueController.errorContinueCheck.isSelected());
                    data2D.stopFilter();
                    return ok;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                tableController.dataController.goPage();
                tableController.requestMouse();
                tableController.popDone();
                tabPane.getSelectionModel().select(dataTab);
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

    public DescriptiveStatistic mean() {
        try {
            DescriptiveStatistic calculation = new DescriptiveStatistic()
                    .setMean(true).setScale(4)
                    .setStatisticObject(DescriptiveStatistic.StatisticObject.Columns)
                    .setHandleController(this).setData2D(data2D)
                    .setColsIndices(checkedColsIndices)
                    .setColsNames(checkedColsNames);
            if (data2D.isMutiplePages()) {

            } else {
                int rowsNumber = tableData.size();
                for (int col : checkedColsIndices) {
                    String[] colData = new String[rowsNumber];
                    for (int r = 0; r < rowsNumber; r++) {
                        colData[r] = tableData.get(r).get(col + 1);
                    }
                    DoubleStatistic statistic = new DoubleStatistic(colData, calculation);
                    data2D.column(col).setDoubleStatistic(statistic);
                }
            }
            return calculation;
        } catch (Exception e) {
            error = e.toString();
            return null;
        }
    }

    public boolean setValue() {
        try {
            Random random = new Random();
            String script = valueController.expressionController.scriptInput.getText();
            ExpressionCalculator calculator = valueController.expressionController.calculator;
            for (int row : checkedRowsIndices) {
                List<String> values = tableController.tableData.get(row);
                String v = valueController.value;
                if (valueController.blankRadio.isSelected()) {
                    v = "";
                } else if (valueController.expressionRadio.isSelected()) {
                    if (!calculator.calculateTableRowExpression(data2D, script, values, row)) {
                        if (valueController.errorContinueCheck.isSelected()) {
                            continue;
                        } else {
                            if (data2D.getError() != null) {
                                popError(data2D.getError());
                            }
                            return false;
                        }
                    }
                    v = calculator.getResult();
                }
                for (int col : checkedColsIndices) {
                    if (valueController.randomRadio.isSelected()) {
                        v = tableController.data2D.random(random, col, false);
                    } else if (valueController.randomNnRadio.isSelected()) {
                        v = tableController.data2D.random(random, col, true);
                    }
                    values.set(col + 1, v);
                }
                tableController.tableData.set(row, values);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
            return false;
        }
    }

    public boolean gaussianDistribution() {
        try {
            float[][] m = ConvolutionKernel.makeGaussMatrix((int) checkedRowsIndices.size() / 2);
            int rowIndex = 0, colIndex;
            for (int row : checkedRowsIndices) {
                List<String> tableRow = tableController.tableData.get(row);
                colIndex = 0;
                for (int col : checkedColsIndices) {
                    try {
                        tableRow.set(col + 1, DoubleTools.format(m[rowIndex][colIndex], scale));
                    } catch (Exception e) {
                    }
                    colIndex++;
                }
                tableController.tableData.set(row, tableRow);
                rowIndex++;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
            return false;
        }
    }

    public boolean identifyMatrix() {
        try {
            int rowIndex = 0, colIndex;
            for (int row : checkedRowsIndices) {
                List<String> values = tableController.tableData.get(row);
                colIndex = 0;
                for (int col : checkedColsIndices) {
                    if (rowIndex == colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                tableController.tableData.set(row, values);
                rowIndex++;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
            return false;
        }
    }

    public boolean upperTriangleMatrix() {
        try {
            int rowIndex = 0, colIndex;
            for (int row : checkedRowsIndices) {
                List<String> values = tableController.tableData.get(row);
                colIndex = 0;
                for (int col : checkedColsIndices) {
                    if (rowIndex <= colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                tableController.tableData.set(row, values);
                rowIndex++;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
            return false;
        }
    }

    public boolean lowerTriangleMatrix() {
        try {
            int rowIndex = 0, colIndex;
            for (int row : checkedRowsIndices) {
                List<String> values = tableController.tableData.get(row);
                colIndex = 0;
                for (int col : checkedColsIndices) {
                    if (rowIndex >= colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                tableController.tableData.set(row, values);
                rowIndex++;
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
    public static Data2DSetValuesController open(ControlData2DEditTable tableController) {
        try {
            Data2DSetValuesController controller = (Data2DSetValuesController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DSetValuesFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
