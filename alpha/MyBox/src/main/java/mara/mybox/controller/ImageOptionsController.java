package mara.mybox.controller;

import java.awt.RenderingHints;
import java.sql.Connection;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.ImageHints;
import mara.mybox.value.Fxmls;
import mara.mybox.value.ImageRenderHints;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-7-19
 * @License Apache License Version 2.0
 */
public class ImageOptionsController extends BaseChildController {

    protected BaseImageController imageController;

    @FXML
    protected FlowPane stepPane;
    @FXML
    protected ControlColorSet alphaColorSetController, rulerColorController, gridColorController;
    @FXML
    protected ComboBox<String> zoomStepSelector, decimalSelector,
            gridWidthSelector, gridIntervalSelector, gridOpacitySelector;
    @FXML
    protected ToggleGroup renderGroup, colorRenderGroup, pixelsInterGroup, alphaInterGroup, shapeAntiGroup,
            textAntiGroup, fontFmGroup, strokeGroup, ditherGroup;
    @FXML
    protected TextField thumbnailWidthInput, maxDemoInput;
    @FXML
    protected CheckBox renderCheck;
    @FXML
    protected VBox viewBox, renderBox;
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

    public ImageOptionsController() {
        baseTitle = message("ImageOptions");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            baseName = "ImageOptions";

            initViewOptions();
            initRenderOptions();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseImageController parent) {
        try {
            if (parent == null) {
                close();
                return;
            }
            imageController = parent;

            if (!viewBox.getChildren().contains(stepPane)) {
                viewBox.getChildren().add(0, stepPane);
            }
            imageController.zoomStep = UserConfig.getInt(imageController.baseName + "ZoomStep", 40);
            imageController.zoomStep = imageController.zoomStep <= 0 ? 40 : imageController.zoomStep;
            imageController.xZoomStep = imageController.zoomStep;
            imageController.yZoomStep = imageController.zoomStep;
            zoomStepSelector.setValue(imageController.zoomStep + "");
            zoomStepSelector.getItems().addAll(
                    Arrays.asList("40", "20", "5", "1", "3", "15", "30", "50", "80", "100", "150", "200", "300", "500")
            );
            zoomStepSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                    if (isSettingValues) {
                        return;
                    }
                    try {
                        int v = Integer.parseInt(newVal);
                        if (v > 0) {
                            imageController.zoomStep = v;
                            UserConfig.setInt(imageController.baseName + "ZoomStep", imageController.zoomStep);
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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initViewOptions() {
        try {
            viewBox.getChildren().remove(stepPane);

            rulerColorController.init(this, "RulerColor", Color.RED);
            rulerColorController.setNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    if (isSettingValues) {
                        return;
                    }
                    BaseImageController.updateMaskRulerXY();
                }
            });

            gridColorController.init(this, "GridLinesColor", Color.LIGHTGRAY);
            gridColorController.setNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    if (isSettingValues) {
                        return;
                    }
                    BaseImageController.updateMaskGrid();
                }
            });

