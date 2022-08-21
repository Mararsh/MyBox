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
import javafx.scene.control.Tab;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.stage.Window;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
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

    protected DataMatrix dataAMatrix, dataBMatrix, resultMatrix;
    protected double[][] result;

    @FXML
    protected ControlMatrixTable listController;
    @FXML
    protected Tab dataATab, dataBTab, resultTab;
    @FXML
    protected ControlData2D dataAController, dataBController, resultController;
    @FXML
    protected ToggleGroup opGroup;
    @FXML
    protected RadioButton plusRadio, minusRadio, multiplyRadio,
            hadamardProductRadio, kroneckerProductRadio, verticalMergeRadio, horizontalMergeRadio;
    @FXML
    protected Label matrixLabel, resultLabel, checkLabel;
    @FXML
    protected Button matrixAButton, matrixBButton, calculateButton;

    public MatricesBinaryCalculationController() {
        baseTitle = message("MatricesBinaryCalculation");
        TipsLabelKey = "Data2DTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            dataAController.setDataType(null, Data2D.Type.Matrix);
            dataAMatrix = (DataMatrix) dataAController.data2D;
            dataAController.createAction();

            dataBController.setDataType(null, Data2D.Type.Matrix);
            dataBMatrix = (DataMatrix) dataBController.data2D;
            dataBController.createAction();

            listController.setParameters(this);

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

            dataAController.statusNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    tabPane.getSelectionModel().select(dataATab);
                    checkMatrices();
                }
            });

            dataBController.statusNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    tabPane.getSelectionModel().select(dataBTab);
                    checkMatrices();
                }
            });

            dataAController.savedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    listController.refreshAction();
                }
            });

            dataBController.savedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    listController.refreshAction();
                }
            });

            resultController.savedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    listController.refreshAction();
                }
            });

            opGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> ov, Toggle oldValue, Toggle newValue) -> {
                        checkMatrices();
                    });
            checkMatrices();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();

    }

    protected boolean checkMatrices() {
        checkLabel.setText("");
        if (plusRadio.isSelected() || minusRadio.isSelected() || hadamardProductRadio.isSelected()) {
            if (dataAMatrix.tableColsNumber() != dataBMatrix.tableColsNumber()
                    || dataAMatrix.tableRowsNumber() != dataBMatrix.tableRowsNumber()) {
                checkLabel.setText(message("MatricesCannotCalculateShouldSame"));
                calculateButton.setDisable(true);
                return false;
            }

        } else if (multiplyRadio.isSelected()) {
            if (dataAMatrix.tableColsNumber() != dataBMatrix.tableRowsNumber()) {
                checkLabel.setText(message("MatricesCannotCalculateMultiply"));
                calculateButton.setDisable(true);
                return false;
            }

        } else if (verticalMergeRadio.isSelected()) {
            if (dataAMatrix.tableColsNumber() != dataBMatrix.tableColsNumber()) {
                checkLabel.setText(message("MatricesCannotCalculateShouldSameCols"));
                calculateButton.setDisable(true);
                return false;
            }

        } else if (horizontalMergeRadio.isSelected()) {
            if (dataAMatrix.tableRowsNumber() != dataBMatrix.tableRowsNumber()) {
                checkLabel.setText(message("MatricesCannotCalculateShouldSameRows"));
                calculateButton.setDisable(true);
                return false;
            }

        }
        calculateButton.setDisable(false);
        return true;
    }

    @FXML
    @Override
    public void createAction() {
        dataAController.create();
        dataBController.create();
    }

    @FXML
    public void calculateAction() {
        if (!checkMatrices()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            resultLabel.setText("");
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    try {
                        if (plusRadio.isSelected()) {
                            result = DoubleMatrixTools.add(dataAMatrix.toMatrix(), dataBMatrix.toMatrix());

                        } else if (minusRadio.isSelected()) {
                            result = DoubleMatrixTools.subtract(dataAMatrix.toMatrix(), dataBMatrix.toMatrix());

                        } else if (multiplyRadio.isSelected()) {
                            result = DoubleMatrixTools.multiply(dataAMatrix.toMatrix(), dataBMatrix.toMatrix());

                        } else if (hadamardProductRadio.isSelected()) {
                            result = DoubleMatrixTools.hadamardProduct(dataAMatrix.toMatrix(), dataBMatrix.toMatrix());

                        } else if (kroneckerProductRadio.isSelected()) {
                            result = DoubleMatrixTools.kroneckerProduct(dataAMatrix.toMatrix(), dataBMatrix.toMatrix());

                        } else if (verticalMergeRadio.isSelected()) {
                            result = DoubleMatrixTools.vertivalMerge(dataAMatrix.toMatrix(), dataBMatrix.toMatrix());

                        } else if (horizontalMergeRadio.isSelected()) {
                            result = DoubleMatrixTools.horizontalMerge(dataAMatrix.toMatrix(), dataBMatrix.toMatrix());

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
