package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ImageDemoTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageSharpenController extends BasePixelsController {

    @FXML
    protected ControlImageSharpen sharpenController;

    public ImageSharpenController() {
        baseTitle = message("Sharpen");
    }

    @Override
    protected Image handleImage(Image inImage, ImageScope inScope) {
        try {
            ConvolutionKernel kernel = sharpenController.kernel();
            if (kernel == null) {
                return null;
            }
            ImageConvolution convolution = ImageConvolution.create();
            convolution.setImage(inImage)
                    .setScope(inScope)
                    .setKernel(kernel)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent())
                    .setTask(task);
            operation = message("Sharpen");
            opInfo = message("Intensity") + ": " + sharpenController.intensity;
            return convolution.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    @Override
    protected void makeDemoFiles(List<String> files, Image demoImage) {
        try {
            ImageConvolution convolution = ImageConvolution.create();
            convolution.setImage(demoImage)
                    .setScope(scope())
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent());
            ImageDemoTools.sharpen(demoTask, files, convolution);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            ImageSharpenController controller = (ImageSharpenController) WindowTools.branch(
                    parent.getMyWindow(), Fxmls.ImageSharpenFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