            gridWidthSelector.getItems().addAll(Arrays.asList("2", "1", "3", "4", "5", "6", "7", "8", "9", "10"));
            int v = UserConfig.getInt("GridLinesWidth", 1);
            if (v <= 0) {
                v = 1;
            }
            gridWidthSelector.setValue(v + "");
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
            v = UserConfig.getInt("GridLinesInterval", -1);
            gridIntervalSelector.setValue(v <= 0 ? message("Automatic") : (v + ""));
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
            float f = UserConfig.getFloat("GridLinesOpacity", 0.1f);
            if (f < 0) {
                f = 0.1f;
            }
            gridOpacitySelector.setValue(f + "");
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
            v = UserConfig.imageScale();
            if (v < 0) {
                v = 0;
            }
            decimalSelector.setValue(v + "");
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
            alphaColorSetController.setNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    if (!Color.WHITE.equals(alphaColorSetController.color())) {
                        alphaLabel.setText(message("AlphaReplaceComments"));
                        alphaLabel.setStyle(NodeStyleTools.darkRedTextStyle());
                    } else {
                        alphaLabel.setText("");
                    }
                }
            });

            thumbnailWidthInput.setText(AppVariables.thumbnailWidth + "");
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

            maxDemoInput.setText(AppVariables.maxDemoImage + "");
            maxDemoInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        long v = Long.parseLong(maxDemoInput.getText());
                        if (v > 0) {
                            UserConfig.setLong("MaxDemoImage", v);
                            AppVariables.maxDemoImage = v;
                            maxDemoInput.setStyle(null);
                        } else {
                            maxDemoInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        maxDemoInput.setStyle(UserConfig.badStyle());
                    }
                }
            });

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
            ImageHints = null;
        }
    }

    public synchronized void applyHints() {
        try {
            if (ImageHints == null) {
                return;
            }
            isSettingValues = true;

            renderCheck.setSelected(ImageRenderHints.applyHints());

            Object render = ImageHints.get(RenderingHints.KEY_RENDERING);
            if (RenderingHints.VALUE_RENDER_QUALITY.equals(render)) {
                renderQualityRadio.setSelected(true);
            } else if (RenderingHints.VALUE_RENDER_SPEED.equals(render)) {
                renderSpeedRadio.setSelected(true);
            } else {
                renderDefaultRadio.setSelected(true);
            }

            Object crender = ImageHints.get(RenderingHints.KEY_COLOR_RENDERING);
            if (RenderingHints.VALUE_COLOR_RENDER_QUALITY.equals(crender)) {
                colorRenderQualityRadio.setSelected(true);
            } else if (RenderingHints.VALUE_COLOR_RENDER_SPEED.equals(crender)) {
                colorRenderSpeedRadio.setSelected(true);
            } else {
                colorRenderDefaultRadio.setSelected(true);
            }

            Object pinter = ImageHints.get(RenderingHints.KEY_INTERPOLATION);
            if (RenderingHints.VALUE_INTERPOLATION_BILINEAR.equals(pinter)) {
                pInter4Radio.setSelected(true);
            } else if (RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR.equals(pinter)) {
                pInter1Radio.setSelected(true);
            } else {
                pInter9Radio.setSelected(true);
            }

            Object ainter = ImageHints.get(RenderingHints.KEY_ALPHA_INTERPOLATION);
            if (RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY.equals(ainter)) {
                aInterQualityRadio.setSelected(true);
            } else if (RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED.equals(ainter)) {
                aInterSpeedRadio.setSelected(true);
            } else {
                aInterDefaultRadio.setSelected(true);
            }

            Object anti = ImageHints.get(RenderingHints.KEY_ANTIALIASING);
            if (RenderingHints.VALUE_ANTIALIAS_ON.equals(anti)) {
                antiQualityRadio.setSelected(true);
            } else if (RenderingHints.VALUE_ANTIALIAS_OFF.equals(anti)) {
                antiSpeedRadio.setSelected(true);
            } else {
                antiDefaultRadio.setSelected(true);
            }

            Object tanti = ImageHints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
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

            Object fontfm = ImageHints.get(RenderingHints.KEY_FRACTIONALMETRICS);
            if (RenderingHints.VALUE_FRACTIONALMETRICS_ON.equals(fontfm)) {
                fmOnRadio.setSelected(true);
            } else if (RenderingHints.VALUE_FRACTIONALMETRICS_OFF.equals(fontfm)) {
                fmOffRadio.setSelected(true);
            } else {
                fmDefaultRadio.setSelected(true);
            }

            Object stroke = ImageHints.get(RenderingHints.KEY_STROKE_CONTROL);
            if (RenderingHints.VALUE_STROKE_NORMALIZE.equals(stroke)) {
                strokeNormalizeRadio.setSelected(true);
            } else if (RenderingHints.VALUE_STROKE_PURE.equals(stroke)) {
                strokePureRadio.setSelected(true);
            } else {
                strokeDefaultRadio.setSelected(true);
            }

            Object dither = ImageHints.get(RenderingHints.KEY_DITHERING);
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
                return ImageHints;
            }
            if (!ImageRenderHints.applyHints()) {
                ImageHints = null;
                return null;
            }
            ImageHints = new HashMap<>();

            if (renderQualityRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            } else if (renderSpeedRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            } else {
                ImageHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
            }

            if (colorRenderQualityRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            } else if (colorRenderSpeedRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            } else {
                ImageHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_DEFAULT);
            }

            if (pInter4Radio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            } else if (pInter1Radio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            } else {
                ImageHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            }

            if (aInterQualityRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            } else if (aInterSpeedRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            } else {
                ImageHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
            }

            if (antiQualityRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            } else if (antiSpeedRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            } else {
                ImageHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
            }

            if (tantiOnRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            } else if (tantiOffRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            } else if (tantiGaspRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
            } else if (tantiLcdHrgbRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            } else if (tantiLcdHbgrRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR);
            } else if (tantiLcdVrgbOnRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB);
            } else if (tantiLcdVbgrRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR);
            } else {
                ImageHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
            }

            if (fmOnRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            } else if (fmOffRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            } else {
                ImageHints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
            }

            if (strokeNormalizeRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
            } else if (strokePureRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            } else {
                ImageHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
            }

            if (ditherOnRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            } else if (ditherOffRadio.isSelected()) {
                ImageHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
            } else {
                ImageHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DEFAULT);
            }

            try (Connection conn = DerbyBase.getConnection()) {
                ImageRenderHints.saveImageRenderHints(conn);
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
            return ImageHints;
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
            ImageOptionsController controller = (ImageOptionsController) WindowTools.referredTopStage(
                    parent, Fxmls.ImageOptionsFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
