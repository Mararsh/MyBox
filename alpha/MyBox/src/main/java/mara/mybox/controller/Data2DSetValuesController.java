package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.layout.FlowPane;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.NumberTools;
import mara.mybox.value.AppVariables;
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
    protected boolean rejectInvalid;

    @FXML
    protected ControlData2DSetValue valueController;
    @FXML
    protected Tab valuesTab;
    @FXML
    protected CheckBox rejectCheck;
    @FXML
    protected FlowPane invalidAsPane;

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

            rejectCheck.setDisable(data2D.alwayRejectInvalid());
            rejectCheck.setSelected(data2D.rejectInvalidWhenSave());
            rejectCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.rejectInvalidValueWhenSave = rejectCheck.isSelected();
                    UserConfig.setBoolean("Data2DValidateSave", AppVariables.rejectInvalidValueWhenSave);
                    updateControls();
                }
            });

            updateControls();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void updateControls() {
        useInvalidRadio.setDisable(rejectCheck.isSelected());
        skipInvalidRadio.setDisable(rejectCheck.isSelected());
        if (rejectCheck.isSelected()) {
            failInvalidRadio.setSelected(true);
        }
    }

    @Override
    public void sourceChanged() {
        super.sourceChanged();
        valueController.setData2D(data2D);
    }

    @Override
    public void sourceTypeChanged() {
        super.sourceTypeChanged();
        valueController.setMatrixPane(!isAllPages());
    }

    @Override
    public boolean checkOptions() {
        try {
            if (!super.checkOptions() || !valueController.pickValues()) {
                return false;
            }
            rejectInvalid = data2D.rejectInvalidWhenSave()
                    || rejectCheck.isSelected()
                    || failInvalidRadio.isSelected();

            for (String name : checkedColsNames) {
                Data2DColumn column = data2D.columnByName(name);
                String dummyValue = valueController.setValue.dummyValue(data2D, column);
                if (valueController.setValue.valueInvalid
                        || !column.validValue(dummyValue)) {
                    String info = message("InvalidData") + "\n"
                            + message("Column") + ": " + name + "\n"
                            + message("TestingValue") + ": " + dummyValue;
                    info += "\n------------------------\n"
                            + message("SureContinue");
                    if (!PopTools.askSure(getTitle(), info)) {
                        return false;
                    }
                }
            }

            String info = message("SureOverwriteColumns") + "\n";
            for (String name : checkedColsNames) {
                Data2DColumn column = data2D.columnByName(name);
                info += message("Column") + ": " + name + "    "
                        + message(column.getType().name()) + "\n";
            }
            info += "--------------\n"
                    + message("Set") + ": " + message(valueController.setValue.type.name()) + "\n";
            String para = valueController.setValue.majorParameter();
            if (para != null) {
                info += message("Parameter") + ": " + valueController.setValue.parameter + "\n";
            }
            info += message("ToInvalidValue") + ": "
                    + message(valueController.setValue.invalidAs.name()) + "\n"
                    + "------------------------\n"
                    + message("DataSetValuesComments");
            return PopTools.askSure(getTitle(), info);
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
                if (task != null) {
                    task.setError(message("Invalid") + ": " + message("RowExpression"));
                }
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
                    count = data2D.setValue(this, checkedColsIndices,
                            valueController.setValue, invalidAs);
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
            if (valueController.valueRadio.isSelected()
                    || valueController.zeroRadio.isSelected()
                    || valueController.oneRadio.isSelected()
                    || valueController.emptyRadio.isSelected()
                    || valueController.nullRadio.isSelected()
                    || valueController.randomRadio.isSelected()
                    || valueController.randomNnRadio.isSelected()
                    || valueController.scaleRadio.isSelected()
                    || valueController.prefixRadio.isSelected()
                    || valueController.suffixRadio.isSelected()
                    || valueController.numberRadio.isSelected()
                    || valueController.expressionRadio.isSelected()) {
                return setValue();
            } else if (valueController.gaussianDistributionRadio.isSelected()) {
                return gaussianDistribution();
            } else if (valueController.identifyRadio.isSelected()) {
                return identifyMatrix();
            } else if (valueController.upperTriangleRadio.isSelected()) {
                return upperTriangleMatrix();
            } else if (valueController.lowerTriangleRadio.isSelected()) {
                return lowerTriangleMatrix();
            } else {
                return false;
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    public boolean setValue() {
        try {
            outputData = new ArrayList<>();
            outputData.addAll(sourceController.tableData);
            int numberIndex = valueController.setValue.getStart();
            int digit = valueController.setValue.countFinalDigit(data2D.getRowsNumber());
            String currentValue, newValue;
            boolean rowChanged;
            Random random = new Random();
            boolean valueInvalid;
            boolean validateColumn = rejectInvalid || !useInvalidRadio.isSelected();
            boolean skipInvalid = skipInvalidRadio.isSelected();
            for (int row : sourceController.filteredRowsIndices) {
                List<String> values = sourceController.tableData.get(row);
                rowChanged = false;
                for (int col : checkedColsIndices) {
                    currentValue = values.get(col + 1);
                    Data2DColumn column = data2D.columns.get(col);
                    newValue = valueController.setValue.makeValue(data2D,
                            column, currentValue, values, row,
                            numberIndex, digit, random);
                    valueInvalid = valueController.setValue.valueInvalid;
                    if (!valueInvalid && validateColumn) {
                        if (!column.validValue(newValue)) {
                            valueInvalid = true;
                        }
                    }
                    if (valueInvalid) {
                        if (skipInvalid) {
                            newValue = currentValue;
                        } else if (rejectInvalid) {
                            if (task != null) {
                                task.setError(message("InvalidData") + ". "
                                        + message("Column") + ":" + column.getColumnName() + "  "
                                        + message("Value") + ": " + newValue);
                                task.cancel();
                            }
                            outputData = null;
                            return false;
                        }
                    }
                    if ((currentValue == null && newValue != null)
                            || (currentValue != null && !currentValue.equals(newValue))) {
                        rowChanged = true;
                        values.set(col + 1, newValue);
                    }
                }
                if (rowChanged) {
                    numberIndex++;
                    outputData.set(row, values);
                }
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            outputData = null;
            return false;
        }
    }

    public boolean gaussianDistribution() {
        try {
            if (sourceController.filteredRowsIndices.size() != checkedColsIndices.size()) {
                if (task != null) {
                    task.setError(message("MatricesCannotCalculateShouldSqure"));
                }
                return false;
            }
            if (sourceController.filteredRowsIndices.size() % 2 == 0) {
                if (task != null) {
                    task.setError(message("MatricesCannotCalculateShouldOdd"));
                }
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
            if (task != null) {
                task.setError(e.toString());
            }
            outputData = null;
            return false;
        }
    }

    public boolean identifyMatrix() {
        try {
            if (sourceController.filteredRowsIndices.size() != checkedColsIndices.size()) {
                if (task != null) {
                    task.setError(message("MatricesCannotCalculateShouldSqure"));
                }
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
            if (task != null) {
                task.setError(e.toString());
            }
            outputData = null;
            return false;
        }
    }

    public boolean upperTriangleMatrix() {
        try {
            if (sourceController.filteredRowsIndices.size() != checkedColsIndices.size()) {
                if (task != null) {
                    task.setError(message("MatricesCannotCalculateShouldSqure"));
                }
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
            if (task != null) {
                task.setError(e.toString());
            }
            outputData = null;
            return false;
        }
    }

    public boolean lowerTriangleMatrix() {
        try {
            if (sourceController.filteredRowsIndices.size() != checkedColsIndices.size()) {
                if (task != null) {
                    task.setError(message("MatricesCannotCalculateShouldSqure"));
                }
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
            if (task != null) {
                task.setError(e.toString());
            }
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
            if (task != null) {
                task.setError(message(e.toString()));
            }
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
