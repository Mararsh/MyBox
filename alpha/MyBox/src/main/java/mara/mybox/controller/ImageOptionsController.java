package mara.mybox.controller;

import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.imageRenderHints;
import mara.mybox.value.Fxmls;
import mara.mybox.value.ImageRenderHints;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-7-19
 * @License Apache License Version 2.0
 */
public class ImageOptionsController extends BaseController {

    protected BaseImageController imageController;

    @FXML
    protected ControlColorSet alphaColorSetController, rulerColorController, gridColorController;
    @FXML
    protected ComboBox<String> zoomStepSelector,
            decimalSelector,
            gridWidthSelector, gridIntervalSelector, gridOpacitySelector;
    @FXML
    protected ToggleGroup renderGroup, colorRenderGroup, pixelsInterGroup, alphaInterGroup, shapeAntiGroup,
            textAntiGroup, fontFmGroup, strokeGroup, ditherGroup;
    @FXML
    protected TextField thumbnailWidthInput;
    @FXML
    protected CheckBox renderCheck;
    @FXML
    protected VBox renderBox;
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
    @FXML
    protected Label alphaLabel;

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(zoomStepSelector, new Tooltip(message("ZoomStep")));

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setParameters(BaseImageController parent) {
        try {
            if (parent == null) {
                close();
                return;
            }
            imageController = parent;
            baseName = imageController.baseName;

            initViewOptions();
            initRenderOptions();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initViewOptions() {
        try {
            imageController.zoomStep = UserConfig.getInt(baseName + "ZoomStep", 40);
            imageController.zoomStep = imageController.zoomStep <= 0 ? 40 : imageController.zoomStep;
            imageController.xZoomStep = imageController.zoomStep;
            imageController.yZoomStep = imageController.zoomStep;
            zoomStepSelector.getItems().addAll(
                    Arrays.asList("40", "20", "5", "1", "3", "15", "30", "50", "80", "100", "150", "200", "300", "500")
            );
            zoomStepSelector.setValue(imageController.zoomStep + "");
            zoomStepSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                    try {
                        int v = Integer.parseInt(newVal);
                        if (v > 0) {
                            imageController.zoomStep = v;
                            UserConfig.setInt(baseName + "ZoomStep", imageController.zoomStep);
                            zoomStepSelector.getEditor().setStyle(null);
                            imageController.zoomStepChanged();
                        } else {
                            zoomStepSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        zoomStepSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            rulerColorController.init(this, "RulerColor", Color.RED);
            rulerColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> v, Paint ov, Paint nv) {
                    if (isSettingValues) {
                        return;
                    }
                    BaseImageController.updateMaskRulerXY();
                }
            });

            gridColorController.init(this, "GridLinesColor", Color.LIGHTGRAY);
            gridColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> v, Paint ov, Paint nv) {
                    if (isSettingValues) {
                        return;
                    }
                    BaseImageController.updateMaskGrid();
                }
            });

