package mara.mybox.controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleMatrixTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-17
 * @License Apache License Version 2.0
 */
public class MatrixUnaryCalculationController extends BaseData2DTaskController {

    protected int column, row, power;
    protected double number, resultValue;

    @FXML
    protected ControlData2DSource matrixController;
    @FXML
    protected ToggleGroup opGroup;
    @FXML
    protected VBox setBox, xyBox, normalizeBox;
    @FXML
    protected HBox numberBox, powerBox;
    @FXML
    protected TextField rowInput, columnInput, numberInput, powerInput;
    @FXML
    protected RadioButton transposeRadio, DivideNumberRadio, normalizeRadio,
            DeterminantByEliminationRadio, DeterminantByComplementMinorRadio,
            InverseMatrixByEliminationRadio, InverseMatrixByAdjointRadio, MatrixRankRadio, AdjointMatrixRadio, PowerRadio,
            ComplementMinorRadio, MultiplyNumberRadio;
    @FXML
    protected ControlData2DNormalize normalizeController;

    public MatrixUnaryCalculationController() {
        baseTitle = message("MatrixUnaryCalculation");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            stageType = StageType.Normal;

            sourceController = matrixController;
            filterController = matrixController.filterController;
            dataController = matrixController;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            matrixController.setParameters(this);

            if (normalizeController != null) {
                row = UserConfig.getInt(interfaceName + "Row", 1);
                rowInput.setText(row + "");

                column = UserConfig.getInt(interfaceName + "Column", 1);
                columnInput.setText(column + "");

                try {
                    number = Double.parseDouble(UserConfig.getString(interfaceName + "Number", "2"));
                } catch (Exception e) {
                    number = 2d;
                }
                numberInput.setText(number + "");

                try {
                    power = UserConfig.getInt(interfaceName + "Power", 2);
                } catch (Exception e) {
                    power = 2;
                }
                powerInput.setText(power + "");

                opGroup.selectedToggleProperty().addListener(
                        (ObservableValue<? extends Toggle> ov, Toggle oldValue, Toggle newValue) -> {
                            checkControls();
                        });
                checkControls();
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected boolean checkXY() {
        if (matrixController.data2D == null) {
            return false;
        }
        boolean valid = true;
        try {
            if (!setBox.getChildren().contains(xyBox)) {
                return true;
            }
            int v = Integer.parseInt(rowInput.getText().trim());
            if (v > 0 && v <= matrixController.data2D.getRowsNumber()) {
                row = v;
                UserConfig.setInt(interfaceName + "Row", v);
            } else {
                valid = false;
            }
        } catch (Exception e) {
            valid = false;
        }
        try {
            int v = Integer.parseInt(columnInput.getText().trim());
            if (v > 0 && v <= matrixController.data2D.getColsNumber()) {
                column = v;
                UserConfig.setInt(interfaceName + "Column", v);
            } else {
                valid = false;
            }
        } catch (Exception e) {
            valid = false;
        }
        return valid;
    }

    protected boolean checkNumber() {
        if (matrixController.data2D == null) {
            return false;
        }
        try {
            if (!setBox.getChildren().contains(numberBox)) {
                return true;
            }
            double v = Double.parseDouble(numberInput.getText().trim());
            if (DivideNumberRadio.isSelected() && v == 0) {
                return false;
            } else {
                number = v;
                UserConfig.setDouble(interfaceName + "Number", number);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean checkPower() {
        if (matrixController.data2D == null) {
            return false;
        }
        try {
            if (!setBox.getChildren().contains(powerBox)) {
                return true;
            }
            int v = Integer.parseInt(powerInput.getText().trim());
            if (v > 1) {
                power = v;
                UserConfig.setInt(interfaceName + "Power", power);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public boolean checkControls() {
        try {
            setBox.getChildren().clear();
            if (matrixController.data2D == null) {
                return false;
            }
            if (DeterminantByEliminationRadio.isSelected()
                    || DeterminantByComplementMinorRadio.isSelected()
                    || InverseMatrixByEliminationRadio.isSelected()
                    || InverseMatrixByAdjointRadio.isSelected()
                    || MatrixRankRadio.isSelected()
                    || AdjointMatrixRadio.isSelected()
                    || PowerRadio.isSelected()) {
                int colsNumber = matrixController.checkedColsIndices.size();
                int rowsNumber = matrixController.allPagesRadio.isSelected()
                        ? (int) matrixController.data2D.getRowsNumber()
                        : matrixController.filteredRowsIndices.size();
                if (colsNumber != rowsNumber) {
                    popError(message("MatricesCannotCalculateShouldSqure"));
                    return false;
                }

                if (PowerRadio.isSelected()) {
                    setBox.getChildren().add(powerBox);
                    if (!checkPower()) {
                        popError(message("InvalidParameters"));
                        return false;
                    }
                }

            } else if (ComplementMinorRadio.isSelected()) {
                setBox.getChildren().add(xyBox);
                if (!checkXY()) {
                    popError(message("InvalidParameters"));
                    return false;
                }

            } else if (MultiplyNumberRadio.isSelected() || DivideNumberRadio.isSelected()) {
                setBox.getChildren().add(numberBox);
                if (!checkNumber()) {
                    popError(message("InvalidParameters"));
                    return false;
                }

            } else if (normalizeRadio.isSelected()) {
                setBox.getChildren().add(normalizeBox);

            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void setParameters(BaseData2DLoadController controller) {
        try {
            controller.setIconified(true);

            matrixController.data2D = controller.data2D.cloneAll();
            matrixController.tableData.setAll(controller.tableData);

            dataLoaded();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkParameters() {
        if (!matrixController.checkSelections() || !checkControls()) {
            return false;
        }
        if (!matrixController.allPagesRadio.isSelected()
                && matrixController.selectedRowsIndices.isEmpty()) {
            popError(message("SelectToHandle") + ": " + message("Rows"));
            tabPane.getSelectionModel().select(sourceTab);
            return false;
        }
        return true;
    }

    @Override
    public boolean checkOptions() {
        try {
            if (!checkParameters()) {
                return false;
            }
            data2D = matrixController.data2D;
            invalidAs = InvalidAs.Fail;

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public double[][] pickSourceMatrix(FxTask rtask, ControlData2DSource controller) {
        try {
            List<List<String>> data = controller.selectedData(rtask, false);
            if (data == null) {
                return null;
            }
            int rowsNumber = data.size();
            int colsNumber = data.get(0).size();
            if (rowsNumber <= 0 || colsNumber <= 0) {
                return null;
            }
            double[][] matrix = new double[rowsNumber][colsNumber];
            for (int r = 0; r < rowsNumber; r++) {
                for (int c = 0; c < colsNumber; c++) {
                    matrix[r][c] = DoubleTools.toDouble(data.get(r).get(c), invalidAs);
                }
            }
            return matrix;
        } catch (Exception e) {
            if (rtask != null) {
                rtask.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    public DataMatrix writeResultMatrix(FxTask rtask, double[][] result, String dataname) {
        try {
            int rowsNumber = result.length;
            int colsNumber = result[0].length;
            targetFile = DataMatrix.file(dataname);
            DataMatrix resultMatrix = new DataMatrix();
            resultMatrix.setFile(targetFile).setSheet("Double")
                    .setDataName(dataname)
                    .setColsNumber(colsNumber)
                    .setRowsNumber(rowsNumber);
            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(targetFile, resultMatrix.getCharset(), false))) {
                String line;
                for (int i = 0; i < rowsNumber; i++) {
                    line = result[i][0] + "";
                    for (int j = 1; j < colsNumber; j++) {
                        line += DataMatrix.MatrixDelimiter + result[i][j];
                    }
                    writer.write(line + "\n");
                }
                writer.flush();
            } catch (Exception ex) {
            }
            resultMatrix = (DataMatrix) new TableData2DDefinition().insertData(resultMatrix);
            return resultMatrix;
        } catch (Exception e) {
            if (rtask != null) {
                rtask.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        taskSuccessed = false;
        task = new FxSingletonTask<Void>(this) {
            private String op, resultInfo;
            private DataMatrix resultMatrix;

            @Override
            protected boolean handle() {
                try {
                    double[][] sourceMatrix = pickSourceMatrix(this, matrixController);
                    if (sourceMatrix == null) {
                        return false;
                    }
                    double[][] result = null;
                    resultValue = AppValues.InvalidDouble;
                    op = ((RadioButton) opGroup.getSelectedToggle()).getText();
                    if (message("Transpose").equals(op)) {
                        result = DoubleMatrixTools.transpose(sourceMatrix);

                    } else if (message("RowEchelonForm").equals(op)) {
                        result = DoubleMatrixTools.rowEchelonForm(sourceMatrix);

                    } else if (message("ReducedRowEchelonForm").equals(op)) {
                        result = DoubleMatrixTools.reducedRowEchelonForm(sourceMatrix);

                    } else if (message("ComplementMinor").equals(op)) {
                        result = DoubleMatrixTools.complementMinor(sourceMatrix, row - 1, column - 1);

                    } else if (message("Normalize").equals(op)) {
                        result = normalizeController.calculateDoubles(sourceMatrix, InvalidAs.Empty);

                    } else if (message("MultiplyNumber").equals(op)) {
                        result = DoubleMatrixTools.multiply(sourceMatrix, number);

                    } else if (message("DivideNumber").equals(op)) {
                        result = DoubleMatrixTools.divide(sourceMatrix, number);

                    } else if (message("DeterminantByElimination").equals(op)) {
                        resultValue = DoubleMatrixTools.determinantByElimination(sourceMatrix);

                    } else if (message("DeterminantByComplementMinor").equals(op)) {
                        resultValue = DoubleMatrixTools.determinantByComplementMinor(sourceMatrix);

                    } else if (message("InverseMatrixByElimination").equals(op)) {
                        result = DoubleMatrixTools.inverseByElimination(sourceMatrix);

                    } else if (message("InverseMatrixByAdjoint").equals(op)) {
                        result = DoubleMatrixTools.inverseByAdjoint(sourceMatrix);

                    } else if (message("MatrixRank").equals(op)) {
                        resultValue = DoubleMatrixTools.rank(sourceMatrix);

                    } else if (message("AdjointMatrix").equals(op)) {
                        result = DoubleMatrixTools.adjoint(sourceMatrix);

                    } else if (message("Power").equals(op)) {
                        result = DoubleMatrixTools.power(sourceMatrix, power);

                    }
                    if (result != null) {
                        resultMatrix = writeResultMatrix(this, result,
                                matrixController.data2D.getDataName() + "_" + op);
                        taskSuccessed = resultMatrix != null;

                    } else if (!DoubleTools.invalidDouble(resultValue)) {
                        resultInfo = op + ":\n" + resultValue;
                        taskSuccessed = true;

                    } else {
                        taskSuccessed = false;
                    }
                } catch (Exception e) {
                    error = e.toString();
                    taskSuccessed = false;
                }
                return taskSuccessed;
            }

            @Override
            protected void whenSucceeded() {
                cost = new Date().getTime() - startTime.getTime();
                showLogs(op + "  " + message("Cost") + ":" + DateTools.datetimeMsDuration(cost));
                tabPane.getSelectionModel().select(logsTab);
                if (resultMatrix != null) {
                    Data2DManufactureController.openDef(resultMatrix).setAlwaysOnTop();
                } else if (resultInfo != null) {
                    TextPopController.loadText(resultInfo);
                }
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
    public boolean keyEventsFilter(KeyEvent event) {
        if (sourceTab != null) {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == sourceTab) {
                if (matrixController.keyEventsFilter(event)) {
                    return true;
                }
            }
        }
        return super.keyEventsFilter(event);
    }


    /*
        static
     */
    public static MatrixUnaryCalculationController open(BaseData2DLoadController tableController) {
        try {
            MatrixUnaryCalculationController controller = (MatrixUnaryCalculationController) WindowTools.openStage(
                    Fxmls.MatrixUnaryCalculationFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
