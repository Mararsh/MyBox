package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.bufferedimage.BufferedImageTools.KeepRatioType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ScaleTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-5
 * @License Apache License Version 2.0
 */
public class ControlImageSize extends BaseController {

    protected ImageViewerController imageController;
    protected ScaleType scaleType;
    protected double width, height;
    protected float scale = 1.0f;
    protected int keepRatioType;
    protected Label infoLabel;

    @FXML
    protected ToggleGroup scaleGroup, keepGroup, interpolationGroup, ditherGroup, antiAliasGroup, qualityGroup;
    @FXML
    protected VBox typeBox, setBox, pixelBox, keepBox, ratioBox;
    @FXML
    protected ComboBox<String> scaleSelector;
    @FXML
    protected RadioButton scaleRadio, pixelsRadio,
            widthRadio, heightRadio, largerRadio, smallerRadio;
    @FXML
    protected CheckBox keepRatioCheck;
    @FXML
    protected TextField widthInput, heightInput;
    @FXML
    protected HBox buttonsBox;

    public enum ScaleType {
        Dragging, Scale, Pixels
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

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

            checkScaleType();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void setParameters(ImageViewerController imageController) {
        this.imageController = imageController;

        imageController.loadNotify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                imageLoaded();
            }
        });

    }

    protected void imageLoaded() {
        if (isSettingValues) {
            return;
        }
        originalSize();
    }

    protected Image getImage() {
        if (imageController == null) {
            return null;
        } else {
            return imageController.scopeImage();
        }
    }

    protected void checkScaleType() {
        try {
            resetControls();
            switchType();
            refreshStyle(setBox);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void resetControls() {
        try {
            buttonsBox.setVisible(imageController != null);
            setBox.getChildren().clear();
            widthInput.setStyle(null);
            heightInput.setStyle(null);
            scaleSelector.getEditor().setStyle(null);
            if (imageController != null) {
                imageController.clearMask();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void switchType() {
        try {
            if (pixelsRadio.isSelected()) {
                scaleType = ScaleType.Pixels;
                setBox.getChildren().addAll(keepBox, pixelBox);
                pickSize();

            } else if (scaleRadio.isSelected()) {
                scaleType = ScaleType.Scale;
                setBox.getChildren().addAll(scaleSelector);
                pickScale();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected boolean pickSize() {
        if (scaleType != ScaleType.Pixels || isSettingValues) {
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

    protected void adjustRadio() {
        try {
            if (isSettingValues || scaleType == ScaleType.Scale) {
                return;
            }
            Image image = getImage();
            if (image == null) {
                return;
            }
            scale = 1;
            widthInput.setDisable(false);
            heightInput.setDisable(false);

            if (!keepRatioCheck.isSelected()) {
                ratioBox.setDisable(true);
                keepRatioType = KeepRatioType.None;

            } else {
                ratioBox.setDisable(false);

                if (widthRadio.isSelected()) {
                    keepRatioType = KeepRatioType.BaseOnWidth;
                    heightInput.setDisable(true);

                } else if (heightRadio.isSelected()) {
                    keepRatioType = KeepRatioType.BaseOnHeight;
                    widthInput.setDisable(true);

                } else if (largerRadio.isSelected()) {
                    keepRatioType = KeepRatioType.BaseOnLarger;

                } else if (smallerRadio.isSelected()) {
                    keepRatioType = KeepRatioType.BaseOnSmaller;

                } else {
                    keepRatioType = KeepRatioType.None;
                }
            }

            if (keepRatioType != KeepRatioType.None) {
                int[] wh = mara.mybox.bufferedimage.ScaleTools.scaleValues(
                        (int) image.getWidth(),
                        (int) image.getHeight(),
                        (int) width, (int) height, keepRatioType);
                width = wh[0];
                height = wh[1];
                widthInput.setStyle(null);
                heightInput.setStyle(null);
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

    protected void pickScale() {
        try {
            if (scaleType != ScaleType.Scale) {
                return;
            }
            Image image = getImage();
            if (image == null) {
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

    @FXML
    public void originalSize() {
        Image image = getImage();
        if (image == null) {
            return;
        }
        isSettingValues = true;
        widthInput.setText((int) Math.round(image.getWidth()) + "");
        heightInput.setText((int) Math.round(image.getHeight()) + "");
        isSettingValues = false;
        checkScaleType();
    }

    @FXML
    public void calculator(ActionEvent event) {
        try {
            Image image = getImage();
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
        if (infoLabel == null || imageController == null) {
            return;
        }
        Image image = getImage();
        if (image == null) {
            return;
        }
        String info = message("OriginalSize") + ": " + (int) Math.round(imageController.image.getWidth())
                + "x" + (int) Math.round(imageController.image.getHeight()) + "\n"
                + message("CurrentSize") + ": " + (int) Math.round(image.getWidth())
                + "x" + (int) Math.round(image.getHeight()) + "\n"
                + message("AfterChange") + ": " + (int) Math.round(width)
                + "x" + (int) Math.round(height) + "\n";
        infoLabel.setText(info);
    }

    public void scale() {
        Image image = getImage();
        if (image == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image newImage;

            @Override
            protected boolean handle() {
                switch (scaleType) {
                    case Scale:
                        if (scale <= 0) {
                            return false;
                        }
                        newImage = ScaleTools.scaleImage(image, scale);
                        break;
                    case Dragging:
                    case Pixels:
                        if (width <= 0 || height <= 0) {
                            return false;
                        }
                        newImage = ScaleTools.scaleImage(image,
                                (int) width, (int) height);
                        break;
                    default:
                        return false;
                }
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                afterScaled(newImage, cost);
            }
        };
        start(task);
    }

    public void afterScaled(Image newImage, long cost) {
        imageController.loadImage(newImage);
    }

}
