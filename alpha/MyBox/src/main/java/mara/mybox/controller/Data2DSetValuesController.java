package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.NumberTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public class Data2DSetValuesController extends BaseData2DTaskTargetsController {

    protected Data2DManufactureController editor;

    @FXML
    protected ControlData2DSetValue valueController;
    @FXML
    protected Tab valuesTab;

    public Data2DSetValuesController() {
        baseTitle = message("SetValues");
    }

    public void setParameters(Data2DManufactureController dataController) {
        try {
            editor = dataController;
            super.setParameters(dataController);

            idExclude = true;
            noCheckedColumnsMeansAll = false;

            valueController.setParameter(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void sourceChanged() {
        super.sourceChanged();
        valueController.setData2D(data2D);
    }

    @Override
    public boolean checkOptions() {
        try {
            if (!super.checkOptions() || !valueController.checkSelection()) {
                return false;
            }
            return PopTools.askSure(getTitle(), message("SureOverwriteColumns") + "\n" + checkedColsNames);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void preprocessStatistic() {
        List<String> scripts = new ArrayList<>();
        String filterScript = data2D.filterScipt();
        if (filterScript != null && !filterScript.isBlank()) {
            scripts.add(filterScript);
            updateLogs(message("Filter") + ": " + filterScript, true);
        }
        if (valueController.expressionRadio.isSelected()) {
            String expression = valueController.value();
            if (expression == null || expression.isBlank()) {
                setError(message("Invalid") + ": " + message("RowExpression"));
                return;
            }
            scripts.add(expression);
            updateLogs(message("Expression") + ": " + expression, true);
        }
        if (scripts.isEmpty()) {
            startOperation();
            return;
        }
        updateLogs(message("Statistic") + " ... ", true);
        if (task != null) {
            task.cancel();
        }
        taskSuccessed = false;
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(this);
                    List<String> filled = data2D.calculateScriptsStatistic(scripts);
                    if (filled == null) {
                        return false;
                    }
                    String filledExp = null;
                    if (filterScript != null && !filterScript.isBlank()) {
                        String filledScript = filled.get(0);
                        if (filledScript == null || filledScript.isBlank()) {
                            error = message("Invalid") + ": " + message("RowFilter");
                            return false;
                        }
                        data2D.filter.setFilledScript(filledScript);
                        if (valueController.expressionRadio.isSelected()) {
                            filledExp = filled.get(1);
                        }
                    } else if (valueController.expressionRadio.isSelected()) {
                        filledExp = filled.get(0);
                    }
                    if (valueController.expressionRadio.isSelected()) {
                        if (filledExp == null || filledExp.isBlank()) {
                            error = message("Invalid") + ": " + message("RowExpression");
                            return false;
                        }
                        valueController.setValue(filledExp);
                    }
                    taskSuccessed = true;
                    return taskSuccessed;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.setTask(null);
                if (taskSuccessed) {
                    updateLogs(baseTitle + " ... ", true);
                    startOperation();
                } else {
                    closeTask(ok);
                }
            }

        };
        start(task, false);
    }

    @Override
    public void handleAllTask() {
        if (task != null) {
            task.cancel();
        }
        taskSuccessed = false;
        task = new FxSingletonTask<Void>(this) {

            private long count;

            @Override
            protected boolean handle() {
                try {
                    if (data2D.needBackup()) {
                        addBackup(this, data2D.getFile());
                    }
                    data2D.startTask(this, filterController.filter);
                    count = data2D.setValue(this, checkedColsIndices, valueController.setValue);
                    data2D.stopFilter();
                    taskSuccessed = count >= 0;
                    return taskSuccessed;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                dataController.data2D.cloneData(data2D);
                dataController.goPage();
                dataController.requestMouse();
                dataController.alertInformation(message("ChangedRowsNumber") + ": " + count);
                tabPane.getSelectionModel().select(sourceTab);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                valueController.expressionController.calculator.reset();
                closeTask(ok);
            }

        };
        start(task, false);
    }

    @Override
    public boolean handleRows() {
        try {
            if (valueController.randomRadio.isSelected()) {
                return random(false);
            } else if (valueController.randomNnRadio.isSelected()) {
                return random(true);
            } else if (valueController.scaleRadio.isSelected()) {
                return scale();
            } else if (valueController.prefixRadio.isSelected()) {
                return prefix();
            } else if (valueController.suffixRadio.isSelected()) {
                return suffix();
            } else if (valueController.numberRadio.isSelected()) {
                return number();
            } else if (valueController.expressionRadio.isSelected()) {
                return expression();
            } else if (valueController.gaussianDistributionRadio.isSelected()) {
                return gaussianDistribution();
            } else if (valueController.identifyRadio.isSelected()) {
                return identifyMatrix();
            } else if (valueController.upperTriangleRadio.isSelected()) {
                return upperTriangleMatrix();
            } else if (valueController.lowerTriangleRadio.isSelected()) {
                return lowerTriangleMatrix();
            } else {
                return setValue(valueController.value());
            }
        } catch (Exception e) {
            setError(e.toString());
            return false;
        }
    }

    public boolean setValue(String value) {
        try {
            outputData.addAll(sourceController.tableData);
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = sourceController.tableData.get(row);
                for (int col : checkedColsIndices) {
                    values.set(col + 1, value);
                }
                outputData.set(row, values);
            }
            return true;
        } catch (Exception e) {
            setError(e.toString());
            outputData = null;
            return false;
        }
    }

    public boolean random(boolean nonNegative) {
        try {
            outputData = new ArrayList<>();
            outputData.addAll(sourceController.tableData);
            Random random = new Random();
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = sourceController.tableData.get(row);
                for (int col : checkedColsIndices) {
                    String v = data2D.random(random, col, nonNegative);
                    values.set(col + 1, v);
                }
                outputData.set(row, values);
            }
            return true;
        } catch (Exception e) {
            setError(e.toString());
            outputData = null;
            return false;
        }
    }

    public boolean scale() {
        try {
            outputData = new ArrayList<>();
            outputData.addAll(sourceController.tableData);
            String currentValue;
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = sourceController.tableData.get(row);
                for (int col : checkedColsIndices) {
                    currentValue = values.get(col + 1);
                    values.set(col + 1, valueController.scale(currentValue));
                }
                outputData.set(row, values);
            }
            return true;
        } catch (Exception e) {
            setError(e.toString());
            outputData = null;
            return false;
        }
    }

    public boolean prefix() {
        try {
            String prefix = valueController.value();
            if (prefix == null) {
                setError(message("Invalid") + ": " + message("AddPrefix"));
                return false;
            }
            outputData = new ArrayList<>();
            outputData.addAll(sourceController.tableData);
            String currentValue;
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = sourceController.tableData.get(row);
                for (int col : checkedColsIndices) {
                    currentValue = values.get(col + 1);
                    values.set(col + 1, currentValue == null ? prefix : prefix + currentValue);
                }
                outputData.set(row, values);
            }
            return true;
        } catch (Exception e) {
            setError(e.toString());
            outputData = null;
            return false;
        }
    }

    public boolean suffix() {
        try {
            String suffix = valueController.value();
            if (suffix == null) {
                setError(message("Invalid") + ": " + message("AppendSuffix"));
                return false;
            }
            outputData = new ArrayList<>();
            outputData.addAll(sourceController.tableData);
            String currentValue;
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = sourceController.tableData.get(row);
                for (int col : checkedColsIndices) {
                    currentValue = values.get(col + 1);
                    values.set(col + 1, currentValue == null ? suffix : currentValue + suffix);
                }
                outputData.set(row, values);
            }
            return true;
        } catch (Exception e) {
            setError(e.toString());
            outputData = null;
            return false;
        }
    }

    public boolean number() {
        try {
            outputData = new ArrayList<>();
            outputData.addAll(sourceController.tableData);
            int num = valueController.setValue.getStart();
            int digit = valueController.setValue.countFinalDigit(sourceController.filteredRowsIndices.size());
            String currentValue, suffix;
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = sourceController.tableData.get(row);
                suffix = StringTools.fillLeftZero(num++, digit);
                for (int col : checkedColsIndices) {
                    currentValue = values.get(col + 1);
                    values.set(col + 1, currentValue == null ? suffix : currentValue + suffix);
                }
                outputData.set(row, values);
            }
            return true;
        } catch (Exception e) {
            setError(message(e.toString()));
            outputData = null;
            return false;
        }
    }

    public boolean expression() {
        try {
            outputData = new ArrayList<>();
            outputData.addAll(sourceController.tableData);
            String script = valueController.value();
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = sourceController.tableData.get(row);
                if (!data2D.calculateTableRowExpression(script, values, row)) {
                    setError(data2D.expressionError());
                    return false;
                }
                String v = data2D.expressionResult();
                for (int col : checkedColsIndices) {
                    values.set(col + 1, v);
                }
                outputData.set(row, values);
            }
            return true;
        } catch (Exception e) {
            setError(e.toString());
            outputData = null;
            return false;
        }
    }

    public boolean gaussianDistribution() {
        try {
            if (sourceController.filteredRowsIndices.size() != checkedColsIndices.size()) {
                setError(message("MatricesCannotCalculateShouldSqure"));
                return false;
            }
            if (sourceController.filteredRowsIndices.size() % 2 == 0) {
                setError(message("MatricesCannotCalculateShouldOdd"));
                return false;
            }
            outputData = new ArrayList<>();
            outputData.addAll(sourceController.tableData);
            float[][] m = ConvolutionKernel.makeGaussMatrix((int) sourceController.filteredRowsIndices.size() / 2);
            int rowIndex = 0, colIndex;
            for (int row : sourceController.filteredRowsIndices) {
                List<String> tableRow = sourceController.tableData.get(row);
                colIndex = 0;
                for (int col : checkedColsIndices) {
                    try {
                        tableRow.set(col + 1, NumberTools.format(m[rowIndex][colIndex], scale));
                    } catch (Exception e) {
                    }
                    colIndex++;
                }
                outputData.set(row, tableRow);
                rowIndex++;
            }
            return true;
        } catch (Exception e) {
            setError(e.toString());
            outputData = null;
            return false;
        }
    }

    public boolean identifyMatrix() {
        try {
            if (sourceController.filteredRowsIndices.size() != checkedColsIndices.size()) {
                setError(message("MatricesCannotCalculateShouldSqure"));
                return false;
            }
            outputData = new ArrayList<>();
            outputData.addAll(sourceController.tableData);
            int rowIndex = 0, colIndex;
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = sourceController.tableData.get(row);
                colIndex = 0;
                for (int col : checkedColsIndices) {
                    if (rowIndex == colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                outputData.set(row, values);
                rowIndex++;
            }
            return true;
        } catch (Exception e) {
            setError(e.toString());
            outputData = null;
            return false;
        }
    }

    public boolean upperTriangleMatrix() {
        try {
            if (sourceController.filteredRowsIndices.size() != checkedColsIndices.size()) {
                setError(message("MatricesCannotCalculateShouldSqure"));
                return false;
            }
            outputData = new ArrayList<>();
            outputData.addAll(sourceController.tableData);
            int rowIndex = 0, colIndex;
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = sourceController.tableData.get(row);
                colIndex = 0;
                for (int col : checkedColsIndices) {
                    if (rowIndex <= colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                outputData.set(row, values);
                rowIndex++;
            }
            return true;
        } catch (Exception e) {
            setError(e.toString());
            outputData = null;
            return false;
        }
    }

    public boolean lowerTriangleMatrix() {
        try {
            if (sourceController.filteredRowsIndices.size() != checkedColsIndices.size()) {
                setError(message("MatricesCannotCalculateShouldSqure"));
                return false;
            }
            outputData = new ArrayList<>();
            outputData.addAll(sourceController.tableData);
            int rowIndex = 0, colIndex;
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = sourceController.tableData.get(row);
                colIndex = 0;
                for (int col : checkedColsIndices) {
                    if (rowIndex >= colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                outputData.set(row, values);
                rowIndex++;
            }
            return true;
        } catch (Exception e) {
            setError(e.toString());
            outputData = null;
            return false;
        }
    }

    @Override
    public void ouputRows() {
        try {
            dataController.updateTable(outputData);
            dataController.tableChanged(true);
            dataController.requestMouse();
            dataController.alertInformation(message("ChangedRowsNumber") + ": "
                    + sourceController.filteredRowsIndices.size());
            tabPane.getSelectionModel().select(sourceTab);
        } catch (Exception e) {
            setError(message(e.toString()));
        }
    }

    /*
        static
     */
    public static Data2DSetValuesController open(Data2DManufactureController dataController) {
        try {
            Data2DSetValuesController controller = (Data2DSetValuesController) WindowTools.branchStage(
                    dataController, Fxmls.Data2DSetValuesFxml);
            controller.setParameters(dataController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
