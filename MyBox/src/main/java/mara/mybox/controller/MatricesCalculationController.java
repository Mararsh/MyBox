package mara.mybox.controller;

import java.util.Date;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.MatrixTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-5-18
 * @Description
 * @License Apache License Version 2.0
 */
public class MatricesCalculationController extends BaseController {

    public int scale = 6;

    @FXML
    private MatrixCalculationController matrixAController, matrixBController, matrixResultController;
    @FXML
    private Button matrixPlusButton, matrixMinusButton, matrixMultiplyButton,
            hadamardProductButton, kroneckerProductButton, verticalMergeButton, horizontalMergeButton;
    @FXML
    private TextField scaleInput;
    @FXML
    private VBox mainBox, bpBox, cpBox;
    @FXML
    private ToggleGroup calTypeGroup;
    @FXML
    private SplitPane dataPane;

    public MatricesCalculationController() {
        baseTitle = AppVariables.message("MatricesCalculation");

    }

    @Override
    public void initializeNext() {
        matrixAController.setParentController(this);
        matrixBController.setParentController(this);
        matrixResultController.setParentController(this);

        if (mainBox.getChildren().contains(cpBox)) {
            mainBox.getChildren().remove(cpBox);
        }

        calTypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkCalculationType();
            }
        });
        checkCalculationType();

        scaleInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    int p = Integer.parseInt(scaleInput.getText());
                    if (p <= 0) {
                        scaleInput.setStyle(badStyle);
                    } else {
                        scale = p;
                        matrixAController.scale = scale;
                        matrixBController.scale = scale;
                        matrixResultController.scale = scale;
                        scaleInput.setStyle(null);
                        AppVariables.setUserConfigInt("MatrixDecimalScale", scale);
                        popInformation(message("Successful"), 1000);
                    }
                } catch (Exception e) {
                    scaleInput.setStyle(badStyle);
                }
            }
        });
        int p = AppVariables.getUserConfigInt("MatrixDecimalScale", 6);
        scaleInput.setText(p + "");

        matrixPlusButton.disableProperty().bind(Bindings.isEmpty(matrixAController.valueArea.textProperty())
                .or(Bindings.isEmpty(matrixBController.valueArea.textProperty()))
                .or(scaleInput.styleProperty().isEqualTo(badStyle))
        );
        matrixMinusButton.disableProperty().bind(matrixPlusButton.disableProperty());
        matrixMultiplyButton.disableProperty().bind(matrixPlusButton.disableProperty());
        hadamardProductButton.disableProperty().bind(matrixPlusButton.disableProperty());
        kroneckerProductButton.disableProperty().bind(matrixPlusButton.disableProperty());
        verticalMergeButton.disableProperty().bind(matrixPlusButton.disableProperty());
        horizontalMergeButton.disableProperty().bind(matrixPlusButton.disableProperty());

    }

    private void checkCalculationType() {
        RadioButton selected = (RadioButton) calTypeGroup.getSelectedToggle();
        if (message("UnaryCalculation").equals(selected.getText())) {
            dataPane.getItems().removeAll(bpBox, cpBox);
        } else if (message("BinaryCalculation").equals(selected.getText())) {
            dataPane.getItems().addAll(bpBox, cpBox);
            FxmlControl.refreshStyle(dataPane);
            dataPane.setDividerPositions(0.333f, 0.666f);
        }
    }

    @FXML
    public void copyAsAAction() {
        matrixAController.editArea.setText(matrixResultController.valueArea.getText());
        matrixAController.editOKAction();
    }

    @FXML
    public void copyAsBAction() {
        matrixBController.editArea.setText(matrixResultController.valueArea.getText());
        matrixBController.editOKAction();
    }

    @FXML
    public void verticalMergeAction() {
        long startTime = new Date().getTime();
        double[][] result = MatrixTools.vertivalMerge(matrixAController.matrix, matrixBController.matrix);
        if (result == null) {
            popError(message("InvalidData"));
            matrixResultController.getBottomLabel().setText(message("Failed") + ":"
                    + message("VerticalMerge") + "  " + message("InvalidData"));
            return;
        }
        matrixResultController.matrix = result;
        matrixResultController.setMatrix();
        long cost = new Date().getTime() - startTime;
        matrixResultController.getBottomLabel().setText(AppVariables.message("Successful") + ":"
                + message("VerticalMerge") + "  " + message("Cost") + ":" + cost + " ms");
    }

    @FXML
    public void horizontalMergeAction() {
        long startTime = new Date().getTime();
        double[][] result = MatrixTools.horizontalMerge(matrixAController.matrix, matrixBController.matrix);
        if (result == null) {
            popError(message("InvalidData"));
            matrixResultController.getBottomLabel().setText(message("Failed") + ":"
                    + message("HorizontalMerge") + "  " + message("InvalidData"));
            return;
        }
        matrixResultController.matrix = result;
        matrixResultController.setMatrix();
        long cost = new Date().getTime() - startTime;
        matrixResultController.getBottomLabel().setText(AppVariables.message("Successful") + ":"
                + message("HorizontalMerge") + "  " + message("Cost") + ":" + cost + " ms");
    }

    @FXML
    public void plusAction() {
        long startTime = new Date().getTime();
        double[][] result = MatrixTools.add(matrixAController.matrix, matrixBController.matrix);
        if (result == null) {
            popError(message("InvalidData"));
            matrixResultController.getBottomLabel().setText(message("Failed") + ":"
                    + message("Plus") + "  " + message("InvalidData"));
            return;
        }
        matrixResultController.matrix = result;
        matrixResultController.setMatrix();
        long cost = new Date().getTime() - startTime;
        matrixResultController.getBottomLabel().setText(AppVariables.message("Successful") + ":" + message("Plus") + "  "
                + message("Cost") + ":" + cost + " ms");
    }

    @FXML
    public void minusAction() {
        long startTime = new Date().getTime();
        double[][] result = MatrixTools.subtract(matrixAController.matrix, matrixBController.matrix);
        if (result == null) {
            popError(message("InvalidData"));
            matrixResultController.getBottomLabel().setText(message("Failed") + ":"
                    + message("Minus") + "  " + message("InvalidData"));
            return;
        }
        matrixResultController.matrix = result;
        matrixResultController.setMatrix();
        long cost = new Date().getTime() - startTime;
        matrixResultController.getBottomLabel().setText(AppVariables.message("Successful") + ":" + message("Minus") + "  "
                + message("Cost") + ":" + cost + " ms");

    }

    @FXML
    public void multiplyAction() {
        long startTime = new Date().getTime();
        double[][] result = MatrixTools.multiply(matrixAController.matrix, matrixBController.matrix);
        if (result == null) {
            popError(message("InvalidData"));
            matrixResultController.getBottomLabel().setText(message("Failed") + ":"
                    + message("Multiply") + "  " + message("InvalidData"));
            return;
        }
        matrixResultController.matrix = result;
        matrixResultController.setMatrix();
        long cost = new Date().getTime() - startTime;
        matrixResultController.getBottomLabel().setText(AppVariables.message("Successful") + ":" + message("Multiply") + "  "
                + message("Cost") + ":" + cost + " ms");
    }

    @FXML
    public void hadamardProductAction() {
        long startTime = new Date().getTime();
        double[][] result = MatrixTools.hadamardProduct(matrixAController.matrix, matrixBController.matrix);
        if (result == null) {
            popError(message("InvalidData"));
            matrixResultController.getBottomLabel().setText(message("Failed") + ":"
                    + message("HadamardProduct") + "  " + message("InvalidData"));
            return;
        }
        matrixResultController.matrix = result;
        matrixResultController.setMatrix();
        long cost = new Date().getTime() - startTime;
        matrixResultController.getBottomLabel().setText(AppVariables.message("Successful") + ":" + message("HadamardProduct") + "  "
                + message("Cost") + ":" + cost + " ms");
    }

    @FXML
    public void kroneckerProductAction() {
        long startTime = new Date().getTime();
        double[][] result = MatrixTools.kroneckerProduct(matrixAController.matrix, matrixBController.matrix);
        if (result == null) {
            popError(message("InvalidData"));
            matrixResultController.getBottomLabel().setText(message("Failed") + ":"
                    + message("KroneckerProduct") + "  " + message("InvalidData"));
            return;
        }
        matrixResultController.matrix = result;
        matrixResultController.setMatrix();
        long cost = new Date().getTime() - startTime;
        matrixResultController.getBottomLabel().setText(AppVariables.message("Successful") + ":" + message("KroneckerProduct") + "  "
                + message("Cost") + ":" + cost + " ms");
    }

}
