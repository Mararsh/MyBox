package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
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
public class MatrixUnaryCalculationController extends BaseController {

    protected DataMatrix resultMatrix;
    protected int column, row, power;
    protected double number, resultValue;
    protected double[][] sourceMatrix, result;

    @FXML
    protected ControlData2DMatrix dataController;
    @FXML
    protected ControlData2DView resultController;
    @FXML
    protected Tab resultTab;
    @FXML
    protected ToggleGroup opGroup;
    @FXML
    protected VBox setBox, xyBox, resultBox, normalizeBox;
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
    @FXML
    protected Label resultLabel, checkLabel;
    @FXML
    protected TextArea resultArea;
    @FXML
    protected ScrollPane resultTablePane;

    public MatrixUnaryCalculationController() {
        baseTitle = message("MatrixUnaryCalculation");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            resultController.createData(Data2D.DataType.Matrix);
            resultMatrix = (DataMatrix) resultController.data2D;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            resultBox.getChildren().clear();

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
                        checkMatrix(false);
                    });
            checkMatrix(false);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected boolean checkXY() {
        if (sourceMatrix == null || sourceMatrix.length == 0) {
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
            if (v > 0 && v <= sourceMatrix.length) {
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
            if (v > 0 && v <= sourceMatrix[0].length) {
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
        if (sourceMatrix == null || sourceMatrix.length == 0) {
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
        if (sourceMatrix == null || sourceMatrix.length == 0) {
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

    protected void checkMatrix(boolean calculate) {
        if (!dataController.checkSelections()) {
            popError(message("NoData") + ": " + message("Matrix"));
            return;
        }
        setBox.getChildren().clear();
        rowInput.setStyle(null);
        columnInput.setStyle(null);
        numberInput.setStyle(null);
        powerInput.setStyle(null);
        checkLabel.setText("");
        if (task != null) {
            task.cancel();
        }
        resultLabel.setText("");
        resultBox.getChildren().clear();
        sourceMatrix = null;
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    sourceMatrix = dataController.pickMatrix(this);
                    return sourceMatrix != null && sourceMatrix.length > 0;
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
                setControls();
                if (ok && calculate) {
                    calculate();
                }
            }

        };
        start(task);
    }

    protected void handleError(String error) {
        popError(error);
        checkLabel.setText(error);
    }

    protected boolean setControls() {
        if (sourceMatrix == null || sourceMatrix.length == 0) {
            handleError(message("InvalidData") + ": " + message("Matrix"));
            return false;
        }
        if (DeterminantByEliminationRadio.isSelected() || DeterminantByComplementMinorRadio.isSelected()
                || InverseMatrixByEliminationRadio.isSelected() || InverseMatrixByAdjointRadio.isSelected()
                || MatrixRankRadio.isSelected() || AdjointMatrixRadio.isSelected() || PowerRadio.isSelected()) {
            if (!dataController.isSquare(sourceMatrix)) {
                handleError(message("MatricesCannotCalculateShouldSameRows"));
                return false;
            }

            if (PowerRadio.isSelected()) {
                setBox.getChildren().add(powerBox);
                if (!checkPower()) {
                    handleError(message("InvalidParameters"));
                    return false;
                }
            }

        } else if (ComplementMinorRadio.isSelected()) {
            setBox.getChildren().add(xyBox);
            if (!checkXY()) {
                handleError(message("InvalidParameters"));
                return false;
            }

        } else if (MultiplyNumberRadio.isSelected() || DivideNumberRadio.isSelected()) {
            setBox.getChildren().add(numberBox);
            if (!checkNumber()) {
                handleError(message("InvalidParameters"));
                return false;
            }

        } else if (normalizeRadio.isSelected()) {
            setBox.getChildren().add(normalizeBox);

        }
        return true;
    }

    @FXML
    public void calculateAction() {
        checkMatrix(true);
    }

    @FXML
    public void calculate() {
        if (sourceMatrix == null || sourceMatrix.length == 0) {
            handleError(message("InvalidData") + ": " + message("Matrix"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        resultLabel.setText("");
        resultBox.getChildren().clear();
        task = new FxSingletonTask<Void>(this) {
            private String op;

            @Override
            protected boolean handle() {
                try {
                    result = null;
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
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                cost = new Date().getTime() - startTime.getTime();
                resultLabel.setText(op + "  " + message("Cost") + ":" + DateTools.datetimeMsDuration(cost));
                if (result != null) {
                    resultBox.getChildren().add(resultTablePane);
                    resultController.loadMatrix(result);
                } else if (!DoubleTools.invalidDouble(resultValue)) {
                    resultBox.getChildren().add(resultArea);
                    resultArea.setText(resultValue + "");
                }
                refreshStyle(resultBox);
                tabPane.getSelectionModel().select(resultTab);
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
