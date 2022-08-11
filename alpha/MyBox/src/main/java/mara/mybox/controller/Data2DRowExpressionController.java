package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-7-1
 * @License Apache License Version 2.0
 */
public class Data2DRowExpressionController extends BaseData2DHandleController {

    protected String expression;

    @FXML
    protected TextField nameInput;
    @FXML
    protected ControlData2DRowExpression expressionController;
    @FXML
    protected CheckBox errorContinueCheck;

    public Data2DRowExpressionController() {
        baseTitle = message("RowExpression");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            nameInput.setText(UserConfig.getString(interfaceName + "Name", message("Value")));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
    public boolean checkOptions() {
        boolean ok = expressionController.checkExpression(isAllPages());
        if (!ok && data2D.getError() != null) {
            infoLabel.setText(message("Invalid") + ": " + message("RowExpression") + "\n"
                    + data2D.getError());
        }
        String name = nameInput.getText();
        if (name == null || name.isBlank()) {
            popError(message("InvalidParameter") + ": " + message("Name"));
            ok = false;
        } else {
            name = name.trim();
            UserConfig.setString(interfaceName + "Name", name);
            TableStringValues.add(interfaceName + "NameHistories", name);
        }
        ok = super.checkOptions() && ok;
        okButton.setDisable(!ok);
        return ok;
    }

    @Override
    public void preprocessStatistic() {
        List<String> scripts = new ArrayList<>();
        String filterScript = data2D.filterScipt();
        if (filterScript != null && !filterScript.isBlank()) {
            scripts.add(filterScript);
        }
        expression = expressionController.scriptInput.getText();
        if (expression == null || expression.isBlank()) {
            popError(message("Invalid") + ": " + message("RowExpression"));
            return;
        }
        scripts.add(expression);
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(task);
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
                task = null;
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
            outputData = filtered(showRowNumber);
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
            String name = nameInput.getText().trim();
            if (showColNames()) {
                List<String> names = checkedColsNames;
                if (showRowNumber) {
                    names.add(0, message("SourceRowNumber"));
                }
                names.add(name);
                outputData.add(0, names);
            }
            outputColumns.add(new Data2DColumn(name, ColumnDefinition.ColumnType.String));
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public DataFileCSV generatedFile() {
        MyBoxLog.console(expression);
        return data2D.rowExpression(expression, nameInput.getText().trim(), errorContinueCheck.isSelected(),
                checkedColsIndices, rowNumberCheck.isSelected(), colNameCheck.isSelected());
    }

    @FXML
    protected void popNameHistories(MouseEvent event) {
        if (UserConfig.getBoolean(interfaceName + "NameHistoriesPopWhenMouseHovering", true)) {
            PopTools.popStringValues(this, nameInput, event, interfaceName + "NameHistories", true, true);
        }
    }

    @FXML
    protected void showNameHistories(ActionEvent event) {
        PopTools.popStringValues(this, nameInput, event, interfaceName + "NameHistories", true, true);
    }

    /*
        static
     */
    public static Data2DRowExpressionController open(ControlData2DEditTable tableController) {
        try {
            Data2DRowExpressionController controller = (Data2DRowExpressionController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DRowExpressionFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
