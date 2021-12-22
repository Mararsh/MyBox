package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
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
import javafx.stage.Window;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataMatrix;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleMatrixTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-17
 * @License Apache License Version 2.0
 */
public class MatrixUnaryCalculationController extends MatricesManageController {

    protected DataMatrix dataMatrix, resultMatrix;
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
    protected RadioButton transposeRadio, DivideNumberRadio,
            DeterminantByEliminationRadio, DeterminantByComplementMinorRadio,
            InverseMatrixByEliminationRadio, InverseMatrixByAdjointRadio, MatrixRankRadio, AdjointMatrixRadio, PowerRadio,
            ComplementMinorRadio, MultiplyNumberRadio;
    @FXML
    protected Label resultLabel, checkLabel;
    @FXML
    protected ControlData2D resultController;
    @FXML
    protected TextArea resultArea;
    @FXML
    protected ScrollPane resultTablePane;

    public MatrixUnaryCalculationController() {
        baseTitle = message("MatrixUnaryCalculation");
        TipsLabelKey = "Data2DTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            dataMatrix = (DataMatrix) dataController.data2D;

            resultController.setDataType(null, Data2D.Type.Matrix);
            resultMatrix = (DataMatrix) resultController.data2D;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            dataController.statusNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    checkMatrix();
                }
            });

            resultController.savedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    listController.refreshAction();
                }
            });

            resultBox.getChildren().clear();

            row = UserConfig.getInt(baseName + "Row", 1);
            rowInput.setText(row + "");
            rowInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        calculateButton.setDisable(!checkXY());
                    });

            column = UserConfig.getInt(baseName + "Column", 1);
            columnInput.setText(column + "");
            columnInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        calculateButton.setDisable(!checkXY());
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
                    calculateButton.setDisable(false);
                } catch (Exception e) {
                    numberInput.setStyle(UserConfig.badStyle());
                    calculateButton.setDisable(true);
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
                        calculateButton.setDisable(false);
                    } else {
                        powerInput.setStyle(UserConfig.badStyle());
                        calculateButton.setDisable(true);
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
            if (v > 0 && v <= dataMatrix.tableRowsNumber()) {
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
            if (v > 0 && v <= dataMatrix.tableColsNumber()) {
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
        if (dataMatrix == null || !dataMatrix.isValid()) {
            return false;
        }
        setBox.getChildren().clear();
        rowInput.setStyle(null);
        columnInput.setStyle(null);
        numberInput.setStyle(null);
        powerInput.setStyle(null);
        checkLabel.setText("");

        if (DeterminantByEliminationRadio.isSelected() || DeterminantByComplementMinorRadio.isSelected()
                || InverseMatrixByEliminationRadio.isSelected() || InverseMatrixByAdjointRadio.isSelected()
                || MatrixRankRadio.isSelected() || AdjointMatrixRadio.isSelected() || PowerRadio.isSelected()) {
            if (!dataMatrix.isSquare()) {
                checkLabel.setText(message("MatricesCannotCalculateShouldSqure"));
                calculateButton.setDisable(true);
                return false;
            }

        } else if (ComplementMinorRadio.isSelected()) {
            setBox.getChildren().add(xyBox);
            if (!checkXY()) {
                checkLabel.setText(message("InvalidParameters"));
                calculateButton.setDisable(true);
                return false;
            }

        } else if (MultiplyNumberRadio.isSelected() || DivideNumberRadio.isSelected()) {
            setBox.getChildren().add(numberBox);
            if (!checkNumber()) {
                checkLabel.setText(message("InvalidParameters"));
                calculateButton.setDisable(true);
                return false;
            }

        } else if (PowerRadio.isSelected()) {
            setBox.getChildren().add(powerBox);
            if (!checkPower()) {
                checkLabel.setText(message("InvalidParameters"));
                calculateButton.setDisable(true);
                return false;
            }
        }
        calculateButton.setDisable(false);
        return true;
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
            task = new SingletonTask<Void>(this) {
                private String op;

                @Override
                protected boolean handle() {
                    try {
                        result = null;
                        resultValue = AppValues.InvalidDouble;
                        op = ((RadioButton) opGroup.getSelectedToggle()).getText();
                        if (message("Transpose").equals(op)) {
                            result = DoubleMatrixTools.transpose(dataMatrix.toArray());

                        } else if (message("RowEchelonForm").equals(op)) {
                            result = DoubleMatrixTools.rowEchelonForm(dataMatrix.toArray());

                        } else if (message("ReducedRowEchelonForm").equals(op)) {
                            result = DoubleMatrixTools.reducedRowEchelonForm(dataMatrix.toArray());

                        } else if (message("ComplementMinor").equals(op)) {
                            result = DoubleMatrixTools.complementMinor(dataMatrix.toArray(), row - 1, column - 1);

                        } else if (message("Normalize").equals(op)) {
                            result = DoubleMatrixTools.normalizeSum(dataMatrix.toArray());

                        } else if (message("MultiplyNumber").equals(op)) {
                            result = DoubleMatrixTools.multiply(dataMatrix.toArray(), number);

                        } else if (message("DivideNumber").equals(op)) {
                            result = DoubleMatrixTools.divide(dataMatrix.toArray(), number);

                        } else if (message("DeterminantByElimination").equals(op)) {
                            resultValue = DoubleMatrixTools.determinantByElimination(dataMatrix.toArray());

                        } else if (message("DeterminantByComplementMinor").equals(op)) {
                            resultValue = DoubleMatrixTools.determinantByComplementMinor(dataMatrix.toArray());

                        } else if (message("InverseMatrixByElimination").equals(op)) {
                            result = DoubleMatrixTools.inverseByElimination(dataMatrix.toArray());

                        } else if (message("InverseMatrixByAdjoint").equals(op)) {
                            result = DoubleMatrixTools.inverseByAdjoint(dataMatrix.toArray());

                        } else if (message("MatrixRank").equals(op)) {
                            resultValue = DoubleMatrixTools.rank(dataMatrix.toArray());

                        } else if (message("AdjointMatrix").equals(op)) {
                            result = DoubleMatrixTools.adjoint(dataMatrix.toArray());

                        } else if (message("Power").equals(op)) {
                            result = DoubleMatrixTools.power(dataMatrix.toArray(), power);

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
//                        if (resultTableController.autoNameCheck.isSelected()) {
//                            resultTableController.nameInput.setText(dataMatrix.getDataName() + " " + op);
//                        }
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
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (MatrixUnaryCalculationController) WindowTools.openStage(Fxmls.MatrixUnaryCalculationFxml);
        }
        return controller;
    }

    public static MatrixUnaryCalculationController open() {
        MatrixUnaryCalculationController controller = oneOpen();
        controller.createAction();
        return controller;
    }

}
