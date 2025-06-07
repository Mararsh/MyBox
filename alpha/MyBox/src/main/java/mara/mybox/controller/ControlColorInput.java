package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.image.FxColorTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-29
 * @License Apache License Version 2.0
 */
public class ControlColorInput extends BaseController {

    protected ColorData colorData;
    protected final SimpleBooleanProperty updateNotify = new SimpleBooleanProperty(false);

    @FXML
    protected ControlColorSet colorController;
    @FXML
    protected TextField colorInput;
    @FXML
    protected ColorPicker colorPicker;
    @FXML
    protected Slider hueSlider, saturationSlider, brightnessSlider, opacitySlider;
    @FXML
    protected HtmlTableController htmlController;

    @Override
    public void initControls() {
        try {
            colorController.init(this, baseName);
            colorController.setNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    if (isSettingValues) {
                        return;
                    }
                    colorInput.setText(colorController.rgba());
                    goAction();
                }
            });

            colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> v, Color ov, Color nv) {
                    if (isSettingValues || nv == null) {
                        return;
                    }
                    colorInput.setText(FxColorTools.color2rgba(nv));
                    goAction();
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

            goButton.disableProperty().bind(colorInput.textProperty().isEmpty());

            colorInput.setText("#552288");
            goAction();

        } catch (Exception e) {
            MyBoxLog.error(e);
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
        goAction();
    }

    public boolean pickValue() {
        try {
            String value = colorInput.getText();
            if (value == null || value.isBlank()) {
                return false;
            }
            TableStringValues.add("ColorHistories", value);
            ColorData c = new ColorData(value).convert();
            if (c.isValid()) {
                colorData = c;
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @FXML
    @Override
    public void goAction() {
        try {
            if (!pickValue()) {
                popError(message("InvalidParameters") + ": " + message("Color"));
            }

            isSettingValues = true;
            Color color = colorData.getColor();
            colorController.setColor(color);
            colorPicker.setValue(color);
            hueSlider.setValue((int) color.getHue());
            saturationSlider.setValue((int) (color.getSaturation() * 100));
            brightnessSlider.setValue((int) (color.getBrightness() * 100));
            opacitySlider.setValue((int) (color.getOpacity() * 100));
            isSettingValues = false;

            updateNotify.set(!updateNotify.get());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
        PopTools.popSavedValues(this, colorInput, event, "ColorHistories");
    }

    @FXML
    protected void popColorHistories(Event event) {
        if (UserConfig.getBoolean("ColorHistoriesPopWhenMouseHovering", false)) {
            showHistories(event);
        }
    }

    @Override
    public boolean keyEnter() {
        if (goButton.isDisable()) {
            return false;
        }
        goAction();
        return true;
    }

    /*
        static
     */
    public static ControlColorInput open() {
        try {
            ControlColorInput controller = (ControlColorInput) WindowTools.openStage(Fxmls.ColorQueryFxml);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
