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
                popError(message("Invalid") + ": " + message("RowExpression"));
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
                    count = data2D.setValue(this, checkedColsIndices, valueController.setValue,
                            valueController.errorContinueCheck.isSelected());
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
    public void ouputRows() {
        try {
            dataController.isSettingValues = true;
            if (valueController.randomRadio.isSelected()) {
                random(false);
            } else if (valueController.randomNnRadio.isSelected()) {
                random(true);
            } else if (valueController.scaleRadio.isSelected()) {
                scale();
            } else if (valueController.prefixRadio.isSelected()) {
                prefix();
            } else if (valueController.suffixRadio.isSelected()) {
                suffix();
            } else if (valueController.numberRadio.isSelected()) {
                number();
            } else if (valueController.expressionRadio.isSelected()) {
                expression();
            } else if (valueController.gaussianDistributionRadio.isSelected()) {
                gaussianDistribution();
            } else if (valueController.identifyRadio.isSelected()) {
                identifyMatrix();
            } else if (valueController.upperTriangleRadio.isSelected()) {
                upperTriangleMatrix();
            } else if (valueController.lowerTriangleRadio.isSelected()) {
                lowerTriangleMatrix();
            } else {
                setValue(valueController.value());
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(message(e.toString()));
        }
        dataController.isSettingValues = false;
    }

    public boolean updateTable(List<List<String>> data) {
        dataController.updateTable(data);
        dataController.isSettingValues = true;
        dataController.tableView.refresh();
        dataController.isSettingValues = false;
        dataController.tableChanged(true);
        dataController.requestMouse();
        dataController.alertInformation(message("ChangedRowsNumber") + ": "
                + sourceController.filteredRowsIndices.size());
        tabPane.getSelectionModel().select(sourceTab);
        return true;
    }

    public void setValue(String value) {
        try {
            List<List<String>> tableData = new ArrayList<>();
            tableData.addAll(dataController.tableData);
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = dataController.tableData.get(row);
                for (int col : checkedColsIndices) {
                    values.set(col + 1, value);
                }
                tableData.set(row, values);
            }
            updateTable(tableData);
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(message(e.toString()));
        }
    }

    public void random(boolean nonNegative) {
        try {
            List<List<String>> tableData = new ArrayList<>();
            tableData.addAll(dataController.tableData);
            Random random = new Random();
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = dataController.tableData.get(row);
                for (int col : checkedColsIndices) {
                    String v = dataController.data2D.random(random, col, nonNegative);
                    values.set(col + 1, v);
                }
                tableData.set(row, values);
            }
            updateTable(tableData);
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(message(e.toString()));
        }
    }

    public void scale() {
        try {
            List<List<String>> tableData = new ArrayList<>();
            tableData.addAll(dataController.tableData);
            String currentValue;
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = dataController.tableData.get(row);
                for (int col : checkedColsIndices) {
                    currentValue = values.get(col + 1);
                    values.set(col + 1, valueController.scale(currentValue));
                }
                tableData.set(row, values);
            }
            updateTable(tableData);
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(message(e.toString()));
        }
    }

    public void prefix() {
        try {
            String prefix = valueController.value();
            if (prefix == null) {
                popError(message("Invalid") + ": " + message("AddPrefix"));
                return;
            }
            List<List<String>> tableData = new ArrayList<>();
            tableData.addAll(dataController.tableData);
            String currentValue;
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = dataController.tableData.get(row);
                for (int col : checkedColsIndices) {
                    currentValue = values.get(col + 1);
                    values.set(col + 1, currentValue == null ? prefix : prefix + currentValue);
                }
                tableData.set(row, values);
            }
            updateTable(tableData);
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(message(e.toString()));
        }
    }

    public void suffix() {
        try {
            String suffix = valueController.value();
            if (suffix == null) {
                popError(message("Invalid") + ": " + message("AppendSuffix"));
                return;
            }
            List<List<String>> tableData = new ArrayList<>();
            tableData.addAll(dataController.tableData);
            String currentValue;
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = dataController.tableData.get(row);
                for (int col : checkedColsIndices) {
                    currentValue = values.get(col + 1);
                    values.set(col + 1, currentValue == null ? suffix : currentValue + suffix);
                }
                tableData.set(row, values);
            }
            updateTable(tableData);
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(message(e.toString()));
        }
    }

    public void number() {
        try {
            List<List<String>> tableData = new ArrayList<>();
            tableData.addAll(dataController.tableData);
            int num = valueController.setValue.getStart();
            int digit = valueController.setValue.countFinalDigit(sourceController.filteredRowsIndices.size());
            String currentValue, suffix;
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = dataController.tableData.get(row);
                suffix = StringTools.fillLeftZero(num++, digit);
                for (int col : checkedColsIndices) {
                    currentValue = values.get(col + 1);
                    values.set(col + 1, currentValue == null ? suffix : currentValue + suffix);
                }
                tableData.set(row, values);
            }
            updateTable(tableData);
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(message(e.toString()));
        }
    }

    public void expression() {
        try {
            List<List<String>> tableData = new ArrayList<>();
            tableData.addAll(dataController.tableData);
            String script = valueController.value();
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = dataController.tableData.get(row);
                if (!data2D.calculateTableRowExpression(script, values, row)) {
                    error = data2D.getError();
                    if (valueController.errorContinueCheck.isSelected()) {
                        if (error != null) {
                            MyBoxLog.console(error);
                        }
                        continue;
                    } else {
                        if (error != null) {
                            popError(error);
                        }
                        return;
                    }
                }
                String v = data2D.expressionResult();
                for (int col : checkedColsIndices) {
                    values.set(col + 1, v);
                }
                tableData.set(row, values);
            }
            updateTable(tableData);
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(message(e.toString()));
        }
    }

    public void gaussianDistribution() {
        try {
            if (sourceController.filteredRowsIndices.size() != checkedColsIndices.size()) {
                popError(message("MatricesCannotCalculateShouldSqure"));
                return;
            }
            if (sourceController.filteredRowsIndices.size() % 2 == 0) {
                popError(message("MatricesCannotCalculateShouldOdd"));
                return;
            }
            List<List<String>> tableData = new ArrayList<>();
            tableData.addAll(dataController.tableData);
            float[][] m = ConvolutionKernel.makeGaussMatrix((int) sourceController.filteredRowsIndices.size() / 2);
            int rowIndex = 0, colIndex;
            for (int row : sourceController.filteredRowsIndices) {
                List<String> tableRow = dataController.tableData.get(row);
                colIndex = 0;
                for (int col : checkedColsIndices) {
                    try {
                        tableRow.set(col + 1, NumberTools.format(m[rowIndex][colIndex], scale));
                    } catch (Exception e) {
                    }
                    colIndex++;
                }
                tableData.set(row, tableRow);
                rowIndex++;
            }
            updateTable(tableData);
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(message(e.toString()));
        }
    }

    public void identifyMatrix() {
        try {
            if (sourceController.filteredRowsIndices.size() != checkedColsIndices.size()) {
                popError(message("MatricesCannotCalculateShouldSqure"));
                return;
            }
            List<List<String>> tableData = new ArrayList<>();
            tableData.addAll(dataController.tableData);
            int rowIndex = 0, colIndex;
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = dataController.tableData.get(row);
                colIndex = 0;
                for (int col : checkedColsIndices) {
                    if (rowIndex == colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                tableData.set(row, values);
                rowIndex++;
            }
            updateTable(tableData);
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(message(e.toString()));
        }
    }

    public void upperTriangleMatrix() {
        try {
            if (sourceController.filteredRowsIndices.size() != checkedColsIndices.size()) {
                popError(message("MatricesCannotCalculateShouldSqure"));
                return;
            }
            List<List<String>> tableData = new ArrayList<>();
            tableData.addAll(dataController.tableData);
            int rowIndex = 0, colIndex;
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = dataController.tableData.get(row);
                colIndex = 0;
                for (int col : checkedColsIndices) {
                    if (rowIndex <= colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                tableData.set(row, values);
                rowIndex++;
            }
            updateTable(tableData);
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(message(e.toString()));
        }
    }

    public void lowerTriangleMatrix() {
        try {
            if (sourceController.filteredRowsIndices.size() != checkedColsIndices.size()) {
                popError(message("MatricesCannotCalculateShouldSqure"));
                return;
            }
            List<List<String>> tableData = new ArrayList<>();
            tableData.addAll(dataController.tableData);
            int rowIndex = 0, colIndex;
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = dataController.tableData.get(row);
                colIndex = 0;
                for (int col : checkedColsIndices) {
                    if (rowIndex >= colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                tableData.set(row, values);
                rowIndex++;
            }
            updateTable(tableData);
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(message(e.toString()));
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
