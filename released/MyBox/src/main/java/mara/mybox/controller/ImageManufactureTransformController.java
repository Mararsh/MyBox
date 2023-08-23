package mara.mybox.controller;

import java.util.Arrays;
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
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageManufactureTransformController extends ImageManufactureOperationController {

    protected float shearX, shearY;
    protected int rotateAngle;

    @FXML
    protected ComboBox angleSelector, xSelector, ySelector;
    @FXML
    protected Slider angleSlider;
    @FXML
    protected Button shearButton, rotateLeftButton, rotateRightButton;

    @Override
    public void initPane() {
        try {
            super.initPane();

            shearX = UserConfig.getFloat("ImageShearX", 0.5f);
            xSelector.getItems().addAll(Arrays.asList(
                    "0.5", "-0.5", "0", "0.4", "-0.4", "0.2", "-0.2", "0.1", "-0.1",
                    "0.7", "-0.7", "0.9", "-0.9", "0.8", "-0.8", "1", "-1",
                    "1.5", "-1.5", "2", "-2"));
            xSelector.setValue(shearX + "");
            xSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        shearX = Float.parseFloat(newValue);
                        UserConfig.setFloat("ImageShearX", shearX);
                        shearButton.setDisable(false);
                        ValidationTools.setEditorNormal(xSelector);
                    } catch (Exception e) {
                        shearButton.setDisable(true);
                        ValidationTools.setEditorBadStyle(xSelector);
                    }
                }
            });

            shearY = UserConfig.getFloat("ImageShearY", 0f);
            ySelector.getItems().addAll(Arrays.asList(
                    "0", "0.5", "-0.5", "0.4", "-0.4", "0.2", "-0.2", "0.1", "-0.1",
                    "0.7", "-0.7", "0.9", "-0.9", "0.8", "-0.8", "1", "-1",
                    "1.5", "-1.5", "2", "-2"));
            ySelector.setValue(shearY + "");
            ySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        shearY = Float.parseFloat(newValue);
                        UserConfig.setFloat("ImageShearY", shearY);
                        shearButton.setDisable(false);
                        ValidationTools.setEditorNormal(ySelector);
                    } catch (Exception e) {
                        shearButton.setDisable(true);
                        ValidationTools.setEditorBadStyle(ySelector);
                    }
                }
            });

            angleSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    rotate(newValue.intValue());
                }
            });

            rotateAngle = UserConfig.getInt("ImageRotateAngle", 45);
            angleSelector.getItems().addAll(Arrays.asList(
                    "45", "90", "180", "30", "60", "15", "5", "10", "1", "75", "120", "135"));
            angleSelector.setVisibleRowCount(10);
            angleSelector.setValue(rotateAngle + "");
            angleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        rotateAngle = Integer.parseInt(newValue);
                        UserConfig.setInt("ImageRotateAngle", rotateAngle);
                        rotateLeftButton.setDisable(false);
                        rotateRightButton.setDisable(false);
                        ValidationTools.setEditorNormal(angleSelector);
                    } catch (Exception e) {
                        rotateLeftButton.setDisable(true);
                        rotateRightButton.setDisable(true);
                        ValidationTools.setEditorBadStyle(angleSelector);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    protected void paneExpanded() {
        editor.showRightPane();
        editor.resetImagePane();
        editor.imageTab();
    }

    @FXML
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
                editor.popSuccessful();
                editor.updateImage(ImageOperation.Transform, "rotateRight", rotateAngle + "",
                        newImage, cost);
            }
        };
        start(task);
    }

    @FXML
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
                editor.popSuccessful();
                editor.updateImage(ImageOperation.Transform, "rotateLeft", (360 - rotateAngle) + "",
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
                editor.popSuccessful();
                editor.updateImage(ImageOperation.Transform, "horizontal", null, newImage, cost);
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
                editor.popSuccessful();
                editor.updateImage(ImageOperation.Transform, "vertical", null, newImage, cost);
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
                newImage = TransformTools.shearImage(imageView.getImage(), shearX, shearY);
                if (task == null || isCancelled()) {
                    return false;
                }
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                editor.popSuccessful();
                editor.updateImage(ImageOperation.Transform, "shear", shearX + "", newImage, cost);
            }

        };
        start(task);
    }

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
                editor.popSuccessful();
                editor.updateImage(ImageOperation.Transform, "rotate", angle + "", newImage, cost);
            }
        };
        start(task);
    }

}
