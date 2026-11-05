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
import mara.mybox.fxml.image.LiquifyTools;
import mara.mybox.image.data.PixelSampler.SampleMode;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2026-11-1
 * @License Apache License Version 2.0
 */
public class ImageLiquifyController extends BaseImageEditController {

    protected IntPoint startPoint, endPoint;
    protected int radius = 50, step = 20;
    protected float strengthMax = 1.0f, strengthThreshold = 0.05f;
    protected boolean isGauss = false;

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
        if (!super.checkOptions()) {
            return false;
        }
        return true;
    }

    @Override
    protected void handleImage(FxTask currentTask) {
        opInfo = message("StartPoint") + ":" + startPoint.text() + " "
                + message("EndPoint") + ":" + endPoint.text();
        handledImage = LiquifyTools.forwardWarp(currentTask, imageView.getImage(),
                startPoint, endPoint, SampleMode.BILINEAR,
                radius, step, strengthMax, strengthThreshold, isGauss);
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
