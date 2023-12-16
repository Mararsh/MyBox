package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.TransformTools;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageRotateController extends BaseImageEditController {

    @FXML
    protected ComboBox angleSelector;
    @FXML
    protected Slider angleSlider;

    public ImageRotateController() {
        baseTitle = message("Rotate");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Rotate");

            angleSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    rotate(newValue.intValue());
                }
            });

            rotateAngle = UserConfig.getInt("ImageRotateAngle", 45);
            angleSelector.getItems().addAll(Arrays.asList(
                    "45", "-45", "90", "-90", "180", "-180", "30", "-30", "60", "-60",
                    "120", "-120", "15", "-15", "5", "-5", "10", "-10", "1", "-1",
                    "75", "-75", "135", "-135"));
            angleSelector.setVisibleRowCount(10);
            angleSelector.setValue(rotateAngle + "");
            angleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        rotateAngle = Integer.parseInt(newValue);
                        UserConfig.setInt("ImageRotateAngle", rotateAngle);
                        ValidationTools.setEditorNormal(angleSelector);
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(angleSelector);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded() || image == null) {
                return false;
            }
            rotate(0);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @FXML
    @Override
    public void goAction() {
        rotate(rotateAngle);
    }

    @Override
    public void rotate(int angle) {
        currentAngle = angle;
        imageView.setRotate(angle);
    }

    @Override
    protected void handleImage(FxTask currentTask) {
        opInfo = currentAngle + "";
        handledImage = TransformTools.rotateImage(currentTask, currentImage(), currentAngle);
    }

    /*
        static methods
     */
    public static ImageRotateController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageRotateController controller = (ImageRotateController) WindowTools.branchStage(
                    parent, Fxmls.ImageRotateFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