            gridWidthSelector.getItems().addAll(Arrays.asList("2", "1", "3", "4", "5", "6", "7", "8", "9", "10"));
            gridWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues || newValue == null || newValue.isEmpty()) {
                        return;
                    }
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            UserConfig.setInt("GridLinesWidth", v);
                            ValidationTools.setEditorNormal(gridWidthSelector);
                            BaseImageController.updateMaskGrid();
                        } else {
                            ValidationTools.setEditorBadStyle(gridWidthSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(gridWidthSelector);
                    }
                }
            });

            gridIntervalSelector.getItems().addAll(Arrays.asList(message("Automatic"), "10", "20", "25", "50", "100", "5", "1", "2", "200", "500"));
            gridIntervalSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues || newValue == null || newValue.isEmpty()) {
                        return;
                    }
                    int v = -1;
                    try {
                        if (!message("Automatic").equals(newValue)) {
                            v = Integer.parseInt(newValue);
                        }
                    } catch (Exception e) {
                    }
                    UserConfig.setInt("GridLinesInterval", v);
                    BaseImageController.updateMaskGrid();
                }
            });

            gridOpacitySelector.getItems().addAll(Arrays.asList("0.5", "0.2", "1.0", "0.7", "0.1", "0.3", "0.8", "0.9", "0.6", "0.4"));
            gridOpacitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues || newValue == null || newValue.isEmpty()) {
                        return;
                    }
                    float v = 0.1f;
                    try {
                        v = Float.parseFloat(newValue);
                    } catch (Exception e) {
                    }
                    UserConfig.setFloat("GridLinesOpacity", v);
                    BaseImageController.updateMaskGrid();
                }
            });

            decimalSelector.getItems().addAll(Arrays.asList("2", "1", "3", "0", "4", "5", "6", "7", "8"));
            decimalSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues || newValue == null || newValue.isEmpty()) {
                        return;
                    }
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            UserConfig.setInt("ImageDecimal", v);
                            ValidationTools.setEditorNormal(decimalSelector);
                        } else {
                            ValidationTools.setEditorBadStyle(decimalSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(decimalSelector);
                    }
                }
            });

            alphaColorSetController.init(this, "AlphaAsColor", Color.WHITE);
            alphaColorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable,
                        Paint oldValue, Paint newValue) {
                    if (!Color.WHITE.equals((Color) newValue)) {
                        alphaLabel.setText(message("AlphaReplaceComments"));
                        alphaLabel.setStyle(NodeStyleTools.darkRedTextStyle());
                    } else {
                        alphaLabel.setText("");
                    }
                }
            });

            thumbnailWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(thumbnailWidthInput.getText());
                        if (v > 0) {
                            UserConfig.setInt("ThumbnailWidth", v);
                            AppVariables.thumbnailWidth = v;
                            thumbnailWidthInput.setStyle(null);
                        } else {
                            thumbnailWidthInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        thumbnailWidthInput.setStyle(UserConfig.badStyle());
                    }
                }
            });

            isSettingValues = true;

            gridWidthSelector.setValue(UserConfig.getInt("GridLinesWidth", 1) + "");
            int gi = UserConfig.getInt("GridLinesInterval", -1);
            gridIntervalSelector.setValue(gi <= 0 ? message("Automatic") : gi + "");
            gridOpacitySelector.setValue(UserConfig.getFloat("GridLinesOpacity", 0.1f) + "");
            decimalSelector.setValue(UserConfig.imageScale() + "");

            gridColorController.asSaved();

            thumbnailWidthInput.setText(AppVariables.thumbnailWidth + "");

            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initRenderOptions() {
        try {

            renderCheck.setSelected(ImageRenderHints.applyHints());
            renderCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    ImageRenderHints.applyHints(renderCheck.isSelected());
                    checkHints();
                }
            });

            renderBox.disableProperty().bind(renderCheck.selectedProperty().not());

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

            checkHints();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void checkHints() {
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

            renderCheck.setSelected(ImageRenderHints.applyHints());

            Object render = imageRenderHints.get(RenderingHints.KEY_RENDERING);
            if (RenderingHints.VALUE_RENDER_QUALITY.equals(render)) {
                renderQualityRadio.setSelected(true);
            } else if (RenderingHints.VALUE_RENDER_SPEED.equals(render)) {
                renderSpeedRadio.setSelected(true);
            } else {
                renderDefaultRadio.setSelected(true);
            }

            Object crender = imageRenderHints.get(RenderingHints.KEY_COLOR_RENDERING);
            if (RenderingHints.VALUE_COLOR_RENDER_QUALITY.equals(crender)) {
                colorRenderQualityRadio.setSelected(true);
            } else if (RenderingHints.VALUE_COLOR_RENDER_SPEED.equals(crender)) {
                colorRenderSpeedRadio.setSelected(true);
            } else {
                colorRenderDefaultRadio.setSelected(true);
            }

            Object pinter = imageRenderHints.get(RenderingHints.KEY_INTERPOLATION);
            if (RenderingHints.VALUE_INTERPOLATION_BILINEAR.equals(pinter)) {
                pInter4Radio.setSelected(true);
            } else if (RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR.equals(pinter)) {
                pInter1Radio.setSelected(true);
            } else {
                pInter9Radio.setSelected(true);
            }

            Object ainter = imageRenderHints.get(RenderingHints.KEY_ALPHA_INTERPOLATION);
            if (RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY.equals(ainter)) {
                aInterQualityRadio.setSelected(true);
            } else if (RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED.equals(ainter)) {
                aInterSpeedRadio.setSelected(true);
            } else {
                aInterDefaultRadio.setSelected(true);
            }

            Object anti = imageRenderHints.get(RenderingHints.KEY_ANTIALIASING);
            if (RenderingHints.VALUE_ANTIALIAS_ON.equals(anti)) {
                antiQualityRadio.setSelected(true);
            } else if (RenderingHints.VALUE_ANTIALIAS_OFF.equals(anti)) {
                antiSpeedRadio.setSelected(true);
            } else {
                antiDefaultRadio.setSelected(true);
            }

            Object tanti = imageRenderHints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
            if (RenderingHints.VALUE_TEXT_ANTIALIAS_ON.equals(tanti)) {
                tantiOnRadio.setSelected(true);
            } else if (RenderingHints.VALUE_TEXT_ANTIALIAS_OFF.equals(tanti)) {
                tantiOffRadio.setSelected(true);
            } else if (RenderingHints.VALUE_TEXT_ANTIALIAS_GASP.equals(tanti)) {
                tantiGaspRadio.setSelected(true);
            } else if (RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB.equals(tanti)) {
                tantiLcdHrgbRadio.setSelected(true);
            } else if (RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR.equals(tanti)) {
                tantiLcdHbgrRadio.setSelected(true);
            } else if (RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB.equals(tanti)) {
                tantiLcdVrgbOnRadio.setSelected(true);
            } else if (RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR.equals(tanti)) {
                tantiLcdVbgrRadio.setSelected(true);
            } else {
                tantiDefaultRadio.setSelected(true);
            }

            Object fontfm = imageRenderHints.get(RenderingHints.KEY_FRACTIONALMETRICS);
            if (RenderingHints.VALUE_FRACTIONALMETRICS_ON.equals(fontfm)) {
                fmOnRadio.setSelected(true);
            } else if (RenderingHints.VALUE_FRACTIONALMETRICS_OFF.equals(fontfm)) {
                fmOffRadio.setSelected(true);
            } else {
                fmDefaultRadio.setSelected(true);
            }

            Object stroke = imageRenderHints.get(RenderingHints.KEY_STROKE_CONTROL);
            if (RenderingHints.VALUE_STROKE_NORMALIZE.equals(stroke)) {
                strokeNormalizeRadio.setSelected(true);
            } else if (RenderingHints.VALUE_STROKE_PURE.equals(stroke)) {
                strokePureRadio.setSelected(true);
            } else {
                strokeDefaultRadio.setSelected(true);
            }

            Object dither = imageRenderHints.get(RenderingHints.KEY_DITHERING);
            if (RenderingHints.VALUE_DITHER_ENABLE.equals(dither)) {
                ditherOnRadio.setSelected(true);
            } else if (RenderingHints.VALUE_DITHER_DISABLE.equals(dither)) {
                ditherOffRadio.setSelected(true);
            } else {
                ditherDefaultRadio.setSelected(true);
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

            return imageRenderHints;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    public void aboutRenderHints() {
        openLink(HelpTools.renderingHintsLink());
    }

    /*
        static methods
     */
    public static ImageOptionsController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageOptionsController controller = (ImageOptionsController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageOptionsFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
