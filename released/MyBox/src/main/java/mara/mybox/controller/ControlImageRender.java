package mara.mybox.controller;

import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.imageRenderHints;
import mara.mybox.value.ImageRenderHints;

/**
 * @Author Mara
 * @CreateDate 2022-1-17
 * @License Apache License Version 2.0
 */
public class ControlImageRender extends BaseController {

    @FXML
    protected ToggleGroup renderGroup, colorRenderGroup, pixelsInterGroup, alphaInterGroup, shapeAntiGroup,
            textAntiGroup, fontFmGroup, strokeGroup, ditherGroup;
    @FXML
    protected CheckBox applyCheck;
    @FXML
    protected VBox optionsBox;
    @FXML
    protected RadioButton renderDefaultRadio, renderQualityRadio, renderSpeedRadio,
            colorRenderDefaultRadio, colorRenderQualityRadio, colorRenderSpeedRadio,
            pInter9Radio, pInter4Radio, pInter1Radio,
            aInterDefaultRadio, aInterQualityRadio, aInterSpeedRadio,
            antiDefaultRadio, antiQualityRadio, antiSpeedRadio,
            tantiDefaultRadio, tantiOnRadio, tantiOffRadio, tantiGaspRadio, tantiLcdHrgbRadio,
            tantiLcdHbgrRadio, tantiLcdVrgbOnRadio, tantiLcdVbgrRadio,
            fmDefaultRadio, fmOnRadio, fmOffRadio,
            strokeDefaultRadio, strokeNormalizeRadio, strokePureRadio,
            ditherDefaultRadio, ditherOnRadio, ditherOffRadio;

