package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageSmoothController extends BasePixelsController {

    protected int intensity;

    @FXML
    protected ComboBox<String> intensitySelector;
    @FXML
    protected RadioButton avarageRadio, gaussianRadio, motionRadio;

    public ImageSmoothController() {
        baseTitle = message("Smooth");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Smooth");

            intensity = UserConfig.getInt(baseName + "Intensity", 10);
            if (intensity <= 0) {
                intensity = 10;
            }
            intensitySelector.getItems().addAll(Arrays.asList("3", "5", "10", "2", "1", "8", "15", "20", "30"));
            intensitySelector.getSelectionModel().select(intensity + "");
            intensitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            intensity = v;
                            UserConfig.setInt(baseName + "Intensity", intensity);
                            ValidationTools.setEditorNormal(intensitySelector);
                        } else {
                            ValidationTools.setEditorBadStyle(intensitySelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(intensitySelector);
                    }
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        if (intensity > 0) {
            return true;
        } else {
            popError(message("InvalidParameter"));
            return false;
        }
    }

    @Override
    protected Image handleImage(Image inImage, ImageScope inScope) {
        try {
            ConvolutionKernel kernel;
            if (avarageRadio.isSelected()) {
                kernel = ConvolutionKernel.makeAverageBlur(intensity);
            } else if (gaussianRadio.isSelected()) {
                kernel = ConvolutionKernel.makeGaussBlur(intensity);
            } else if (motionRadio.isSelected()) {
                kernel = ConvolutionKernel.makeMotionBlur(intensity);
            } else {
                return null;
            }
            ImageConvolution convolution = ImageConvolution.create();
            convolution.setImage(inImage)
                    .setScope(inScope)
                    .setKernel(kernel)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent())
                    .setTask(task);
            opInfo = message("Intensity") + ": " + intensity;
            return convolution.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    /*
        static methods
     */
    public static ImageSmoothController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageSmoothController controller = (ImageSmoothController) WindowTools.branchStage(
                    parent, Fxmls.ImageSmoothFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
