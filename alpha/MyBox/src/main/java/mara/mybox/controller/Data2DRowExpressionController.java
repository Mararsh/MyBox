package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-7-1
 * @License Apache License Version 2.0
 */
public class Data2DRowExpressionController extends BaseData2DTargetsController {

    protected String expression;

    @FXML
    protected TextField nameInput;
    @FXML
    protected ControlData2DRowExpression expressionController;
    @FXML
    protected CheckBox errorContinueCheck;
    @FXML
    protected Tab valuesTab;

    public Data2DRowExpressionController() {
        baseTitle = message("RowExpression");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            nameInput.setText(UserConfig.getString(interfaceName + "Name", message("Value")));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void sourceChanged() {
        if (tableController == null) {
            return;
        }
        super.sourceChanged();
        expressionController.setData2D(data2D);
    }

    @Override
    public boolean initData() {
        try {
            if (!super.initData()) {
                return false;
            }
            expression = expressionController.scriptInput.getText();
            if (expression == null || expression.isBlank()) {
                tabPane.getSelectionModel().select(valuesTab);
                popError(message("Invalid") + ": " + message("RowExpression"));
                return false;
            }
            if (!expressionController.checkExpression(isAllPages())) {
                tabPane.getSelectionModel().select(valuesTab);
                alertError(message("Invalid") + ": " + message("RowExpression") + "\n"
                        + expressionController.error);
                return false;
            }
            String name = nameInput.getText();
            if (name == null || name.isBlank()) {
                outOptionsError(message("InvalidParameter") + ": " + message("Name"));
                tabPane.getSelectionModel().select(valuesTab);
                return false;
            } else {
                name = name.trim();
                UserConfig.setString(interfaceName + "Name", name);
                TableStringValues.add(interfaceName + "NameHistories", name);
            }
            return true;
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
        }
        scripts.add(expression);
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
                    if (filled.size() > 1) {
                        String filledScript = filled.get(0);
                        if (filledScript == null || filledScript.isBlank()) {
                            error = message("Invalid") + ": " + message("RowFilter");
                            return false;
                        }
                        data2D.filter.setFilledScript(filledScript);
                        filledExp = filled.get(1);
                    } else if (filled.size() == 1) {
                        filledExp = filled.get(0);
                    }
                    if (filledExp == null || filledExp.isBlank()) {
                        error = message("Invalid") + ": " + message("RowExpression");
                        return false;
                    }
                    expression = filledExp;
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
                data2D.stopTask();
                if (ok) {
                    startOperation();
                }
            }

        };
        start(task);
    }

    @Override
    public boolean handleRows() {
        try {
            boolean showRowNumber = showRowNumber();
            outputData = tableFiltered(showRowNumber);
            if (outputData == null) {
                error = message("SelectToHandle");
                return false;
            }
            for (int i = 0; i < filteredRowsIndices.size(); i++) {
                int rowIndex = filteredRowsIndices.get(i);
                List<String> checkedRow = outputData.get(i);
                if (data2D.calculateTableRowExpression(expression, tableController.tableData.get(rowIndex), rowIndex)) {
                    checkedRow.add(data2D.expressionResult());
                } else {
                    if (errorContinueCheck.isSelected()) {
                        checkedRow.add(null);
                    } else {
                        error = data2D.getError();
                        return false;
                    }
                }
                outputData.set(i, checkedRow);
            }
            outputColumns = data2D.targetColumns(checkedColsIndices, null, showRowNumber(), null);
            String name = nameInput.getText().trim();
            outputColumns.add(new Data2DColumn(name, ColumnDefinition.ColumnType.String));

            if (showColNames()) {
                List<String> names = new ArrayList<>();
                for (Data2DColumn column : outputColumns) {
                    names.add(column.getColumnName());
                }
                outputData.add(0, names);
            }

            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean handleAllData(FxTask currentTask, Data2DWriter writer) {
        return data2D.rowExpression(currentTask, writer, expression,
                nameInput.getText().trim(), errorContinueCheck.isSelected(),
                checkedColsIndices, rowNumberCheck.isSelected(), colNameCheck.isSelected());
    }

    @FXML
    protected void popNameHistories(Event event) {
        if (UserConfig.getBoolean(interfaceName + "NameHistoriesPopWhenMouseHovering", false)) {
            showNameHistories(event);
        }
    }

    @FXML
    protected void showNameHistories(Event event) {
        PopTools.popStringValues(this, nameInput, event, interfaceName + "NameHistories", true);
    }

    /*
        static
     */
    public static Data2DRowExpressionController open(BaseData2DLoadController tableController) {
        try {
            Data2DRowExpressionController controller = (Data2DRowExpressionController) WindowTools.branchStage(
                    tableController, Fxmls.Data2DRowExpressionFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