    @Override
    public void initControls() {
        try {
            super.initControls();

            applyCheck.setSelected(ImageRenderHints.applyHints());
            applyCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    ImageRenderHints.applyHints(applyCheck.isSelected());
                    checkApply();
                }
            });

            optionsBox.disableProperty().bind(applyCheck.selectedProperty().not());

            applyHints();

            renderGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    writeHints();
                }
            });

            colorRenderGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    writeHints();
                }
            });

            pixelsInterGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    writeHints();
                }
            });

            alphaInterGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    writeHints();
                }
            });

            shapeAntiGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    writeHints();
                }
            });

            textAntiGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    writeHints();
                }
            });

            fontFmGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    writeHints();
                }
            });

            strokeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    writeHints();
                }
            });

            ditherGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    writeHints();
                }
            });

            checkApply();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkApply() {
        if (isSettingValues) {
            return;
        }
        if (ImageRenderHints.applyHints()) {
            writeHints();
        } else {
            imageRenderHints = null;
        }
    }

    public synchronized void applyHints() {
        try {
            if (imageRenderHints == null) {
                return;
            }
            isSettingValues = true;

            applyCheck.setSelected(ImageRenderHints.applyHints());

            Object render = imageRenderHints.get(RenderingHints.KEY_RENDERING);
            if (RenderingHints.VALUE_RENDER_QUALITY.equals(render)) {
                renderQualityRadio.fire();
            } else if (RenderingHints.VALUE_RENDER_SPEED.equals(render)) {
                renderSpeedRadio.fire();
            } else {
                renderDefaultRadio.fire();
            }

            Object crender = imageRenderHints.get(RenderingHints.KEY_COLOR_RENDERING);
            if (RenderingHints.VALUE_COLOR_RENDER_QUALITY.equals(crender)) {
                colorRenderQualityRadio.fire();
            } else if (RenderingHints.VALUE_COLOR_RENDER_SPEED.equals(crender)) {
                colorRenderSpeedRadio.fire();
            } else {
                colorRenderDefaultRadio.fire();
            }

            Object pinter = imageRenderHints.get(RenderingHints.KEY_INTERPOLATION);
            if (RenderingHints.VALUE_INTERPOLATION_BILINEAR.equals(pinter)) {
                pInter4Radio.fire();
            } else if (RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR.equals(pinter)) {
                pInter1Radio.fire();
            } else {
                pInter9Radio.fire();
            }

            Object ainter = imageRenderHints.get(RenderingHints.KEY_ALPHA_INTERPOLATION);
            if (RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY.equals(ainter)) {
                aInterQualityRadio.fire();
            } else if (RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED.equals(ainter)) {
                aInterSpeedRadio.fire();
            } else {
                aInterDefaultRadio.fire();
            }

            Object anti = imageRenderHints.get(RenderingHints.KEY_ANTIALIASING);
            if (RenderingHints.VALUE_ANTIALIAS_ON.equals(anti)) {
                antiQualityRadio.fire();
            } else if (RenderingHints.VALUE_ANTIALIAS_OFF.equals(anti)) {
                antiSpeedRadio.fire();
            } else {
                antiDefaultRadio.fire();
            }

            Object tanti = imageRenderHints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
            if (RenderingHints.VALUE_TEXT_ANTIALIAS_ON.equals(tanti)) {
                tantiOnRadio.fire();
            } else if (RenderingHints.VALUE_TEXT_ANTIALIAS_OFF.equals(tanti)) {
                tantiOffRadio.fire();
            } else if (RenderingHints.VALUE_TEXT_ANTIALIAS_GASP.equals(tanti)) {
                tantiGaspRadio.fire();
            } else if (RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB.equals(tanti)) {
                tantiLcdHrgbRadio.fire();
            } else if (RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR.equals(tanti)) {
                tantiLcdHbgrRadio.fire();
            } else if (RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB.equals(tanti)) {
                tantiLcdVrgbOnRadio.fire();
            } else if (RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR.equals(tanti)) {
                tantiLcdVbgrRadio.fire();
            } else {
                tantiDefaultRadio.fire();
            }

            Object fontfm = imageRenderHints.get(RenderingHints.KEY_FRACTIONALMETRICS);
            if (RenderingHints.VALUE_FRACTIONALMETRICS_ON.equals(fontfm)) {
                fmOnRadio.fire();
            } else if (RenderingHints.VALUE_FRACTIONALMETRICS_OFF.equals(fontfm)) {
                fmOffRadio.fire();
            } else {
                fmDefaultRadio.fire();
            }

            Object stroke = imageRenderHints.get(RenderingHints.KEY_STROKE_CONTROL);
            if (RenderingHints.VALUE_STROKE_NORMALIZE.equals(stroke)) {
                strokeNormalizeRadio.fire();
            } else if (RenderingHints.VALUE_STROKE_PURE.equals(stroke)) {
                strokePureRadio.fire();
            } else {
                strokeDefaultRadio.fire();
            }

            Object dither = imageRenderHints.get(RenderingHints.KEY_DITHERING);
            if (RenderingHints.VALUE_DITHER_ENABLE.equals(dither)) {
                ditherOnRadio.fire();
            } else if (RenderingHints.VALUE_DITHER_DISABLE.equals(dither)) {
                ditherOffRadio.fire();
            } else {
                ditherDefaultRadio.fire();
            }

            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public Map<RenderingHints.Key, Object> writeHints() {
        try {
            if (isSettingValues) {
                return imageRenderHints;
            }
            if (!ImageRenderHints.applyHints()) {
                imageRenderHints = null;
                return null;
            }
            imageRenderHints = new HashMap<>();

            if (renderQualityRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            } else if (renderSpeedRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            } else {
                imageRenderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
            }

            if (colorRenderQualityRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            } else if (colorRenderSpeedRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            } else {
                imageRenderHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_DEFAULT);
            }

            if (pInter4Radio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            } else if (pInter1Radio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            } else {
                imageRenderHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            }

            if (aInterQualityRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            } else if (aInterSpeedRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            } else {
                imageRenderHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
            }

            if (antiQualityRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            } else if (antiSpeedRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            } else {
                imageRenderHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
            }

            if (tantiOnRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            } else if (tantiOffRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            } else if (tantiGaspRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
            } else if (tantiLcdHrgbRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            } else if (tantiLcdHbgrRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR);
            } else if (tantiLcdVrgbOnRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB);
            } else if (tantiLcdVbgrRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR);
            } else {
                imageRenderHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
            }

            if (fmOnRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            } else if (fmOffRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            } else {
                imageRenderHints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
            }

            if (strokeNormalizeRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
            } else if (strokePureRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            } else {
                imageRenderHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
            }

            if (ditherOnRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            } else if (ditherOffRadio.isSelected()) {
                imageRenderHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
            } else {
                imageRenderHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DEFAULT);
            }

            ImageRenderHints.saveImageRenderHints();

            updateOptions(parentController);

            return imageRenderHints;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    public void about() {
        openLink("https://docs.oracle.com/en/java/javase/17/docs/api/java.desktop/java/awt/RenderingHints.html");
    }

    /*
        static methods
     */
    public static void updateOptions(BaseController parent) {
        Platform.runLater(() -> {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                if (!(window instanceof Stage)) {
                    continue;
                }
                Stage stage = (Stage) window;
                Object controller = stage.getUserData();
                if (controller == null) {
                    continue;
                }
                if (controller instanceof BaseImageController) {
                    try {
                        BaseImageController imageController = (BaseImageController) controller;
                        if (!imageController.equals(parent) && imageController.renderController != null) {
                            imageController.renderController.applyHints();
                        }
                    } catch (Exception e) {
                    }
                } else if (controller instanceof SettingsController) {
                    try {
                        SettingsController settingsController = (SettingsController) controller;
                        if (!settingsController.equals(parent)) {
                            settingsController.renderController.applyHints();
                        }
                    } catch (Exception e) {
                    }
                }
            }
        });
    }

}
