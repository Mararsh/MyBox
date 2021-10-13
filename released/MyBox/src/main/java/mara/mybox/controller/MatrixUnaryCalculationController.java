package mara.mybox.controller;

import java.util.Date;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.MatrixDoubleTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-17
 * @License Apache License Version 2.0
 */
public class MatrixUnaryCalculationController extends MatricesManageController {

    protected int column, row, power;
    protected double number, resultValue;
    protected double[][] result;

    @FXML
    protected ToggleGroup opGroup;
    @FXML
    protected VBox setBox, xyBox, resultBox;
    @FXML
    protected HBox numberBox, powerBox;
    @FXML
    protected TextField rowInput, columnInput, numberInput, powerInput;
    @FXML
    protected Button calculateButton;
    @FXML
    protected RadioButton divideRadio;
    @FXML
    protected Label resultLabel, checkLabel;
    @FXML
    protected ControlMatrixEdit resultTableController;
    @FXML
    protected TextArea resultArea;
    @FXML
    protected ScrollPane resultTablePane;

    public MatrixUnaryCalculationController() {
        baseTitle = message("MatrixUnaryCalculation");
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
                        checkMatrix();
                    });
            checkMatrix();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected boolean checkXY() {
        boolean valid = true;
        try {
            if (!setBox.getChildren().contains(xyBox)) {
                rowInput.setStyle(null);
                columnInput.setStyle(null);
                return true;
            }
            int v = Integer.parseInt(rowInput.getText().trim());
            if (v > 0 && v <= editController.matrixDouble().length) {
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
            if (v > 0 && v <= editController.matrixDouble().length) {
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
        try {
            if (!setBox.getChildren().contains(numberBox)) {
                numberInput.setStyle(null);
                return true;
            }
            number = Double.parseDouble(numberInput.getText().trim());
            if (divideRadio.isSelected() && number == 0) {
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

    protected boolean checkMatrix() {
        String op = ((RadioButton) opGroup.getSelectedToggle()).getText();
        setBox.getChildren().clear();
        rowInput.setStyle(null);
        columnInput.setStyle(null);
        numberInput.setStyle(null);
        powerInput.setStyle(null);
        checkLabel.setText("");
        if (editController.colsNumber != editController.rowsNumber
                && (message("DeterminantByElimination").equals(op)
                || message("DeterminantByComplementMinor").equals(op)
                || message("InverseMatrixByElimination").equals(op)
                || message("InverseMatrixByAdjoint").equals(op)
                || message("MatrixRank").equals(op)
                || message("AdjointMatrix").equals(op)
                || message("Power").equals(op))) {
            checkLabel.setText(message("MatricesCannotCalculateShouldSqure"));
            calculateButton.setDisable(true);
            return false;

        } else if (message("ComplementMinor").equals(op)) {
            setBox.getChildren().add(xyBox);
            if (!checkXY()) {
                checkLabel.setText(message("InvalidParameters"));
                return false;
            }

        } else if (message("MultiplyNumber").equals(op) || message("DivideNumber").equals(op)) {
            setBox.getChildren().add(numberBox);
            if (!checkNumber()) {
                checkLabel.setText(message("InvalidParameters"));
                return false;
            }

        } else if (message("Power").equals(op)) {
            setBox.getChildren().add(powerBox);
            if (!checkPower()) {
                checkLabel.setText(message("InvalidParameters"));
                return false;
            }
        }

        calculateButton.setDisable(false);
        return true;
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();

        resultTableController.setManager(listController);

        editController.sheetChangedNotify.addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    checkMatrix();
                });
    }

    @FXML
    public void calculateAction() {
        if (!checkMatrix()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            resultLabel.setText("");
            resultBox.getChildren().clear();
            task = new SingletonTask<Void>() {
                private String op;

                @Override
                protected boolean handle() {
                    try {
                        result = null;
                        resultValue = AppValues.InvalidDouble;
                        op = ((RadioButton) opGroup.getSelectedToggle()).getText();
                        if (message("Transpose").equals(op)) {
                            result = MatrixDoubleTools.transpose(editController.matrixDouble());

                        } else if (message("RowEchelonForm").equals(op)) {
                            result = MatrixDoubleTools.rowEchelonForm(editController.matrixDouble());

                        } else if (message("ReducedRowEchelonForm").equals(op)) {
                            result = MatrixDoubleTools.reducedRowEchelonForm(editController.matrixDouble());

                        } else if (message("ComplementMinor").equals(op)) {
                            result = MatrixDoubleTools.complementMinor(editController.matrixDouble(), row - 1, column - 1);

                        } else if (message("Normalize").equals(op)) {
                            result = MatrixDoubleTools.normalize(editController.matrixDouble());

                        } else if (message("MultiplyNumber").equals(op)) {
                            result = MatrixDoubleTools.multiply(editController.matrixDouble(), number);

                        } else if (message("DivideNumber").equals(op)) {
                            result = MatrixDoubleTools.divide(editController.matrixDouble(), number);

                        } else if (message("DeterminantByElimination").equals(op)) {
                            resultValue = MatrixDoubleTools.determinantByElimination(editController.matrixDouble());

                        } else if (message("DeterminantByComplementMinor").equals(op)) {
                            resultValue = MatrixDoubleTools.determinantByComplementMinor(editController.matrixDouble());

                        } else if (message("InverseMatrixByElimination").equals(op)) {
                            result = MatrixDoubleTools.inverseByElimination(editController.matrixDouble());

                        } else if (message("InverseMatrixByAdjoint").equals(op)) {
                            result = MatrixDoubleTools.inverseByAdjoint(editController.matrixDouble());

                        } else if (message("MatrixRank").equals(op)) {
                            resultValue = MatrixDoubleTools.rank(editController.matrixDouble());

                        } else if (message("AdjointMatrix").equals(op)) {
                            result = MatrixDoubleTools.adjoint(editController.matrixDouble());

                        } else if (message("Power").equals(op)) {
                            result = MatrixDoubleTools.power(editController.matrixDouble(), power);

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
                        resultTableController.idInput.clear();
                        resultTableController.loadMatrix(result);
                        if (resultTableController.autoNameCheck.isSelected()) {
                            resultTableController.nameInput.setText(editController.nameInput.getText() + " " + op);
                        }
                    } else if (resultValue != AppValues.InvalidDouble) {
                        resultBox.getChildren().add(resultArea);
                        resultArea.setText(resultValue + "");
                    }
                    refreshStyle(resultBox);
                }

            };
            start(task);
        }

    }
}
