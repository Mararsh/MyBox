package mara.mybox.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import static mara.mybox.controller.BaseController.logger;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.image.FxmlImageTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureCropController extends ImageManufactureController {

    protected int cropLeftX, cropLeftY, cropRightX, cropRightY;
    protected boolean areaValid;

    @FXML
    protected TextField cropLeftXInput, cropLeftYInput, cropRightXInput, cropRightYInput;
    @FXML
    protected Button cropOkButton;
    @FXML
    protected ToolBar cropBar;

    public ImageManufactureCropController() {
    }

    @Override
    protected void initializeNext2() {
        try {
            initCommon();
            initCropTab();
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
            cropRightXInput.setText(values.getImageInfo().getxPixels() * 3 / 4 + "");
            cropRightYInput.setText(values.getImageInfo().getyPixels() * 3 / 4 + "");
            cropLeftXInput.setText(values.getImageInfo().getxPixels() / 4 + "");
            cropLeftYInput.setText(values.getImageInfo().getyPixels() / 4 + "");

            isSettingValues = false;

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initCropTab() {
        try {

            Tooltip tips = new Tooltip(getMessage("CropComments"));
            tips.setFont(new Font(16));
            FxmlTools.setComments(cropBar, tips);
            cropLeftXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkCropValues();
                }
            });
            cropLeftYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkCropValues();
                }
            });
            cropRightXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkCropValues();
                }
            });
            cropRightYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkCropValues();
                }
            });

            cropOkButton.disableProperty().bind(
                    cropLeftXInput.styleProperty().isEqualTo(badStyle)
                            .or(cropLeftYInput.styleProperty().isEqualTo(badStyle))
                            .or(cropRightXInput.styleProperty().isEqualTo(badStyle))
                            .or(cropRightYInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(cropLeftXInput.textProperty()))
                            .or(Bindings.isEmpty(cropLeftYInput.textProperty()))
                            .or(Bindings.isEmpty(cropRightXInput.textProperty()))
                            .or(Bindings.isEmpty(cropRightYInput.textProperty()))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkCropValues() {
        areaValid = true;
        try {
            cropLeftX = Integer.valueOf(cropLeftXInput.getText());
            cropLeftXInput.setStyle(null);
            if (cropLeftX >= 0 && cropLeftX <= values.getCurrentImage().getWidth()) {
                cropLeftXInput.setStyle(null);
            } else {
                cropLeftXInput.setStyle(badStyle);
                areaValid = false;
            }
        } catch (Exception e) {
            cropLeftXInput.setStyle(badStyle);
            areaValid = false;
        }

        try {
            cropLeftY = Integer.valueOf(cropLeftYInput.getText());
            cropLeftYInput.setStyle(null);
            if (cropLeftY >= 0 && cropLeftY <= values.getCurrentImage().getHeight()) {
                cropLeftYInput.setStyle(null);
            } else {
                cropLeftYInput.setStyle(badStyle);
                areaValid = false;
            }
        } catch (Exception e) {
            cropLeftYInput.setStyle(badStyle);
            areaValid = false;
        }

        try {
            cropRightX = Integer.valueOf(cropRightXInput.getText());
            cropRightXInput.setStyle(null);
            if (cropRightX >= 0 && cropRightX <= values.getCurrentImage().getWidth()) {
                cropRightXInput.setStyle(null);
            } else {
                cropRightXInput.setStyle(badStyle);
                areaValid = false;
            }
        } catch (Exception e) {
            cropRightXInput.setStyle(badStyle);
            areaValid = false;
        }

        try {
            cropRightY = Integer.valueOf(cropRightYInput.getText());
            cropRightYInput.setStyle(null);
            if (cropRightY >= 0 && cropRightY <= values.getCurrentImage().getHeight()) {
                cropRightYInput.setStyle(null);
            } else {
                cropRightYInput.setStyle(badStyle);
                areaValid = false;
            }
        } catch (Exception e) {
            cropRightYInput.setStyle(badStyle);
            areaValid = false;
        }

        if (cropLeftX >= cropRightX) {
            cropLeftXInput.setStyle(badStyle);
            cropRightXInput.setStyle(badStyle);
            areaValid = false;
        }

        if (cropLeftY >= cropRightY) {
            cropLeftYInput.setStyle(badStyle);
            cropRightYInput.setStyle(badStyle);
            areaValid = false;
        }

        if (!isSettingValues) {
            if (!areaValid) {
                popError(getMessage("InvalidRectangle"));
                return;
            }
            showCropScope();
        }

    }

    protected void showCropScope() {
        if (!areaValid) {
            imageView.setImage(values.getCurrentImage());
            popInformation(getMessage("CropComments"));
            return;
        }

        if (task != null && task.isRunning()) {
            return;
        }
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    int lineWidth = 1;
                    if (values.getCurrentImage().getWidth() >= 100) {
                        lineWidth = (int) values.getCurrentImage().getWidth() / 100;
                    }
                    final Image newImage = FxmlImageTools.indicateArea(values.getCurrentImage(),
                            Color.RED, lineWidth,
                            cropLeftX, cropLeftY, cropRightX, cropRightY);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImage(newImage);
//                            popInformation(AppVaribles.getMessage("CropComments"));
                        }
                    });
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    @Override
    public void clickImage(MouseEvent event) {
        if (values.getCurrentImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        imageView.setCursor(Cursor.HAND);

        int x = (int) Math.round(event.getX() * values.getCurrentImage().getWidth() / imageView.getBoundsInLocal().getWidth());
        int y = (int) Math.round(event.getY() * values.getCurrentImage().getHeight() / imageView.getBoundsInLocal().getHeight());

        if (event.getButton() == MouseButton.SECONDARY) {

            isSettingValues = true;
            cropRightXInput.setText(x + "");
            cropRightYInput.setText(y + "");
            isSettingValues = false;

            if (!areaValid) {
                popError(getMessage("InvalidRectangle"));
            } else if (task == null || !task.isRunning()) {
                showCropScope();
            }

        } else if (event.getButton() == MouseButton.PRIMARY) {

            isSettingValues = true;
            cropLeftXInput.setText(x + "");
            cropLeftYInput.setText(y + "");
            isSettingValues = false;

            if (!areaValid) {
                popError(getMessage("InvalidRectangle"));
            } else if (task == null || !task.isRunning()) {
                showCropScope();
            }
        }

    }

    @FXML
    public void cropAction() {
        imageView.setCursor(Cursor.OPEN_HAND);

        Task cropTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    final Image newImage = FxmlImageTools.cropImage(values.getCurrentImage(),
                            cropLeftX, cropLeftY, cropRightX, cropRightY);
                    recordImageHistory(ImageOperationType.Crop, newImage);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            values.setUndoImage(values.getCurrentImage());
                            values.setCurrentImage(newImage);
                            imageView.setImage(newImage);
                            setImageChanged(true);

                            isSettingValues = true;
                            cropRightXInput.setText((int) (values.getCurrentImage().getWidth() - 1) + "");
                            cropRightYInput.setText((int) (values.getCurrentImage().getHeight() - 1) + "");
                            cropLeftXInput.setText("0");
                            cropLeftYInput.setText("0");
                            isSettingValues = false;
                            setBottomLabel();

                        }
                    });
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        openHandlingStage(cropTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(cropTask);
        thread.setDaemon(true);
        thread.start();
    }

}
