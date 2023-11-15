package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ScaleTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-5
 * @License Apache License Version 2.0
 */
public class ImageSizeController extends BaseImageEditController {

    protected double width, height;
    protected float scale = 1.0f;
    protected int keepRatioType;

    @FXML
    protected ToggleGroup scaleGroup, keepGroup;
    @FXML
    protected VBox typeBox, setBox, pixelBox, keepBox, ratioBox;
    @FXML
    protected ComboBox<String> scaleSelector;
    @FXML
    protected RadioButton scaleRadio, pixelsRadio, dragRadio,
            widthRadio, heightRadio, largerRadio, smallerRadio;
    @FXML
    protected CheckBox keepRatioCheck;
    @FXML
    protected TextField widthInput, heightInput;
    @FXML
    protected HBox buttonsBox;
    @FXML
    protected Button selectAllRectButton;
    @FXML
    protected Label commentsLabel;

    public ImageSizeController() {
        baseTitle = message("Scale");
    }

    @Override
    protected void initMore() {
        try {
            operation = "Scale";

            scaleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkScaleType();
                }
            });

            keepGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    adjustRadio();
                }
            });

            keepRatioCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    adjustRadio();
                }
            });

            widthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    pickSize();
                }
            });
            heightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    pickSize();
                }
            });

            scaleSelector.getItems().addAll(Arrays.asList("0.5", "2.0", "0.8", "0.1", "1.5", "3.0", "10.0", "0.01", "5.0", "0.3"));
            scaleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    pickScale();
                }
            });
            scaleSelector.getSelectionModel().select(0);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded() || image == null) {
                return false;
            }
            originalSize();

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @FXML
    public void originalSize() {
        isSettingValues = true;
        widthInput.setText((int) Math.round(image.getWidth()) + "");
        heightInput.setText((int) Math.round(image.getHeight()) + "");
        isSettingValues = false;
        checkScaleType();
        String info = message("CurrentSize") + ": " + Math.round(image.getWidth())
                + "x" + Math.round(image.getHeight());
        commentsLabel.setText(info);
    }

    protected void checkScaleType() {
        try {
            setBox.getChildren().clear();
            widthInput.setStyle(null);
            heightInput.setStyle(null);
            scaleSelector.getEditor().setStyle(null);
            commentsLabel.setText("");
            clearMask();

            if (dragRadio.isSelected()) {
                setBox.getChildren().addAll(keepBox);
                if (commentsLabel != null) {
                    commentsLabel.setText(message("DragSizeComments"));
                }
                width = image.getWidth();
                height = image.getHeight();
                maskRectangleData = DoubleRectangle.xywh(0, 0, width, height);
                popAnchorMenu = false;
                showAnchors = true;
                showMaskRectangle();

            } else if (pixelsRadio.isSelected()) {
                setBox.getChildren().addAll(keepBox, pixelBox);
                pickSize();

            } else if (scaleRadio.isSelected()) {
                setBox.getChildren().addAll(scaleSelector);
                pickScale();
            }

            refreshStyle(setBox);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void maskShapeDataChanged() {
        if (!dragRadio.isSelected() || maskRectangleData == null) {
            return;
        }
        super.maskShapeDataChanged();
        width = maskRectangleData.getWidth();
        height = maskRectangleData.getHeight();
        if (keepRatioType != BufferedImageTools.KeepRatioType.None) {
            adjustRadio();

        } else {
            labelSize();
        }
    }

    protected boolean pickSize() {
        if (!pixelsRadio.isSelected() || isSettingValues) {
            return false;
        }
        try {
            double v = Double.parseDouble(widthInput.getText());
            if (v > 0) {
                width = v;
                widthInput.setStyle(null);
            } else {
                widthInput.setStyle(UserConfig.badStyle());
                popError(message("InvalidParameter") + ": " + message("Width"));
                return false;
            }
        } catch (Exception e) {
            widthInput.setStyle(UserConfig.badStyle());
            popError(message("InvalidParameter") + ": " + message("Width"));
            return false;
        }
        try {
            double v = Double.parseDouble(heightInput.getText());
            if (v > 0) {
                height = v;
                heightInput.setStyle(null);
            } else {
                heightInput.setStyle(UserConfig.badStyle());
                popError(message("InvalidParameter") + ": " + message("Height"));
                return false;
            }
        } catch (Exception e) {
            heightInput.setStyle(UserConfig.badStyle());
            popError(message("InvalidParameter") + ": " + message("Height"));
            return false;
        }
        adjustRadio();
        return true;
    }

    protected void pickScale() {
        try {
            if (!scaleRadio.isSelected()) {
                return;
            }
            float f = Float.parseFloat(scaleSelector.getValue());
            if (f >= 0) {
                scale = f;
                width = Math.round(image.getWidth() * scale);
                height = Math.round(image.getHeight() * scale);
                widthInput.setText(width + "");
                heightInput.setText(height + "");
                ValidationTools.setEditorNormal(scaleSelector);

                labelSize();
            } else {
                ValidationTools.setEditorBadStyle(scaleSelector);
            }
        } catch (Exception e) {
            ValidationTools.setEditorBadStyle(scaleSelector);
        }
    }

    protected void adjustRadio() {
        try {
            if (isSettingValues || scaleRadio.isSelected() || image == null) {
                return;
            }

            scale = 1;
            widthInput.setDisable(false);
            heightInput.setDisable(false);

            if (!keepRatioCheck.isSelected()) {
                ratioBox.setDisable(true);
                keepRatioType = BufferedImageTools.KeepRatioType.None;

            } else {
                ratioBox.setDisable(false);

                if (widthRadio.isSelected()) {
                    keepRatioType = BufferedImageTools.KeepRatioType.BaseOnWidth;
                    heightInput.setDisable(true);

                } else if (heightRadio.isSelected()) {
                    keepRatioType = BufferedImageTools.KeepRatioType.BaseOnHeight;
                    widthInput.setDisable(true);

                } else if (largerRadio.isSelected()) {
                    keepRatioType = BufferedImageTools.KeepRatioType.BaseOnLarger;

                } else if (smallerRadio.isSelected()) {
                    keepRatioType = BufferedImageTools.KeepRatioType.BaseOnSmaller;

                } else {
                    keepRatioType = BufferedImageTools.KeepRatioType.None;
                }
            }

            if (keepRatioType != BufferedImageTools.KeepRatioType.None) {
                int[] wh = mara.mybox.bufferedimage.ScaleTools.scaleValues(
                        (int) image.getWidth(),
                        (int) image.getHeight(),
                        (int) width, (int) height, keepRatioType);
                width = wh[0];
                height = wh[1];
                widthInput.setStyle(null);
                heightInput.setStyle(null);
                if (dragRadio.isSelected()) {
                    maskRectangleData = DoubleRectangle.xywh(
                            maskRectangleData.getX(), maskRectangleData.getY(), width, height);
                    drawMaskRectangle();
                }
            }
            isSettingValues = true;
            widthInput.setText((int) width + "");
            heightInput.setText((int) height + "");
            isSettingValues = false;
            labelSize();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void calculator(ActionEvent event) {
        try {
            image = imageView.getImage();
            if (image == null) {
                return;
            }
            PixelsCalculationController controller
                    = (PixelsCalculationController) openChildStage(Fxmls.PixelsCalculatorFxml, true);
            controller.setSource((int) image.getWidth(), (int) image.getHeight(), keepRatioType);
            controller.notify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    isSettingValues = true;
                    widthInput.setText(controller.getFinalX() + "");
                    heightInput.setText(controller.getFinalY() + "");
                    isSettingValues = false;
                    controller.close();
                    pickSize();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void labelSize() {
        String info = message("CurrentSize") + ": " + (int) Math.round(image.getWidth())
                + "x" + (int) Math.round(image.getHeight()) + "  "
                + message("AfterChange") + ": " + (int) Math.round(width)
                + "x" + (int) Math.round(height) + "\n";
        commentsLabel.setText(info);
    }

    @FXML
    public void selectAllRect() {
        if (!dragRadio.isSelected() || imageView.getImage() == null) {
            return;
        }
        maskRectangleData = DoubleRectangle.xywh(0, 0,
                imageView.getImage().getWidth(), imageView.getImage().getHeight());
        drawMaskRectangle();
    }

    @Override
    protected void handleImage() {
        if (scaleRadio.isSelected()) {
            if (scale <= 0) {
                return;
            }
            opInfo = message("Times2") + " " + scale;
            handledImage = ScaleTools.scaleImage(image, scale);
        } else {
            if (width <= 0 || height <= 0) {
                return;
            }
            handledImage = ScaleTools.scaleImage(image,
                    (int) width, (int) height);
            opInfo = (int) Math.round(handledImage.getWidth()) + "x"
                    + (int) Math.round(handledImage.getHeight());
        }
    }

    /*
        static methods
     */
    public static ImageSizeController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageSizeController controller = (ImageSizeController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageSizeFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
