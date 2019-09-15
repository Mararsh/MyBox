package mara.mybox.controller;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.color.ColorConversion.SpaceType;
import static mara.mybox.value.AppVariables.message;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-6-7
 * @Description
 * @License Apache License Version 2.0
 */
public class ColorConversionController extends ChromaticityBaseController {

    @FXML
    public ColorController sourceController;
    @FXML
    protected Button calculateButton, exportButton;
    @FXML
    protected TextArea calculateArea;

    public ColorConversionController() {
        baseTitle = AppVariables.message("ColorConversion");
    }

    @Override
    public void initializeNext() {
        try {
            calculateButton.disableProperty().bind(Bindings.isEmpty(scaleInput.textProperty())
                    .or(scaleInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(sourceController.vInput1.textProperty()))
                    .or(sourceController.vInput1.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(sourceController.vInput2.textProperty()))
                    .or(sourceController.vInput2.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(sourceController.vInput3.textProperty()))
                    .or(sourceController.vInput3.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(sourceController.vInput4.textProperty()))
                    .or(sourceController.vInput4.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void checkScale() {
        super.checkScale();
        sourceController.scale = scale;
    }

    @FXML
    public void calculateAction(ActionEvent event) {
        calculateArea.clear();
        if (calculateButton.isDisabled()) {
            return;
        }
        calculateArea.setText(message("SourceColor") + "\n");
        calculateArea.appendText("-------------------------------------------------\n");
        calculateArea.appendText(message("ColorSpace") + ": " + sourceController.spaceName + "\n");
        calculateArea.appendText(message("ReferenceWhite") + ": " + sourceController.white + "\n");
        calculateArea.appendText(message("GammaCorrection") + ": " + sourceController.gamma + "\n");
        if (sourceController.spaceType == SpaceType.RGB) {
            calculateArea.appendText(message("ColorValues") + ": "
                    + message("Red") + " = " + sourceController.d1 + " (" + (int) Math.round(sourceController.d1 * 255) + ")   "
                    + message("Green") + " = " + sourceController.d2 + " (" + (int) Math.round(sourceController.d2 * 255) + ")   "
                    + message("Blue") + " = " + sourceController.d3 + " (" + (int) Math.round(sourceController.d3 * 255) + ")   "
                    + "\n\n");

            calculateArea.appendText(message("ConvertedValues") + " - " + message("DefaultWhite") + "\n");
            calculateArea.appendText("-------------------------------------------------\n");

        }

    }

}
