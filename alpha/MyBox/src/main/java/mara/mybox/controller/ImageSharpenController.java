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
public class ImageSharpenController extends BaseScopeController {

    protected int intensity;

    @FXML
    protected ComboBox<String> intensitySelector;
    @FXML
    protected RadioButton unmaskRadio, eightRadio, fourRadio;

    public ImageSharpenController() {
        baseTitle = message("Sharpen");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            if (editor == null) {
                close();
                return;
            }
            intensity = UserConfig.getInt(baseName + "Intensity", 2);
            if (intensity <= 0) {
                intensity = 2;
            }
            intensitySelector.getItems().addAll(Arrays.asList("2", "1", "3", "4"));
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

            intensitySelector.disableProperty().bind(unmaskRadio.selectedProperty().not());

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
            if (unmaskRadio.isSelected()) {
                kernel = ConvolutionKernel.makeUnsharpMasking(intensity);
            } else if (eightRadio.isSelected()) {
                kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
            } else if (fourRadio.isSelected()) {
                kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
            } else {
                return null;
            }
            ImageConvolution convolution = ImageConvolution.create();
            convolution.setImage(inImage)
                    .setScope(inScope)
                    .setKernel(kernel)
                    .setExcludeScope(excludeRadio.isSelected())
                    .setSkipTransparent(ignoreTransparentCheck.isSelected());
            operation = message("Sharpen");
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
    public static ImageSharpenController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageSharpenController controller = (ImageSharpenController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageSharpenFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
