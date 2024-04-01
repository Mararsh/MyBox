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
import mara.mybox.value.UserConfig;

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
                    return true;
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
                if (ok) {
                    updateLogs(baseTitle + " ... ", true);
                    startOperation();
                } else {
                    closeTask();
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
        task = new FxSingletonTask<Void>(this) {

            private long count;

            @Override
            protected boolean handle() {
                try {
                    if (!data2D.isTmpData() && UserConfig.getBoolean(dataController.baseName + "BackupWhenSave", true)) {
                        addBackup(this, data2D.getFile());
                    }
                    data2D.startTask(this, filterController.filter);
                    count = data2D.setValue(this, checkedColsIndices, valueController.setValue,
                            valueController.errorContinueCheck.isSelected());
                    data2D.stopFilter();
                    return count >= 0;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                tabPane.getSelectionModel().select(sourceTab);
                dataController.data2D.cloneData(data2D);
                dataController.goPage();
                dataController.requestMouse();
                dataController.alertInformation(message("ChangedRowsNumber") + ": " + count);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                valueController.expressionController.calculator.reset();
                closeTask();
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

    @Override
    public boolean updateTable() {
        tabPane.getSelectionModel().select(sourceTab);
        dataController.isSettingValues = true;
        dataController.tableView.refresh();
        dataController.isSettingValues = false;
        dataController.tableChanged(true);
        dataController.requestMouse();
        dataController.alertInformation(message("ChangedRowsNumber") + ": "
                + sourceController.filteredRowsIndices.size());
        return true;
    }

    public void setValue(String value) {
        try {
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = dataController.tableData.get(row);
                for (int col : checkedColsIndices) {
                    values.set(col + 1, value);
                }
                dataController.tableData.set(row, values);
            }
            updateTable();
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(message(e.toString()));
        }
    }

    public void random(boolean nonNegative) {
        try {
            Random random = new Random();
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = dataController.tableData.get(row);
                for (int col : checkedColsIndices) {
                    String v = dataController.data2D.random(random, col, nonNegative);
                    values.set(col + 1, v);
                }
                dataController.tableData.set(row, values);
            }
            updateTable();
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(message(e.toString()));
        }
    }

    public void scale() {
        try {
            String currentValue;
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = dataController.tableData.get(row);
                for (int col : checkedColsIndices) {
                    currentValue = values.get(col + 1);
                    values.set(col + 1, valueController.scale(currentValue));
                }
                dataController.tableData.set(row, values);
            }
            updateTable();
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
            String currentValue;
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = dataController.tableData.get(row);
                for (int col : checkedColsIndices) {
                    currentValue = values.get(col + 1);
                    values.set(col + 1, currentValue == null ? prefix : prefix + currentValue);
                }
                dataController.tableData.set(row, values);
            }
            updateTable();
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
            String currentValue;
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = dataController.tableData.get(row);
                for (int col : checkedColsIndices) {
                    currentValue = values.get(col + 1);
                    values.set(col + 1, currentValue == null ? suffix : currentValue + suffix);
                }
                dataController.tableData.set(row, values);
            }
            updateTable();
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(message(e.toString()));
        }
    }

    public void number() {
        try {
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
                dataController.tableData.set(row, values);
            }
            updateTable();
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(message(e.toString()));
        }
    }

    public void expression() {
        try {
            String script = valueController.value();
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = dataController.tableData.get(row);
                if (!data2D.calculateTableRowExpression(script, values, row)) {
                    if (valueController.errorContinueCheck.isSelected()) {
                        continue;
                    } else {
                        if (data2D.getError() != null) {
                            popError(data2D.getError());
                        }
                        return;
                    }
                }
                String v = data2D.expressionResult();
                for (int col : checkedColsIndices) {
                    values.set(col + 1, v);
                }
                dataController.tableData.set(row, values);
            }
            updateTable();
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
                dataController.tableData.set(row, tableRow);
                rowIndex++;
            }
            updateTable();
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
                dataController.tableData.set(row, values);
                rowIndex++;
            }
            updateTable();
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
                dataController.tableData.set(row, values);
                rowIndex++;
            }
            updateTable();
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
                dataController.tableData.set(row, values);
                rowIndex++;
            }
            updateTable();
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
