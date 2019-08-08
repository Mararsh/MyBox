package mara.mybox.controller;

import java.awt.color.ColorSpace;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import mara.mybox.controller.base.BaseController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import mara.mybox.fxml.FxmlColor;
import static mara.mybox.fxml.FxmlColor.rgb2AlphaHex;
import static mara.mybox.fxml.FxmlColor.rgb2Hex;
import mara.mybox.color.ColorConversion;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.AppVaribles;

/**
 * @Author Mara
 * @CreateDate 2018-07-24
 * @Description
 * @License Apache License Version 2.0
 */
public class ColorPaletteController12 extends BaseController {

    @FXML
    private HBox paletteBox;
    @FXML
    private Button closeButton;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private TextField redInput, greenInput, blueInput, opacityInput,
            redRInput, greenRInput, blueRInput, opacityRInput,
            saturationInput, hueInput, brightnessInput,
            saturationRInput, hueRInput, brightnessRInput,
            xInput, yInput, zInput, argbInput, rgbInput,
            appleRedInput, appleBlueInput, appleGreenInput,
            pyccRedInput, pyccBlueInput, pyccGreenInput,
            cyanInput, magentaInput, yellowInput, blackInput,
            adobeRedInput, adobeGreenInput, adobeBlueInput;

    // http://brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html
    public ColorPaletteController12() {
        baseTitle = AppVaribles.message("ColorPalette");
    }

    @Override
    public void initializeNext() {
        try {
            colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> observable,
                        Color oldTab, Color newTab) {
                    if (isSettingValues) {
                        return;
                    }
                    checkColorPicker();
                }
            });
            colorPicker.setValue(Color.RED);

        } catch (Exception e) {

        }
    }

    @FXML
    private void checkColorPicker() {
        changeColor(colorPicker.getValue());
    }

    private void changeColor(Color color) {
        isSettingValues = true;

        redInput.setText(Math.round(color.getRed() * 255) + "");
        greenInput.setText(Math.round(color.getGreen() * 255) + "");
        blueInput.setText(Math.round(color.getBlue() * 255) + "");
        opacityInput.setText(Math.round(color.getOpacity() * 255) + "");

        float[] srgb = FxmlColor.toFloat(color);
        redRInput.setText(srgb[0] + "");
        greenRInput.setText(srgb[1] + "");
        blueRInput.setText(srgb[2] + "");
        opacityRInput.setText(DoubleTools.scale6(color.getOpacity()) + "");

        argbInput.setText(rgb2AlphaHex(color));
        rgbInput.setText(rgb2Hex(color));

        hueInput.setText(Math.round(color.getHue()) + "");
        saturationInput.setText(Math.round(color.getSaturation() * 100) + "");
        brightnessInput.setText(Math.round(color.getBrightness() * 100) + "");

        hueRInput.setText(DoubleTools.scale6(color.getHue() / 360.0) + "");
        saturationRInput.setText(DoubleTools.scale6(color.getSaturation()) + "");
        brightnessRInput.setText(DoubleTools.scale6(color.getBrightness()) + "");

        float[] xyz = ColorConversion.SRGBtoXYZ(srgb);
        xInput.setText(DoubleTools.scale6(xyz[0]) + "");
        yInput.setText(DoubleTools.scale6(xyz[1]) + "");
        zInput.setText(DoubleTools.scale6(xyz[2]) + "");
//
//        double[] appleRgb = FxmlColor.rgb2AppleRgb(srgb);
//        appleRedInput.setText(ValueTools.roundDouble6(appleRgb[0]) + "");
//        appleGreenInput.setText(ValueTools.roundDouble6(appleRgb[1]) + "");
//        appleBlueInput.setText(ValueTools.roundDouble6(appleRgb[2]) + "");

//        float[] adobeRgb = ImageColorSpace.SRGBtoAbodeRGB(srgb);
//        adobeRedInput.setText(adobeRgb[0] + "");
//        adobeGreenInput.setText(adobeRgb[1] + "");
//        adobeBlueInput.setText(adobeRgb[2] + "");
        float[] pyccRgb = ColorConversion.fromSRGB(ColorSpace.CS_PYCC, srgb);
        pyccRedInput.setText(pyccRgb[0] + "");
        pyccGreenInput.setText(pyccRgb[1] + "");
        pyccBlueInput.setText(pyccRgb[2] + "");

        float[] cmykRgb = ColorConversion.fromSRGB(ColorSpace.TYPE_CMYK, srgb);
        cyanInput.setText(cmykRgb[0] + "");
        magentaInput.setText(cmykRgb[1] + "");
        yellowInput.setText(cmykRgb[2] + "");
        blackInput.setText(cmykRgb[3] + "");

        isSettingValues = false;
    }

    // https://www.w3.org/Graphics/Color/sRGB.html
    // http://brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html
    @FXML
    private void close(ActionEvent event) {
        closeStage();
    }

}
