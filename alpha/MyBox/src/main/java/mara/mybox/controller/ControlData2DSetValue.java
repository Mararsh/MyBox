package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import mara.mybox.data2d.Data2D;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public class ControlData2DSetValue extends BaseController {

    protected BaseData2DHandleController handleController;
    protected Data2D data2D;
    protected String value;

    @FXML
    protected ToggleGroup valueGroup;
    @FXML
    protected RadioButton zeroRadio, oneRadio, blankRadio, randomRadio, randomNnRadio,
            expressionRadio, columnMeanRadio, columnModeRadio, columnMedianRadio,
            setRadio, gaussianDistributionRadio, identifyRadio, upperTriangleRadio, lowerTriangleRadio;
    @FXML
    protected TextField valueInput;
    @FXML
    protected FlowPane matrixPane;
    @FXML
    protected ControlData2DRowExpression expressionController;
    @FXML
    protected CheckBox errorContinueCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();

            value = UserConfig.getString(baseName + "Value", "0");
            if (value == null) {
                value = "0";
            }
            switch (value) {
                case "0":
                    zeroRadio.fire();
                    break;
                case "1":
                    oneRadio.fire();
                    break;
                case "MyBox##blank":
                    blankRadio.fire();
                    break;
                case "MyBox##random":
                    randomRadio.fire();
                    break;
                case "MyBox##randomNn":
                    randomNnRadio.fire();
                    break;
                case "MyBox##gaussianDistribution":
                    gaussianDistributionRadio.fire();
                    break;
                case "MyBox##identify":
                    identifyRadio.fire();
                    break;
                case "MyBox##upperTriangle":
                    upperTriangleRadio.fire();
                    break;
                case "MyBox##lowerTriangle":
                    lowerTriangleRadio.fire();
                    break;
                case "MyBox##columnMean":
                    columnMeanRadio.fire();
                    break;
                case "MyBox##columnMode":
                    columnModeRadio.fire();
                    break;
                case "MyBox##columnMedian":
                    columnMedianRadio.fire();
                    break;
                default:
                    if (value.startsWith("MyBox##Expression")) {
                        valueInput.setText(value.substring("MyBox##Expression".length()));
                        expressionRadio.fire();
                    } else {
                        valueInput.setText(value);
                        setRadio.fire();
                    }
            }
            valueGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkValue();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameter(BaseData2DHandleController handleController) {
        try {
            this.handleController = handleController;
            expressionController.calculator.setWebEngine(handleController.rowFilterController.rowFilter.webEngine);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void checkValue() {
        try {
            if (isSettingValues) {
                return;
            }
            value = null;
            valueInput.setStyle(null);
            if (setRadio.isSelected()) {
                value = valueInput.getText();
            } else if (zeroRadio.isSelected()) {
                value = "0";
            } else if (oneRadio.isSelected()) {
                value = "1";
            } else if (blankRadio.isSelected()) {
                value = "MyBox##blank";
            } else if (randomRadio.isSelected()) {
                value = "MyBox##random";
            } else if (randomNnRadio.isSelected()) {
                value = "MyBox##randomNn";
            } else if (gaussianDistributionRadio.isSelected()) {
                value = "MyBox##gaussianDistribution";
            } else if (identifyRadio.isSelected()) {
                value = "MyBox##identify";
            } else if (upperTriangleRadio.isSelected()) {
                value = "MyBox##upperTriangle";
            } else if (lowerTriangleRadio.isSelected()) {
                value = "MyBox##lowerTriangle";
            } else if (columnMeanRadio.isSelected()) {
                value = "MyBox##columnMean";
            } else if (columnModeRadio.isSelected()) {
                value = "MyBox##columnMode";
            } else if (columnMedianRadio.isSelected()) {
                value = "MyBox##columnMedian";
            } else if (expressionRadio.isSelected()) {
                value = "MyBox##Expression##" + expressionController.scriptInput.getText();
            }
            if (value != null && !value.isBlank()) {
                UserConfig.setString(baseName + "Value", value);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void showMatrixPane(boolean show) {
        if (show) {
            if (!thisPane.getChildren().contains(matrixPane)) {
                thisPane.getChildren().add(1, matrixPane);
            }
        } else {
            if (thisPane.getChildren().contains(matrixPane)) {
                thisPane.getChildren().remove(matrixPane);
            }
        }
    }

    public void setData2D(Data2D data2D) {
        this.data2D = data2D;
        expressionController.setData2D(data2D);
    }

    public boolean checkSelection() {
        if (handleController == null) {
            return true;
        }
        if (thisPane.getChildren().contains(matrixPane)) {
            if (handleController.isAllPages()) {
                matrixPane.setDisable(true);
                if (gaussianDistributionRadio.isSelected() || identifyRadio.isSelected()
                        || upperTriangleRadio.isSelected() || lowerTriangleRadio.isSelected()) {
                    zeroRadio.fire();
                    return false;
                }
            } else {
                boolean isSquare = handleController.isSquare();
                boolean canGD = isSquare && handleController.checkedColsIndices.size() % 2 != 0;
                gaussianDistributionRadio.setDisable(!canGD);
                identifyRadio.setDisable(!isSquare);
                upperTriangleRadio.setDisable(!isSquare);
                lowerTriangleRadio.setDisable(!isSquare);
                if (!isSquare) {
                    if (gaussianDistributionRadio.isSelected() || identifyRadio.isSelected()
                            || upperTriangleRadio.isSelected() || lowerTriangleRadio.isSelected()) {
                        zeroRadio.fire();
                        return false;
                    }
                }
                if (!canGD) {
                    if (gaussianDistributionRadio.isSelected()) {
                        zeroRadio.fire();
                        return false;
                    }
                }
                matrixPane.setDisable(false);
            }
        }
        checkValue();
        if (value == null) {
            return false;
        } else {
            boolean ok = true;
            if (expressionRadio.isSelected()) {
                ok = expressionController.checkExpression(handleController.isAllPages());
                if (!ok && data2D.getError() != null) {
                    handleController.infoLabel.setText(message("Invalid") + ": " + message("RowExpression") + "\n"
                            + data2D.getError());
                }
            }
            return ok;
        }
    }

}
