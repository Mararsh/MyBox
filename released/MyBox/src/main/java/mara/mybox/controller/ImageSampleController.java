package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import mara.mybox.bufferedimage.CropTools;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.imagefile.ImageFileReaders;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-12-03
 * @License Apache License Version 2.0
 */
public class ImageSampleController extends BaseShapeController {

    private int widthScale, heightScale;
    private double x1, y1, x2, y2;

    @FXML
    protected Label infoLabel;
    @FXML
    protected TextField rectLeftTopXInput, rectLeftTopYInput, rightBottomXInput, rightBottomYInput;
    @FXML
    protected ComboBox<String> widthScaleSelector, heightScaleSelector;

    public ImageSampleController() {
        baseTitle = message("ImageSample");
        TipsLabelKey = "ImageSampleTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            List<String> values = Arrays.asList("1", "2", "3", "4", "5", "6", "8", "9", "10", "15", "20",
                    "25", "30", "50", "80", "100", "200", "500", "800", "1000");
            widthScaleSelector.getItems().addAll(values);
            widthScaleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkScales();
                }
            });
            widthScaleSelector.getSelectionModel().select("1");

            heightScaleSelector.getItems().addAll(values);
            heightScaleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkScales();
                }
            });
            heightScaleSelector.getSelectionModel().select("1");

            rectLeftTopXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRegion();
                }
            });
            rectLeftTopYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRegion();
                }
            });
            rightBottomXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRegion();
                }
            });
            rightBottomYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRegion();
                }
            });

            clearMask();

            goButton.disableProperty().bind(
                    Bindings.isEmpty(rectLeftTopXInput.textProperty())
                            .or(rectLeftTopXInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(Bindings.isEmpty(rectLeftTopYInput.textProperty()))
                            .or(rectLeftTopYInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(Bindings.isEmpty(rightBottomXInput.textProperty()))
                            .or(rightBottomXInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(Bindings.isEmpty(rightBottomYInput.textProperty()))
                            .or(rightBottomYInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );
            saveButton.disableProperty().bind(
                    goButton.disabledProperty()
                            .or(widthScaleSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(heightScaleSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(Bindings.isEmpty(heightScaleSelector.getEditor().textProperty()))
                            .or(Bindings.isEmpty(widthScaleSelector.getEditor().textProperty()))
            );

            rightPane.disableProperty().bind(imageView.imageProperty().isNull());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean checkScales() {
        if (isSettingValues) {
            return true;
        }
        int v;
        try {
            v = Integer.parseInt(widthScaleSelector.getValue());
        } catch (Exception e) {
            v = -1;
        }
        if (v >= 1) {
            widthScale = v;
            ValidationTools.setEditorNormal(widthScaleSelector);
        } else {
            ValidationTools.setEditorBadStyle(widthScaleSelector);
            popError(message("InvalidParameter") + ": " + message("SampleScale"));
            return false;
        }
        try {
            v = Integer.parseInt(heightScaleSelector.getValue());
        } catch (Exception e) {
            v = -1;
        }
        if (v >= 1) {
            heightScale = v;
            ValidationTools.setEditorNormal(heightScaleSelector);
        } else {
            ValidationTools.setEditorBadStyle(heightScaleSelector);
            popError(message("InvalidParameter") + ": " + message("SampleScale"));
            return false;
        }
        updateLabel();
        return true;
    }

    public DoubleRectangle checkRegion() {
        if (isSettingValues) {
            return null;
        }
        try {
            x1 = Double.parseDouble(rectLeftTopXInput.getText());
            rectLeftTopXInput.setStyle(null);
        } catch (Exception e) {
            rectLeftTopXInput.setStyle(UserConfig.badStyle());
            popError(message("InvalidParameter") + ": " + message("LeftTop"));
            return null;
        }
        try {
            y1 = Double.parseDouble(rectLeftTopYInput.getText());
            rectLeftTopYInput.setStyle(null);
        } catch (Exception e) {
            rectLeftTopYInput.setStyle(UserConfig.badStyle());
            popError(message("InvalidParameter") + ": " + message("LeftTop"));
            return null;
        }
        try {
            x2 = Double.parseDouble(rightBottomXInput.getText());
            rightBottomXInput.setStyle(null);
        } catch (Exception e) {
            rightBottomXInput.setStyle(UserConfig.badStyle());
            popError(message("InvalidParameter") + ": " + message("RightBottom"));
            return null;
        }
        try {
            y2 = Double.parseDouble(rightBottomYInput.getText());
            rightBottomYInput.setStyle(null);
        } catch (Exception e) {
            rightBottomYInput.setStyle(UserConfig.badStyle());
            popError(message("InvalidParameter") + ": " + message("RightBottom"));
            return null;
        }
        DoubleRectangle rect = DoubleRectangle.xy12(
                x1 * widthRatio(), y1 * heightRatio(),
                x2 * widthRatio(), y2 * heightRatio());
        if (!rect.isValid()) {
            popError(message("InvalidData"));
            return null;
        }
        return rect;
    }

    private void updateLabel() {
        if (imageView.getImage() == null) {
            return;
        }
        if (widthScale < 1 || heightScale < 1) {
            infoLabel.setText(message("InvalidParameters"));
            return;
        }
        infoLabel.setText(message("ImageSize") + ": "
                + operationWidth() + "x" + operationHeight()
                + "\n" + message("SamplingSize") + ": "
                + (int) (maskRectangleData.getWidth() / (widthRatio() * widthScale))
                + "x" + (int) (maskRectangleData.getHeight() / (heightRatio() * heightScale)));

    }

    @Override
    public boolean drawMaskRectangle() {
        if (!super.drawMaskRectangle()) {
            return false;
        }
        isSettingValues = true;
        rectLeftTopXInput.setText(scale(maskRectangleData.getX() / widthRatio(), 2) + "");
        rectLeftTopYInput.setText(scale(maskRectangleData.getY() / heightRatio(), 2) + "");
        rightBottomXInput.setText(scale(maskRectangleData.getMaxX() / widthRatio(), 2) + "");
        rightBottomYInput.setText(scale(maskRectangleData.getMaxY() / heightRatio(), 2) + "");
        isSettingValues = false;
        updateLabel();
        return true;
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded() || imageView.getImage() == null) {
                return false;
            }
            showMaskRectangle();
            checkScales();
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }

    }

    @Override
    public void selectAllAction() {
        if (imageView.getImage() == null) {
            return;
        }
        maskRectangleData = DoubleRectangle.xywh(0, 0,
                imageView.getImage().getWidth(), imageView.getImage().getHeight());
        maskShapeDataChanged();
    }

    @FXML
    @Override
    public void goAction() {
        try {
            DoubleRectangle rect = checkRegion();
            if (rect == null) {
                return;
            }
            maskRectangleData = rect;
            isSettingValues = true;
            drawMaskRectangle();
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public BufferedImage imageToSaveAs(FxTask currentTask) {
        if (sourceFile != null && imageInformation != null) {
            ImageInformation info = new ImageInformation(sourceFile);
            info.setIndex(imageInformation.getIndex())
                    .setRegion(x1, y1, x2, y2)
                    .setXscale(widthScale).setYscale(heightScale)
                    .setImageFormat(imageInformation.getImageFormat());
            return ImageFileReaders.readFrame(currentTask, info);
        } else if (image != null) {
            return CropTools.sample(currentTask, SwingFXUtils.fromFXImage(image, null),
                    (int) x1, (int) y1, (int) x2, (int) y2, widthScale, heightScale);
        } else {
            return null;
        }
    }

    @FXML
    @Override
    public void saveAction() {
        saveAsAction();
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (image == null
                || !checkScales() || widthScale < 1 || heightScale < 1
                || checkRegion() == null) {
            return;
        }
        super.saveAsAction();
    }

}
