package mara.mybox.controller;

import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import static mara.mybox.controller.BaseController.logger;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.fxml.FxmlTransformTools;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureTransformController extends ImageManufactureController {

    protected float shearX;

    @FXML
    protected ToolBar transformBar;
    @FXML
    protected ComboBox angleBox, shearBox;
    @FXML
    protected Slider angleSlider;
    @FXML
    protected Button leftButton, rightButton, shearButton;
    @FXML
    protected HBox tranBox;

    public ImageManufactureTransformController() {
    }

    @Override
    protected void initializeNext2() {
        try {
            initCommon();
            initTransformTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

//    @Override
//    protected void initInterface() {
//        try {
//            if (values == null || values.getImage() == null) {
//                return;
//            }
//            super.initInterface();
//
//
//            isSettingValues = true;
//
//            isSettingValues = false;
//        } catch (Exception e) {
//            logger.debug(e.toString());
//        }
//
//    }
    protected void initTransformTab() {
        try {

            Tooltip tips = new Tooltip(getMessage("transformComments"));
            tips.setFont(new Font(16));
            FxmlTools.setComments(tranBox, tips);

            List<String> shears = Arrays.asList(
                    "0.5", "-0.5", "0.4", "-0.4", "0.2", "-0.2", "0.1", "-0.1",
                    "0.7", "-0.7", "0.9", "-0.9", "0.8", "-0.8", "1", "-1",
                    "1.5", "-1.5", "2", "-2");
            shearBox.getItems().addAll(shears);
            shearBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        shearX = Float.valueOf(newValue);
                        shearBox.getEditor().setStyle(null);
                        shearButton.setDisable(false);
                    } catch (Exception e) {
                        shearX = 0;
                        shearBox.getEditor().setStyle(badStyle);
                        shearButton.setDisable(true);
                    }
                }
            });
            shearBox.getSelectionModel().select(0);

            angleSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    rotateAngle = newValue.intValue();
                    angleBox.getEditor().setText(rotateAngle + "");
                    leftButton.setDisable(false);
                    rightButton.setDisable(false);
                }
            });

            angleBox.getItems().addAll(Arrays.asList("90", "180", "45", "30", "60", "15", "75", "120", "135"));
            angleBox.setVisibleRowCount(10);
            angleBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        rotateAngle = Integer.valueOf(newValue);
                        angleSlider.setValue(rotateAngle);
                        angleBox.getEditor().setStyle(null);
                        leftButton.setDisable(false);
                        rightButton.setDisable(false);
                    } catch (Exception e) {
                        rotateAngle = 0;
                        angleBox.getEditor().setStyle(badStyle);
                        leftButton.setDisable(true);
                        rightButton.setDisable(true);
                    }
                }
            });
            angleBox.getSelectionModel().select(0);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void rightRotate() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTransformTools.rotateImage(values.getCurrentImage(), rotateAngle);
                recordImageHistory(ImageOperationType.Transform, newImage);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                        setBottomLabel();
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

    @FXML
    public void leftRotate() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTransformTools.rotateImage(values.getCurrentImage(), 360 - rotateAngle);
                recordImageHistory(ImageOperationType.Transform, newImage);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                        setBottomLabel();
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

    @FXML
    public void horizontalAction() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTransformTools.horizontalImage(values.getCurrentImage());
                recordImageHistory(ImageOperationType.Transform, newImage);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                        setBottomLabel();
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

    @FXML
    public void verticalAction() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTransformTools.verticalImage(values.getCurrentImage());
                recordImageHistory(ImageOperationType.Transform, newImage);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                        setBottomLabel();
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

    @FXML
    public void shearAction() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTransformTools.shearImage(values.getCurrentImage(), shearX, 0);
                recordImageHistory(ImageOperationType.Transform, newImage);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                        setBottomLabel();
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
