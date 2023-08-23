package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.PixelsBlend;
import mara.mybox.bufferedimage.PixelsBlend.ImagesBlendMode;
import mara.mybox.bufferedimage.PixelsBlendFactory;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
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
            baseName = parentController.interfaceName + "Blend";
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
            MyBoxLog.error(e);
        }
    }

    public void notifyOptionChanged() {
        optionChangedNotify.set(!optionChangedNotify.get());
    }

    public boolean isTop() {
        return foreTopCheck.isSelected();
    }

    public boolean ignoreTransparent() {
        return ignoreTransparentCheck.isSelected();
    }

    public PixelsBlend blender() {
        return PixelsBlend.blender(blendMode, opacity, !isTop(), ignoreTransparent());
    }

    protected void setImage(Image image, Color color) {
        backImage = image;
        foreImage = FxImageTools.createImage((int) (image.getWidth() * 3 / 4), (int) (image.getHeight() * 3 / 4), color);
        x = (int) (backImage.getWidth() - foreImage.getWidth()) / 2;
        y = (int) (backImage.getHeight() - foreImage.getHeight()) / 2;
    }

    @FXML
    public void demo() {
        demo(foreImage, backImage, x, y);
    }

    public void demo(Image fImage, Image bImage, int x, int y) {
        this.foreImage = fImage;
        this.backImage = bImage;
        this.x = x;
        this.y = y;
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private List<File> files;

            @Override
            protected boolean handle() {
                try {
                    if (backImage == null) {
                        backImage = new Image("img/cover" + AppValues.AppYear + "g5.png");
                    }
                    if (foreImage == null) {
                        foreImage = FxImageTools.createImage(
                                (int) (backImage.getWidth() * 3 / 4), (int) (backImage.getHeight() * 3 / 4),
                                Color.WHITE);
                    }
                    BufferedImage foreBI = SwingFXUtils.fromFXImage(foreImage, null);
                    foreBI = ScaleTools.scaleImageLess(foreBI, 1000000);
                    BufferedImage backBI = SwingFXUtils.fromFXImage(backImage, null);
                    backBI = ScaleTools.scaleImageLess(backBI, 1000000);
                    files = new ArrayList<>();
                    boolean reversed = !isTop();
                    boolean ignoreTrans = ignoreTransparent();
                    float copacity = opacity >= 1f ? 0.5f : 1f;
                    BufferedImage blended = PixelsBlend.blend(foreBI, backBI, x, y,
                            PixelsBlend.blender(ImagesBlendMode.NORMAL, copacity, reversed, ignoreTrans));
                    if (task == null || isCancelled()) {
                        return true;
                    }
                    File tmpFile = new File(AppVariables.MyBoxTempPath + File.separator
                            + message("NormalMode") + "-" + message("Opacity") + "-" + copacity + "f.png");
                    if (ImageFileWriters.writeImageFile(blended, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile.getAbsolutePath());
                    }
                    for (String name : PixelsBlendFactory.blendModes()) {
                        if (task == null || isCancelled()) {
                            return true;
                        }
                        PixelsBlend.ImagesBlendMode mode = PixelsBlendFactory.blendMode(name);
                        blended = PixelsBlend.blend(foreBI, backBI, x, y,
                                PixelsBlend.blender(mode, opacity, reversed, ignoreTrans));
                        if (task == null || isCancelled()) {
                            return true;
                        }
                        tmpFile = new File(AppVariables.MyBoxTempPath + File.separator + name + "-"
                                + message("Opacity") + "-" + opacity + "f.png");
                        if (ImageFileWriters.writeImageFile(blended, tmpFile)) {
                            files.add(tmpFile);
                            task.setInfo(tmpFile.getAbsolutePath());
                        }
                    }
                    return !files.isEmpty();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                if (files != null && !files.isEmpty()) {
                    ImagesBrowserController b
                            = (ImagesBrowserController) WindowTools.openStage(Fxmls.ImagesBrowserFxml);
                    b.loadImages(files);
                }
            }

        };
        start(task);
    }

    public BufferedImage blend(BufferedImage fore, BufferedImage back, int x, int y) {
        this.foreImage = SwingFXUtils.toFXImage(fore, null);
        this.backImage = SwingFXUtils.toFXImage(back, null);
        this.x = x;
        this.y = y;
        return PixelsBlend.blend(fore, back, x, y, blender());
    }

    public Image blend(Image foreImage, Image backImage, int x, int y) {
        this.foreImage = foreImage;
        this.backImage = backImage;
        this.x = x;
        this.y = y;
        return FxImageTools.blend(foreImage, backImage, x, y, blender());
    }

}
