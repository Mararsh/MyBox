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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlWindow;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.ImageManufacture.KeepRatioType;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-9-5
 * @License Apache License Version 2.0
 */
public class ImageManufactureScaleController extends ImageManufactureOperationController {

    protected ScaleType scaleType;
    protected double width, height;
    protected float scale = 1.0f;
    protected int keepRatioType;

    @FXML
    protected ToggleGroup scaleGroup, keepGroup, interpolationGroup, ditherGroup, antiAliasGroup, qualityGroup;
    @FXML
    protected VBox setBox, pixelBox, keepBox, ratioBox, hintsBox;
    @FXML
    protected ComboBox<String> scaleSelector;
    @FXML
    protected RadioButton dragRadio, scaleRadio, pixelsRadio,
            widthRadio, heightRadio, largerRadio, smallerRadio,
            interpolationNullRadio, interpolation9Radio, interpolation4Radio, interpolation1Radio,
            ditherNullRadio, ditherOnRadio, ditherOffRadio, antiNullRadio, antiOnRadio, antiOffRadio,
            qualityNullRadio, qualityOnRadio, qualityOffRadio;
    @FXML
    protected CheckBox keepRatioCheck;
    @FXML
    protected Button originalButton, calculatorButton;
    @FXML
    protected TextField widthInput, heightInput;
    @FXML
    protected Label commentsLabel;

    public enum ScaleType {
        Dragging, Scale, Pixels
    }

