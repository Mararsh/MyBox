package mara.mybox.controller;

import mara.mybox.controller.base.BaseController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import static mara.mybox.fxml.FxmlColor.rgb2AlphaHex;
import static mara.mybox.fxml.FxmlColor.rgb2Hex;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.message;
import static mara.mybox.value.AppVaribles.message;

/**
 * @Author Mara
 * @CreateDate 2018-07-24
 * @Description
 * @License Apache License Version 2.0
 */
public class ColorPaletteController extends BaseController {

    @FXML
    private HBox paletteBox;
    @FXML
    private Button closeButton;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private TextField rgbValue, hsbValue, hexValue;

    public ColorPaletteController() {
        baseTitle = AppVaribles.message("ColorPalette");
    }

    @Override
    public void initializeNext() {
        try {
            colorPicker.setValue(Color.WHITE);
            colorAction();

        } catch (Exception e) {

        }
    }

    @FXML
    private void colorAction() {
        Color color = colorPicker.getValue();
        rgbValue.setText(message("Opacity") + ": " + Math.round(color.getOpacity() * 100) + "%    "
                + message("Red") + ": " + Math.round(color.getRed() * 255) + "    "
                + message("Green") + ": " + Math.round(color.getGreen() * 255) + "    "
                + message("Blue") + ": " + Math.round(color.getBlue() * 255));
        hsbValue.setText(message("Hue") + ": " + Math.round(color.getHue()) + "    "
                + message("Saturation") + ": " + Math.round(color.getSaturation() * 100) + "%    "
                + message("Brightness") + ": " + Math.round(color.getBrightness() * 100) + "%");
        hexValue.setText("ARGB: " + rgb2AlphaHex(color) + "    RGB: " + rgb2Hex(color));
    }

    @FXML
    private void close(ActionEvent event) {
        closeStage();
    }

}
