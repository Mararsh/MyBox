package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.BufferedImageTools.KeepRatioType;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.PixelsBlend.ImagesBlendMode;
import mara.mybox.bufferedimage.PixelsBlendFactory;
import mara.mybox.controller.ImageManufactureController_Image.ImageOperation;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fximage.MarginTools;
import mara.mybox.fximage.ScaleTools;
import mara.mybox.fximage.TransformTools;
import mara.mybox.fxml.ImageClipboardTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageManufactureClipboardController extends ImageManufactureOperationController {

    protected ImagesBlendMode blendMode;
    protected float opacity;
    protected Image clipSource, currentClip, blendedImage, finalClip, bgImage;
    protected DoubleRectangle rectangle;
    protected int keepRatioType;

    @FXML
    protected ControlImagesClipboard clipsController;
    @FXML
    protected Tab imagesPane, setPane;
    @FXML
    protected ComboBox<String> blendBox, opacityBox, angleBox, ratioBox;
    @FXML
    protected Slider angleSlider;
    @FXML
    protected CheckBox keepRatioCheck, enlargeCheck, clipTopCheck, ignoreTransparentCheck;
    @FXML
    protected Label listLabel;
    @FXML
    protected Button demoButton;

    @Override
    public void initControls() {
        try {
            super.initControls();

            clipsController.useClipButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    selectClip();
                }
            });

            clipsController.tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        selectClip();
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initPane() {
        try {
            super.initPane();

            rotateAngle = currentAngle = 0;
            clipsController.setParameters(imageController, true);

            enlargeCheck.setSelected(UserConfig.getBoolean(baseName + "EnlargerImageAsClip", true));
            enlargeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "EnlargerImageAsClip", enlargeCheck.isSelected());
                    if (imageController != null) {
                        pasteClip(currentAngle);
                    }
                }
            });

            clipTopCheck.setSelected(UserConfig.getBoolean(baseName + "ClipOnTop", true));
            clipTopCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "ClipOnTop", clipTopCheck.isSelected());
                    if (imageController != null) {
                        pasteClip(currentAngle);
                    }
                }
            });

            ignoreTransparentCheck.setSelected(UserConfig.getBoolean(baseName + "IgnoreTransparent", true));
            ignoreTransparentCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "IgnoreTransparent", ignoreTransparentCheck.isSelected());
                    if (imageController != null) {
                        pasteClip(currentAngle);
                    }
                }
            });

            keepRatioCheck.setSelected(UserConfig.getBoolean(baseName + "KeepClipRatio", true));
            keepRatioCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "KeepClipRatio", keepRatioCheck.isSelected());
                    if (imageController != null) {
                        pasteClip(currentAngle);
                    }
                }
            });

            keepRatioType = KeepRatioType.BaseOnWidth;
            ratioBox.getItems().addAll(Arrays.asList(Languages.message("BaseOnWidth"), Languages.message("BaseOnHeight"),
                    Languages.message("BaseOnLarger"), Languages.message("BaseOnSmaller")));
            ratioBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        if (Languages.message("BaseOnWidth").equals(newValue)) {
                            keepRatioType = KeepRatioType.BaseOnWidth;
                        } else if (Languages.message("BaseOnHeight").equals(newValue)) {
                            keepRatioType = KeepRatioType.BaseOnHeight;
                        } else if (Languages.message("BaseOnLarger").equals(newValue)) {
                            keepRatioType = KeepRatioType.BaseOnLarger;
                        } else if (Languages.message("BaseOnSmaller").equals(newValue)) {
                            keepRatioType = KeepRatioType.BaseOnSmaller;
                        } else {
                            keepRatioType = KeepRatioType.None;
                        }
                    }
                }
            });
            ratioBox.getSelectionModel().select(0);

            String mode = UserConfig.getString("ImageClipBlendMode", Languages.message("NormalMode"));
            blendMode = PixelsBlendFactory.blendMode(mode);
            blendBox.getItems().addAll(PixelsBlendFactory.blendModes());
            blendBox.setValue(mode);
            blendBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    String mode = blendBox.getSelectionModel().getSelectedItem();
                    blendMode = PixelsBlendFactory.blendMode(mode);
                    UserConfig.setString("ImageClipBlendMode", mode);
                    if (imageController != null) {
                        pasteClip(rotateAngle);
                    }
                }
            });

            opacity = UserConfig.getInt(baseName + "ImageClipOpacity", 100) / 100f;
            opacity = (opacity >= 0.0f && opacity <= 1.0f) ? opacity : 1.0f;
            opacityBox.getItems().addAll(Arrays.asList("0.5", "1.0", "0.3", "0.1", "0.8", "0.2", "0.9", "0.0"));
            opacityBox.setValue(opacity + "");
            opacityBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        float f = Float.valueOf(newValue);
                        if (opacity >= 0.0f && opacity <= 1.0f) {
                            opacity = f;
                            UserConfig.setInt("ImageClipOpacity", (int) (f * 100));
                            ValidationTools.setEditorNormal(opacityBox);
                            if (imageController != null) {
                                pasteClip(0);
                            }
                        } else {
                            ValidationTools.setEditorBadStyle(opacityBox);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(opacityBox);
                    }
                }
            });

            angleSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    pasteClip(newValue.intValue());
                }
            });

            angleBox.getItems().addAll(Arrays.asList("0", "90", "180", "45", "30", "60", "15", "5", "10", "1", "75", "120", "135"));
            angleBox.setVisibleRowCount(10);
            angleBox.setValue("0");
            angleBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        rotateAngle = Integer.valueOf(newValue);
                        rotateLeftButton.setDisable(false);
                        rotateRightButton.setDisable(false);
                        ValidationTools.setEditorNormal(angleBox);
                    } catch (Exception e) {
                        rotateLeftButton.setDisable(true);
                        rotateRightButton.setDisable(true);
                        ValidationTools.setEditorBadStyle(angleBox);
                    }
                }
            });

            okButton.setDisable(true);
            cancelButton.disableProperty().bind(okButton.disableProperty());
            demoButton.disableProperty().bind(okButton.disableProperty());
            angleSlider.disableProperty().bind(okButton.disableProperty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void paneExpanded() {
        imageController.showRightPane();
        imageController.resetImagePane();
        imageController.imageTab();

    }

    public void selectClip() {
        initOperation();
        if (clipsController.tableData.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private Image clipImage;

                @Override
                protected boolean handle() {
                    try {
                        ImageClipboard clip = clipsController.tableView.getSelectionModel().getSelectedItem();
                        if (clip == null) {
                            clip = clipsController.tableData.get(0);
                        }
                        clipImage = ImageClipboard.loadImage(clip);
                        return clipImage != null;
                    } catch (Exception e) {
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    setSourceClip(clipImage);
                }
            };
            imageController.start(task);
        }
    }

    public void setSourceClip(Image image) {
        try {
            if (image == null) {
                return;
            }
            clipSource = image;
            currentClip = clipSource;
            imageController.scope = new ImageScope();
            imageController.maskRectangleData = new DoubleRectangle(0, 0,
                    currentClip.getWidth() - 1, currentClip.getHeight() - 1);
            imageController.scope.setRectangle(imageController.maskRectangleData.cloneValues());
            pasteClip(0);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void pasteClip(int angle) {
        if (clipSource == null) {
            return;
        }
        imageController.showRightPane();
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private boolean enlarged;

                @Override
                protected boolean handle() {
                    try {
                        if (angle != 0) {
                            currentClip = TransformTools.rotateImage(clipSource, angle);
                            if (task == null || isCancelled()) {
                                return false;
                            }
                        }
                        finalClip = ScaleTools.scaleImage(currentClip,
                                (int) imageController.scope.getRectangle().getWidth(),
                                (int) imageController.scope.getRectangle().getHeight(),
                                keepRatioCheck.isSelected(), keepRatioType);
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        bgImage = imageView.getImage();
                        enlarged = false;
                        if (enlargeCheck.isSelected()) {
                            if (finalClip.getWidth() > bgImage.getWidth()) {
                                bgImage = MarginTools.addMarginsFx(bgImage,
                                        Color.TRANSPARENT, (int) (finalClip.getWidth() - bgImage.getWidth()) + 1,
                                        false, false, false, true);
                                enlarged = true;
                            }
                            if (finalClip.getHeight() > bgImage.getHeight()) {
                                bgImage = MarginTools.addMarginsFx(bgImage,
                                        Color.TRANSPARENT, (int) (finalClip.getHeight() - bgImage.getHeight()) + 1,
                                        false, true, false, false);
                                enlarged = true;
                            }
                        }
                        blendedImage = FxImageTools.blend(
                                finalClip, bgImage,
                                (int) imageController.scope.getRectangle().getSmallX(),
                                (int) imageController.scope.getRectangle().getSmallY(),
                                blendMode, opacity, !clipTopCheck.isSelected(), ignoreTransparentCheck.isSelected());
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        return blendedImage != null;
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    currentAngle = angle;
                    if (enlarged) {
                        imageController.setImage(ImageOperation.Margins, bgImage);
                    }
                    imageController.setMaskRectangleLineVisible(true);
                    imageController.maskRectangleData = new DoubleRectangle(
                            imageController.maskRectangleData.getSmallX(),
                            imageController.maskRectangleData.getSmallY(),
                            imageController.maskRectangleData.getSmallX() + finalClip.getWidth() - 1,
                            imageController.maskRectangleData.getSmallY() + finalClip.getHeight() - 1);
                    imageController.drawMaskRectangleLineAsData();
                    imageController.scope.setRectangle(imageController.maskRectangleData.cloneValues());
                    maskView.setImage(blendedImage);
                    maskView.setOpacity(1.0);
                    maskView.setVisible(true);
                    imageView.setVisible(false);
                    imageView.toBack();
                    tabPane.getSelectionModel().select(setPane);
                    okButton.setDisable(false);
                    okButton.requestFocus();
                    imageController.adjustRightPane();
                    imageController.operation = ImageOperation.Paste;
                }
            };
            imageController.start(task);
        }

    }

    @FXML
    @Override
    public void rotateRight() {
        pasteClip(rotateAngle);
    }

    @FXML
    @Override
    public void rotateLeft() {
        pasteClip(360 - rotateAngle);

    }

    @Override
    public void imageClicked(MouseEvent event, DoublePoint p) {
        if (imageController.scope == null || imageController.maskRectangleData == null) {
            return;
        }
        if (!imageController.scope.getRectangle().same(imageController.maskRectangleData)) {
            imageController.scope.setRectangle(imageController.maskRectangleData.cloneValues());
            pasteClip(0);
        }
    }

    @FXML
    @Override
    public void okAction() {
        imageController.popSuccessful();
        imageController.updateImage(ImageOperation.Paste, null, null,
                maskView.getImage(), -1);
        initOperation();
    }

    @FXML
    @Override
    public void cancelAction() {
        initOperation();
    }

    @FXML
    protected void demo() {
        FxImageTools.blendDemoFx(imageController, demoButton, finalClip, bgImage,
                (int) imageController.scope.getRectangle().getSmallX(),
                (int) imageController.scope.getRectangle().getSmallY(),
                opacity, !clipTopCheck.isSelected(), ignoreTransparentCheck.isSelected());
    }

    public void pasteImageInSystemClipboard() {
        Image clip = ImageClipboardTools.fetchImageInClipboard(false);
        if (clip == null) {
            popError(Languages.message("NoImageInClipboard"));
            return;
        }
        setSourceClip(clip);
    }

    @Override
    public void resetOperationPane() {
        clipSource = null;
        currentClip = null;
        okButton.setDisable(true);
        tabPane.getSelectionModel().select(imagesPane);
    }

}