    @Override
    public void initPane() {
        try {
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

            originalButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    isSettingValues = true;
                    widthInput.setText((int) imageController.image.getWidth() + "");
                    heightInput.setText((int) imageController.image.getHeight() + "");
                    isSettingValues = false;
                }
            });

            calculatorButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        final PixelsCalculationController controller
                                = (PixelsCalculationController) FxmlWindow.openStage(myStage,
                                        CommonValues.PixelsCalculatorFxml,
                                        true, Modality.WINDOW_MODAL, null);

                        attributes = new ImageAttributes();
                        attributes.setRatioAdjustion(keepRatioType);
                        attributes.setKeepRatio(keepRatioType != KeepRatioType.None);
                        attributes.setSourceWidth((int) imageController.image.getWidth());
                        attributes.setSourceHeight((int) imageController.image.getHeight());
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
            });

            scaleSelector.getItems().addAll(Arrays.asList("0.5", "2.0", "0.8", "0.1", "1.5", "3.0", "10.0", "0.01", "5.0", "0.3"));
            scaleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkScale();
                }
            });
            scaleSelector.getSelectionModel().select(0);

            okButton.disableProperty().bind(widthInput.styleProperty().isEqualTo(badStyle)
                    .or(heightInput.styleProperty().isEqualTo(badStyle))
                    .or(scaleSelector.getEditor().styleProperty().isEqualTo(badStyle))
            );

            checkScaleType();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    protected void paneExpanded() {
        checkScaleType();
    }

    protected void checkScaleType() {
        try {
            imageController.resetImagePane();
            imageController.hideScopePane();
            imageController.showImagePane();
            setBox.getChildren().clear();
            widthInput.setStyle(null);
            heightInput.setStyle(null);
            scaleSelector.getEditor().setStyle(null);
            commentsLabel.setText("");
            if (scaleGroup.getSelectedToggle() == null) {
                return;
            }

            if (dragRadio.isSelected()) {
                scaleType = ScaleType.Dragging;
                setBox.getChildren().addAll(keepBox, hintsBox, okButton);
                commentsLabel.setText(message("DragSizeComments"));
                initDrag();
                checkKeepType();
                checkRatio();

            } else if (pixelsRadio.isSelected()) {
                scaleType = ScaleType.Pixels;
                setBox.getChildren().addAll(keepBox, pixelBox, hintsBox, okButton);
                checkKeepType();
                checkPixelsWidth();
                checkPixelsHeight();

            } else if (scaleRadio.isSelected()) {
                scaleType = ScaleType.Scale;
                setBox.getChildren().addAll(scaleSelector, hintsBox, okButton);
                checkScale();
            }

            FxmlControl.refreshStyle(setBox);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void initDrag() {
        try {
            width = imageView.getImage().getWidth();
            height = imageView.getImage().getHeight();
            imageController.setMaskRectangleLineVisible(true);
            imageController.maskRectangleData = new DoubleRectangle(0, 0, width, height);
            imageController.drawMaskRectangleLineAsData();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void checkKeepType() {
        try {
            widthInput.setDisable(false);
            heightInput.setDisable(false);
            widthInput.setStyle(null);
            heightInput.setStyle(null);

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
            double v = Double.valueOf(widthInput.getText());
            if (width > 0) {
                width = v;
                widthInput.setStyle(null);
                checkRatio();
            } else {
                widthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            widthInput.setStyle(badStyle);
        }
    }

    protected void checkPixelsHeight() {
        if (scaleType != ScaleType.Pixels || isSettingValues) {
            return;
        }
        try {
            double v = Double.valueOf(heightInput.getText());
            if (height > 0) {
                height = v;
                heightInput.setStyle(null);
                checkRatio();
            } else {
                heightInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            heightInput.setStyle(badStyle);
        }
    }

    protected void checkRatio() {
        if (isSettingValues || scaleType != ScaleType.Pixels) {
            return;
        }
        try {
            if (keepRatioType != KeepRatioType.None) {
                int[] wh = ImageManufacture.scaleValues(
                        (int) imageView.getImage().getWidth(),
                        (int) imageView.getImage().getHeight(),
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

    protected void labelSize() {
        String info = message("OriginalSize") + ": " + (int) Math.round(imageController.image.getWidth())
                + "x" + (int) Math.round(imageController.image.getHeight()) + "\n"
                + message("CurrentSize") + ": " + (int) Math.round(imageView.getImage().getWidth())
                + "x" + (int) Math.round(imageView.getImage().getHeight()) + "\n"
                + message("AfterChange") + ": " + (int) Math.round(width)
                + "x" + (int) Math.round(height) + "\n";
        commentsLabel.setText(info);
    }

    protected void checkScale() {
        try {
            scale = Float.valueOf(scaleSelector.getValue());
            if (scale >= 0) {
                width = Math.round(imageView.getImage().getWidth() * scale);
                height = Math.round(imageView.getImage().getHeight() * scale);
                widthInput.setText(width + "");
                heightInput.setText(height + "");
                FxmlControl.setEditorNormal(scaleSelector);

                labelSize();
            } else {
                FxmlControl.setEditorBadStyle(scaleSelector);
            }
        } catch (Exception e) {
            scale = 0;
            FxmlControl.setEditorBadStyle(scaleSelector);
        }
    }

    @Override
    public void imageClicked(MouseEvent event, DoublePoint p) {
        if (scaleType != ScaleType.Dragging || imageController.maskRectangleData == null) {
            return;
        }
        if (keepRatioType == KeepRatioType.None) {
            width = imageController.maskRectangleData.getWidth();
            height = imageController.maskRectangleData.getHeight();
        } else {
            int[] wh = ImageManufacture.scaleValues(
                    (int) imageView.getImage().getWidth(),
                    (int) imageView.getImage().getHeight(),
                    (int) imageController.maskRectangleData.getWidth(),
                    (int) imageController.maskRectangleData.getHeight(),
                    keepRatioType);
            width = wh[0];
            height = wh[1];

            imageController.maskRectangleData = new DoubleRectangle(
                    imageController.maskRectangleData.getSmallX(),
                    imageController.maskRectangleData.getSmallY(),
                    imageController.maskRectangleData.getSmallX() + width - 1,
                    imageController.maskRectangleData.getSmallY() + height - 1);
            imageController.drawMaskRectangleLineAsData();
        }
        labelSize();
    }

    @FXML
    public void originalSize() {
        isSettingValues = true;
        widthInput.setText((int) Math.round(imageController.image.getWidth()) + "");
        heightInput.setText((int) Math.round(imageController.image.getHeight()) + "");
        isSettingValues = false;
        checkRatio();
    }

    @FXML
    @Override
    public void okAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image newImage;

                @Override
                protected boolean handle() {
                    int interpolation = -1, dither = -1, anti = -1, quality = -1;
                    if (interpolation9Radio.isSelected()) {
                        interpolation = 9;
                    } else if (interpolation4Radio.isSelected()) {
                        interpolation = 4;
                    } else if (interpolation1Radio.isSelected()) {
                        interpolation = 1;
                    }
                    if (ditherOnRadio.isSelected()) {
                        dither = 1;
                    } else if (ditherOffRadio.isSelected()) {
                        dither = 0;
                    }
                    if (antiOnRadio.isSelected()) {
                        anti = 1;
                    } else if (antiOffRadio.isSelected()) {
                        anti = 0;
                    }
                    if (qualityOnRadio.isSelected()) {
                        quality = 1;
                    } else if (qualityOffRadio.isSelected()) {
                        quality = 0;
                    }
                    switch (scaleType) {
                        case Scale:
                            if (scale <= 0) {
                                return false;
                            }
                            newImage = FxmlImageManufacture.scaleImage(imageView.getImage(), scale,
                                    dither, anti, quality, interpolation);
                            break;
                        case Dragging:
                        case Pixels:
                            if (width <= 0 || height <= 0) {
                                return false;
                            }
                            newImage = FxmlImageManufacture.scaleImage(imageView.getImage(), (int) width, (int) height,
                                    dither, anti, quality, interpolation);
                            break;
                        default:
                            return false;
                    }
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    imageController.popSuccessful();
                    String newSize = (int) Math.round(newImage.getWidth()) + "x" + (int) Math.round(newImage.getHeight());
                    if (scaleType == ScaleType.Scale) {
                        imageController.updateImage(ImageOperation.Scale2, scale + "", newSize, newImage, cost);
                    } else if (scaleType == ScaleType.Dragging || scaleType == ScaleType.Pixels) {
                        imageController.updateImage(ImageOperation.Scale2, "Pixels", newSize, newImage, cost);
                    }

                    String info = message("OriginalSize") + ": " + (int) Math.round(imageController.image.getWidth())
                            + "x" + (int) Math.round(imageController.image.getHeight()) + "\n"
                            + message("CurrentSize") + ": " + Math.round(newImage.getWidth())
                            + "x" + Math.round(newImage.getHeight());
                    commentsLabel.setText(info);

                    if (scaleType == ScaleType.Dragging) {
                        initDrag();
                    }
                }
            };
            imageController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }

    }

    @Override
    protected void resetOperationPane() {
        checkScaleType();
    }

}
