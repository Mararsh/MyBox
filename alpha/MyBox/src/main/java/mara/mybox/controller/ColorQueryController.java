package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.image.FxColorTools;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-29
 * @License Apache License Version 2.0
 */
public class ColorQueryController extends BaseController {

    protected ColorData colorData;

    @FXML
    protected TextField colorInput;
    @FXML
    protected ColorPicker colorPicker;
    @FXML
    protected Button queryButton, refreshButton, paletteButton;
    @FXML
    protected Slider hueSlider, saturationSlider, brightnessSlider, opacitySlider;
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
            colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> v, Color ov, Color nv) {
                    if (isSettingValues || nv == null) {
                        return;
                    }
                    colorInput.setText(FxColorTools.color2rgba(nv));
                    queryAction();
                }
            });

            hueSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> v, Number ov, Number nv) {
                    pickSliders();
                }
            });

            saturationSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> v, Number ov, Number nv) {
                    pickSliders();
                }
            });

            brightnessSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> v, Number ov, Number nv) {
                    pickSliders();
                }
            });

            opacitySlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> v, Number ov, Number nv) {
                    pickSliders();
                }
            });
            separatorInput.setText(UserConfig.getString(baseName + "Separator", ", "));

            queryButton.disableProperty().bind(colorInput.textProperty().isEmpty());
            refreshButton.disableProperty().bind(colorInput.textProperty().isEmpty()
                    .or(separatorInput.textProperty().isEmpty())
            );
            paletteButton.disableProperty().bind(queryButton.disableProperty());

            colorInput.setText("#552288");
            queryAction();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.setTooltip(queryButton, message("Query") + "\nF1 / ENTER");
            NodeStyleTools.setTooltip(paletteButton, message("AddInColorPalette"));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public ColorData pickValue() {
        try {
            String value = colorInput.getText();
            if (value == null || value.isBlank()) {
                return null;
            }
            TableStringValues.add("ColorQueryColorHistories", value);
            String separator = separatorInput.getText();
            if (separator == null || separator.isEmpty()) {
                separator = ", ";
            }
            UserConfig.setString(baseName + "Separator", separator);
            ColorData c = new ColorData(value).setvSeparator(separator).convert();
            return c;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    public void queryAction() {
        try {
            if (!showValue()) {
                popError(message("InvalidParameters") + ": " + message("Color"));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean showValue() {
        try {
            ColorData c = pickValue();
            if (c == null || c.getSrgb() == null) {
                return false;
            }
            colorData = c;
            htmlController.displayHtml(colorData.html());

            isSettingValues = true;
            Color color = colorData.getColor();
            colorPicker.setValue(color);
            hueSlider.setValue((int) color.getHue());
            saturationSlider.setValue((int) (color.getSaturation() * 100));
            brightnessSlider.setValue((int) (color.getBrightness() * 100));
            opacitySlider.setValue((int) (color.getOpacity() * 100));
            isSettingValues = false;

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void pickSliders() {
        if (isSettingValues) {
            return;
        }
        int h = (int) hueSlider.getValue();
        int s = (int) saturationSlider.getValue();
        int b = (int) brightnessSlider.getValue();
        double a = DoubleTools.scale2(opacitySlider.getValue() / 100);
        colorInput.setText("hsla(" + h + "," + s + "%," + b + "%," + a + ")");
        showValue();
    }

    @FXML
    protected void showExamples(Event event) {
        PopTools.popColorExamples(this, colorInput, event);
    }

    @FXML
    public void popExamples(Event event) {
        if (UserConfig.getBoolean("ColorExamplesPopWhenMouseHovering", false)) {
            showExamples(event);
        }
    }

    @FXML
    protected void showHistories(Event event) {
        PopTools.popSavedValues(this, colorInput, event, "ColorQueryColorHistories");
    }

    @FXML
    protected void popColorHistories(Event event) {
        if (UserConfig.getBoolean("ColorQueryColorHistoriesPopWhenMouseHovering", false)) {
            showHistories(event);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        queryAction();
    }

    @FXML
    public void addColor() {
        if (colorData == null) {
            return;
        }
        ColorsManageController.addOneColor(colorData.getColor());
    }

    @FXML
    protected void popHelps(Event event) {
        if (UserConfig.getBoolean("ColorHelpsPopWhenMouseHovering", false)) {
            showHelps(event);
        }
    }

    @FXML
    protected void showHelps(Event event) {
        popEventMenu(event, HelpTools.colorHelps(true));
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

    /*
        static
     */
    public static ColorQueryController open() {
        try {
            ColorQueryController controller = (ColorQueryController) WindowTools.openStage(Fxmls.ColorQueryFxml);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
