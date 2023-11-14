package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.bufferedimage.ImageContrast;
import mara.mybox.bufferedimage.ImageContrast.ContrastAlgorithm;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageContrastController extends BaseImageEditController {

    protected ContrastAlgorithm contrastAlgorithm;
    protected int left, right, offset, para1, para2;

    @FXML
    protected ToggleGroup contrastGroup;
    @FXML
    protected RadioButton hsbRaido, grayEqualizationRaido, grayStretchingRaido, grayShiftingRaido;
    @FXML
    protected VBox setBox;
    @FXML
    protected FlowPane leftValuePane, rightValuePane, offsetPane;
    @FXML
    protected TextField leftInput, rightInput, offsetInput;

    public ImageContrastController() {
        baseTitle = message("Contrast");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            if (editor == null) {
                close();
                return;
            }

            NodeStyleTools.setTooltip(leftInput, new Tooltip("0~255"));
            NodeStyleTools.setTooltip(rightInput, new Tooltip("0~255"));
            NodeStyleTools.setTooltip(offsetInput, new Tooltip("-255~255"));

            left = UserConfig.getInt(baseName + "LeftThreshold", 100);
            if (left < 0 || left > 255) {
                left = 100;
            }
            leftInput.setText(left + "");
            leftInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    try {
                        if (newValue) {
                            return;
                        }
                        int v = Integer.parseInt(leftInput.getText());
                        if (v >= 0 && v <= 255) {
                            left = v;
                            leftInput.setStyle(null);
                            UserConfig.setInt(baseName + "LeftThreshold", left);
                        } else {
                            leftInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        leftInput.setStyle(UserConfig.badStyle());
                    }
                }
            });

            right = UserConfig.getInt(baseName + "RightThreshold", 100);
            if (right < 0 || right > 255) {
                right = 100;
            }
            rightInput.setText(right + "");
            rightInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    try {
                        if (newValue) {
                            return;
                        }
                        int v = Integer.parseInt(rightInput.getText());
                        if (v >= 0 && v <= 255) {
                            right = v;
                            rightInput.setStyle(null);
                            UserConfig.setInt(baseName + "RightThreshold", right);
                        } else {
                            rightInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        rightInput.setStyle(UserConfig.badStyle());
                    }
                }
            });

            offset = UserConfig.getInt(baseName + "Offset", 100);
            if (offset < -255 || offset > 255) {
                offset = 100;
            }
            offsetInput.setText(offset + "");
            offsetInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    try {
                        if (newValue) {
                            return;
                        }
                        int v = Integer.parseInt(offsetInput.getText());
                        if (v >= -255 && v <= 255) {
                            offset = v;
                            offsetInput.setStyle(null);
                            UserConfig.setInt(baseName + "Offset", offset);
                        } else {
                            offsetInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        offsetInput.setStyle(UserConfig.badStyle());
                    }
                }
            });

            contrastGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkContrastAlgorithm();
                }
            });

            checkContrastAlgorithm();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void checkContrastAlgorithm() {
        try {
            contrastAlgorithm = null;
            setBox.getChildren().clear();
            if (hsbRaido.isSelected()) {
                contrastAlgorithm = ContrastAlgorithm.HSB_Histogram_Equalization;

            } else if (grayEqualizationRaido.isSelected()) {
                contrastAlgorithm = ContrastAlgorithm.Gray_Histogram_Equalization;

            } else if (grayStretchingRaido.isSelected()) {
                contrastAlgorithm = ContrastAlgorithm.Gray_Histogram_Stretching;
                if (!setBox.getChildren().contains(leftValuePane)) {
                    setBox.getChildren().addAll(leftValuePane, rightValuePane);
                }

            } else if (grayShiftingRaido.isSelected()) {
                contrastAlgorithm = ContrastAlgorithm.Gray_Histogram_Shifting;
                if (!setBox.getChildren().contains(offsetPane)) {
                    setBox.getChildren().add(offsetPane);
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    protected boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        if (contrastAlgorithm != null
                && left >= 0 && left <= 255
                && right >= 0 && right <= 255
                && offset >= -255 && offset <= 255) {
            return true;
        } else {
            popError(message("InvalidParameter"));
            return false;
        }
    }

    @Override
    protected void handleImage() {

        try {
            ImageContrast imageContrast = new ImageContrast(imageView.getImage(), contrastAlgorithm);
            if (contrastAlgorithm == ContrastAlgorithm.Gray_Histogram_Stretching) {
                imageContrast.setIntPara1(left).setIntPara2(right);
                opInfo = left + "-" + right;

            } else if (contrastAlgorithm == ContrastAlgorithm.Gray_Histogram_Shifting) {
                imageContrast.setIntPara1(offset);
                opInfo = offset + "";
            }

            handledImage = imageContrast.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
        }
    }

    @FXML
    protected void demo() {
        if (!checkOptions()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        reset();
        task = new SingletonCurrentTask<Void>(this) {
            private Image demoImage = null;

            @Override
            protected boolean handle() {
                try {
//                    demoImage = ScaleTools.demoImage(srcImage());
//                    demoImage = handleImage(demoImage, scope());
                    return demoImage != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                ImagePopController.openImage(myController, demoImage);
            }

        };
        start(task);
    }


    /*
        static methods
     */
    public static ImageContrastController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageContrastController controller = (ImageContrastController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageContrastFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
