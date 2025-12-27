package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleMatrixTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-12-22
 * @License Apache License Version 2.0
 */
public class MatricesBinaryCalculationController extends MatrixUnaryCalculationController {

    @FXML
    protected Tab dataATab, dataBTab;
    @FXML
    protected ControlData2DSource dataAController, dataBController;
    @FXML
    protected RadioButton plusRadio, minusRadio, multiplyRadio,
            hadamardProductRadio, kroneckerProductRadio, verticalMergeRadio, horizontalMergeRadio;

    public MatricesBinaryCalculationController() {
        baseTitle = message("MatricesBinaryCalculation");
    }

    @Override
    public void initValues() {
        try {
            matrixController = dataAController;
            super.initValues();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            dataBController.setParameters(this);

            dataAController.refreshTitle = false;
            dataBController.refreshTitle = false;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(plusRadio, new Tooltip(message("MatricesPlusComments")));
            NodeStyleTools.setTooltip(minusRadio, new Tooltip(message("MatricesMinusComments")));
            NodeStyleTools.setTooltip(multiplyRadio, new Tooltip(message("MatricesMultiplyComments")));
            NodeStyleTools.setTooltip(hadamardProductRadio, new Tooltip(message("HadamardProductComments")));
            NodeStyleTools.setTooltip(kroneckerProductRadio, new Tooltip(message("KroneckerProductComments")));
            NodeStyleTools.setTooltip(verticalMergeRadio, new Tooltip(message("VerticalMergeComments")));
            NodeStyleTools.setTooltip(horizontalMergeRadio, new Tooltip(message("HorizontalMergeComments")));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void setBaseTitle(String title) {
    }

    @Override
    public boolean checkParameters() {
        try {
            if (!dataAController.checkSelections()) {
                popError(message("NoData") + ": " + message("MatrixA"));
                return false;
            }
            int colsNumberA = dataAController.checkedColsIndices.size();
            int rowsNumberA = dataAController.allPagesRadio.isSelected()
                    ? (int) dataAController.data2D.getRowsNumber()
                    : dataAController.filteredRowsIndices.size();
            if (colsNumberA == 0 || rowsNumberA == 0) {
                popError(message("InvalidData") + ": " + message("MatrixA"));
                return false;
            }

            if (!dataBController.checkSelections()) {
                popError(message("NoData") + ": " + message("MatrixB"));
                return false;
            }
            int colsNumberB = dataBController.checkedColsIndices.size();
            int rowsNumberB = dataBController.allPagesRadio.isSelected()
                    ? (int) dataBController.data2D.getRowsNumber()
                    : dataBController.filteredRowsIndices.size();

            if (colsNumberB == 0 || rowsNumberB == 0) {
                popError(message("InvalidData") + ": " + message("MatrixB"));
                return false;
            }

            if (plusRadio.isSelected()
                    || minusRadio.isSelected()
                    || hadamardProductRadio.isSelected()) {
                if (rowsNumberA != rowsNumberB || colsNumberA != colsNumberB) {
                    popError(message("MatricesCannotCalculateShouldSame"));
                    return false;
                }

            } else if (multiplyRadio.isSelected()) {
                if (colsNumberA != rowsNumberB) {
                    popError(message("MatricesCannotCalculateMultiply"));
                    return false;
                }

            } else if (verticalMergeRadio.isSelected()) {
                if (colsNumberA != colsNumberB) {
                    popError(message("MatricesCannotCalculateShouldSameCols"));
                    return false;
                }

            } else if (horizontalMergeRadio.isSelected()) {
                if (rowsNumberA != rowsNumberB) {
                    popError(message("MatricesCannotCalculateShouldSameRows"));
                    return false;
                }

            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void preprocessStatistic() {
        List<String> scriptsA = new ArrayList<>();
        String filterScriptA = dataAController.data2D.filterScipt();
        boolean hasFilterScriptA = filterScriptA != null && !filterScriptA.isBlank();
        if (hasFilterScriptA) {
            scriptsA.add(filterScriptA);
            updateLogs(message("Filter") + ": " + filterScriptA, true);
        }
        List<String> scriptsB = new ArrayList<>();
        String filterScriptB = dataBController.data2D.filterScipt();
        boolean hasFilterScriptB = filterScriptB != null && !filterScriptB.isBlank();
        if (hasFilterScriptB) {
            scriptsB.add(filterScriptB);
            updateLogs(message("Filter") + ": " + filterScriptB, true);
        }

        if (scriptsA.isEmpty() && scriptsB.isEmpty()) {
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
                dataAController.data2D.setTask(this);
                List<String> filledScriptsA = dataAController.data2D.calculateScriptsStatistic(scriptsA);
                if (hasFilterScriptA && filledScriptsA != null && filledScriptsA.size() == scriptsA.size()) {
                    dataAController.data2D.filter.setFilledScript(filledScriptsA.get(0));
                }
                dataBController.data2D.setTask(this);
                List<String> filledScriptsB = dataBController.data2D.calculateScriptsStatistic(scriptsB);
                if (hasFilterScriptB && filledScriptsB != null && filledScriptsB.size() == scriptsB.size()) {
                    dataBController.data2D.filter.setFilledScript(filledScriptsB.get(0));
                }
                taskSuccessed = true;
                return taskSuccessed;
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void whenCanceled() {
                taskCanceled();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                dataAController.data2D.stopTask();
                dataBController.data2D.stopTask();
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
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private String op;
            private DataMatrix resultMatrix;

            @Override
            protected boolean handle() {
                try {
                    double[][] sourceMatrixA = pickSourceMatrix(this, dataAController);
                    if (sourceMatrixA == null) {
                        return false;
                    }
                    double[][] sourceMatrixB = pickSourceMatrix(this, dataBController);
                    if (sourceMatrixB == null) {
                        return false;
                    }
                    double[][] result = null;
                    if (plusRadio.isSelected()) {
                        result = DoubleMatrixTools.add(sourceMatrixA, sourceMatrixB);

                    } else if (minusRadio.isSelected()) {
                        result = DoubleMatrixTools.subtract(sourceMatrixA, sourceMatrixB);

                    } else if (multiplyRadio.isSelected()) {
                        result = DoubleMatrixTools.multiply(sourceMatrixA, sourceMatrixB);

                    } else if (hadamardProductRadio.isSelected()) {
                        result = DoubleMatrixTools.hadamardProduct(sourceMatrixA, sourceMatrixB);

                    } else if (kroneckerProductRadio.isSelected()) {
                        result = DoubleMatrixTools.kroneckerProduct(sourceMatrixA, sourceMatrixB);

                    } else if (verticalMergeRadio.isSelected()) {
                        result = DoubleMatrixTools.vertivalMerge(sourceMatrixA, sourceMatrixB);

                    } else if (horizontalMergeRadio.isSelected()) {
                        result = DoubleMatrixTools.horizontalMerge(sourceMatrixA, sourceMatrixB);

                    }
                    op = ((RadioButton) opGroup.getSelectedToggle()).getText();
                    resultMatrix = writeResultMatrix(this, result,
                            dataAController.data2D.getDataName()
                            + "_" + dataBController.data2D.getDataName()
                            + "_" + op);
                    return resultMatrix != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                cost = new Date().getTime() - startTime.getTime();
                showLogs(op + "  " + message("Cost") + ":" + DateTools.datetimeMsDuration(cost));
                tabPane.getSelectionModel().select(logsTab);
                Data2DManufactureController.openDef(resultMatrix).setAlwaysOnTop();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                closeTask(ok);
            }

        };
        start(task, false);
    }

    @Override
    public boolean handleKeyEvent(KeyEvent event) {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab == dataATab) {
            if (dataAController.handleKeyEvent(event)) {
                return true;
            }
        } else if (tab == dataBTab) {
            if (dataBController.handleKeyEvent(event)) {
                return true;
            }
        }
        return super.handleKeyEvent(event);
    }

    /*
        static
     */
    public static MatricesBinaryCalculationController open(BaseData2DLoadController tableController) {
        try {
            MatricesBinaryCalculationController controller = (MatricesBinaryCalculationController) WindowTools.openStage(
                    Fxmls.MatricesBinaryCalculationFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
