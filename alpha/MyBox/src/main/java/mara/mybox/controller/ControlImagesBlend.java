package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.PixelsBlend;
import mara.mybox.bufferedimage.PixelsBlend.ImagesBlendMode;
import mara.mybox.bufferedimage.PixelsBlend.TransparentAs;
import static mara.mybox.bufferedimage.PixelsBlend.fixedOpacity;
import mara.mybox.bufferedimage.PixelsBlendFactory;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.SingletonCurrentTask;
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
    protected float opacity;
    protected int keepRatioType;
    protected TransparentAs baseTransparentAs = TransparentAs.Transparent,
            overlayTransparentAs = TransparentAs.Another;

    @FXML
    protected ListView<String> modeList;
    @FXML
    protected ComboBox<String> opacitySelector;
    @FXML
    protected CheckBox baseAboveCheck;
    @FXML
    protected ToggleGroup baseGroup, overlayGroup;
    @FXML
    protected RadioButton baseAsOverlayRadio, baseAsTransparentRadio, baseBlendRadio,
            overlayAsBaseRadio, overlayAsTransparentRadio, overlayBlendRadio;
    @FXML
    protected Button demoButton;

    public void setParameters(BaseController parent) {
        try (Connection conn = DerbyBase.getConnection()) {
            setParameters(conn, parent);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(Connection conn, BaseController parent) {
        try {
            this.parentController = parent;
            baseName = parentController.baseName + "_Blend";

//            baseAboveCheck.setSelected(UserConfig.getBoolean(conn, baseName + "BaseAbove", false));
            String v = UserConfig.getString(conn, baseName + "BaseTransparentAs", "Transparent");
            if ("Overlay".equals(v)) {
                baseAsOverlayRadio.setSelected(true);
                baseTransparentAs = TransparentAs.Another;
            } else if ("Blend".equals(v)) {
                baseBlendRadio.setSelected(true);
                baseTransparentAs = TransparentAs.Blend;
            } else {
                baseAsTransparentRadio.setSelected(true);
                baseTransparentAs = TransparentAs.Transparent;
            }

            v = UserConfig.getString(conn, baseName + "OverlayTransparentAs", "Another");
            if ("Transparent".equals(v)) {
                overlayAsTransparentRadio.setSelected(true);
                overlayTransparentAs = TransparentAs.Transparent;
            } else if ("Blend".equals(v)) {
                overlayBlendRadio.setSelected(true);
                overlayTransparentAs = TransparentAs.Blend;
            } else {
                overlayAsBaseRadio.setSelected(true);
                overlayTransparentAs = TransparentAs.Another;
            }

            String mode = UserConfig.getString(conn, baseName + "BlendMode", message("NormalMode"));
            blendMode = PixelsBlendFactory.blendMode(mode);
            modeList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            modeList.getItems().setAll(PixelsBlendFactory.blendModes());
            modeList.scrollTo(mode);
            modeList.getSelectionModel().select(mode);

            opacity = UserConfig.getInt(conn, baseName + "BlendOpacity", 100) / 100f;
            opacity = (opacity >= 0.0f && opacity <= 1.0f) ? opacity : 1.0f;
            opacitySelector.getItems().addAll(Arrays.asList("0.5", "1.0", "0.3", "0.1", "0.8", "0.2", "0.9", "0.0"));
            opacitySelector.setValue(opacity + "");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean isBaseAbove() {
        return baseAboveCheck.isSelected();
    }

    public TransparentAs baseTransparentAs() {
        if (baseAsOverlayRadio.isSelected()) {
            baseTransparentAs = TransparentAs.Another;
        } else if (baseBlendRadio.isSelected()) {
            baseTransparentAs = TransparentAs.Blend;
        } else {
            baseTransparentAs = TransparentAs.Transparent;
        }
        return baseTransparentAs;
    }

    public TransparentAs overlayTransparentAs() {
        if (overlayAsTransparentRadio.isSelected()) {
            overlayTransparentAs = TransparentAs.Transparent;
        } else if (overlayBlendRadio.isSelected()) {
            overlayTransparentAs = TransparentAs.Blend;
        } else {
            overlayTransparentAs = TransparentAs.Another;
        }
        return overlayTransparentAs;
    }

    public float opacity() {
        float f = -1;
        try {
            f = Float.parseFloat(opacitySelector.getValue());
        } catch (Exception e) {
        }
        if (f >= 0.0f && f <= 1.0f) {
            opacity = f;
        } else {
            popError(message("InvalidParameter") + ": " + message("Opacity"));
        }
        return f;
    }

    public boolean checkValues() {
        return opacity() >= 0;
    }

    public PixelsBlend pickValues() {
        if (!checkValues()) {
            return null;
        }
        PixelsBlend blend = null;
        try (Connection conn = DerbyBase.getConnection()) {
            blend = pickValues(conn);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return blend;
    }

    public PixelsBlend pickValues(Connection conn) {
        try {
            String mode = modeList.getSelectionModel().getSelectedItem();
            blendMode = PixelsBlendFactory.blendMode(mode);
            UserConfig.setString(conn, baseName + "BlendMode", mode);

            UserConfig.setInt(conn, baseName + "BlendOpacity", (int) (opacity * 100));

//            UserConfig.setBoolean(conn, baseName + "BaseAbove", baseAboveCheck.isSelected());
            baseTransparentAs();
            UserConfig.setString(conn, baseName + "BaseTransparentAs", baseTransparentAs.name());

            overlayTransparentAs();
            UserConfig.setString(conn, baseName + "OverlayTransparentAs", overlayTransparentAs.name());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return makeBlend();
    }

    public PixelsBlend makeBlend() {
        return PixelsBlendFactory.create(blendMode)
                .setBlendMode(blendMode)
                .setOpacity(fixedOpacity(opacity))
                .setBaseAbove(baseAboveCheck.isSelected())
                .setBaseTransparentAs(baseTransparentAs)
                .setOverlayTransparentAs(overlayTransparentAs);
    }

    @FXML
    public void demo() {
        Image baseImage = null;
        Image overlay = null;
        Color color = Color.PINK;
        if (parentController instanceof ImageBlendColorController) {
            baseImage = ((ImageBlendColorController) parentController).srcImage();
            color = ((ImageBlendColorController) parentController).colorController.color();

        } else if (parentController instanceof ImagePasteController) {
            baseImage = ((ImagePasteController) parentController).srcImage();
            overlay = ((ImagePasteController) parentController).finalClip;

        } else if (parentController instanceof BaseImageEditController) {
            baseImage = ((BaseImageEditController) parentController).srcImage();

        }

        if (baseImage == null) {
            baseImage = new Image("img/cover" + AppValues.AppYear + "g5.png");
        }
        if (overlay == null) {
            overlay = FxImageTools.createImage(
                    (int) (baseImage.getWidth()), (int) (baseImage.getHeight()),
                    color);
        }
        demo(baseImage, overlay);
    }

    public void demo(Image baseImage, Image overlay) {
        if (baseImage == null || overlay == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private List<File> files;

            @Override
            protected boolean handle() {
                try {
                    BufferedImage overlayBI = SwingFXUtils.fromFXImage(overlay, null);
                    overlayBI = ScaleTools.demoImage(overlayBI);
                    BufferedImage baseBI = SwingFXUtils.fromFXImage(baseImage, null);
                    baseBI = ScaleTools.demoImage(baseBI);
                    int x = (int) (baseBI.getWidth() - overlayBI.getWidth()) / 2;
                    int y = (int) (baseBI.getHeight() - overlayBI.getHeight()) / 2;
                    files = new ArrayList<>();
                    float copacity = opacity >= 1f ? 0.5f : 1f;
                    PixelsBlend blender = PixelsBlendFactory.create(ImagesBlendMode.NORMAL)
                            .setBlendMode(ImagesBlendMode.NORMAL)
                            .setOpacity(copacity)
                            .setBaseAbove(baseAboveCheck.isSelected())
                            .setBaseTransparentAs(baseTransparentAs)
                            .setOverlayTransparentAs(overlayTransparentAs);
                    BufferedImage blended = PixelsBlend.blend(overlayBI, baseBI, x, y, blender);
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
                        blender = PixelsBlendFactory.create(mode)
                                .setBlendMode(mode)
                                .setOpacity(opacity)
                                .setBaseAbove(baseAboveCheck.isSelected())
                                .setBaseTransparentAs(baseTransparentAs)
                                .setOverlayTransparentAs(overlayTransparentAs);
                        blended = PixelsBlend.blend(overlayBI, baseBI, x, y, blender);
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

}
