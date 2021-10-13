package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.bufferedimage.ImageBlend;
import mara.mybox.bufferedimage.MargionTools;
import mara.mybox.bufferedimage.PixelsBlend;
import mara.mybox.bufferedimage.PixelsBlendFactory;
import mara.mybox.bufferedimage.TransformTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.value.UserConfig;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.value.Colors;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-6-22
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchPasteController extends BaseImageManufactureBatchController {

    protected int positionType, margin, posX, posY;
    protected PixelsBlend.ImagesBlendMode blendMode;
    protected float opacity;
    protected BufferedImage clipSource;
    protected int rotateAngle;

    @FXML
    protected ComboBox<String> opacitySelector, blendSelector, angleSelector;
    @FXML
    protected CheckBox enlargeCheck, clipTopCheck, ignoreTransparentCheck;
    @FXML
    protected ToggleGroup positionGroup;
    @FXML
    protected TextField xInput, yInput, marginInput;
    @FXML
    protected Button demoButton;

    private class PositionType {

        static final int RightBottom = 0;
        static final int RightTop = 1;
        static final int LeftBottom = 2;
        static final int LeftTop = 3;
        static final int Center = 4;
        static final int Custom = 5;
    }

    public ImageManufactureBatchPasteController() {
        baseTitle = Languages.message("ImageManufactureBatchPaste");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(Bindings.isEmpty(tableView.getItems()))
                    .or(Bindings.isEmpty(sourceFileInput.textProperty()))
                    .or(sourceFileInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(xInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(yInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(marginInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {
        try {
            rotateAngle = 0;

            enlargeCheck.setSelected(UserConfig.getBoolean(baseName + "EnlargerImageAsClip", true));
            enlargeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "EnlargerImageAsClip", enlargeCheck.isSelected());
                }
            });

            clipTopCheck.setSelected(UserConfig.getBoolean(baseName + "ClipOnTop", true));
            clipTopCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "ClipOnTop", clipTopCheck.isSelected());
                }
            });

            ignoreTransparentCheck.setSelected(UserConfig.getBoolean(baseName + "IgnoreTransparent", true));
            ignoreTransparentCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "IgnoreTransparent", ignoreTransparentCheck.isSelected());
                }
            });

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

            opacity = UserConfig.getInt(baseName + "Opacity", 100) / 100f;
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
                            UserConfig.setInt(baseName + "Opacity", (int) (f * 100));
                            ValidationTools.setEditorNormal(opacitySelector);
                        } else {
                            ValidationTools.setEditorBadStyle(opacitySelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(opacitySelector);
                    }
                }
            });

            angleSelector.getItems().addAll(Arrays.asList("0", "90", "180", "45", "30", "60", "15", "5", "10", "1", "75", "120", "135"));
            angleSelector.setVisibleRowCount(10);
            angleSelector.setValue("0");
            angleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        rotateAngle = Integer.valueOf(newValue);
                        ValidationTools.setEditorNormal(angleSelector);
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(angleSelector);
                    }
                }
            });

            xInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkWaterPosition();
                }
            });
            yInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkWaterPosition();
                }
            });

            margin = UserConfig.getInt(baseName + "Margin", 20);
            marginInput.setText(margin + "");
            positionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkPositionType();
                }
            });
            checkPositionType();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void checkPositionType() {
        xInput.setDisable(true);
        xInput.setStyle(null);
        yInput.setDisable(true);
        yInput.setStyle(null);
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
            xInput.setDisable(false);
            yInput.setDisable(false);
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
            int v = Integer.valueOf(xInput.getText());
            if (v >= 0) {
                posX = v;
                xInput.setStyle(null);
            } else {
                xInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            xInput.setStyle(UserConfig.badStyle());
        }

        try {
            int v = Integer.valueOf(yInput.getText());
            if (v >= 0) {
                posY = v;
                yInput.setStyle(null);
            } else {
                yInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            yInput.setStyle(UserConfig.badStyle());
        }

    }

    @Override
    public boolean makeMoreParameters() {
        if (!super.makeMoreParameters()) {
            return false;
        }

        clipSource = ImageFileReaders.readImage(sourceFile);
        if (clipSource != null) {
            clipSource = TransformTools.rotateImage(clipSource, rotateAngle);
        }
        return clipSource != null;
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            BufferedImage bgImage = source;
            if (enlargeCheck.isSelected()) {
                if (clipSource.getWidth() > bgImage.getWidth()) {
                    bgImage = MargionTools.addMargins(bgImage,
                            Colors.TRANSPARENT, clipSource.getWidth() - bgImage.getWidth() + 1,
                            false, false, false, true);
                }
                if (clipSource.getHeight() > bgImage.getHeight()) {
                    bgImage = MargionTools.addMargins(bgImage,
                            Colors.TRANSPARENT, clipSource.getHeight() - bgImage.getHeight() + 1,
                            false, true, false, false);
                }
            }

            int x, y;
            switch (positionType) {
                case PositionType.Center:
                    x = (bgImage.getWidth() - clipSource.getWidth()) / 2;
                    y = (bgImage.getHeight() - clipSource.getHeight()) / 2;
                    break;
                case PositionType.RightBottom:
                    x = bgImage.getWidth() - 1 - margin;
                    y = bgImage.getHeight() - 1 - margin;
                    break;
                case PositionType.RightTop:
                    x = bgImage.getWidth() - 1 - margin;
                    y = margin;
                    break;
                case PositionType.LeftBottom:
                    x = margin;
                    y = bgImage.getHeight() - 1 - margin;
                    break;
                case PositionType.Custom:
                    x = posX;
                    y = posY;
                    break;
                default:
                    x = margin;
                    y = margin;
                    break;
            }

            BufferedImage target = ImageBlend.blend(clipSource, bgImage, x, y,
                    blendMode, opacity, !clipTopCheck.isSelected(), ignoreTransparentCheck.isSelected());
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }

    }

    @FXML
    protected void demo() {
        FxImageTools.blendDemo(this, demoButton, null, null, 20, 20, opacity, !clipTopCheck.isSelected(), ignoreTransparentCheck.isSelected());
    }

}
