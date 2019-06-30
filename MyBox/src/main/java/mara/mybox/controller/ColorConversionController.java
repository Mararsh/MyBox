package mara.mybox.controller;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import mara.mybox.controller.base.ChromaticityBaseController;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.color.ColorConversion.SpaceType;

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
        baseTitle = AppVaribles.getMessage("ColorConversion");
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
        calculateArea.setText(getMessage("SourceColor") + "\n");
        calculateArea.appendText("-------------------------------------------------\n");
        calculateArea.appendText(getMessage("ColorSpace") + ": " + sourceController.spaceName + "\n");
        calculateArea.appendText(getMessage("ReferenceWhite") + ": " + sourceController.white + "\n");
        calculateArea.appendText(getMessage("GammaCorrection") + ": " + sourceController.gamma + "\n");
        if (sourceController.spaceType == SpaceType.RGB) {
            calculateArea.appendText(getMessage("ColorValues") + ": "
                    + getMessage("Red") + " = " + sourceController.d1 + " (" + (int) Math.round(sourceController.d1 * 255) + ")   "
                    + getMessage("Green") + " = " + sourceController.d2 + " (" + (int) Math.round(sourceController.d2 * 255) + ")   "
                    + getMessage("Blue") + " = " + sourceController.d3 + " (" + (int) Math.round(sourceController.d3 * 255) + ")   "
                    + "\n\n");

            calculateArea.appendText(getMessage("ConvertedValues") + " - " + getMessage("DefaultWhite") + "\n");
            calculateArea.appendText("-------------------------------------------------\n");

        }

    }

}
