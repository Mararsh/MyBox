package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageBlend;
import mara.mybox.bufferedimage.PixelsBlend.ImagesBlendMode;
import mara.mybox.bufferedimage.PixelsBlendFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-2-6
 * @License Apache License Version 2.0
 */
public class ControlImagesBlend extends BaseController {

    protected ImagesBlendMode blendMode;
    protected Image foreImage, backImage;
    protected int x, y;
    protected float opacity;
    protected int keepRatioType;
    protected SimpleBooleanProperty optionChangedNotify;

    @FXML
    protected ComboBox<String> blendSelector, opacitySelector;
    @FXML
    protected CheckBox foreTopCheck, ignoreTransparentCheck;
    @FXML
    protected Button demoButton;

    public void setParameters(BaseController parent) {
        try {
            this.parentController = parent;
            baseName = parentController.baseName + "Blend";
            x = 0;
            y = 0;
            foreImage = null;
            backImage = null;

            optionChangedNotify = new SimpleBooleanProperty(false);

            foreTopCheck.setSelected(UserConfig.getBoolean(baseName + "OnTop", true));
            foreTopCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "OnTop", foreTopCheck.isSelected());
                    notifyOptionChanged();
                }
            });

            ignoreTransparentCheck.setSelected(UserConfig.getBoolean(baseName + "IgnoreTransparent", true));
            ignoreTransparentCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "IgnoreTransparent", ignoreTransparentCheck.isSelected());
                    notifyOptionChanged();
                }
            });

            String mode = UserConfig.getString(baseName + "BlendMode", message("NormalMode"));
            blendMode = PixelsBlendFactory.blendMode(mode);
            blendSelector.getItems().addAll(PixelsBlendFactory.blendModes());
            blendSelector.setValue(mode);
            blendSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    String mode = blendSelector.getSelectionModel().getSelectedItem();
                    blendMode = PixelsBlendFactory.blendMode(mode);
                    UserConfig.setString(baseName + "BlendMode", mode);
                    notifyOptionChanged();
                }
            });

            opacity = UserConfig.getInt(baseName + "BlendOpacity", 100) / 100f;
            opacity = (opacity >= 0.0f && opacity <= 1.0f) ? opacity : 1.0f;
            opacitySelector.getItems().addAll(Arrays.asList("0.5", "1.0", "0.3", "0.1", "0.8", "0.2", "0.9", "0.0"));
            opacitySelector.setValue(opacity + "");
            opacitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        float f = Float.parseFloat(newValue);
                        if (opacity >= 0.0f && opacity <= 1.0f) {
                            opacity = f;
                            UserConfig.setInt(baseName + "BlendOpacity", (int) (f * 100));
                            ValidationTools.setEditorNormal(opacitySelector);
                            notifyOptionChanged();
                        } else {
                            ValidationTools.setEditorBadStyle(opacitySelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(opacitySelector);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void notifyOptionChanged() {
        optionChangedNotify.set(!optionChangedNotify.get());
    }

    protected boolean isTop() {
        return foreTopCheck.isSelected();
    }

    protected boolean ignoreTransparent() {
        return ignoreTransparentCheck.isSelected();
    }

    @FXML
    protected void demo() {
        demo(foreImage, backImage);
    }

    protected void demo(Image foreImage, Image backImage) {
        this.foreImage = foreImage;
        this.backImage = backImage;
        this.x = 0;
        this.y = 0;
        FxImageTools.blendDemoFx(this, demoButton, foreImage, backImage,
                x, y, opacity, !foreTopCheck.isSelected(), ignoreTransparentCheck.isSelected());
    }

    protected BufferedImage blend(BufferedImage fore, BufferedImage back, int x, int y) {
        this.foreImage = SwingFXUtils.toFXImage(fore, null);
        this.backImage = SwingFXUtils.toFXImage(back, null);
        this.x = x;
        this.y = y;
        return ImageBlend.blend(fore, back, x, y,
                blendMode, opacity,
                !foreTopCheck.isSelected(),
                ignoreTransparentCheck.isSelected());
    }

    protected Image blend(Image foreImage, Image backImage, int x, int y) {
        this.foreImage = foreImage;
        this.backImage = backImage;
        this.x = x;
        this.y = y;
        return FxImageTools.blend(foreImage, backImage, x, y,
                blendMode, opacity,
                !foreTopCheck.isSelected(),
                ignoreTransparentCheck.isSelected());
    }

}
