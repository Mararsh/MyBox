package mara.mybox.controller;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import mara.mybox.bufferedimage.ImageTextTools;
import mara.mybox.bufferedimage.PixelsBlend;
import mara.mybox.bufferedimage.PixelsBlendFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-9-25
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchTextController extends BaseImageManufactureBatchController {

    private int fontSize, angle, shadow, waterX, waterY, positionType, textWidth, textHeight, margin;
    protected PixelsBlend.ImagesBlendMode blendMode;
    protected float opacity;
    private java.awt.Font font;
    private java.awt.Color color;

    @FXML
    protected ComboBox<String> waterFamilyBox, waterStyleBox, waterSizeBox, waterShadowBox,
            waterAngleBox, opacitySelector, blendSelector;
    @FXML
    protected ColorSet colorSetController;
    @FXML
    protected ToggleGroup positionGroup;
    @FXML
    protected TextField waterInput, waterXInput, waterYInput, marginInput;
    @FXML
    protected CheckBox outlineCheck, verticalCheck, blendTopCheck, ignoreTransparentCheck;

    private class PositionType {

        static final int RightBottom = 0;
        static final int RightTop = 1;
        static final int LeftBottom = 2;
        static final int LeftTop = 3;
        static final int Center = 4;
        static final int Custom = 5;
    }

    public ImageManufactureBatchTextController() {
        baseTitle = Languages.message("ImageManufactureBatchText");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(targetPathController.valid.not()
                    .or(Bindings.isEmpty(tableView.getItems()))
                    .or(Bindings.isEmpty(waterInput.textProperty()))
                    .or(waterXInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(waterYInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(marginInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {
        try {

            waterXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkWaterPosition();
                }
            });
            waterYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkWaterPosition();
                }
            });

            fontSize = UserConfig.getInt(baseName + "FontSize", 72);
            List<String> sizes = Arrays.asList(
                    "72", "18", "15", "9", "10", "12", "14", "17", "24", "36", "48", "64", "96");
            waterSizeBox.getItems().addAll(sizes);
            waterSizeBox.setValue(fontSize + "");
            waterSizeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            fontSize = v;
                            UserConfig.setInt(baseName + "FontSize", fontSize);
                            ValidationTools.setEditorNormal(waterSizeBox);
                        } else {
                            ValidationTools.setEditorBadStyle(waterSizeBox);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(waterSizeBox);
                    }
                }
            });

            try {
                float f = Float.valueOf(UserConfig.getString(baseName + "Opacity", "1.0"));
                if (f >= 0.0f && f <= 1.0f) {
                    opacity = f;
                }
            } catch (Exception e) {
                opacity = 1.0f;
            }
            String mode = UserConfig.getString(baseName + "TextBlendMode", Languages.message("NormalMode"));
            blendMode = PixelsBlendFactory.blendMode(mode);
            blendSelector.getItems().addAll(PixelsBlendFactory.blendModes());
            blendSelector.setValue(mode);
            blendSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    String mode = blendSelector.getSelectionModel().getSelectedItem();
                    blendMode = PixelsBlendFactory.blendMode(mode);
                    UserConfig.setString(baseName + "TextBlendMode", mode);
                }
            });

            opacity = UserConfig.getInt(baseName + "TextOpacity", 100) / 100f;
            opacity = (opacity >= 0.0f && opacity <= 1.0f) ? opacity : 1.0f;
            opacitySelector.getItems().addAll(Arrays.asList("0.5", "1.0", "0.3", "0.1", "0.8", "0.2", "0.9", "0.0"));
            opacitySelector.setValue(opacity + "");
            opacitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        float f = Float.valueOf(newValue);
                        if (opacity >= 0.0f && opacity <= 1.0f) {
                            opacity = f;
                            UserConfig.setInt(baseName + "TextOpacity", (int) (f * 100));
                            ValidationTools.setEditorNormal(opacitySelector);
                        } else {
                            ValidationTools.setEditorBadStyle(opacitySelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(opacitySelector);
                    }
                }
            });

            blendTopCheck.setSelected(UserConfig.getBoolean(baseName + "TextBlendTop", true));
            blendTopCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_toggle, Boolean new_toggle) {
                    UserConfig.setBoolean(baseName + "TextBlendTop", blendTopCheck.isSelected());
                }
            });

            ignoreTransparentCheck.setSelected(UserConfig.getBoolean(baseName + "IgnoreTransparent", true));
            ignoreTransparentCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "IgnoreTransparent", ignoreTransparentCheck.isSelected());
                }
            });

            shadow = UserConfig.getInt(baseName + "Shadow", 0);
            waterShadowBox.getItems().addAll(Arrays.asList("0", "4", "5", "3", "2", "1", "6"));
            waterShadowBox.setValue(shadow + "");
            waterShadowBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            shadow = v;
                            UserConfig.setInt(baseName + "Shadow", shadow);
                            ValidationTools.setEditorNormal(waterShadowBox);
                        } else {
                            ValidationTools.setEditorBadStyle(waterShadowBox);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(waterShadowBox);
                    }
                }
            });

            List<String> styles = Arrays.asList(Languages.message("Regular"), Languages.message("Bold"), Languages.message("Italic"), Languages.message("Bold Italic"));
            waterStyleBox.getItems().addAll(styles);
            waterStyleBox.getSelectionModel().select(0);

            GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] fontNames = e.getAvailableFontFamilyNames();
            waterFamilyBox.getItems().addAll(Arrays.asList(fontNames));
            waterFamilyBox.setValue(UserConfig.getString(baseName + "FontFamily", fontNames[0]));
            waterFamilyBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (newValue != null) {
                        UserConfig.setString(baseName + "FontFamily", newValue);
                    }
                }
            });

            colorSetController.init(this, baseName + "Color", Color.RED);

            angle = UserConfig.getInt(baseName + "Angle", 0);
            waterAngleBox.getItems().addAll(Arrays.asList("0", "90", "180", "270", "45", "135", "225", "315",
                    "60", "150", "240", "330", "15", "105", "195", "285", "30", "120", "210", "300"));
            waterAngleBox.setVisibleRowCount(10);
            waterAngleBox.setValue(angle + "");
            waterAngleBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            angle = v;
                            UserConfig.setInt(baseName + "Angle", angle);
                            ValidationTools.setEditorNormal(waterAngleBox);
                        } else {
                            ValidationTools.setEditorBadStyle(waterAngleBox);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(waterAngleBox);
                    }
                }
            });

            margin = UserConfig.getInt(baseName + "Margin", 20);
            marginInput.setText(margin + "");
            positionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkPositionType();
                }
            });
            checkPositionType();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void checkPositionType() {
        waterXInput.setDisable(true);
        waterXInput.setStyle(null);
        waterYInput.setDisable(true);
        waterYInput.setStyle(null);
        marginInput.setDisable(true);
        marginInput.setStyle(null);

        RadioButton selected = (RadioButton) positionGroup.getSelectedToggle();
        if (Languages.message("RightBottom").equals(selected.getText())) {
            positionType = PositionType.RightBottom;
            marginInput.setDisable(false);
            checkMargin();

        } else if (Languages.message("RightTop").equals(selected.getText())) {
            positionType = PositionType.RightTop;
            marginInput.setDisable(false);
            checkMargin();

        } else if (Languages.message("LeftBottom").equals(selected.getText())) {
            positionType = PositionType.LeftBottom;
            marginInput.setDisable(false);
            checkMargin();

        } else if (Languages.message("LeftTop").equals(selected.getText())) {
            positionType = PositionType.LeftTop;
            marginInput.setDisable(false);
            checkMargin();

        } else if (Languages.message("Center").equals(selected.getText())) {
            positionType = PositionType.Center;

        } else if (Languages.message("Custom").equals(selected.getText())) {
            positionType = PositionType.Custom;
            waterXInput.setDisable(false);
            waterYInput.setDisable(false);
            checkWaterPosition();
        }
    }

    private void checkMargin() {
        try {
            int v = Integer.valueOf(marginInput.getText());
            if (v >= 0) {
                margin = v;
                UserConfig.setInt(baseName + "Margin", margin);
                marginInput.setStyle(null);
            } else {
                marginInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            marginInput.setStyle(UserConfig.badStyle());
        }

    }

    private void checkWaterPosition() {
        try {
            int v = Integer.valueOf(waterXInput.getText());
            if (v >= 0) {
                waterX = v;
                waterXInput.setStyle(null);
            } else {
                waterXInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            waterXInput.setStyle(UserConfig.badStyle());
        }

        try {
            int v = Integer.valueOf(waterYInput.getText());
            if (v >= 0) {
                waterY = v;
                waterYInput.setStyle(null);
            } else {
                waterYInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            waterYInput.setStyle(UserConfig.badStyle());
        }

    }

    @Override
    public boolean makeMoreParameters() {
        if (!super.makeMoreParameters()) {
            return false;
        }

        String fontFamily = waterFamilyBox.getValue();
        String fontStyle = waterStyleBox.getValue();

        Font FxFont;
        if (Languages.message("Bold").equals(fontStyle)) {
            font = new java.awt.Font(fontFamily, java.awt.Font.BOLD, fontSize);
            FxFont = Font.font(fontFamily, FontWeight.BOLD, FontPosture.REGULAR, fontSize);

        } else if (Languages.message("Italic").equals(fontStyle)) {
            font = new java.awt.Font(fontFamily, java.awt.Font.ITALIC, fontSize);
            FxFont = Font.font(fontFamily, FontWeight.NORMAL, FontPosture.ITALIC, fontSize);

        } else if (Languages.message("Bold Italic").equals(fontStyle)) {
            font = new java.awt.Font(fontFamily, java.awt.Font.BOLD + java.awt.Font.ITALIC, fontSize);
            FxFont = Font.font(fontFamily, FontWeight.BOLD, FontPosture.ITALIC, fontSize);

        } else {
            font = new java.awt.Font(fontFamily, java.awt.Font.PLAIN, fontSize);
            FxFont = Font.font(fontFamily, FontWeight.NORMAL, FontPosture.REGULAR, fontSize);
        }

        color = FxColorTools.toAwtColor((Color) colorSetController.rect.getFill());

        final String msg = waterInput.getText().trim();
        final Text text = new Text(msg);
        text.setFont(FxFont);
        textWidth = (int) Math.round(text.getLayoutBounds().getWidth());
        textHeight = (int) Math.round(text.getLayoutBounds().getHeight());
        return true;
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            if (waterInput.getText() == null || waterInput.getText().trim().isEmpty()) {
                return null;
            }

            int x, y;
            switch (positionType) {
                case PositionType.Center:
                    x = (source.getWidth() - textWidth) / 2;
                    y = (source.getHeight() + textHeight) / 2;
                    break;
                case PositionType.RightBottom:
                    x = source.getWidth() - 1 - textWidth - margin;
                    y = source.getHeight() - 1 - margin;
                    break;
                case PositionType.RightTop:
                    x = source.getWidth() - 1 - textWidth - margin;
                    y = textHeight + margin;
                    break;
                case PositionType.LeftBottom:
                    x = margin;
                    y = source.getHeight() - 1 - margin;
                    break;
                case PositionType.Custom:
                    x = waterX - textWidth / 2;
                    y = waterY + textHeight / 2;
                    break;
                default:
                    x = margin;
                    y = textHeight + margin;
                    break;
            }

            BufferedImage target = ImageTextTools.addText(source,
                    waterInput.getText().trim(), font, color,
                    x, y, blendMode, opacity, !blendTopCheck.isSelected(), ignoreTransparentCheck.isSelected(),
                    shadow, angle, outlineCheck.isSelected(), verticalCheck.isSelected());

            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }

    }

}
