package mara.mybox.controller;

import java.text.MessageFormat;
import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.ImageManufacture.KeepRatioType;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-9-5
 * @License Apache License Version 2.0
 */
public class ImageManufactureScaleController extends ImageManufactureOperationController {

    protected SizeType sizeType;
    protected int width, height;
    protected boolean noRatio;
    protected float scale = 1.0f;
    protected int keepRatioType;

    @FXML
    protected ToggleGroup pixelsGroup;
    @FXML
    protected FlowPane setPane;
    @FXML
    protected ChoiceBox ratioBox;
    @FXML
    protected ComboBox scaleBox;
    @FXML
    protected Button originalButton, calculatorButton;
    @FXML
    protected TextField widthInput, heightInput;
    @FXML
    protected Label label1, label2;
    @FXML
    protected CheckBox keepRatioCheck;

    public enum SizeType {
        Dragging, Scale, Pixels
    }

    public ImageManufactureScaleController() {
        baseTitle = AppVariables.message("ImageManufactureSize");
        operation = ImageOperation.Scale;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            myPane = scalePane;

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public void initPane(ImageManufactureController parent) {
        try {
            super.initPane(parent);
            if (parent == null) {
                return;
            }

            widthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkPixelsWidth();
                }
            });
            heightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkPixelsHeight();
                }
            });

            ratioBox.getItems().addAll(Arrays.asList(message("BaseOnWidth"), message("BaseOnHeight"),
                    message("BaseOnLarger"), message("BaseOnSmaller")));
            ratioBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        checkRatioAdjustion(newValue);
                    }
                }
            });
            ratioBox.getSelectionModel().select(0);

            keepRatioCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    checkRatio();
                }
            });

            originalButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    noRatio = true;
                    widthInput.setText((int) imageController.image.getWidth() + "");
                    heightInput.setText((int) imageController.image.getHeight() + "");
                    noRatio = false;
                }
            });

            calculatorButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        final PixelsCalculationController controller
                                = (PixelsCalculationController) FxmlStage.openStage(myStage,
                                        CommonValues.PixelsCalculatorFxml,
                                        true, Modality.WINDOW_MODAL, null);

                        attributes = new ImageAttributes();
                        attributes.setRatioAdjustion(keepRatioType);
                        attributes.setKeepRatio(keepRatioCheck.isSelected());
                        attributes.setSourceWidth((int) imageController.image.getWidth());
                        attributes.setSourceHeight((int) imageController.image.getHeight());
                        attributes.setTargetWidth(width);
                        attributes.setTargetHeight(height);
                        controller.setSource(attributes, widthInput, heightInput);
                        noRatio = true;
                        controller.getMyStage().setOnCloseRequest(new EventHandler<WindowEvent>() {
                            @Override
                            public void handle(WindowEvent event) {
                                noRatio = false;
                                if (!controller.leavingScene()) {
                                    event.consume();
                                }
                            }
                        });

                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                }
            });

            scaleBox.getItems().addAll(Arrays.asList("0.5", "2.0", "0.8", "0.1", "1.5", "3.0", "10.0", "0.01", "5.0", "0.3"));
            scaleBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        scale = Float.valueOf(newValue);
                        if (scale >= 0) {
                            if (imageView.getImage() != null) {
                                noRatio = true;
                                widthInput.setText(Math.round(imageView.getImage().getWidth() * scale) + "");
                                heightInput.setText(Math.round(imageView.getImage().getHeight() * scale) + "");
                                noRatio = false;
                            }
                            FxmlControl.setEditorNormal(scaleBox);
                        } else {
                            FxmlControl.setEditorBadStyle(scaleBox);
                        }

                    } catch (Exception e) {
                        scale = 0;
                        FxmlControl.setEditorBadStyle(scaleBox);
                    }
                }
            });
            scaleBox.getSelectionModel().select(0);

            okButton.disableProperty().bind(
                    widthInput.styleProperty().isEqualTo(badStyle)
                            .or(heightInput.styleProperty().isEqualTo(badStyle))
                            .or(scaleBox.getEditor().styleProperty().isEqualTo(badStyle))
            );

            pixelsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkScaleType();
                }
            });
            checkScaleType();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void checkScaleType() {
        try {
            if (parent == null) {
                return;
            }
            setPane.getChildren().clear();
            widthInput.setStyle(null);
            heightInput.setStyle(null);
            scaleBox.getEditor().setStyle(null);
            parent.currentImageController.imageLabel.setText("");

            if (pixelsGroup.getSelectedToggle() == null) {
                imageController.clearOperating();
                return;
            }

            RadioButton selected = (RadioButton) pixelsGroup.getSelectedToggle();
            if (message("AdjustByDragging").equals(selected.getText())) {
                sizeType = SizeType.Dragging;
                setPane.getChildren().addAll(keepRatioCheck, ratioBox, okButton);
                keepRatioCheck.setSelected(true);
                initDrag(message("DragSizeComments"));

            } else {

                imageController.clearOperating();

                if (message("Pixels").equals(selected.getText())) {
                    sizeType = SizeType.Pixels;
                    setPane.getChildren().addAll(keepRatioCheck, ratioBox, label1, widthInput, label2, heightInput,
                            originalButton, calculatorButton, okButton);
                    keepRatioCheck.setSelected(true);

                    checkPixelsWidth();
                    checkPixelsHeight();

                } else if (message("ZoomScale").equals(selected.getText())) {
                    sizeType = SizeType.Scale;
                    scaleBox.setDisable(false);
                    setPane.getChildren().addAll(scaleBox, okButton);

                }
            }
            FxmlControl.refreshStyle(setPane);
        } catch (Exception e) {
            widthInput.setStyle(badStyle);
        }
    }

    private void initDrag(String info) {
        imageController.operating();
        imageController.setMaskRectangleLineVisible(true);
        imageController.maskRectangleData = new DoubleRectangle(0, 0,
                imageView.getImage().getWidth() - 1,
                imageView.getImage().getHeight() - 1);
        imageController.scope.setRectangle(imageController.maskRectangleData.cloneValues());
        imageController.drawMaskRectangleLineAsData();
        width = (int) imageView.getImage().getWidth();
        height = (int) imageView.getImage().getHeight();
        if (info != null) {
            imageController.imageLabel.setText(info);
        }

    }

    private void checkPixelsWidth() {
        try {
            width = Integer.valueOf(widthInput.getText());
            if (width > 0) {
                widthInput.setStyle(null);
                checkRatio();
            } else {
                widthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            widthInput.setStyle(badStyle);
        }
    }

    private void checkPixelsHeight() {
        try {
            height = Integer.valueOf(heightInput.getText());
            if (height > 0) {
                heightInput.setStyle(null);
                checkRatio();
            } else {
                heightInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            heightInput.setStyle(badStyle);
        }
    }

    protected void checkRatioAdjustion(String s) {
        try {
            widthInput.setDisable(false);
            heightInput.setDisable(false);
            if (message("BaseOnWidth").equals(s)) {
                keepRatioType = KeepRatioType.BaseOnWidth;
                heightInput.setDisable(true);
            } else if (message("BaseOnHeight").equals(s)) {
                keepRatioType = KeepRatioType.BaseOnHeight;
                widthInput.setDisable(true);
            } else if (message("BaseOnLarger").equals(s)) {
                keepRatioType = KeepRatioType.BaseOnLarger;
            } else if (message("BaseOnSmaller").equals(s)) {
                keepRatioType = KeepRatioType.BaseOnSmaller;
            } else {
                keepRatioType = KeepRatioType.None;
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkRatio() {
        if (sizeType != SizeType.Pixels) {
            return;
        }
        if (!keepRatioCheck.isSelected()) {
            ratioBox.setDisable(true);
            widthInput.setDisable(false);
            heightInput.setDisable(false);
        } else {
            ratioBox.setDisable(false);
            checkRatioAdjustion(ratioBox.getSelectionModel().getSelectedItem().toString());
        }

        if (noRatio || !keepRatioCheck.isSelected()) {
            return;
        }
        try {
            int[] wh = ImageManufacture.scale(
                    (int) imageView.getImage().getWidth(),
                    (int) imageView.getImage().getHeight(),
                    Integer.valueOf(widthInput.getText()),
                    Integer.valueOf(heightInput.getText()),
                    keepRatioType);
            width = wh[0];
            height = wh[1];

            widthInput.setText(width + "");
            heightInput.setText(height + "");

        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @Override
    public void paneClicked(MouseEvent event) {
        if (sizeType != SizeType.Dragging || imageController.scope == null) {
            return;
        }

        int[] wh = ImageManufacture.scale(
                (int) imageView.getImage().getWidth(),
                (int) imageView.getImage().getHeight(),
                (int) imageController.maskRectangleData.getWidth(),
                (int) imageController.maskRectangleData.getWidth(),
                keepRatioType);
        width = wh[0];
        height = wh[1];
        imageController.maskRectangleData = new DoubleRectangle(
                imageController.maskRectangleData.getSmallX(),
                imageController.maskRectangleData.getSmallY(),
                imageController.maskRectangleData.getSmallX() + width - 1,
                imageController.maskRectangleData.getSmallY() + height - 1);
        imageController.scope.setRectangle(imageController.maskRectangleData.cloneValues());
        imageController.drawMaskRectangleLineAsData();

        String info = MessageFormat.format(AppVariables.message("ImageSizeChanged"),
                (int) Math.round(imageController.image.getWidth()) + "x" + (int) Math.round(imageController.image.getHeight()),
                (int) Math.round(imageView.getImage().getWidth()) + "x" + (int) Math.round(imageView.getImage().getHeight()),
                width + "x" + height);
        imageController.imageLabel.setText(info);
    }

    protected void setPixels() {
        if (width <= 0 || height <= 0) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image newImage;

                @Override
                protected boolean handle() {
                    newImage = FxmlImageManufacture.scaleImage(imageController.image, width, height);
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    String info = MessageFormat.format(AppVariables.message("ImageSizeChanged"),
                            (int) Math.round(imageController.image.getWidth()) + "x" + (int) Math.round(imageController.image.getHeight()),
                            (int) Math.round(imageView.getImage().getWidth()) + "x" + (int) Math.round(imageView.getImage().getHeight()),
                            (int) Math.round(newImage.getWidth()) + "x" + (int) Math.round(newImage.getHeight()));
                    parent.updateImage(ImageOperation.Scale, "Pixels",
                            (int) Math.round(newImage.getWidth()) + "x" + (int) Math.round(newImage.getHeight()), newImage);
                    imageController.imageLabel.setText(info);
                    if (sizeType == SizeType.Dragging) {
                        initDrag(null);
                    }
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void setScale() {
        if (scale <= 0) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image newImage;

                @Override
                protected boolean handle() {
                    newImage = FxmlImageManufacture.scaleImage(imageController.image, scale);
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    String info = MessageFormat.format(AppVariables.message("ImageSizeChanged"),
                            (int) Math.round(imageController.image.getWidth()) + "x" + (int) Math.round(imageController.image.getHeight()),
                            (int) Math.round(imageView.getImage().getWidth()) + "x" + (int) Math.round(imageView.getImage().getHeight()),
                            (int) Math.round(newImage.getWidth()) + "x" + (int) Math.round(newImage.getHeight()));
                    parent.updateImage(ImageOperation.Scale, scale + "", null, newImage);
                    imageController.imageLabel.setText(info);
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (sizeType == SizeType.Scale) {
            setScale();
        } else if (sizeType == SizeType.Dragging || sizeType == SizeType.Pixels) {
            setPixels();
        }
    }

}
