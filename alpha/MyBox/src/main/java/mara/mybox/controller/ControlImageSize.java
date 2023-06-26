package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.stage.WindowEvent;
import mara.mybox.bufferedimage.BufferedImageTools.KeepRatioType;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ScaleTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-5
 * @License Apache License Version 2.0
 */
public class ControlImageSize extends BaseController {

    protected ImageViewerController imageController;
    protected ImageAttributes attributes;
    protected Image image;
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
                    checkKeepType();
                }
            });

            keepRatioCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    checkKeepType();
                    checkRatio();
                }
            });

            widthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkPixelsWidth();
                }
            });
            heightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkPixelsHeight();
                }
            });

            scaleSelector.getItems().addAll(Arrays.asList("0.5", "2.0", "0.8", "0.1", "1.5", "3.0", "10.0", "0.01", "5.0", "0.3"));
            scaleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkScale();
                }
            });
            scaleSelector.getSelectionModel().select(0);

            buttonsBox.setVisible(false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected boolean isDrag() {
        return scaleType == ScaleType.Dragging;
    }

    protected boolean isKeepRatio() {
        return keepRatioType != KeepRatioType.None;
    }

    public void setParameters(ImageViewerController imageController) {
        this.imageController = imageController;
        buttonsBox.setVisible(imageController != null);
        imageController.loadNotify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                loadImage();
            }
        });

        imageController.rectDrawnNotify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                loadImage();
            }
        });
        checkScaleType();
    }

    protected void loadImage() {
        image = imageController.scopeImage();
        originalSize();
        checkScaleType();
    }

    protected void checkScaleType() {
        try {
            initScaleType();
            if (scaleGroup.getSelectedToggle() == null) {
                return;
            }
            makeScaleType();
            refreshStyle(setBox);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void initScaleType() {
        try {
            setBox.getChildren().clear();
            widthInput.setStyle(null);
            heightInput.setStyle(null);
            scaleSelector.getEditor().setStyle(null);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void makeScaleType() {
        try {
            if (pixelsRadio.isSelected()) {
                scaleType = ScaleType.Pixels;
                setBox.getChildren().addAll(keepBox, pixelBox);
                checkKeepType();
                checkPixelsWidth();
                checkPixelsHeight();

            } else if (scaleRadio.isSelected()) {
                scaleType = ScaleType.Scale;
                setBox.getChildren().addAll(scaleSelector);
                checkScale();
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void labelSize() {
        if (infoLabel == null || imageController == null) {
            return;
        }
        String info = Languages.message("OriginalSize") + ": " + (int) Math.round(imageController.image.getWidth())
                + "x" + (int) Math.round(imageController.image.getHeight()) + "\n"
                + Languages.message("CurrentSize") + ": " + (int) Math.round(image.getWidth())
                + "x" + (int) Math.round(image.getHeight()) + "\n"
                + Languages.message("AfterChange") + ": " + (int) Math.round(width)
                + "x" + (int) Math.round(height) + "\n";
        infoLabel.setText(info);
    }

    protected void checkKeepType() {
        try {
            widthInput.setDisable(false);
            heightInput.setDisable(false);
            widthInput.setStyle(null);
            heightInput.setStyle(null);
            scale = 1;

            if (!keepRatioCheck.isSelected()) {
                ratioBox.setDisable(true);
                widthInput.setDisable(false);
                heightInput.setDisable(false);
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
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkPixelsWidth() {
        if (scaleType != ScaleType.Pixels || isSettingValues) {
            return;
        }
        try {
            double v = Double.parseDouble(widthInput.getText());
            if (v > 0) {
                width = v;
                widthInput.setStyle(null);
                checkRatio();
            } else {
                widthInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            widthInput.setStyle(UserConfig.badStyle());
        }
    }

    protected void checkPixelsHeight() {
        if (scaleType != ScaleType.Pixels || isSettingValues) {
            return;
        }
        try {
            double v = Double.parseDouble(heightInput.getText());
            if (v > 0) {
                height = v;
                heightInput.setStyle(null);
                checkRatio();
            } else {
                heightInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            heightInput.setStyle(UserConfig.badStyle());
        }
    }

    protected void checkRatio() {
        if (isSettingValues || scaleType != ScaleType.Pixels || image == null) {
            return;
        }
        try {
            if (keepRatioType != KeepRatioType.None) {
                int[] wh = mara.mybox.bufferedimage.ScaleTools.scaleValues(
                        (int) image.getWidth(),
                        (int) image.getHeight(),
                        (int) width, (int) height, keepRatioType);
                width = wh[0];
                height = wh[1];
                isSettingValues = true;
                widthInput.setText((int) width + "");
                heightInput.setText((int) height + "");
                isSettingValues = false;
            }
            labelSize();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkScale() {
        try {
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
        if (image == null) {
            return;
        }
        isSettingValues = true;
        width = image.getWidth();
        height = image.getHeight();
        widthInput.setText((int) Math.round(width) + "");
        heightInput.setText((int) Math.round(height) + "");
        isSettingValues = false;
    }

    @FXML
    public void calculator(ActionEvent event) {
        try {
            if (image == null) {
                return;
            }
            PixelsCalculationController controller
                    = (PixelsCalculationController) openChildStage(Fxmls.PixelsCalculatorFxml, true);
            attributes = new ImageAttributes();
            attributes.setRatioAdjustion(keepRatioType);
            attributes.setKeepRatio(keepRatioType != KeepRatioType.None);
            attributes.setSourceWidth((int) image.getWidth());
            attributes.setSourceHeight((int) image.getHeight());
            attributes.setTargetWidth((int) width);
            attributes.setTargetHeight((int) height);
            controller.setSource(attributes, widthInput, heightInput);
            isSettingValues = true;
            controller.getMyStage().setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    isSettingValues = false;
                    if (!controller.leavingScene()) {
                        event.consume();
                    }
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void scale() {
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
                scale(newImage, cost);
            }
        };
        start(task);
    }

    public void scale(Image newImage, long cost) {
        imageController.loadImage(newImage);
    }

}
