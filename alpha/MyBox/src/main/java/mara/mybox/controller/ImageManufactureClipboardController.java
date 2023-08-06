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
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.BufferedImageTools.KeepRatioType;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.controller.ImageManufactureController_Image.ImageOperation;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.MarginTools;
import mara.mybox.fximage.ScaleTools;
import mara.mybox.fximage.TransformTools;
import mara.mybox.fxml.ImageClipboardTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageManufactureClipboardController extends ImageManufactureOperationController {

    protected Image clipSource, currentClip, blendedImage, finalClip, bgImage;
    protected DoubleRectangle rectangle;
    protected int keepRatioType, rotateAngle, currentAngle;

    @FXML
    protected ControlImagesClipboard clipsController;
    @FXML
    protected Tab imagesPane, setPane;
    @FXML
    protected ComboBox<String> angleBox, ratioBox;
    @FXML
    protected CheckBox keepRatioCheck, enlargeCheck;
    @FXML
    protected Button rotateLeftButton, rotateRightButton;
    @FXML
    protected Label listLabel;
    @FXML
    protected ControlImagesBlend blendController;

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
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initPane() {
        try {
            super.initPane();

            rotateAngle = currentAngle = 0;
            clipsController.setParameters(editor, true);

            enlargeCheck.setSelected(UserConfig.getBoolean(baseName + "EnlargerImageAsClip", true));
            enlargeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "EnlargerImageAsClip", enlargeCheck.isSelected());
                    if (editor != null) {
                        pasteClip(currentAngle);
                    }
                }
            });

            blendController.setParameters(this);
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
                    if (editor != null) {
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

            angleBox.getItems().addAll(Arrays.asList("0", "90", "180", "45", "30", "60", "15", "5", "10", "1", "75", "120", "135"));
            angleBox.setVisibleRowCount(10);
            angleBox.setValue("0");
            angleBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        rotateAngle = Integer.parseInt(newValue);
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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void paneExpanded() {
        editor.showRightPane();
        editor.resetImagePane();
        editor.imageTab();
    }

    public void selectClip() {
        initOperation();
        if (clipsController.tableData.isEmpty()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image clipImage;

            @Override
            protected boolean handle() {
                try {
                    ImageClipboard clip = clipsController.selectedItem();
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
        start(task);
    }

    public void setSourceClip(Image image) {
        try {
            if (image == null) {
                return;
            }
            clipSource = image;
            currentClip = clipSource;
            editor.scope = new ImageScope();
            editor.maskRectangleData = DoubleRectangle.image(currentClip);
            editor.scope.setRectangle(editor.maskRectangleData.cloneValues());
            pasteClip(0);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void pasteClip(int angle) {
        if (clipSource == null) {
            return;
        }
        editor.showRightPane();
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

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
                            (int) editor.scope.getRectangle().getWidth(),
                            (int) editor.scope.getRectangle().getHeight(),
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
                    blendedImage = blendController.blend(finalClip, bgImage,
                            (int) editor.scope.getRectangle().getX(),
                            (int) editor.scope.getRectangle().getY());
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
                if (enlarged) {
                    editor.setImage(ImageOperation.Margins, bgImage);
                }
                editor.maskRectangleData = DoubleRectangle.xywh(
                        editor.maskRectangleData.getX(), editor.maskRectangleData.getY(),
                        finalClip.getWidth(), finalClip.getHeight());
                editor.showMaskRectangle();
                editor.scope.setRectangle(editor.maskRectangleData.cloneValues());
                maskView.setImage(blendedImage);
                maskView.setOpacity(1.0);
                maskView.setVisible(true);
                imageView.setVisible(false);
                imageView.toBack();
                tabPane.getSelectionModel().select(setPane);
                okButton.setDisable(false);
                okButton.requestFocus();
                editor.adjustRightPane();
                editor.operation = ImageOperation.Paste;
            }
        };
        start(task);
    }

    @FXML
    public void rotateRight() {
        pasteClip(rotateAngle);
    }

    @FXML
    public void rotateLeft() {
        pasteClip(360 - rotateAngle);

    }

    @Override
    public void paneClicked(MouseEvent event, DoublePoint p) {
        if (editor.scope == null || editor.maskRectangleData == null) {
            return;
        }
        if (!editor.scope.getRectangle().same(editor.maskRectangleData)) {
            editor.scope.setRectangle(editor.maskRectangleData.cloneValues());
            pasteClip(0);
        }
    }

    @FXML
    @Override
    public void okAction() {
        editor.popSuccessful();
        editor.updateImage(ImageOperation.Paste, null, null,
                maskView.getImage(), -1);
        initOperation();
    }

    @FXML
    @Override
    public void cancelAction() {
        initOperation();
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
