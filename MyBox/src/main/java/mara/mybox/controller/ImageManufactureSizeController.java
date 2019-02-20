package mara.mybox.controller;

import java.util.Arrays;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.image.ImageConvert;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.value.CommonValues;
import mara.mybox.data.ImageAttributes;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.fxml.image.ImageTools;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureSizeController extends ImageManufactureController {

    protected int width, height;
    protected boolean isScale, noRatio;
    protected float scale = 1.0f;

    @FXML
    protected ToolBar pixelsBar;
    @FXML
    protected ToggleGroup pixelsGroup;
    @FXML
    protected CheckBox keepRatioCheck;
    @FXML
    protected TextField widthInput, heightInput;
    @FXML
    protected ChoiceBox ratioBox;
    @FXML
    protected ComboBox scaleBox;

    public ImageManufactureSizeController() {
    }

    @Override
    protected void initializeNext2() {
        try {
            initCommon();
            initPixelsTab();
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
            widthInput.setText((int) values.getImage().getWidth() + "");
            heightInput.setText((int) values.getImage().getHeight() + "");
            attributes.setSourceWidth((int) values.getImage().getWidth());
            attributes.setSourceHeight((int) values.getImage().getHeight());
            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initPixelsTab() {
        try {
            attributes = new ImageAttributes();

            widthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkPixelsWidth();
                }
            });
            checkPixelsWidth();

            heightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkPixelsHeight();
                }
            });
            checkPixelsHeight();

            keepRatioCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    checkRatio();
                }
            });
            checkRatio();

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
            checkSizeType();

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

            scaleBox.getItems().addAll(Arrays.asList("0.5", "2.0", "0.8", "0.1", "1.5", "3.0", "10.0", "0.01", "5.0", "0.3"));
            scaleBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        scale = Float.valueOf(newValue);
                        if (scale > 0) {
                            scaleBox.getEditor().setStyle(null);
                            if (values.getCurrentImage() != null) {
                                noRatio = true;
                                widthInput.setText(Math.round(values.getCurrentImage().getWidth() * scale) + "");
                                heightInput.setText(Math.round(values.getCurrentImage().getHeight() * scale) + "");
                                noRatio = false;
                            }
                        } else {
                            scaleBox.getEditor().setStyle(badStyle);
                        }

                    } catch (Exception e) {
                        scale = 0;
                        scaleBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            scaleBox.getSelectionModel().select(0);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkSizeType() {
        try {
            RadioButton selected = (RadioButton) pixelsGroup.getSelectedToggle();
            if (getMessage("Pixels").equals(selected.getText())) {
                pixelsBar.setDisable(false);
                isScale = false;
                checkPixelsWidth();
                checkPixelsHeight();
                scaleBox.getEditor().setStyle(null);
                scaleBox.setDisable(true);
            } else {
                pixelsBar.setDisable(true);
                scaleBox.setDisable(false);
                isScale = true;
                widthInput.setStyle(null);
                heightInput.setStyle(null);
            }
        } catch (Exception e) {
            widthInput.setStyle(badStyle);
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
            if (getMessage("BaseOnWidth").equals(s)) {
                attributes.setRatioAdjustion(ImageConvert.KeepRatioType.BaseOnWidth);
            } else if (getMessage("BaseOnHeight").equals(s)) {
                attributes.setRatioAdjustion(ImageConvert.KeepRatioType.BaseOnHeight);
            } else if (getMessage("BaseOnLarger").equals(s)) {
                attributes.setRatioAdjustion(ImageConvert.KeepRatioType.BaseOnLarger);
            } else if (getMessage("BaseOnSmaller").equals(s)) {
                attributes.setRatioAdjustion(ImageConvert.KeepRatioType.BaseOnSmaller);
            } else {
                attributes.setRatioAdjustion(ImageConvert.KeepRatioType.None);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkRatio() {
        if (!keepRatioCheck.isSelected()) {
            return;
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
                case ImageConvert.KeepRatioType.BaseOnWidth:
                    heightInput.setText(Math.round(width * sourceY / sourceX) + "");
                    break;
                case ImageConvert.KeepRatioType.BaseOnHeight:
                    widthInput.setText(Math.round(height * sourceX / sourceY) + "");
                    break;
                case ImageConvert.KeepRatioType.BaseOnLarger:
                    if (ratioX > ratioY) {
                        heightInput.setText(Math.round(width * sourceY / sourceX) + "");
                    } else {
                        widthInput.setText(Math.round(height * sourceX / sourceY) + "");
                    }
                    break;
                case ImageConvert.KeepRatioType.BaseOnSmaller:
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
    protected void setOriginalSize() {
        noRatio = true;
        if (values.getImage().getWidth() > 0) {
            widthInput.setText((int) values.getImage().getWidth() + "");
        }
        if (values.getImage().getHeight() > 0) {
            heightInput.setText((int) values.getImage().getHeight() + "");
        }
        noRatio = false;
    }

    @FXML
    public void pixelsCalculator() {
        try {
            attributes.setKeepRatio(keepRatioCheck.isSelected());
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.PixelsCalculatorFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            final PixelsCalculationController controller = fxmlLoader.getController();
            Stage stage = new Stage();
            controller.setMyStage(stage);
            controller.setSource(attributes, widthInput, heightInput);

            Scene scene = new Scene(pane);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(getMyStage());
            stage.setTitle(AppVaribles.getMessage("PixelsCalculator"));
            stage.getIcons().add(CommonValues.AppIcon);
            stage.setScene(scene);
            stage.show();
            noRatio = true;
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    noRatio = false;
                    if (!controller.stageClosing()) {
                        event.consume();
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (isScale) {
            setScale();
        } else {
            setPixels();
        }
    }

    protected void setScale() {
        if (scale <= 0) {
            return;
        }
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = ImageTools.scaleImage(values.getCurrentImage(), scale);
                if (task.isCancelled()) {
                    return null;
                }
                recordImageHistory(ImageOperationType.Size, newImage);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                        setBottomLabel();
                        popInformation(AppVaribles.getMessage("Successful"), 1500);
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

    protected void setPixels() {
        if (width <= 0 || height <= 0) {
            return;
        }
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = ImageTools.scaleImage(values.getCurrentImage(), width, height);
                if (task.isCancelled()) {
                    return null;
                }
                recordImageHistory(ImageOperationType.Size, newImage);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                        setBottomLabel();
                        popInformation(AppVaribles.getMessage("Successful"), 1500);
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
