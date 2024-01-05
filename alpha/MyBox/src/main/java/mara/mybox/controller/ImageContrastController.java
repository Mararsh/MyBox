package mara.mybox.controller;

import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageContrast;
import mara.mybox.bufferedimage.ImageContrast.ContrastAlgorithm;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.PixelDemos;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageContrastController extends BaseImageEditController {

    protected ImageContrast contrast;

    @FXML
    protected ControlImageContrast contrastController;

    public ImageContrastController() {
        baseTitle = message("Contrast");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Contrast");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded()) {
                return false;
            }
            contrastController.forImage(srcImage());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    protected boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        contrast = contrastController.pickValues();
        return contrast != null;
    }

    @Override
    protected void handleImage(FxTask currentTask) {
        try {
            if (contrastController.contrastAlgorithm == ContrastAlgorithm.GrayHistogramStretching) {
                opInfo = contrastController.threshold + "-" + contrastController.percentage;

            } else if (contrastController.contrastAlgorithm == ContrastAlgorithm.GrayHistogramShifting) {
                opInfo = contrastController.offset + "";
            }
            handledImage = contrast.setImage(srcImage()).setTask(currentTask).startFx();
        } catch (Exception e) {
            displayError(e.toString());
        }
    }

    @Override
    protected void makeDemoFiles(FxTask currentTask, List<String> files, Image demoImage) {
        PixelDemos.contrast(currentTask, files,
                SwingFXUtils.fromFXImage(demoImage, null), srcFile());
    }

    /*
        static methods
     */
    public static ImageContrastController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageContrastController controller = (ImageContrastController) WindowTools.branchStage(
                    parent, Fxmls.ImageContrastFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
