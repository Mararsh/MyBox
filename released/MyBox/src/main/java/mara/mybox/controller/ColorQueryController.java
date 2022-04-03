package mara.mybox.controller;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-29
 * @License Apache License Version 2.0
 */
public class ColorQueryController extends BaseController {

    protected ColorData color;

    @FXML
    protected TextField colorInput;
    @FXML
    protected ColorPicker colorPicker;
    @FXML
    protected Button queryButton, refreshButton, paletteButton;
    @FXML
    protected TextField separatorInput;
    @FXML
    protected HtmlTableController htmlController;

    public ColorQueryController() {
        baseTitle = message("ColorQuery");
    }

    @Override
    public void initControls() {
        try {
            colorInput.setText("#552288");

            colorPicker.valueProperty().addListener((ObservableValue<? extends Color> ov, Color oldVal, Color newVal) -> {
                if (isSettingValues || newVal == null) {
                    return;
                }
                colorInput.setText(FxColorTools.color2rgba(newVal));
                queryAction();
            });

            separatorInput.setText(UserConfig.getString(baseName + "Separator", ", "));

            queryButton.disableProperty().bind(colorInput.textProperty().isEmpty());
            refreshButton.disableProperty().bind(colorInput.textProperty().isEmpty()
                    .or(separatorInput.textProperty().isEmpty())
            );
            paletteButton.disableProperty().bind(queryButton.disableProperty());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.setTooltip(queryButton, message("Query") + "\nF1 / ENTER");
            NodeStyleTools.setTooltip(paletteButton, message("AddInColorPalette"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void queryAction() {
        try {
            String value = colorInput.getText();
            if (value == null || value.isBlank()) {
                popError(message("InvalidParameters") + ": " + message("Color"));
                return;
            }
            ColorData c = new ColorData(value).calculate();
            if (c.getSrgb() == null) {
                popError(message("InvalidParameters") + ": " + message("Color"));
                return;
            }
            TableStringValues.add("ColorQueryColorHistories", value);
            color = c;
            String separator = separatorInput.getText();
            if (separator == null || separator.isEmpty()) {
                separator = ", ";
            }
            UserConfig.setString(baseName + "Separator", separator);
            htmlController.initTable(message("Color"));
            htmlController.addData(message("Color"),
                    "<DIV style=\"width: 50px;  background-color:" + color.getRgb() + "; \">&nbsp;&nbsp;&nbsp;</DIV>");
            htmlController.addData(message("Value"), color.getColorValue() + "");
            htmlController.addData("RGBA", color.getRgba().replaceAll(" ", separator));
            htmlController.addData("RGB", color.getRgb().replaceAll(" ", separator));
            htmlController.addData("sRGB", color.getSrgb().replaceAll(" ", separator));
            htmlController.addData("HSB", color.getHsb().replaceAll(" ", separator));
            htmlController.addData("Adobe RGB", color.getAdobeRGB().replaceAll(" ", separator));
            htmlController.addData("Apple RGB", color.getAppleRGB().replaceAll(" ", separator));
            htmlController.addData("ECI RGB", color.getEciRGB().replaceAll(" ", separator));
            htmlController.addData("sRGB Linear", color.getSRGBLinear().replaceAll(" ", separator));
            htmlController.addData("Adobe RGB Linear", color.getAdobeRGBLinear().replaceAll(" ", separator));
            htmlController.addData("Apple RGB Linear", color.getAppleRGBLinear().replaceAll(" ", separator));
            htmlController.addData("Calculated CMYK", color.getCalculatedCMYK().replaceAll(" ", separator));
            htmlController.addData("ECI CMYK", color.getEciCMYK().replaceAll(" ", separator));
            htmlController.addData("Adobe CMYK Uncoated FOGRA29", color.getAdobeCMYK().replaceAll(" ", separator));
            htmlController.addData("XYZ", color.getXyz().replaceAll(" ", separator));
            htmlController.addData("CIE-L*ab", color.getCieLab().replaceAll(" ", separator));
            htmlController.addData("LCH(ab)", color.getLchab().replaceAll(" ", separator));
            htmlController.addData("CIE-L*uv", color.getCieLuv().replaceAll(" ", separator));
            htmlController.addData("LCH(uv)", color.getLchuv().replaceAll(" ", separator));
            htmlController.displayHtml();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popExamples(MouseEvent mouseEvent) {
        PopTools.popColorExamples(this, colorInput, mouseEvent);
    }

    @FXML
    protected void popColorHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, colorInput, mouseEvent, "ColorQueryColorHistories", true);
    }

    @FXML
    public void refreshAction() {
        queryAction();
    }

    @FXML
    public void addColor() {
        if (color == null) {
            return;
        }
        ColorsManageController.addColor(color.getColor());
    }

    @Override
    public boolean keyEnter() {
        return keyF1();
    }

    @Override
    public boolean keyF1() {
        if (queryButton.isDisable()) {
            return false;
        }
        queryAction();
        return true;
    }

}
