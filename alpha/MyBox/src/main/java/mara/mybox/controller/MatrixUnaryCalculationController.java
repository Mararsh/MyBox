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
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.MatrixDoubleTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Languages;
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
    protected ControlMatrix resultTableController;
    @FXML
    protected TextArea resultArea;
    @FXML
    protected ScrollPane resultTablePane;

    public MatrixUnaryCalculationController() {
        baseTitle = Languages.message("MatrixUnaryCalculation");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            resultTableController.initManager(listController);
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
                    numberInput.setStyle(NodeStyleTools.badStyle);
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
                        powerInput.setStyle(NodeStyleTools.badStyle);
                    }
                } catch (Exception e) {
                    powerInput.setStyle(NodeStyleTools.badStyle);
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
                UserConfig.setInt(baseName + "Row", v);
            } else {
                rowInput.setStyle(NodeStyleTools.badStyle);
                valid = false;
            }
        } catch (Exception e) {
            rowInput.setStyle(NodeStyleTools.badStyle);
            valid = false;
        }
        try {
            int v = Integer.parseInt(columnInput.getText().trim());
            if (v > 0 && v <= editController.matrix().length) {
                column = v;
                columnInput.setStyle(null);
                UserConfig.setInt(baseName + "Column", v);
            } else {
                columnInput.setStyle(NodeStyleTools.badStyle);
                valid = false;
            }
        } catch (Exception e) {
            columnInput.setStyle(NodeStyleTools.badStyle);
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
                numberInput.setStyle(NodeStyleTools.badStyle);
                return false;
            }
            numberInput.setStyle(null);
            UserConfig.setString(baseName + "Number", number + "");
            return true;
        } catch (Exception e) {
            numberInput.setStyle(NodeStyleTools.badStyle);
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
                powerInput.setStyle(NodeStyleTools.badStyle);
                return false;
            }
        } catch (Exception e) {
            powerInput.setStyle(NodeStyleTools.badStyle);
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
                && (Languages.message("DeterminantByElimination").equals(op)
                || Languages.message("DeterminantByComplementMinor").equals(op)
                || Languages.message("InverseMatrixByElimination").equals(op)
                || Languages.message("InverseMatrixByAdjoint").equals(op)
                || Languages.message("MatrixRank").equals(op)
                || Languages.message("AdjointMatrix").equals(op)
                || Languages.message("Power").equals(op))) {
            checkLabel.setText(Languages.message("MatricesCannotCalculateShouldSqure"));
            calculateButton.setDisable(true);
            return false;

        } else if (Languages.message("ComplementMinor").equals(op)) {
            setBox.getChildren().add(xyBox);
            if (!checkXY()) {
                checkLabel.setText(Languages.message("InvalidParameters"));
                return false;
            }

        } else if (Languages.message("MultiplyNumber").equals(op) || Languages.message("DivideNumber").equals(op)) {
            setBox.getChildren().add(numberBox);
            if (!checkNumber()) {
                checkLabel.setText(Languages.message("InvalidParameters"));
                return false;
            }

        } else if (Languages.message("Power").equals(op)) {
            setBox.getChildren().add(powerBox);
            if (!checkPower()) {
                checkLabel.setText(Languages.message("InvalidParameters"));
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
                        resultValue = AppValues.InvalidDouble;
                        op = ((RadioButton) opGroup.getSelectedToggle()).getText();
                        if (Languages.message("Transpose").equals(op)) {
                            result = MatrixDoubleTools.transpose(editController.matrix());

                        } else if (Languages.message("RowEchelonForm").equals(op)) {
                            result = MatrixDoubleTools.rowEchelonForm(editController.matrix());

                        } else if (Languages.message("ReducedRowEchelonForm").equals(op)) {
                            result = MatrixDoubleTools.reducedRowEchelonForm(editController.matrix());

                        } else if (Languages.message("ComplementMinor").equals(op)) {
                            result = MatrixDoubleTools.complementMinor(editController.matrix(), row - 1, column - 1);

                        } else if (Languages.message("Normalize").equals(op)) {
                            result = MatrixDoubleTools.normalize(editController.matrix());

                        } else if (Languages.message("MultiplyNumber").equals(op)) {
                            result = MatrixDoubleTools.multiply(editController.matrix(), number);

                        } else if (Languages.message("DivideNumber").equals(op)) {
                            result = MatrixDoubleTools.divide(editController.matrix(), number);

                        } else if (Languages.message("DeterminantByElimination").equals(op)) {
                            resultValue = MatrixDoubleTools.determinantByElimination(editController.matrix());

                        } else if (Languages.message("DeterminantByComplementMinor").equals(op)) {
                            resultValue = MatrixDoubleTools.determinantByComplementMinor(editController.matrix());

                        } else if (Languages.message("InverseMatrixByElimination").equals(op)) {
                            result = MatrixDoubleTools.inverseByElimination(editController.matrix());

                        } else if (Languages.message("InverseMatrixByAdjoint").equals(op)) {
                            result = MatrixDoubleTools.inverseByAdjoint(editController.matrix());

                        } else if (Languages.message("MatrixRank").equals(op)) {
                            resultValue = MatrixDoubleTools.rank(editController.matrix());

                        } else if (Languages.message("AdjointMatrix").equals(op)) {
                            result = MatrixDoubleTools.adjoint(editController.matrix());

                        } else if (Languages.message("Power").equals(op)) {
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
                    resultLabel.setText(op + "  " + Languages.message("Cost") + ":" + DateTools.datetimeMsDuration(cost));
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
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }
}
