package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fximage.MarginTools;
import mara.mybox.fximage.ScaleTools;
import mara.mybox.fximage.TransformTools;
import mara.mybox.fxml.ImageClipboardTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-10-28
 * @License Apache License Version 2.0
 */
public class ImagePasteController extends BaseShapeController {

    protected ImageEditorController editor;
    protected Image clipSource, currentClip, blendedImage, finalClip, bgImage;
    protected DoubleRectangle rectangle;
    protected int keepRatioType;

    @FXML
    protected Tab imagesPane, setPane;
    @FXML
    protected ComboBox<String> angleSelector, ratioSelector;
    @FXML
    protected Button rotateLeftButton, rotateRightButton;
    @FXML
    protected CheckBox keepRatioCheck, enlargeCheck;
    @FXML
    protected Label listLabel;
    @FXML
    protected ControlImagesBlend blendController;
    @FXML
    protected CheckBox closeAfterCheck;

    public ImagePasteController() {
        baseTitle = message("SelectScope");
        TipsLabelKey = "ImageScopeTips";
    }

    @Override
    public void setStageStatus() {
        setAsPop(baseName);
    }

    protected void setParameters(ImageEditorController parent) {
        try {
            if (parent == null) {
                close();
            }
            editor = parent;
            loadImage(editor.sourceFile,
                    editor.imageInformation,
                    editor.image,
                    false);

            rotateAngle = currentAngle = 0;

            enlargeCheck.setSelected(UserConfig.getBoolean(baseName + "EnlargerImageAsClip", true));
            enlargeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "EnlargerImageAsClip", enlargeCheck.isSelected());
                    pasteClip(currentAngle);
                }
            });

            blendController.setParameters(this, imageView);
            blendController.optionChangedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    if (editor != null) {
                        pasteClip(currentAngle);
                    }
                }
            });

            keepRatioCheck.setSelected(UserConfig.getBoolean(baseName + "KeepClipRatio", true));
            keepRatioCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "KeepClipRatio", keepRatioCheck.isSelected());
                    pasteClip(currentAngle);
                }
            });

            keepRatioType = BufferedImageTools.KeepRatioType.BaseOnWidth;
            ratioSelector.getItems().addAll(Arrays.asList(Languages.message("BaseOnWidth"), Languages.message("BaseOnHeight"),
                    Languages.message("BaseOnLarger"), Languages.message("BaseOnSmaller")));
            ratioSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        if (Languages.message("BaseOnWidth").equals(newValue)) {
                            keepRatioType = BufferedImageTools.KeepRatioType.BaseOnWidth;
                        } else if (Languages.message("BaseOnHeight").equals(newValue)) {
                            keepRatioType = BufferedImageTools.KeepRatioType.BaseOnHeight;
                        } else if (Languages.message("BaseOnLarger").equals(newValue)) {
                            keepRatioType = BufferedImageTools.KeepRatioType.BaseOnLarger;
                        } else if (Languages.message("BaseOnSmaller").equals(newValue)) {
                            keepRatioType = BufferedImageTools.KeepRatioType.BaseOnSmaller;
                        } else {
                            keepRatioType = BufferedImageTools.KeepRatioType.None;
                        }
                    }
                }
            });
            ratioSelector.getSelectionModel().select(0);

            angleSelector.getItems().addAll(Arrays.asList("0", "90", "180", "45", "30", "60", "15", "5", "10", "1", "75", "120", "135"));
            angleSelector.setVisibleRowCount(10);
            angleSelector.setValue("0");
            angleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        rotateAngle = Integer.parseInt(newValue);
                        rotateLeftButton.setDisable(false);
                        rotateRightButton.setDisable(false);
                        ValidationTools.setEditorNormal(angleSelector);
                    } catch (Exception e) {
                        rotateLeftButton.setDisable(true);
                        rotateRightButton.setDisable(true);
                        ValidationTools.setEditorBadStyle(angleSelector);
                    }
                }
            });

            closeAfterCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(interfaceName + "SaveClose", closeAfterCheck.isSelected());
                }
            });
            closeAfterCheck.setSelected(UserConfig.getBoolean(interfaceName + "SaveClose", false));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setSourceClip(Image image) {
        try {
            if (image == null) {
                return;
            }
            clipSource = image;
            currentClip = clipSource;
            scope = new ImageScope();
            maskRectangleData = DoubleRectangle.image(currentClip);
            scope.setRectangle(maskRectangleData.copy());
            pasteClip(0);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void pasteClip(int angle) {
        if (clipSource == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

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
                            (int) maskRectangleData.getWidth(),
                            (int) maskRectangleData.getHeight(),
                            keepRatioCheck.isSelected(), keepRatioType);
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    bgImage = editor.imageView.getImage();
                    if (enlargeCheck.isSelected()) {
                        bgImage = MarginTools.addMarginsFx(bgImage,
                                Color.TRANSPARENT,
                                0 - (int) maskRectangleData.getY(),
                                (int) (maskRectangleData.getMaxY() - bgImage.getHeight()),
                                0 - (int) maskRectangleData.getX(),
                                (int) (maskRectangleData.getMaxX() - bgImage.getWidth()));
                        if (maskRectangleData.getX() < 0) {
                            maskRectangleData.setX(0);
                        }
                        if (maskRectangleData.getY() < 0) {
                            maskRectangleData.setY(0);
                        }
                    }
                    blendedImage = blendController.blend(finalClip, bgImage,
                            (int) maskRectangleData.getX(),
                            (int) maskRectangleData.getY());
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return blendedImage != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                currentAngle = angle;
                maskRectangleData = DoubleRectangle.xywh(
                        maskRectangleData.getX(), maskRectangleData.getY(),
                        finalClip.getWidth(), finalClip.getHeight());
                showMaskRectangle();
                updateImage(blendedImage);
                scope.setRectangle(maskRectangleData.copy());
            }
        };
        start(task);
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
    public void paneClicked(MouseEvent event, DoublePoint p) {
        if (scope == null || maskRectangleData == null) {
            return;
        }
        if (!scope.getRectangle().same(maskRectangleData)) {
            scope.setRectangle(maskRectangleData.copy());
            pasteClip(0);
        }
    }

    @Override
    public void sourceFileChanged(File file) {
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image clip;

            @Override
            protected boolean handle() {
                clip = FxImageTools.readImage(file);
                return clip != null;
            }

            @Override
            protected void whenSucceeded() {
                setSourceClip(clip);
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void pasteContentInSystemClipboard() {
        Image clip = ImageClipboardTools.fetchImageInClipboard(false);
        if (clip == null) {
            popError(Languages.message("NoImageInClipboard"));
            return;
        }
        setSourceClip(clip);
    }

    @FXML
    @Override
    public void selectAction() {
        ImageClipSelectController.open(this);
    }

    @FXML
    @Override
    public void okAction() {
        popSuccessful();
        editor.updateImage("Paste", null, scope, imageView.getImage(), -1);
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    @Override
    public boolean keyESC() {
        close();
        return false;
    }


    /*
        static methods
     */
    public static ImagePasteController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImagePasteController controller = (ImagePasteController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImagePasteFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
