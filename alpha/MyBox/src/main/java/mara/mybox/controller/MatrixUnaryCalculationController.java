package mara.mybox.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
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
public class MatrixUnaryCalculationController extends BaseData2DTaskTargetsController {

    protected int rowsNumber, colsNumber;
    protected int column, row, power;
    protected double number, resultValue;

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
    public void initOptions() {
        try {
            super.initOptions();

            targetController.setTarget(TargetType.Matrix);

            row = UserConfig.getInt(baseName + "Row", 1);
            rowInput.setText(row + "");
            rowInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        checkXY();
                    });

            column = UserConfig.getInt(baseName + "Column", 1);
            columnInput.setText(column + "");
            columnInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        checkXY();
                    });

            try {
                number = Double.parseDouble(UserConfig.getString(baseName + "Number", "2"));
            } catch (Exception e) {
                number = 2d;
            }
            numberInput.setText(number + "");
            numberInput.textProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                try {
                    number = Double.parseDouble(newValue);
                    numberInput.setStyle(null);
                    UserConfig.setString(baseName + "Number", number + "");
                } catch (Exception e) {
                    numberInput.setStyle(UserConfig.badStyle());
                }
            });

            try {
                power = UserConfig.getInt(baseName + "Power", 2);
            } catch (Exception e) {
                power = 2;
            }
            powerInput.setText(power + "");
            powerInput.textProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v > 1) {
                        power = v;
                        powerInput.setStyle(null);
                        UserConfig.setInt(baseName + "Power", power);
                    } else {
                        powerInput.setStyle(UserConfig.badStyle());
                    }
                } catch (Exception e) {
                    powerInput.setStyle(UserConfig.badStyle());
                }
            });

            opGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> ov, Toggle oldValue, Toggle newValue) -> {
                        checkParameters();
                    });
            checkParameters();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected boolean checkXY() {
        if (data2D == null) {
            return false;
        }
        boolean valid = true;
        try {
            if (!setBox.getChildren().contains(xyBox)) {
                rowInput.setStyle(null);
                columnInput.setStyle(null);
                return true;
            }
            int v = Integer.parseInt(rowInput.getText().trim());
            if (v > 0 && v <= data2D.getRowsNumber()) {
                row = v;
                rowInput.setStyle(null);
                UserConfig.setInt(baseName + "Row", v);
            } else {
                rowInput.setStyle(UserConfig.badStyle());
                valid = false;
            }
        } catch (Exception e) {
            rowInput.setStyle(UserConfig.badStyle());
            valid = false;
        }
        try {
            int v = Integer.parseInt(columnInput.getText().trim());
            if (v > 0 && v <= data2D.getColsNumber()) {
                column = v;
                columnInput.setStyle(null);
                UserConfig.setInt(baseName + "Column", v);
            } else {
                columnInput.setStyle(UserConfig.badStyle());
                valid = false;
            }
        } catch (Exception e) {
            columnInput.setStyle(UserConfig.badStyle());
            valid = false;
        }
        return valid;
    }

    protected boolean checkNumber() {
        if (data2D == null) {
            return false;
        }
        try {
            if (!setBox.getChildren().contains(numberBox)) {
                numberInput.setStyle(null);
                return true;
            }
            number = Double.parseDouble(numberInput.getText().trim());
            if (DivideNumberRadio.isSelected() && number == 0) {
                numberInput.setStyle(UserConfig.badStyle());
                return false;
            }
            numberInput.setStyle(null);
            UserConfig.setString(baseName + "Number", number + "");
            return true;
        } catch (Exception e) {
            numberInput.setStyle(UserConfig.badStyle());
            return false;
        }
    }

    protected boolean checkPower() {
        if (data2D == null) {
            return false;
        }
        try {
            if (!setBox.getChildren().contains(powerBox)) {
                powerInput.setStyle(null);
                return true;
            }
            int v = Integer.parseInt(powerInput.getText().trim());
            if (v > 1) {
                power = v;
                powerInput.setStyle(null);
                UserConfig.setInt(baseName + "Power", power);
                return true;
            } else {
                powerInput.setStyle(UserConfig.badStyle());
                return false;
            }
        } catch (Exception e) {
            powerInput.setStyle(UserConfig.badStyle());
            return false;
        }
    }

    @Override
    public boolean checkOptions() {
        try {
            if (!super.checkOptions()) {
                return false;
            }
            colsNumber = checkedColsIndices.size();
            rowsNumber = sourceController.allPagesRadio.isSelected()
                    ? (int) data2D.getRowsNumber()
                    : sourceController.filteredRowsIndices.size();

            if (DeterminantByEliminationRadio.isSelected()
                    || DeterminantByComplementMinorRadio.isSelected()
                    || InverseMatrixByEliminationRadio.isSelected()
                    || InverseMatrixByAdjointRadio.isSelected()
                    || MatrixRankRadio.isSelected()
                    || AdjointMatrixRadio.isSelected()
                    || PowerRadio.isSelected()) {
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
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private String op, resultInfo;
            private DataMatrix resultMatrix;

            @Override
            protected boolean handle() {
                try {
                    outputData = filteredData(checkedColsIndices, false);
                    if (outputData == null) {
                        return false;
                    }
                    rowsNumber = outputData.size();
                    colsNumber = outputColumns.size();
                    double[][] sourceMatrix = new double[rowsNumber][colsNumber];
                    for (int r = 0; r < rowsNumber; r++) {
                        for (int c = 0; c < colsNumber; c++) {
                            sourceMatrix[r][c] = DoubleTools.toDouble(outputData.get(r).get(c), invalidAs);
                        }
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
                        targetFile = new File(DataMatrix.filename(data2D.getDataName()
                                + "_" + op + "_" + new Date().getTime()));
                        resultMatrix = new DataMatrix();
                        resultMatrix.setFile(targetFile).setSheet("Double")
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
                        new TableData2DDefinition().updateData(resultMatrix);

                    } else if (!DoubleTools.invalidDouble(resultValue)) {
//                        resultInfo =

                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                cost = new Date().getTime() - startTime.getTime();
                showLogs(op + "  " + message("Cost") + ":" + DateTools.datetimeMsDuration(cost));
//                resultLabel.setText();
//                if (result != null) {
//                    resultBox.getChildren().add(resultTablePane);
//                    resultController.loadMatrix(result);
//                } else if (!DoubleTools.invalidDouble(resultValue)) {
//                    resultBox.getChildren().add(resultArea);
//                    resultArea.setText(resultValue + "");
//                }
                tabPane.getSelectionModel().select(logsTab);
            }

        };
        start(task);
    }


    /*
        static
     */
    public static MatrixUnaryCalculationController oneOpen() {
        MatrixUnaryCalculationController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof MatrixUnaryCalculationController) {
                try {
                    controller = (MatrixUnaryCalculationController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (MatrixUnaryCalculationController) WindowTools.openStage(Fxmls.MatrixUnaryCalculationFxml);
        }
        controller.requestMouse();
        return controller;
    }

    public static MatrixUnaryCalculationController open() {
        MatrixUnaryCalculationController controller = oneOpen();
        return controller;
    }

}
