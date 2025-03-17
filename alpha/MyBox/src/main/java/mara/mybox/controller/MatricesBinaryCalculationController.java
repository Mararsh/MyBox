package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.stage.Window;
import mara.mybox.data2d.Data2D;
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
public class MatricesBinaryCalculationController extends BaseController {

    protected DataMatrix resultMatrix;
    protected double[][] sourceA, sourceB, result;

    @FXML
    protected Tab dataATab, dataBTab, resultTab;
    @FXML
    protected ControlData2DMatrix dataAController, dataBController;
    @FXML
    protected ControlData2DView resultController;
    @FXML
    protected ToggleGroup opGroup;
    @FXML
    protected RadioButton plusRadio, minusRadio, multiplyRadio,
            hadamardProductRadio, kroneckerProductRadio, verticalMergeRadio, horizontalMergeRadio;
    @FXML
    protected Label matrixLabel, resultLabel, checkLabel;
    @FXML
    protected Button matrixAButton, matrixBButton;

    public MatricesBinaryCalculationController() {
        baseTitle = message("MatricesBinaryCalculation");
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

            opGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> ov, Toggle oldValue, Toggle newValue) -> {
                        checkMatrices(false);
                    });
            checkMatrices(false);

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

    @FXML
    @Override
    public void createAction() {
        dataAController.createAction();
        dataBController.createAction();
    }

    protected void checkMatrices(boolean calculate) {
        if (!dataAController.checkSelections()) {
            popError(message("NoData") + ": " + message("MatrixA"));
            return;
        }
        if (!dataBController.checkSelections()) {
            popError(message("NoData") + ": " + message("MatrixB"));
            return;
        }
        checkLabel.setText("");
        if (task != null) {
            task.cancel();
        }
        resultLabel.setText("");
        sourceA = null;
        sourceB = null;
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    sourceA = dataAController.pickMatrix(this);
                    if (sourceA == null || sourceA.length == 0) {
                        return false;
                    }
                    sourceB = dataBController.pickMatrix(this);
                    if (sourceB == null || sourceB.length == 0) {
                        return false;
                    }
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
        if (sourceA == null || sourceA.length == 0) {
            handleError(message("InvalidData") + ": " + message("MatrixA"));
            return false;
        }
        if (sourceB == null || sourceB.length == 0) {
            handleError(message("InvalidData") + ": " + message("MatrixB"));
            return false;
        }
        checkLabel.setText("");
        int rowsNumberA = sourceA.length;
        int colsNumberA = sourceA[0].length;
        int rowsNumberB = sourceB.length;
        int colsNumberB = sourceB[0].length;
        if (plusRadio.isSelected() || minusRadio.isSelected() || hadamardProductRadio.isSelected()) {
            if (rowsNumberA != rowsNumberB || colsNumberA != colsNumberB) {
                handleError(message("MatricesCannotCalculateShouldSame"));
                return false;
            }

        } else if (multiplyRadio.isSelected()) {
            if (colsNumberA != rowsNumberB) {
                handleError(message("MatricesCannotCalculateMultiply"));
                return false;
            }

        } else if (verticalMergeRadio.isSelected()) {
            if (colsNumberA != colsNumberB) {
                handleError(message("MatricesCannotCalculateShouldSameCols"));
                return false;
            }

        } else if (horizontalMergeRadio.isSelected()) {
            if (rowsNumberA != rowsNumberB) {
                handleError(message("MatricesCannotCalculateShouldSameRows"));
                return false;
            }

        }

        return true;
    }

    @FXML
    public void calculateAction() {
        checkMatrices(true);
    }

    @FXML
    public void calculate() {
        if (sourceA == null || sourceA.length == 0) {
            handleError(message("InvalidData") + ": " + message("MatrixA"));
            return;
        }
        if (sourceB == null || sourceB.length == 0) {
            handleError(message("InvalidData") + ": " + message("MatrixB"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        resultLabel.setText("");
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    if (plusRadio.isSelected()) {
                        result = DoubleMatrixTools.add(sourceA, sourceB);

                    } else if (minusRadio.isSelected()) {
                        result = DoubleMatrixTools.subtract(sourceA, sourceB);

                    } else if (multiplyRadio.isSelected()) {
                        result = DoubleMatrixTools.multiply(sourceA, sourceB);

                    } else if (hadamardProductRadio.isSelected()) {
                        result = DoubleMatrixTools.hadamardProduct(sourceA, sourceB);

                    } else if (kroneckerProductRadio.isSelected()) {
                        result = DoubleMatrixTools.kroneckerProduct(sourceA, sourceB);

                    } else if (verticalMergeRadio.isSelected()) {
                        result = DoubleMatrixTools.vertivalMerge(sourceA, sourceB);

                    } else if (horizontalMergeRadio.isSelected()) {
                        result = DoubleMatrixTools.horizontalMerge(sourceA, sourceB);

                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return result != null;
            }

            @Override
            protected void whenSucceeded() {
                cost = new Date().getTime() - startTime.getTime();
                String op = ((RadioButton) opGroup.getSelectedToggle()).getText();
                resultLabel.setText(op + "  " + message("Cost") + ":" + DateTools.datetimeMsDuration(cost));
                resultController.loadMatrix(result);
                tabPane.getSelectionModel().select(resultTab);
            }

        };
        start(task);
    }

    /*
        static
     */
    public static MatricesBinaryCalculationController oneOpen() {
        MatricesBinaryCalculationController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof MatricesBinaryCalculationController) {
                try {
                    controller = (MatricesBinaryCalculationController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (MatricesBinaryCalculationController) WindowTools.openStage(Fxmls.MatricesBinaryCalculationFxml);
        }
        controller.requestMouse();
        return controller;
    }

}
