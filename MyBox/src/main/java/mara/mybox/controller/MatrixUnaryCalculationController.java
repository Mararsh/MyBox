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
import javafx.stage.Modality;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.MatrixDoubleTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

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
    protected BaseMatrixController resultTableController;
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
            resultTableController.setManager(listController);
            resultBox.getChildren().clear();

            row = AppVariables.getUserConfigInt(baseName + "Row", 1);
            rowInput.setText(row + "");
            rowInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        checkXY();
                    });

            column = AppVariables.getUserConfigInt(baseName + "Column", 1);
            columnInput.setText(column + "");
            columnInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        checkXY();
                    });

            try {
                number = Double.parseDouble(AppVariables.getUserConfigValue(baseName + "Number", "2"));
            } catch (Exception e) {
                number = 2d;
            }
            numberInput.setText(number + "");
            numberInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            number = Double.parseDouble(newValue);
                            numberInput.setStyle(null);
                            AppVariables.setUserConfigValue(baseName + "Number", number + "");
                        } catch (Exception e) {
                            numberInput.setStyle(badStyle);
                        }
                    });

            try {
                power = AppVariables.getUserConfigInt(baseName + "Power", 2);
            } catch (Exception e) {
                power = 2;
            }
            powerInput.setText(power + "");
            powerInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 1) {
                                power = v;
                                powerInput.setStyle(null);
                                AppVariables.setUserConfigInt(baseName + "Power", power);
                            } else {
                                powerInput.setStyle(badStyle);
                            }
                        } catch (Exception e) {
                            powerInput.setStyle(badStyle);
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
            if (v > 0 && v <= editController.matrix().length) {
                row = v;
                rowInput.setStyle(null);
                AppVariables.setUserConfigInt(baseName + "Row", v);
            } else {
                rowInput.setStyle(badStyle);
                valid = false;
            }
        } catch (Exception e) {
            rowInput.setStyle(badStyle);
            valid = false;
        }
        try {
            int v = Integer.parseInt(columnInput.getText().trim());
            if (v > 0 && v <= editController.matrix().length) {
                column = v;
                columnInput.setStyle(null);
                AppVariables.setUserConfigInt(baseName + "Column", v);
            } else {
                columnInput.setStyle(badStyle);
                valid = false;
            }
        } catch (Exception e) {
            columnInput.setStyle(badStyle);
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
                numberInput.setStyle(badStyle);
                return false;
            }
            numberInput.setStyle(null);
            AppVariables.setUserConfigValue(baseName + "Number", number + "");
            return true;
        } catch (Exception e) {
            numberInput.setStyle(badStyle);
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
                AppVariables.setUserConfigInt(baseName + "Power", power);
                return true;
            } else {
                powerInput.setStyle(badStyle);
                return false;
            }
        } catch (Exception e) {
            powerInput.setStyle(badStyle);
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

        editController.notify.addListener(
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
                        resultValue = CommonValues.InvalidDouble;
                        op = ((RadioButton) opGroup.getSelectedToggle()).getText();
                        if (message("Transpose").equals(op)) {
                            result = MatrixDoubleTools.transpose(editController.matrix());

                        } else if (message("RowEchelonForm").equals(op)) {
                            result = MatrixDoubleTools.rowEchelonForm(editController.matrix());

                        } else if (message("ReducedRowEchelonForm").equals(op)) {
                            result = MatrixDoubleTools.reducedRowEchelonForm(editController.matrix());

                        } else if (message("ComplementMinor").equals(op)) {
                            result = MatrixDoubleTools.complementMinor(editController.matrix(), row - 1, column - 1);

                        } else if (message("Normalize").equals(op)) {
                            result = MatrixDoubleTools.normalize(editController.matrix());

                        } else if (message("MultiplyNumber").equals(op)) {
                            result = MatrixDoubleTools.multiply(editController.matrix(), number);

                        } else if (message("DivideNumber").equals(op)) {
                            result = MatrixDoubleTools.divide(editController.matrix(), number);

                        } else if (message("DeterminantByElimination").equals(op)) {
                            resultValue = MatrixDoubleTools.determinantByElimination(editController.matrix());

                        } else if (message("DeterminantByComplementMinor").equals(op)) {
                            resultValue = MatrixDoubleTools.determinantByComplementMinor(editController.matrix());

                        } else if (message("InverseMatrixByElimination").equals(op)) {
                            result = MatrixDoubleTools.inverseByElimination(editController.matrix());

                        } else if (message("InverseMatrixByAdjoint").equals(op)) {
                            result = MatrixDoubleTools.inverseByAdjoint(editController.matrix());

                        } else if (message("MatrixRank").equals(op)) {
                            resultValue = MatrixDoubleTools.rank(editController.matrix());

                        } else if (message("AdjointMatrix").equals(op)) {
                            result = MatrixDoubleTools.adjoint(editController.matrix());

                        } else if (message("Power").equals(op)) {
                            result = MatrixDoubleTools.power(editController.matrix(), power);

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
                    } else if (resultValue != CommonValues.InvalidDouble) {
                        resultBox.getChildren().add(resultArea);
                        resultArea.setText(resultValue + "");
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }
}
