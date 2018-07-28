package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.tools.FxmlTools;

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
    private Button useButton;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private TextField rgbValue, hsbValue, hexValue;

    @Override
    protected void initializeNext() {
        try {
            colorPicker.setValue(Color.WHITE);
            colorAction();

        } catch (Exception e) {

        }
    }

    @FXML
    private void colorAction() {
        Color color = colorPicker.getValue();
        rgbValue.setText(getMessage("Opacity") + ": " + Math.round(color.getOpacity() * 100) + "%    "
                + getMessage("Red") + ": " + Math.round(color.getRed() * 255) + "    "
                + getMessage("Green") + ": " + Math.round(color.getGreen() * 255) + "    "
                + getMessage("Blue") + ": " + Math.round(color.getBlue() * 255));
        hsbValue.setText(getMessage("Hue") + ": " + Math.round(color.getHue()) + "    "
                + getMessage("Saturation") + ": " + Math.round(color.getSaturation() * 100) + "%    "
                + getMessage("Brightness") + ": " + Math.round(color.getBrightness() * 100) + "%");
        hexValue.setText("ARGB: " + FxmlTools.rgb2AlphaHex(color) + "    RGB: " + FxmlTools.rgb2Hex(color));
    }

    @FXML
    private void close(ActionEvent event) {
        getMyStage().close();
    }

}
