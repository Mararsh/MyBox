package mara.mybox.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import static mara.mybox.controller.BaseController.logger;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ImageScope;
import mara.mybox.objects.ImageScope.AreaScopeType;
import mara.mybox.objects.ImageScope.OperationType;
import mara.mybox.tools.FxmlImageTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureColorController extends ImageManufactureController {

    protected ImageScope colorScope;
    protected int colorOperationType, colorValue;

    @FXML
    protected Slider colorSlider;
    @FXML
    protected ToggleGroup colorGroup;
    @FXML
    protected TextField colorInput;
    @FXML
    protected Button colorScopeButton, colorDecreaseButton, colorIncreaseButton;
    @FXML
    protected RadioButton opacityRadio;
    @FXML
    protected Label colorUnit;

    public static class ColorOperationType {

        public static int Brightness = 0;
        public static int Sauration = 1;
        public static int Hue = 2;
        public static int Opacity = 3;
        public static int Red = 4;
        public static int Green = 5;
        public static int Blue = 6;

    }

    public ImageManufactureColorController() {
    }

    @Override
    protected void initializeNext2() {
        try {
            initCommon();
            initColorTab();
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
            if (CommonValues.NoAlphaImages.contains(values.getImageInfo().getImageFormat())) {
                opacityRadio.setDisable(true);
            } else {
                opacityRadio.setDisable(false);
            }

            colorScope = new ImageScope();
            colorScope.setOperationType(OperationType.Color);
            colorScope.setAllColors(true);
            colorScope.setAreaScopeType(AreaScopeType.AllArea);

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initColorTab() {
        try {
            colorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkColorOperationType();
                }
            });
            checkColorOperationType();

            colorSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    colorValue = newValue.intValue();
                    colorInput.setText(colorValue + "");
                }
            });

            colorInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkColorInput();
                }
            });
            checkColorInput();

            Tooltip stips = new Tooltip(getMessage("ScopeComments"));
            stips.setFont(new Font(16));
            FxmlTools.setComments(colorScopeButton, stips);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkColorOperationType() {
        RadioButton selected = (RadioButton) colorGroup.getSelectedToggle();
        if (getMessage("Brightness").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Brightness;
            colorSlider.setMax(100);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("10");
            }
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        } else if (getMessage("Saturation").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Sauration;
            colorSlider.setMax(100);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("10");
            }
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        } else if (getMessage("Hue").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Hue;
            colorSlider.setMax(359);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText(getMessage("Degree"));
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("10");
            }
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        } else if (getMessage("Opacity").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Opacity;
            colorSlider.setMax(100);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            colorDecreaseButton.setVisible(false);
            colorIncreaseButton.setText(getMessage("OK"));
        } else if (getMessage("Red").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Red;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("10");
            }
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        } else if (getMessage("Green").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Green;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("10");
            }
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        } else if (getMessage("Blue").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Blue;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("10");
            }
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        }
    }

    private void checkColorInput() {
        try {
            colorValue = Integer.valueOf(colorInput.getText());
            if (colorValue >= 0 && colorValue <= colorSlider.getMax()) {
                colorInput.setStyle(null);
                colorSlider.setValue(colorValue);
            } else {
                colorInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            colorInput.setStyle(badStyle);
        }
    }

    @FXML
    public void increaseColor() {
        if (colorOperationType == ColorOperationType.Brightness) {
            increaseBrightness();
        } else if (colorOperationType == ColorOperationType.Sauration) {
            increaseSaturate();
        } else if (colorOperationType == ColorOperationType.Hue) {
            increaseHue();
        } else if (colorOperationType == ColorOperationType.Opacity) {
            setOpacity();
        } else if (colorOperationType == ColorOperationType.Red) {
            increaseRed();
        } else if (colorOperationType == ColorOperationType.Green) {
            increaseGreen();
        } else if (colorOperationType == ColorOperationType.Blue) {
            increaseBlue();
        }
    }

    @FXML
    public void decreaseColor() {
        if (colorOperationType == ColorOperationType.Brightness) {
            decreaseBrightness();
        } else if (colorOperationType == ColorOperationType.Sauration) {
            decreaseSaturate();
        } else if (colorOperationType == ColorOperationType.Hue) {
            decreaseHue();
        } else if (colorOperationType == ColorOperationType.Red) {
            decreaseRed();
        } else if (colorOperationType == ColorOperationType.Green) {
            decreaseGreen();
        } else if (colorOperationType == ColorOperationType.Blue) {
            decreaseBlue();
        }
    }

    @FXML
    public void setColorScope() {
        setScope(colorScope);
    }

    @Override
    protected void setScopePane() {
        try {
            showScopeCheck.setDisable(false);
            values.setCurrentScope(colorScope);
            scopePaneValid = true;
            super.setScopePane();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void wholeColorScope() {
        colorScope = new ImageScope();
        colorScope.setOperationType(OperationType.Color);
        colorScope.setAllColors(true);
        colorScope.setAreaScopeType(AreaScopeType.AllArea);
        setScopePane();

    }

    @Override
    public void scopeDetermined(ImageScope imageScope) {
        values.setCurrentScope(imageScope);
        colorScope = imageScope;
        setScopePane();
    }

    public void increaseHue() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeHue(values.getCurrentImage(), colorValue, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void decreaseHue() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeHue(values.getCurrentImage(), 0 - colorValue, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void increaseSaturate() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeSaturate(values.getCurrentImage(), colorValue / 100.0f, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    public void decreaseSaturate() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeSaturate(values.getCurrentImage(), 0.0f - colorValue / 100.0f, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void increaseBrightness() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeBrightness(values.getCurrentImage(), colorValue / 100.0f, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void decreaseBrightness() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeBrightness(values.getCurrentImage(), 0.0f - colorValue / 100.0f, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void setOpacity() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.setOpacity(values.getCurrentImage(), colorValue / 100.0f, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void increaseRed() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeRed(values.getCurrentImage(), colorValue / 255.0, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void increaseGreen() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeGreen(values.getCurrentImage(), colorValue / 255.0, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void increaseBlue() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeBlue(values.getCurrentImage(), colorValue / 255.0, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void decreaseRed() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeRed(values.getCurrentImage(), 0.0 - colorValue / 255.0, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void decreaseGreen() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeGreen(values.getCurrentImage(), 0.0 - colorValue / 255.0, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);

                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void decreaseBlue() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeBlue(values.getCurrentImage(), 0.0 - colorValue / 255.0, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);

                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

}
