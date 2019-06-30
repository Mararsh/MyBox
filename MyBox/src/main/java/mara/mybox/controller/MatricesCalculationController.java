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
import mara.mybox.controller.base.BaseController;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.MatrixTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;

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
        baseTitle = AppVaribles.getMessage("MatricesCalculation");

    }

    @Override
    public void initializeNext() {
        matrixAController.parentController = this;
        matrixBController.parentController = this;
        matrixResultController.parentController = this;

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
                        AppVaribles.setUserConfigInt("MatrixDecimalScale", scale);
                        popInformation(getMessage("Successful"), 1000);
                    }
                } catch (Exception e) {
                    scaleInput.setStyle(badStyle);
                }
            }
        });
        int p = AppVaribles.getUserConfigInt("MatrixDecimalScale", 6);
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
        if (getMessage("UnaryCalculation").equals(selected.getText())) {
            dataPane.getItems().removeAll(bpBox, cpBox);
        } else if (getMessage("BinaryCalculation").equals(selected.getText())) {
            dataPane.getItems().addAll(bpBox, cpBox);
            refreshStyle(dataPane);
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
            popError(getMessage("InvalidData"));
            matrixResultController.bottomLabel.setText(getMessage("Failed") + ":"
                    + getMessage("VerticalMerge") + "  " + getMessage("InvalidData"));
            return;
        }
        matrixResultController.matrix = result;
        matrixResultController.setMatrix();
        long cost = new Date().getTime() - startTime;
        matrixResultController.bottomLabel.setText(AppVaribles.getMessage("Successful") + ":"
                + getMessage("VerticalMerge") + "  " + getMessage("Cost") + ":" + cost + " ms");
    }

    @FXML
    public void horizontalMergeAction() {
        long startTime = new Date().getTime();
        double[][] result = MatrixTools.horizontalMerge(matrixAController.matrix, matrixBController.matrix);
        if (result == null) {
            popError(getMessage("InvalidData"));
            matrixResultController.bottomLabel.setText(getMessage("Failed") + ":"
                    + getMessage("HorizontalMerge") + "  " + getMessage("InvalidData"));
            return;
        }
        matrixResultController.matrix = result;
        matrixResultController.setMatrix();
        long cost = new Date().getTime() - startTime;
        matrixResultController.bottomLabel.setText(AppVaribles.getMessage("Successful") + ":"
                + getMessage("HorizontalMerge") + "  " + getMessage("Cost") + ":" + cost + " ms");
    }

    @FXML
    public void plusAction() {
        long startTime = new Date().getTime();
        double[][] result = MatrixTools.add(matrixAController.matrix, matrixBController.matrix);
        if (result == null) {
            popError(getMessage("InvalidData"));
            matrixResultController.bottomLabel.setText(getMessage("Failed") + ":"
                    + getMessage("Plus") + "  " + getMessage("InvalidData"));
            return;
        }
        matrixResultController.matrix = result;
        matrixResultController.setMatrix();
        long cost = new Date().getTime() - startTime;
        matrixResultController.bottomLabel.setText(AppVaribles.getMessage("Successful") + ":" + getMessage("Plus") + "  "
                + getMessage("Cost") + ":" + cost + " ms");
    }

    @FXML
    public void minusAction() {
        long startTime = new Date().getTime();
        double[][] result = MatrixTools.subtract(matrixAController.matrix, matrixBController.matrix);
        if (result == null) {
            popError(getMessage("InvalidData"));
            matrixResultController.bottomLabel.setText(getMessage("Failed") + ":"
                    + getMessage("Minus") + "  " + getMessage("InvalidData"));
            return;
        }
        matrixResultController.matrix = result;
        matrixResultController.setMatrix();
        long cost = new Date().getTime() - startTime;
        matrixResultController.bottomLabel.setText(AppVaribles.getMessage("Successful") + ":" + getMessage("Minus") + "  "
                + getMessage("Cost") + ":" + cost + " ms");

    }

    @FXML
    public void multiplyAction() {
        long startTime = new Date().getTime();
        double[][] result = MatrixTools.multiply(matrixAController.matrix, matrixBController.matrix);
        if (result == null) {
            popError(getMessage("InvalidData"));
            matrixResultController.bottomLabel.setText(getMessage("Failed") + ":"
                    + getMessage("Multiply") + "  " + getMessage("InvalidData"));
            return;
        }
        matrixResultController.matrix = result;
        matrixResultController.setMatrix();
        long cost = new Date().getTime() - startTime;
        matrixResultController.bottomLabel.setText(AppVaribles.getMessage("Successful") + ":" + getMessage("Multiply") + "  "
                + getMessage("Cost") + ":" + cost + " ms");
    }

    @FXML
    public void hadamardProductAction() {
        long startTime = new Date().getTime();
        double[][] result = MatrixTools.hadamardProduct(matrixAController.matrix, matrixBController.matrix);
        if (result == null) {
            popError(getMessage("InvalidData"));
            matrixResultController.bottomLabel.setText(getMessage("Failed") + ":"
                    + getMessage("HadamardProduct") + "  " + getMessage("InvalidData"));
            return;
        }
        matrixResultController.matrix = result;
        matrixResultController.setMatrix();
        long cost = new Date().getTime() - startTime;
        matrixResultController.bottomLabel.setText(AppVaribles.getMessage("Successful") + ":" + getMessage("HadamardProduct") + "  "
                + getMessage("Cost") + ":" + cost + " ms");
    }

    @FXML
    public void kroneckerProductAction() {
        long startTime = new Date().getTime();
        double[][] result = MatrixTools.kroneckerProduct(matrixAController.matrix, matrixBController.matrix);
        if (result == null) {
            popError(getMessage("InvalidData"));
            matrixResultController.bottomLabel.setText(getMessage("Failed") + ":"
                    + getMessage("KroneckerProduct") + "  " + getMessage("InvalidData"));
            return;
        }
        matrixResultController.matrix = result;
        matrixResultController.setMatrix();
        long cost = new Date().getTime() - startTime;
        matrixResultController.bottomLabel.setText(AppVaribles.getMessage("Successful") + ":" + getMessage("KroneckerProduct") + "  "
                + getMessage("Cost") + ":" + cost + " ms");
    }

}
