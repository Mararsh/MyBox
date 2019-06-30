package mara.mybox.controller;

import mara.mybox.controller.base.ImageManufactureController;
import java.text.MessageFormat;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
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
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import mara.mybox.data.DoubleRectangle;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.image.ImageManufacture;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.image.ImageAttributes;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.FxmlImageManufacture;
import static mara.mybox.value.AppVaribles.getMessage;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureSizeController extends ImageManufactureController {

    protected SizeType sizeType;
    protected int width, height;
    protected boolean noRatio;
    protected float scale = 1.0f;

    @FXML
    protected ToggleGroup pixelsGroup;
    @FXML
    protected HBox setBox;
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

    public ImageManufactureSizeController() {
    }

    @Override
    public void initializeNext2() {
        try {
            initCommon();
            initSizeTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void initInterface() {
        try {
            if (values == null || values.getImage() == null) {
                return;
            }
            super.initInterface();
            isSettingValues = true;
            tabPane.getSelectionModel().select(sizeTab);
            widthInput.setText((int) values.getImage().getWidth() + "");
            heightInput.setText((int) values.getImage().getHeight() + "");
            attributes.setSourceWidth((int) values.getImage().getWidth());
            attributes.setSourceHeight((int) values.getImage().getHeight());
            isSettingValues = false;

            checkSizeType();

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initSizeTab() {
        try {

            attributes = new ImageAttributes();

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

            ratioBox.getItems().addAll(Arrays.asList(
                    getMessage("BaseOnWidth"), getMessage("BaseOnHeight"),
                    getMessage("BaseOnLarger"), getMessage("BaseOnSmaller")));
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
                    if (values.getImage().getWidth() > 0) {
                        widthInput.setText((int) values.getImage().getWidth() + "");
                    }
                    if (values.getImage().getHeight() > 0) {
                        heightInput.setText((int) values.getImage().getHeight() + "");
                    }
                    noRatio = false;
                }
            });

            calculatorButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        final PixelsCalculationController controller
                                = (PixelsCalculationController) FxmlStage.openStage( myStage,
                                        CommonValues.PixelsCalculatorFxml,
                                        true, Modality.WINDOW_MODAL, null);

                        attributes.setKeepRatio(keepRatioCheck.isSelected());
                        controller.setSource(attributes, widthInput, heightInput);
                        noRatio = true;
                        controller.myStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
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
                    checkSizeType();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkSizeType() {
        try {
            setBox.getChildren().clear();
            widthInput.setStyle(null);
            heightInput.setStyle(null);
            scaleBox.getEditor().setStyle(null);
            okButton.setVisible(true);
            promptLabel.setText("");

            RadioButton selected = (RadioButton) pixelsGroup.getSelectedToggle();
            if (getMessage("AdjustByDragging").equals(selected.getText())) {
                sizeType = SizeType.Dragging;
                scaleBox.setDisable(false);
                setBox.getChildren().addAll(okButton, keepRatioCheck);
                keepRatioCheck.setSelected(true);
                okButton.setVisible(false);
                initMaskRectangleLine(true);
                promptLabel.setText(getMessage("DragSizeComments"));

            } else {

                initMaskRectangleLine(false);

                if (getMessage("Pixels").equals(selected.getText())) {
                    sizeType = SizeType.Pixels;
                    setBox.getChildren().addAll(keepRatioCheck, ratioBox, label1, widthInput, label2, heightInput,
                            originalButton, calculatorButton, okButton);
                    keepRatioCheck.setSelected(true);

                    checkPixelsWidth();
                    checkPixelsHeight();

                } else if (getMessage("ZoomScale").equals(selected.getText())) {
                    sizeType = SizeType.Scale;
                    scaleBox.setDisable(false);
                    setBox.getChildren().addAll(scaleBox, okButton);

                }
            }
        } catch (Exception e) {
            widthInput.setStyle(badStyle);
        }
    }

    @Override
    public void setDafultMaskRectangleValues() {
        if (imageView == null || maskPane == null || maskRectangleLine == null) {
            return;
        }
        maskRectangleData = new DoubleRectangle(0, 0,
                imageView.getImage().getWidth() - 1, imageView.getImage().getHeight() - 1);
    }

    @Override
    public boolean drawMaskRectangleLine() {
        if (maskRectangleLine == null || !maskRectangleLine.isVisible()
                || imageView.getImage() == null) {
            return false;
        }

        double oldWidth = imageView.getImage().getWidth();
        double oldHeight = imageView.getImage().getHeight();
        double newWidth = maskRectangleData.getBigX() - maskRectangleData.getSmallX() + 1;
        double newHeight = maskRectangleData.getBigY() - maskRectangleData.getSmallY() + 1;
        double offsetW = Math.abs(newWidth - oldWidth);
        double offsetH = Math.abs(newHeight - oldHeight);
        if (offsetW == 0 && offsetH == 0) {
            width = (int) Math.round(oldWidth);
            height = (int) Math.round(oldHeight);
            drawMaskAroundLine(imageView);
            updateLabelTitle();
            return true;
        }

        if (Math.abs(offsetW) >= Math.abs(offsetH)) {
            if (keepRatioCheck.isSelected()) {
                newHeight = oldHeight * newWidth / oldWidth;
            }

        } else {
            if (keepRatioCheck.isSelected()) {
                newWidth = oldWidth * newHeight / oldHeight;
            }
        }
        width = (int) Math.round(newWidth);
        height = (int) Math.round(newHeight);
        maskRectangleData = new DoubleRectangle(0, 0, width - 1, height - 1);
        setPixels();
        return true;
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
            if (getMessage("BaseOnWidth").equals(s)) {
                attributes.setRatioAdjustion(ImageManufacture.KeepRatioType.BaseOnWidth);
                heightInput.setDisable(true);
            } else if (getMessage("BaseOnHeight").equals(s)) {
                attributes.setRatioAdjustion(ImageManufacture.KeepRatioType.BaseOnHeight);
                widthInput.setDisable(true);
            } else if (getMessage("BaseOnLarger").equals(s)) {
                attributes.setRatioAdjustion(ImageManufacture.KeepRatioType.BaseOnLarger);
            } else if (getMessage("BaseOnSmaller").equals(s)) {
                attributes.setRatioAdjustion(ImageManufacture.KeepRatioType.BaseOnSmaller);
            } else {
                attributes.setRatioAdjustion(ImageManufacture.KeepRatioType.None);
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

        try {
            width = Integer.valueOf(widthInput.getText());
            height = Integer.valueOf(heightInput.getText());
            attributes.setTargetWidth(width);
            attributes.setTargetHeight(height);
            int sourceX = (int) values.getImage().getWidth();
            int sourceY = (int) values.getImage().getHeight();
            if (noRatio || !keepRatioCheck.isSelected() || sourceX <= 0 || sourceY <= 0) {
                return;
            }
            long ratioX = Math.round(width * 1000 / sourceX);
            long ratioY = Math.round(height * 1000 / sourceY);
            if (ratioX == ratioY) {
                return;
            }
            switch (attributes.getRatioAdjustion()) {
                case ImageManufacture.KeepRatioType.BaseOnWidth:
                    heightInput.setText(Math.round(width * sourceY / sourceX) + "");
                    break;
                case ImageManufacture.KeepRatioType.BaseOnHeight:
                    widthInput.setText(Math.round(height * sourceX / sourceY) + "");
                    break;
                case ImageManufacture.KeepRatioType.BaseOnLarger:
                    if (ratioX > ratioY) {
                        heightInput.setText(Math.round(width * sourceY / sourceX) + "");
                    } else {
                        widthInput.setText(Math.round(height * sourceX / sourceY) + "");
                    }
                    break;
                case ImageManufacture.KeepRatioType.BaseOnSmaller:
                    if (ratioX > ratioY) {
                        widthInput.setText(Math.round(height * sourceX / sourceY) + "");
                    } else {
                        heightInput.setText(Math.round(width * sourceY / sourceX) + "");
                    }
                    break;
                default:
                    break;
            }
            width = Integer.valueOf(widthInput.getText());
            height = Integer.valueOf(heightInput.getText());
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (sizeType == SizeType.Scale) {
            setScale();
        } else if (sizeType == SizeType.Pixels) {
            setPixels();
        }
    }

    protected void setScale() {
        if (scale <= 0) {
            return;
        }
        task = new Task<Void>() {
            private Image newImage;
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                newImage = FxmlImageManufacture.scaleImage(imageView.getImage(), scale);
                if (task == null || task.isCancelled()) {
                    return null;
                }
                recordImageHistory(ImageOperationType.Size, newImage);

                ok = true;
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (ok) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            showNewImage(newImage);
                            fitSize();
                        }
                    });
                }
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    protected void setPixels() {
        if (width <= 0 || height <= 0) {
            return;
        }
        task = new Task<Void>() {
            private Image newImage;
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                newImage = FxmlImageManufacture.scaleImage(imageView.getImage(), width, height);
                if (task == null || task.isCancelled()) {
                    return null;
                }
                recordImageHistory(ImageOperationType.Size, newImage);

                ok = true;
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (ok) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            showNewImage(newImage);
                            fitSize();
                        }
                    });
                }
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void showNewImage(Image newImage) {
        String info = (int) Math.round(imageView.getImage().getWidth()) + "x"
                + (int) Math.round(imageView.getImage().getHeight());
        values.setUndoImage(imageView.getImage());
        values.setCurrentImage(newImage);
        imageView.setImage(newImage);
        if (sizeType == SizeType.Dragging) {
            maskRectangleData = new DoubleRectangle(0, 0, newImage.getWidth() - 1, newImage.getHeight() - 1);
            resetMaskControls();
        }
        setImageChanged(true);
        info = MessageFormat.format(AppVaribles.getMessage("ImageSizeChanged"),
                info, (int) Math.round(newImage.getWidth()) + "x"
                + (int) Math.round(newImage.getHeight()));
        popInformation(info, 3000);
        promptLabel.setText(info);
    }

    @FXML
    @Override
    public void recoverAction() {
        super.recoverAction();
        promptLabel.setText("");
    }

    @FXML
    @Override
    public void undoAction() {
        super.undoAction();
        promptLabel.setText("");
    }

    @FXML
    @Override
    public void redoAction() {
        super.redoAction();
        promptLabel.setText("");
    }

}
