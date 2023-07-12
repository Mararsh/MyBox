package mara.mybox.controller;

import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import mara.mybox.controller.ImageManufactureController_Image.ImageOperation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.TransformTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageManufactureTransformController extends ImageManufactureOperationController {

    protected float shearX;
    protected int rotateAngle;

    @FXML
    protected ComboBox angleBox, shearBox;
    @FXML
    protected Slider angleSlider;
    @FXML
    protected Button shearButton;

    @Override
    public void initPane() {
        try {
            super.initPane();

            List<String> shears = Arrays.asList(
                    "0.5", "-0.5", "0.4", "-0.4", "0.2", "-0.2", "0.1", "-0.1",
                    "0.7", "-0.7", "0.9", "-0.9", "0.8", "-0.8", "1", "-1",
                    "1.5", "-1.5", "2", "-2");
            shearBox.getItems().addAll(shears);
            shearBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        shearX = Float.parseFloat(newValue);
                        shearButton.setDisable(false);
                        ValidationTools.setEditorNormal(shearBox);
                    } catch (Exception e) {
                        shearX = 0;
                        shearButton.setDisable(true);
                        ValidationTools.setEditorBadStyle(shearBox);
                    }
                }
            });
            shearBox.getSelectionModel().select(0);

            angleSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    rotate(newValue.intValue());
                }
            });

            angleBox.getItems().addAll(Arrays.asList("90", "180", "45", "30", "60", "15", "5", "10", "1", "75", "120", "135"));
            angleBox.setVisibleRowCount(10);
            angleBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        rotateAngle = Integer.parseInt(newValue);
                        rotateLeftButton.setDisable(false);
                        rotateRightButton.setDisable(false);
                        ValidationTools.setEditorNormal(angleBox);
                    } catch (Exception e) {
                        rotateLeftButton.setDisable(true);
                        rotateRightButton.setDisable(true);
                        ValidationTools.setEditorBadStyle(angleBox);
                    }
                }
            });
            angleBox.getSelectionModel().select(0);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    protected void paneExpanded() {
        imageController.showRightPane();
        imageController.resetImagePane();
        imageController.imageTab();
    }

    @FXML
    @Override
    public void rotateRight() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image newImage;

            @Override
            protected boolean handle() {
                newImage = TransformTools.rotateImage(imageView.getImage(), rotateAngle);
                if (task == null || isCancelled()) {
                    return false;
                }
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                imageController.popSuccessful();
                imageController.updateImage(ImageOperation.Transform, "rotateRight", rotateAngle + "",
                        newImage, cost);
            }
        };
        start(task);
    }

    @FXML
    @Override
    public void rotateLeft() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image newImage;

            @Override
            protected boolean handle() {
                newImage = TransformTools.rotateImage(imageView.getImage(), 360 - rotateAngle);
                if (task == null || isCancelled()) {
                    return false;
                }
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                imageController.popSuccessful();
                imageController.updateImage(ImageOperation.Transform, "rotateLeft", (360 - rotateAngle) + "",
                        newImage, cost);
            }

        };
        start(task);
    }

    @FXML
    public void horizontalAction() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image newImage;

            @Override
            protected boolean handle() {
                newImage = TransformTools.horizontalImage(imageView.getImage());
                if (task == null || isCancelled()) {
                    return false;
                }
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                imageController.popSuccessful();
                imageController.updateImage(ImageOperation.Transform, "horizontal", null, newImage, cost);
            }

        };
        start(task);
    }

    @FXML
    public void verticalAction() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image newImage;

            @Override
            protected boolean handle() {
                newImage = TransformTools.verticalImage(imageView.getImage());
                if (task == null || isCancelled()) {
                    return false;
                }
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                imageController.popSuccessful();
                imageController.updateImage(ImageOperation.Transform, "vertical", null, newImage, cost);
            }

        };
        start(task);
    }

    @FXML
    public void shearAction() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image newImage;

            @Override
            protected boolean handle() {
                newImage = TransformTools.shearImage(imageView.getImage(), shearX, 0);
                if (task == null || isCancelled()) {
                    return false;
                }
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                imageController.popSuccessful();
                imageController.updateImage(ImageOperation.Transform, "shear", shearX + "", newImage, cost);
            }

        };
        start(task);
    }

    @Override
    public void rotate(int angle) {
        imageView.setRotate(angle);
    }

    @FXML
    @Override
    public void okAction() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private int angle;
            private Image newImage;

            @Override
            protected boolean handle() {
                angle = (int) angleSlider.getValue();
                newImage = TransformTools.rotateImage(imageView.getImage(), angle);
                if (task == null || isCancelled()) {
                    return false;
                }
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                imageController.popSuccessful();
                imageController.updateImage(ImageOperation.Transform, "rotate", angle + "", newImage, cost);
            }
        };
        start(task);
    }

}
