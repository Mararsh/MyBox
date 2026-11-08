package mara.mybox.controller;

import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.IntPoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.image.ImageDemos;
import mara.mybox.fxml.image.ImageViewTools;
import mara.mybox.image.data.ImageLiquify;
import mara.mybox.image.data.ImageLiquify.FalloffAlgorithm;
import mara.mybox.image.data.ImageLiquify.LiquifyOperation;
import mara.mybox.image.data.PixelSampler.EdgeMode;
import mara.mybox.image.data.PixelSampler.SamplingAlgorithm;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2026-11-1
 * @License Apache License Version 2.0
 */
public class ImageLiquifyController extends BaseImageEditController {

    protected IntPoint startPoint, endPoint;
    protected float strengthMax = 1.0f, strengthThreshold = 0.05f, hardness = 0.5f;
    protected LiquifyOperation liquifyOperation = LiquifyOperation.ROTATE_CLOCKWISE;
    protected FalloffAlgorithm brushKernel = FalloffAlgorithm.SMOOTH_QUADRATIC;
    protected ImageLiquify liquify;
    protected SamplingAlgorithm sampleMode = SamplingAlgorithm.BilinearInterpolation;
    protected EdgeMode edgeMode = EdgeMode.CLAMP_EDGE;

    @FXML
    protected ControlImageLiquify liquifyController;

    public ImageLiquifyController() {
        baseTitle = message("Liquify");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Liquify");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void mousePressed(MouseEvent event) {
        if (imageView == null || imageView.getImage() == null
                || isPickingColor) {
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        if (p == null) {
            return;
        }
        scrollPane.setPannable(false);
        lastPoint = p;
    }

    @FXML
    @Override
    public void mouseReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (imageView == null || imageView.getImage() == null
                || isPickingColor || lastPoint == null) {
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        if (p == null) {
            return;
        }
        startPoint = new IntPoint((int) lastPoint.getX(), (int) lastPoint.getY());
        endPoint = new IntPoint((int) p.getX(), (int) p.getY());
        okAction();
        lastPoint = null;
    }

    @Override
    public boolean checkOptions() {
        try {
            if (!super.checkOptions() || !liquifyController.pickValues()) {
                return false;
            }
            if (liquifyController.pushRadio.isSelected()) {
                liquifyOperation = LiquifyOperation.PUSH;
            } else if (liquifyController.expandRadio.isSelected()) {
                liquifyOperation = LiquifyOperation.EXPAND;
            } else if (liquifyController.shrinkRadio.isSelected()) {
                liquifyOperation = LiquifyOperation.SHRINK;
            } else if (liquifyController.rotateRadio.isSelected()) {
                liquifyOperation = LiquifyOperation.ROTATE_CLOCKWISE;
            } else if (liquifyController.rotateCounterRadio.isSelected()) {
                liquifyOperation = LiquifyOperation.ROTATE_COUNTER_CLOCKWISE;
            } else if (liquifyController.turbulenceRadio.isSelected()) {
                liquifyOperation = LiquifyOperation.TURBULENCE;
            } else {
                liquifyOperation = LiquifyOperation.PUSH;
            }
            liquify = new ImageLiquify()
                    .setStartPoint(startPoint)
                    .setEndPoint(endPoint)
                    .setOperation(liquifyOperation)
                    .setFallOff(FalloffAlgorithm.SMOOTH_QUADRATIC)
                    .setSampler(sampleMode, edgeMode)
                    .setRadius(liquifyController.radius)
                    .setStep(liquifyController.step)
                    .setSafeMargin(liquifyController.safeMargin)
                    .setStrengthMax(strengthMax)
                    .setStrengthThreshold(strengthThreshold)
                    .setHardness(hardness)
                    .setTurbulenceSeed(System.currentTimeMillis())
                    .setTurbulencePower(0.12f)
                    .setTurbulenceFreq(0.028f)
                    .setTurbulenceOctaves(3);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    protected void handleImage(FxTask currentTask) {
        opInfo = message("StartPoint") + ":" + startPoint.text() + " "
                + message("EndPoint") + ":" + endPoint.text();
        handledImage = liquify.render(imageView.getImage(), currentTask);
        liquify = null;
    }

    @Override
    protected void makeDemoFiles(FxTask currentTask, List<String> files, Image demoImage) {
        ImageDemos.shear(currentTask, files, SwingFXUtils.fromFXImage(demoImage, null), srcFile());
    }


    /*
        static methods
     */
    public static ImageLiquifyController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageLiquifyController controller = (ImageLiquifyController) WindowTools.referredStage(
                    parent, Fxmls.ImageLiquifyFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
