package mara.mybox.controller;

import java.util.Date;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataMatrix;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.MatrixDoubleTools;
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
    protected ControlMatrixTable2 listController;
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
    @FXML
    protected TitledPane matrixAPane, matrixBPane;

    public MatricesBinaryCalculationController() {
        baseTitle = message("MatricesBinaryCalculation");
        TipsLabelKey = "Data2DTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            dataAController.setDataType(this, Data2D.Type.Matrix);
            dataAMatrix = (DataMatrix) dataAController.data2D;
            dataAController.createAction();
            dataAController.statusNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    matrixAPane.setText(message("MatrixA") + " - " + dataAMatrix.getDataName());
                }
            });

            dataBController.setDataType(this, Data2D.Type.Matrix);
            dataBMatrix = (DataMatrix) dataBController.data2D;
            dataBController.createAction();
            dataBController.statusNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    matrixBPane.setText(message("MatrixA") + " - " + dataBMatrix.getDataName());
                }
            });

            listController.setParameters(dataAController, dataBController);

            resultController.setDataType(this, Data2D.Type.Matrix);
            resultMatrix = (DataMatrix) resultController.data2D;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

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

        dataAController.statusNotify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                checkMatrices();
            }
        });

        dataBController.statusNotify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
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
    }

    protected boolean checkMatrices() {
        checkLabel.setText("");
        if (plusRadio.isSelected() || minusRadio.isSelected() || hadamardProductRadio.isSelected()) {
            if (dataAMatrix.getColsNumber() != dataBMatrix.getColsNumber()
                    || dataAMatrix.getRowsNumber() != dataBMatrix.getRowsNumber()) {
                checkLabel.setText(message("MatricesCannotCalculateShouldSame"));
                calculateButton.setDisable(true);
                return false;
            }

        } else if (multiplyRadio.isSelected()) {
            if (dataAMatrix.getColsNumber() != dataBMatrix.getRowsNumber()) {
                checkLabel.setText(message("MatricesCannotCalculateMultiply"));
                calculateButton.setDisable(true);
                return false;
            }

        } else if (verticalMergeRadio.isSelected()) {
            if (dataAMatrix.getColsNumber() != dataBMatrix.getColsNumber()) {
                checkLabel.setText(message("MatricesCannotCalculateShouldSameCols"));
                calculateButton.setDisable(true);
                return false;
            }

        } else if (horizontalMergeRadio.isSelected()) {
            if (dataAMatrix.getRowsNumber() != dataBMatrix.getRowsNumber()) {
                checkLabel.setText(message("MatricesCannotCalculateShouldSameRows"));
                calculateButton.setDisable(true);
                return false;
            }

        }
        calculateButton.setDisable(false);
        return true;
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
                            result = MatrixDoubleTools.add(dataAMatrix.toArray(), dataBMatrix.toArray());

                        } else if (minusRadio.isSelected()) {
                            result = MatrixDoubleTools.subtract(dataAMatrix.toArray(), dataBMatrix.toArray());

                        } else if (multiplyRadio.isSelected()) {
                            result = MatrixDoubleTools.multiply(dataAMatrix.toArray(), dataBMatrix.toArray());

                        } else if (hadamardProductRadio.isSelected()) {
                            result = MatrixDoubleTools.hadamardProduct(dataAMatrix.toArray(), dataBMatrix.toArray());

                        } else if (kroneckerProductRadio.isSelected()) {
                            result = MatrixDoubleTools.kroneckerProduct(dataAMatrix.toArray(), dataBMatrix.toArray());

                        } else if (verticalMergeRadio.isSelected()) {
                            result = MatrixDoubleTools.vertivalMerge(dataAMatrix.toArray(), dataBMatrix.toArray());

                        } else if (horizontalMergeRadio.isSelected()) {
                            result = MatrixDoubleTools.horizontalMerge(dataAMatrix.toArray(), dataBMatrix.toArray());

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
                    resultController.loadMatrix(result, false);
//                    if (resultController.autoNameCheck.isSelected()) {
//                        resultController.nameInput.setText(op);
//                    }
                }

            };
            start(task);
        }
    }

}
