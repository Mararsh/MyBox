package mara.mybox.controller;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import mara.mybox.color.ColorConversion.SpaceType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

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
        baseTitle = Languages.message("ColorConversion");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            calculateButton.disableProperty().bind(Bindings.isEmpty(scaleInput.textProperty())
                    .or(scaleInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(Bindings.isEmpty(sourceController.vInput1.textProperty()))
                    .or(sourceController.vInput1.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(Bindings.isEmpty(sourceController.vInput2.textProperty()))
                    .or(sourceController.vInput2.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(Bindings.isEmpty(sourceController.vInput3.textProperty()))
                    .or(sourceController.vInput3.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(Bindings.isEmpty(sourceController.vInput4.textProperty()))
                    .or(sourceController.vInput4.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
        calculateArea.setText(Languages.message("SourceColor") + "\n");
        calculateArea.appendText("-------------------------------------------------\n");
        calculateArea.appendText(Languages.message("ColorSpace") + ": " + sourceController.spaceName + "\n");
        calculateArea.appendText(Languages.message("ReferenceWhite") + ": " + sourceController.white + "\n");
        calculateArea.appendText(Languages.message("GammaCorrection") + ": " + sourceController.gamma + "\n");
        if (sourceController.spaceType == SpaceType.RGB) {
            calculateArea.appendText(Languages.message("ColorValues") + ": "
                    + Languages.message("Red") + " = " + sourceController.d1 + " (" + (int) Math.round(sourceController.d1 * 255) + ")   "
                    + Languages.message("Green") + " = " + sourceController.d2 + " (" + (int) Math.round(sourceController.d2 * 255) + ")   "
                    + Languages.message("Blue") + " = " + sourceController.d3 + " (" + (int) Math.round(sourceController.d3 * 255) + ")   "
                    + "\n\n");

            calculateArea.appendText(Languages.message("ConvertedValues") + " - " + Languages.message("DefaultWhite") + "\n");
            calculateArea.appendText("-------------------------------------------------\n");

        }

    }

}
